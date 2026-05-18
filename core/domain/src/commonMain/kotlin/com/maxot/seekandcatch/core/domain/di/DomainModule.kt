package com.maxot.seekandcatch.core.domain.di

import com.maxot.seekandcatch.core.common.di.Qualifiers
import com.maxot.seekandcatch.core.domain.AuthUseCase
import com.maxot.seekandcatch.core.domain.engine.FlashGameEngine
import com.maxot.seekandcatch.core.domain.engine.FlowGameEngine
import com.maxot.seekandcatch.core.domain.flash.FlashGameUseCase
import com.maxot.seekandcatch.core.domain.flow.FlowGameUseCase
import com.maxot.seekandcatch.core.domain.user.UserUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

val domainModule = module {
    single { FlowGameEngine(get(named(Qualifiers.APPLICATION_SCOPE)), get(), get()) }
    single { FlashGameEngine(get(named(Qualifiers.APPLICATION_SCOPE)), get(), get()) }
    single { FlowGameUseCase(get(named(Qualifiers.APPLICATION_SCOPE)), get()) }
    single { FlashGameUseCase(get()) }
    single { AuthUseCase(get(), get()) }
    single { UserUseCase(get(), get()) }
}
