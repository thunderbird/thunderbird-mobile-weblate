plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(plugin(libs.plugins.kotlin.multiplatform))
    implementation(plugin(libs.plugins.kotlin.serialization))
    implementation(plugin(libs.plugins.kotlin.compose))
    implementation(plugin(libs.plugins.ktfmt))
    implementation(plugin(libs.plugins.kover))
    implementation(plugin(libs.plugins.testBalloon))
    implementation(plugin(libs.plugins.detekt))
}

gradlePlugin {
    plugins {
        register("kmpConventions") {
            id = "tb-kmp-conventions"
            implementationClass = "KmpConventionsPlugin"
        }
        register("kmpTestConventions") {
            id = "tb-kmp-test-conventions"
            implementationClass = "KmpTestConventionsPlugin"
        }
        register("jvmConventions") {
            id = "tb-jvm-conventions"
            implementationClass = "JvmConventionsPlugin"
        }
        register("jvmTestConventions") {
            id = "tb-jvm-test-conventions"
            implementationClass = "JvmTestConventionsPlugin"
        }
    }
}

private fun plugin(provider: Provider<PluginDependency>) = with(provider.get()) {
    "$pluginId:$pluginId.gradle.plugin:$version"
}
