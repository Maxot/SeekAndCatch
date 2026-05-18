package com.maxot.seekandcatch.feature.leaderboard.di

import com.maxot.seekandcatch.feature.leaderboard.LeaderboardViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val leaderboardModule = module {
    viewModel { LeaderboardViewModel(get(), get()) }
}
