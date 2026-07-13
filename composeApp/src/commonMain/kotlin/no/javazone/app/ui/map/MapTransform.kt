package no.javazone.app.ui.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

/**
 * Zoom/pan state for the venue map. All math is relative to the viewport
 * center, which is also the image's transform origin.
 */
class MapTransform {
    var scale by mutableStateOf(1f)
        private set
    var offset by mutableStateOf(Offset.Zero)
        private set

    var contentSize: Size = Size.Zero

    /** Zooms towards [pivot] (a point relative to the viewport center). */
    fun zoomBy(factor: Float, pivot: Offset = Offset.Zero) {
        val newScale = (scale * factor).coerceIn(1f, 5f)
        offset = pivot - (pivot - offset) * (newScale / scale)
        scale = newScale
        clamp()
    }

    fun panBy(delta: Offset) {
        offset += delta
        clamp()
    }

    /** Double-tap / double-click: toggle between 1x and 2.5x. */
    fun toggleZoom(pivot: Offset) {
        if (scale > 1.5f) reset() else zoomBy(2.5f / scale, pivot)
    }

    fun reset() {
        scale = 1f
        offset = Offset.Zero
    }

    /** The image may never leave the viewport entirely. */
    private fun clamp() {
        val maxX = contentSize.width * scale / 2f
        val maxY = contentSize.height * scale / 2f
        offset = Offset(offset.x.coerceIn(-maxX, maxX), offset.y.coerceIn(-maxY, maxY))
    }
}
