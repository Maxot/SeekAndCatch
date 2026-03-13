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
        super.initGame(gameParams)
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

            val initialData = GameEngineData(
                goals = goals,
                figures = allFigures,
                goalSuitableFigures = suitable,
                maxLifeCount = gameParams.maxLifeCount.coerceAtLeast(1),
                lifeCount = gameParams.lifeCount,
                score = 0,
                coefficient = 1f,
                rowWidth = gridWidth,
                flashMillis = gameParams.rowDuration.toLong().coerceAtLeast(1000L),
                spawnPeriodMillis = (gameParams.rowDuration * 0.75f).toLong().coerceAtLeast(200L)
            )
            _gameData.value = initialData
            _gameState.value = GameEngineState.Created(initialData.goalSuitableFigures)
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

                delay(current.spawnPeriodMillis)
            }
        }
    }

    private fun handleMissedItems(visibleIndices: Set<Int>) {
        val current = _gameData.value
        var missedCount = 0
        visibleIndices.forEach { index ->
            val figure = current.figures.getOrNull(index)
            if (figure != null && isItemFitForGoals(current.goals, figure) && !clickedSuitableCells.contains(index)) {
                missedCount++
            }
        }

        repeat(missedCount) {
            itemsPassedWithoutMissing = 0
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
        // In Flash mode, itemId is actually the index in the grid/figures list
        val index = itemId 
        if (index !in currentData.visibleCells) return

        val figure = currentData.figures.getOrNull(index) ?: return
        
        if (isItemFitForGoals(currentData.goals, figure)) {
            clickedSuitableCells.add(index)
            handleCorrectTap(figure, index)
        } else {
            itemsPassedWithoutMissing = 0
            decreaseLifeCount()
        }
    }

    private fun handleCorrectTap(figure: Figure, index: Int) {
        itemsPassedWithoutMissing++

        val pointsAdded = calculatePoints()

        _gameData.update { current ->
            val updatedFigures = current.figures.toMutableList()
            updatedFigures[index] = figure.copy(pointsReceived = pointsAdded)
            val newCoefficient = current.coefficient + (gameParams?.coefficientStep ?: 0f)
            current.copy(
                figures = updatedFigures,
                score = current.score + pointsAdded,
                coefficient = newCoefficient
            )
        }

        gameParams?.let { params ->
            if (itemsPassedWithoutMissing >= params.itemsPassedWithoutMissToGetLife) {
                increaseLifeCount()
                itemsPassedWithoutMissing = 0
            }
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
}
