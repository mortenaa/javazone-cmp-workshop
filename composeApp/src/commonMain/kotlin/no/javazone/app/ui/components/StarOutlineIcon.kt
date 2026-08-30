package no.javazone.app.ui.components

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A hollow star. material-icons-core has no StarBorder, and its Outlined.Star
 * glyph is solid — favorite state must differ by shape, not tint alone
 * (DESIGN.md §6.4). Path data equals Material Design's star_border icon.
 */
val StarOutline: ImageVector = materialIcon(name = "StarOutline") {
    materialPath {
        moveTo(22.0f, 9.24f)
        lineToRelative(-7.19f, -0.62f)
        lineTo(12.0f, 2.0f)
        lineTo(9.19f, 8.63f)
        lineTo(2.0f, 9.24f)
        lineToRelative(5.46f, 4.73f)
        lineTo(5.82f, 21.0f)
        lineTo(12.0f, 17.27f)
        lineTo(18.18f, 21.0f)
        lineToRelative(-1.63f, -7.03f)
        lineTo(22.0f, 9.24f)
        close()
        moveTo(12.0f, 15.4f)
        lineToRelative(-3.76f, 2.27f)
        lineToRelative(1.0f, -4.28f)
        lineToRelative(-3.32f, -2.88f)
        lineToRelative(4.38f, -0.38f)
        lineTo(12.0f, 6.1f)
        lineToRelative(1.71f, 4.04f)
        lineToRelative(4.38f, 0.38f)
        lineToRelative(-3.32f, 2.88f)
        lineToRelative(1.0f, 4.28f)
        lineTo(12.0f, 15.4f)
        close()
    }
}
