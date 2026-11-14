package me.naptila.neteaseios

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.naptila.neteaseios.bridge.BridgeProtocol
import me.naptila.neteaseios.bridge.DirectoryAccessRequired
import me.naptila.neteaseios.bridge.GameSpec
import me.naptila.neteaseios.bridge.GameTarget
import me.naptila.neteaseios.bridge.InstalledGame
import me.naptila.neteaseios.bridge.MpayBridgeSession
import me.naptila.neteaseios.bridge.SeedStorage
import me.naptila.neteaseios.bridge.SeedStorageException
import me.naptila.neteaseios.bridge.SeedTransportMode
import me.naptila.neteaseios.ui.BridgeScreen
import me.naptila.neteaseios.ui.BridgeUiState
import me.naptila.neteaseios.ui.StatusTone
import me.naptila.neteaseios.ui.theme.NeteaseiosTheme

/** Generic NetEase MPay iOS account login helper; uses the target game's own login UI. */
class MainActivity : ComponentActivity() {

    private var uiState by mutableStateOf(BridgeUiState())
    private var session: MpayBridgeSession? = null
    private var directorySpec: GameSpec? = null
    private var resumePrepareAfterGrant: Boolean = false

    private val directoryPicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val spec = directorySpec
            val shouldResumePrepare = resumePrepareAfterGrant
            directorySpec = null
            resumePrepareAfterGrant = false

            val uri = result.data?.data
            if (spec == null || result.resultCode != RESULT_OK || uri == null) {
                if (shouldResumePrepare) {
                    complete("未授权目录，本次登录准备已取消。", StatusTone.Warning)
                } else {
                    complete("已取消目录授权。", StatusTone.Neutral)
                }
            } else {
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            SeedStorage.saveDirectoryGrant(
                                context = this@MainActivity,
                                packageName = spec.packageName,
                                uri = uri,
                                flags = result.data?.flags ?: 0
                            )
                        }
                        uiState = uiState.copy(busy = false)
                        refreshFromPrefs(updateStatusMessage = false)

                        if (shouldResumePrepare) {
                            if (uiState.currentGame?.spec?.packageName == spec.packageName) {
                                begin(capture = false)
                            } else {
                                showMessage("已保存目录授权，请重新选择目标游戏后再执行。", StatusTone.Warning)
                            }
                        } else {
                            showMessage("已保存「${spec.displayName}」的共享目录授权。", StatusTone.Success)
                        }
                    } catch (e: Exception) {
                        val msg = if (e is SeedStorageException) {
                            e.message.orEmpty().ifBlank { "目录授权失败，请重新选择。" }
                        } else {
                            "目录授权失败，请重新选择。"
                        }
                        complete(msg, StatusTone.Warning)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MpayBridgeSession.cleanup(this)
        val savedGame = loadSavedGameSpec()?.let { GameTarget.inspect(this, it, loadIcon = true) }

        uiState = uiState.copy(currentGame = savedGame)
        savedInstanceState?.getString("directory_package")?.let { pkg ->
            if (BridgeProtocol.isValidPackageName(pkg)) {
                directorySpec = GameSpec(pkg, savedInstanceState.getString("directory_name") ?: pkg)
                resumePrepareAfterGrant = savedInstanceState.getBoolean("directory_resume_prepare", false)
                uiState = uiState.copy(busy = true)
            }
        }
        refreshFromPrefs(updateStatusMessage = true)
        loadInstalledAppsAsync()

        setContent {
            LaunchedEffect(uiState.identityFresh, uiState.currentGame?.spec?.packageName) {
                while (uiState.identityFresh) {
                    val pkg = uiState.currentGame?.spec?.packageName ?: break
                    delay(1_000L)
                    val rem = remainingIdentitySeconds(pkg)
                    if (rem <= 0 && uiState.identityRemainingSec > 0) {
                        refreshFromPrefs(updateStatusMessage = !uiState.busy)
                    } else {
                        uiState = uiState.copy(identityRemainingSec = rem)
                    }
                }
            }

            NeteaseiosTheme {
                BridgeScreen(
                    state = uiState,
                    onOpenGame = ::openTargetGame,
                    onSelectInstalledGame = ::selectInstalledGame,
                    onRefreshApps = ::loadInstalledAppsAsync,
                    onIdentifyAccount = { begin(capture = true) },
                    onOpenSwitchWindow = ::openSwitchAccountWindow,
                    onSelectTransportMode = ::selectTransportMode,
                    onAuthorizeSafDirectory = ::authorizeCurrentGameDirectory,
                    onPrepareIosLogin = { begin(capture = false) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (session == null && !uiState.busy) {
            MpayBridgeSession.cleanup(this)
        }
        val current = uiState.currentGame
        if (current != null) {
            uiState = uiState.copy(
                currentGame = GameTarget.inspect(this, current.spec, loadIcon = true)
            )
        }
        refreshFromPrefs(updateStatusMessage = !uiState.busy)
    }

    override fun onDestroy() {
        session?.close()
        session = null
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        directorySpec?.let {
            outState.putString("directory_package", it.packageName)
            outState.putString("directory_name", it.displayName)
            outState.putBoolean("directory_resume_prepare", resumePrepareAfterGrant)
        }
        super.onSaveInstanceState(outState)
    }

    private fun loadInstalledAppsAsync() {
        if (uiState.loadingApps) return
        uiState = uiState.copy(loadingApps = true)
        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                GameTarget.listInstalledLauncherApps(this@MainActivity)
            }
            uiState = uiState.copy(
                installedApps = apps,
                loadingApps = false
            )
        }
    }

    private fun selectInstalledGame(game: InstalledGame) {
        if (uiState.busy) return
        val withIcon = GameTarget.inspect(this, game.spec, loadIcon = true)
        persistSelectedSpec(withIcon.spec)
        uiState = uiState.copy(currentGame = withIcon)
        refreshFromPrefs(updateStatusMessage = true)
    }

    private fun selectTransportMode(mode: SeedTransportMode) {
        if (uiState.busy) return
        val pkg = uiState.currentGame?.spec?.packageName
        if (pkg != null) {
            SeedStorage.setTransportMode(this, pkg, mode)
            val granted = SeedStorage.hasValidDirectoryGrant(this, pkg)
            uiState = uiState.copy(transportMode = mode, safAuthorized = granted)
        } else {
            uiState = uiState.copy(transportMode = mode)
        }
    }

    private fun authorizeCurrentGameDirectory() {
        if (uiState.busy) return
        val spec = uiState.currentGame?.spec ?: run {
            showMessage("请先在上方选择目标游戏应用。", StatusTone.Warning)
            return
        }
        launchDirectoryPicker(spec, resumePrepare = false)
    }

    private fun launchDirectoryPicker(spec: GameSpec, resumePrepare: Boolean) {
        if (isFinishing || isDestroyed) return
        session?.close()
        session = null
        directorySpec = spec
        resumePrepareAfterGrant = resumePrepare
        uiState = uiState.copy(busy = true)
        showMessage(
            "请选择内部存储的 Android/media 或 ${spec.packageName} 子目录，允许助手写入临时准备文件。",
            StatusTone.Warning
        )
        try {
            directoryPicker.launch(SeedStorage.directoryIntent())
        } catch (_: Exception) {
            directorySpec = null
            resumePrepareAfterGrant = false
            complete("系统目录选择器不可用，当前设备无法使用共享目录方式。", StatusTone.Error)
        }
    }

    private fun loadSavedGameSpec(): GameSpec? {
        val p = MpayBridgeSession.prefs(this)
        val pkg = p.getString("selected_package", null)
            ?.takeIf { BridgeProtocol.isValidPackageName(it) }
            ?: return null
        val name = p.getString("selected_name", pkg) ?: pkg
        return GameSpec(
            packageName = pkg,
            displayName = name
        )
    }

    private fun persistSelectedSpec(spec: GameSpec) {
        MpayBridgeSession.prefs(this).edit()
            .putString("selected_package", spec.packageName)
            .putString("selected_name", spec.displayName)
            .apply()
    }

    private fun openTargetGame() {
        val spec = uiState.currentGame?.spec ?: run {
            showMessage("请先在上方选择目标游戏应用。", StatusTone.Warning)
            return
        }
        try {
            if (!GameTarget.open(this, spec.packageName)) {
                showMessage("无法打开「${spec.displayName}」。", StatusTone.Error)
            }
        } catch (_: Exception) {
            showMessage("无法打开「${spec.displayName}」，请手动启动游戏。", StatusTone.Warning)
        }
    }

    private fun openSwitchAccountWindow() {
        val spec = uiState.currentGame?.spec ?: run {
            showMessage("请先在上方选择目标游戏应用。", StatusTone.Warning)
            return
        }
        try {
            if (!GameTarget.open(this, spec.packageName)) {
                showMessage("无法打开「${spec.displayName}」。", StatusTone.Error)
            }
        } catch (_: Exception) {
            showMessage("请手动打开「${spec.displayName}」，并停在网易登录窗口。", StatusTone.Warning)
        }
    }

    private fun begin(capture: Boolean) {
        if (uiState.busy) return
        val spec = uiState.currentGame?.spec ?: run {
            showMessage("请先在上方选择目标游戏应用。", StatusTone.Warning)
            return
        }
        if (!capture && remainingIdentitySeconds(spec.packageName) <= 0) {
            showMessage("请先在「${spec.displayName}」登录所需账号，再点「识别当前账号」。", StatusTone.Warning)
            return
        }

        val selectedMode = uiState.transportMode
        val waitingText = if (capture) {
            "正在打开「${spec.displayName}」读取当前账号…"
        } else {
            "正在准备 iOS 登录；返回「${spec.displayName}」后请验证同一账号。"
        }

        uiState = uiState.copy(
            busy = true,
            statusMessage = waitingText,
            statusTone = StatusTone.Neutral
        )

        Thread({
            try {
                val target = GameTarget.verify(this, spec)
                val next = MpayBridgeSession(
                    context = this,
                    target = target,
                    capture = capture,
                    transportMode = selectedMode,
                    listener = object : MpayBridgeSession.Listener {
                        override fun onIdentified(gameId: String) {
                            complete(
                                text = "已识别当前网易账号（代号 $gameId）。请到「${target.appLabel}」点「切换账号」，停在登录窗口后回来准备 iOS 登录。",
                                tone = StatusTone.Success
                            )
                        }

                        override fun onSubmitted(confirmed: Boolean, usedSaf: Boolean) {
                            val text = if (confirmed) {
                                "SDK 已接受登录准备。请在「${target.appLabel}」点击账号列表右上角的「×」，刷新账号列表后重新登录并核对角色。"
                            } else {
                                "准备请求已发送，但未收到 SDK 确认。请在「${target.appLabel}」先点账号列表右上角的「×」关闭并重开刷新，再重新验证同一账号并核对实际角色。"
                            }
                            complete(
                                text = text,
                                tone = if (confirmed) StatusTone.Success else StatusTone.Warning
                            )
                        }

                        override fun onFailed(message: String) {
                            complete(text = message, tone = StatusTone.Error)
                        }
                    }
                )
                runOnUiThread {
                    if (isFinishing || isDestroyed) {
                        next.close()
                        return@runOnUiThread
                    }
                    session?.close()
                    session = next
                    refreshFromPrefs(updateStatusMessage = false)
                    next.start()
                }
            } catch (_: PackageManager.NameNotFoundException) {
                runOnUiThread { complete("未找到已安装的「${spec.displayName}」。", StatusTone.Error) }
            } catch (_: DirectoryAccessRequired) {
                runOnUiThread {
                    launchDirectoryPicker(spec, resumePrepare = true)
                }
            } catch (e: SeedStorageException) {
                runOnUiThread {
                    complete(e.message ?: "共享目录写入失败。", StatusTone.Error)
                }
            } catch (e: IllegalStateException) {
                val text = if (e.message == "identity-expired") {
                    "当前游戏的账号识别已过期，请重新识别当前账号。"
                } else {
                    "「${spec.displayName}」缺少必要的客户端运行环境，该游戏不支持此登录方式。"
                }
                runOnUiThread { complete(text, StatusTone.Warning) }
            } catch (_: Exception) {
                runOnUiThread {
                    complete(
                        "未能完成「${spec.displayName}」的准备文件写入，请检查目录授权与剩余空间后重试。",
                        StatusTone.Error
                    )
                }
            }
        }, "NeteaseBridgeSetup").apply {
            isDaemon = true
            start()
        }
    }

    private fun complete(text: String, tone: StatusTone) {
        session?.close()
        session = null
        uiState = uiState.copy(busy = false)
        refreshFromPrefs(updateStatusMessage = false)
        showMessage(text, tone)
    }

    private fun showMessage(text: String, tone: StatusTone) {
        uiState = uiState.copy(
            statusMessage = text,
            statusTone = tone
        )
    }

    private fun refreshFromPrefs(updateStatusMessage: Boolean) {
        val current = uiState.currentGame
        if (current == null) {
            uiState = uiState.copy(
                detectedGameId = null,
                identityRemainingSec = 0,
                submittedCurrent = false,
                unconfirmedCurrent = false,
                safAuthorized = false,
                statusMessage = if (updateStatusMessage && !uiState.busy) "请先在上方选择目标网易游戏。" else uiState.statusMessage,
                statusTone = if (updateStatusMessage && !uiState.busy) StatusTone.Neutral else uiState.statusTone
            )
            return
        }

        val pkg = current.spec.packageName
        val gameName = current.spec.displayName
        val remainingSec = remainingIdentitySeconds(pkg)
        val p = MpayBridgeSession.prefs(this)
        val detectedGid = p.getString("last_game_id_$pkg", null)
        val stateForCurrentPkg = p.getString("state_package", null) == pkg
        val state = if (stateForCurrentPkg) (p.getString("state", "") ?: "") else ""
        val currentOpMatches = p.getString("operation", "new") == p.getString("completed_operation", "old")
        val submittedCurrent = state == "submitted" && currentOpMatches
        val unconfirmedCurrent = state == "unconfirmed" && currentOpMatches
        val mode = SeedStorage.getTransportMode(this, pkg)
        val safAuthorized = SeedStorage.hasValidDirectoryGrant(this, pkg)

        var message = uiState.statusMessage
        var tone = uiState.statusTone

        if (updateStatusMessage && !uiState.busy) {
            when {
                state == "identified" && remainingSec > 0 -> {
                    message = "「$gameName」账号已识别。请到游戏内点「切换账号」，再回来准备 iOS 登录。"
                    tone = StatusTone.Success
                }

                submittedCurrent -> {
                    message = "SDK 已接受登录准备，请在「$gameName」点账号列表右上角的「×」刷新后重新登录并核对角色。"
                    tone = StatusTone.Success
                }

                unconfirmedCurrent -> {
                    message = "准备请求已发送，但未收到 SDK 确认。请在「$gameName」点账号列表右上角的「×」刷新列表、重新验证同一账号并核对角色。"
                    tone = StatusTone.Warning
                }

                state == "failed" -> {
                    message = p.getString("last_error", "本次操作未完成，请重试。") ?: "本次操作未完成，请重试。"
                    tone = StatusTone.Error
                }

                state == "identifying" || state == "preparing" -> {
                    message = "本次操作尚未完成；若已离开准备网页，请关闭网页后重试。"
                    tone = StatusTone.Warning
                }

                else -> {
                    message = "当前目标：$gameName。请按下方步骤准备 iOS 登录。"
                    tone = StatusTone.Neutral
                }
            }
        }

        uiState = uiState.copy(
            detectedGameId = detectedGid,
            identityRemainingSec = remainingSec,
            submittedCurrent = submittedCurrent,
            unconfirmedCurrent = unconfirmedCurrent,
            transportMode = mode,
            safAuthorized = safAuthorized,
            statusMessage = message,
            statusTone = tone
        )
    }

    private fun remainingIdentitySeconds(packageName: String): Int {
        val p = MpayBridgeSession.prefs(this)
        if (p.getString("uid", null) == null) return 0
        if (p.getString("captured_package", null) != packageName) return 0
        val elapsed = System.currentTimeMillis() - p.getLong("captured_at", 0L)
        if (elapsed < 0L || elapsed >= BridgeProtocol.IDENTITY_TTL_MS) return 0
        return ((BridgeProtocol.IDENTITY_TTL_MS - elapsed + 999L) / 1000L).toInt()
    }
}
