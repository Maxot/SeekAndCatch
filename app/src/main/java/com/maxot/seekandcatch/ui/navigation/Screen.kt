package com.maxot.seekandcatch.ui.navigation

sealed class Screen(val route: String) {
    object MainScreen: Screen(mainScreen)
    object ScoreScreen: Screen(scoreScreen)
    object FlowGameScreen: Screen(flowGameScreen)
    object FrameGameScreen: Screen(frameGameScreen)

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
        private const val flowGameScreen = "flow_screen"
        private const val frameGameScreen = "frame_screen"
    }
}
