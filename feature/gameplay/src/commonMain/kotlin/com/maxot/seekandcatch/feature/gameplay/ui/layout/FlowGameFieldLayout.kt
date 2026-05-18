package com.maxot.seekandcatch.feature.gameplay.ui.layout

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.feature.gameplay.generated.resources.Res
import com.maxot.seekandcatch.feature.gameplay.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun FlowGameFieldLayout(
    modifier: Modifier = Modifier,
    gridWidth: Int = 4,
    spacerHeight: Dp,
    figures: List<Figure>,
    gridState: LazyGridState,
    onItemHeightMeasured: (height: Int) -> Unit = { },
    onItemClick: (id: Int) -> Unit,
    reverseLayout: Boolean = false,
    isGameOver: Boolean = false
) {
    val gameFieldLayoutContentDesc = stringResource(Res.string.game_field_layout_content_desc)

    LazyVerticalGrid(
        modifier = Modifier
            .then(modifier)
            .semantics { contentDescription = gameFieldLayoutContentDesc },
        userScrollEnabled = false,
        state = gridState,
        columns = GridCells.Fixed(gridWidth),
        reverseLayout = reverseLayout
    ) {
        // Add spacer for one row to reach scrolling from empty space
        repeat(gridWidth) {
            item {
                Spacer(
                    modifier = Modifier
                        .height(spacerHeight)
                )
            }
        }
        items(
            items = figures,
            key = { figure -> figure.id }
        ) { figure ->
            ColoredFigureLayout(
                modifier = Modifier
                    .onGloballyPositioned {
                        onItemHeightMeasured(it.size.height)
                    },
                figure = figure,
                onItemClick = { onItemClick(figure.id) },
                isGameOver = isGameOver
            )
        }
        // Add spacer for one row to reach scrolling to empty space
        repeat(gridWidth) {
            item {
                Spacer(
                    modifier = Modifier
                        .height(spacerHeight)
                )
            }
        }
    }
}
