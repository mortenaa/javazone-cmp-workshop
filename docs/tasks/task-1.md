# Task 1 — `SessionCard`

**Goal:** build the composable that renders one session in the list.

**Time box:** ~25 min · **Compare against:** `checkpoint-1`

## Theory recap

See *Block 1 — Building the UI with Compose*: `@Composable` functions *emit* UI,
you compose functions from `Column`/`Row`/`Box` and Material 3 components, and
`@Preview` renders them in the IDE. Every component takes a trailing
`modifier: Modifier = Modifier` by convention.

## Steps

1. Create `SessionCard.kt` in `ui/components/`.
2. Make a `Card(onClick = …)` containing a `Column`, padded 16.dp.
3. Top row: the title (`titleMedium`, `maxLines = 2`, ellipsis, `Modifier.weight(1f)`)
   and a favorite `IconButton` to its right.
4. Below: the speaker names (`bodyMedium`, `onSurfaceVariant`), then a bottom row
   with a **format badge** on the left and `room · LANGUAGE` on the right.
5. Add a `FormatBadge` sub-composable: a coloured `Surface` whose text always
   *names* the format (colour is never the only signal).
6. `@Preview` it with `sampleSession` — one **light**, one **dark**.

## Hints

<details>
<summary>Hint 1 — nudge</summary>

The provided `sampleSession` (in `ui/components/SampleData.kt`) has everything you
need: `title`, `speakers`, `format`, `lengthMinutes`, `room`, `language`. Start
by putting the `@Preview` in place and building the card *inside* it, so you get
instant visual feedback.

For the star button, there's already a `FavoriteIconButton` idea in the finished
app, but for Task 1 a plain `IconButton` with `Icons.Filled.Star` /
`StarOutline` (provided in `StarOutlineIcon.kt`) is enough.
</details>

<details>
<summary>Hint 2 — API / types</summary>

- `Card(onClick = onClick, modifier = modifier.fillMaxWidth()) { … }`
- Title `Text(session.title, style = MaterialTheme.typography.titleMedium,
  maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))`
  — `weight` only works inside a `Row`/`Column`.
- Format colours come from the theme: `MaterialTheme.colorScheme.primaryContainer`
  (presentation), `tertiaryContainer` (lightning), `secondaryContainer` (workshop),
  each with its matching `on…Container` content colour.
- The card is **stateless**: it takes `isFavorite: Boolean` and reports
  `onToggleFavorite: () -> Unit` — it doesn't own the favorite state.
</details>

<details>
<summary>Hint 3 — code</summary>

```kotlin
@Composable
fun SessionCard(
    session: Session,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    session.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (isFavorite) Icons.Filled.Star else StarOutline,
                        contentDescription = if (isFavorite) "Remove '${session.title}'"
                                             else "Add '${session.title}'",
                        tint = if (isFavorite) MaterialTheme.colorScheme.tertiary
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (session.speakers.isNotEmpty()) {
                Text(session.speakers.joinToString { it.name },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                FormatBadge(session.format, session.lengthMinutes)
                Spacer(Modifier.weight(1f))
                Text(
                    listOfNotNull(session.room, session.language.uppercase()).joinToString(" · "),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
```

`FavoriteIconButton` and `FormatBadge` as their own small composables (as in
`checkpoint-1`) is the cleaner version — extract them once the card works.
</details>

## Done when…

- [ ] `SessionCard` renders title, speakers, a format badge and room/language.
- [ ] The star reflects `isFavorite` and calls `onToggleFavorite` — the card owns
      no state of its own.
- [ ] The star's `contentDescription` includes the session title (accessibility).
- [ ] You have a **light and a dark** `@Preview` using `sampleSession`.
- [ ] `App()` shows a few `SessionCard`s (e.g. a small `Column` of samples).

## Expected result

A Material card showing one session, in both light and dark, with a working star.
`checkpoint-1` puts a handful of cards on screen in `App()`.
