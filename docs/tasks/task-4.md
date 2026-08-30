# Task 4 — `ProgramViewModel` + navigation

**Goal:** move state out of the composables into a ViewModel, and tap a card to
open its detail screen.

**Start from:** `checkpoint-3` · **Finished result:** `checkpoint-4`

## Theory recap

See *Block 2 — State & architecture*: state hoisting, unidirectional data flow,
MVVM vs MVI. We use **MVVM with MVI-flavoured intents** — one immutable
`UiState`, a sealed `ProgramIntent` (the closed list of "everything the user can
do"), and a `ViewModel` exposing a `StateFlow`, collected in the UI with
`collectAsState()`. Navigation is `navigation-compose`: string routes, one
`NavHost`, `session/{sessionId}` as a path argument.

## Steps

1. Fill out the `TODO()`s in the provided `ProgramUiState`
   (`ui/program/ProgramUiState.kt`): `dayTabs`, `hasActiveFilters`,
   `showOfflineBanner`, `session(id)`, and the **filtering logic** in
   `daySlots(day)` / `favoriteSlots(day)` — pure functions of the state, not
   composables. (The loading/offline fields exist already but only matter from
   Task 5.)
2. Read the provided `ProgramIntent` — the closed `sealed interface` of
   "everything the user can do": `SelectDay`, `ToggleFormat`, `ToggleLanguage`,
   `Search`, `ClearFilters`, `ToggleFavorite`, `SelectSession`, `Retry`,
   `DismissOfflineBanner`. You'll handle every one of them in the ViewModel.
3. `ProgramViewModel : ViewModel()` — private `MutableStateFlow`, public
   `StateFlow`, and `fun onIntent(intent)` with an exhaustive `when`. Load the
   bundled program in `init`. Favorites are an **in-memory `Set`** for now.
4. In `App()`: `val viewModel = viewModel { ProgramViewModel() }`, `val state by
   viewModel.state.collectAsState()`, and make screens take `(state, onIntent)`.
5. Add the `session/{sessionId}` route to a `NavHost` and navigate to it from
   `SessionCard.onClick`. **Read the argument** — it's the source of truth.

## Provided in your starting point

`checkpoint-3` already carries the ready-made UI for this task, the detail screen, 
the **My schedule** tab, the **Info**
screen, the filters/search chip row.
It also ships `ProgramIntent` complete (it's pure declarations) and a
`ProgramUiState` **skeleton**: all the fields are there so the screens
compile, and the logic is `TODO()`s — filling those out *is* step 1. The
screens all take `(state, onIntent)`-shaped inputs, so they drop straight
into your `NavHost` routes. The **Map** screen stays stubbed until
`checkpoint-6`.

## Hints

<details>
<summary>Hint 1 — nudge</summary>

The `SessionCard` from Task 1 is already stateless — it takes `isFavorite` and
calls `onToggleFavorite`. That's the whole point of hoisting: nothing in the
composables changes shape, you just move *where the state lives*. Every screen
gets one `onIntent: (ProgramIntent) -> Unit` instead of a pile of callbacks.
</details>

<details>
<summary>Hint 2 — API / types</summary>

The signatures (`ProgramUiState` and `ProgramIntent`
are already in `ui/program/` — the ViewModel is the one new file):

```kotlin
data class ProgramUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val isOffline: Boolean = false,            // stays false until Task 5
    val offlineBannerDismissed: Boolean = false,
    val sessions: List<Session> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val selectedDay: LocalDate? = null,
    val activeFormats: Set<Format> = emptySet(),
    val activeLanguages: Set<String> = emptySet(),
    val searchQuery: String = "",
    val selectedSessionId: String? = null,
) {
    val dayTabs: List<LocalDate>
    val hasActiveFilters: Boolean
    val showOfflineBanner: Boolean
    fun session(id: String?): Session?
    fun daySlots(day: LocalDate?): List<TimeSlot>       // filters applied (Program tab)
    fun favoriteSlots(day: LocalDate?): List<TimeSlot>  // favorites only (My schedule tab)
}

sealed interface ProgramIntent {
    data class SelectDay(val day: LocalDate) : ProgramIntent
    data class ToggleFormat(val format: Format) : ProgramIntent
    data class ToggleLanguage(val language: String) : ProgramIntent
    data class Search(val query: String) : ProgramIntent
    data object ClearFilters : ProgramIntent
    data class ToggleFavorite(val sessionId: String) : ProgramIntent
    data class SelectSession(val sessionId: String?) : ProgramIntent
    data object DismissOfflineBanner : ProgramIntent
    data object Retry : ProgramIntent
}

class ProgramViewModel : ViewModel() {
    val state: StateFlow<ProgramUiState>
    fun onIntent(intent: ProgramIntent)
}
```

And the screen changes shape — it becomes a pure function of state:

```kotlin
@Composable fun ProgramScreen(
    state: ProgramUiState,
    onIntent: (ProgramIntent) -> Unit,
    expanded: Boolean,
    onOpenSession: (String) -> Unit,
)
```

- `_state.update { it.copy(...) }` is an atomic compare-and-set on the private
  `MutableStateFlow`; the public side is `state.asStateFlow()`.
- Keep filtering on the state as pure functions —
  `fun daySlots(day) = sessions.filter { it.matchesFilters() }.slotsFor(day)` —
  never in composables.
- Navigate with `navController.navigate("session/$sessionId")`; read the
  argument back with `entry.arguments?.read { getStringOrNull("sessionId") }`.
</details>

<details>
<summary>Hint 3 — full code (copy-paste solves the task)</summary>

Everything you need to type, in build order. (`FilterChipsRow`,
`SessionDetailScreen`/`SessionDetailPane` and `ScheduleScreen` are provided in
your starting point — this hint covers what the task *builds*.)

**`ui/program/ProgramIntent.kt`** (already provided — shown for reference)

```kotlin
/** Everything the user can do on the program screens, as data (MVI-flavored MVVM). */
sealed interface ProgramIntent {
    data class SelectDay(val day: LocalDate) : ProgramIntent
    data class ToggleFormat(val format: Format) : ProgramIntent
    data class ToggleLanguage(val language: String) : ProgramIntent
    data class Search(val query: String) : ProgramIntent
    data object ClearFilters : ProgramIntent
    data class ToggleFavorite(val sessionId: String) : ProgramIntent
    data class SelectSession(val sessionId: String?) : ProgramIntent
    data object DismissOfflineBanner : ProgramIntent
    data object Retry : ProgramIntent
}
```

**`ui/program/ProgramUiState.kt`** (the provided skeleton with its `TODO()`s
filled out)

```kotlin
/** Single immutable snapshot of everything the program screens render. */
data class ProgramUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val isOffline: Boolean = false,
    val offlineBannerDismissed: Boolean = false,
    val sessions: List<Session> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val selectedDay: LocalDate? = null,
    val activeFormats: Set<Format> = emptySet(),
    val activeLanguages: Set<String> = emptySet(),
    val searchQuery: String = "",
    val selectedSessionId: String? = null,
) {
    /** Tabs always show all days, even when filters empty one of them. Lazy: computed once per state instance. */
    val dayTabs: List<LocalDate> by lazy { sessions.toConferenceDays().map { it.date } }

    val hasActiveFilters: Boolean
        get() = activeFormats.isNotEmpty() || activeLanguages.isNotEmpty() || searchQuery.isNotBlank()

    val showOfflineBanner: Boolean get() = isOffline && !offlineBannerDismissed

    fun session(id: String?): Session? = sessions.firstOrNull { it.id == id }

    /** The selected day's slots with format/language filters applied (Program tab). */
    fun daySlots(day: LocalDate?): List<TimeSlot> =
        sessions.filter { it.matchesFilters() }.slotsFor(day)

    /** The selected day's favorited sessions, unfiltered (My Schedule tab). */
    fun favoriteSlots(day: LocalDate?): List<TimeSlot> =
        sessions.filter { it.id in favoriteIds }.slotsFor(day)

    private fun Session.matchesFilters(): Boolean =
        (activeFormats.isEmpty() || format in activeFormats) &&
            (activeLanguages.isEmpty() || language in activeLanguages) &&
            matchesSearch()

    /** Case-insensitive substring match on title and speaker names. */
    private fun Session.matchesSearch(): Boolean =
        searchQuery.isBlank() ||
            title.contains(searchQuery, ignoreCase = true) ||
            speakers.any { it.name.contains(searchQuery, ignoreCase = true) }

    private fun List<Session>.slotsFor(day: LocalDate?): List<TimeSlot> =
        toConferenceDays().firstOrNull { it.date == day }?.slots.orEmpty()
}
```

**`ui/program/ProgramViewModel.kt`** (new file — note it absorbs
`loadBundledSessions()`, which you can then delete from `App.kt`)

```kotlin
/**
 * Owns the program data and favorites; the UI only sends [ProgramIntent]s.
 *
 * Task 4 version: the program is loaded straight from the bundled resource and
 * favorites are an in-memory [Set] that vanishes on restart. Task 5 swaps the
 * load for a Ktor fetch behind a repository, and Task 6 gives favorites a real
 * persistent home — neither of which changes this class's public surface.
 */
class ProgramViewModel : ViewModel() {

    private val _state = MutableStateFlow(ProgramUiState())
    val state: StateFlow<ProgramUiState> = _state.asStateFlow()

    init {
        loadProgram()
    }

    fun onIntent(intent: ProgramIntent) {
        when (intent) {
            is ProgramIntent.SelectDay -> _state.update { it.copy(selectedDay = intent.day) }
            is ProgramIntent.ToggleFormat ->
                _state.update { it.copy(activeFormats = it.activeFormats.toggle(intent.format)) }
            is ProgramIntent.ToggleLanguage ->
                _state.update { it.copy(activeLanguages = it.activeLanguages.toggle(intent.language)) }
            is ProgramIntent.Search -> _state.update { it.copy(searchQuery = intent.query) }
            ProgramIntent.ClearFilters ->
                _state.update {
                    it.copy(activeFormats = emptySet(), activeLanguages = emptySet(), searchQuery = "")
                }
            is ProgramIntent.ToggleFavorite ->
                _state.update { it.copy(favoriteIds = it.favoriteIds.toggle(intent.sessionId)) }
            is ProgramIntent.SelectSession -> _state.update { it.copy(selectedSessionId = intent.sessionId) }
            ProgramIntent.DismissOfflineBanner -> _state.update { it.copy(offlineBannerDismissed = true) }
            ProgramIntent.Retry -> loadProgram()
        }
    }

    private fun loadProgram() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = it.sessions.isEmpty(), loadFailed = false) }
            val sessions = loadBundledSessions()
            _state.update {
                it.copy(
                    isLoading = false,
                    sessions = sessions,
                    selectedDay = it.selectedDay ?: defaultDay(sessions),
                )
            }
        }
    }

    /** Today during the conference, otherwise the Wednesday. */
    private fun defaultDay(sessions: List<Session>): LocalDate? {
        val dates = sessions.toConferenceDays().map { it.date }
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return dates.firstOrNull { it == today }
            ?: dates.firstOrNull { it.dayOfWeek == DayOfWeek.WEDNESDAY }
            ?: dates.firstOrNull()
    }
}

/** Loads and maps the bundled program.json. Task 5 replaces this with a real network fetch. */
private suspend fun loadBundledSessions(): List<Session> {
    val bytes = Res.readBytes("files/program.json")
    val dto = ProgramJson.decodeFromString(ProgramDto.serializer(), bytes.decodeToString())
    return dto.sessions.map { it.toSession() }
}

private fun <T> Set<T>.toggle(value: T): Set<T> = if (value in this) this - value else this + value
```

**`ui/program/ProgramScreen.kt`** (replace the whole file's contents — the
screen is now a pure function of `(state, onIntent)`; selection/favorites no
longer live here)

```kotlin
/** Full schedule: day tabs, filters, search and the session list; list-detail on expanded windows. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramScreen(
    state: ProgramUiState,
    onIntent: (ProgramIntent) -> Unit,
    expanded: Boolean,
    onOpenSession: (String) -> Unit,
) {
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
```

**`App.kt`** (replace the whole file's contents below the imports — the
`loadBundledSessions()` helper moves into the ViewModel, so delete it here)

```kotlin
/** Root composable: theme, one shared ViewModel (manual wiring, no DI), adaptive scaffold, NavHost. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    JavaZoneTheme {
        val viewModel: ProgramViewModel = viewModel { ProgramViewModel() }
        val state by viewModel.state.collectAsState()
        val navController = rememberNavController()
        val windowWidth = currentWindowWidth()
        val expanded = windowWidth == WindowWidth.Expanded
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        // Standard M3 top-level navigation: tabs keep their state, back returns to Program.
        fun navigateTopLevel(route: String) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }

        // On expanded windows the detail is the second pane — state, not navigation.
        fun openSession(sessionId: String) {
            viewModel.onIntent(ProgramIntent.SelectSession(sessionId))
            if (!expanded) navController.navigate("session/$sessionId")
        }

        // The detail pane only exists on the Program and My Schedule tabs.
        val onListDetailTab = currentRoute == TopDestination.Program.route ||
            currentRoute == TopDestination.Schedule.route

        // Back/Esc on expanded windows clears the pane selection first —
        // but only where the pane is actually visible.
        BackHandler(enabled = expanded && onListDetailTab && state.selectedSessionId != null) {
            viewModel.onIntent(ProgramIntent.SelectSession(null))
        }

        // Window resized across the 840 dp breakpoint: keep the open detail visible.
        var wasExpanded by remember { mutableStateOf(expanded) }
        LaunchedEffect(expanded) {
            val route = navController.currentBackStackEntry?.destination?.route
            val onDetailRoute = route?.startsWith("session") == true
            val onPaneTab = route == TopDestination.Program.route ||
                route == TopDestination.Schedule.route
            when {
                expanded && !wasExpanded && onDetailRoute ->
                    navController.navigateUp() // the pane takes over via selectedSessionId
                !expanded && wasExpanded && onPaneTab && state.selectedSessionId != null ->
                    navController.navigate("session/${state.selectedSessionId}")
            }
            wasExpanded = expanded
        }

        AdaptiveScaffold(windowWidth, currentRoute, onNavigate = ::navigateTopLevel) {
            NavHost(navController, startDestination = TopDestination.Program.route) {
                composable(TopDestination.Program.route) {
                    ProgramScreen(state, viewModel::onIntent, expanded, onOpenSession = ::openSession)
                }
                composable(TopDestination.Schedule.route) {
                    ScheduleScreen(
                        state = state,
                        onIntent = viewModel::onIntent,
                        expanded = expanded,
                        onOpenSession = ::openSession,
                        onBrowseProgram = { navigateTopLevel(TopDestination.Program.route) },
                    )
                }
                composable(TopDestination.Info.route) { InfoScreen() }
                composable(TopDestination.Map.route) {
                    // The venue map is a Task 6 stretch — placeholder until then.
                    EmptyState(
                        icon = Icons.Outlined.Place,
                        title = "Venue map",
                        body = "Coming in Task 6.",
                    )
                }
                composable("session/{sessionId}") { entry ->
                    // The route argument is the source of truth: it survives Android
                    // process death, where the ViewModel's selection state does not.
                    val sessionId = entry.arguments?.read { getStringOrNull("sessionId") }
                    val session = state.session(sessionId)
                    when {
                        session != null -> SessionDetailScreen(
                            session = session,
                            isFavorite = session.id in state.favoriteIds,
                            onBack = { navController.navigateUp() },
                            onToggleFavorite = { viewModel.onIntent(ProgramIntent.ToggleFavorite(session.id)) },
                        )
                        state.isLoading -> LoadingState()
                        else -> EmptyState(
                            icon = Icons.Outlined.Warning,
                            title = "Session not found",
                            body = "This session is not in the current program.",
                            actionLabel = "Back to program",
                            onAction = { navigateTopLevel(TopDestination.Program.route) },
                        )
                    }
                }
            }
        }
    }
}
```

Two imports the IDE may need help with: `BackHandler` is
`androidx.compose.ui.backhandler.BackHandler` (hence the
`@OptIn(ExperimentalComposeUiApi::class)`), and `read { getStringOrNull(...) }`
comes from `androidx.savedstate.read`.
</details>

## Done when…

- [ ] All screen state lives in `ProgramUiState` / the ViewModel; screens are pure
      functions of `(state, onIntent)`.
- [ ] Search actually filters; the star toggles (in-memory).
- [ ] Tapping a card opens the detail screen, and back works.

## Expected result

The same app as before, but now every interaction flows through one ViewModel and
one sealed intent type. Favorites still vanish on restart — that's the cliffhanger
Block 3 resolves. Compare with `checkpoint-4`.
