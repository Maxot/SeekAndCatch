package com.maxot.seekandcatch.core.domain

import androidx.compose.ui.graphics.Color
import com.maxot.seekandcatch.core.common.model.GameParams
import com.maxot.seekandcatch.core.domain.flow.FlowGameEvent
import com.maxot.seekandcatch.core.domain.flow.FlowGameData
import com.maxot.seekandcatch.core.domain.flow.FlowGameState
import com.maxot.seekandcatch.core.domain.flow.FlowGameUseCase
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.test.repository.FakeFiguresRepository
import com.maxot.seekandcatch.data.test.repository.FakeGoalsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.maxot.seekandcatch.core.domain.engine.FlowGameEngine
import com.maxot.seekandcatch.core.domain.engine.GameEngineData
import com.maxot.seekandcatch.core.domain.engine.GameEngineState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FlowGameUseCaseTest {

    private val flowGameEngine = mock<FlowGameEngine>()
    private val testScope = TestScope()
    private val useCase = FlowGameUseCase(testScope, flowGameEngine)
    
    private val gameParam = GameParams(
        itemsCount = 10,
        percentOfSuitableItem = 0.5f,
        coefficientStep = 0.25f,
        scorePoint = 10,
        maxLifeCount = 5,
        lifeCount = 3,
        itemsPassedWithoutMissToGetLife = 3
    )

    @Before
    fun setup() {
        whenever(flowGameEngine.gameState).thenReturn(MutableStateFlow(GameEngineState.Idle))
        whenever(flowGameEngine.gameData).thenReturn(MutableStateFlow(GameEngineData()))
    }

    @Test
    fun startGame_callsEngineStart() {
        useCase.onEvent(FlowGameEvent.StartGame)
        verify(flowGameEngine).startGame()
    }

    @Test
    fun finishGame_callsEngineFinish() {
        useCase.onEvent(FlowGameEvent.FinishGame)
        verify(flowGameEngine).finishGame()
    }

    @Test
    fun onItemClick_callsEngineItemClick() {
        useCase.onEvent(FlowGameEvent.OnItemClick(5))
        verify(flowGameEngine).onItemClick(5)
    }
    
    @Test
    fun initGame_callsEngineInit() {
        useCase.initGame(gameParam)
        verify(flowGameEngine).initGame(gameParam)
    }
}
