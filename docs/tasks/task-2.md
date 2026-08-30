# Task 2 — Program list

**Goal:** a scrollable, day-tabbed program of all sessions from the bundled JSON.

**Start from:** `checkpoint-1` · **Finished result:** `checkpoint-2`

> If you want to keep your work from task 1, commit or stash it before checking out checkpoint-1

What you're aiming for — day tabs over the sessions, grouped under sticky
time-slot headers:

<img src="media/task-2-result.png" alt="The program list: day tabs, sticky time-slot headers, session cards" width="320">

## Theory recap

See *Block 1*: `LazyColumn` composes only visible items (it's Compose's
RecyclerView), `stickyHeader` pins group headers, stable `key`s let Compose track
items, and `PrimaryTabRow` gives you Material tabs. State that must survive
recomposition lives in `remember { mutableStateOf(...) }` for now, right in the
screen.

## What you build

Four small composables, then wire them into `App()`:

| File | What it is |
| :--- | :--- |
| `ui/components/TimeSlotHeader.kt` | The "10:20 – 11:20" heading over each group of sessions |
| `ui/components/SessionList.kt` | `LazyColumn` of your `SessionCard`s with a sticky `TimeSlotHeader` per slot |
| `ui/components/DayTabRow.kt` | `PrimaryTabRow` with one tab per conference day |
| `ui/program/ProgramScreen.kt` | The screen: `Scaffold` with a `TopAppBar`, tabs on top, the selected day's list below |

Already there for you: `App.kt` loads the bundled `program.json` from Task 1; 
`List<Session>.toConferenceDays()` (in `model/Schedule.kt`) does all
the grouping; `dayLabel()` / `timeRangeLabel()` (in `ui/components/Formatting.kt`)
format the labels; `EmptyState` covers the no-sessions case.

## Steps

1. **`TimeSlotHeader(start, end)`** — a `Surface` (background colour) containing
   a `Text` with `timeRangeLabel(start, end)`, `titleMedium`, a little vertical
   padding.
2. **`SessionList(slots, favoriteIds, onSessionClick, onToggleFavorite)`** — a
   `LazyColumn`; for each `TimeSlot`: a `stickyHeader` with your
   `TimeSlotHeader`, then `items(slot.sessions, key = { it.id })` rendering
   `SessionCard`s.
3. **`DayTabRow(days, selected, onSelect)`** — `PrimaryTabRow` with a `Tab` per
   day, labelled with `dayLabel()`.
4. **`ProgramScreen(sessions)`** — group with `sessions.toConferenceDays()`,
   keep the selected day in `remember { mutableStateOf(days.firstOrNull()?.date) }`
   (favorites too, as a `Set<String>`), and lay out: `Scaffold` + `TopAppBar`
   titled "JavaZone 2026", `DayTabRow` on top, then the selected day's slots in
   your `SessionList` — or `EmptyState` if there are none.
5. In **`App.kt`**: replace the Task 1 card column with `ProgramScreen(loaded)`.

   > **Note:** with the real bundled data every day has sessions, so you won't
   > actually *see* the `EmptyState`. Wire it anyway: once Task 4
   > adds search and filters, "zero sessions on this day" becomes a real state

## Hints

<details>
<summary>Hint 1 — nudge</summary>

Build bottom-up: header first, then the list, then tabs, then the screen that
combines them — each piece is previewable/runnable on its own. The data shapes
you're consuming (all provided): `toConferenceDays(): List<ConferenceDay>`;
each `ConferenceDay` has `.date` and `.slots: List<TimeSlot>`; each `TimeSlot`
has `.start`, `.end`, `.sessions`.

The whole screen is just: tabs on top, then the selected day's `LazyColumn`
below — `days.firstOrNull { it.date == selectedDay }?.slots.orEmpty()` picks
the current slots. Favorites stay a local
`remember { mutableStateOf(setOf<String>()) }` — the real state layer is Task 4.
</details>

<details>
<summary>Hint 2 — API / types</summary>

The signatures, as in `checkpoint-2`:

```kotlin
@Composable fun TimeSlotHeader(start: LocalDateTime?, end: LocalDateTime?)

@Composable fun SessionList(
    slots: List<TimeSlot>,
    favoriteIds: Set<String>,
    onSessionClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
)

@Composable fun DayTabRow(days: List<LocalDate>, selected: LocalDate?, onSelect: (LocalDate) -> Unit)

@Composable fun ProgramScreen(sessions: List<Session>)
```

- Sticky headers need the foundation opt-in: annotate `SessionList` with
  `@OptIn(ExperimentalFoundationApi::class)`.
- `PrimaryTabRow(selectedTabIndex = index) { days.forEach { Tab(selected = …,
  onClick = …, text = { Text(it.dayLabel()) }) } }`.
- `Scaffold(topBar = { TopAppBar(title = { Text("JavaZone 2026") }) }) { padding -> … }`
  — remember to apply `Modifier.padding(padding)` to the content column, and
  `@OptIn(ExperimentalMaterial3Api::class)` for `TopAppBar`.
</details>

<details>
<summary>Hint 3 — full code (copy-paste solves the task)</summary>

Everything you need to type, in build order.

**`ui/components/TimeSlotHeader.kt`** (new file)

```kotlin
/** Sticky header for one time slot; marked as a heading so screen readers can jump slot-by-slot. */
@Composable
fun TimeSlotHeader(start: LocalDateTime?, end: LocalDateTime?) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = timeRangeLabel(start, end),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .semantics { heading() },
        )
    }
}
```

**`ui/components/SessionList.kt`** (new file — note: `items` is
`androidx.compose.foundation.lazy.items`, the overload that takes a list)

```kotlin
/** The day's sessions, grouped under sticky time-slot headers. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionList(
    slots: List<TimeSlot>,
    favoriteIds: Set<String>,
    onSessionClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
    ) {
        slots.forEach { slot ->
            stickyHeader(key = "header-${slot.start}") {
                TimeSlotHeader(slot.start, slot.end)
            }
            items(slot.sessions, key = { it.id }) { session ->
                SessionCard(
                    session = session,
                    isFavorite = session.id in favoriteIds,
                    onClick = { onSessionClick(session.id) },
                    onToggleFavorite = { onToggleFavorite(session.id) },
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }
    }
}
```

**`ui/components/DayTabRow.kt`** (new file)

```kotlin
/** One tab per conference day ("Tue 1 Sep" …). */
@Composable
fun DayTabRow(days: List<LocalDate>, selected: LocalDate?, onSelect: (LocalDate) -> Unit) {
    if (days.isEmpty()) return
    val selectedIndex = days.indexOf(selected).coerceAtLeast(0)
    PrimaryTabRow(selectedTabIndex = selectedIndex) {
        days.forEach { day ->
            Tab(
                selected = day == selected,
                onClick = { onSelect(day) },
                text = { Text(day.dayLabel(), style = MaterialTheme.typography.titleSmall, maxLines = 1) },
            )
        }
    }
}
```

**`ui/program/ProgramScreen.kt`** (new file, in a new `ui/program/` package)

```kotlin
/**
 * Task 2 program screen: day tabs over a sticky-header session list.
 *
 * State (selected day, favorites) lives right here in `remember` for now. 
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramScreen(sessions: List<Session>) {
    val days = remember(sessions) { sessions.toConferenceDays() }
    var selectedDay by remember(days) { mutableStateOf(days.firstOrNull()?.date) }
    var favoriteIds by remember { mutableStateOf(emptySet<String>()) }

    Scaffold(topBar = { TopAppBar(title = { Text("JavaZone 2026") }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            DayTabRow(days.map { it.date }, selectedDay) { selectedDay = it }

            val slots = remember(days, selectedDay) {
                days.firstOrNull { it.date == selectedDay }?.slots.orEmpty()
            }
            if (slots.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.DateRange,
                    title = "No sessions",
                    body = "There are no sessions on this day.",
                )
            } else {
                SessionList(
                    slots = slots,
                    favoriteIds = favoriteIds,
                    onSessionClick = {},
                    onToggleFavorite = { id ->
                        favoriteIds = if (id in favoriteIds) favoriteIds - id else favoriteIds + id
                    },
                )
            }
        }
    }
}
```

**`App.kt`** (edit the existing file)

Keep the loading logic and the `if (loaded == null)` spinner branch as they
are. In the **`else` branch**, delete everything — the `favoriteIds` state and
the whole scrolling `Column` of cards, and put this in its place:

```kotlin
} else {
    ProgramScreen(loaded)
}
```

so the body reads: `if (loaded == null) { …spinner… } else { ProgramScreen(loaded) }`.

> **Why the old column must go:** it has `.verticalScroll(...)` on it, and a
> `LazyColumn` scrolls itself — nesting it inside an already-scrolling parent
> wont work.
</details>

## Done when…

- [ ] All sessions render, grouped under sticky time-slot headers.
- [ ] Day tabs switch which day is shown.
- [ ] Toggling a star updates the card (local state is fine for now).

## Expected result

A full, scrollable program with day tabs and pinned time headers. `checkpoint-2`
keeps the selected day in a plain `remember`
