package com.maxot.seekandcatch.core.domain.engine

import com.maxot.seekandcatch.core.common.model.GameParams
import com.maxot.seekandcatch.core.common.model.Figure
import com.maxot.seekandcatch.core.common.model.Goal
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
    private var nextFigureId: Int = 1000

    override fun startInitJob(params: GameParams) {
        initJob = coroutineScope.launch {
            val goal = goalsRepository.getRandomGoal()
            val goals = setOf(goal)
            val suitable = figuresRepository.getFigureSuitableForGoal(goal)
            
            val gridWidth = params.rowWidth.coerceAtLeast(1)
            val gridCount = gridWidth * gridWidth

            val allFigures = figuresRepository.getRandomFigures(
                itemsCount = maxOf(32, gridCount),
                percentageOfSuitableGoalItems = params.percentOfSuitableItem,
                goal = goal
            ).take(gridCount) // We only need gridCount figures for the initial set

            visibleAtOnce = maxOf(1, gridWidth - 1)

            val initialCoefficient = 1f
            val baseFlashMillis = (params.rowDuration * 2L * 1.2f).toLong()
            val baseSpawnPeriodMillis = (params.rowDuration * 1.2f * 1.5f).toLong()

            val initialData = GameEngineData(
                goals = goals,
                figures = allFigures,
                goalSuitableFigures = suitable,
                maxLifeCount = params.maxLifeCount.coerceAtLeast(1),
                lifeCount = params.lifeCount,
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

                // Only pick figures that haven't been correctly clicked yet (isActive)
                val activeIndices = _gameData.value.figures.indices.filter { 
                    _gameData.value.figures[it].isActive 
                }
                
                if (activeIndices.isEmpty()) {
                    delay(100)
                    continue
                }
                
                var suitableIndices = activeIndices.filter { 
                    isItemFitForGoals(_gameData.value.goals, _gameData.value.figures[it])
                }
                
                var forceGeneratedFigure: Figure? = null
                var indexToMakeSuitable: Int = -1
                
                if (suitableIndices.isEmpty()) {
                    // Force generate a suitable item at a random active index
                    indexToMakeSuitable = activeIndices.random()
                    val goal = _gameData.value.goals.random()
                    forceGeneratedFigure = figuresRepository.getRandomFigures(
                        itemsCount = 1,
                        startId = nextFigureId++,
                        percentageOfSuitableGoalItems = 1f,
                        goal = goal
                    ).first().copy(isActive = true)
                    suitableIndices = listOf(indexToMakeSuitable)
                }

                val targetCount = minOf(visibleAtOnce, activeIndices.size)
                val newlyVisible = mutableSetOf<Int>()
                
                // Guaranteed at least one suitable item
                newlyVisible.add(suitableIndices[Random.nextInt(0, suitableIndices.size)])
                
                while (newlyVisible.size < targetCount) {
                    newlyVisible.add(activeIndices[Random.nextInt(0, activeIndices.size)])
                }

                // Atomic update for figures and visible cells
                var figuresAtFlashStart = emptyList<Figure>()
                var goalsAtFlashStart = emptySet<Goal<Any>>()

                _gameData.update { current ->
                    val updatedFigures = if (forceGeneratedFigure != null) {
                        val list = current.figures.toMutableList()
                        list[indexToMakeSuitable] = forceGeneratedFigure
                        list
                    } else {
                        current.figures
                    }
                    figuresAtFlashStart = updatedFigures.toList()
                    goalsAtFlashStart = current.goals
                    current.copy(figures = updatedFigures, visibleCells = newlyVisible)
                }

                delay(_gameData.value.flashMillis)

                var snapshotClicked = emptySet<Int>()
                _gameData.update {
                    snapshotClicked = it.clickedSuitableCells
                    it.copy(visibleCells = emptySet(), clickedSuitableCells = emptySet())
                }
                // Process missed items using the figures that were actually shown
                handleMissedItems(newlyVisible, snapshotClicked, figuresAtFlashStart, goalsAtFlashStart)
            }
        }
    }

    private fun handleMissedItems(
        visibleIndices: Set<Int>,
        clickedIndices: Set<Int>,
        figuresAtFlashStart: List<Figure>,
        goalsAtFlashStart: Set<Goal<Any>>
    ) {
        var missedCount = 0
        visibleIndices.forEach { index ->
            val figure = figuresAtFlashStart.getOrNull(index)
            if (figure != null && figure.isActive && isItemFitForGoals(goalsAtFlashStart, figure) && !clickedIndices.contains(index)) {
                missedCount++
            }
        }

        repeat(missedCount) {
            decreaseCoefficient()
        }
    }

    private fun stopFlashLoop() {
        flashJob?.cancel()
        flashJob = null
    }

    override fun onItemClick(itemId: Int) {
        if (_gameState.value !is GameEngineState.Started) return

        var figureToHandle: Figure? = null
        var indexToHandle: Int = -1
        var isWrongTap = false

        _gameData.update { currentData ->
            val index = itemId
            if (index !in currentData.visibleCells || currentData.clickedSuitableCells.contains(index)) {
                return@update currentData
            }

            val figure = currentData.figures.getOrNull(index) ?: return@update currentData
            if (!figure.isActive) return@update currentData
            
            if (isItemFitForGoals(currentData.goals, figure)) {
                figureToHandle = figure
                indexToHandle = index
                currentData.copy(clickedSuitableCells = currentData.clickedSuitableCells + index)
            } else {
                isWrongTap = true
                currentData
            }
        }

        if (isWrongTap) {
            handleWrongTap()
            return
        }

        figureToHandle?.let {
            handleCorrectTap(it, indexToHandle)
        }
    }

    private fun handleCorrectTap(figure: Figure, index: Int) {
        itemsPassedWithoutMissing++

        val pointsAdded = calculatePoints()
        val params = gameParams ?: return

        _gameData.update { current ->
            val updatedFigures = current.figures.toMutableList()
            
            // Replace with a new figure instead of marking as inactive to keep the game infinite
            val newFigure = figuresRepository.getRandomFigures(
                itemsCount = 1,
                startId = nextFigureId++,
                percentageOfSuitableGoalItems = params.percentOfSuitableItem,
                goal = current.goals.first()
            ).first()
            
            updatedFigures[index] = newFigure.copy(isActive = true, pointsReceived = pointsAdded)
            
            val newCoefficient = current.coefficient + (params.coefficientStep ?: 0f)
            
            val baseFlashMillis = (params.rowDuration * 2L * 1.2f).toLong()
            val baseSpawnPeriodMillis = (params.rowDuration * 1.2f * 1.5f).toLong()

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
        var shouldDecreaseLife = false
        _gameData.update { current ->
            val newCoefficient = (current.coefficient / 2f).coerceAtLeast(1f)
            
            val baseFlashMillis = (params.rowDuration * 2L * 1.2f).toLong()
            val baseSpawnPeriodMillis = (params.rowDuration * 1.2f * 1.5f).toLong()

            val updated = current.copy(
                coefficient = newCoefficient,
                isLifeWasted = true
            )
            val finalData = updated.copy(
                flashMillis = calculateFlashDuration(baseFlashMillis, updated),
                spawnPeriodMillis = calculateSpawnDuration(baseSpawnPeriodMillis, updated)
            )
            if (finalData.coefficient <= 1.0f && current.coefficient <= 1.0f) {
                shouldDecreaseLife = true
            }
            finalData
        }
        if (shouldDecreaseLife) {
            decreaseLifeCount()
        } else {
            coroutineScope.launch {
                delay(500)
                _gameData.update { it.copy(isLifeWasted = false) }
            }
        }
        itemsPassedWithoutMissing = 0
    }

    private fun updateDurations() {
        val params = gameParams ?: return
        _gameData.update { current ->
            val baseFlashMillis = (params.rowDuration * 2L * 1.2f).toLong()
            val baseSpawnPeriodMillis = (params.rowDuration * 1.2f * 1.5f).toLong()
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
        return (base / data.coefficient).toLong().coerceAtLeast(MIN_FLASH_MILLIS)
    }

    private fun calculateSpawnDuration(base: Long, data: GameEngineData): Long {
        return (base / data.coefficient).toLong().coerceAtLeast(MIN_SPAWN_PERIOD_MILLIS)
    }

    override fun reset() {
        super.reset()
        stopFlashLoop()
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
