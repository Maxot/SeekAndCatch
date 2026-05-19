package com.maxot.seekandcatch.di

import com.maxot.seekandcatch.core.common.di.coroutineScopesModule
import com.maxot.seekandcatch.core.common.di.dispatchersModule
import com.maxot.seekandcatch.core.domain.di.domainModule
import com.maxot.seekandcatch.data.iosDataModule
import com.maxot.seekandcatch.feature.account.di.accountModule
import com.maxot.seekandcatch.feature.gameplay.di.gameplayModule
import com.maxot.seekandcatch.feature.leaderboard.di.leaderboardModule
import com.maxot.seekandcatch.feature.settings.di.settingsModule
import com.maxot.seekandcatch.ui.iosSettingsModule
import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(
            dispatchersModule,
            coroutineScopesModule,
            iosDataModule,
            domainModule,
            accountModule,
            leaderboardModule,
            settingsModule,
            iosSettingsModule,
            gameplayModule,
        )
    }
}
