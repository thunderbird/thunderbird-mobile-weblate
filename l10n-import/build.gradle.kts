plugins {
    alias(libs.plugins.application)
    id("thunderbird.kotlin-jvm-conventions")
    id("thunderbird.kotlin-test-conventions")
}

version = "unspecified"

application { mainClass.set("net.thunderbird.cli.importer.MainKt") }

dependencies {
    implementation(project(":l10n-tools-config"))
    implementation(libs.logback.classic)
}
