package com.maxot.seekandcatch.di

import com.maxot.seekandcatch.feature.gameplay.data.repository.FiguresRepository
import com.maxot.seekandcatch.feature.gameplay.data.repository.FiguresRepositoryImpl
import com.maxot.seekandcatch.feature.score.data.repository.ScoreRepository
import com.maxot.seekandcatch.feature.score.data.repository.ScoreRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    fun provideScoreRepository(impl: ScoreRepositoryImpl): ScoreRepository

    @Binds
    fun provideFiguresRepository(impl: FiguresRepositoryImpl): FiguresRepository
}
