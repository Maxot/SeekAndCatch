package com.maxot.seekandcatch.feature.leaderboard.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.data.model.LeaderboardRecord
import com.maxot.seekandcatch.feature.leaderboard.LeaderboardUiState
import com.maxot.seekandcatch.feature.leaderboard.LeaderboardViewModel
import com.maxot.seekandcatch.feature.leaderboard.R

@Composable
fun LeaderBoardScreenRoute(
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val leaderboardUiState by viewModel.leaderboardUiState.collectAsStateWithLifecycle()

    LeaderBoardScreen(leaderboardUiState = leaderboardUiState)
}

@Composable
fun LeaderBoardScreen(
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
                verticalArrangement = Arrangement.Center
            ) {
                itemsIndexed(
                    items = leaderboardUiState.data,
                    key = { index: Int, item: LeaderboardRecord -> index }
                ) { index: Int, item: LeaderboardRecord ->
                    LeaderLayout(
                        itemIndex = index, leaderRecord = item
                    )
                }
            }
        }
    }

}

@Composable
fun LeaderLayout(
    modifier: Modifier = Modifier,
    itemIndex: Int,
    leaderRecord: LeaderboardRecord,
) {
    val borderColor = when (itemIndex) {
        0 -> Color.Yellow
        1 -> Color.LightGray
        2 -> Color.Gray
        else -> Color.White
    }

    ElevatedCard(
        modifier = Modifier
            .then(modifier)
            .padding(start = 10.dp, top = 10.dp, end = 10.dp)
            .fillMaxWidth()
            .border(width = 3.dp, color = borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
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

@Preview
@Composable
fun LeaderboardScreenSuccessfulPreview() {
    val leaderRecords = listOf(
        LeaderboardRecord(userName = "Max", score = 555),
        LeaderboardRecord(userName = "John", score = 435),
        LeaderboardRecord(userName = "Piter", score = 235),
        LeaderboardRecord(userName = "Stas", score = 125),
    )
    SeekAndCatchTheme {
        LeaderBoardScreen(leaderboardUiState = LeaderboardUiState.Successful(leaderRecords))
    }
}

@Preview
@Composable
fun LeaderboardScreenLoadingPreview() {
    SeekAndCatchTheme {
        LeaderBoardScreen(leaderboardUiState = LeaderboardUiState.Loading)
    }
}
