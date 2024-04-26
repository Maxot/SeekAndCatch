package com.maxot.seekandcatch.feature.leaderboard.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.data.model.LeaderboardRecord
import com.maxot.seekandcatch.feature.leaderboard.LeaderboardViewModel
import com.maxot.seekandcatch.feature.leaderboard.LeaderboardUiState

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
    when (leaderboardUiState) {
        LeaderboardUiState.Failed -> {

        }

        LeaderboardUiState.Loading -> {
            CircularProgressIndicator(modifier = Modifier.fillMaxSize())
        }

        is LeaderboardUiState.Successful -> {
            LazyColumn(
                modifier = Modifier
                    .then(modifier)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                itemsIndexed(
                    items = leaderboardUiState.data,
//            key = { leaderRecord -> leaderRecord }
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
        1 -> Color.Gray
        2 -> Color.Gray
        else -> Color.Gray
    }


    ElevatedCard(
        modifier = Modifier
            .then(modifier)
            .padding(20.dp)
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
            horizontalArrangement = Arrangement.SpaceAround,
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
