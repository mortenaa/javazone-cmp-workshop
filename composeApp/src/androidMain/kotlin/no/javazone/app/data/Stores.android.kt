package no.javazone.app.data

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.Dispatchers
import no.javazone.app.JavaZoneApp
import no.javazone.app.db.AppDatabase

private val database by lazy {
    AppDatabase(AndroidSqliteDriver(AppDatabase.Schema, JavaZoneApp.appContext, "javazone.db"))
}

actual fun createFavoritesStore(): FavoritesStore = SqlFavoritesStore(database, Dispatchers.IO)

actual fun createProgramCache(): ProgramCache = SqlProgramCache(database, Dispatchers.IO)
