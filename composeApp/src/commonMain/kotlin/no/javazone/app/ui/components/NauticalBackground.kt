package no.javazone.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * A full-screen ocean backdrop: the water fills everything from the bottom of
 * the box up to a wavy surface near the top, with bubbles drifting upward and
 * dissolving as they reach the surface. Put content on top of it.
 *
 * It is 100% Compose drawing + animation, so it renders identically on Android,
 * iOS, Desktop and Web — no platform code. This is stretch/demo material; it
 * shows the graphics layer the core tasks skip: [Canvas], [Path], [Brush],
 * [clipPath] and two ways to animate — a per-frame clock ([withFrameNanos]) for
 * the bubbles and an [rememberInfiniteTransition] for the wave.
 *
 * @param surfaceFraction where the water surface sits, as a fraction of height
 *   from the top (0.12 = the top ~12% stays clear, just enough for a top bar).
 */
@Composable
fun NauticalBackground(
    modifier: Modifier = Modifier,
    surfaceFraction: Float = 0.12f,
    bubbleCount: Int = 32, // keep modest — every bubble is drawn each frame (matters on Web)
) {
    val surface = Color(0xFF0E4A63) // sunlit shallows
    val deep = Color(0xFF04121F)    // abyss
    val lagoon = Color(0xFF57C4D1)  // blue lagoon

    // Each bubble keeps its own position and drift; we mutate these in place.
    val bubbles = remember {
        List(bubbleCount) {
            Bubble(
                x = Random.nextFloat(),
                y = surfaceFraction + Random.nextFloat() * (1f - surfaceFraction),
                radiusDp = 2f + Random.nextFloat() * 7f,
                speed = 0.03f + Random.nextFloat() * 0.09f,
                drift = 0.008f + Random.nextFloat() * 0.025f,
                phase = Random.nextFloat() * TWO_PI,
                alpha = 0.18f + Random.nextFloat() * 0.32f,
            )
        }
    }

    // A frame-clock tick. Reading it inside the Canvas is what makes it redraw,
    // and advancing by real elapsed time keeps motion frame-rate independent.
    val time = remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) {
                    val dt = (now - last) / 1_000_000_000f
                    time.value += dt
                    bubbles.forEach { bubble ->
                        bubble.y -= bubble.speed * dt
                        if (bubble.y < surfaceFraction - 0.03f) {   // broke the surface -> respawn deep
                            bubble.y = 1.03f
                            bubble.x = Random.nextFloat()
                        }
                    }
                }
                last = now
            }
        }
    }

    // The surface slides sideways forever.
    val wavePhase by rememberInfiniteTransition(label = "wave").animateFloat(
        initialValue = 0f,
        targetValue = TWO_PI,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing)),
        label = "wavePhase",
    )

    Canvas(modifier.fillMaxSize()) {
        val t = time.value // read the tick so this draw reruns every frame
        val surfaceY = size.height * surfaceFraction
        val amp = 12.dp.toPx()

        // The water: a wavy top edge, filled down to the bottom of the box.
        val water = Path().apply {
            moveTo(0f, size.height)
            lineTo(0f, surfaceY)
            val steps = 48
            for (i in 0..steps) {
                val x = size.width * i / steps
                val y = surfaceY + amp * sin(wavePhase + i / steps.toFloat() * TWO_PI * 2f)
                lineTo(x, y)
            }
            lineTo(size.width, size.height)
            close()
        }

        clipPath(water) {
            drawRect(Brush.verticalGradient(listOf(surface, deep), startY = surfaceY, endY = size.height))

            val fadeBand = size.height * 0.22f // bubbles fade over this depth below the surface
            bubbles.forEach { bubble ->
                val wobble = sin(t * 1.5f + bubble.phase) * bubble.drift
                val cx = wrap01(bubble.x + wobble) * size.width
                val cy = bubble.y * size.height
                val fade = ((cy - surfaceY) / fadeBand).coerceIn(0f, 1f) // 0 at surface, 1 deeper
                drawCircle(lagoon.copy(alpha = bubble.alpha * fade), bubble.radiusDp.dp.toPx(), Offset(cx, cy))
            }
        }
    }
}

private class Bubble(
    var x: Float,
    var y: Float,
    val radiusDp: Float,
    val speed: Float,
    val drift: Float,
    val phase: Float,
    val alpha: Float,
)

private const val TWO_PI = (2.0 * PI).toFloat()

private fun wrap01(v: Float): Float = ((v % 1f) + 1f) % 1f
