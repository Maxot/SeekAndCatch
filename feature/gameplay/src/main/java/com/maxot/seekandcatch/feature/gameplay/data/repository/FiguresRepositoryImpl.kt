package com.maxot.seekandcatch.feature.gameplay.data.repository

import com.maxot.seekandcatch.feature.gameplay.data.Figure
import javax.inject.Inject
import kotlin.random.Random

class FiguresRepositoryImpl
@Inject constructor() : FiguresRepository {

    override fun createRandomFigure(): Figure {
        val figureType = FiguresRepository.getRandomFigureType(Random.nextInt(4))
        val color = FiguresRepository.getRandomColor(Random.nextInt(4))
        return Figure(figureType, color)
    }

    override fun generateFigures(itemsCount: Int): List<Figure> {
        val listOfFigures = mutableListOf<Figure>()
        for (i in 1..itemsCount) {
            listOfFigures.add(createRandomFigure())
        }
        return listOfFigures
    }



}