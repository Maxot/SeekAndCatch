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
include(":core:designsystem")
include(":data-test")
include(":feature:gameplay")
include(":feature:score")
include(":feature:settings")
include(":data")
include(":core:domain")
