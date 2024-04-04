package com.maxot.seekandcatch.feature.gameplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.feature.gameplay.data.Figure
import com.maxot.seekandcatch.feature.gameplay.data.GameParams
import com.maxot.seekandcatch.feature.gameplay.data.Goal
import com.maxot.seekandcatch.feature.gameplay.usecase.FlowGameUseCase
import com.maxot.seekandcatch.feature.settings.AppSoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlowGameViewModel
@Inject constructor(
    private val gameUseCase: FlowGameUseCase,
    private val appSoundManager: AppSoundManager
) : ViewModel() {

    val goals: StateFlow<Set<Goal<Any>>> = gameUseCase.goals
    val score: StateFlow<Int> = gameUseCase.score
    val figures: StateFlow<List<Figure>> = gameUseCase.figures
    val coefficient: StateFlow<Float> = gameUseCase.coefficient
    val gameState = gameUseCase.gameState

    init {
        viewModelScope.launch {
            appSoundManager.init()

            coefficient.collect {
                val musicSpeed = 1 + it / 5
                appSoundManager.setMusicSpeed(musicSpeed.toFloat())
            }
        }
    }

    fun startGame() {
        appSoundManager.startMusic()
        gameUseCase.startGame(GameParams(5000, 0.3f))
    }

    fun pauseGame() {
        appSoundManager.pauseMusic()
        gameUseCase.pauseGame()
    }

    fun resumeGame() {
        appSoundManager.startMusic()
        gameUseCase.resumeGame()
    }

    fun finishGame() {
        appSoundManager.stopMusic()
        gameUseCase.finishGame()
    }

    fun stopMusic() {
        appSoundManager.stopMusic()
    }

    fun onItemClick(id: Int) {
        gameUseCase.onItemClick(id)
    }

    fun onItemsMissed(start: Int, end: Int) {
        gameUseCase.onItemsMissed(start, end)
    }

    fun getAnimationDuration(): Int {
        return gameUseCase.getAnimationDuration()
    }

    override fun onCleared() {
        super.onCleared()
        appSoundManager.release()
    }
}
