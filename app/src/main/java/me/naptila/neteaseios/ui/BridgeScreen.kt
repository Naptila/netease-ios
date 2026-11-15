package me.naptila.neteaseios.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.naptila.neteaseios.bridge.GameSpec
import me.naptila.neteaseios.bridge.GameTarget
import me.naptila.neteaseios.bridge.InstalledGame
import me.naptila.neteaseios.bridge.SeedTransportMode
import me.naptila.neteaseios.ui.theme.NeteaseiosTheme
import me.naptila.neteaseios.ui.theme.StatusErrorContainerDark
import me.naptila.neteaseios.ui.theme.StatusErrorContainerLight
import me.naptila.neteaseios.ui.theme.StatusErrorDark
import me.naptila.neteaseios.ui.theme.StatusErrorLight
import me.naptila.neteaseios.ui.theme.StatusSuccessContainerDark
import me.naptila.neteaseios.ui.theme.StatusSuccessContainerLight
import me.naptila.neteaseios.ui.theme.StatusSuccessDark
import me.naptila.neteaseios.ui.theme.StatusSuccessLight
import me.naptila.neteaseios.ui.theme.StatusWarningContainerDark
import me.naptila.neteaseios.ui.theme.StatusWarningContainerLight
import me.naptila.neteaseios.ui.theme.StatusWarningDark
import me.naptila.neteaseios.ui.theme.StatusWarningLight
import java.util.Locale

enum class StatusTone {
    Neutral,
    Success,
    Warning,
    Error
}

enum class OperationMode { SWITCH_IOS, RESTORE_ANDROID }

data class BridgeUiState(
    val currentGame: InstalledGame? = null,
    val installedApps: List<InstalledGame> = emptyList(),
    val loadingApps: Boolean = false,
    val detectedGameId: String? = null,
    val identityRemainingSec: Int = 0,
    val busy: Boolean = false,
    val statusMessage: String = "请先在上方选择目标网易游戏。",
    val statusTone: StatusTone = StatusTone.Neutral,
    val submittedCurrent: Boolean = false,
    val unconfirmedCurrent: Boolean = false,
    val transportMode: SeedTransportMode = SeedTransportMode.STANDARD,
    val safAuthorized: Boolean = false,
    val operationMode: OperationMode = OperationMode.SWITCH_IOS,
    val lastClientType: Int? = null,
    val capturedUid: String? = null
) {
    val identityFresh: Boolean get() = identityRemainingSec > 0
    val effectiveGameId: String? get() = currentGame?.spec?.gameId ?: detectedGameId
    val gameDisplayName: String get() = currentGame?.spec?.displayName ?: "目标游戏"
}

@Composable
fun BridgeScreen(
    state: BridgeUiState,
    onOpenGame: () -> Unit,
    onSelectInstalledGame: (InstalledGame) -> Unit,
    onRefreshApps: () -> Unit,
    onSelectTransportMode: (SeedTransportMode) -> Unit,
    onAuthorizeSafDirectory: () -> Unit,
    onPrepareIosLogin: () -> Unit,
    onSelectOperationMode: (OperationMode) -> Unit
) {
    val dark = isSystemInDarkTheme()
    val scheme = MaterialTheme.colorScheme
    var showAppPicker by remember { mutableStateOf(false) }

    if (showAppPicker) {
        InstalledAppPickerDialog(
            currentGame = state.currentGame,
            installedApps = state.installedApps,
            loading = state.loadingApps,
            onDismiss = { showAppPicker = false },
            onRefresh = onRefreshApps,
            onSelectApp = { app ->
                onSelectInstalledGame(app)
                showAppPicker = false
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = scheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AmbientBackdrop(dark = dark)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GameStatusCard(
                    state = state,
                    dark = dark,
                    onShowPicker = { showAppPicker = true },
                    onOpenGame = onOpenGame
                )

                ModesCard(
                    mode = state.operationMode,
                    onSelectMode = onSelectOperationMode
                )

                TransportCard(
                    state = state,
                    onSelectTransportMode = onSelectTransportMode,
                    onAuthorizeSafDirectory = {
                        if (state.currentGame == null) showAppPicker = true else onAuthorizeSafDirectory()
                    }
                )

                MainActionButton(
                    state = state,
                    onAction = {
                        if (state.currentGame == null) showAppPicker = true else onPrepareIosLogin()
                    }
                )

                StatusFeedbackBanner(
                    busy = state.busy,
                    message = state.statusMessage,
                    tone = state.statusTone,
                    dark = dark
                )

                Text(
                    text = "提示：返回游戏后先点账号列表右上角「×」刷新，再用同一账号登录。首次使用需输一次账号密码建立记录。",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
@Composable
private fun AmbientBackdrop(dark: Boolean) {
    val topGlow = if (dark) Color(0xFF1A2F5A) else Color(0xFFDCE8FF)
    val goldGlow = if (dark) Color(0x33F6C36B) else Color(0x38F59E0B)
    val baseColor = MaterialTheme.colorScheme.background

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(topGlow.copy(alpha = if (dark) 0.55f else 0.7f), baseColor),
                startY = 0f,
                endY = size.height * 0.42f
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(goldGlow, Color.Transparent),
                center = Offset(size.width * 0.88f, size.height * 0.08f),
                radius = size.minDimension * 0.45f
            ),
            radius = size.minDimension * 0.45f,
            center = Offset(size.width * 0.88f, size.height * 0.08f)
        )
    }
}

@Composable
private fun GameStatusCard(
    state: BridgeUiState,
    dark: Boolean,
    onShowPicker: () -> Unit,
    onOpenGame: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val selected = state.currentGame

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, scheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = if (dark) 0.dp else 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        if (dark) listOf(Color(0xFF3A2030), Color(0xFF2A1722))
                        else listOf(Color(0xFFFFFFFF), Color(0xFFFFF0F6))
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(15.dp),
                    color = scheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = AppIcons.IosBridgeLogo,
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "网易游戏 iOS 登录",
                        style = MaterialTheme.typography.headlineSmall,
                        color = scheme.onSurface
                    )
                    Text(
                        text = "在安卓端登录 iOS 角色",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = scheme.surfaceVariant.copy(alpha = if (dark) 0.55f else 0.7f),
                border = BorderStroke(
                    width = if (selected == null) 1.4.dp else 1.dp,
                    color = if (selected == null) scheme.primary.copy(alpha = 0.6f) else scheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = !state.busy, onClick = onShowPicker)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selected != null) {
                        AsyncGameAppAvatar(
                            packageName = selected.spec.packageName,
                            cachedIcon = selected.icon,
                            size = 42
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = selected.spec.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = scheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                val gameCode = state.effectiveGameId
                                if (!gameCode.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = scheme.primaryContainer
                                    ) {
                                        Text(
                                            text = gameCode,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = scheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            val sub = buildString {
                                append(selected.spec.packageName)
                                if (selected.versionName.isNotBlank()) {
                                    append(" · v")
                                    append(selected.versionName)
                                }
                            }
                            Text(
                                text = sub,
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(11.dp),
                            color = scheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = AppIcons.Gamepad,
                                    contentDescription = null,
                                    tint = scheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "点击选择目标游戏",
                                style = MaterialTheme.typography.titleSmall,
                                color = scheme.primary
                            )
                            Text(
                                text = "从本机已安装的应用列表中选择",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (selected == null) "选择" else "更换",
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.primary
                        )
                        Icon(
                            imageVector = AppIcons.ChevronDown,
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "当前角色：" + when (state.lastClientType) {
                        2 -> "苹果"
                        1 -> "安卓"
                        else -> "未识别（一键注入后更新）"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ModesCard(
    mode: OperationMode,
    onSelectMode: (OperationMode) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = scheme.surface.copy(alpha = 0.9f),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "操作模式",
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onSurfaceVariant
            )
            ModeOptionRow(
                selected = mode == OperationMode.SWITCH_IOS,
                icon = AppIcons.IosBridgeLogo,
                title = "切到苹果角色（iOS）",
                subtitle = "把当前网易账号切到 iOS 端角色",
                onClick = { onSelectMode(OperationMode.SWITCH_IOS) }
            )
            ModeOptionRow(
                selected = mode == OperationMode.RESTORE_ANDROID,
                icon = AppIcons.SwitchAccount,
                title = "还原为安卓角色",
                subtitle = "把已切换的账号还原回安卓端",
                onClick = { onSelectMode(OperationMode.RESTORE_ANDROID) }
            )
        }
    }
}

@Composable
private fun ModeOptionRow(
    selected: Boolean,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val accent = if (selected) scheme.primary else scheme.onSurfaceVariant.copy(alpha = 0.5f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) scheme.primaryContainer.copy(alpha = 0.55f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(2.dp, accent, CircleShape)
                .padding(3.dp)
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(scheme.primary)
                )
            }
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) scheme.primary else scheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TransportCard(
    state: BridgeUiState,
    onSelectTransportMode: (SeedTransportMode) -> Unit,
    onAuthorizeSafDirectory: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = scheme.surface.copy(alpha = 0.9f),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "写入方式",
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onSurfaceVariant
            )
            ModeOptionRow(
                selected = state.transportMode == SeedTransportMode.STANDARD,
                icon = AppIcons.Folder,
                title = "标准缓存",
                subtitle = "免授权，适用于多数游戏",
                onClick = { onSelectTransportMode(SeedTransportMode.STANDARD) }
            )
            ModeOptionRow(
                selected = state.transportMode == SeedTransportMode.SAF_MEDIA,
                icon = AppIcons.ShieldCheck,
                title = "共享目录（SAF）",
                subtitle = "适用于阴阳师等未开放缓存接口的游戏",
                onClick = { onSelectTransportMode(SeedTransportMode.SAF_MEDIA) }
            )
            if (state.transportMode == SeedTransportMode.SAF_MEDIA) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (state.safAuthorized) "目录已授权" else "需授权一次 Android/media 目录",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.safAuthorized) scheme.primary else scheme.onSurfaceVariant
                    )
                    if (!state.safAuthorized) {
                        TextButton(onClick = onAuthorizeSafDirectory, enabled = !state.busy) {
                            Text("授权目录")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainActionButton(
    state: BridgeUiState,
    onAction: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val isIos = state.operationMode == OperationMode.SWITCH_IOS
    val label = when {
        state.currentGame == null -> "请先选择目标游戏"
        isIos -> "一键登录 iOS 角色"
        else -> "还原为安卓角色"
    }
    Button(
        onClick = onAction,
        enabled = !state.busy && state.currentGame != null,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (state.busy) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = scheme.onPrimary
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AsyncGameAppAvatar(
    packageName: String,
    cachedIcon: ImageBitmap?,
    size: Int
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    var icon by remember(packageName) { mutableStateOf(cachedIcon) }
    LaunchedEffect(packageName, cachedIcon) {
        icon = cachedIcon ?: withContext(Dispatchers.IO) {
            GameTarget.loadAppIcon(context, packageName)
        }
    }

    val bmp = icon
    if (bmp != null) {
        Image(
            bitmap = bmp,
            contentDescription = null,
            modifier = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape(11.dp))
        )
    } else {
        Surface(
            shape = RoundedCornerShape(11.dp),
            color = scheme.primaryContainer,
            modifier = Modifier.size(size.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = AppIcons.Gamepad,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size((size * 0.54f).dp)
                )
            }
        }
    }
}

@Composable
private fun SelectionIndicator(selected: Boolean) {
    val scheme = MaterialTheme.colorScheme
    if (selected) {
        Surface(
            shape = CircleShape,
            color = scheme.primary,
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = AppIcons.Check,
                    contentDescription = null,
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(1.5.dp, scheme.outline),
                modifier = Modifier.fillMaxSize()
            ) {}
        }
    }
}


@Composable
private fun InstalledAppPickerDialog(
    currentGame: InstalledGame?,
    installedApps: List<InstalledGame>,
    loading: Boolean,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onSelectApp: (InstalledGame) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    var query by remember { mutableStateOf("") }

    val filteredApps = remember(installedApps, query) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            installedApps
        } else {
            installedApps.filter {
                it.spec.displayName.contains(trimmed, ignoreCase = true) ||
                    it.spec.packageName.contains(trimmed, ignoreCase = true)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = scheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "选择已安装的应用",
                            style = MaterialTheme.typography.titleMedium,
                            color = scheme.onSurface
                        )
                        Text(
                            text = "请选择要登录 iOS 角色的网易游戏",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onRefresh, enabled = !loading) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = AppIcons.Refresh,
                                contentDescription = "刷新应用列表",
                                tint = scheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("搜索应用名称或包名…") },
                    leadingIcon = {
                        Icon(
                            imageVector = AppIcons.Search,
                            contentDescription = null,
                            modifier = Modifier.size(19.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (filteredApps.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = scheme.surfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (loading) "正在读取本机已安装应用…" else "没有找到匹配的应用。",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = filteredApps,
                            key = { it.spec.packageName }
                        ) { item ->
                            val selected = item.spec.packageName == currentGame?.spec?.packageName
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) scheme.primaryContainer.copy(alpha = 0.7f)
                                else scheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(
                                    width = if (selected) 1.5.dp else 1.dp,
                                    color = if (selected) scheme.primary else scheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSelectApp(item) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    AsyncGameAppAvatar(
                                        packageName = item.spec.packageName,
                                        cachedIcon = item.icon,
                                        size = 38
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.spec.displayName,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = scheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = item.spec.packageName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = scheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    SelectionIndicator(selected = selected)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("关闭")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    tone: StatusTone,
    dark: Boolean,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val (bg, fg) = when (tone) {
        StatusTone.Success -> if (dark) StatusSuccessContainerDark to StatusSuccessDark else StatusSuccessContainerLight to StatusSuccessLight
        StatusTone.Warning -> if (dark) StatusWarningContainerDark to StatusWarningDark else StatusWarningContainerLight to StatusWarningLight
        StatusTone.Error -> if (dark) StatusErrorContainerDark to StatusErrorDark else StatusErrorContainerLight to StatusErrorLight
        StatusTone.Neutral -> scheme.surfaceVariant to scheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatusFeedbackBanner(
    busy: Boolean,
    message: String,
    tone: StatusTone,
    dark: Boolean
) {
    val scheme = MaterialTheme.colorScheme
    val targetBg = when {
        busy -> scheme.primaryContainer
        tone == StatusTone.Success -> if (dark) StatusSuccessContainerDark else StatusSuccessContainerLight
        tone == StatusTone.Warning -> if (dark) StatusWarningContainerDark else StatusWarningContainerLight
        tone == StatusTone.Error -> if (dark) StatusErrorContainerDark else StatusErrorContainerLight
        else -> scheme.surfaceVariant.copy(alpha = if (dark) 0.75f else 0.85f)
    }
    val targetFg = when {
        busy -> scheme.onPrimaryContainer
        tone == StatusTone.Success -> if (dark) StatusSuccessDark else StatusSuccessLight
        tone == StatusTone.Warning -> if (dark) StatusWarningDark else StatusWarningLight
        tone == StatusTone.Error -> if (dark) StatusErrorDark else StatusErrorLight
        else -> scheme.onSurfaceVariant
    }

    val containerColor by animateColorAsState(targetBg, tween(250), label = "statusBg")
    val contentColor by animateColorAsState(targetFg, tween(250), label = "statusFg")

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.18f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (busy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.4.dp,
                    color = contentColor
                )
            } else {
                val icon = when (tone) {
                    StatusTone.Success -> AppIcons.Check
                    StatusTone.Warning, StatusTone.Error -> AppIcons.Alert
                    StatusTone.Neutral -> AppIcons.Info
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedContent(
                targetState = message,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "statusText",
                modifier = Modifier.weight(1f)
            ) { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
private fun BridgeScreenPreview() {
    NeteaseiosTheme {
        BridgeScreen(
            state = BridgeUiState(
                currentGame = InstalledGame(
                    spec = GameSpec(packageName = "com.netease.sky", displayName = "光·遇", gameId = "ma75"),
                    versionName = "0.16.8",
                    installed = true
                ),
                identityRemainingSec = 542,
                statusMessage = "账号已识别。请到游戏内点「切换账号」，再回来准备 iOS 登录。",
                statusTone = StatusTone.Success
            ),
            onOpenGame = {},
            onSelectInstalledGame = {},
            onRefreshApps = {},
            onSelectTransportMode = {},
            onAuthorizeSafDirectory = {},
            onPrepareIosLogin = {},
            onSelectOperationMode = {}
        )
    }
}
