# Task 5 — Fetch the program

**Goal:** the program arrives over the network — and the app survives without it.

**Start from:** `checkpoint-4` · **Finished result:** `checkpoint-5`

> `checkpoint-5` is more than this task's solution: it also ships the finished
> Map screen that will be used for Task 6.

## Theory recap

See *Block 3 — Shared logic & data*: Ktor is one HTTP client with a per-platform
engine (OkHttp / Darwin / JS), `ContentNegotiation` plugs in
kotlinx.serialization, and everything is `suspend`. The repository pattern gives
the ViewModel a single door to data with a **network → cache → bundled** fallback
chain. Flaky wifi usually *hangs* rather than fails, so an `HttpTimeout` is
essential.

## Steps

1. Build `ProgramApi`: an `HttpClient` with `ContentNegotiation { json(ProgramJson) }`
   and `HttpTimeout { requestTimeoutMillis = 5_000 }`. (`ProgramJson` is already
   provided in `data/ProgramDto.kt`.)
2. `suspend fun fetchProgram(): ProgramDto = client.get(PROGRAM_URL).body()`, and a
   `suspend fun bundledProgram()` that reads the bundled resource as the fallback.
3. `ProgramRepository.loadSessions()`: try the network and refresh the cache; on
   **any** failure serve cache-or-bundled and set `isOffline = true`.
4. Wire `isLoading` / `loadFailed` / `isOffline` into the ViewModel; handle the
   `Retry` intent; show the `OfflineBanner` when offline.
5. **Test it:** turn wifi off, relaunch → real data with the offline banner.

The hosted URL is real (GitHub Pages, serves proper `application/json` + CORS):
`https://mortenaa.github.io/javazone-cmp-workshop/program.json`

## Hints

<details>
<summary>Hint 1 — nudge</summary>

Two gotchas to avoid up front: (1) don't forget the `HttpTimeout` — without it a
stalled connection gives you an infinite spinner; (2) in the repository's `catch`,
**rethrow `CancellationException`** before catching everything else.

To *prove* the network path works (and not just the fallback), watch the
offline banner: online → no banner; wifi off → banner. If the banner shows
even with wifi on, your fetch is failing silently — check the logs for the
actual exception before assuming the wifi is at fault.
</details>

<details>
<summary>Hint 2 — API / types</summary>

The signatures, as in `checkpoint-5` (two new files in `data/`):

```kotlin
const val PROGRAM_URL: String

class ProgramApi(private val client: HttpClient = /* configured default */) {
    suspend fun fetchProgram(): ProgramDto
    suspend fun bundledProgram(): ProgramDto   // the offline fallback
}

class ProgramLoad(val sessions: List<Session>, val isOffline: Boolean)

interface ProgramCache {
    suspend fun read(): String?
    suspend fun write(programJson: String)
}
class InMemoryProgramCache : ProgramCache     // enough for Task 5; Task 6 makes it persistent

class ProgramRepository(
    private val api: ProgramApi = ProgramApi(),
    private val cache: ProgramCache = InMemoryProgramCache(),
) {
    suspend fun loadSessions(): ProgramLoad
}
```

And the ViewModel gains a constructor parameter:

```kotlin
class ProgramViewModel(
    private val repository: ProgramRepository = ProgramRepository(),
) : ViewModel()
```

Taking the `client` (and the repository) as constructor parameters with
defaults is what makes these testable with Ktor's `MockEngine` later.
</details>

<details>
<summary>Hint 3 — full code (copy-paste solves the task)</summary>

Everything you need to type. If your screens came via Task 4's Hint 3 or
`checkpoint-4`, the UI side (`OfflineBanner`, `LoadingState`, `ErrorState`,
the `Retry` refresh button) is already wired to the state flags — this task is
data-layer only.

**`data/ProgramApi.kt`** (new file)

```kotlin
/** The hosted program feed; the bundled resource is the offline fallback. */
const val PROGRAM_URL =
    "https://mortenaa.github.io/javazone-cmp-workshop/program.json"

/**
 * Fetches the conference program. The Ktor engine is chosen by the
 * per-platform dependency in build.gradle.kts (OkHttp, Darwin or JS).
 *
 * [ProgramJson] (the lenient parser) is provided in ProgramDto.kt.
 */
class ProgramApi(
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) { json(ProgramJson) }
        install(HttpTimeout) { requestTimeoutMillis = 5_000 }
    },
) {
    suspend fun fetchProgram(): ProgramDto = client.get(PROGRAM_URL).body()

    /** Offline fallback: the same JSON bundled as a compose resource. */
    suspend fun bundledProgram(): ProgramDto =
        ProgramJson.decodeFromString(Res.readBytes("files/program.json").decodeToString())
}
```

(Import note: `ContentNegotiation` is the **client** plugin,
`io.ktor.client.plugins.contentnegotiation.ContentNegotiation`, and `json` is
`io.ktor.serialization.kotlinx.json.json`.)

**`data/ProgramRepository.kt`** (new file)

```kotlin
/** Result of a program load; [isOffline] is true when the network fetch failed. */
class ProgramLoad(val sessions: List<Session>, val isOffline: Boolean)

/** Offline cache for the last successfully fetched program JSON. */
interface ProgramCache {
    suspend fun read(): String?
    suspend fun write(programJson: String)
}

/**
 * In-memory cache: enough for Task 5, gone on restart. Task 6 replaces this with
 * an SQLDelight-backed cache behind the same interface.
 */
class InMemoryProgramCache : ProgramCache {
    private var stored: String? = null
    override suspend fun read(): String? = stored
    override suspend fun write(programJson: String) {
        stored = programJson
    }
}

/**
 * Single entry point for program data.
 * Load order: network -> cached copy of the last fetch -> bundled resource.
 */
class ProgramRepository(
    private val api: ProgramApi = ProgramApi(),
    private val cache: ProgramCache = InMemoryProgramCache(),
) {
    suspend fun loadSessions(): ProgramLoad = try {
        val fetched = api.fetchProgram()
        cache.write(ProgramJson.encodeToString(ProgramDto.serializer(), fetched))
        ProgramLoad(fetched.sessions.map { it.toSession() }, isOffline = false)
    } catch (e: CancellationException) {
        throw e                                   // never swallow cancellation
    } catch (e: Exception) {
        val fallback = readCache() ?: api.bundledProgram()
        ProgramLoad(fallback.sessions.map { it.toSession() }, isOffline = true)
    }

    private suspend fun readCache(): ProgramDto? = try {
        cache.read()?.let { ProgramJson.decodeFromString(ProgramDto.serializer(), it) }
    } catch (e: Exception) {
        null
    }
}
```

**`ui/program/ProgramViewModel.kt`** (edit the existing file, three changes)

1. Give the class the repository as a constructor parameter:

```kotlin
class ProgramViewModel(
    private val repository: ProgramRepository = ProgramRepository(),
) : ViewModel() {
```

2. Replace the body of `loadProgram()`:

```kotlin
private fun loadProgram() {
    viewModelScope.launch {
        // Every (re)load resets the banner dismissal: it may reappear on a
        // failed refresh and disappears for real once a fetch succeeds.
        // The full-screen spinner only shows when there is nothing to look at.
        _state.update {
            it.copy(isLoading = it.sessions.isEmpty(), loadFailed = false, offlineBannerDismissed = false)
        }
        try {
            val load = repository.loadSessions()
            _state.update {
                it.copy(
                    isLoading = false,
                    sessions = load.sessions,
                    isOffline = load.isOffline,
                    selectedDay = it.selectedDay ?: defaultDay(load.sessions),
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Never replace a visible list with the error screen.
            _state.update { it.copy(isLoading = false, loadFailed = it.sessions.isEmpty()) }
        }
    }
}
```

3. Delete the file-private `loadBundledSessions()` helper — the repository
   (via `ProgramApi.bundledProgram()`) owns that now.
</details>

## Done when…

- [ ] With connectivity, the program loads from the network — **no offline
      banner**. (Banner showing while online = the fetch is silently failing;
      see Hint 1.)
- [ ] With wifi off, the app falls back to bundled data and shows the offline
      banner (dismissible) instead of an error.
- [ ] `Retry` re-attempts the fetch.

## Expected result

The app behaves the same online and offline — offline it shows slightly stale data
with a banner, never an apology screen. Compare with `checkpoint-5`.
