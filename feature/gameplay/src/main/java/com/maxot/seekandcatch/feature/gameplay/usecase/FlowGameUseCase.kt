package com.maxot.seekandcatch.feature.gameplay.usecase

import com.maxot.seekandcatch.feature.gameplay.data.Figure
import com.maxot.seekandcatch.feature.gameplay.data.GameParams
import com.maxot.seekandcatch.feature.gameplay.data.Goal
import com.maxot.seekandcatch.feature.gameplay.data.repository.FiguresRepository
import com.maxot.seekandcatch.feature.gameplay.data.repository.GoalsRepository
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
        MutableStateFlow(emptySet<Goal<Any>>())
    val goals: StateFlow<Set<Goal<Any>>> = _goals

    private var _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private var _figures = MutableStateFlow(listOf<Figure>())
    val figures: StateFlow<List<Figure>> = _figures


    private var _coefficient = MutableStateFlow(1f)
    val coefficient: StateFlow<Float> = _coefficient

    private val _gameState =
        MutableStateFlow(GameState.STARTED)
    val gameState: StateFlow<GameState>
        get() = _gameState

    fun startGame(gameParams: GameParams) {
        _goals.value = setOf(goalsRepository.getRandomGoal())
        _figures.value = figuresRepository.getRandomFigures(
            itemsCount = gameParams.itemsCount,
            percentOfGoalSuitedItems = gameParams.percentOfCorrectItem,
            goal = _goals.value.first()
        )
        _gameState.value = GameState.RESUMED
    }

    fun pauseGame() {
        _gameState.value = GameState.PAUSED
    }

    fun resumeGame() {
        _gameState.value = GameState.RESUMED
    }

    fun finishGame() {
        scoreRepository.setScore(score = score.value)
        _gameState.value = GameState.FINISHED
    }

    fun onItemClick(id: Int) {
        //TODO: Look for some replacement for 'find' operator. Maybe use HashMap?
        figures.value.find { figure -> figure.id == id }.let { figure ->
            if (isItemFitForGoals(goals.value, figure!!)) {
                increaseScore()
                increaseCoefficients()
                figure.isActive = false
            } else {
                scoreRepository.setScore(score = score.value)
                _gameState.value = GameState.FINISHED
            }
        }
    }

    fun onItemsMissed(start: Int, end: Int) {
        val missedItemsCount = getMissedItemsCount(start, end)
        repeat(missedItemsCount) {
            decreaseCoefficients()
        }
    }

    private fun increaseCoefficients() {
        _coefficient.value += COEFFICIENT_STEP
    }

    private fun decreaseCoefficients() {
        if (_coefficient.value > 1)
            _coefficient.value -= COEFFICIENT_STEP
    }

    private fun increaseScore() {
        _score.value += _coefficient.value.toInt() * SCORE_POINT
    }

    private fun getMissedItemsCount(startIndex: Int, endIndex: Int): Int {
        var count = 0
        for (i in startIndex..endIndex) {
            val item = figures.value[i]
            if (isItemFitForGoals(
                    goals.value,
                    item
                ) && item.isActive
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

                    is Goal.Shaped -> {
                        goal.getGoal() == item.type
                    }
                }
                if (condition) return@goal
            }
        }
        return condition
    }

    fun getAnimationDuration(): Int {
        return figures.value.size * 50 / coefficient.value.toInt() * 10
    }

    companion object {
        const val SCORE_POINT = 10
        const val COEFFICIENT_STEP = 0.2f
    }
}