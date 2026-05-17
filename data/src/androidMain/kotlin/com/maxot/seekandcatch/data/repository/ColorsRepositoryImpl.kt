package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.data.datastore.AccountDataStore
import com.maxot.seekandcatch.data.model.FigureColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ColorsRepositoryImpl
@Inject constructor(
    private val accountDataStore: AccountDataStore
) : ColorsRepository {
    override val selectedColors: Flow<Set<FigureColor>>
        get() = accountDataStore.selectedColors

    override fun getAvailableColors(): Set<FigureColor> =
        setOf(
            FigureColor.Red,
            FigureColor.Blue,
            FigureColor.Green,
            FigureColor.Yellow,
            FigureColor.Cyan,
            FigureColor.Magenta,
            FigureColor.Black
        )

    override suspend fun setSelectedColors(colors: Set<FigureColor>) {
        try {
            accountDataStore.setSelectedColors(colors)
        } catch (e: Exception) {
            println("Error setting selected colors: ${e.message}")
        }
    }

    override suspend fun getRandomSelectedColor(): FigureColor {
        return try {
            selectedColors.first().random()
        } catch (e: Exception) {
            println("Error getting random selected color: ${e.message}")
            FigureColor.White
        }
    }
}
