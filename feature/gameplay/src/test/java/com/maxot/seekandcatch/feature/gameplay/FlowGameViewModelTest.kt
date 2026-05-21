package com.maxot.seekandcatch.feature.gameplay.ui.flowgame

import com.maxot.seekandcatch.core.common.VisualFeedbackManager
import com.maxot.seekandcatch.core.common.model.GameDifficulty
import com.maxot.seekandcatch.core.common.model.GameMode
import com.maxot.seekandcatch.core.domain.flow.FlowGameData
import com.maxot.seekandcatch.core.domain.flow.FlowGameState
import com.maxot.seekandcatch.core.domain.flow.FlowGameUseCase
import com.maxot.seekandcatch.core.media.AudioManager
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.feature.gameplay.model.FlowGameUiEvent
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
class FlowGameViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var gameUseCase: FlowGameUseCase
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var vibrationManager: VibrationManager
    private lateinit var visualFeedbackManager: VisualFeedbackManager
    private lateinit var audioManager: AudioManager

    private val fakeGameState = MutableStateFlow<FlowGameState>(FlowGameState.Idle)

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
        whenever(settingsRepository.observeGameMode()).thenReturn(flowOf(GameMode.FLOW))
        whenever(settingsRepository.observeSoundState()).thenReturn(flowOf(true))
        whenever(settingsRepository.observeMusicState()).thenReturn(flowOf(true))
        whenever(settingsRepository.observeVibrationState()).thenReturn(flowOf(true))
        whenever(visualFeedbackManager.isLifeWasted).thenReturn(MutableStateFlow(false))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = FlowGameViewModel(
        gameUseCase, settingsRepository, vibrationManager, visualFeedbackManager, audioManager
    )

    @Test
    fun `idle game state shows loading`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.flowGameUiState.value.isLoading)
        assertFalse(viewModel.flowGameUiState.value.isReady)
    }

    @Test
    fun `created game state marks game as ready`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        fakeGameState.value = FlowGameState.Created(emptySet())
        advanceUntilIdle()

        assertTrue(viewModel.flowGameUiState.value.isReady)
        assertFalse(viewModel.flowGameUiState.value.isLoading)
    }

    @Test
    fun `paused game state sets isPaused flag`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        fakeGameState.value = FlowGameState.Paused
        advanceUntilIdle()

        assertTrue(viewModel.flowGameUiState.value.isPaused)
    }

    @Test
    fun `finished game state sets isFinished flag`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        fakeGameState.value = FlowGameState.Finished(42)
        advanceUntilIdle()

        assertTrue(viewModel.flowGameUiState.value.isFinished)
        assertFalse(viewModel.flowGameUiState.value.isActive)
    }

    @Test
    fun `resumed state updates score in UI state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        fakeGameState.value = FlowGameState.Resumed(FlowGameData(score = 7, lifeCount = 3))
        advanceUntilIdle()

        assertTrue(viewModel.flowGameUiState.value.isActive)
        assert(viewModel.flowGameUiState.value.score == 7)
    }
}
