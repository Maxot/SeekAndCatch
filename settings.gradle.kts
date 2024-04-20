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
include(":core:domain")
include(":core:designsystem")
include(":data")
include(":data-test")

include(":feature:gameplay")
include(":feature:score")
include(":feature:settings")
include(":feature:leaderboard")
