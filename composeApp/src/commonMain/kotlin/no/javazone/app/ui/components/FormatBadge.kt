package no.javazone.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.javazone.app.model.Format

/** Colored session-format tag. The text always names the format — color is never the only signal. */
@Composable
fun FormatBadge(format: Format, lengthMinutes: Int? = null) {
    val colors = MaterialTheme.colorScheme
    val (container, name) = when (format) {
        Format.PRESENTATION -> colors.primaryContainer to "Presentation"
        Format.LIGHTNING_TALK -> colors.tertiaryContainer to "Lightning"
        Format.WORKSHOP -> colors.secondaryContainer to "Workshop"
    }
    val content = when (format) {
        Format.PRESENTATION -> colors.onPrimaryContainer
        Format.LIGHTNING_TALK -> colors.onTertiaryContainer
        Format.WORKSHOP -> colors.onSecondaryContainer
    }
    Surface(color = container, contentColor = content, shape = MaterialTheme.shapes.small) {
        Text(
            text = listOfNotNull(name, lengthLabel(lengthMinutes)).joinToString(" · "),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

/** 45/60 stay in minutes, 120/240 read better as hours. */
private fun lengthLabel(minutes: Int?): String? = when {
    minutes == null -> null
    minutes >= 120 && minutes % 60 == 0 -> "${minutes / 60} h"
    else -> "$minutes min"
}
