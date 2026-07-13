package no.javazone.app.data

import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Kotlin/Wasm has no SQLite driver, so the web target persists to the
 * browser's localStorage instead. This is the other side of the
 * expect/actual seam declared in FavoritesStore.kt.
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
