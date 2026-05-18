package com.maxot.seekandcatch.feature.gameplay.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maxot.seekandcatch.data.model.Figure
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun FlashGameFieldLayout(
    modifier: Modifier = Modifier,
    gridWidth: Int,
    gridSize: Int,
    figuresByCell: Map<Int, Figure>,
    visibleCells: Set<Int>,
    onCellClick: (Int) -> Unit,
    isGameOver: Boolean = false,
) {
    // Track which visible cells were clicked so they hide immediately
    val (hiddenIds, setHiddenIds) = remember { mutableStateOf<Set<Int>>(emptySet()) }

    // Cleanup hidden ids when visibility changes to avoid leaks
    LaunchedEffect(visibleCells) {
        setHiddenIds(hiddenIds.intersect(visibleCells))
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(gridWidth),
        modifier = Modifier.then(modifier),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        val cells = (0 until gridSize).toList()
        items(cells, key = { it }) { id ->
            val isCurrentlyVisible = visibleCells.contains(id)
            val visible = isCurrentlyVisible && !hiddenIds.contains(id)
            val base = figuresByCell[id]
            if (base != null) {
                val figure = base.copy(isActive = visible)
                ColoredFigureLayout(
                    figure = figure,
                    onItemClick = {
                        if (isCurrentlyVisible && !hiddenIds.contains(id)) {
                            setHiddenIds(hiddenIds + id)
                            onCellClick(id)
                        }
                    },
                    isGameOver = isGameOver
                )
            } else {
                Box {}
            }
        }
    }
}
