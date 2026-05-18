package com.maxot.seekandcatch.core.domain.engine

import com.maxot.seekandcatch.core.common.model.GameParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface GameEngine {
    val gameState: StateFlow<GameEngineState>
    val gameData: StateFlow<GameEngineData>
    val coroutineScope: CoroutineScope

    fun initGame(gameParams: GameParams)
    fun startGame()
    fun pauseGame()
    fun resumeGame()
    fun finishGame()
    fun reset()
    fun onItemClick(itemId: Int)
    fun setFirstVisibleItemIndex(index: Int)
    fun setItemHeight(height: Int)
}
