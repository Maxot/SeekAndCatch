package com.maxot.seekandcatch.core.domain.flash

import com.maxot.seekandcatch.core.common.di.ApplicationScope
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.GameParams
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.model.isFitForGoal
import com.maxot.seekandcatch.data.repository.FiguresRepository
import com.maxot.seekandcatch.data.repository.GoalsRepository
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

    // Scoring params from GameParams (same semantics as Flow mode)
    private var scorePoint: Int = 10
    private var coefficientStep: Float = 0.25f

    // Track clicked suitable cells within current flash window to avoid counting them as missed
    private val clickedSuitableCells = mutableSetOf<Int>()

    fun initGame(gameParams: GameParams) {
        // Reuse GameParams fields where meaningful: itemsCount -> gridSize upper bound, lifeCount, etc.
        coroutineScope.launch {
            scorePoint = gameParams.scorePoint
            coefficientStep = gameParams.coefficientStep
            val goal = goalsRepository.getRandomGoal()
            val goals = setOf(goal)
            val suitable = figuresRepository.getFigureSuitableForGoal(goal)
            // Grid size and figures pool depend on difficulty via GameParams.rowWidth
            val gridWidth = gameParams.rowWidth.coerceAtLeast(1)
            val grid = gridWidth * gridWidth

            val allFigures: List<Figure> = figuresRepository.getRandomFigures(
                itemsCount = maxOf(32, grid),
                percentageOfSuitableGoalItems = gameParams.percentOfSuitableItem,
                goal = goal
            )
            val initialMap = (0 until grid).associateWith { idx ->
                allFigures[idx % allFigures.size]
            }

            // derive how many items should be visible at once from difficulty params
            visibleAtOnce = maxOf(1, gridWidth - 1)

            gameData.value = FlashGameData(
                goals = goals,
                gridSize = grid,
                visibleCells = emptySet(),
                figuresByCell = initialMap,
                goalSuitableFigures = suitable,
                maxLifeCount = gameParams.maxLifeCount.coerceAtLeast(1),
                lifeCount = gameParams.lifeCount,
                score = 0,
                coefficient = 1f,
                gameDuration = 0L,
                // Use rowDuration to influence flash timing; keep spawn slightly shorter
                flashMillis = gameParams.rowDuration.toLong().coerceAtLeast(1000L),
                spawnPeriodMillis = (gameParams.rowDuration * 0.75f).toLong().coerceAtLeast(200L)
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
        _gameState.value = FlashGameState.Finished(gameData.value.score)
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
                // Before hiding, process skipped suitable cells to adjust coefficient/lives
                val beforeHideVisible = gameData.value.visibleCells
                val goals = gameData.value.goals
                val figuresByCell = gameData.value.figuresByCell
                // Count suitable cells that were visible and not clicked
                var missedSuitable = 0
                beforeHideVisible.forEach { cellId ->
                    val fig = figuresByCell[cellId]
                    if (fig != null && isItemFitForGoals(goals, fig) && !clickedSuitableCells.contains(cellId)) {
                        missedSuitable++
                    }
                }
                repeat(missedSuitable) {
                    if (gameData.value.coefficient > 1f) {
                        decreaseCoefficients()
                    } else {
                        // decrease life; finish if no lives left
                        val newLife = gameData.value.lifeCount - 1
                        if (newLife < 0) {
                            gameData.update { it.copy(lifeCount = 0) }
                            _gameState.update { state -> if (state is FlashGameState.Resumed) state.copy(gameData.value) else state }
                            finishGame()
                            return@launch
                        } else {
                            gameData.update { it.copy(lifeCount = newLife) }
                        }
                    }
                }

                // Clear for next window
                clickedSuitableCells.clear()

                gameData.update { data -> data.copy(visibleCells = emptySet()) }
                _gameState.update { state ->
                    if (state is FlashGameState.Resumed) state.copy(gameData.value) else state
                }
                // wait until next spawn
                delay(current.spawnPeriodMillis)
                // increment duration
                gameData.update { data -> data.copy(gameDuration = data.gameDuration + current.flashMillis + current.spawnPeriodMillis) }
                _gameState.update { state -> if (state is FlashGameState.Resumed) state.copy(gameData.value) else state }
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
            // Track so it won't be considered as missed when window hides
            clickedSuitableCells.add(cellId)
            val pointsAdded = increaseScore()
            increaseCoefficients()
            // Add transient points to the clicked figure so UI can show "+points"
            figure?.let { base ->
                val updated = base.copy(pointsReceived = pointsAdded)
                gameData.update { d ->
                    val newMap = d.figuresByCell.toMutableMap()
                    newMap[cellId] = updated
                    d.copy(figuresByCell = newMap)
                }
                // Clear points after a short delay to avoid persisting label across flashes
                coroutineScope.launch {
                    delay(1000)
                    gameData.update { d ->
                        val f = d.figuresByCell[cellId]
                        if (f?.pointsReceived != null) {
                            val newMap = d.figuresByCell.toMutableMap()
                            newMap[cellId] = f.copy(pointsReceived = null)
                            d.copy(figuresByCell = newMap)
                        } else d
                    }
                    _gameState.update { state -> if (state is FlashGameState.Resumed) state.copy(gameData.value) else state }
                }
            }
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

    private fun increaseCoefficients() {
        val coefficient = gameData.value.coefficient + coefficientStep
        gameData.update { it.copy(coefficient = coefficient) }
    }

    private fun decreaseCoefficients() {
        gameData.update { currentData ->
            val newCoefficient =
                if (currentData.coefficient > 1f) (currentData.coefficient / 2f).coerceAtLeast(1f) else
                    currentData.coefficient
            currentData.copy(coefficient = newCoefficient.coerceAtLeast(1f))
        }
    }

    private fun increaseScore(): Int {
        val pointsAdded = gameData.value.coefficient.toInt() * scorePoint
        gameData.update { currentData ->
            val newScore = currentData.score + pointsAdded
            currentData.copy(score = newScore)
        }
        return pointsAdded
    }
}
