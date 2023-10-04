package com.maxot.seekandcatch

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.maxot.seekandcatch.data.Figure
import com.maxot.seekandcatch.data.FigureType
import com.maxot.seekandcatch.data.Goal
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class MainActivityViewModel : ViewModel() {
    private val delay = 2000L
    private var delayCoefficient = 1.0
    private val maxCountItems = 28

    val goal: Goal = Goal.Colored(Color.Red)

    private var _lastScore = MutableStateFlow(0)
    val lastScore: StateFlow<Int> = _lastScore

    private var _isGameActive = MutableStateFlow(false)
    val isGameActive: StateFlow<Boolean> = _isGameActive

    private var _figures = MutableStateFlow(listOf<Figure>())
    val figures: StateFlow<List<Figure>> = _figures

    suspend fun startGame() {
        delayCoefficient = 1.0
        _lastScore.value = 0
        _isGameActive.value = true
        while (_isGameActive.value) {
            decreaseDelayCoefficient()
            _figures.emit(emptyList())
            delay(200)
            _figures.emit(generateFigures())
            delay((delay * delayCoefficient).toLong())
        }
    }

    fun stopGame(score: Int) {
        _isGameActive.value = false
        _lastScore.value = score
    }

    private fun decreaseDelayCoefficient(){
        if (delayCoefficient >= 0.4) {
            delayCoefficient -= 0.05
        }
    }

    private fun generateFigures(): List<Figure> {
        val listOfFigures = mutableListOf<Figure>()
        for (i in 1..maxCountItems) {
            listOfFigures.add(createRandomFigure())
        }
        return listOfFigures
    }

    private fun createRandomFigure(): Figure {
        val figureType = FigureType.Square
        val color = getRandomColor(Random.nextInt(4))
        return Figure(figureType, color)
    }
}

fun getRandomColor(seed: Int): Color {
    return when (seed) {
        0 -> Color.Red
        1 -> Color.Blue
        2 -> Color.Green
        else -> Color.White
    }
}
