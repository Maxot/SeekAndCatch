package com.maxot.seekandcatch.feature.gameplay

import androidx.lifecycle.ViewModel
import com.maxot.seekandcatch.feature.gameplay.data.Figure
import com.maxot.seekandcatch.feature.gameplay.data.Goal
import com.maxot.seekandcatch.feature.gameplay.usecase.FlowGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class FlowGameViewModel
@Inject constructor(
    private val gameUseCase: FlowGameUseCase
) : ViewModel() {

    val goals: StateFlow<Set<Goal<Any>>> = gameUseCase.goals
    val score: StateFlow<Int> = gameUseCase.score
    val figures: StateFlow<Map<Int, Figure>> = gameUseCase.figures
    val coefficient: StateFlow<Int> = gameUseCase.coefficient
    val coefficientProgress: StateFlow<Float> = gameUseCase.coefficientProgress
    val gameState = gameUseCase.gameState

    fun startGame() {
        gameUseCase.startGame()
    }

    fun pauseGame() {
        gameUseCase.pauseGame()
    }

    fun resumeGame() {
        gameUseCase.resumeGame()
    }

    fun onItemClick(id: Int) {
        gameUseCase.onItemClick(id)
    }

    fun onItemsMissed(start: Int, end: Int) {
        gameUseCase.onItemsMissed(start, end)
    }

    fun getAnimationDuration(): Int {
        return gameUseCase.getAnimationDuration()
    }
}
