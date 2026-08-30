package no.javazone.app.ui.program

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import no.javazone.app.ui.components.DayTabRow
import no.javazone.app.ui.components.EmptyState
import no.javazone.app.ui.components.ErrorState
import no.javazone.app.ui.components.ListDetailLayout
import no.javazone.app.ui.components.LoadingState
import no.javazone.app.ui.components.OfflineBanner
import no.javazone.app.ui.components.SessionList
import no.javazone.app.ui.detail.SessionDetailPane

/** Full schedule: day tabs, filters, search and the session list; list-detail on expanded windows. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramScreen(
    state: ProgramUiState,
    onIntent: (ProgramIntent) -> Unit,
    expanded: Boolean,
    onOpenSession: (String) -> Unit,
) {
    // State-saving teaching point: searchActive survives process death
    // (rememberSaveable), but the query lives in the ViewModel, which does not —
    // a restore lands on an open, empty search field. The full fix is keeping
    // the query in a SavedStateHandle; deliberately out of scope here.
    var searchActive by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (searchActive) {
                        SearchField(
                            query = state.searchQuery,
                            onQueryChange = { onIntent(ProgramIntent.Search(it)) },
                            onClose = {
                                onIntent(ProgramIntent.Search(""))
                                searchActive = false
                            },
                        )
                    } else {
                        Text("JavaZone 2026")
                    }
                },
                actions = {
                    if (!searchActive) {
                        IconButton(onClick = { searchActive = true }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                    }
                    IconButton(onClick = { onIntent(ProgramIntent.Retry) }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh program")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            DayTabRow(state.dayTabs, state.selectedDay) { onIntent(ProgramIntent.SelectDay(it)) }
            if (state.showOfflineBanner) {
                OfflineBanner { onIntent(ProgramIntent.DismissOfflineBanner) }
            }
            FilterChipsRow(
                activeFormats = state.activeFormats,
                activeLanguages = state.activeLanguages,
                onToggleFormat = { onIntent(ProgramIntent.ToggleFormat(it)) },
                onToggleLanguage = { onIntent(ProgramIntent.ToggleLanguage(it)) },
            )
            when {
                state.isLoading -> LoadingState()
                state.loadFailed -> ErrorState(onRetry = { onIntent(ProgramIntent.Retry) })
                else -> ListDetailLayout(
                    expanded = expanded,
                    list = { ProgramList(state, onIntent, expanded, onOpenSession) },
                    detail = { SessionDetailPane(state, onIntent) },
                )
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        placeholder = { Text("Search title or speaker") },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Close search")
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ProgramList(
    state: ProgramUiState,
    onIntent: (ProgramIntent) -> Unit,
    expanded: Boolean,
    onOpenSession: (String) -> Unit,
) {
    // remember: recompute the grouping when the state changes, not on every recomposition.
    val slots = remember(state) { state.daySlots(state.selectedDay) }
    if (slots.isEmpty()) {
        EmptyState(
            icon = Icons.Outlined.Search,
            title = "No sessions match",
            body = "Try removing a filter or changing your search.",
            actionLabel = if (state.hasActiveFilters) "Clear filters" else null,
            onAction = { onIntent(ProgramIntent.ClearFilters) },
        )
    } else {
        SessionList(
            slots = slots,
            favoriteIds = state.favoriteIds,
            onSessionClick = onOpenSession,
            onToggleFavorite = { onIntent(ProgramIntent.ToggleFavorite(it)) },
            selectedSessionId = if (expanded) state.selectedSessionId else null,
        )
    }
}
