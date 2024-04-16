package com.maxot.seekandcatch.data.test.repository.di

import com.maxot.seekandcatch.data.di.DataModule
import com.maxot.seekandcatch.data.repository.FiguresRepository
import com.maxot.seekandcatch.data.repository.GoalsRepository
import com.maxot.seekandcatch.data.repository.ScoreRepository
import com.maxot.seekandcatch.data.test.repository.FakeFiguresRepository
import com.maxot.seekandcatch.data.test.repository.FakeGoalsRepository
import com.maxot.seekandcatch.data.test.repository.FakeScoreRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataModule::class],
)
interface TestDataModule {
    @Binds
    fun bindScoreRepository(impl: FakeScoreRepository): ScoreRepository

    @Binds
    fun bindFiguresRepository(impl: FakeFiguresRepository): FiguresRepository

    @Binds
    fun bindGoalsRepository(impl: FakeGoalsRepository): GoalsRepository

}