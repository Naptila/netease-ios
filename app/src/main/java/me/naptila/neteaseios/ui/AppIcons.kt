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
