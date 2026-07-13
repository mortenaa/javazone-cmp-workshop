package no.javazone.app.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import no.javazone.app.db.AppDatabase

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
