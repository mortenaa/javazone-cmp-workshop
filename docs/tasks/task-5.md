# Task 5 — Fetch the program

**Goal:** the program arrives over the network — and the app survives without it.

**Time box:** ~20 min · **Compare against:** `checkpoint-5`

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

```kotlin
class ProgramApi(
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) { json(ProgramJson) }
        install(HttpTimeout) { requestTimeoutMillis = 5_000 }
    },
) {
    suspend fun fetchProgram(): ProgramDto = client.get(PROGRAM_URL).body()
    suspend fun bundledProgram(): ProgramDto =
        ProgramJson.decodeFromString(Res.readBytes("files/program.json").decodeToString())
}
```

Taking the `client` as a constructor parameter with a default is what makes this
testable with Ktor's `MockEngine` later.
</details>

<details>
<summary>Hint 3 — code (the fallback chain)</summary>

```kotlin
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
```

For now the cache can be a trivial in-memory/no-op implementation — real
persistence is Task 6. Full `ProgramApi` / `ProgramRepository` in `checkpoint-5`.
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
