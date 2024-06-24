package com.maxot.seekandcatch.data.repository

import androidx.compose.ui.graphics.Color
import com.maxot.seekandcatch.data.datastore.AccountDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ColorsRepositoryImpl
@Inject constructor(
    private val accountDataStore: AccountDataStore
) : ColorsRepository {
    override val selectedColors: Flow<Set<Color>>
        get() = accountDataStore.selectedColors

    override fun getAvailableColors(): Set<Color> =
        setOf(
            Color.Red,
            Color.Blue,
            Color.Green,
            Color.Yellow,
            Color.Cyan,
            Color.Magenta,
            Color.Black
        )

    override suspend fun setSelectedColors(colors: Set<Color>) {
        try {
            accountDataStore.setSelectedColors(colors)
        } catch (e: Exception) {
            // Handle the exception as needed
            println("Error setting selected colors: ${e.message}")
        }
    }


    override suspend fun getRandomSelectedColor(): Color {
        return try {
            selectedColors.first().random()
        } catch (e: Exception) {
            // Handle the exception as needed
            println("Error getting random selected color: ${e.message}")
            // Return a default color or handle as needed
            Color.White
        }
    }
}
