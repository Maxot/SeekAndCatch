package com.maxot.seekandcatch.core.domain.di

import com.maxot.seekandcatch.core.common.di.ApplicationScope
import com.maxot.seekandcatch.core.domain.AuthUseCase
import com.maxot.seekandcatch.core.domain.engine.FlashGameEngine
import com.maxot.seekandcatch.core.domain.engine.FlowGameEngine
import com.maxot.seekandcatch.core.domain.flash.FlashGameUseCase
import com.maxot.seekandcatch.core.domain.flow.FlowGameUseCase
import com.maxot.seekandcatch.core.domain.user.UserUseCase
import com.maxot.seekandcatch.data.repository.AuthRepository
import com.maxot.seekandcatch.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(SingletonComponent::class)
object DomainHiltModule {

    @Provides
    fun provideFlowGameUseCase(
        @ApplicationScope coroutineScope: CoroutineScope,
        flowGameEngine: FlowGameEngine
    ): FlowGameUseCase = FlowGameUseCase(coroutineScope, flowGameEngine)

    @Provides
    fun provideFlashGameUseCase(
        flashGameEngine: FlashGameEngine
    ): FlashGameUseCase = FlashGameUseCase(flashGameEngine)

    @Provides
    fun provideAuthUseCase(
        authRepository: AuthRepository,
        userRepository: UserRepository
    ): AuthUseCase = AuthUseCase(authRepository, userRepository)

    @Provides
    fun provideUserUseCase(
        userRepository: UserRepository,
        authRepository: AuthRepository
    ): UserUseCase = UserUseCase(userRepository, authRepository)
}
