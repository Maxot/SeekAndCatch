package com.maxot.seekandcatch.core.domain.engine

import com.maxot.seekandcatch.core.common.model.GameParams
import com.maxot.seekandcatch.data.repository.FiguresRepository
import com.maxot.seekandcatch.data.repository.GoalsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class FlowGameEngine(
    coroutineScope: CoroutineScope,
    figuresRepository: FiguresRepository,
    goalsRepository: GoalsRepository
) : BaseGameEngine(coroutineScope, figuresRepository, goalsRepository) {

    private var firstVisibleItemIndex = 0
    private var itemHeightPx = 100
    private var gameJob: Job? = null
    private var addItemsJob: Job? = null

    override fun initGame(gameParams: GameParams) {
        super.initGame(gameParams)
        firstVisibleItemIndex = 0
        itemsPassedWithoutMissing = 0
        _gameData.update { it.copy(isReverseScrolling = Random.nextBoolean()) }
    }

    override fun setFirstVisibleItemIndex(index: Int) {
        val oldIndex = firstVisibleItemIndex
        firstVisibleItemIndex = index
        
        // Process passed items
        if (gameParams != null) {
            val rowWidth = _gameData.value.rowWidth.coerceAtLeast(1)
            processPassedItems(firstPassedItemIndex = index - rowWidth * 2)
        }

        // Add more items if end soon
        val currentFigures = _gameData.value.figures
        if (index > currentFigures.size - 100) {
            addMoreItems()
        }
        
        updateScrollDuration()
        updatePixelsToScroll()
    }

    override fun setItemHeight(height: Int) {
        itemHeightPx = height
        updateScrollDuration()
        updatePixelsToScroll()
    }

    override fun startGame() {
        super.startGame()
        updateScrollDuration()
        updatePixelsToScroll()
    }

    override fun resumeGame() {
        super.resumeGame()
        updateScrollDuration()
        updatePixelsToScroll()
    }

    private fun processPassedItems(firstPassedItemIndex: Int) {
        if (firstPassedItemIndex < 0) return
        
        val currentData = _gameData.value
        val rowWidth = currentData.rowWidth.coerceAtLeast(1)
        val figures = currentData.figures
        
        val startIndex = firstPassedItemIndex
        val endIndex = firstPassedItemIndex + rowWidth - 1
        
        if (endIndex < figures.size) {
            val missedItemsCount = getMissedItemsCount(startIndex, endIndex)
            repeat(missedItemsCount) {
                if (_gameData.value.coefficient > 1f) {
                    decreaseCoefficient()
                } else {
                    decreaseLifeCount()
                }
            }
        }
    }

    private fun getMissedItemsCount(startIndex: Int, endIndex: Int): Int {
        val data = _gameData.value
        val figures = data.figures
        var count = 0
        if (startIndex < 0 || endIndex >= figures.size) return 0
        for (i in startIndex..endIndex) {
            val item = figures[i]
            if (isItemFitForGoals(data.goals, item) && item.isActive) {
                count++
            }
        }
        return count
    }

    private fun addMoreItems() {
        val data = _gameData.value
        if (data.goals.isEmpty()) return
        
        addItemsJob?.cancel()
        addItemsJob = coroutineScope.launch {
            val newList = figuresRepository.getRandomFigures(
                itemsCount = gameParams?.itemsCount ?: 1000,
                startId = data.figures.size,
                percentageOfSuitableGoalItems = gameParams?.percentOfSuitableItem ?: 0.5f,
                goal = data.goals.first()
            )
            _gameData.update { it.copy(figures = it.figures + newList) }
        }
    }

    private fun updateScrollDuration() {
        _gameData.update { it.copy(scrollDuration = calculateScrollDuration(it)) }
    }

    private fun calculateScrollDuration(data: GameEngineData): Int {
        val rowDuration = gameParams?.rowDuration ?: 1000
        val rowWidth = data.rowWidth.coerceAtLeast(1)
        val rowCount = data.figures.size / rowWidth
        
        val coefPercentage = (data.coefficient * data.coefficient / 100f)
        val timePercentage = (((data.gameDuration / 1000 / 30) * 5) / 100f)
        val actualDurationPercentage = (1f - coefPercentage - timePercentage).coerceAtLeast(0.35f)
        
        return (rowCount * rowDuration * actualDurationPercentage).toInt()
    }

    private fun updatePixelsToScroll() {
        _gameData.update { it.copy(pixelsToScroll = calculatePixelsToScroll(it)) }
    }

    private fun calculatePixelsToScroll(data: GameEngineData): Float {
        val rowWidth = data.rowWidth.coerceAtLeast(1)
        val rowCount = data.figures.size / rowWidth
        return rowCount * itemHeightPx.toFloat()
    }
    
    override fun reset() {
        super.reset()
        gameJob?.cancel()
        gameJob = null
        addItemsJob?.cancel()
        addItemsJob = null
        firstVisibleItemIndex = 0
    }
}
