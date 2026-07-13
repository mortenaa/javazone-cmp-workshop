package no.javazone.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Favorite toggle. The contentDescription includes the session title so a list
 * of cards doesn't announce twenty identical "favorite" buttons (DESIGN.md §6.4).
 */
@Composable
fun FavoriteIconButton(sessionTitle: String, isFavorite: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle) {
        Icon(
            // Filled vs hollow: the state must differ by shape, not tint alone (§6.4).
            imageVector = if (isFavorite) Icons.Filled.Star else StarOutline,
            contentDescription = if (isFavorite) {
                "Remove '$sessionTitle' from my schedule"
            } else {
                "Add '$sessionTitle' to my schedule"
            },
            tint = if (isFavorite) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}
