package com.maxot.singleselectionlazyrow

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.Text
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun <T> SingleSelectionLazyRow(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    scaleParams: ScaleParams = ScaleParams(),
    items: List<T>,
    selectedItemIndex: Int = 0,
    onSelectedItemChanged: (Int) -> Unit,
    itemContent: @Composable (Modifier, T) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    var parentWidth by remember {
        mutableFloatStateOf(0f)
    }
    var itemWidth by remember {
        mutableFloatStateOf(0f)
    }

    val selectedItemOffset by remember {
        derivedStateOf {
            (parentWidth - itemWidth) / 2
        }
    }

    var currentSelectedItemIndex by remember {
        mutableIntStateOf(selectedItemIndex.coerceIn(items.indices))
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

    LaunchedEffect(key1 = selectedItemIndex) {
        currentSelectedItemIndex = selectedItemIndex
        centralizeSelectedItem()
    }

    LazyRow(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .onGloballyPositioned { layoutCoordinates ->
                val newWidth = layoutCoordinates.size.width.toFloat()
                if (parentWidth != newWidth) {
                    parentWidth = newWidth
                }
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        state = state,
    ) {
        val itemWidthDp = with(density) {
            itemWidth.toDp()
        }
        item {
            Spacer(
                modifier = Modifier
                    .width(itemWidthDp)
            )
        }
        itemsIndexed(items = items, key = { index, item -> item.toString() }) { index, item ->
            var scale by remember {
                mutableFloatStateOf(1f)
            }
            var currentX by remember {
                mutableFloatStateOf(0f)
            }

            itemContent(
                Modifier
                    .onGloballyPositioned { layoutCoordinates ->
                        if (layoutCoordinates.isAttached) {
                            currentX =
                                layoutCoordinates.positionInParent().x
                        }

                        val offset =
                            abs(currentX - selectedItemOffset).coerceIn(0f, selectedItemOffset)
                        scale = calculateScale(scaleParams, currentOffset = offset)
                    }
                    .scale(scale), item)

        }
        item {
            Spacer(
                modifier = Modifier
                    .width(itemWidthDp)
            )
        }
    }

    LaunchedEffect(Unit) {
        val visibleItems = state.layoutInfo.visibleItemsInfo
        var currentItem = visibleItems.getOrNull(1) // Safely access visible items
        currentItem?.let {
            itemWidth = it.size.toFloat()
            centralizeSelectedItem()
        }

        state.interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is DragInteraction.Stop -> {
                    val newIndex = if ((currentItem?.offset ?: 0) > selectedItemOffset) {
                        (currentItem?.index ?: 1) - 1
                    } else {
                        (currentItem?.index ?: 1) + 1
                    }

                    currentSelectedItemIndex = (newIndex - 1).coerceIn(items.indices)
                    onSelectedItemChanged(currentSelectedItemIndex)
                    centralizeSelectedItem()
                }

                is DragInteraction.Start -> {
                    currentItem = state.layoutInfo.visibleItemsInfo.getOrNull(1)
                    currentItem?.let {
                        itemWidth = it.size.toFloat()
                    }
                }

            }
        }
    }

}

/**
 * Scale param used for calculate items scale depending of their offset.
 * @param offset1
 * @param scale1
 * @param offset2
 * @param scale2
 */
data class ScaleParams(
    val offset1: Float = 0f,
    val scale1: Float = 1f,
    val offset2: Float = 300f,
    val scale2: Float = 0.8f,
)

fun calculateScale(
    scaleParams: ScaleParams,
    currentOffset: Float
): Float {
    val offset1 = scaleParams.offset1
    val scale1 = scaleParams.scale1
    val offset2 = scaleParams.offset2
    val scale2 = scaleParams.scale2
    // Calculate slope (m)
    val m = (scale2 - scale1) / (offset2 - offset1)

    // Calculate y-intercept (b)
    val b = scale1 - m * offset1

    // Calculate scale for the given offset
    return m * currentOffset + b
}

@Preview
@Composable
fun <T> SingleSelectionLazyRowPreview() {
    SingleSelectionLazyRow(
        modifier = Modifier.fillMaxSize(),
        items = listOf("a", "b", "c"),
        onSelectedItemChanged = { item ->

        }
    ) { modifier, item ->
        Card(
            modifier = Modifier
                .height(350.dp)
                .width(250.dp)
                .padding(20.dp)
                .background(Color.Red)
        ) {
            Text(
                text = item,
            )
        }
    }

}
