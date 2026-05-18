package com.maxot.seekandcatch.feature.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class SCLocaleManager : LocaleController {

    private val allSupportedLocales = listOf("en-US", "uk")

    override fun setLocale(languageTag: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageTag)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    override fun getLocales(): List<String> {
        val locales: MutableSet<String> = mutableSetOf()
        locales.add(AppCompatDelegate.getApplicationLocales().toLanguageTags())
        locales.addAll(allSupportedLocales)
        return locales.toList()
    }

    override fun getSelectedLocaleTag(): String? =
        AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag()
}