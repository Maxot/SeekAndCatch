package com.maxot.seekandcatch.feature.score.di

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
    fun bindScoreRepository(impl: ScoreRepositoryImpl): ScoreRepository

}
