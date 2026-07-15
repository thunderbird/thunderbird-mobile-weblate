plugins {
    alias(libs.plugins.application)
    id("thunderbird.kotlin-jvm-conventions")
    id("thunderbird.kotlin-test-conventions")
}

version = "unspecified"

application { mainClass.set("net.thunderbird.cli.weblate.MainKt") }

dependencies {
    implementation(project(":l10n-tools-config"))
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.logback.classic)
}
