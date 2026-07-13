# Task 4 — `ProgramViewModel` + navigation

**Goal:** move state out of the composables into a ViewModel, and tap a card to
open its detail screen.

**Time box:** ~30 min · **Compare against:** `checkpoint-4`

## Theory recap

See *Block 2 — State & architecture*: state hoisting, unidirectional data flow,
MVVM vs MVI. We use **MVVM with MVI-flavoured intents** — one immutable
`UiState`, a sealed `ProgramIntent` (the closed list of "everything the user can
do"), and a `ViewModel` exposing a `StateFlow`, collected in the UI with
`collectAsState()`. Navigation is `navigation-compose`: string routes, one
`NavHost`, `session/{sessionId}` as a path argument.

## Steps

1. `ProgramUiState` — a `data class` holding `sessions`, `favoriteIds`,
   `selectedDay`, `activeFormats`, `searchQuery`, `selectedSessionId`, plus derived
   helpers (`daySlots(day)`, `session(id)`). Put the **filtering logic here**, as
   pure functions of the state — not in composables.
2. `ProgramIntent` — a `sealed interface`: `SelectDay`, `ToggleFormat`,
   `ToggleLanguage`, `Search`, `ClearFilters`, `ToggleFavorite`, `SelectSession`,
   `Retry`, `DismissOfflineBanner`.
3. `ProgramViewModel : ViewModel()` — private `MutableStateFlow`, public
   `StateFlow`, and `fun onIntent(intent)` with an exhaustive `when`. Load the
   bundled program in `init`. Favorites are an **in-memory `Set`** for now.
4. In `App()`: `val viewModel = viewModel { ProgramViewModel() }`, `val state by
   viewModel.state.collectAsState()`, and make screens take `(state, onIntent)`.
5. Add the `session/{sessionId}` route to a `NavHost` and navigate to it from
   `SessionCard.onClick`. **Read the argument** — it's the source of truth.

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

```kotlin
class ProgramViewModel : ViewModel() {
    private val _state = MutableStateFlow(ProgramUiState())
    val state: StateFlow<ProgramUiState> = _state.asStateFlow()

    fun onIntent(intent: ProgramIntent) = when (intent) {
        is ProgramIntent.SelectDay -> _state.update { it.copy(selectedDay = intent.day) }
        is ProgramIntent.Search    -> _state.update { it.copy(searchQuery = intent.query) }
        is ProgramIntent.ToggleFavorite -> _state.update {
            it.copy(favoriteIds = it.favoriteIds.toggle(intent.sessionId))
        }
        // …
    }
}
```

`update { it.copy(...) }` is an atomic compare-and-set. Keep filtering on the
state: `fun daySlots(day) = sessions.filter { it.matchesFilters() }.slotsFor(day)`.
</details>

<details>
<summary>Hint 3 — code (reading the route argument)</summary>

```kotlin
composable("session/{sessionId}") { entry ->
    // The route argument is the source of truth: it survives Android process
    // death, where the ViewModel's selection state does not.
    val sessionId = entry.arguments?.read { getStringOrNull("sessionId") }
    val session = state.session(sessionId)
    if (session != null) {
        SessionDetailScreen(session, session.id in state.favoriteIds,
            onBack = { navController.navigateUp() },
            onToggleFavorite = { viewModel.onIntent(ProgramIntent.ToggleFavorite(session.id)) })
    } else {
        EmptyState(Icons.Outlined.Warning, "Session not found", "…")
    }
}
```

Navigate with `navController.navigate("session/$sessionId")`. Full `App.kt`,
`ProgramScreen`, `ScheduleScreen` and the detail screens are in `checkpoint-4`.
</details>

## Where the extra screens land

`checkpoint-4` also wires up the **My schedule** tab (the program list filtered to
favorites), the **filters/search UI** driven through intents, and the **Info**
screen (static content — a good "content as data, UI as a loop" warm-up). The
**Map** screen is stubbed until `checkpoint-6`.

## Done when…

- [ ] All screen state lives in `ProgramUiState` / the ViewModel; screens are pure
      functions of `(state, onIntent)`.
- [ ] Search actually filters; the star toggles (in-memory).
- [ ] Tapping a card opens the detail screen, and back works.

## Expected result

The same app as before, but now every interaction flows through one ViewModel and
one sealed intent type. Favorites still vanish on restart — that's the cliffhanger
Block 3 resolves. Compare with `checkpoint-4`.
