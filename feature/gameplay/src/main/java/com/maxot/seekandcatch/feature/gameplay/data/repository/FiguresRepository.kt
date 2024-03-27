package com.maxot.seekandcatch.feature.gameplay.data.repository

import com.maxot.seekandcatch.feature.gameplay.data.Figure

interface FiguresRepository {
    fun getRandomFigure(): Figure

    fun getRandomFigures(itemsCount: Int): List<Figure>

}