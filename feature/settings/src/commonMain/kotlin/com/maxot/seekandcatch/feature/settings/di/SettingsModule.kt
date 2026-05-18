package com.maxot.seekandcatch.feature.settings.di

import com.maxot.seekandcatch.feature.settings.ui.SettingsViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel { SettingsViewModel(get(), get(), get(), get()) }
}
