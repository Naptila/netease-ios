package me.naptila.neteaseios.bridge

/**
 * Target NetEase MPay game specification.
 *
 * [gameId] is the MPay/UniSDK game code (e.g. "ma75"). When null, the bridge automatically
 * reads it from gameContextInfo and binds it to the selected package.
 */
data class GameSpec(
    val packageName: String,
    val displayName: String = packageName,
    val gameId: String? = null,
    val gmActivity: String = BridgeProtocol.DEFAULT_GM_ACTIVITY,
    val cacheAuthority: String = "${packageName}${BridgeProtocol.DEFAULT_CACHE_AUTHORITY_SUFFIX}"
) {
    val cacheProviderUriPrefix: String
        get() = "content://$cacheAuthority/${BridgeProtocol.DEFAULT_CACHE_PATH}/"
}

/** Common MPay / GMBridge5 protocol helpers. */
object BridgeProtocol {
    const val DEFAULT_GM_ACTIVITY = "com.netease.unisdk.gmbridge5.activity.GMWebviewActivity"
    const val DEFAULT_CACHE_AUTHORITY_SUFFIX = ".widget_file_provider"
    const val DEFAULT_CACHE_PATH = "widget_file_cache"
    const val DEFAULT_LOGIN_CHANNEL = "netease"
    const val DEFAULT_LOGIN_TYPE = 1
    const val IOS_CLIENT_TYPE = 2
    const val ANDROID_CLIENT_TYPE = 1

    private val GAME_ID_REGEX = Regex("^[a-zA-Z0-9_.-]{1,64}$")
    private val PACKAGE_NAME_REGEX = Regex("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z0-9_]+)+$")

    fun isValidPackageName(packageName: String): Boolean =
        packageName.length <= 160 && PACKAGE_NAME_REGEX.matches(packageName)

    fun uidFromQualifiedName(full: String?): String {
        if (full.isNullOrBlank() || full.length > 512) {
            throw IllegalArgumentException("identity-missing")
        }
        for (ch in full) {
            if (ch.isISOControl() || ch.isWhitespace()) {
                throw IllegalArgumentException("identity-format")
            }
        }
        val at = full.indexOf('@')
        return if (at > 0) full.substring(0, at) else full
    }

    fun supportedSource(
        game: String?,
        expectedGameId: String? = null
    ): Boolean {
        if (game.isNullOrBlank() || !GAME_ID_REGEX.matches(game)) return false
        if (!expectedGameId.isNullOrBlank() && game != expectedGameId) return false
        return true
    }
}
