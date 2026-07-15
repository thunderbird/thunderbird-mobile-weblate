plugins {
    id("tb-kmp-conventions")
    id("tb-kmp-test-conventions")
}

version = "unspecified"

kotlin {
    jvm { binaries { executable { mainClass.set("net.thunderbird.cli.l10n.sync.MainKt") } } }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":l10n-config"))
            implementation(project(":l10n-terminal"))
            implementation(libs.xmlutil.serialization)
        }
    }
}
