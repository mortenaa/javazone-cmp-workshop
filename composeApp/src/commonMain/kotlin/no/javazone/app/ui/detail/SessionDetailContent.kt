package no.javazone.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.javazone.app.model.Session
import no.javazone.app.ui.components.FormatBadge
import no.javazone.app.ui.components.StarOutline
import no.javazone.app.ui.components.timeRangeLabel

/** The full session view: one composable used both as the pushed detail screen and the expanded-pane content. */
@Composable
fun SessionDetailContent(
    session: Session,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(session.title, style = MaterialTheme.typography.headlineSmall)

        if (session.speakers.isNotEmpty()) {
            Text(
                session.speakers.joinToString { it.name },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        FormatBadge(session.format, session.lengthMinutes)

        Text(
            listOfNotNull(
                timeRangeLabel(session.startTime, session.endTime),
                session.room,
                session.language.uppercase(),
            ).joinToString(" · "),
            style = MaterialTheme.typography.titleMedium,
        )

        FilledTonalButton(onClick = onToggleFavorite) {
            Icon(if (isFavorite) Icons.Filled.Star else StarOutline, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (isFavorite) "Remove from my schedule" else "Add to my schedule")
        }

        session.abstract.split("\n\n").forEach { paragraph ->
            Text(paragraph.trim(), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
