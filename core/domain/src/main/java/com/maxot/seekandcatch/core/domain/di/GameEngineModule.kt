package com.maxot.seekandcatch.core.domain.di

import com.maxot.seekandcatch.core.common.di.ApplicationScope
import com.maxot.seekandcatch.core.domain.engine.FlashGameEngine
import com.maxot.seekandcatch.core.domain.engine.FlowGameEngine
import com.maxot.seekandcatch.data.repository.FiguresRepository
import com.maxot.seekandcatch.data.repository.GoalsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GameEngineModule {

    @Provides
    @Singleton
    fun provideFlowGameEngine(
        @ApplicationScope coroutineScope: CoroutineScope,
        figuresRepository: FiguresRepository,
        goalsRepository: GoalsRepository
    ): FlowGameEngine {
        return FlowGameEngine(coroutineScope, figuresRepository, goalsRepository)
    }

    @Provides
    @Singleton
    fun provideFlashGameEngine(
        @ApplicationScope coroutineScope: CoroutineScope,
        figuresRepository: FiguresRepository,
        goalsRepository: GoalsRepository
    ): FlashGameEngine {
        return FlashGameEngine(coroutineScope, figuresRepository, goalsRepository)
    }
}
