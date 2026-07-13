# Task 2 — Program list

**Goal:** a scrollable, day-tabbed program of all sessions from the bundled JSON.

**Time box:** ~30 min · **Compare against:** `checkpoint-2`

## Theory recap

See *Block 1*: `LazyColumn` composes only visible items (it's Compose's
RecyclerView), `stickyHeader` pins group headers, stable `key`s let Compose track
items, and `PrimaryTabRow` gives you Material tabs. State that must survive
recomposition lives in `remember { mutableStateOf(...) }` — for now, right in the
screen.

## Steps

1. Load sessions from the bundled `program.json`. The DTO + mapping is provided
   (`data/ProgramDto.kt`); `App.kt` already shows how to read the resource.
2. Group them with `List<Session>.toConferenceDays()` (**provided** in
   `model/Schedule.kt`) — it returns `ConferenceDay`s, each with `TimeSlot`s.
3. Build a `SessionList` (`LazyColumn`) of your `SessionCard`s, with a
   `stickyHeader` per time slot (`TimeSlotHeader`) and `items(..., key = { it.id })`.
4. Add `DayTabRow` (`PrimaryTabRow`) with one tab per conference day; keep the
   selected day in `remember { mutableStateOf(days.first()) }`.
5. When a day has no sessions, show the provided `EmptyState`.

## Hints

<details>
<summary>Hint 1 — nudge</summary>

Keep favorites as a local `remember { mutableStateOf(setOf<String>()) }` for now —
the real state layer is Task 4. The whole screen is just: tabs on top, then the
selected day's `LazyColumn` below. Compute the current day's slots from the
grouped data.
</details>

<details>
<summary>Hint 2 — API / types</summary>

- `toConferenceDays(): List<ConferenceDay>`; each `ConferenceDay` has `.date` and
  `.slots: List<TimeSlot>`; each `TimeSlot` has `.start`, `.end`, `.sessions`.
- Sticky headers need the foundation opt-in: annotate the composable with
  `@OptIn(ExperimentalFoundationApi::class)`.
- `PrimaryTabRow(selectedTabIndex = index) { days.forEach { Tab(selected = …,
  onClick = …, text = { Text(it.dayLabel()) }) } }` — `dayLabel()` is a provided
  helper in `ui/components/Formatting.kt`.
</details>

<details>
<summary>Hint 3 — code (the list)</summary>

```kotlin
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
            stickyHeader(key = "header-${slot.start}") { TimeSlotHeader(slot.start, slot.end) }
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

Then in your screen: pick the selected `ConferenceDay`, feed its `.slots` to
`SessionList`, and put `DayTabRow` above it. See `checkpoint-2` for the full
screen + tabs.
</details>

## Done when…

- [ ] All sessions render, grouped under sticky time-slot headers.
- [ ] Day tabs switch which day is shown.
- [ ] Toggling a star updates the card (local state is fine for now).
- [ ] An empty day shows the `EmptyState`.

## Expected result

A full, scrollable program with day tabs and pinned time headers. `checkpoint-2`
keeps the selected day in a plain `remember` — feeling that "where should this
state live?" itch is exactly the setup for Block 2.
