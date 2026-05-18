package com.maxot.seekandcatch

import android.app.Application
import com.maxot.seekandcatch.di.appModule
import com.maxot.seekandcatch.core.common.di.coroutineScopesModule
import com.maxot.seekandcatch.core.common.di.dispatchersModule
import com.maxot.seekandcatch.core.domain.di.domainModule
import com.maxot.seekandcatch.data.di.dataAndroidModule
import com.maxot.seekandcatch.feature.account.di.accountModule
import com.maxot.seekandcatch.feature.gameplay.di.gameplayAndroidModule
import com.maxot.seekandcatch.feature.gameplay.di.gameplayModule
import com.maxot.seekandcatch.feature.leaderboard.di.leaderboardModule
import com.maxot.seekandcatch.feature.settings.di.settingsAndroidModule
import com.maxot.seekandcatch.feature.settings.di.settingsModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SeekCatchApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@SeekCatchApplication)
            modules(
                dispatchersModule,
                coroutineScopesModule,
                dataAndroidModule,
                domainModule,
                accountModule,
                leaderboardModule,
                settingsModule,
                settingsAndroidModule,
                gameplayModule,
                gameplayAndroidModule,
                appModule,
            )
        }
    }
}
