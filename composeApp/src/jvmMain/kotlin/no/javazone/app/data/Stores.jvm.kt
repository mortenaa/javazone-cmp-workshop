package no.javazone.app.data

import kotlinx.coroutines.Dispatchers
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import no.javazone.app.db.AppDatabase
import java.io.File

private val database by lazy {
    val dir = File(System.getProperty("user.home"), ".javazone2026").apply { mkdirs() }
    val driver = JdbcSqliteDriver("jdbc:sqlite:${File(dir, "javazone.db")}")
    AppDatabase.Schema.create(driver) // tables use IF NOT EXISTS, safe on every start
    AppDatabase(driver)
}

actual fun createFavoritesStore(): FavoritesStore = SqlFavoritesStore(database, Dispatchers.IO)

actual fun createProgramCache(): ProgramCache = SqlProgramCache(database, Dispatchers.IO)
