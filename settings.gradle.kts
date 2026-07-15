pluginManagement {
    includeBuild("build-logic")

    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "thunderbird-mobile-weblate"

include(":l10n-config")
include(":l10n-terminal")
include(":l10n-sync")
include(":l10n-weblate")
