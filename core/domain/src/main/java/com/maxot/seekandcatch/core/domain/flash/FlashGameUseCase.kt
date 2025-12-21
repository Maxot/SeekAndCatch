package com.maxot.seekandcatch.core.domain.flash

import com.maxot.seekandcatch.core.common.di.ApplicationScope
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.GameParams
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.model.isFitForGoal
import com.maxot.seekandcatch.data.repository.FiguresRepository
import com.maxot.seekandcatch.data.repository.GoalsRepository
import com.maxot.seekandcatch.data.repository.ScoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

/**
 * Simple implementation of Flash game logic: a grid of cells where figures randomly appear and disappear.
 */
class FlashGameUseCase @Inject constructor(
    @ApplicationScope private val coroutineScope: CoroutineScope,
    private val scoreRepository: ScoreRepository,
    private val figuresRepository: FiguresRepository,
    private val goalsRepository: GoalsRepository,
) {

    private val gameData = MutableStateFlow(FlashGameData())

    private val _gameState = MutableStateFlow<FlashGameState>(FlashGameState.Idle)
    val gameState: StateFlow<FlashGameState> get() = _gameState

    private var gameLoopJob: Job? = null
    private var timeJob: Job? = null

    // How many cells are visible simultaneously; derived from difficulty
    private var visibleAtOnce: Int = 2

    fun initGame(gameParams: GameParams) {
        // Reuse GameParams fields where meaningful: itemsCount -> gridSize upper bound, lifeCount, etc.
        coroutineScope.launch {
            val goal = goalsRepository.getRandomGoal()
            val goals = setOf(goal)
            val suitable = figuresRepository.getFigureSuitableForGoal(goal)
            val allFigures: List<Figure> = figuresRepository.getRandomFigures(
                itemsCount = 32,
                percentageOfSuitableGoalItems = 0.5f,
                goal = goal
            )
            val grid = 16 // 4x4 minimal grid for now
            val initialMap = (0 until grid).associateWith { idx ->
                allFigures[idx % allFigures.size]
            }

            // derive how many items should be visible at once from difficulty params
            visibleAtOnce = maxOf(2, gameParams.rowWidth - 1)

            gameData.value = FlashGameData(
                goals = goals,
                gridSize = grid,
                visibleCells = emptySet(),
                figuresByCell = initialMap,
                goalSuitableFigures = suitable,
                maxLifeCount = gameParams.maxLifeCount.coerceAtLeast(1),
                lifeCount = gameParams.lifeCount,
                score = 0,
                gameDuration = 0L,
                flashMillis = 1200L,
                spawnPeriodMillis = 900L
            )
            _gameState.value = FlashGameState.Created(suitable)
        }
    }

    fun onEvent(event: FlashGameEvent) {
        when (event) {
            is FlashGameEvent.OnCellClick -> onCellClick(event.cellId)
            FlashGameEvent.FinishGame -> finishGame()
            FlashGameEvent.PauseGame -> pauseGame()
            FlashGameEvent.ResumeGame -> resumeGame()
            FlashGameEvent.StartGame -> startGame()
            is FlashGameEvent.Tick -> { /* optional external tick not used now */ }
        }
    }

    private fun startGame() {
        if (_gameState.value is FlashGameState.Resumed) return
        _gameState.value = FlashGameState.Started
        resumeGame()
    }

    private fun pauseGame() {
        _gameState.value = FlashGameState.Paused
        stopLoops()
    }

    private fun resumeGame() {
        _gameState.value = FlashGameState.Resumed(gameData.value)
        startLoops()
    }

    private fun finishGame() {
        stopLoops()
        val score = gameData.value.score
        scoreRepository.setLastScore(score)
        _gameState.value = FlashGameState.Finished(score)
    }

    private fun startLoops() {
        gameLoopJob?.cancel()
        gameLoopJob = coroutineScope.launch {
            while (_gameState.value is FlashGameState.Resumed) {
                // spawn a few random visible cells based on difficulty
                val current = gameData.value
                val targetCount = visibleAtOnce.coerceIn(1, current.gridSize)
                val newlyVisible = mutableSetOf<Int>()
                while (newlyVisible.size < targetCount) {
                    newlyVisible.add(Random.Default.nextInt(0, current.gridSize))
                }
                gameData.update { data ->
                    data.copy(visibleCells = newlyVisible)
                }
                _gameState.update { state ->
                    if (state is FlashGameState.Resumed) state.copy(gameData.value) else state
                }
                // hide after flash duration
                delay(current.flashMillis)
                gameData.update { data -> data.copy(visibleCells = emptySet()) }
                _gameState.update { state ->
                    if (state is FlashGameState.Resumed) state.copy(gameData.value) else state
                }
                // wait until next spawn
                delay(current.spawnPeriodMillis)
                // increment duration
                gameData.update { data -> data.copy(gameDuration = data.gameDuration + current.flashMillis + current.spawnPeriodMillis) }
            }
        }
    }

    private fun stopLoops() {
        gameLoopJob?.cancel()
        timeJob?.cancel()
    }

    private fun onCellClick(cellId: Int) {
        val current = gameData.value
        if (cellId !in current.visibleCells) return // clicking hidden cell does nothing
        val figure: Figure? = current.figuresByCell[cellId]
        val isFit = figure?.let { isItemFitForGoals(current.goals, it) } ?: false
        if (isFit) {
            val newScore = current.score + 10
            gameData.update { it.copy(score = newScore) }
        } else {
            val newLife = (current.lifeCount - 1)
            if (newLife <= 0) {
                gameData.update { it.copy(lifeCount = 0) }
                finishGame()
                return
            } else {
                gameData.update { it.copy(lifeCount = newLife) }
            }
        }
        // reflect changes into state
        _gameState.update { state -> if (state is FlashGameState.Resumed) state.copy(gameData.value) else state }
    }

    private fun isItemFitForGoals(goals: Set<Goal<Any>>, item: Figure): Boolean =
        goals.any { goal -> item.isFitForGoal(goal) }
}
