import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpTestConventionsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("de.infix.testBalloon")
            }

            extensions.configure<KotlinMultiplatformExtension>("kotlin") {
                sourceSets.commonTest.dependencies {
                    implementation(libs.kotlin.test)
                    implementation(libs.testBalloon.core)
                }
            }

            tasks.withType<Test>().configureEach {
                useJUnitPlatform()
            }
        }
    }
}
