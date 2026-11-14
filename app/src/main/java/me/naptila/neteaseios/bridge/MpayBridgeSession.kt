package me.naptila.neteaseios.bridge

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import me.naptila.neteaseios.MainActivity
import org.json.JSONObject
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Short-lived loopback MPay iOS login bridge for NetEase UniSDK/GMBridge5 games.
 * Never receives passwords, OTPs or login tokens.
 */
class MpayBridgeSession(
    context: Context,
    private val target: GameTarget,
    private val capture: Boolean,
    private val transportMode: SeedTransportMode = SeedTransportMode.STANDARD,
    private val listener: Listener
) : AutoCloseable {

    interface Listener {
        fun onIdentified(gameId: String)
        fun onSubmitted(confirmed: Boolean, usedSaf: Boolean)
        fun onFailed(message: String)
    }

    private data class ContextIdentity(
        val uid: String?,
        val gameId: String
    )

    private val appContext: Context = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private val resultDelivered = AtomicBoolean(false)
    private val closed = AtomicBoolean(false)
    private val server: ServerSocket
    private val nonce: String
    private val base: String
    private val host: String
    private val seedUid: String?
    private val expectedGameId: String?
    private var preparedSeed: PreparedSeed? = null
    @Volatile
    private var unconfirmedGraceDelete = false

    init {
        val p = prefs(appContext)
        if (capture) {
            seedUid = null
            expectedGameId = target.spec.gameId
        } else {
            val savedUid = p.getString("uid", null)
            val savedPkg = p.getString("captured_package", null)
            val savedAt = p.getLong("captured_at", 0L)
            if (savedUid == null ||
                savedPkg != target.spec.packageName ||
                System.currentTimeMillis() - savedAt > BridgeProtocol.IDENTITY_TTL_MS
            ) {
                throw IllegalStateException("identity-expired")
            }
            seedUid = savedUid
            expectedGameId = target.spec.gameId ?: p.getString("captured_game_id", null)
        }

        val random = ByteArray(24).also { SecureRandom().nextBytes(it) }
        nonce = random.joinToString("") { "%02x".format(Locale.ROOT, it.toInt() and 0xff) }
        base = "/bridge/$nonce"

        server = ServerSocket().apply {
            bind(InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0))
            soTimeout = 1000
        }
        host = "127.0.0.1:${server.localPort}"

        if (!capture) {
            try {
                writeSeed()
            } catch (e: Exception) {
                runCatching { server.close() }
                SeedStorage.deleteAsync(appContext, preparedSeed?.cleanupUri?.toString())
                throw e
            }
        }
    }

    fun start() {
        val pending = prefs(appContext).edit()
            .putString("state", if (capture) "identifying" else "preparing")
            .putString("state_package", target.spec.packageName)
            .remove("last_error")
            .putString("operation", nonce)
            .remove("completed_operation")
        if (capture) {
            pending
                .remove("uid")
                .remove("captured_at")
                .remove("captured_package")
                .remove("captured_game_id")
        }
        pending.apply()

        Thread(::serve, "NeteaseLocalBridge").apply {
            isDaemon = true
            start()
        }

        main.postDelayed({
            if (closed.get()) return@postDelayed
            if (!resultDelivered.get()) {
                fail(
                    if (capture) "未能识别账号，请先在「${target.appLabel}」正常登录，再回来重试。"
                    else "未能完成 iOS 登录准备，请返回助手重试。"
                )
            }
            close()
        }, 45_000L)

        // Opening a fresh container after the previous one has finished avoids stale static WebView state.
        main.postDelayed({
            if (closed.get()) return@postDelayed
            val intent = Intent().apply {
                component = ComponentName(target.spec.packageName, target.gmActivityClass)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("GM_WEBVIEW_URL", "http://$host$base/page")
            }
            try {
                appContext.startActivity(intent)
            } catch (_: Exception) {
                fail("无法拉起「${target.appLabel}」网页容器，该游戏大概率不支持此登录方式（仅支持开放网页桥接口的网易官服游戏）。")
                close()
            }
        }, 1500L)
    }

    private fun serve() {
        while (!closed.get()) {
            try {
                server.accept().use { socket ->
                    if (!socket.inetAddress.isLoopbackAddress) return@use
                    socket.soTimeout = 2500

                    val request = HttpRequest.read(socket.getInputStream())
                    val correctHost = host == request.headers["host"]
                    val origin = request.headers["origin"]
                    if (!correctHost || (origin != null && origin != "http://$host") || !request.path.startsWith("$base/")) {
                        reply(socket, 403, "text/plain", "Unavailable")
                        return@use
                    }

                    val route = request.path.substring(base.length)
                    if (request.method == "GET" && route == "/page") {
                        reply(socket, 200, "text/html; charset=utf-8", page())
                        return@use
                    }
                    if (request.method != "POST") {
                        reply(socket, 404, "text/plain", "Unavailable")
                        return@use
                    }

                    val input = try {
                        JSONObject(String(request.body, StandardCharsets.UTF_8))
                    } finally {
                        request.body.fill(0)
                    }

                    when (route) {
                        "/identity" -> {
                            if (!capture) {
                                reply(socket, 403, "application/json", "{\"ok\":false}")
                                return@use
                            }
                            val parsed = readIdentity(input, allowEmpty = false)
                            val uid = parsed.uid ?: throw IOException("identity-missing")
                            val first = resultDelivered.compareAndSet(false, true)
                            if (first) {
                                prefs(appContext).edit()
                                    .putString("uid", uid)
                                    .putLong("captured_at", System.currentTimeMillis())
                                    .putString("captured_package", target.spec.packageName)
                                    .putString("captured_game_id", parsed.gameId)
                                    .putString("last_game_id_${target.spec.packageName}", parsed.gameId)
                                    .putString("state", "identified")
                                    .putString("state_package", target.spec.packageName)
                                    .putString("completed_operation", nonce)
                                    .apply()
                            }
                            reply(socket, 200, "application/json", "{\"ok\":true}")
                            if (first) {
                                main.post { listener.onIdentified(parsed.gameId) }
                                main.postDelayed({
                                    val back = Intent(appContext, MainActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    runCatching { appContext.startActivity(back) }
                                }, 1800L)
                            }
                        }

                        "/validate" -> {
                            if (capture) {
                                reply(socket, 403, "application/json", "{\"ok\":false}")
                                return@use
                            }
                            val parsed = readIdentity(input, allowEmpty = true)
                            val allowed = parsed.uid == null || seedUid == parsed.uid
                            reply(socket, 200, "application/json", if (allowed) "{\"ok\":true}" else "{\"ok\":false}")
                        }

                        "/submitted" -> {
                            if (capture) {
                                reply(socket, 403, "application/json", "{\"ok\":false}")
                                return@use
                            }
                            val confirmed = input.optBoolean("confirmed", false)
                            val usedSaf = preparedSeed?.usedSaf == true
                            val first = resultDelivered.compareAndSet(false, true)
                            if (first) {
                                unconfirmedGraceDelete = !confirmed
                                prefs(appContext).edit()
                                    .putString("state", if (confirmed) "submitted" else "unconfirmed")
                                    .putString("state_package", target.spec.packageName)
                                    .putString("completed_operation", nonce)
                                    .apply()
                            }
                            reply(socket, 200, "application/json", "{\"ok\":true}")
                            if (first) {
                                main.post { listener.onSubmitted(confirmed, usedSaf) }
                            }
                        }

                        "/failed" -> {
                            val reason = input.optString("reason")
                            val message = when (reason) {
                                "identity_changed" -> "游戏内当前账号或目标客户端已改变，请重新识别。"
                                "no_callback" -> "没有收到游戏接口响应，请返回游戏关闭当前页面后重试。"
                                "bridge_missing" -> "游戏网页桥未就绪，请先打开「${target.appLabel}」再重试。"
                                "sdk_read_failed" -> "SDK 未能读取准备文件，该游戏可能不支持当前写入方式。"
                                "sdk_invalid_session" -> "SDK 已读取文件，但未接受账号准备格式，当前游戏仍需适配。"
                                "sdk_rejected" -> "SDK 拒绝了本次登录准备，当前游戏仍需适配。"
                                else -> if (capture) {
                                    "未读取到已登录的网易账号，请先在「${target.appLabel}」完成登录。"
                                } else {
                                    "本次准备未完成，请返回游戏关闭当前页面后重试。"
                                }
                            }
                            reply(socket, 200, "application/json", "{\"ok\":true}")
                            fail(message)
                        }

                        else -> reply(socket, 404, "text/plain", "Unavailable")
                    }
                }
            } catch (_: SocketTimeoutException) {
            } catch (_: Exception) {
            }
        }
    }

    private fun readIdentity(input: JSONObject, allowEmpty: Boolean): ContextIdentity {
        val game = input.optString("game")
        if (!BridgeProtocol.supportedSource(game = game, expectedGameId = expectedGameId)) {
            throw IOException("source")
        }
        val full = input.optString("full")
        if (full.isEmpty() && allowEmpty) {
            return ContextIdentity(uid = null, gameId = game)
        }
        return ContextIdentity(
            uid = BridgeProtocol.uidFromQualifiedName(full),
            gameId = game
        )
    }

    private fun writeSeed() {
        val fileName = "ntes_mpay_${UUID.randomUUID().toString().replace("-", "")}.tmp"

        val user = JSONObject()
            .put("id", seedUid)
            .put("token", "")
            .put("need_mask", true)
            .put("login_type", BridgeProtocol.DEFAULT_LOGIN_TYPE)
            .put("login_channel", BridgeProtocol.DEFAULT_LOGIN_CHANNEL)
            .put(
                "pc_ext_info",
                JSONObject()
                    .put("src_client_type", BridgeProtocol.IOS_CLIENT_TYPE)
                    .put("is_from_ticket_login", true)
            )
        val bytes = (JSONObject().put("user", user).toString() + "\n")
            .toByteArray(StandardCharsets.UTF_8)

        val seed = try {
            SeedStorage.write(appContext, target, fileName, bytes, transportMode)
        } finally {
            bytes.fill(0)
        }
        preparedSeed = seed

        val cleanupUri = seed.cleanupUri.toString()
        main.postDelayed({ SeedStorage.deleteAsync(appContext, cleanupUri) }, 45_000L)
    }

    private fun page(): String {
        val payload = if (capture) {
            "null"
        } else {
            val seed = preparedSeed ?: throw IOException("seed-missing")
            JSONObject()
                .put("methodId", "execute_extend_func")
                .put(
                    "reqData",
                    JSONObject()
                        .put("channel", BridgeProtocol.DEFAULT_LOGIN_CHANNEL)
                        .put("methodId", "loadMpaySession")
                        .put("path", seed.sdkPath)
                        .put("callback_mode", "web")
                )
                .toString()
        }
        return BridgePage.render(
            capture = capture,
            appLabel = target.appLabel,
            base = "http://$host$base",
            payload = payload
        )
    }

    private fun fail(message: String) {
        if (!resultDelivered.compareAndSet(false, true)) return
        prefs(appContext).edit()
            .putString("state", "failed")
            .putString("state_package", target.spec.packageName)
            .putString("last_error", message)
            .remove("completed_operation")
            .apply()
        main.post { listener.onFailed(message) }
    }

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            runCatching { server.close() }
            val cleanupUri = preparedSeed?.cleanupUri?.toString()
            if (cleanupUri != null) {
                if (unconfirmedGraceDelete) {
                    main.postDelayed({ SeedStorage.deleteAsync(appContext, cleanupUri) }, 5_000L)
                } else {
                    SeedStorage.deleteAsync(appContext, cleanupUri)
                }
            }
        }
    }

    companion object {
        private const val PREFS = "netease_mpay_bridge"

        fun prefs(context: Context): SharedPreferences =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        fun cleanup(context: Context) {
            val appCtx = context.applicationContext
            Thread({ SeedStorage.cleanup(appCtx) }, "NeteaseSeedBootCleanup").apply {
                isDaemon = true
                start()
            }
        }

        private fun reply(socket: Socket, status: Int, type: String, text: String) {
            val body = text.toByteArray(StandardCharsets.UTF_8)
            val statusText = if (status == 200) "OK" else "Unavailable"
            val header = "HTTP/1.1 $status $statusText\r\n" +
                "Content-Type: $type\r\n" +
                "Content-Length: ${body.size}\r\n" +
                "Cache-Control: no-store\r\n" +
                "Connection: close\r\n" +
                "Content-Security-Policy: default-src 'none';connect-src 'self';script-src 'unsafe-inline';style-src 'unsafe-inline'\r\n\r\n"
            val out = socket.getOutputStream()
            out.write(header.toByteArray(StandardCharsets.US_ASCII))
            out.write(body)
            out.flush()
        }
    }
}
