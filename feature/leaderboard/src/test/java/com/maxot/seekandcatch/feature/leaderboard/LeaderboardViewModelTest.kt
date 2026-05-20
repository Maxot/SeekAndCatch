package com.maxot.seekandcatch.feature.leaderboard

import com.maxot.seekandcatch.core.domain.user.UserUseCase
import com.maxot.seekandcatch.data.repository.LeaderboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
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
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class LeaderboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when repository throws, leaderboardUiState becomes Failed`() = runTest {
        val repository = mock<LeaderboardRepository>()
        whenever(repository.observeRecords()).thenReturn(
            flow { throw RuntimeException("Network error") }
        )
        val userUseCase = mock<UserUseCase>()

        val viewModel = LeaderboardViewModel(userUseCase, repository)

        val states = mutableListOf<LeaderboardUiState>()
        val job = launch {
            viewModel.leaderboardUiState.collect { states.add(it) }
        }

        advanceUntilIdle()

        assertEquals(LeaderboardUiState.Failed, states.last())
        job.cancel()
    }
}
