pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
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
include(":feature-goals")
include(":feature-reports")
