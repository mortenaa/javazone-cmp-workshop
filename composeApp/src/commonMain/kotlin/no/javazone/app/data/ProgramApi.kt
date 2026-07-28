package no.javazone.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import no.javazone.app.resources.Res

/** The hosted program feed; the bundled resource is the offline fallback. */
const val PROGRAM_URL =
    "https://raw.githubusercontent.com/mortenaa/javazone-cmp-workshop/main/program.json"

class ProgramApi(
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(ProgramJson)
            // raw.githubusercontent.com serves .json files as text/plain (see notes) —
            // register that content type too so ContentNegotiation still deserializes it.
            json(ProgramJson, contentType = ContentType.Text.Plain)
        }
        // Conference wifi stalls more often than it fails: cap the wait, then fall back.
        install(HttpTimeout) { requestTimeoutMillis = 5_000 }
    },
) {
    suspend fun fetchProgram(): ProgramDto = client.get(PROGRAM_URL).body()

    /** Offline fallback: the same JSON bundled as a compose resource. */
    suspend fun bundledProgram(): ProgramDto =
        ProgramJson.decodeFromString(ProgramDto.serializer(), Res.readBytes("files/program.json").decodeToString())
}
