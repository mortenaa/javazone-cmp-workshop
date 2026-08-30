package no.javazone.app.data

import kotlinx.coroutines.flow.Flow

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
