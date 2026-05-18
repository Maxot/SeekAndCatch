package com.maxot.seekandcatch.data.di

import com.maxot.seekandcatch.core.common.di.Qualifiers
import com.maxot.seekandcatch.data.datastore.AccountDataStore
import com.maxot.seekandcatch.data.datastore.SettingsDataStore
import com.maxot.seekandcatch.data.firebase.datasource.LeaderboardDataSource
import com.maxot.seekandcatch.data.firebase.datasource.LeaderboardFirestoreDataSource
import com.maxot.seekandcatch.data.firebase.datasource.UserDataSource
import com.maxot.seekandcatch.data.firebase.datasource.UserFirestoreDataSource
import com.maxot.seekandcatch.data.firebase.datasource.auth.FirebaseAuthDataSource
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
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dataAndroidModule = module {
    single { SettingsDataStore(androidContext()) }
    single { AccountDataStore(androidContext()) }
    single { FirebaseAuthDataSource() }
    single<LeaderboardDataSource> { LeaderboardFirestoreDataSource() }
    single<UserDataSource> { UserFirestoreDataSource() }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<ColorsRepository> { ColorsRepositoryImpl(get()) }
    single<FiguresRepository> { FiguresRepositoryImpl(get(), get(named(Qualifiers.APPLICATION_SCOPE))) }
    single<GoalsRepository> { GoalsRepositoryImpl(get()) }
    single<LeaderboardRepository> { LeaderboardRepositoryImpl(get(named(Qualifiers.APPLICATION_SCOPE)), get(), get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
}
