package com.maxot.seekandcatch.feature.gameplay.gameselection

import com.maxot.seekandcatch.core.common.model.GameDifficulty
import com.maxot.seekandcatch.core.common.model.GameMode
import com.maxot.seekandcatch.core.media.AudioManager
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.feature.gameplay.gameselection.model.GameSelectionUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class GameSelectionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var audioManager: AudioManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = mock()
        audioManager = mock()
        whenever(settingsRepository.observeDifficulty()).thenReturn(flowOf(GameDifficulty.NORMAL))
        whenever(settingsRepository.observeGameMode()).thenReturn(flowOf(GameMode.FLOW))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = GameSelectionViewModel(settingsRepository, audioManager)

    @Test
    fun `initial state reflects repository values`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(GameDifficulty.NORMAL, viewModel.uiState.value.selectedDifficulty)
        assertEquals(GameMode.FLOW, viewModel.uiState.value.selectedGameMode)
    }

    @Test
    fun `changing difficulty saves to repository`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(GameSelectionUiEvent.ChangeGameDifficult(GameDifficulty.HARD))
        advanceUntilIdle()
        verify(settingsRepository).setDifficulty(GameDifficulty.HARD)
    }

    @Test
    fun `changing game mode saves to repository`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(GameSelectionUiEvent.ChangeGameMode(GameMode.FLASH))
        advanceUntilIdle()
        verify(settingsRepository).setGameMode(GameMode.FLASH)
    }
}
