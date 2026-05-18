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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.maxot.seekandcatch.core.common.model.GameDifficulty
import com.maxot.seekandcatch.core.common.model.GameMode
import com.maxot.seekandcatch.core.common.model.LeaderboardRecord
import com.maxot.seekandcatch.core.common.model.User
import com.maxot.seekandcatch.core.designsystem.component.PixelBorderBox
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.core.designsystem.theme.Shapes
import com.maxot.seekandcatch.core.designsystem.theme.bronze
import com.maxot.seekandcatch.core.designsystem.theme.gold
import com.maxot.seekandcatch.core.designsystem.theme.red
import com.maxot.seekandcatch.core.designsystem.theme.silver
import com.maxot.seekandcatch.feature.leaderboard.generated.resources.Res
import com.maxot.seekandcatch.feature.leaderboard.generated.resources.*

@Composable
fun LeaderBoardScreen(
    viewModel: LeaderboardViewModel = koinViewModel()
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
    val modes: List<GameMode?> = listOf<GameMode?>(null) + GameMode.entries.filter { it != GameMode.DROP }
    val difficulties: List<GameDifficulty?> = listOf<GameDifficulty?>(null) + GameDifficulty.entries

    Column(modifier = Modifier.fillMaxSize()) {
        val selectedMode = (leaderboardUiState as? LeaderboardUiState.Successful)?.selectedMode
        val selectedIndex = modes.indexOf(selectedMode).coerceAtLeast(0)

        // Mode tabs
        TabRow(selectedTabIndex = selectedIndex) {
            modes.forEachIndexed { index, mode ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = {
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
            val selectedDifficulty =
                (leaderboardUiState as? LeaderboardUiState.Successful)?.selectedDifficulty
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

        // Content
        LeaderBoardPageContent(
            leaderboardUiState = leaderboardUiState,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LeaderBoardPageContent(
    leaderboardUiState: LeaderboardUiState,
    modifier: Modifier = Modifier
) {
    val contentDesc = stringResource(Res.string.feature_leaderboard_screen_content_desc)
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
                    Text(text = stringResource(Res.string.feature_leaderboard_empty_state))
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
                            itemIndex = index,
                            leaderRecord = item,
                            user = leaderboardUiState.userData
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
    user: User? = null,
    itemIndex: Int,
    leaderRecord: LeaderboardRecord,
) {
    val borderColor = if (user?.id == leaderRecord.userId) {
        red
    } else {
        when (itemIndex) {
            0 -> gold
            1 -> silver
            2 -> bronze
            else -> Color.DarkGray
        }
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
