package com.maxot.seekandcatch.feature.gameplay.ui.flashgame

import com.maxot.seekandcatch.core.common.VisualFeedbackManager
import com.maxot.seekandcatch.core.common.model.GameDifficulty
import com.maxot.seekandcatch.core.common.model.GameMode
import com.maxot.seekandcatch.core.domain.flash.FlashGameData
import com.maxot.seekandcatch.core.domain.flash.FlashGameState
import com.maxot.seekandcatch.core.domain.flash.FlashGameUseCase
import com.maxot.seekandcatch.core.media.AudioManager
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.feature.settings.VibrationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class FlashGameViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var gameUseCase: FlashGameUseCase
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var vibrationManager: VibrationManager
    private lateinit var visualFeedbackManager: VisualFeedbackManager
    private lateinit var audioManager: AudioManager

    private val fakeGameState = MutableStateFlow<FlashGameState>(FlashGameState.Idle)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        gameUseCase = mock()
        settingsRepository = mock()
        vibrationManager = mock()
        visualFeedbackManager = mock()
        audioManager = mock()

        whenever(gameUseCase.gameState).thenReturn(fakeGameState)
        whenever(settingsRepository.observeDifficulty()).thenReturn(flowOf(GameDifficulty.NORMAL))
        whenever(settingsRepository.observeGameMode()).thenReturn(flowOf(GameMode.FLASH))
        whenever(settingsRepository.observeSoundState()).thenReturn(flowOf(true))
        whenever(settingsRepository.observeMusicState()).thenReturn(flowOf(true))
        whenever(settingsRepository.observeVibrationState()).thenReturn(flowOf(true))
        whenever(visualFeedbackManager.isLifeWasted).thenReturn(MutableStateFlow(false))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = FlashGameViewModel(
        gameUseCase, settingsRepository, vibrationManager, visualFeedbackManager, audioManager
    )

    @Test
    fun `idle game state shows loading`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isReady)
    }

    @Test
    fun `created game state marks game as ready`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        fakeGameState.value = FlashGameState.Created(emptySet())
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isReady)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `paused game state sets isPaused flag`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        fakeGameState.value = FlashGameState.Paused
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isPaused)
        assertFalse(viewModel.uiState.value.isActive)
    }

    @Test
    fun `finished game state sets isFinished flag`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        fakeGameState.value = FlashGameState.Finished(score = 15)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isFinished)
        assertFalse(viewModel.uiState.value.isActive)
    }

    @Test
    fun `resumed state updates score in UI state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        fakeGameState.value = FlashGameState.Resumed(FlashGameData(score = 5, lifeCount = 2))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isActive)
        assert(viewModel.uiState.value.score == 5)
    }
}
