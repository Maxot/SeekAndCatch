package com.maxot.seekandcatch.di

import com.maxot.seekandcatch.MainViewModel
import com.maxot.seekandcatch.MusicController
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { MusicController(get()) }
    viewModel { MainViewModel(get(), get(), get()) }
}
