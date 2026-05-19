package com.maxot.seekandcatch.core.common.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dispatchersModule = module {
    single<CoroutineDispatcher>(named(Qualifiers.DEFAULT_DISPATCHER)) { Dispatchers.Default }
    single<CoroutineDispatcher>(named(Qualifiers.IO_DISPATCHER)) { Dispatchers.Default }
    single<CoroutineDispatcher>(named(Qualifiers.MAIN_DISPATCHER)) { Dispatchers.Main }
}

val coroutineScopesModule = module {
    single<CoroutineScope>(named(Qualifiers.APPLICATION_SCOPE)) {
        CoroutineScope(SupervisorJob() + get<CoroutineDispatcher>(named(Qualifiers.DEFAULT_DISPATCHER)))
    }
}
