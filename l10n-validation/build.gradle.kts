plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    alias(libs.plugins.ktfmt)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
}

version = providers.gradleProperty("releaseVersion").getOrElse("unspecified")

kotlin { jvmToolchain(21) }

application { mainClass.set("net.thunderbird.cli.l10n.MainKt") }

dependencies {
    implementation(libs.clikt)
    testImplementation(libs.assertk)
    testImplementation(kotlin("test"))
}

ktfmt { kotlinLangStyle() }

detekt {
    buildUponDefaultConfig.set(true)
    parallel.set(true)
    basePath.set(rootDir)
    config.from(rootProject.layout.projectDirectory.file("config/detekt/detekt.yml"))
    source.from("src/main/kotlin", "src/test/kotlin")
}

kover {
    reports {
        total {
            verify {
                onCheck.set(true)
                warningInsteadOfFailure.set(false)
                rule("Minimum line coverage") { minBound(60) }
            }
        }
    }
}

tasks.test { useJUnitPlatform() }

tasks.check {
    dependsOn("detekt")
    dependsOn("ktfmtCheck")
}
