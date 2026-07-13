package no.javazone.app.data

import kotlinx.coroutines.CancellationException
import no.javazone.app.model.Session

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
        throw e
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
