plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.serialization.gradle.plugin)
    implementation(libs.kotlin.compose.gradle.plugin)
    implementation(libs.ktfmt.gradle.plugin)
    implementation(libs.kover.gradle.plugin)
    implementation(libs.testBalloon.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("kotlinJvmConventions") {
            id = "thunderbird.kotlin-jvm-conventions"
            implementationClass = "JvmConventionsPlugin"
        }
        register("kotlinTestConventions") {
            id = "thunderbird.kotlin-test-conventions"
            implementationClass = "TestConventionsPlugin"
        }
    }
}
