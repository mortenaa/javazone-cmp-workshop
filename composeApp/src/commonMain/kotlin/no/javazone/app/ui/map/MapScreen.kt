package no.javazone.app.ui.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import no.javazone.app.resources.Res
import no.javazone.app.resources.venue_map
import org.jetbrains.compose.resources.painterResource

/**
 * Zoomable venue map: Image + graphicsLayer transforms, in pure common code.
 * The marker overlay is the app's single sanctioned custom-drawn element.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    val transform = remember { MapTransform() }
    var selectedMarker by remember { mutableStateOf<VenueMarker?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Venue map") },
                actions = {
                    // Keyboard/mouse users always have a way back to the full view.
                    IconButton(onClick = { transform.reset() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reset zoom")
                    }
                },
            )
        },
    ) { padding ->
        BoxWithConstraints(Modifier.padding(padding).fillMaxSize().clipToBounds()) {
            val viewport = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
            val center = Offset(viewport.width / 2f, viewport.height / 2f)
            val base = fitInside(viewport, aspect = 4f / 3f)
            transform.contentSize = base

            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(center) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            transform.zoomBy(zoom, centroid - center)
                            transform.panBy(pan)
                        }
                    }
                    .pointerInput(center) {
                        detectTapGestures(
                            onDoubleTap = { tap -> transform.toggleZoom(tap - center) },
                            onTap = { selectedMarker = null },
                        )
                    }
                    // Scroll-wheel zoom (desktop/web); phones simply never send Scroll events.
                    .pointerInput(center) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.type != PointerEventType.Scroll) continue
                                val change = event.changes.firstOrNull() ?: continue
                                val factor = if (change.scrollDelta.y < 0) 1.1f else 1f / 1.1f
                                transform.zoomBy(factor, change.position - center)
                                change.consume()
                            }
                        }
                    },
            )

            Image(
                painter = painterResource(Res.drawable.venue_map),
                contentDescription = "Venue floor plan",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(with(LocalDensity.current) { DpSize(base.width.toDp(), base.height.toDp()) })
                    .graphicsLayer {
                        scaleX = transform.scale
                        scaleY = transform.scale
                        translationX = transform.offset.x
                        translationY = transform.offset.y
                    },
            )

            // Markers follow the image transform with their *position* only —
            // they stay 32 dp and readable at any zoom level.
            venueMarkers.forEach { marker ->
                MapMarker(
                    marker = marker,
                    position = Offset(
                        (marker.x - 0.5f) * base.width * transform.scale + transform.offset.x,
                        (marker.y - 0.5f) * base.height * transform.scale + transform.offset.y,
                    ),
                    onClick = { selectedMarker = marker },
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            selectedMarker?.let { marker ->
                // A plain docked Card, not a ModalBottomSheet: less code, identical everywhere.
                Card(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .widthIn(max = 480.dp)
                        .fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(marker.room, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = marker.hint,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MapMarker(marker: VenueMarker, position: Offset, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier.offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) },
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(32.dp),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(marker.label, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

private fun fitInside(viewport: Size, aspect: Float): Size =
    if (viewport.width / viewport.height > aspect) {
        Size(viewport.height * aspect, viewport.height)
    } else {
        Size(viewport.width, viewport.width / aspect)
    }
