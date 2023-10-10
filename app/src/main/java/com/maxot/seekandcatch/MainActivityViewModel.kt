package com.maxot.seekandcatch

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.maxot.seekandcatch.data.Figure
import com.maxot.seekandcatch.data.FigureType
import com.maxot.seekandcatch.data.GameMode
import com.maxot.seekandcatch.data.Goal
import com.maxot.seekandcatch.data.repository.ScoreRepository
import com.maxot.seekandcatch.usecase.GameplayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MainActivityViewModel
@Inject
constructor(
    private val gameplayUseCase: GameplayUseCase,
    private val scoreRepository: ScoreRepository
) : ViewModel() {

    private val oneFigureDuration = 200L
    private val baseFrameDuration = 500L
    private val levelDuration = 10000L
    private var delayCoefficient = 1.0f
    private val minDelayCoefficient = 0.7
    private val delayStep = 0.05f
    private val maxCountItems = 28

    private var _gameMode = MutableStateFlow<GameMode>(GameMode.LevelsGameMode)
    val gameMode: StateFlow<GameMode> = _gameMode

    private var _level = MutableStateFlow(0)
    val level: StateFlow<Int> = _level

    private var _levelChanged = MutableStateFlow(true)
    val levelChanged: StateFlow<Boolean> = _levelChanged

    private var _goals = MutableStateFlow(generateRandomGoals(getRandomNumber()))
    val goals: StateFlow<Set<Goal<Any>>> = _goals

    private var _lastScore = MutableStateFlow(0)
    val lastScore: StateFlow<Int> = _lastScore

    private var _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private var _isGameActive = MutableStateFlow<Boolean?>(null)
    val isGameActive: StateFlow<Boolean?> = _isGameActive

    private var _figures = MutableStateFlow(listOf<Figure>())
    val figures: StateFlow<List<Figure>> = _figures

    private var gameJob: Job = Job()
    private var levelJob: Job = Job()

    suspend fun startGame(gameMode: GameMode){
        when (gameMode){
            GameMode.FlowGameMode -> {
                startFlowModeGame()
            }
            GameMode.LevelsGameMode -> {
                startLevelsGame()
            }

        }
    }

    /**
     * Game mode where the screen scrolls infinitely down and show new items.
     */
    private suspend fun startFlowModeGame(){
        _gameMode.value = GameMode.FlowGameMode
        _isGameActive.value = true
        gameJob = CoroutineScope(Job()).launch {
            while (isGameActive.value != null && isGameActive.value == true) {
                _goals.value = generateRandomGoals()
                // Show game screen
                _figures.emit(generateFigures(1000))
                delay(10000)
            }
        }
    }


    /**
     * Game mode where levels and frames changes infinitely .
     */
    private suspend fun startLevelsGame() {
        _gameMode.value = GameMode.LevelsGameMode
        delayCoefficient = 1.0f
        _lastScore.value = 0
        _isGameActive.value = true

        startLevelsUpdate()
    }

    private suspend fun startLevelsUpdate() {
        gameJob = CoroutineScope(Job()).launch {
            while (isGameActive.value != null && isGameActive.value == true) {
                _goals.value = generateRandomGoals()
                _level.value += 1
                _levelChanged.value = true
                // Show level screen
                delay(3000)
                _levelChanged.value = false
                // Show game screen
                startNextLevel()
                delay(levelDuration)
                levelJob.cancel()
                decreaseDelayCoefficient()
            }
        }
    }

    private suspend fun startNextLevel() {
        levelJob = CoroutineScope(Job()).launch {
            while (isGameActive.value != null && isGameActive.value == true) {
                _figures.emit(emptyList())
                delay(200)
                _figures.emit(generateFigures())
                delay(
                    gameplayUseCase.calculateFrameDuration(
                        baseDuration = baseFrameDuration,
                        oneFigureDuration = oneFigureDuration,
                        delayCoefficient = delayCoefficient,
                        frameFigures = figures.value, goals = goals.value
                    )
                )
            }
        }
    }

    private fun stopGame() {
        _isGameActive.value = false
        scoreRepository.saveBestScore(score.value)
        _lastScore.value = score.value
        _score.value = 0
        _level.value = 0
        gameJob.cancel()
        levelJob.cancel()
    }

    private fun decreaseDelayCoefficient() {
        if (delayCoefficient >= minDelayCoefficient) {
            delayCoefficient -= delayStep
        }
    }

    private fun generateFigures(itemsCount: Int = maxCountItems): List<Figure> {
        val listOfFigures = mutableListOf<Figure>()
        for (i in 1..itemsCount) {
            listOfFigures.add(createRandomFigure())
        }
        return listOfFigures
    }

    fun getBestScore() = scoreRepository.getBestScore()

    private fun incrementScore() {
        _score.value += 1
    }

    fun onFigureClick(figure: Figure){
        val condition = gameplayUseCase.checkGoalCondition(
            goals = goals.value,
            figure = figure
        )
        if (condition) {
            incrementScore()
        } else {
            stopGame()
        }
    }

    private fun createRandomFigure(): Figure {
        val figureType = getRandomFigureType(Random.nextInt(4))
        val color = getRandomColor(Random.nextInt(4))
        return Figure(figureType, color)
    }

    private fun generateRandomGoals(seed: Int = getRandomNumber()): Set<Goal<Any>> {
        return when (seed) {
            0 -> setOf(getRandomGoal())
            1 -> setOf(getRandomGoal(), getRandomGoal())
            else -> setOf(Goal.Colored(Color.Red))
        }
    }

    private fun getRandomGoal(seed: Int = getRandomNumber()): Goal<Any> {
        return when (seed) {
            0 -> Goal.Colored(getRandomColor())
            1 -> Goal.Figured(Figure(getRandomFigureType(), null))
//            2 -> Goal.Figured(Figure(getRandomFigureType(), getRandomColor()))
            else -> Goal.Colored(getRandomColor())
        }
    }

    private fun getRandomFigureType(seed: Int = getRandomNumber()): FigureType {
        return when (seed) {
            0 -> FigureType.Circle
            1 -> FigureType.Square
            2 -> FigureType.Triangle
            else -> FigureType.Circle
        }
    }

    private fun getRandomColor(seed: Int = getRandomNumber()): Color {
        return when (seed) {
            0 -> Color.Red
            1 -> Color.Blue
            2 -> Color.Green
            3 -> Color.Yellow
            else -> Color.Red
        }
    }

    private fun getRandomNumber(): Int {
        return Random.nextInt(4)
    }

}
