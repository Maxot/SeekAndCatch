package com.maxot.seekandcatch.core.domain

import androidx.annotation.Px
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
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TAG = "FlowGameUseCase"

enum class GameState {
    IDLE, CREATED, STARTED, RESUMED, PAUSED, FINISHED
}

/**
 * Represent game logic for Flow Game Mode.
 * The game consists of grid of [figures] with fixed [rowWidth] and the [goals].
 * Items are auto-scrolled with some speed(px/ms), the amount of pixel to scroll calculated by [getPixelsToScroll]
 * and the scroll duration calculated by [getScrollDuration]. Then UI pass back [firstVisibleItemIndex]
 * to process game updates via [processGameChanges].
 * During this process user should click on items via [onItemClick] then it check if item if fit for the goals
 * via [isFitForGoal], and if it is fit [onCorrectItemClicked] else [finishGame].
 */
class FlowGameUseCase
@Inject constructor(
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
    var maxLifeCount: Int = 5
        private set

    private var itemsPassedWithoutMissToGetLife = 10

    private var itemsPassedWithoutMissing = 0

    private var _lifeCount =
        MutableStateFlow(0)
    val lifeCount: StateFlow<Int> = _lifeCount

    private var _goals =
        MutableStateFlow(emptySet<Goal<Any>>())
    val goals: StateFlow<Set<Goal<Any>>> = _goals

    private var _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private var _figures = MutableStateFlow(listOf<Figure>())
    val figures: StateFlow<List<Figure>> = _figures

    private var _firstVisibleItemIndex = MutableStateFlow(0)
    val firstVisibleItemIndex: StateFlow<Int> = _firstVisibleItemIndex

    private var _coefficient = MutableStateFlow(1f)
    val coefficient: StateFlow<Float> = _coefficient

    private var _gameDuration = MutableStateFlow(0L)
    val gameDuration: StateFlow<Long> = _gameDuration

    private val _gameState =
        MutableStateFlow(GameState.IDLE)
    val gameState: StateFlow<GameState>
        get() = _gameState

    private var gameJob: Job? = null
    private var timeJob: Job? = null

    fun setFirstVisibleItemIndex(index: Int) {
        _firstVisibleItemIndex.value = index
    }

    fun initGame(gameParams: GameParams) {
        scorePoint = gameParams.scorePoint
        coefficientStep = gameParams.coefficientStep
        itemsCount = gameParams.itemsCount
        percentOfSuitableItem = gameParams.percentOfSuitableItem
        rowWidth = gameParams.rowWidth
        rowDuration = gameParams.rowDuration
        maxLifeCount = gameParams.maxLifeCount
        itemsPassedWithoutMissToGetLife = gameParams.itemsPassedWithoutMissToGetLife

        _lifeCount.value = gameParams.lifeCount
        _goals.value = setOf(goalsRepository.getRandomGoal())
        _figures.value = figuresRepository.getRandomFigures(
            itemsCount = itemsCount,
            percentageOfSuitableGoalItems = percentOfSuitableItem,
            goal = _goals.value.first()
        )

        _gameState.value = GameState.CREATED
    }

    fun startGame() {
        processGameChanges()

        _gameState.value = GameState.STARTED
    }

    fun pauseGame() {
        gameJob?.cancel()
        timeJob?.cancel()

        _gameState.value = GameState.PAUSED
    }

    fun resumeGame() {
        processGameChanges()

        _gameState.value = GameState.RESUMED
    }

    fun finishGame() {
        gameJob?.cancel()
        timeJob?.cancel()
        scoreRepository.setLastScore(score = score.value)

        _gameState.value = GameState.FINISHED
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
            while (gameState.value == GameState.STARTED || gameState.value == GameState.RESUMED) {
//                startTime = System.currentTimeMillis()
                delay(1000)
//                val currentTime = System.currentTimeMillis()
//                val duration = currentTime - startTime
//                _gameDuration.value += duration
                _gameDuration.value += 1000
            }
        }
    }

    private fun processPassedItems(firstPassedItemIndex: Int) {
        if (firstPassedItemIndex >= 0) {
            val startIndex = firstPassedItemIndex
            val endIndex = firstPassedItemIndex + rowWidth - 1

            if (endIndex < figures.value.size) {
                val missedItemsCount = getMissedItemsCount(startIndex, endIndex)
                repeat(missedItemsCount) {
                    itemsPassedWithoutMissing = 0
                    if (coefficient.value > 1) {
                        decreaseCoefficients()
                    } else {
                        decreaseLifeCount()
                        if (_lifeCount.value < 0) finishGame()
                    }
                }
            } else {
//                finishGame()
            }
        }
    }

    private fun increaseLifeCount() {
        _lifeCount.value =
            if (lifeCount.value < maxLifeCount) lifeCount.value.inc() else lifeCount.value
    }

    private fun decreaseLifeCount() {
        _lifeCount.value = lifeCount.value.dec()
    }

    private fun increaseCoefficients() {
        _coefficient.value += coefficientStep
    }

    private fun decreaseCoefficients() {
        if (_coefficient.value > 1)
            _coefficient.value = (coefficient.value / 2).coerceAtLeast(1f)
    }

    private fun increaseScore() {
        _score.value += _coefficient.value.toInt() * scorePoint
    }

    /**
     * Return the amount of missed items in diapason from [startIndex] to [endIndex].
     * The items is missed if is [isFitForGoal] and still active.
     *
     * @return count of missed items.
     */
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
        val newList = figuresRepository.getRandomFigures(
            itemsCount = itemsCount,
            startId = itemsCount,
            percentageOfSuitableGoalItems = percentOfSuitableItem,
            goal = _goals.value.first()
        )

        _figures.value += newList
        itemsCount = _figures.value.size
    }

    /**
     * Do actions when an item clicked.
     *
     * @param id id of item that was clicked.
     */
    fun onItemClick(id: Int) {
        //TODO: Look for some replacement for 'find' operator. Maybe use HashMap?
        figures.value.find { figure -> figure.id == id }?.let { figure ->
            if (isItemFitForGoals(goals.value, figure)) {
                figure.isActive = false
                onCorrectItemClicked()
            } else {
                finishGame()
            }
        }
    }

    private fun onCorrectItemClicked() {
        increaseScore()
        increaseCoefficients()

        itemsPassedWithoutMissing++
        if (itemsPassedWithoutMissing >= itemsPassedWithoutMissToGetLife) {
            increaseLifeCount()
            itemsPassedWithoutMissing = 0
        }
    }

    /**
     * @return the items count in one row.
     */
    fun getRowWidth(): Int = rowWidth

    /**
     * Set the real height of an item. Used in calculations.
     */
    fun setItemHeight(@Px height: Int) {
        itemHeightPx = height
    }

    fun getItemHeight(): Int = itemHeightPx

    /**
     * The duration of scrolling animation for fixed items count.
     * Formula: scroll duration = (row count * row duration) * acceleration percent(<=1).
     *
     * @return scroll duration in millisecond.
     */
    fun getScrollDuration(): Int {
        val rowCount = figures.value.size / rowWidth
        val coefInt = coefficient.value.toInt()

        // Percentage of time that need to be subtracted from 100% of duration
        val coefPercentage = (coefInt * coefInt / 100f)
        val timePercentage = (((gameDuration.value / 1000 / 30) * 5) / 100f)
        val actualDurationPercentage = (1f - coefPercentage - timePercentage).coerceAtLeast(0.35f)

        val duration = (rowCount * rowDuration) * actualDurationPercentage

        return duration.toInt()
    }

    fun getPixelsToScroll(): Float =
        getPixelsToScroll(itemHeightPx.toFloat())

    /**
     * Return the amount of pixels needed to scroll to pass through all items.
     *
     * @param rowHeight - the height of row of item. It's equal to the item height.
     * Measured in pixels.
     *
     * @return amount of pixel needed to scroll to reach the end.
     */
    private fun getPixelsToScroll(@Px rowHeight: Float): Float {
        val rowCount = figures.value.size / rowWidth
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
    fun getScrollSpeed(pixelsToScroll: Float, scrollDuration: Int): Float =
        pixelsToScroll / (scrollDuration / 1000)
}
