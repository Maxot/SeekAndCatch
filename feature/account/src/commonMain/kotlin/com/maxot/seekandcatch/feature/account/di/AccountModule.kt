package com.maxot.seekandcatch.feature.account.di

import com.maxot.seekandcatch.feature.account.AccountViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val accountModule = module {
    viewModel { AccountViewModel(get(), get()) }
}
