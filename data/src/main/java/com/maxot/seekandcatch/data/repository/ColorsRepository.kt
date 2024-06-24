package com.maxot.seekandcatch.data.repository

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow

interface ColorsRepository {

    val selectedColors: Flow<Set<Color>>

    fun getAvailableColors(): Set<Color>

    suspend fun setSelectedColors(colors: Set<Color>)

    suspend fun getRandomSelectedColor(): Color
}
