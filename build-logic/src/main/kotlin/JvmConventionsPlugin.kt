import org.gradle.api.Plugin
import org.gradle.api.Project

class JvmConventionsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
                apply("org.jetbrains.kotlin.plugin.serialization")
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("com.ncorti.ktfmt.gradle")
                apply("org.jetbrains.kotlinx.kover")
            }

            dependencies.add("implementation", libs.kotlinx.serialization.json)
            dependencies.add("implementation", libs.mosaic.runtime)
            dependencies.add("implementation", libs.molecule.runtime)

            extensions.configure<com.ncorti.ktfmt.gradle.KtfmtExtension>("ktfmt") {
                kotlinLangStyle()
            }

            tasks.named("check") {
                dependsOn("ktfmtCheck")
            }
        }
    }
}
