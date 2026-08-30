package no.javazone.app.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.javazone.app.model.Session

/** Result of a program load; [isOffline] is true when the network fetch failed. */
class ProgramLoad(val sessions: List<Session>, val isOffline: Boolean)

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

    private val toggleMutex = Mutex()

    /** Serialized read-modify-write so rapid taps can't race each other. */
    suspend fun toggleFavorite(sessionId: String) = toggleMutex.withLock {
        val favorites = favoritesStore.favoriteIds.first()
        favoritesStore.setFavorite(sessionId, sessionId !in favorites)
    }

    private suspend fun readCache(): ProgramDto? = try {
        cache.read()?.let { ProgramJson.decodeFromString(ProgramDto.serializer(), it) }
    } catch (e: Exception) {
        null
    }
}
