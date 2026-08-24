import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("tb-kmp-conventions")
    id("tb-kmp-test-conventions")
}

version = providers.gradleProperty("releaseVersion").getOrElse("unspecified")

kotlin {
    jvm { binaries { executable { mainClass.set("net.thunderbird.cli.l10n.weblate.MainKt") } } }

    targets.withType<KotlinNativeTarget>().configureEach {
        binaries { executable { entryPoint = "net.thunderbird.cli.l10n.weblate.main" } }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":l10n-config"))
            implementation(project(":l10n-terminal"))
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        jvmMain.dependencies { runtimeOnly(libs.logback.classic) }
    }
}
