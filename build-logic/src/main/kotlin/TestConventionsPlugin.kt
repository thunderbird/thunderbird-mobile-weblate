import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

class TestConventionsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("de.infix.testBalloon")
            }

            dependencies.add("testImplementation", libs.testBalloon.core.jvm)
            dependencies.add("testImplementation", libs.kotlin.test)

            tasks.withType<Test>().configureEach {
                useJUnitPlatform()
            }
        }
    }
}
