package com.maxot.seekandcatch.feature.gameplay.di

import com.maxot.seekandcatch.core.common.VisualFeedbackManager
import com.maxot.seekandcatch.feature.gameplay.GameResultViewModel
import com.maxot.seekandcatch.feature.gameplay.gameselection.GameSelectionViewModel
import com.maxot.seekandcatch.feature.gameplay.ui.flashgame.FlashGameViewModel
import com.maxot.seekandcatch.feature.gameplay.ui.flowgame.FlowGameViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val gameplayModule = module {
    single { VisualFeedbackManager() }
    viewModel { FlowGameViewModel(get(), get(), get(), get(), get()) }
    viewModel { FlashGameViewModel(get(), get(), get(), get(), get()) }
    viewModel { GameSelectionViewModel(get(), get()) }
    viewModel { params -> GameResultViewModel(params.get(), get(), get(), get(), get()) }
}
