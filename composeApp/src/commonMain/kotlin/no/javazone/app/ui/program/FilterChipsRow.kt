package no.javazone.app.ui.program

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.javazone.app.model.Format

private val formatLabels = mapOf(
    Format.PRESENTATION to "Presentation",
    Format.LIGHTNING_TALK to "Lightning",
    Format.WORKSHOP to "Workshop",
)

private val languageLabels = mapOf("no" to "Norwegian", "en" to "English")

/** Always-visible filter chips: multi-select per group, empty selection = no filtering. */
@Composable
fun FilterChipsRow(
    activeFormats: Set<Format>,
    activeLanguages: Set<String>,
    onToggleFormat: (Format) -> Unit,
    onToggleLanguage: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(formatLabels.keys.toList()) { format ->
            FilterChip(
                selected = format in activeFormats,
                onClick = { onToggleFormat(format) },
                label = { Text(formatLabels.getValue(format)) },
            )
        }
        item { VerticalDivider(Modifier.height(32.dp)) }
        items(languageLabels.keys.toList()) { language ->
            FilterChip(
                selected = language in activeLanguages,
                onClick = { onToggleLanguage(language) },
                label = { Text(languageLabels.getValue(language)) },
            )
        }
    }
}
