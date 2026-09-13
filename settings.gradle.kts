pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "equilibrio"

include(":app")
include(":core-ui")
include(":core-domain")
include(":core-data")
include(":feature-home")
include(":feature-entry")
