package no.javazone.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.javazone.app.model.Session
import no.javazone.app.ui.components.FormatBadge
import no.javazone.app.ui.components.StarOutline
import no.javazone.app.ui.components.dayLabel
import no.javazone.app.ui.components.timeRangeLabel

/**
 * The full session view. One composable, two hosts: the compact detail route
 * and the expanded right-hand pane (DESIGN.md §2.3).
 */
@Composable
fun SessionDetailContent(
    session: Session,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(session.title, style = MaterialTheme.typography.headlineSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            FormatBadge(session.format, session.lengthMinutes)
            LanguageTag(session.language)
        }
        session.startTime?.let { start ->
            Text(
                text = "${start.date.dayLabel()} · ${timeRangeLabel(start, session.endTime)}",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        session.room?.let { Text(it, style = MaterialTheme.typography.titleMedium) }

        FilledTonalButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else StarOutline,
                contentDescription = null,
            )
            Spacer(Modifier.width(8.dp))
            Text(if (isFavorite) "Remove from my schedule" else "Add to my schedule")
        }

        SectionHeader("Abstract")
        // \n\n means paragraphs — never collapse or truncate in the detail view (DESIGN.md §6.2).
        session.abstract.split("\n\n").forEach { paragraph ->
            Text(paragraph.trim(), style = MaterialTheme.typography.bodyMedium)
        }

        session.intendedAudience?.takeIf { it.isNotBlank() }?.let {
            SectionHeader("Intended audience")
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }

        if (session.keywords.isNotEmpty()) {
            SectionHeader("Keywords")
            Text(
                text = session.keywords.joinToString(" · "),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (session.speakers.isNotEmpty()) {
            SectionHeader("Speakers")
            session.speakers.forEach { SpeakerRow(it) }
        }

        session.workshopPrerequisites?.takeIf { it.isNotBlank() }?.let {
            SectionHeader("Workshop prerequisites")
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun LanguageTag(language: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = language.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
