plugins {
  alias(libs.plugins.application)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.power.assert)
  alias(libs.plugins.testBalloon)
}

version = "unspecified"

application { mainClass.set("net.thunderbird.cli.importer.MainKt") }

tasks.test {
  useJUnitPlatform()
}

dependencies {
  implementation(project(":l10n-tools-config"))
  implementation(libs.logback.classic)
  implementation(libs.mosaic.runtime)

  testImplementation(libs.testBalloon.core.jvm)
  testImplementation(libs.kotlin.test)
}
