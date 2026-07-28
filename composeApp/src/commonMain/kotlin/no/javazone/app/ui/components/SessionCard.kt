package no.javazone.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import no.javazone.app.model.Format
import no.javazone.app.model.Session
import no.javazone.app.ui.theme.JavaZoneTheme

@Composable
fun SessionCard(
    session: Session,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = if (selected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    session.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                FavoriteIconButton(
                    isFavorite = isFavorite,
                    sessionTitle = session.title,
                    onClick = onToggleFavorite,
                )
            }
            if (session.speakers.isNotEmpty()) {
                Text(
                    session.speakers.joinToString { it.name },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                FormatBadge(session.format, session.lengthMinutes)
                Spacer(Modifier.weight(1f))
                Text(
                    listOfNotNull(session.room, session.language.uppercase()).joinToString(" · "),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FavoriteIconButton(
    isFavorite: Boolean,
    sessionTitle: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            if (isFavorite) Icons.Filled.Star else StarOutline,
            contentDescription = if (isFavorite) "Remove '$sessionTitle' from favorites"
            else "Add '$sessionTitle' to favorites",
            tint = if (isFavorite) MaterialTheme.colorScheme.tertiary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun FormatBadge(format: Format, lengthMinutes: Int, modifier: Modifier = Modifier) {
    val (container, content, label) = when (format) {
        Format.PRESENTATION -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Presentation",
        )
        Format.LIGHTNING_TALK -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "Lightning talk",
        )
        Format.WORKSHOP -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Workshop",
        )
    }
    Surface(color = container, contentColor = content, shape = MaterialTheme.shapes.small, modifier = modifier) {
        Text(
            "$label · $lengthMinutes min",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Preview
@Composable
private fun SessionCardPreview() {
    JavaZoneTheme(darkTheme = false) {
        Surface {
            SessionCard(sampleSession, isFavorite = true, onClick = {}, onToggleFavorite = {})
        }
    }
}

@Preview
@Composable
private fun SessionCardPreviewDark() {
    JavaZoneTheme(darkTheme = true) {
        Surface {
            SessionCard(sampleSession, isFavorite = false, onClick = {}, onToggleFavorite = {})
        }
    }
}
