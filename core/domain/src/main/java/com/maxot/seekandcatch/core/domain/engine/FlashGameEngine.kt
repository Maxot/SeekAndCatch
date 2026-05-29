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
import kotlin.math.sqrt
import kotlin.random.Random

class FlashGameEngine(
    coroutineScope: CoroutineScope,
    figuresRepository: FiguresRepository,
    goalsRepository: GoalsRepository,
    private val random: Random = Random.Default
) : BaseGameEngine(coroutineScope, figuresRepository, goalsRepository) {

    private var flashJob: Job? = null
    private var visibleAtOnceMin: Int = 2
    private var visibleAtOnceMax: Int = 3
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
            ).take(gridCount)

            visibleAtOnceMin = params.visibleAtOnceMin.coerceAtLeast(1)
            visibleAtOnceMax = params.visibleAtOnceMax.coerceAtLeast(visibleAtOnceMin)

            val initialCoefficient = 1f
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
                flashMillis = calculateFlashDuration(visibleAtOnceMax, params, initialCoefficient),
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
            // Cells tapped in the previous cycle, carried into the next iteration so
            // replacement happens after spawnPeriodMillis when all alphas have settled to 0.
            var pendingReplacements: Set<Int> = emptySet()

            while (_gameState.value is GameEngineState.Started) {
                val params = gameParams
                if (params == null) {
                    delay(100)
                    continue
                }
                if (_gameData.value.figures.isEmpty()) {
                    delay(100)
                    continue
                }

                // ── PHASE 1: inter-cycle pause ────────────────────────────────────────
                // The grid is invisible here. Alpha animations from the previous cycle
                // have time to fully settle to 0.
                delay(_gameData.value.spawnPeriodMillis)
                if (_gameState.value !is GameEngineState.Started) break

                // ── PHASE 2: replace figures from the previous cycle ─────────────────
                // All cells are at alpha 0, so swapping figures is invisible.
                replaceTappedFigures(pendingReplacements, params)
                pendingReplacements = emptySet()

                // ── PHASE 3: build the batch to display ──────────────────────────────
                val snapshot = _gameData.value
                val activeIndices = snapshot.figures.indices.filter {
                    snapshot.figures[it].isActive && snapshot.figures[it].pointsReceived == null
                }

                if (activeIndices.isEmpty()) {
                    delay(100)
                    continue
                }

                var suitableIndices = activeIndices.filter {
                    isItemFitForGoals(snapshot.goals, snapshot.figures[it])
                }

                val visibleAtOnce = random.nextInt(visibleAtOnceMin, visibleAtOnceMax + 1)
                val minCorrect = ((visibleAtOnce + 1) / 2).coerceAtLeast(1)
                val forcedReplacements = mutableMapOf<Int, Figure>()

                while (suitableIndices.size < minCorrect) {
                    val candidates = activeIndices.filter { it !in suitableIndices && it !in forcedReplacements.keys }
                    if (candidates.isEmpty()) break
                    val idx = candidates.random(random)
                    val goal = snapshot.goals.random(random)
                    val fig = figuresRepository.getRandomFigures(
                        itemsCount = 1,
                        startId = nextFigureId++,
                        percentageOfSuitableGoalItems = 1f,
                        goal = goal
                    ).first().copy(isActive = true)
                    forcedReplacements[idx] = fig
                    suitableIndices = suitableIndices + idx
                }

                val correctCount = random.nextInt(minCorrect, visibleAtOnce + 1)
                val actualCorrectCount = minOf(correctCount, suitableIndices.size)
                val cycleFlashMillis = calculateFlashDuration(actualCorrectCount, params, snapshot.coefficient)

                val pickedSuitable = suitableIndices.shuffled(random).take(actualCorrectCount).toMutableSet()
                val newlyVisible = pickedSuitable.toMutableSet()

                val nonSuitable = activeIndices.filter { it !in suitableIndices.toSet() && it !in newlyVisible }
                newlyVisible.addAll(nonSuitable.shuffled(random).take(visibleAtOnce - newlyVisible.size))

                if (newlyVisible.size < minOf(visibleAtOnce, activeIndices.size)) {
                    val fallback = activeIndices.filter { it !in newlyVisible }
                    newlyVisible.addAll(
                        fallback.shuffled(random).take(minOf(visibleAtOnce, activeIndices.size) - newlyVisible.size)
                    )
                }

                // ── PHASE 4: show the batch ──────────────────────────────────────────
                var figuresAtFlashStart = emptyList<Figure>()
                var goalsAtFlashStart = emptySet<Goal<Any>>()

                _gameData.update { current ->
                    val updatedFigures = if (forcedReplacements.isNotEmpty()) {
                        val list = current.figures.toMutableList()
                        forcedReplacements.forEach { (idx, fig) -> list[idx] = fig }
                        list
                    } else {
                        current.figures
                    }
                    figuresAtFlashStart = updatedFigures.toList()
                    goalsAtFlashStart = current.goals
                    current.copy(figures = updatedFigures, visibleCells = newlyVisible, flashMillis = cycleFlashMillis)
                }

                delay(cycleFlashMillis)

                // ── PHASE 5: process ─────────────────────────────────────────────────
                var snapshotClicked = emptySet<Int>()
                _gameData.update {
                    snapshotClicked = it.clickedSuitableCells
                    it.copy(visibleCells = emptySet(), clickedSuitableCells = emptySet())
                }
                handleMissedItems(newlyVisible, snapshotClicked, figuresAtFlashStart, goalsAtFlashStart)
                pendingReplacements = snapshotClicked
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

        // Mark the original figure with pointsReceived so the UI plays the break animation.
        // The slot is replaced with a fresh figure at cycle end (replaceTappedFigures).
        _gameData.update { current ->
            val updatedFigures = current.figures.toMutableList()
            updatedFigures[index] = figure.copy(pointsReceived = pointsAdded)

            val newCoefficient = current.coefficient + (params.coefficientStep ?: 0f)
            val baseSpawnPeriodMillis = (params.rowDuration * 1.2f * 1.5f).toLong()

            val updated = current.copy(
                figures = updatedFigures,
                score = current.score + pointsAdded,
                coefficient = newCoefficient,
            )
            updated.copy(
                spawnPeriodMillis = calculateSpawnDuration(baseSpawnPeriodMillis, updated)
            )
        }

        if (itemsPassedWithoutMissing >= params.itemsPassedWithoutMissToGetLife) {
            increaseLifeCount()
            itemsPassedWithoutMissing = 0
        }
    }

    private fun replaceTappedFigures(clickedIndices: Set<Int>, params: GameParams) {
        if (clickedIndices.isEmpty()) return
        _gameData.update { current ->
            val updatedFigures = current.figures.toMutableList()
            clickedIndices.forEach { index ->
                if (index < updatedFigures.size) {
                    val newFigure = figuresRepository.getRandomFigures(
                        itemsCount = 1,
                        startId = nextFigureId++,
                        percentageOfSuitableGoalItems = params.percentOfSuitableItem,
                        goal = current.goals.first()
                    ).first()
                    updatedFigures[index] = newFigure.copy(isActive = true)
                }
            }
            current.copy(figures = updatedFigures)
        }
    }

    override fun decreaseCoefficient() {
        val params = gameParams ?: return
        var shouldDecreaseLife = false
        _gameData.update { current ->
            val newCoefficient = (current.coefficient / 2f).coerceAtLeast(1f)
            val baseSpawnPeriodMillis = (params.rowDuration * 1.2f * 1.5f).toLong()

            val updated = current.copy(
                coefficient = newCoefficient,
                isLifeWasted = true
            )
            val finalData = updated.copy(
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
            val baseSpawnPeriodMillis = (params.rowDuration * 1.2f * 1.5f).toLong()
            current.copy(
                spawnPeriodMillis = calculateSpawnDuration(baseSpawnPeriodMillis, current)
            )
        }
    }

    override fun onTimeTick() {
        super.onTimeTick()
        updateDurations()
    }

    private fun calculateFlashDuration(correctCount: Int, params: GameParams, coefficient: Float): Long {
        val divisor = sqrt(coefficient.toInt().toFloat()).coerceAtLeast(1f)
        return (correctCount * params.flashTimePerItemMillis / divisor).toLong()
            .coerceAtLeast(MIN_FLASH_MILLIS)
    }

    private fun calculateSpawnDuration(base: Long, data: GameEngineData): Long {
        return (base / data.coefficient).toLong().coerceAtLeast(MIN_SPAWN_PERIOD_MILLIS)
    }

    override fun reset() {
        super.reset()
        stopFlashLoop()
    }

    companion object {
        private const val MIN_FLASH_MILLIS = 300L
        private const val MIN_SPAWN_PERIOD_MILLIS = 300L
    }
}
