package me.naptila.neteaseios.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {
    val IosBridgeLogo: ImageVector by lazy {
        strokeIcon("IosBridgeLogo") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(15f, 8.8f)
                lineTo(15f, 6.2f)
                quadTo(15f, 3.5f, 12.3f, 3.5f)
                lineTo(7.2f, 3.5f)
                quadTo(4.5f, 3.5f, 4.5f, 6.2f)
                lineTo(4.5f, 17.8f)
                quadTo(4.5f, 20.5f, 7.2f, 20.5f)
                lineTo(12.3f, 20.5f)
                quadTo(15f, 20.5f, 15f, 17.8f)
                lineTo(15f, 15.2f)
                moveTo(8.5f, 6.2f)
                lineTo(11f, 6.2f)
                moveTo(19.5f, 12f)
                lineTo(9f, 12f)
                moveTo(12f, 9f)
                lineTo(8.8f, 12f)
                lineTo(12f, 15f)
            }
        }
    }

    val Gamepad: ImageVector by lazy {
        strokeIcon("Gamepad") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6f, 7f)
                lineTo(18f, 7f)
                quadTo(21f, 7f, 21.5f, 11f)
                lineTo(22f, 15.5f)
                quadTo(22f, 18.5f, 19.5f, 18.5f)
                quadTo(18f, 18.5f, 16.5f, 16.5f)
                lineTo(15.5f, 15f)
                lineTo(8.5f, 15f)
                lineTo(7.5f, 16.5f)
                quadTo(6f, 18.5f, 4.5f, 18.5f)
                quadTo(2f, 18.5f, 2f, 15.5f)
                lineTo(2.5f, 11f)
                quadTo(3f, 7f, 6f, 7f)
                close()
                moveTo(7.5f, 11f)
                lineTo(10.5f, 11f)
                moveTo(9f, 9.5f)
                lineTo(9f, 12.5f)
                moveTo(15.5f, 10.2f)
                lineTo(15.51f, 10.2f)
                moveTo(17.5f, 12f)
                lineTo(17.51f, 12f)
            }
        }
    }

    val ChevronDown: ImageVector by lazy {
        strokeIcon("ChevronDown") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.1f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6f, 9.5f)
                lineTo(12f, 15.5f)
                lineTo(18f, 9.5f)
            }
        }
    }

    val Search: ImageVector by lazy {
        strokeIcon("Search") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(11f, 4f)
                arcTo(7f, 7f, 0f, false, true, 11f, 18f)
                arcTo(7f, 7f, 0f, false, true, 11f, 4f)
                close()
                moveTo(16.2f, 16.2f)
                lineTo(20f, 20f)
            }
        }
    }

    val Refresh: ImageVector by lazy {
        strokeIcon("Refresh") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(20f, 11f)
                arcTo(8f, 8f, 0f, false, false, 6.3f, 6.3f)
                lineTo(4f, 8.5f)
                moveTo(4f, 4.5f)
                lineTo(4f, 8.5f)
                lineTo(8f, 8.5f)
                moveTo(4f, 13f)
                arcTo(8f, 8f, 0f, false, false, 17.7f, 17.7f)
                lineTo(20f, 15.5f)
                moveTo(20f, 19.5f)
                lineTo(20f, 15.5f)
                lineTo(16f, 15.5f)
            }
        }
    }

    val OpenExternal: ImageVector by lazy {
        strokeIcon("OpenExternal") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(14f, 4f)
                lineTo(20f, 4f)
                lineTo(20f, 10f)
                moveTo(20f, 4f)
                lineTo(11f, 13f)
                moveTo(18f, 13.5f)
                lineTo(18f, 18f)
                quadTo(18f, 20f, 16f, 20f)
                lineTo(6f, 20f)
                quadTo(4f, 20f, 4f, 18f)
                lineTo(4f, 8f)
                quadTo(4f, 6f, 6f, 6f)
                lineTo(10.5f, 6f)
            }
        }
    }

    val ShieldCheck: ImageVector by lazy {
        strokeIcon("ShieldCheck") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 3f)
                lineTo(19f, 6f)
                lineTo(19f, 11.5f)
                quadTo(19f, 17.5f, 12f, 21f)
                quadTo(5f, 17.5f, 5f, 11.5f)
                lineTo(5f, 6f)
                close()
                moveTo(9f, 12f)
                lineTo(11.2f, 14.2f)
                lineTo(15.2f, 10f)
            }
        }
    }

    val Check: ImageVector by lazy {
        strokeIcon("Check") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(5.5f, 12.5f)
                lineTo(10f, 17f)
                lineTo(18.5f, 7.5f)
            }
        }
    }

    val Info: ImageVector by lazy {
        strokeIcon("Info") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 3f)
                arcTo(9f, 9f, 0f, false, true, 12f, 21f)
                arcTo(9f, 9f, 0f, false, true, 12f, 3f)
                close()
                moveTo(12f, 11.5f)
                lineTo(12f, 16.5f)
                moveTo(12f, 8f)
                lineTo(12.01f, 8f)
            }
        }
    }

    val Alert: ImageVector by lazy {
        strokeIcon("Alert") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 4f)
                lineTo(21f, 19.5f)
                lineTo(3f, 19.5f)
                close()
                moveTo(12f, 10f)
                lineTo(12f, 14f)
                moveTo(12f, 17f)
                lineTo(12.01f, 17f)
            }
        }
    }

    val UserScan: ImageVector by lazy {
        strokeIcon("UserScan") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 8f)
                lineTo(4f, 5f)
                lineTo(7f, 5f)
                moveTo(17f, 5f)
                lineTo(20f, 5f)
                lineTo(20f, 8f)
                moveTo(20f, 16f)
                lineTo(20f, 19f)
                lineTo(17f, 19f)
                moveTo(7f, 19f)
                lineTo(4f, 19f)
                lineTo(4f, 16f)
                moveTo(12f, 7.2f)
                arcTo(2.6f, 2.6f, 0f, false, true, 12f, 12.4f)
                arcTo(2.6f, 2.6f, 0f, false, true, 12f, 7.2f)
                close()
                moveTo(7.8f, 16.5f)
                quadTo(12f, 13.5f, 16.2f, 16.5f)
            }
        }
    }

    val SwitchAccount: ImageVector by lazy {
        strokeIcon("SwitchAccount") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(17f, 4f)
                lineTo(20f, 7f)
                lineTo(17f, 10f)
                moveTo(4f, 7f)
                lineTo(20f, 7f)
                moveTo(7f, 20f)
                lineTo(4f, 17f)
                lineTo(7f, 14f)
                moveTo(20f, 17f)
                lineTo(4f, 17f)
            }
        }
    }

    val Folder: ImageVector by lazy {
        strokeIcon("Folder") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3.5f, 7f)
                quadTo(3.5f, 5f, 5.5f, 5f)
                lineTo(9.3f, 5f)
                lineTo(11.3f, 7.3f)
                lineTo(18.5f, 7.3f)
                quadTo(20.5f, 7.3f, 20.5f, 9.3f)
                lineTo(20.5f, 17f)
                quadTo(20.5f, 19f, 18.5f, 19f)
                lineTo(5.5f, 19f)
                quadTo(3.5f, 19f, 3.5f, 17f)
                close()
            }
        }
    }

    val GitHub: ImageVector by lazy {
        strokeIcon("GitHub") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f)
                arcTo(10f, 10f, 0f, false, false, 8.84f, 21.49f)
                quadTo(9.34f, 21.58f, 9.34f, 20.88f)
                lineTo(9.34f, 19.0f)
                curveTo(6.6f, 19.6f, 6.0f, 17.7f, 6.0f, 17.7f)
                curveTo(5.55f, 16.55f, 4.9f, 16.25f, 4.9f, 16.25f)
                curveTo(4.0f, 15.65f, 4.97f, 15.66f, 4.97f, 15.66f)
                curveTo(5.97f, 15.73f, 6.5f, 16.68f, 6.5f, 16.68f)
                curveTo(7.4f, 18.2f, 8.85f, 17.76f, 9.42f, 17.5f)
                curveTo(9.5f, 16.85f, 9.77f, 16.4f, 10.06f, 16.15f)
                curveTo(7.84f, 15.9f, 5.5f, 15.04f, 5.5f, 11.2f)
                curveTo(5.5f, 10.1f, 5.9f, 9.2f, 6.53f, 8.5f)
                curveTo(6.43f, 8.25f, 6.08f, 7.22f, 6.63f, 5.85f)
                curveTo(6.63f, 5.85f, 7.47f, 5.58f, 9.38f, 6.88f)
                curveTo(10.2f, 6.66f, 11.1f, 6.55f, 12f, 6.55f)
                curveTo(12.9f, 6.55f, 13.8f, 6.66f, 14.62f, 6.88f)
                curveTo(16.53f, 5.58f, 17.37f, 5.85f, 17.37f, 5.85f)
                curveTo(17.92f, 7.22f, 17.57f, 8.25f, 17.47f, 8.5f)
                curveTo(18.1f, 9.2f, 18.5f, 10.1f, 18.5f, 11.2f)
                curveTo(18.5f, 15.05f, 16.15f, 15.9f, 13.92f, 16.15f)
                curveTo(14.28f, 16.46f, 14.6f, 17.08f, 14.6f, 18.02f)
                lineTo(14.6f, 20.88f)
                quadTo(14.6f, 21.58f, 15.16f, 21.49f)
                arcTo(10f, 10f, 0f, false, false, 12f, 2f)
                close()
            }
        }
    }

    val Bilibili: ImageVector by lazy {
        strokeIcon("Bilibili") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.95f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Antennae
                moveTo(7.8f, 3.2f)
                lineTo(10.6f, 6.2f)
                moveTo(16.2f, 3.2f)
                lineTo(13.4f, 6.2f)
                // Rounded TV body
                moveTo(6.5f, 6.2f)
                lineTo(17.5f, 6.2f)
                quadTo(20.5f, 6.2f, 20.5f, 9.2f)
                lineTo(20.5f, 16.2f)
                quadTo(20.5f, 19.2f, 17.5f, 19.2f)
                lineTo(6.5f, 19.2f)
                quadTo(3.5f, 19.2f, 3.5f, 16.2f)
                lineTo(3.5f, 9.2f)
                quadTo(3.5f, 6.2f, 6.5f, 6.2f)
                close()
                // Eyes
                moveTo(9.2f, 10.8f)
                lineTo(8.6f, 14.2f)
                moveTo(14.8f, 10.8f)
                lineTo(15.4f, 14.2f)
            }
        }
    }

    private inline fun strokeIcon(
        name: String,
        block: ImageVector.Builder.() -> Unit
    ): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply(block).build()
}
