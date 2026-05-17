package com.maxot.seekandcatch.data.repository


import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal
import javax.inject.Inject
import kotlin.random.Random

class GoalsRepositoryImpl
@Inject constructor(
    private val colorsRepository: ColorsRepository
) : GoalsRepository {
    override suspend fun getRandomGoal(): Goal<Any> {
        return when (Random.nextInt(0, 2)) {
            0 -> Goal.Colored(colorsRepository.getRandomSelectedColor())
            1 -> Goal.Shaped(Figure.FigureType.getRandomFigureType())
            else -> Goal.Colored(colorsRepository.getRandomSelectedColor())
        }
    }
}

