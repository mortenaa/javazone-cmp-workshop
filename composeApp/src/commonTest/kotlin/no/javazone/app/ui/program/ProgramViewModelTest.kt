package no.javazone.app.ui.program

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import no.javazone.app.data.FavoritesStore
import no.javazone.app.data.ProgramApi
import no.javazone.app.data.ProgramCache
import no.javazone.app.data.ProgramJson
import no.javazone.app.data.ProgramRepository
import no.javazone.app.model.Format
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val PROGRAM = """
{
  "sessions": [
    {"id": "a1", "title": "Talk A", "abstract": "A.", "format": "presentation", "length": "45",
     "language": "en", "room": "Room I", "speakers": [{"name": "Ada Lovelace"}],
     "startTime": "2026-09-02T09:00", "endTime": "2026-09-02T09:45", "startSlot": "2026-09-02T09:00"},
    {"id": "b2", "title": "Workshop B", "abstract": "B.", "format": "workshop", "length": "120",
     "language": "no", "room": "Workshop A",
     "startTime": "2026-09-01T09:00", "endTime": "2026-09-01T11:00", "startSlot": "2026-09-01T09:00"},
    {"id": "c3", "title": "Lightning C", "abstract": "C.", "format": "lightning-talk", "length": "20",
     "language": "en", "room": "Room VI",
     "startTime": "2026-09-03T09:00", "endTime": "2026-09-03T09:20", "startSlot": "2026-09-03T09:00"}
  ]
}
"""

private class FakeFavoritesStore : FavoritesStore {
    private val state = MutableStateFlow(emptySet<String>())
    override val favoriteIds: Flow<Set<String>> = state
    override suspend fun setFavorite(sessionId: String, favorite: Boolean) {
        state.value = if (favorite) state.value + sessionId else state.value - sessionId
    }
}

private class FakeProgramCache : ProgramCache {
    var stored: String? = null
    override suspend fun read(): String? = stored
    override suspend fun write(programJson: String) {
        stored = programJson
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProgramViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(failNetwork: Boolean = false): ProgramViewModel {
        val engine = MockEngine {
            if (failNetwork) throw RuntimeException("no network")
            respond(PROGRAM, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = HttpClient(engine) { install(ContentNegotiation) { json(ProgramJson) } }
        return ProgramViewModel(ProgramRepository(ProgramApi(client), FakeFavoritesStore(), FakeProgramCache()))
    }

    @Test
    fun loadsProgramAndDefaultsToWednesday() = runTest {
        val state = viewModel().state.first { !it.isLoading }

        assertEquals(3, state.sessions.size)
        assertFalse(state.isOffline)
        assertEquals(LocalDate(2026, 9, 2), state.selectedDay)
        assertEquals(listOf(LocalDate(2026, 9, 1), LocalDate(2026, 9, 2), LocalDate(2026, 9, 3)), state.dayTabs)
    }

    @Test
    fun formatFilterNarrowsTheDayAndClearRestoresIt() = runTest {
        val vm = viewModel()
        vm.state.first { !it.isLoading }

        vm.onIntent(ProgramIntent.ToggleFormat(Format.WORKSHOP))
        assertTrue(vm.state.value.daySlots(LocalDate(2026, 9, 2)).isEmpty())

        vm.onIntent(ProgramIntent.ClearFilters)
        assertEquals(1, vm.state.value.daySlots(LocalDate(2026, 9, 2)).size)
    }

    @Test
    fun searchMatchesTitleAndSpeakerName() = runTest {
        val vm = viewModel()
        vm.state.first { !it.isLoading }
        val wednesday = LocalDate(2026, 9, 2)

        vm.onIntent(ProgramIntent.Search("lovelace")) // speaker of "Talk A"
        assertEquals(1, vm.state.value.daySlots(wednesday).size)

        vm.onIntent(ProgramIntent.Search("no such session"))
        assertTrue(vm.state.value.daySlots(wednesday).isEmpty())
        assertTrue(vm.state.value.hasActiveFilters)

        vm.onIntent(ProgramIntent.ClearFilters)
        assertEquals("", vm.state.value.searchQuery)
        assertEquals(1, vm.state.value.daySlots(wednesday).size)
    }

    @Test
    fun toggleFavoriteRoundTripsThroughTheStore() = runTest {
        val vm = viewModel()
        vm.state.first { !it.isLoading }

        vm.onIntent(ProgramIntent.ToggleFavorite("a1"))
        assertEquals(setOf("a1"), vm.state.first { it.favoriteIds.isNotEmpty() }.favoriteIds)
        assertEquals(1, vm.state.value.favoriteSlots(LocalDate(2026, 9, 2)).size)

        vm.onIntent(ProgramIntent.ToggleFavorite("a1"))
        assertTrue(vm.state.first { it.favoriteIds.isEmpty() }.favoriteIds.isEmpty())
    }

    @Test
    fun networkFailureFallsBackAndShowsOfflineBanner() = runTest {
        // No cache -> the repository falls back to the bundled program.json (156 sessions).
        val vm = viewModel(failNetwork = true)
        val state = vm.state.first { !it.isLoading }

        assertTrue(state.isOffline)
        assertTrue(state.showOfflineBanner)
        assertTrue(state.sessions.size > 100)

        vm.onIntent(ProgramIntent.DismissOfflineBanner)
        assertFalse(vm.state.value.showOfflineBanner)
    }
}
