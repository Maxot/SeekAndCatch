package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.data.model.FigureColor
import kotlinx.coroutines.flow.Flow

interface ColorsRepository {

    val selectedColors: Flow<Set<FigureColor>>

    fun getAvailableColors(): Set<FigureColor>

    suspend fun setSelectedColors(colors: Set<FigureColor>)

    suspend fun getRandomSelectedColor(): FigureColor
}
