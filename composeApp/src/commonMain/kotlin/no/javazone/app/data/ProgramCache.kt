package no.javazone.app.data

/** Offline cache for the last successfully fetched program JSON. */
interface ProgramCache {
    suspend fun read(): String?
    suspend fun write(programJson: String)
}

/** Same platform split as [createFavoritesStore]: SQLite everywhere except Wasm. */
expect fun createProgramCache(): ProgramCache
