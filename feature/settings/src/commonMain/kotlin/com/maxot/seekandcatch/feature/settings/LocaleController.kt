package com.maxot.seekandcatch.feature.settings

interface LocaleController {
    fun setLocale(languageTag: String)
    fun getLocales(): List<String>
    fun getSelectedLocaleTag(): String?
}
