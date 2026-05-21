package com.maxot.seekandcatch.feature.gameplay

import androidx.lifecycle.SavedStateHandle
import com.maxot.seekandcatch.core.common.model.GameDifficulty
import com.maxot.seekandcatch.core.common.model.GameMode
import com.maxot.seekandcatch.core.common.model.LeaderboardRecord
import com.maxot.seekandcatch.core.media.AudioManager
import com.maxot.seekandcatch.data.repository.AuthRepository
import com.maxot.seekandcatch.data.repository.LeaderboardRepository
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.feature.gameplay.navigation.SCORE_ARG
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class GameResultViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var leaderboardRepository: LeaderboardRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var audioManager: AudioManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        leaderboardRepository = mock()
        authRepository = mock()
        settingsRepository = mock()
        audioManager = mock()
        whenever(settingsRepository.observeGameMode()).thenReturn(flowOf(GameMode.FLOW))
        whenever(settingsRepository.observeDifficulty()).thenReturn(flowOf(GameDifficulty.NORMAL))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(score: Int): GameResultViewModel {
        val savedStateHandle = SavedStateHandle(mapOf(SCORE_ARG to score))
        return GameResultViewModel(
            savedStateHandle,
            leaderboardRepository,
            authRepository,
            settingsRepository,
            audioManager
        )
    }

    @Test
    fun `score is set from SavedStateHandle`() = runTest {
        whenever(authRepository.getUserId()).thenReturn("user1")
        whenever(leaderboardRepository.observeRecords()).thenReturn(flowOf(emptyList()))

        val viewModel = createViewModel(score = 42)
        advanceUntilIdle()

        assertEquals(42, viewModel.uiState.value.lastScore)
    }

    @Test
    fun `isNewBest is true when no previous records exist`() = runTest {
        whenever(authRepository.getUserId()).thenReturn("user1")
        whenever(leaderboardRepository.observeRecords()).thenReturn(flowOf(emptyList()))

        val viewModel = createViewModel(score = 100)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isNewBest)
    }

    @Test
    fun `isNewBest is false when previous best is higher`() = runTest {
        whenever(authRepository.getUserId()).thenReturn("user1")
        val existing = LeaderboardRecord(
            userId = "user1",
            score = 200,
            gameMode = GameMode.FLOW,
            difficulty = GameDifficulty.NORMAL
        )
        whenever(leaderboardRepository.observeRecords()).thenReturn(flowOf(listOf(existing)))

        val viewModel = createViewModel(score = 100)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isNewBest)
    }

    @Test
    fun `addRecord is called when score beats remote best`() = runTest {
        whenever(authRepository.getUserId()).thenReturn("user1")
        whenever(leaderboardRepository.observeRecords()).thenReturn(flowOf(emptyList()))

        createViewModel(score = 100)
        advanceUntilIdle()

        verify(leaderboardRepository).addRecord(any())
    }

    @Test
    fun `addRecord is not called when score does not beat remote best`() = runTest {
        whenever(authRepository.getUserId()).thenReturn("user1")
        val existing = LeaderboardRecord(
            userId = "user1",
            score = 200,
            gameMode = GameMode.FLOW,
            difficulty = GameDifficulty.NORMAL
        )
        whenever(leaderboardRepository.observeRecords()).thenReturn(flowOf(listOf(existing)))

        createViewModel(score = 100)
        advanceUntilIdle()

        verify(leaderboardRepository, never()).addRecord(any())
    }

    @Test
    fun `isProcessing is false after successful score submission`() = runTest {
        whenever(authRepository.getUserId()).thenReturn("user1")
        whenever(leaderboardRepository.observeRecords()).thenReturn(flowOf(emptyList()))

        val viewModel = createViewModel(score = 100)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isProcessing)
    }
}
