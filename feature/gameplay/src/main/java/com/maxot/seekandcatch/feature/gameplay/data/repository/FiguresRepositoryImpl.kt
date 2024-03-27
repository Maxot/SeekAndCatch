package com.maxot.seekandcatch.feature.gameplay.data.repository

import com.maxot.seekandcatch.feature.gameplay.data.Figure
import javax.inject.Inject

class FiguresRepositoryImpl
@Inject constructor() : FiguresRepository {

    override fun getRandomFigure(): Figure {
        return Figure.getRandomFigure()
    }

    override fun getRandomFigures(itemsCount: Int): List<Figure> {
        val listOfFigures = mutableListOf<Figure>()
        for (i in 0..<itemsCount) {
            listOfFigures.add(Figure.getRandomFigure(id = i))
        }
        return listOfFigures
    }


}