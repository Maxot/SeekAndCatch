package com.maxot.seekandcatch.data.di

import com.maxot.seekandcatch.data.firebase.datasource.LeaderboardDataSource
import com.maxot.seekandcatch.data.firebase.datasource.LeaderboardFirestoreDataSource
import com.maxot.seekandcatch.data.firebase.datasource.UserDataSource
import com.maxot.seekandcatch.data.firebase.datasource.UserFirestoreDataSource
import com.maxot.seekandcatch.data.repository.AuthRepository
import com.maxot.seekandcatch.data.repository.AuthRepositoryImpl
import com.maxot.seekandcatch.data.repository.ColorsRepository
import com.maxot.seekandcatch.data.repository.ColorsRepositoryImpl
import com.maxot.seekandcatch.data.repository.FiguresRepository
import com.maxot.seekandcatch.data.repository.FiguresRepositoryImpl
import com.maxot.seekandcatch.data.repository.GoalsRepository
import com.maxot.seekandcatch.data.repository.GoalsRepositoryImpl
import com.maxot.seekandcatch.data.repository.LeaderboardRepository
import com.maxot.seekandcatch.data.repository.LeaderboardRepositoryImpl
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.data.repository.SettingsRepositoryImpl
import com.maxot.seekandcatch.data.repository.UserRepository
import com.maxot.seekandcatch.data.repository.impl.UserRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.persistentCacheSettings
import com.google.firebase.ktx.Firebase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    fun bindFiguresRepository(impl: FiguresRepositoryImpl): FiguresRepository

    @Binds
    fun bindColorsRepository(impl: ColorsRepositoryImpl): ColorsRepository

    @Binds
    fun bindGoalsRepository(impl: GoalsRepositoryImpl): GoalsRepository

    @Binds
    fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    fun bindLeaderboardRepository(impl: LeaderboardRepositoryImpl): LeaderboardRepository

    @Binds
    fun bindLeaderboardDataSource(impl: LeaderboardFirestoreDataSource): LeaderboardDataSource

    @Binds
    fun bindUserDataSource(impl: UserFirestoreDataSource): UserDataSource

    @Binds
    fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirestore(): FirebaseFirestore {
            val settings = firestoreSettings {
                setLocalCacheSettings(persistentCacheSettings {})
            }
            return Firebase.firestore.apply { firestoreSettings = settings }
        }
    }
}