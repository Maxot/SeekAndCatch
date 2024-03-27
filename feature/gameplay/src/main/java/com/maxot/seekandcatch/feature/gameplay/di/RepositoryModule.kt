package com.maxot.seekandcatch.feature.gameplay.di

import com.maxot.seekandcatch.feature.gameplay.data.repository.FiguresRepository
import com.maxot.seekandcatch.feature.gameplay.data.repository.FiguresRepositoryImpl
import com.maxot.seekandcatch.feature.gameplay.data.repository.GoalsRepository
import com.maxot.seekandcatch.feature.gameplay.data.repository.GoalsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    fun bindFiguresRepository(impl: FiguresRepositoryImpl): FiguresRepository

    @Binds
    fun bindGoalsRepository(impl: GoalsRepositoryImpl): GoalsRepository
}