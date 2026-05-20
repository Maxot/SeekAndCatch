package com.maxot.seekandcatch.core.domain.engine

import com.maxot.seekandcatch.core.common.model.GameParams
import com.maxot.seekandcatch.core.common.model.Figure
import com.maxot.seekandcatch.core.common.model.Goal
import com.maxot.seekandcatch.core.common.model.isFitForGoal
import com.maxot.seekandcatch.data.repository.FiguresRepository
import com.maxot.seekandcatch.data.repository.GoalsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseGameEngine(
    override val coroutineScope: CoroutineScope,
    protected val figuresRepository: FiguresRepository,
    protected val goalsRepository: GoalsRepository
) : GameEngine {

    protected val _gameState = MutableStateFlow<GameEngineState>(GameEngineState.Idle)
    override val gameState: StateFlow<GameEngineState> = _gameState.asStateFlow()

    protected val _gameData = MutableStateFlow(GameEngineData())
    override val gameData: StateFlow<GameEngineData> = _gameData.asStateFlow()

    protected var gameParams: GameParams? = null
    protected var timeJob: Job? = null
    protected var initJob: Job? = null

    protected var itemsPassedWithoutMissing = 0

    override fun initGame(gameParams: GameParams) {
        reset()
        this.gameParams = gameParams
        startInitJob(gameParams)
    }

    protected open fun startInitJob(params: GameParams) {
        initJob = coroutineScope.launch {
            val goal = goalsRepository.getRandomGoal()
            val goals = setOf(goal)
            val figures = figuresRepository.getRandomFigures(
                itemsCount = params.itemsCount,
                percentageOfSuitableGoalItems = params.percentOfSuitableItem,
                goal = goal
            )
            val initialData = GameEngineData(
                goals = goals,
                figures = figures,
                goalSuitableFigures = figuresRepository.getFigureSuitableForGoal(goal),
                maxLifeCount = params.maxLifeCount,
                lifeCount = params.lifeCount,
                score = 0,
                coefficient = 1f,
                rowWidth = params.rowWidth
            )
            _gameData.value = initialData
            _gameState.value = GameEngineState.Created(initialData.goalSuitableFigures)
        }
    }

    override fun startGame() {
        _gameState.value = GameEngineState.Started
        startTimeTracking()
    }

    override fun pauseGame() {
        if (_gameState.value is GameEngineState.Started) {
            _gameState.value = GameEngineState.Paused
            stopTimeTracking()
        }
    }

    override fun resumeGame() {
        if (_gameState.value is GameEngineState.Paused) {
            _gameState.value = GameEngineState.Started
            startTimeTracking()
        }
    }

    override fun finishGame() {
        val finalScore = _gameData.value.score
        _gameState.value = GameEngineState.Finished(finalScore)
        stopTimeTracking()
    }
    
    override fun reset() {
        stopTimeTracking()
        initJob?.cancel()
        initJob = null
        _gameData.value = GameEngineData()
        _gameState.value = GameEngineState.Idle
        itemsPassedWithoutMissing = 0
    }

    protected fun startTimeTracking() {
        timeJob?.cancel()
        timeJob = coroutineScope.launch {
            while (true) {
                delay(1000)
                _gameData.update { it.copy(gameDuration = it.gameDuration + 1000) }
            }
        }
    }

    protected fun stopTimeTracking() {
        timeJob?.cancel()
        timeJob = null
    }

    protected open fun onTimeTick() {
    }

    override fun onItemClick(itemId: Int) {
        if (_gameState.value !is GameEngineState.Started) return

        val currentData = _gameData.value
        val figures = currentData.figures
        val figure = figures.find { it.id == itemId } ?: return
        if (!figure.isActive) return

        if (isItemFitForGoals(currentData.goals, figure)) {
            handleCorrectTap(figure)
        } else {
            handleWrongTap()
        }
    }

    protected open fun handleCorrectTap(figure: Figure) {
        itemsPassedWithoutMissing++

        val pointsAdded = calculatePoints()

        // Update figure state
        _gameData.update { current ->
            val updatedFigures = current.figures.map {
                if (it.id == figure.id) it.copy(isActive = false, pointsReceived = pointsAdded) else it
            }

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
    }

    protected open fun handleWrongTap() {
        finishGame()
    }

    protected fun calculatePoints(): Int {
        val basePoints = gameParams?.scorePoint ?: 10
        return (basePoints * _gameData.value.coefficient).toInt()
    }

    protected fun increaseLifeCount() {
        _gameData.update { 
            val newLifeCount = (it.lifeCount + 1).coerceAtMost(it.maxLifeCount)
            it.copy(lifeCount = newLifeCount)
        }
    }

    protected fun decreaseLifeCount() {
        _gameData.update {
            val newLifeCount = it.lifeCount - 1
            if (newLifeCount == 0) {
                finishGame()
            }
            it.copy(lifeCount = newLifeCount.coerceAtLeast(0), isLifeWasted = true)
        }
        coroutineScope.launch {
            delay(500)
            _gameData.update { it.copy(isLifeWasted = false) }
        }
        itemsPassedWithoutMissing = 0
    }

    protected open fun decreaseCoefficient() {
        _gameData.update {
            val newCoef = (it.coefficient / 2f).coerceAtLeast(1f)
            it.copy(coefficient = newCoef, isLifeWasted = true)
        }
        coroutineScope.launch {
            delay(500)
            _gameData.update { it.copy(isLifeWasted = false) }
        }
        itemsPassedWithoutMissing = 0
    }

    protected fun isItemFitForGoals(goals: Set<Goal<Any>>, item: Figure): Boolean {
        return goals.any { item.isFitForGoal(it) }
    }
    override fun setFirstVisibleItemIndex(index: Int) {
        // Default implementation for engines that don't need it
    }

    override fun setItemHeight(height: Int) {
        // Default implementation for engines that don't need it
    }
}
