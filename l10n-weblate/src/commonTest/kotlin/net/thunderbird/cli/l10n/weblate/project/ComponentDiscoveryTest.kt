package net.thunderbird.cli.l10n.weblate.project

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import net.thunderbird.cli.l10n.config.ComponentDiscoveryConfig
import net.thunderbird.cli.l10n.config.ResourceKind
import net.thunderbird.cli.l10n.config.resolve
import net.thunderbird.cli.l10n.config.writeText
import net.thunderbird.cli.l10n.weblate.support.temporaryDirectoryFixture

val ComponentDiscoveryTests by
    testSuite("ComponentDiscovery") {
        temporaryDirectoryFixture().asParameterForEach {
            test("discovers Android and Compose components with stable slugs") { root ->
                root.resolve("app-common/src/main/res/values/strings.xml").writeText(RESOURCES_XML)
                root
                    .resolve("feature/chat/src/main/res/values/strings.xml")
                    .writeText(RESOURCES_XML)
                root
                    .resolve("feature/chat/src/commonMain/composeResources/values/strings.xml")
                    .writeText(RESOURCES_XML)
                root
                    .resolve("feature/chat/src/main/res/values-de/strings.xml")
                    .writeText(RESOURCES_XML)
                root
                    .resolve("build/generated/src/main/res/values/strings.xml")
                    .writeText(RESOURCES_XML)

                val result =
                    ComponentDiscovery(
                            translationsRoot = root,
                            config =
                                ComponentDiscoveryConfig(
                                    resources = listOf(ResourceKind.ANDROID, ResourceKind.COMPOSE)
                                ),
                        )
                        .discover()

                assertEquals(
                    listOf(
                        ComponentInfo(
                            slug = "app-common",
                            path = "app-common",
                            type = ResourceKind.ANDROID,
                        ),
                        ComponentInfo(
                            slug = "feature-chat-android",
                            path = "feature/chat",
                            type = ResourceKind.ANDROID,
                        ),
                        ComponentInfo(
                            slug = "feature-chat-compose",
                            path = "feature/chat",
                            type = ResourceKind.COMPOSE,
                        ),
                    ),
                    result,
                )
            }

            test("skips configured modules") { root ->
                root.resolve("app-common/src/main/res/values/strings.xml").writeText(RESOURCES_XML)
                root
                    .resolve("feature/chat/src/main/res/values/strings.xml")
                    .writeText(RESOURCES_XML)

                val result =
                    ComponentDiscovery(
                            translationsRoot = root,
                            config =
                                ComponentDiscoveryConfig(resources = listOf(ResourceKind.ANDROID)),
                            ignoredModules = listOf("feature/chat"),
                        )
                        .discover()

                assertEquals(
                    listOf(
                        ComponentInfo(
                            slug = "app-common",
                            path = "app-common",
                            type = ResourceKind.ANDROID,
                        )
                    ),
                    result,
                )
            }

            test("fails when distinct modules generate the same slug") { root ->
                root.resolve("a-b/c/src/main/res/values/strings.xml").writeText(RESOURCES_XML)
                root.resolve("a/b-c/src/main/res/values/strings.xml").writeText(RESOURCES_XML)

                val error =
                    assertFailsWith<IllegalArgumentException> {
                        ComponentDiscovery(
                                translationsRoot = root,
                                config =
                                    ComponentDiscoveryConfig(
                                        resources = listOf(ResourceKind.ANDROID)
                                    ),
                            )
                            .discover()
                    }

                assertEquals("Duplicate component slugs: a-b-c (a-b/c, a/b-c)", error.message)
            }
        }
    }

private const val RESOURCES_XML = "<resources></resources>"
