package no.javazone.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import no.javazone.app.db.AppDatabase

private val database by lazy {
    AppDatabase(NativeSqliteDriver(AppDatabase.Schema, "javazone.db"))
}

actual fun createFavoritesStore(): FavoritesStore = SqlFavoritesStore(database, Dispatchers.IO)

actual fun createProgramCache(): ProgramCache = SqlProgramCache(database, Dispatchers.IO)
