package com.maxot.seekandcatch.feature.leaderboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maxot.seekandcatch.core.designsystem.component.PixelBorderBox
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.core.designsystem.theme.Shapes
import com.maxot.seekandcatch.core.designsystem.theme.bronze
import com.maxot.seekandcatch.core.designsystem.theme.gold
import com.maxot.seekandcatch.core.designsystem.theme.silver
import com.maxot.seekandcatch.data.model.GameDifficulty
import com.maxot.seekandcatch.data.model.GameMode
import com.maxot.seekandcatch.data.model.LeaderboardRecord
import kotlinx.coroutines.launch

@Composable
fun LeaderBoardScreen(
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.leaderboardUiState.collectAsStateWithLifecycle()

    LeaderBoardScreenContent(
        leaderboardUiState = uiState,
        onSelectMode = { mode -> viewModel.onEvent(LeaderboardEvent.SelectMode(mode)) },
        onSelectDifficulty = { diff -> viewModel.onEvent(LeaderboardEvent.SelectDifficulty(diff)) }
    )
}

@Composable
private fun LeaderBoardScreenContent(
    leaderboardUiState: LeaderboardUiState,
    onSelectMode: (GameMode?) -> Unit = {},
    onSelectDifficulty: (GameDifficulty?) -> Unit = {},
) {
    val modes: List<GameMode?> = listOf<GameMode?>(null) + GameMode.entries
    val difficulties: List<GameDifficulty?> = listOf<GameDifficulty?>(null) + GameDifficulty.entries

    val pagerState = rememberPagerState(pageCount = { modes.size })
    val coroutineScope = rememberCoroutineScope()

    // Keep pager in sync with selectedMode from state
    LaunchedEffect(leaderboardUiState) {
        if (leaderboardUiState is LeaderboardUiState.Successful) {
            val state = leaderboardUiState
            val index = modes.indexOf(state.selectedMode).coerceAtLeast(0)
            if (index != pagerState.currentPage) {
                pagerState.scrollToPage(index)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Mode tabs
        TabRow(selectedTabIndex = pagerState.currentPage) {
            modes.forEachIndexed { index, mode ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        onSelectMode(mode)
                    },
                    text = { Text(text = mode?.name ?: "ALL") }
                )
            }
        }

        // Difficulty selector chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val selectedDifficulty = (leaderboardUiState as? LeaderboardUiState.Successful)?.selectedDifficulty
            difficulties.forEach { diff ->
                val label = diff?.name ?: "ALL"
                val selected = selectedDifficulty == diff
                FilterChip(
                    selected = selected,
                    onClick = { onSelectDifficulty(diff) },
                    label = { Text(text = label) }
                )
            }
        }

        // Pager pages by mode
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            val mode = modes[page]
            LaunchedEffect(mode) {
                onSelectMode(mode)
            }
            LeaderBoardPageContent(leaderboardUiState = leaderboardUiState)
        }
    }
}

@Composable
private fun LeaderBoardPageContent(
    leaderboardUiState: LeaderboardUiState,
    modifier: Modifier = Modifier
) {
    val contentDesc = stringResource(id = R.string.feature_leaderboard_screen_content_desc)
    when (leaderboardUiState) {
        is LeaderboardUiState.Failed -> {

        }

        is LeaderboardUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is LeaderboardUiState.Successful -> {
            if (leaderboardUiState.data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .then(modifier)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(id = R.string.feature_leaderboard_empty_state))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .then(modifier)
                        .semantics {
                            contentDescription = contentDesc
                        }
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(
                        items = leaderboardUiState.data,
                        key = { index: Int, _: LeaderboardRecord -> index }
                    ) { index: Int, item: LeaderboardRecord ->
                        LeaderLayout(
                            itemIndex = index, leaderRecord = item
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderLayout(
    modifier: Modifier = Modifier,
    itemIndex: Int,
    leaderRecord: LeaderboardRecord,
) {
    val borderColor = when (itemIndex) {
        0 -> gold
        1 -> silver
        2 -> bronze
        else -> Color.DarkGray
    }

    val shape = Shapes.large

    PixelBorderBox(
        modifier = Modifier
            .then(modifier)
            .padding(start = 10.dp, end = 10.dp)
            .clip(shape)
            .fillMaxWidth(),
        middleBorderColor = borderColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = ((("${itemIndex + 1}. " + leaderRecord.userName))))
            Spacer(modifier = Modifier.width(50.dp))
            Text(text = "${leaderRecord.score}")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LeaderboardScreenSuccessfulPreview() {
    val leaderRecords = listOf(
        LeaderboardRecord(userName = "Max", score = 555),
        LeaderboardRecord(userName = "John", score = 435),
        LeaderboardRecord(userName = "Piter", score = 235),
        LeaderboardRecord(userName = "Stas", score = 125),
    )
    SeekAndCatchTheme {
        LeaderBoardScreenContent(
            leaderboardUiState = LeaderboardUiState.Successful(
                data = leaderRecords,
                selectedMode = GameMode.FLOW,
                selectedDifficulty = null
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LeaderboardScreenLoadingPreview() {
    SeekAndCatchTheme {
        LeaderBoardScreenContent(leaderboardUiState = LeaderboardUiState.Loading)
    }
}
