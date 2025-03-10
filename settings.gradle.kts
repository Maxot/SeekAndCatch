pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "SeekAndCatch"

include(":app")
include(":core:common")
include(":core:domain")
include(":core:designsystem")
include(":core:model")
include(":data")
include(":data-test")

include(":feature:gameplay")
include(":feature:settings")
include(":feature:leaderboard")
include(":feature:account")
include(":feature:colorpicker")
include(":singleselectionlazyrow")
