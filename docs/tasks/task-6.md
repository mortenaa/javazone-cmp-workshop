# Task 6 — Persist favorites 🏁

**Goal:** favorites survive a restart — on every platform.

**Time box:** ~25 min · **Compare against:** `checkpoint-6` (the complete app)

## Theory recap

See *Block 3 — SQLDelight & expect/actual*: SQLDelight generates type-safe Kotlin
from plain `.sq` files, and `asFlow()` turns a query into a `Flow` that re-emits on
every table change (SQL → Flow → StateFlow → recomposition). But **there is no
SQLDelight driver for Kotlin/Wasm** — that's the `expect`/`actual` teaching
moment: same contract, a different implementation per platform.

## Steps

1. Write `Favorites.sq` (and `ProgramCache.sq`) under
   `commonMain/sqldelight/no/javazone/app/db/`. Build once, then look at the
   generated `favoritesQueries`.
2. `SqlFavoritesStore` in `commonMain`: `selectAll().asFlow().mapToList(...)` for
   the `Flow<Set<String>>`, and `insert`/`delete` for `setFavorite`.
3. Declare the seam in `commonMain`: an interface `FavoritesStore` +
   `expect fun createFavoritesStore(): FavoritesStore`. Add `actual`s for
   android / ios / jvm — each just hands `SqlFavoritesStore` its driver (the
   drivers are provided in the starter's platform source sets... or you write the
   one-liners; see `checkpoint-6`).
4. wasmJs `actual`: a `localStorage`-backed store implementing the same interface.
5. Point the repository's favorites `Flow` at the store and route `ToggleFavorite`
   through it. Restart the app and verify your stars are still there.

## Hints

<details>
<summary>Hint 1 — nudge</summary>

Keep the `expect` surface **tiny**: an ordinary `interface` for the behaviour, and
`expect fun createFavoritesStore()` only for the platform-specific *construction*.
The repository and ViewModel depend on the interface and never learn that four
storage engines exist.
</details>

<details>
<summary>Hint 2 — API / types</summary>

```sql
-- Favorites.sq
CREATE TABLE IF NOT EXISTS favorite (session_id TEXT NOT NULL PRIMARY KEY);
selectAll: SELECT session_id FROM favorite;
insert:    INSERT OR IGNORE INTO favorite(session_id) VALUES (?);
delete:    DELETE FROM favorite WHERE session_id = ?;
```

```kotlin
// commonMain
interface FavoritesStore {
    val favoriteIds: Flow<Set<String>>
    suspend fun setFavorite(sessionId: String, favorite: Boolean)
}
expect fun createFavoritesStore(): FavoritesStore
```

Drivers: `AndroidSqliteDriver` (needs a `Context`), `NativeSqliteDriver` (iOS),
`JdbcSqliteDriver` (JVM — call `Schema.create(driver)` yourself, JDBC won't).
</details>

<details>
<summary>Hint 3 — code (the two actual sides)</summary>

```kotlin
// android / ios / jvm — one line each, different driver:
actual fun createFavoritesStore(): FavoritesStore = SqlFavoritesStore(database, Dispatchers.IO)

// wasmJs — no SQLite driver, so localStorage:
private class LocalStorageFavoritesStore : FavoritesStore {
    private val state = MutableStateFlow(load())
    override val favoriteIds: Flow<Set<String>> = state
    override suspend fun setFavorite(sessionId: String, favorite: Boolean) {
        val updated = if (favorite) state.value + sessionId else state.value - sessionId
        localStorage.setItem(KEY, updated.joinToString(","))
        state.value = updated
    }
    // load() reads the comma-joined key back
}
actual fun createFavoritesStore(): FavoritesStore = LocalStorageFavoritesStore()
```

Full stores, the `ProgramCache` sibling, and the drivers are in `checkpoint-6`.
</details>

## Windows / Linux note

You can build android + jvm + wasm and **skip the iOS `actual`** — it's a
one-liner to add later on a Mac. The other three prove the seam.

## Done when…

- [ ] Favorites survive an app restart on your platform.
- [ ] The repository, ViewModel and screens didn't change — only the storage seam
      was added.
- [ ] Toggling a favorite on one screen updates it everywhere (the `Flow`).

## Expected result

`checkpoint-6` is the **complete app**: adaptive UI, ViewModel architecture,
networking with offline fallback, and persistence across two entirely different
storage technologies behind one `expect` function — plus the Map screen and the
test suite. Congratulations. See [stretch.md](stretch.md) if you're hungry.
