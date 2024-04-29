package com.maxot.seekandcatch.data.di

import com.maxot.seekandcatch.data.firebase.datasource.LeaderboardDataSource
import com.maxot.seekandcatch.data.firebase.datasource.LeaderboardFirestoreDataSource
import com.maxot.seekandcatch.data.repository.AccountRepository
import com.maxot.seekandcatch.data.repository.AccountRepositoryImpl
import com.maxot.seekandcatch.data.repository.FiguresRepository
import com.maxot.seekandcatch.data.repository.FiguresRepositoryImpl
import com.maxot.seekandcatch.data.repository.GoalsRepository
import com.maxot.seekandcatch.data.repository.GoalsRepositoryImpl
import com.maxot.seekandcatch.data.repository.LeaderboardRepository
import com.maxot.seekandcatch.data.repository.LeaderboardRepositoryImpl
import com.maxot.seekandcatch.data.repository.ScoreRepository
import com.maxot.seekandcatch.data.repository.ScoreRepositoryImpl
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.data.repository.SettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    fun bindScoreRepository(impl: ScoreRepositoryImpl): ScoreRepository

    @Binds
    fun bindFiguresRepository(impl: FiguresRepositoryImpl): FiguresRepository

    @Binds
    fun bindGoalsRepository(impl: GoalsRepositoryImpl): GoalsRepository

    @Binds
    fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    fun bindLeaderboardRepository(impl: LeaderboardRepositoryImpl): LeaderboardRepository

    @Binds
    fun bindLeaderboardDataSource(impl: LeaderboardFirestoreDataSource): LeaderboardDataSource

}