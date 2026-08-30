# Task 6 — Persist favorites

**Goal:** favorites survive a restart — on every platform.

**Start from:** `checkpoint-5` · **Finished result:** `checkpoint-6` (the complete app)

> `checkpoint-6` is more than this task's solution: it also ships the test suite
> that Block 4 walks through.

## Theory recap

See *Block 3 — SQLDelight & expect/actual*: SQLDelight generates type-safe Kotlin
from plain `.sq` files, and `asFlow()` turns a query into a `Flow` that re-emits on
every table change (SQL → Flow → StateFlow → recomposition). On the web
SQLDelight works differently: its `web-worker-driver` runs **sql.js** — SQLite
compiled to JavaScript — inside a background web worker, which comes with its
own npm and bundler setup. We take that as an opportunity: `localStorage` on the
web, SQLite everywhere else. Which is exactly what `expect`/`actual` is for:
same contract, a different implementation per platform.

## Steps

1. Write `Favorites.sq` (and `ProgramCache.sq`) under
   `commonMain/sqldelight/no/javazone/app/db/`. Build once, then look at the
   generated `favoritesQueries`.
2. `SqlFavoritesStore` in `commonMain`: `selectAll().asFlow().mapToList(...)` for
   the `Flow<Set<String>>`, and `insert`/`delete` for `setFavorite`.
3. Declare the contract in `commonMain`: an interface `FavoritesStore` +
   `expect fun createFavoritesStore(): FavoritesStore`. Add `actual`s for
   android / ios / jvm — each just hands `SqlFavoritesStore` its driver (the
   drivers are provided in the starter's platform source sets... or you write the
   one-liners; see `checkpoint-6`).
4. wasmJs `actual`: a `localStorage`-backed store implementing the same interface.
5. Point the repository's favorites `Flow` at the store and route `ToggleFavorite`
   through it. Restart the app and verify your stars are still there.

## Provided: the Map screen

Your starting point already contains the finished **Map screen** (`ui/map/` —
pan/zoom over a vector drawable; provided content, no task builds it), still
unwired. Replace the Map route's `EmptyState` stub in `App.kt` with
`MapScreen()` whenever you like. `checkpoint-6` additionally carries a small
test suite (`commonTest` / `jvmTest`) worth a read.

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

The signatures, as in `checkpoint-6`. The Gradle side is already done for you:
the SQLDelight plugin, the per-platform drivers and the
`AppDatabase` block (`packageName no.javazone.app.db`) are all in the starter's
`build.gradle.kts`.

```sql
-- Favorites.sq
CREATE TABLE IF NOT EXISTS favorite (session_id TEXT NOT NULL PRIMARY KEY);
selectAll: SELECT session_id FROM favorite;
insert:    INSERT OR IGNORE INTO favorite(session_id) VALUES (?);
delete:    DELETE FROM favorite WHERE session_id = ?;
```

```kotlin
// commonMain — the contract:
interface FavoritesStore {
    val favoriteIds: Flow<Set<String>>
    suspend fun setFavorite(sessionId: String, favorite: Boolean)
}
expect fun createFavoritesStore(): FavoritesStore

// ProgramCache.kt — the Task 5 interface moves here and gets the same treatment:
expect fun createProgramCache(): ProgramCache

// commonMain — the SQL implementation the three SQLite platforms share:
class SqlFavoritesStore(database: AppDatabase, dispatcher: CoroutineDispatcher) : FavoritesStore
class SqlProgramCache(database: AppDatabase, dispatcher: CoroutineDispatcher) : ProgramCache
```

The repository grows the favorites side, and the ViewModel follows the `Flow`:

```kotlin
class ProgramRepository(
    private val api: ProgramApi = ProgramApi(),
    private val favoritesStore: FavoritesStore = createFavoritesStore(),
    private val cache: ProgramCache = createProgramCache(),
) {
    val favoriteIds: Flow<Set<String>>
    suspend fun loadSessions(): ProgramLoad
    suspend fun toggleFavorite(sessionId: String)
}
```

Drivers: `AndroidSqliteDriver` (needs a `Context`), `NativeSqliteDriver` (iOS),
`JdbcSqliteDriver` (JVM — call `Schema.create(driver)` yourself, JDBC won't).
</details>

<details>
<summary>Hint 3 — full code (copy-paste solves the task)</summary>

Everything you need to type, in build order.

**`commonMain/sqldelight/no/javazone/app/db/Favorites.sq`** (new file — note
the directory: `sqldelight/`, not `kotlin/`)

```sql
CREATE TABLE IF NOT EXISTS favorite (
    session_id TEXT NOT NULL PRIMARY KEY
);

selectAll:
SELECT session_id FROM favorite;

insert:
INSERT OR IGNORE INTO favorite(session_id) VALUES (?);

delete:
DELETE FROM favorite WHERE session_id = ?;
```

**`commonMain/sqldelight/no/javazone/app/db/ProgramCache.sq`** (new file)

```sql
-- Single-row cache holding the raw JSON of the last successful program fetch.
CREATE TABLE IF NOT EXISTS program_cache (
    id INTEGER NOT NULL PRIMARY KEY,
    json TEXT NOT NULL
);

select:
SELECT json FROM program_cache WHERE id = 0;

upsert:
INSERT OR REPLACE INTO program_cache(id, json) VALUES (0, ?);
```

Build once now (any target) so SQLDelight generates `AppDatabase`,
`favoritesQueries` and `programCacheQueries`.

**`data/FavoritesStore.kt`** (new file)

```kotlin
/** Local persistence for the user's favorite sessions. */
interface FavoritesStore {
    val favoriteIds: Flow<Set<String>>
    suspend fun setFavorite(sessionId: String, favorite: Boolean)
}

/**
 * Android, iOS and Desktop persist favorites in SQLite via SQLDelight. The
 * browser could too — SQLDelight's web-worker driver runs sql.js in a background
 * worker — but that needs its own npm and bundler setup, so the web target keeps
 * it simple with localStorage. Hence expect/actual: one contract, a different
 * implementation per platform.
 */
expect fun createFavoritesStore(): FavoritesStore
```

**`data/ProgramCache.kt`** (new file — and *delete* `ProgramCache` +
`InMemoryProgramCache` from `ProgramRepository.kt`, they move/retire here)

```kotlin
/** Offline cache for the last successfully fetched program JSON. */
interface ProgramCache {
    suspend fun read(): String?
    suspend fun write(programJson: String)
}

/** Same platform split as [createFavoritesStore]: SQLite everywhere except Wasm. */
expect fun createProgramCache(): ProgramCache
```

**`data/SqlStores.kt`** (new file, commonMain — shared by the three SQLite
platforms)

```kotlin
// The platform actuals pass Dispatchers.IO (JVM, Android and Native have it;
// it does not exist in common code, hence the parameter with a Default fallback).

/** SQLDelight-backed favorites, used on Android, iOS and Desktop. */
class SqlFavoritesStore(
    private val database: AppDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : FavoritesStore {

    override val favoriteIds: Flow<Set<String>> =
        database.favoritesQueries.selectAll()
            .asFlow()
            .mapToList(dispatcher)
            .map { it.toSet() }

    override suspend fun setFavorite(sessionId: String, favorite: Boolean) {
        withContext(dispatcher) {
            if (favorite) database.favoritesQueries.insert(sessionId)
            else database.favoritesQueries.delete(sessionId)
        }
    }
}

/** SQLDelight-backed program cache, sharing the same database. */
class SqlProgramCache(
    private val database: AppDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ProgramCache {

    override suspend fun read(): String? = withContext(dispatcher) {
        database.programCacheQueries.select().executeAsOneOrNull()
    }

    override suspend fun write(programJson: String) {
        withContext(dispatcher) {
            database.programCacheQueries.upsert(programJson)
        }
    }
}
```

(`asFlow`/`mapToList` are `app.cash.sqldelight.coroutines.*`; `AppDatabase` is
the generated `no.javazone.app.db.AppDatabase`.)

**`androidMain/…/data/Stores.android.kt`** (new file)

```kotlin
private val database by lazy {
    AppDatabase(AndroidSqliteDriver(AppDatabase.Schema, JavaZoneApp.appContext, "javazone.db"))
}

actual fun createFavoritesStore(): FavoritesStore = SqlFavoritesStore(database, Dispatchers.IO)

actual fun createProgramCache(): ProgramCache = SqlProgramCache(database, Dispatchers.IO)
```

(`JavaZoneApp.appContext` is provided by the starter's Android source set.)

**`iosMain/…/data/Stores.ios.kt`** (new file)

```kotlin
private val database by lazy {
    AppDatabase(NativeSqliteDriver(AppDatabase.Schema, "javazone.db"))
}

actual fun createFavoritesStore(): FavoritesStore = SqlFavoritesStore(database, Dispatchers.IO)

actual fun createProgramCache(): ProgramCache = SqlProgramCache(database, Dispatchers.IO)
```

**`jvmMain/…/data/Stores.jvm.kt`** (new file)

```kotlin
private val database by lazy {
    val dir = File(System.getProperty("user.home"), ".javazone2026").apply { mkdirs() }
    val driver = JdbcSqliteDriver("jdbc:sqlite:${File(dir, "javazone.db")}")
    AppDatabase.Schema.create(driver) // tables use IF NOT EXISTS, safe on every start
    AppDatabase(driver)
}

actual fun createFavoritesStore(): FavoritesStore = SqlFavoritesStore(database, Dispatchers.IO)

actual fun createProgramCache(): ProgramCache = SqlProgramCache(database, Dispatchers.IO)
```

**`wasmJsMain/…/data/Stores.wasmJs.kt`** (new file — `localStorage` is
`kotlinx.browser.localStorage`)

```kotlin
/**
 * The web target could run SQLite via SQLDelight's web-worker driver (sql.js in
 * a background worker), but localStorage is far simpler here. This is the web
 * half of the expect/actual pair declared in FavoritesStore.kt.
 */
private class LocalStorageFavoritesStore : FavoritesStore {
    private val state = MutableStateFlow(load())

    override val favoriteIds: Flow<Set<String>> = state

    override suspend fun setFavorite(sessionId: String, favorite: Boolean) {
        val updated = if (favorite) state.value + sessionId else state.value - sessionId
        localStorage.setItem(KEY, updated.joinToString(","))
        state.value = updated
    }

    private fun load(): Set<String> =
        localStorage.getItem(KEY)?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()

    companion object {
        private const val KEY = "javazone.favorites"
    }
}

private class LocalStorageProgramCache : ProgramCache {
    override suspend fun read(): String? = localStorage.getItem("javazone.program")
    override suspend fun write(programJson: String) = localStorage.setItem("javazone.program", programJson)
}

actual fun createFavoritesStore(): FavoritesStore = LocalStorageFavoritesStore()

actual fun createProgramCache(): ProgramCache = LocalStorageProgramCache()
```

**`data/ProgramRepository.kt`** (edit the existing file)

Remove the `ProgramCache` interface and `InMemoryProgramCache` (they moved to
`ProgramCache.kt` / retired), then change the class to own favorites too:

```kotlin
/**
 * Single entry point for program data and favorites.
 * Load order: network -> cached copy of the last fetch -> bundled resource.
 */
class ProgramRepository(
    private val api: ProgramApi = ProgramApi(),
    private val favoritesStore: FavoritesStore = createFavoritesStore(),
    private val cache: ProgramCache = createProgramCache(),
) {
    val favoriteIds: Flow<Set<String>> = favoritesStore.favoriteIds

    // loadSessions() and readCache() stay exactly as in Task 5

    private val toggleMutex = Mutex()

    /** Serialized read-modify-write so rapid taps can't race each other. */
    suspend fun toggleFavorite(sessionId: String) = toggleMutex.withLock {
        val favorites = favoritesStore.favoriteIds.first()
        favoritesStore.setFavorite(sessionId, sessionId !in favorites)
    }
}
```

**`ui/program/ProgramViewModel.kt`** (edit the existing file, two changes)

In `init`, collect the store's `Flow` into the state — this is what makes a
toggle update every screen:

```kotlin
init {
    loadProgram()
    viewModelScope.launch {
        repository.favoriteIds.collect { ids ->
            _state.update { it.copy(favoriteIds = ids) }
        }
    }
}
```

And route the intent through the repository instead of copying state:

```kotlin
is ProgramIntent.ToggleFavorite ->
    viewModelScope.launch { repository.toggleFavorite(intent.sessionId) }
```
</details>

## Windows / Linux note

You can build android + jvm + wasm and **skip the iOS `actual`** — it's a
one-liner to add later on a Mac. The other three prove the point.

## Done when…

- [ ] Favorites survive an app restart on your platform.
- [ ] The repository, ViewModel and screens didn't change — only the platform storage
      code was added.
- [ ] Toggling a favorite on one screen updates it everywhere (the `Flow`).

## Expected result

`checkpoint-6` is the **complete app**: adaptive UI, ViewModel architecture,
networking with offline fallback, and persistence across two entirely different
storage technologies behind one `expect` function — plus the Map screen and the
test suite. Congratulations. See [stretch.md](stretch.md) if you're hungry.
