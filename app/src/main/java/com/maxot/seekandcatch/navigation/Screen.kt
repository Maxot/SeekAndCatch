package com.maxot.seekandcatch.navigation

sealed class Screen(val route: String) {
    object MainScreen : Screen(mainScreen)
    object ScoreScreen : Screen(scoreScreen)
    object GameScreen : Screen(gameScreen)
    object SettingsScreen : Screen(settingsScreen)

    fun withArgs(vararg args: String): String{
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }

    companion object {
        private const val mainScreen = "main_screen"
        private const val scoreScreen = "score_screen"
        private const val gameScreen = "game_screen"
        private const val settingsScreen = "settings_screen"
    }
}
