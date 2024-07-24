package com.maxot.seekandcatch.feature.gameplay.ui.layout

import android.view.MotionEvent
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T> SingleSelectionLazyRow(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    items: List<T>,
    selectedItemIndex: Int,
    onSelectedItemChanged: (Int) -> Unit,
    itemContent: @Composable (Modifier, T) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val screenWidth: Float = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    var itemWidth by remember {
        mutableFloatStateOf(0f)
    }

    var selectedItemX by remember {
        mutableFloatStateOf(200f) // ???
    }

    val selectedItemOffset by remember {
        derivedStateOf {
            (screenWidth - itemWidth) / 2
        }
    }

    var currentSelectedItemIndex by remember {
        mutableIntStateOf(selectedItemIndex)
    }

    fun centralizeSelectedItem() {
        coroutineScope.launch {
            state.animateScrollToItem(
                index = (currentSelectedItemIndex + 1).coerceIn(
                    1,
                    state.layoutInfo.totalItemsCount - 2
                ),
                scrollOffset = -selectedItemOffset.toInt()
            )
        }
    }

    LazyRow(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        state = state,
    ) {
        val itemWidthDp = with(density) {
            itemWidth.toDp()
        }
        item {
            Box(
                modifier = Modifier
                    .width(itemWidthDp)
            )
        }
        itemsIndexed(items = items, key = null) { index, item ->
            var scale by remember {
                mutableFloatStateOf(1f)
            }
            var currentX by remember {
                mutableFloatStateOf(0f)
            }

            itemContent(
                Modifier
                    .pointerInteropFilter {
                        when (it.action) {
                            MotionEvent.ACTION_DOWN -> {
                                if (index == currentSelectedItemIndex) {
                                    selectedItemX = currentX
                                }
                            }
                        }
                        true
                    }
                    .onGloballyPositioned { layoutCoordinates ->
                        if (layoutCoordinates.isAttached) {
                            currentX =
                                layoutCoordinates.positionInParent().x
                        }
                        val offset = abs(currentX - selectedItemX).coerceIn(0f, 300f)
                        scale = calculateScale(offset = offset)
                    }
                    .scale(scale), item)

        }
        item {
            Box(
                modifier = Modifier
                    .width(itemWidthDp)
            )
        }
    }

    LaunchedEffect(Unit) {
        var currentItem = state.layoutInfo.visibleItemsInfo[1]
        itemWidth = currentItem.size.toFloat()

        centralizeSelectedItem()

        state.interactionSource.interactions.collect {
            if (it is DragInteraction.Stop) {
                val index =
                    if (currentItem.offset > selectedItemOffset) currentItem.index - 1 else currentItem.index + 1

                currentSelectedItemIndex = index - 1
                onSelectedItemChanged(index - 1)

                centralizeSelectedItem()
            }
            if (it is DragInteraction.Start) {
                currentItem = state.layoutInfo.visibleItemsInfo[1]
                itemWidth = currentItem.size.toFloat()
            }
        }
    }

}

fun calculateScale(
    offset1: Float = 0f,
    scale1: Float = 1f,
    offset2: Float = 300f,
    scale2: Float = 0.8f,
    offset: Float
): Float {
    // Calculate slope (m)
    val m = (scale2 - scale1) / (offset2 - offset1)

    // Calculate y-intercept (b)
    val b = scale1 - m * offset1

    // Calculate scale for the given offset
    return m * offset + b
}
