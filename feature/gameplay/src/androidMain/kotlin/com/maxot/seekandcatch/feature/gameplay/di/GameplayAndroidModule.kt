package com.maxot.seekandcatch.feature.gameplay.di

import com.maxot.seekandcatch.feature.gameplay.GameResultViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val gameplayAndroidModule = module {
    viewModel { params -> GameResultViewModel(params.get(), get(), get(), get(), get()) }
}
