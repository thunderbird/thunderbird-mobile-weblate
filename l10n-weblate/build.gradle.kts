plugins {
    id("tb-kmp-conventions")
    id("tb-kmp-test-conventions")
}

version = "unspecified"

kotlin {
    jvm { binaries { executable { mainClass.set("net.thunderbird.cli.l10n.weblate.MainKt") } } }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":l10n-config"))
            implementation(project(":l10n-terminal"))
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        jvmMain.dependencies { implementation(libs.ktor.client.cio) }

        nativeMain.dependencies { implementation(libs.ktor.client.darwin) }
    }
}
