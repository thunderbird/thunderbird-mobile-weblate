package net.thunderbird.cli.l10n.config

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

val L10nToolsConfigTests by
    testSuite("L10nToolsConfig") {
        temporaryDirectoryFixture(prefix = "l10n-config-").asParameterForEach {
            test("loads project config") { projectRoot ->
                projectRoot.resolve("l10n-tools.json").toFile().writeText(validConfigJson())

                val config = L10nToolsConfigLoader().load(projectRoot.toFile(), configPath = null)

                assertEquals("thunderbird/thunderbird-android", config.source.repository)
                assertEquals(listOf("main", "beta", "release"), config.source.branches)
                assertEquals("https://hosted.weblate.org/api/", config.weblate.baseUrl)
                assertEquals(
                    listOf(ResourceKind.ANDROID, ResourceKind.COMPOSE),
                    config.weblate.discovery.resources,
                )
            }

            test("loads config from explicit relative path") { projectRoot ->
                java.nio.file.Files.createDirectories(projectRoot.resolve("config"))
                projectRoot.resolve("config/tools.json").toFile().writeText(validConfigJson())

                val config = L10nToolsConfigLoader().load(projectRoot.toFile(), "config/tools.json")

                assertEquals("thunderbird", config.weblate.projectSlug)
            }

            test("fails when config file is missing") { projectRoot ->
                val failure =
                    assertFailsWith<IllegalStateException> {
                        L10nToolsConfigLoader().load(projectRoot.toFile(), configPath = null)
                    }

                assertTrue(failure.message.orEmpty().contains("Configuration file not found"))
            }
        }

        test("validates required config values") {
            val config =
                L10nToolsConfig(
                    source = SourceConfig(repository = "", branches = emptyList()),
                    import =
                        ImportConfig(
                            sourceFilePatterns = emptyList(),
                            translatedFilePatterns = emptyList(),
                            excludedPaths = emptyList(),
                        ),
                    weblate =
                        WeblateConfig(
                            baseUrl = "",
                            projectSlug = "",
                            defaultLinkedComponent = "CHANGE-ME",
                            componentRepo = "CHANGE-ME",
                            discovery = ComponentDiscoveryConfig(resources = emptyList()),
                        ),
                )

            val failure =
                assertFailsWith<IllegalStateException> { L10nToolsConfigValidator.validate(config) }

            val message = failure.message.orEmpty()
            assertTrue(message.contains("source.repository must be configured"))
            assertTrue(message.contains("source.branches must contain at least one value"))
            assertTrue(
                message.contains("import.sourceFilePatterns must contain at least one value")
            )
            assertTrue(
                message.contains("import.translatedFilePatterns must contain at least one value")
            )
            assertTrue(message.contains("weblate.defaultLinkedComponent must be configured"))
            assertTrue(message.contains("weblate.componentRepo must be configured"))
            assertTrue(
                message.contains("weblate.discovery.resources must contain at least one value")
            )
        }

        test("matches excluded paths by glob or path fragment") {
            val patterns = listOf("app-metadata/**/changelogs/**", "openpgp-api")

            assertTrue(
                "app-metadata/net.thunderbird.android/en-US/changelogs/1.txt"
                    .isExcludedPath(patterns)
            )
            assertTrue(
                "plugins/openpgp-api-lib/openpgp-api/src/main/res/values/strings.xml"
                    .isExcludedPath(patterns)
            )
            assertFalse(
                "feature/account/setup/src/main/res/values/strings.xml".isExcludedPath(patterns)
            )
        }
    }

private fun validConfigJson(): String =
    """
    {
      "source": {
        "repository": "thunderbird/thunderbird-android",
        "branches": [
          "main",
          "beta",
          "release"
        ]
      },
      "import": {
        "sourceFilePatterns": [
          "**/res/values/strings.xml"
        ],
        "translatedFilePatterns": [
          "**/res/values*/strings.xml"
        ],
        "excludedPaths": [
          "app-metadata/**/changelogs/**"
        ]
      },
      "weblate": {
        "baseUrl": "https://hosted.weblate.org/api/",
        "projectSlug": "thunderbird",
        "defaultLinkedComponent": "app-common",
        "componentRepo": "weblate://thunderbird/thunderbird-android-l10n/app-common",
        "discovery": {
          "resources": [
            "android",
            "compose"
          ]
        }
      }
    }
    """
        .trimIndent()
