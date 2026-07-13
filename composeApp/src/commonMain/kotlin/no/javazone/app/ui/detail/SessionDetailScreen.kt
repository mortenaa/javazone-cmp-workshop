package no.javazone.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.javazone.app.model.Session
import no.javazone.app.ui.components.FavoriteIconButton
import no.javazone.app.ui.program.ProgramIntent
import no.javazone.app.ui.program.ProgramUiState

/** Compact host: the detail as a pushed, focused reading view (no bottom bar). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    session: Session,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = { FavoriteIconButton(session.title, isFavorite, onToggleFavorite) },
            )
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            SessionDetailContent(
                session = session,
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite,
                modifier = Modifier.widthIn(max = 840.dp).align(Alignment.TopCenter),
            )
        }
    }
}

/** Expanded host: the same content in the right-hand pane, driven by selection state. */
@Composable
fun SessionDetailPane(state: ProgramUiState, onIntent: (ProgramIntent) -> Unit) {
    val session = state.session(state.selectedSessionId)
    if (session == null) {
        // Placeholder per DESIGN.md §2.3: centered icon + bodyLarge text.
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp),
            )
            Text(
                text = "Select a session",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    } else {
        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            SessionDetailContent(
                session = session,
                isFavorite = session.id in state.favoriteIds,
                onToggleFavorite = { onIntent(ProgramIntent.ToggleFavorite(session.id)) },
                modifier = Modifier.fillMaxWidth().widthIn(max = 840.dp),
            )
        }
    }
}
