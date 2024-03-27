package com.maxot.seekandcatch.feature.gameplay.usecase

import com.maxot.seekandcatch.feature.gameplay.data.Figure
import com.maxot.seekandcatch.feature.gameplay.data.Goal
import com.maxot.seekandcatch.feature.gameplay.data.repository.FiguresRepository
import com.maxot.seekandcatch.feature.gameplay.data.repository.GoalsRepository
import com.maxot.seekandcatch.feature.gameplay.getRandomNumber
import com.maxot.seekandcatch.feature.score.data.repository.ScoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

enum class GameState {
    STARTED, RESUMED, PAUSED, FINISHED
}

class FlowGameUseCase
@Inject constructor(
    private val scoreRepository: ScoreRepository,
    private val figuresRepository: FiguresRepository,
    private val goalsRepository: GoalsRepository
) {
    private var _goals =
        MutableStateFlow(goalsRepository.getRandomGoals(Int.getRandomNumber()))
    val goals: StateFlow<Set<Goal<Any>>> = _goals

    private var _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private var _figures = MutableStateFlow(mapOf<Int, Figure>())
    val figures: StateFlow<Map<Int, Figure>> = _figures

    private var _coefficient = MutableStateFlow(1)
    val coefficient: StateFlow<Int> = _coefficient

    private var _coefficientProgress = MutableStateFlow(0f)
    val coefficientProgress: StateFlow<Float> = _coefficientProgress

    private val _gameState =
        MutableStateFlow(GameState.STARTED)
    val gameState: StateFlow<GameState>
        get() = _gameState

    fun startGame() {
        _goals.value = goalsRepository.getRandomGoals(Int.getRandomNumber())
        _figures.value = figuresRepository.getRandomFigures(5000).associateBy { figure ->
            figure.id
        }
        _gameState.value = GameState.RESUMED
    }

    fun pauseGame() {
        _gameState.value = GameState.PAUSED
    }

    fun resumeGame() {
        _gameState.value = GameState.RESUMED
    }

    fun onItemClick(id: Int) {
        figures.value[id].let { figure ->
            if (isItemFitForGoals(goals.value, figure!!)) {
                increaseScore()
                increaseCoefficients()
                figure.isActive = false
            } else {
                _gameState.value = GameState.FINISHED
                scoreRepository.setScore(score = score.value)
            }
        }
    }

    fun onItemsMissed(start: Int, end: Int) {
        val missedItemsCount = getMissedItemsCount(start, end)
        if (missedItemsCount > 0) for (i in 1..missedItemsCount) {
            decreaseCoefficients()
        }
    }

    private fun increaseCoefficients() {
        _coefficientProgress.value += 0.2f
        if (coefficientProgress.value >= 1f) {
            _coefficientProgress.value = 0f
            _coefficient.value += 1
        }
    }

    private fun decreaseCoefficients() {
        if (coefficientProgress.value > 0)
            _coefficientProgress.value -= 0.2f

        if (coefficientProgress.value <= 0f && coefficient.value > 1) {
            _coefficientProgress.value = 1f
            _coefficient.value -= 1
        }
    }

    private fun increaseScore() {
        _score.value +=
            if (coefficient.value > 1) 10 * coefficient.value else 10
    }

    private fun getMissedItemsCount(start: Int, end: Int): Int {
        var count = 0
        for (i in start..end) {
            if (isItemFitForGoals(
                    goals.value,
                    figures.value[i]!!
                ) && figures.value[i]!!.isActive
            ) count++
        }
        return count
    }


    /**
     * Check if the item is fit for the goals.
     */
    private fun isItemFitForGoals(goals: Set<Goal<Any>>, item: Figure): Boolean {
        var condition = false
        run goal@{
            goals.forEach { goal ->
                condition = when (goal) {
                    is Goal.Colored -> {
                        goal.getGoal() == item.color
                    }

                    is Goal.Figured -> {
                        if (goal.getGoal().color == null) {
                            goal.getGoal().type == item.type
                        } else {
                            goal.getGoal() == item
                        }
                    }
                }
                if (condition) return@goal
            }
        }
        return condition
    }

    fun getAnimationDuration(): Int {
        return figures.value.size * 50 / coefficient.value * 10
    }
}