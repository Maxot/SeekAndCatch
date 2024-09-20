package com.maxot.seekandcatch.feature.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SCLocaleManager
@Inject constructor() {

    private val allSupportedLocales = listOf("en-US", "uk")
    fun setLocale(languageTag: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageTag)
        // Call this on the main thread as it may require Activity.restart()
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun getLocales(): List<String> {
        val locales: MutableSet<String> = mutableSetOf()
        locales.add(AppCompatDelegate.getApplicationLocales().toLanguageTags())
        locales.addAll(allSupportedLocales)
        return locales.toList()
    }


    fun getSelectedLocale() = AppCompatDelegate.getApplicationLocales()[0]
}