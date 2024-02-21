package com.maxot.seekandcatch.feature.gameplay

import android.os.CountDownTimer
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.feature.gameplay.data.Figure
import com.maxot.seekandcatch.feature.gameplay.data.FigureType
import com.maxot.seekandcatch.feature.gameplay.data.GameMode
import com.maxot.seekandcatch.feature.gameplay.data.Goal
import com.maxot.seekandcatch.feature.gameplay.usecase.GameplayUseCase
import com.maxot.seekandcatch.feature.score.data.repository.ScoreRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class GameViewModel
@Inject
constructor(
    private val gameplayUseCase: GameplayUseCase,
    private val scoreRepository: ScoreRepository
) : ViewModel() {

    private val oneFigureDuration = 200L
    private val baseFrameDuration = 500L
    private val baseLevelDuration = 10000L
    private val previewDelay = 3000L
    private var delayCoefficient = 1.0f
    private val minDelayCoefficient = 0.7
    private val delayStep = 0.05f
    private val maxCountItems = 28

    private var _gameMode = MutableStateFlow<GameMode>(GameMode.LevelsGameMode)
    val gameMode: StateFlow<GameMode> = _gameMode

    private var _level = MutableStateFlow(0)
    val level: StateFlow<Int> = _level

    private var _goals = MutableStateFlow(generateRandomGoals(getRandomNumber()))
    val goals: StateFlow<Set<Goal<Any>>> = _goals

    private var _lastScore = MutableStateFlow(0)
    val lastScore: StateFlow<Int> = _lastScore

    private var _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private var _figures = MutableStateFlow(listOf<Figure>())
    val figures: StateFlow<List<Figure>> = _figures

    private val _gameUiState = MutableStateFlow<GameUiState>(GameUiState.LevelPreview)
    val gameUiState: StateFlow<GameUiState>
        get() = _gameUiState

    private var gameJob: Job = Job()
    private var levelJob: Job = Job()

    /**
     * Timer for level
     */
    private val timer = object : CountDownTimer(getLevelDuration(), 1){
        override fun onTick(millisUntilFinished: Long) {
            _timeToEnd.value = millisUntilFinished
        }

        override fun onFinish() {

        }
    }

    private var _timeToEnd = MutableStateFlow(0L)
    val timeToEnd: StateFlow<Long> = _timeToEnd


    /**
     * Starts the game in the one of the possible modes.
     */
    suspend fun startGame(gameMode: GameMode) {
        when (gameMode) {
            GameMode.FlowGameMode -> {
                startFlowModeGame()
            }
            GameMode.LevelsGameMode -> {
                startLevelsModeGame()
            }

        }
    }

    /**
     * Game mode where the screen scrolls infinitely down and show new items.
     */
    private suspend fun startFlowModeGame() {
        _gameMode.value = GameMode.FlowGameMode
        _gameUiState.value = GameUiState.InProgress
        gameJob = viewModelScope.launch {
            while (gameUiState.value == GameUiState.InProgress) {
                _level.value += 1
                _goals.value = generateRandomGoals()
                _gameUiState.value = GameUiState.LevelPreview
                delay(previewDelay)
                _gameUiState.value = GameUiState.InProgress
                _figures.emit(generateFigures(500))
                timer.start()
                delay(getLevelDuration())
                timer.cancel()
            }
        }
    }


    /**
     * Game mode where levels and frames changes infinitely.
     */
    private suspend fun startLevelsModeGame() {
        _gameMode.value = GameMode.LevelsGameMode
        delayCoefficient = 1.0f
        _lastScore.value = 0
        _gameUiState.value = GameUiState.InProgress

        startLevelsUpdate()
    }

    private suspend fun startLevelsUpdate() {
        gameJob = viewModelScope.launch {
            while (gameUiState.value == GameUiState.InProgress) {
                _level.value += 1
                _goals.value = generateRandomGoals()
                _gameUiState.value = GameUiState.LevelPreview
                // Show level screen
                delay(previewDelay)
                _gameUiState.value = GameUiState.InProgress
                // Show game screen
                startNextLevel()
                delay(getLevelDuration())
                levelJob.cancel()
                decreaseDelayCoefficient()
            }
        }
    }

    private suspend fun startNextLevel() {
        levelJob = viewModelScope.launch {
            while (gameUiState.value == GameUiState.InProgress) {
                _figures.emit(emptyList())
                delay(1000)
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
        _gameUiState.value = GameUiState.GameEnded
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

    fun getLevelDuration() = gameplayUseCase.calculateLevelDuration(baseLevelDuration, figures.value.size)

    fun onFigureClick(figure: Figure) {
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

    sealed class GameUiState {
//        object Started : GameUiState()
        object InProgress : GameUiState()
        object LevelPreview : GameUiState()
        object GameEnded : GameUiState()
//        object Paused : GameUiState()
    }
}
