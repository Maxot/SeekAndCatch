package com.maxot.seekandcatch.di

import com.maxot.seekandcatch.data.repository.ScoreRepository
import com.maxot.seekandcatch.data.repository.ScoreRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    abstract fun provideScoreRepository(impl: ScoreRepositoryImpl): ScoreRepository
}