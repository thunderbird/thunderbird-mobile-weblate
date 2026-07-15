import org.gradle.api.Plugin
import org.gradle.api.Project
import dev.detekt.gradle.extensions.DetektExtension
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpConventionsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("org.jetbrains.kotlin.plugin.serialization")
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("com.ncorti.ktfmt.gradle")
                apply("org.jetbrains.kotlinx.kover")
                apply("dev.detekt")
            }

            extensions.configure<KotlinMultiplatformExtension>("kotlin") {
                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }

                jvm()
                configureHostNativeTarget()

                sourceSets.commonMain.dependencies {
                    api(libs.kotlinx.io.core)
                    implementation(libs.kotlinx.serialization.json)
                    implementation(libs.mosaic.runtime)
                }
            }

            extensions.configure<com.ncorti.ktfmt.gradle.KtfmtExtension>("ktfmt") {
                kotlinLangStyle()
            }

            extensions.configure<DetektExtension>("detekt") {
                buildUponDefaultConfig.set(true)
                parallel.set(true)
                basePath.set(rootDir)
                config.from(rootProject.layout.projectDirectory.file("config/detekt/detekt.yml"))
                source.from(
                    "src/commonMain/kotlin",
                    "src/commonTest/kotlin",
                    "src/jvmMain/kotlin",
                    "src/jvmTest/kotlin",
                    "src/nativeMain/kotlin",
                    "src/nativeTest/kotlin",
                    "src/main/kotlin",
                    "src/test/kotlin",
                )
            }

            extensions.configure<KoverProjectExtension>("kover") {
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

            tasks.named("check") {
                dependsOn("detekt")
                dependsOn("ktfmtCheck")
            }
        }
    }

    private fun KotlinMultiplatformExtension.configureHostNativeTarget() {
        val os = System.getProperty("os.name").lowercase()
        val arch = System.getProperty("os.arch").lowercase()

        when {
            os.contains("mac") && arch.contains("aarch64") -> macosArm64()
            os.contains("mac") -> macosX64()
            os.contains("linux") -> linuxX64()
            os.contains("windows") -> mingwX64()
        }
    }
}
