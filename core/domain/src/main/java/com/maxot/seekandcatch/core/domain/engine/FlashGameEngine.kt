package com.maxot.seekandcatch.core.domain.engine

import com.maxot.seekandcatch.core.common.model.GameParams
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.repository.FiguresRepository
import com.maxot.seekandcatch.data.repository.GoalsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class FlashGameEngine(
    coroutineScope: CoroutineScope,
    figuresRepository: FiguresRepository,
    goalsRepository: GoalsRepository
) : BaseGameEngine(coroutineScope, figuresRepository, goalsRepository) {

    private var flashJob: Job? = null
    private var visibleAtOnce: Int = 2
    private val clickedSuitableCells = mutableSetOf<Int>()

    override fun initGame(gameParams: GameParams) {
        this.gameParams = gameParams
        itemsPassedWithoutMissing = 0
        stopTimeTracking()
        clickedSuitableCells.clear()
        _gameState.value = GameEngineState.Idle
        
        coroutineScope.launch {
            val goal = goalsRepository.getRandomGoal()
            val goals = setOf(goal)
            val suitable = figuresRepository.getFigureSuitableForGoal(goal)
            
            val gridWidth = gameParams.rowWidth.coerceAtLeast(1)
            val gridCount = gridWidth * gridWidth

            val allFigures = figuresRepository.getRandomFigures(
                itemsCount = maxOf(32, gridCount),
                percentageOfSuitableGoalItems = gameParams.percentOfSuitableItem,
                goal = goal
            ).take(gridCount) // We only need gridCount figures for the initial set

            visibleAtOnce = maxOf(1, gridWidth - 1)

            val initialCoefficient = 1f
            val baseFlashMillis = gameParams.rowDuration * 2L
            val baseSpawnPeriodMillis = (gameParams.rowDuration * 1.5f).toLong()

            val initialData = GameEngineData(
                goals = goals,
                figures = allFigures,
                goalSuitableFigures = suitable,
                maxLifeCount = gameParams.maxLifeCount.coerceAtLeast(1),
                lifeCount = gameParams.lifeCount,
                score = 0,
                coefficient = initialCoefficient,
                rowWidth = gridWidth,
            )
            val dataWithDurations = initialData.copy(
                flashMillis = calculateFlashDuration(baseFlashMillis, initialData),
                spawnPeriodMillis = calculateSpawnDuration(baseSpawnPeriodMillis, initialData)
            )
            _gameData.value = dataWithDurations
            _gameState.value = GameEngineState.Created(dataWithDurations.goalSuitableFigures)
        }
    }

    override fun startGame() {
        super.startGame()
        startFlashLoop()
    }

    override fun pauseGame() {
        super.pauseGame()
        stopFlashLoop()
    }

    override fun resumeGame() {
        super.resumeGame()
        if (_gameState.value is GameEngineState.Started) {
            startFlashLoop()
        }
    }

    override fun finishGame() {
        super.finishGame()
        stopFlashLoop()
    }

    private fun startFlashLoop() {
        flashJob?.cancel()
        flashJob = coroutineScope.launch {
            while (_gameState.value is GameEngineState.Started) {
                val current = _gameData.value
                val gridCount = current.figures.size
                if (gridCount == 0) {
                    delay(100)
                    continue
                }

                delay(current.spawnPeriodMillis)
                if (_gameState.value !is GameEngineState.Started) break

                val targetCount = visibleAtOnce.coerceIn(1, gridCount)
                val newlyVisible = mutableSetOf<Int>()
                while (newlyVisible.size < targetCount) {
                    newlyVisible.add(Random.nextInt(0, gridCount))
                }

                _gameData.update { it.copy(visibleCells = newlyVisible) }

                delay(current.flashMillis)

                // Process missed items before hiding
                handleMissedItems(newlyVisible)

                clickedSuitableCells.clear()
                _gameData.update { it.copy(visibleCells = emptySet()) }
            }
        }
    }

    private fun handleMissedItems(visibleIndices: Set<Int>) {
        val current = _gameData.value
        val figures = current.figures
        var missedCount = 0
        visibleIndices.forEach { index ->
            val figure = figures.getOrNull(index)
            if (figure != null && isItemFitForGoals(current.goals, figure) && !clickedSuitableCells.contains(index)) {
                missedCount++
            }
        }

        repeat(missedCount) {
            if (_gameData.value.coefficient > 1f) {
                decreaseCoefficient()
            } else {
                decreaseLifeCount()
            }
        }
    }

    private fun stopFlashLoop() {
        flashJob?.cancel()
        flashJob = null
    }

    override fun onItemClick(itemId: Int) {
        if (_gameState.value !is GameEngineState.Started) return

        val currentData = _gameData.value
        val figures = currentData.figures
        // In Flash mode, itemId is actually the index in the grid/figures list
        val index = itemId 
        if (index !in currentData.visibleCells) return

        val figure = figures.getOrNull(index) ?: return
        
        if (isItemFitForGoals(currentData.goals, figure)) {
            clickedSuitableCells.add(index)
            handleCorrectTap(figure, index)
        } else {
            handleWrongTap()
        }
    }

    private fun handleCorrectTap(figure: Figure, index: Int) {
        itemsPassedWithoutMissing++

        val pointsAdded = calculatePoints()
        val params = gameParams ?: return

        _gameData.update { current ->
            val updatedFigures = current.figures.toMutableList()
            updatedFigures[index] = figure.copy(pointsReceived = pointsAdded)
            
            val newCoefficient = current.coefficient + (params.coefficientStep ?: 0f)
            
            val baseFlashMillis = params.rowDuration * 2L
            val baseSpawnPeriodMillis = (params.rowDuration * 1.5f).toLong()

            val updated = current.copy(
                figures = updatedFigures,
                score = current.score + pointsAdded,
                coefficient = newCoefficient,
            )
            updated.copy(
                flashMillis = calculateFlashDuration(baseFlashMillis, updated),
                spawnPeriodMillis = calculateSpawnDuration(baseSpawnPeriodMillis, updated)
            )
        }

        if (itemsPassedWithoutMissing >= params.itemsPassedWithoutMissToGetLife) {
            increaseLifeCount()
            itemsPassedWithoutMissing = 0
        }

        // Clear points after delay
        coroutineScope.launch {
            delay(1000)
            _gameData.update { current ->
                val f = current.figures.getOrNull(index)
                if (f?.pointsReceived != null) {
                    val updatedFigures = current.figures.toMutableList()
                    updatedFigures[index] = f.copy(pointsReceived = null)
                    current.copy(figures = updatedFigures)
                } else current
            }
        }
    }
    override fun decreaseCoefficient() {
        val params = gameParams ?: return
        _gameData.update { current ->
            val newCoefficient = (current.coefficient / 2f).coerceAtLeast(1f)
            
            val baseFlashMillis = params.rowDuration * 2L
            val baseSpawnPeriodMillis = (params.rowDuration * 1.5f).toLong()

            val updated = current.copy(
                coefficient = newCoefficient,
            )
            updated.copy(
                flashMillis = calculateFlashDuration(baseFlashMillis, updated),
                spawnPeriodMillis = calculateSpawnDuration(baseSpawnPeriodMillis, updated)
            )
        }
        itemsPassedWithoutMissing = 0
    }

    private fun updateDurations() {
        val params = gameParams ?: return
        _gameData.update { current ->
            val baseFlashMillis = params.rowDuration * 2L
            val baseSpawnPeriodMillis = (params.rowDuration * 1.5f).toLong()
            current.copy(
                flashMillis = calculateFlashDuration(baseFlashMillis, current),
                spawnPeriodMillis = calculateSpawnDuration(baseSpawnPeriodMillis, current)
            )
        }
    }

    override fun onTimeTick() {
        super.onTimeTick()
        updateDurations()
    }

    private fun calculateFlashDuration(base: Long, data: GameEngineData): Long {
        return (base * calculateDurationPercentage(data)).toLong().coerceAtLeast(MIN_FLASH_MILLIS)
    }

    private fun calculateSpawnDuration(base: Long, data: GameEngineData): Long {
        return (base * calculateDurationPercentage(data)).toLong().coerceAtLeast(MIN_SPAWN_PERIOD_MILLIS)
    }

    private fun calculateDurationPercentage(data: GameEngineData): Float {
        val coefPercentage = (data.coefficient * data.coefficient / 100f)
        val timePercentage = (((data.gameDuration / 1000 / 30) * 5) / 100f)
        return (1f - coefPercentage - timePercentage).coerceAtLeast(0.35f)
    }

    companion object {
        private const val MIN_FLASH_MILLIS = 300L
        private const val MIN_SPAWN_PERIOD_MILLIS = 300L
    }
}
