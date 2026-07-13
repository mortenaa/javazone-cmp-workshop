package no.javazone.app.data

import kotlinx.coroutines.flow.Flow

/** Local persistence for the user's favorite sessions. */
interface FavoritesStore {
    val favoriteIds: Flow<Set<String>>
    suspend fun setFavorite(sessionId: String, favorite: Boolean)
}

/**
 * expect/actual seam: Android, iOS and Desktop persist favorites in SQLite
 * via SQLDelight, but there is no SQLite driver on Kotlin/Wasm — the web
 * target stores favorites in the browser's localStorage instead.
 */
expect fun createFavoritesStore(): FavoritesStore
