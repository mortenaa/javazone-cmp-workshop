# Task 3 — Adaptive layout

**Goal:** bottom navigation bar on a phone, navigation rail + two panes on a wide
screen — the *same code* adapting to the window size.

**Time box:** ~25 min · **Compare against:** `checkpoint-3`

## Theory recap

See *Block 1 — One codebase, every window size*: Material window **size classes**
bucket the width into Compact / Medium / Expanded. **Two breakpoints, two
independent decisions**: the nav container changes at **600 dp** (bar ↔ rail);
the pane layout changes at **840 dp** (single pane ↔ list-detail). Adaptive layout
is just state — on desktop it updates live as you drag the window edge.

## Steps

1. Add `currentWindowWidth()` using `currentWindowAdaptiveInfo().windowSizeClass`,
   folded into a small `enum WindowWidth { Compact, Medium, Expanded }`.
2. Add a `TopDestination` enum (route, label, filled + outlined icon) for the four
   tabs: Program, My schedule, Info, Map.
3. Build `AdaptiveScaffold`: a `Scaffold` with a `NavigationBar` when
   `Compact`, otherwise a `Row` with a `NavigationRail` on the left + the content.
4. Add `ListDetailLayout(expanded, list, detail)`: a weighted `Row` (≈0.42 / 0.58)
   when `expanded`, otherwise just `list()`.
5. Track a `selectedSessionId` and pass `selected = session.id == selectedSessionId`
   into `SessionCard` so the chosen card highlights in the two-pane view.
6. **Run on Desktop and resize** across 600 dp and 840 dp — watch it adapt.

## Hints

<details>
<summary>Hint 1 — nudge</summary>

`currentWindowAdaptiveInfo()` comes from the `material3-adaptive` library (already
a dependency). The two breakpoint constants live on `WindowSizeClass`:
`WIDTH_DP_MEDIUM_LOWER_BOUND` (600) and `WIDTH_DP_EXPANDED_LOWER_BOUND` (840).
Keep selection state (`selectedSessionId`) in a `remember { mutableStateOf(...) }`
for now — moving it into a ViewModel is Task 4.
</details>

<details>
<summary>Hint 2 — API / types</summary>

```kotlin
enum class WindowWidth { Compact, Medium, Expanded }

@Composable
fun currentWindowWidth(): WindowWidth {
    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return when {
        sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> WindowWidth.Expanded
        sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> WindowWidth.Medium
        else -> WindowWidth.Compact
    }
}
```

`NavigationBarItem` (bar) and `NavigationRailItem` (rail) take the same
`selected` / `onClick` / `icon` / `label` — drive both from
`TopDestination.entries` so they can't drift apart.
</details>

<details>
<summary>Hint 3 — code (list-detail)</summary>

```kotlin
@Composable
fun ListDetailLayout(
    expanded: Boolean,
    list: @Composable () -> Unit,
    detail: @Composable () -> Unit,
) {
    if (expanded) {
        Row(Modifier.fillMaxSize()) {
            Box(Modifier.weight(0.42f)) { list() }
            Box(Modifier.weight(0.58f)) { detail() }
        }
    } else {
        list()
    }
}
```

The punchline: on a wide screen "detail" is a **second pane** (state), on a phone
it's a **pushed destination** (navigation). Full `AdaptiveScaffold` is in
`checkpoint-3`.
</details>

## Done when…

- [ ] Narrow window → bottom `NavigationBar`; wide window → left `NavigationRail`.
- [ ] At ≥840 dp the Program screen shows two panes (list + detail).
- [ ] The selected card highlights (`selected` parameter).
- [ ] Resizing the desktop window across 600/840 dp adapts live.

## Expected result

The same build, resized, moves through all three columns of the size-class table:
bottom bar → rail → rail + two panes. `checkpoint-3` gets everyone level before
the architecture block.

> **Note (task order):** the `selectedSessionId` plumbing is deliberately a little
> awkward while state still lives in `remember` inside composables. That friction
> is the motivation for Task 4 — don't over-engineer it here.
