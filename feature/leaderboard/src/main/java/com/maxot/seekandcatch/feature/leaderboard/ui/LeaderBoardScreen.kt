package com.maxot.seekandcatch.feature.leaderboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.data.model.Leader
import com.maxot.seekandcatch.feature.leaderboard.LeaderBoardViewModel

@Composable
fun LeaderBoardScreenRoute(
    viewModel: LeaderBoardViewModel = hiltViewModel()
) {
    val leaders by viewModel.leaders.collectAsStateWithLifecycle()

    LeaderBoardScreen(leaders = leaders)
}

@Composable
fun LeaderBoardScreen(
    leaders: List<Leader>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        items(
            items = leaders,
            key = { leader -> leader.userId }
        ) {
            LeaderLayout(leader = it)
        }
    }
}

@Composable
fun LeaderLayout(
    modifier: Modifier = Modifier,
    leader: Leader,
) {
    ElevatedCard(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .padding(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = leader.name)
            Spacer(modifier = Modifier.width(50.dp))
            Text(text = " with Best result: ${leader.score}")
        }
    }
}

@Preview
@Composable
fun LeaderBoardScreenPreview() {
    val leaders = listOf(
        Leader(userId = 1, name = "Max", score = 555),
        Leader(userId = 2, name = "John", score = 435),
        Leader(userId = 3, name = "Piter", score = 235),
        Leader(userId = 4, name = "Stas", score = 125),
    )
    SeekAndCatchTheme {
        LeaderBoardScreen(leaders = leaders)
    }
}