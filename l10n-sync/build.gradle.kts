import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("tb-kmp-conventions")
    id("tb-kmp-test-conventions")
}

version = providers.gradleProperty("releaseVersion").getOrElse("unspecified")

kotlin {
    jvm { binaries { executable { mainClass.set("net.thunderbird.cli.l10n.sync.MainKt") } } }

    targets.withType<KotlinNativeTarget>().configureEach {
        binaries { executable { entryPoint = "net.thunderbird.cli.l10n.sync.main" } }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":l10n-config"))
            implementation(project(":l10n-terminal"))
            implementation(libs.xmlutil.serialization)
        }
    }
}
