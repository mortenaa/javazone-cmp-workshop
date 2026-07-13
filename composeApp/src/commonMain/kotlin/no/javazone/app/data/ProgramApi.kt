package no.javazone.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import no.javazone.app.resources.Res

/** The hosted program feed; the bundled resource is the offline fallback. */
const val PROGRAM_URL =
    "https://raw.githubusercontent.com/mortenaa/javazone-cmp-workshop/main/program.json"

/**
 * Fetches the conference program. The Ktor engine is chosen by the
 * per-platform dependency in build.gradle.kts (OkHttp, Darwin or JS).
 *
 * [ProgramJson] (the lenient parser) is provided in ProgramDto.kt.
 */
class ProgramApi(
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) { json(ProgramJson) }
        // Conference wifi stalls more often than it fails: cap the wait, then fall back.
        install(HttpTimeout) { requestTimeoutMillis = 5_000 }
    },
) {
    suspend fun fetchProgram(): ProgramDto = client.get(PROGRAM_URL).body()

    /** Offline fallback: the same JSON bundled as a compose resource. */
    suspend fun bundledProgram(): ProgramDto =
        ProgramJson.decodeFromString(Res.readBytes("files/program.json").decodeToString())
}
