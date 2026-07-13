package no.javazone.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import no.javazone.app.model.Speaker

/** Speaker with a "monogram avatar" — Surface + Text, no image assets (DESIGN.md §3.3). */
@Composable
fun SpeakerRow(speaker: Speaker) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(speaker.name.initials(), style = MaterialTheme.typography.titleMedium)
            }
        }
        Column(Modifier.weight(1f)) {
            Text(speaker.name, style = MaterialTheme.typography.titleSmall)
            speaker.bio?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SocialLinks(speaker)
        }
    }
}

/** LinkedIn/Bluesky chips — only for values that are actual URLs (twitter is a bare handle). */
@Composable
private fun SocialLinks(speaker: Speaker) {
    val links = listOf("LinkedIn" to speaker.linkedin, "Bluesky" to speaker.bluesky)
        .filter { (_, url) -> url?.startsWith("http") == true }
    if (links.isEmpty()) return
    val uriHandler = LocalUriHandler.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        links.forEach { (label, url) ->
            AssistChip(onClick = { uriHandler.openUri(url!!) }, label = { Text(label) })
        }
    }
}

private fun String.initials(): String =
    split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }
