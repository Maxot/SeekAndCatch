package com.maxot.seekandcatch.feature.leaderboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.core.designsystem.theme.Shapes
import com.maxot.seekandcatch.core.designsystem.theme.bronze
import com.maxot.seekandcatch.core.designsystem.theme.gold
import com.maxot.seekandcatch.core.designsystem.theme.silver
import com.maxot.seekandcatch.core.designsystem.ui.PixelBorderBox
import com.maxot.seekandcatch.data.model.LeaderboardRecord
import com.maxot.seekandcatch.feature.leaderboard.LeaderboardUiState
import com.maxot.seekandcatch.feature.leaderboard.LeaderboardViewModel
import com.maxot.seekandcatch.feature.leaderboard.R

@Composable
fun LeaderBoardScreen(
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val leaderboardUiState by viewModel.leaderboardUiState.collectAsStateWithLifecycle()

    LeaderBoardScreenContent(leaderboardUiState = leaderboardUiState)
}

@Composable
private fun LeaderBoardScreenContent(
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

                item {
                    Spacer(modifier = Modifier.height(10.dp))
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
        LeaderBoardScreenContent(leaderboardUiState = LeaderboardUiState.Successful(leaderRecords))
    }
}

@Preview(showBackground = true)
@Composable
private fun LeaderboardScreenLoadingPreview() {
    SeekAndCatchTheme {
        LeaderBoardScreenContent(leaderboardUiState = LeaderboardUiState.Loading)
    }
}
