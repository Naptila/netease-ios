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
    val safAuthorized: Boolean = false
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
    onIdentifyAccount: () -> Unit,
    onOpenSwitchWindow: () -> Unit,
    onSelectTransportMode: (SeedTransportMode) -> Unit,
    onAuthorizeSafDirectory: () -> Unit,
    onPrepareIosLogin: () -> Unit
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
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HeroHeaderCard(
                    state = state,
                    dark = dark,
                    onShowPicker = { showAppPicker = true },
                    onOpenGame = onOpenGame
                )

                StatusFeedbackBanner(
                    busy = state.busy,
                    message = state.statusMessage,
                    tone = state.statusTone,
                    dark = dark
                )

                StepOneIdentifyCard(
                    gameName = state.gameDisplayName,
                    hasGame = state.currentGame != null,
                    identityFresh = state.identityFresh,
                    remainingSec = state.identityRemainingSec,
                    busy = state.busy,
                    dark = dark,
                    onIdentify = {
                        if (state.currentGame == null) showAppPicker = true else onIdentifyAccount()
                    }
                )

                StepTwoSwitchWindowCard(
                    gameName = state.gameDisplayName,
                    highlighted = state.identityFresh && !state.submittedCurrent && !state.unconfirmedCurrent,
                    busy = state.busy,
                    onOpenSwitchWindow = {
                        if (state.currentGame == null) showAppPicker = true else onOpenSwitchWindow()
                    }
                )

                StepThreePrepareIosCard(
                    state = state,
                    onSelectTransportMode = onSelectTransportMode,
                    onAuthorizeSafDirectory = {
                        if (state.currentGame == null) showAppPicker = true else onAuthorizeSafDirectory()
                    },
                    onPrepareIosLogin = {
                        if (state.currentGame == null) showAppPicker = true else onPrepareIosLogin()
                    }
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
private fun HeroHeaderCard(
    state: BridgeUiState,
    dark: Boolean,
    onShowPicker: () -> Unit,
    onOpenGame: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val gradientColors = if (dark) {
        listOf(Color(0xFF182949), Color(0xFF131F36))
    } else {
        listOf(Color(0xFFFFFFFF), Color(0xFFF5F8FF))
    }
    val selected = state.currentGame

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, scheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = if (dark) 0.dp else 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(15.dp),
                    color = scheme.tertiaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = AppIcons.IosBridgeLogo,
                            contentDescription = null,
                            tint = scheme.tertiary,
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

            if (selected != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusPill(
                        text = if (selected.installed) "应用已安装" else "应用已卸载",
                        tone = if (selected.installed) StatusTone.Success else StatusTone.Error,
                        dark = dark,
                        modifier = Modifier.weight(1f)
                    )

                    val identityLabel = if (state.identityFresh) {
                        val mins = state.identityRemainingSec / 60
                        val secs = state.identityRemainingSec % 60
                        String.format(Locale.ROOT, "账号已识别 · %02d:%02d", mins, secs)
                    } else {
                        "尚未识别账号"
                    }
                    StatusPill(
                        text = identityLabel,
                        tone = if (state.identityFresh) StatusTone.Success else StatusTone.Neutral,
                        dark = dark,
                        modifier = Modifier.weight(1f)
                    )

                    FilledTonalButton(
                        onClick = onOpenGame,
                        enabled = !state.busy && selected.installed,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.OpenExternal,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "打开",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
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
private fun StepOneIdentifyCard(
    gameName: String,
    hasGame: Boolean,
    identityFresh: Boolean,
    remainingSec: Int,
    busy: Boolean,
    dark: Boolean,
    onIdentify: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val activeHighlight = hasGame && !identityFresh

    StepCardContainer(
        stepNumber = 1,
        completed = identityFresh,
        highlighted = activeHighlight,
        title = "识别游戏当前账号",
        subtitle = if (identityFresh) {
            val mins = remainingSec / 60
            val secs = remainingSec % 60
            String.format(Locale.ROOT, "账号标识有效期内（剩余 %02d:%02d）", mins, secs)
        } else {
            "先在「$gameName」正常登录要切到 iOS 的网易账号"
        },
        dark = dark
    ) {
        Text(
            text = "首次使用或更换账号时，请先在「$gameName」正常登录该网易账号，然后点击下方按钮读取账号标识与游戏代号。",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )

        if (!identityFresh) {
            Button(
                onClick = onIdentify,
                enabled = !busy,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 13.dp, horizontal = 18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = AppIcons.UserScan,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (hasGame) "识别当前账号" else "先选择目标游戏",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        } else {
            FilledTonalButton(
                onClick = onIdentify,
                enabled = !busy,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = AppIcons.UserScan,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "重新识别当前账号",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun StepTwoSwitchWindowCard(
    gameName: String,
    highlighted: Boolean,
    busy: Boolean,
    onOpenSwitchWindow: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()

    StepCardContainer(
        stepNumber = 2,
        completed = false,
        highlighted = highlighted,
        title = "在游戏内打开「切换账号」",
        subtitle = "停留在网易登录小窗后切回助手",
        dark = dark
    ) {
        Text(
            text = "识别账号后，进入「$gameName」点击用户中心或设置中的「切换账号」，停留在网易登录窗口界面，再返回本助手准备 iOS 登录。",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )

        OutlinedButton(
            onClick = onOpenSwitchWindow,
            enabled = !busy,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, if (highlighted) scheme.primary else scheme.outline),
            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = AppIcons.SwitchAccount,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "去游戏点「切换账号」",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun StepThreePrepareIosCard(
    state: BridgeUiState,
    onSelectTransportMode: (SeedTransportMode) -> Unit,
    onAuthorizeSafDirectory: () -> Unit,
    onPrepareIosLogin: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()

    StepCardContainer(
        stepNumber = 3,
        completed = state.submittedCurrent,
        highlighted = state.identityFresh,
        title = "注入 iOS 登录准备",
        subtitle = "已停留在游戏登录窗口后执行",
        dark = dark
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "准备文件写入方式",
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onSurfaceVariant
            )

            TransportModeOptionItem(
                title = "标准缓存方式（默认）",
                description = "适用于光·遇等开放缓存接口的游戏，免目录授权",
                selected = state.transportMode == SeedTransportMode.STANDARD,
                enabled = !state.busy,
                onClick = { onSelectTransportMode(SeedTransportMode.STANDARD) }
            )

            TransportModeOptionItem(
                title = "共享目录方式（SAF · 阴阳师适用）",
                description = "经系统授权写入 Android/media 临时文件，适配无缓存接口的游戏",
                selected = state.transportMode == SeedTransportMode.SAF_MEDIA,
                enabled = !state.busy,
                onClick = { onSelectTransportMode(SeedTransportMode.SAF_MEDIA) }
            )

            if (state.transportMode == SeedTransportMode.SAF_MEDIA) {
                val authToneFg = if (state.safAuthorized) {
                    if (dark) StatusSuccessDark else StatusSuccessLight
                } else {
                    scheme.onSurfaceVariant
                }
                Surface(
                    shape = RoundedCornerShape(13.dp),
                    color = scheme.surfaceVariant.copy(alpha = if (dark) 0.5f else 0.65f),
                    border = BorderStroke(1.dp, scheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Folder,
                            contentDescription = null,
                            tint = authToneFg,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (state.safAuthorized) {
                                "已授权 Android/media 共享目录"
                            } else {
                                "待授权内部存储 Android/media 目录"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = authToneFg,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = onAuthorizeSafDirectory,
                            enabled = !state.busy,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (state.safAuthorized) "更换目录" else "授权目录",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onPrepareIosLogin,
            enabled = !state.busy,
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.identityFresh) scheme.primary else scheme.secondary
            ),
            contentPadding = PaddingValues(vertical = 15.dp, horizontal = 20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "已停在登录窗口，准备 iOS 登录",
                style = MaterialTheme.typography.labelLarge
            )
        }

        val warnBg = if (dark) StatusWarningContainerDark else StatusWarningContainerLight
        val warnFg = if (dark) StatusWarningDark else StatusWarningLight

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = warnBg,
            border = BorderStroke(1.2.dp, warnFg.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(13.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = AppIcons.Alert,
                    contentDescription = null,
                    tint = warnFg,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(19.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "重要：返回游戏后请先点右上角「×」关闭旧窗口！",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = warnFg
                    )
                    Text(
                        text = "切勿直接点击旧列表里的原账号，否则因旧会话已失效会导致一直转圈卡死！必须先点切换账号列表右上角的「×」关掉弹窗，重新打开以刷新账号列表，再重新登录即可进入 iOS 角色。",
                        style = MaterialTheme.typography.bodySmall,
                        color = warnFg.copy(alpha = 0.92f)
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = scheme.surfaceVariant.copy(alpha = if (dark) 0.5f else 0.65f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "• 以后切号时如果原本的 iOS 号不小心进成了安卓号，不用重开本助手，直接在游戏「切换账号」里把这个号再点选登录一次就会变回 iOS 号。",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
                Text(
                    text = "• 如果想主动切回安卓角色，在游戏「切换账号」列表里删掉该账号记录后重新登录即可。",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StepCardContainer(
    stepNumber: Int,
    completed: Boolean,
    highlighted: Boolean,
    title: String,
    subtitle: String,
    dark: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val borderColor by animateColorAsState(
        targetValue = when {
            highlighted -> scheme.primary.copy(alpha = 0.55f)
            completed -> (if (dark) StatusSuccessDark else StatusSuccessLight).copy(alpha = 0.4f)
            else -> scheme.outlineVariant
        },
        animationSpec = tween(220),
        label = "stepBorder"
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(if (highlighted) 1.4.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (dark) 0.dp else 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val badgeBg = when {
                    completed -> if (dark) StatusSuccessContainerDark else StatusSuccessContainerLight
                    highlighted -> scheme.primary
                    else -> scheme.surfaceVariant
                }
                val badgeFg = when {
                    completed -> if (dark) StatusSuccessDark else StatusSuccessLight
                    highlighted -> scheme.onPrimary
                    else -> scheme.onSurfaceVariant
                }

                Surface(
                    shape = CircleShape,
                    color = badgeBg,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (completed) {
                            Icon(
                                imageVector = AppIcons.Check,
                                contentDescription = null,
                                tint = badgeFg,
                                modifier = Modifier.size(17.dp)
                            )
                        } else {
                            Text(
                                text = stepNumber.toString(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = badgeFg
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            content()
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
private fun TransportModeOptionItem(
    title: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) scheme.primaryContainer.copy(alpha = 0.6f)
        else scheme.surfaceVariant.copy(alpha = 0.38f),
        border = BorderStroke(
            width = if (selected) 1.4.dp else 1.dp,
            color = if (selected) scheme.primary else scheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
            SelectionIndicator(selected = selected)
        }
    }
}

@Preview(showBackground = true)
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
            onIdentifyAccount = {},
            onOpenSwitchWindow = {},
            onSelectTransportMode = {},
            onAuthorizeSafDirectory = {},
            onPrepareIosLogin = {}
        )
    }
}
