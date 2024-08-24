package com.maxot.seekandcatch.core.domain

import androidx.annotation.Px
import com.maxot.seekandcatch.core.common.di.ApplicationScope
import com.maxot.seekandcatch.core.domain.model.FlowGameData
import com.maxot.seekandcatch.core.domain.model.FlowGameEvent
import com.maxot.seekandcatch.core.domain.model.FlowGameState
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.GameParams
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.model.isFitForGoal
import com.maxot.seekandcatch.data.repository.FiguresRepository
import com.maxot.seekandcatch.data.repository.GoalsRepository
import com.maxot.seekandcatch.data.repository.ScoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TAG = "FlowGameUseCase"


/**
 * Represent game logic for Flow Game Mode.
 * The game consists of grid of [FlowGameData.figures] with fixed [FlowGameData.rowWidth] and the [FlowGameData.goals].
 * Items are auto-scrolled with some speed(px/ms), the amount of pixel to scroll calculated by [getPixelsToScroll]
 * and the scroll duration calculated by [getScrollDuration]. Then UI pass back [firstVisibleItemIndex]
 * to process game updates via [processGameChanges].
 * During this process user should click on items via [onItemClick] then it check if item if fit for the goals
 * via [isFitForGoal], and if it is fit [onCorrectItemClicked] else [finishGame].
 */
class FlowGameUseCase
@Inject constructor(
    @ApplicationScope private val coroutineScope: CoroutineScope,
    private val scoreRepository: ScoreRepository,
    private val figuresRepository: FiguresRepository,
    private val goalsRepository: GoalsRepository
) {
    @Px
    private var itemHeightPx: Int = 0
    private var rowWidth: Int = 4
    private var rowDuration: Int = 500
    private var itemsCount: Int = 1000
    private var scorePoint: Int = 10
    private var coefficientStep: Float = 0.2f
    private var percentOfSuitableItem: Float = 0.5f
    private var scrollDuration = 1000
    private var maxLifeCount: Int = 5
    private var itemsPassedWithoutMissToGetLife = 10
    private var itemsPassedWithoutMissing = 0


    private var firstVisibleItemIndex = MutableStateFlow(0)

    private val gameData = MutableStateFlow(FlowGameData())

    private val _gameState =
        MutableStateFlow<FlowGameState>(FlowGameState.Idle)
    val gameState: StateFlow<FlowGameState>
        get() = _gameState

    private var gameJob: Job? = null
    private var gameDataJob: Job? = null
    private var timeJob: Job? = null

    fun initGame(gameParams: GameParams) {
        scorePoint = gameParams.scorePoint
        coefficientStep = gameParams.coefficientStep
        itemsCount = gameParams.itemsCount
        percentOfSuitableItem = gameParams.percentOfSuitableItem
        rowWidth = gameParams.rowWidth
        rowDuration = gameParams.rowDuration
        maxLifeCount = gameParams.maxLifeCount
        itemsPassedWithoutMissToGetLife = gameParams.itemsPassedWithoutMissToGetLife

        coroutineScope.launch {
            val goals = setOf(goalsRepository.getRandomGoal())
            val figures = figuresRepository.getRandomFigures(
                itemsCount = itemsCount,
                percentageOfSuitableGoalItems = percentOfSuitableItem,
                goal = goals.first()
            )
            val currentGameData = FlowGameData(
                maxLifeCount = maxLifeCount,
                lifeCount = gameParams.lifeCount,
                goals = goals,
                figures = figures,
                score = 0,
                coefficient = 1f,
                gameDuration = 0,
                scrollDuration = getScrollDuration(),
                pixelsToScroll = getPixelsToScroll(rowWidth.toFloat()),
                rowWidth = rowWidth
            )
            gameData.value = currentGameData
        }

        _gameState.value = FlowGameState.Created
    }


    fun onEvent(event: FlowGameEvent) {
        when (event) {
            is FlowGameEvent.OnItemClick -> onItemClick(event.itemId)
            FlowGameEvent.UpdateScrollDuration -> updateScrollDuration()
            FlowGameEvent.UpdatePixelsToScroll -> updatePixelsToScroll()
            FlowGameEvent.FinishGame -> finishGame()
            FlowGameEvent.PauseGame -> pauseGame()
            FlowGameEvent.ResumeGame -> resumeGame()
            FlowGameEvent.StartGame -> startGame()
            is FlowGameEvent.FirstVisibleItemIndexChanged -> setFirstVisibleItemIndex(event.firstVisibleItemIndex)
            is FlowGameEvent.ItemHeightMeasured -> setItemHeight(event.height)
        }

    }

    private fun observeGameData() {
        gameDataJob?.cancel()
        gameDataJob = CoroutineScope(Dispatchers.IO).launch {
            gameData.collect { data ->
                _gameState.update {
                    if (it is FlowGameState.Resumed) it.copy(data) else it
                }
            }
        }
    }

    private fun setFirstVisibleItemIndex(index: Int) {
        firstVisibleItemIndex.value = index
    }

    private fun startGame() {
//        processGameChanges()

        _gameState.value = FlowGameState.Started
        resumeGame()
    }

    private fun pauseGame() {
        gameJob?.cancel()
        timeJob?.cancel()
        gameDataJob?.cancel()

        _gameState.value = FlowGameState.Paused
    }

    private fun resumeGame() {
        processGameChanges()

        _gameState.value = FlowGameState.Resumed(gameData.value)
        observeGameData()
    }

    private fun finishGame() {
        gameJob?.cancel()
        gameDataJob?.cancel()
        timeJob?.cancel()

//        scoreRepository.setLastScore(score = _score.value)

        _gameState.value = FlowGameState.Finished(gameData.value.score)
    }

    private fun processGameChanges() {
        gameJob?.cancel()
        timeJob?.cancel()

        gameJob = CoroutineScope(Dispatchers.IO).launch {
            firstVisibleItemIndex.collect { firstVisibleItemIndex ->
//                Log.d(TAG, "first item index: $firstVisibleItemIndex")
                processPassedItems(firstPassedItemIndex = firstVisibleItemIndex - rowWidth * 2)

                // Add more items if end soon
                if (firstVisibleItemIndex > itemsCount - 100) {
                    addMoreItems()
                }
            }
        }

        timeJob = CoroutineScope(Dispatchers.IO).launch {
            measureGameDuration()
        }
    }

    private suspend fun measureGameDuration() {
        gameState.collect {
            while (gameState.value is FlowGameState.Started || gameState.value is FlowGameState.Resumed) {
//                startTime = System.currentTimeMillis()
                delay(1000)
//                val currentTime = System.currentTimeMillis()
//                val duration = currentTime - startTime


                val gameDuration = gameData.value.gameDuration + 1000
                gameData.update { it.copy(gameDuration = gameDuration) }
            }
        }
    }

    private fun processPassedItems(firstPassedItemIndex: Int) {
        val figures = gameData.value.figures
        val coefficient = gameData.value.coefficient
        val lifeCount = gameData.value.lifeCount

        if (firstPassedItemIndex >= 0) {
            val startIndex = firstPassedItemIndex
            val endIndex = firstPassedItemIndex + rowWidth - 1

            if (endIndex < figures.size) {
                val missedItemsCount = getMissedItemsCount(startIndex, endIndex)
                repeat(missedItemsCount) {
                    itemsPassedWithoutMissing = 0
                    if (coefficient > 1) {
                        decreaseCoefficients()
                    } else {
                        decreaseLifeCount()
                        if (lifeCount < 0) finishGame()
                    }
                }
            } else {
//                finishGame()
            }
        }
    }

    private fun increaseLifeCount() {
        val lifeCount = gameData.value.lifeCount.inc().coerceAtMost(maxLifeCount)

        gameData.update { it.copy(lifeCount = lifeCount) }
    }

    private fun decreaseLifeCount() {
        val lifeCount = gameData.value.lifeCount.dec()

        gameData.update { it.copy(lifeCount = lifeCount) }
    }

    private fun increaseCoefficients() {
        val coefficient = gameData.value.coefficient + coefficientStep

        gameData.update { it.copy(coefficient = coefficient) }
    }

    private fun decreaseCoefficients() {
        gameData.update { currentData ->
            val newCoefficient =
                if (currentData.coefficient > 1) (currentData.coefficient / 2).coerceAtLeast(1f) else
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

    /**
     * Return the amount of missed items in diapason from [startIndex] to [endIndex].
     * The items is missed if is [isFitForGoal] and still active.
     *
     * @return count of missed items.
     */
    private fun getMissedItemsCount(startIndex: Int, endIndex: Int): Int {
        val figures = gameData.value.figures
        val goals = gameData.value.goals
        var count = 0
        for (i in startIndex..endIndex) {
            val item = figures[i]
            if (isItemFitForGoals(
                    goals,
                    item
                ) && item.isActive
            ) count++
        }
        return count
    }

    /**
     * Check if the item is fit for the goals.
     *
     * @return true if item fit for goal else false.
     */
    private fun isItemFitForGoals(goals: Set<Goal<Any>>, item: Figure): Boolean {
        var condition = false
        run goal@{
            goals.forEach { goal ->
                condition = item.isFitForGoal(goal)
                if (condition) return@goal
            }
        }
        return condition
    }

    private fun addMoreItems() {
        gameData.update { currentData ->
            val newList = figuresRepository.getRandomFigures(
                itemsCount = itemsCount,
                startId = itemsCount,
                percentageOfSuitableGoalItems = percentOfSuitableItem,
                goal = currentData.goals.first()
            )

            val newFiguresList = currentData.figures + newList
            itemsCount = newFiguresList.size

            currentData.copy(figures = newFiguresList)
        }
    }

    /**
     * Do actions when an item clicked.
     *
     * @param id id of item that was clicked.
     *
     * @return the number of points added for this click.
     */
    private fun onItemClick(id: Int) {
        //TODO: Look for some replacement for 'find' operator. Maybe use HashMap?
        val figures = gameData.value.figures
        val goals = gameData.value.goals

        figures.find { figure -> figure.id == id }?.let { figure ->
            if (isItemFitForGoals(goals, figure)) {
                figure.isActive = false
                figure.pointsReceived = onCorrectItemClicked()
            } else {
                finishGame()
            }
        }
        gameData.update { it.copy(figures = figures) }
    }

    private fun onCorrectItemClicked(): Int {
        val pointsAdded = increaseScore()
        increaseCoefficients()

        itemsPassedWithoutMissing++
        if (itemsPassedWithoutMissing >= itemsPassedWithoutMissToGetLife) {
            increaseLifeCount()
            itemsPassedWithoutMissing = 0
        }
        return pointsAdded
    }

    /**
     * Set the real height of an item. Used in calculations.
     */
    private fun setItemHeight(@Px height: Int) {
        itemHeightPx = height
        updateScrollDuration()
        updatePixelsToScroll()
    }

    private fun updateScrollDuration() {
        scrollDuration = getScrollDuration()
        gameData.update { it.copy(scrollDuration = scrollDuration) }
    }

    /**
     * The duration of scrolling animation for fixed items count.
     * Formula: scroll duration = (row count * row duration) * acceleration percent(<=1).
     *
     * @return scroll duration in millisecond.
     */
    private fun getScrollDuration(): Int {
        val figures = gameData.value.figures
        val coefficient = gameData.value.coefficient
        val gameDuration = gameData.value.gameDuration

        val rowCount = figures.size / rowWidth
        val coefInt = coefficient.toInt()

        // Percentage of time that need to be subtracted from 100% of duration
        val coefPercentage = (coefInt * coefInt / 100f)
        val timePercentage = (((gameDuration / 1000 / 30) * 5) / 100f)
        val actualDurationPercentage = (1f - coefPercentage - timePercentage).coerceAtLeast(0.35f)

        val duration = (rowCount * rowDuration) * actualDurationPercentage

        return duration.toInt()
    }

    private fun updatePixelsToScroll() {
        gameData.update { it.copy(pixelsToScroll = getPixelsToScroll(itemHeightPx.toFloat())) }
    }

    /**
     * Return the amount of pixels needed to scroll to pass through all items.
     *
     * @param rowHeight - the height of row of item. It's equal to the item height.
     * Measured in pixels.
     *
     * @return amount of pixel needed to scroll to reach the end.
     */
    private fun getPixelsToScroll(@Px rowHeight: Float): Float {
        val figures = gameData.value.figures
        val rowCount = figures.size / rowWidth
//        Log.d(
//            TAG,
//            "current speed: ${
//                getScrollSpeed(
//                    pixelsToScroll = rowCount * rowHeight,
//                    getScrollDuration()
//                )
//            } px/sec"
//        )
        return rowCount * rowHeight
    }

    /**
     * Return speed of game. In Pixels/second.
     *
     * @return speed in px/sec.
     */
    private fun getScrollSpeed(pixelsToScroll: Float, scrollDuration: Int): Float =
        pixelsToScroll / (scrollDuration / 1000)
}
