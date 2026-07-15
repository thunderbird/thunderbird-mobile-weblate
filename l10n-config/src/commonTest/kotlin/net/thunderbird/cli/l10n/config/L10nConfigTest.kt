package net.thunderbird.cli.l10n.config

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString

val L10nConfigTests by
    testSuite("L10nConfig") {
        temporaryDirectoryFixture(prefix = "l10n-config-").asParameterForEach {
            test("loads project config") { projectRoot ->
                writeText(
                    Path(projectRoot, "l10n-config.json"),
                    validConfigJson(
                        extraTopLevel =
                            """
                            ,
                              "ignored": "value"
                            """
                                .trimIndent()
                    ),
                )

                val config = L10nConfigLoader().load(projectRoot, configPath = null)

                assertEquals(
                    Repository("https://github.com/thunderbird/thunderbird-android.git"),
                    config.source.repository,
                )
                assertEquals(
                    listOf(Branch("main"), Branch("beta"), Branch("release")),
                    config.source.branches,
                )
                assertEquals("https://hosted.weblate.org/api/", config.weblate.baseUrl)
                assertEquals(
                    listOf(ResourceKind.ANDROID, ResourceKind.COMPOSE),
                    config.weblate.discovery.resources,
                )
            }

            test("exposes derived working directories") { projectRoot ->
                val config = L10nConfig(projectRoot = projectRoot, project = validConfig())

                assertEquals(Path(projectRoot, ".tmp"), config.workDir)
                assertEquals(
                    Path(projectRoot, ".tmp", "beta"),
                    config.getBranchWorkDir(Branch("beta")),
                )
            }

            test("loads config from explicit relative path") { projectRoot ->
                writeText(Path(projectRoot, "config", "l10n.json"), validConfigJson())

                val config = L10nConfigLoader().load(projectRoot, "config/l10n.json")

                assertEquals("thunderbird", config.weblate.projectSlug)
            }

            test("fails when config file is missing") { projectRoot ->
                val failure =
                    assertFailsWith<IllegalStateException> {
                        L10nConfigLoader().load(projectRoot, configPath = null)
                    }

                assertTrue(failure.message.orEmpty().contains("Configuration file not found"))
            }

            test("validates config while loading") { projectRoot ->
                writeText(
                    Path(projectRoot, "l10n-config.json"),
                    validConfigJson()
                        .replace(
                            "https://github.com/thunderbird/thunderbird-android.git",
                            "CHANGE-ME",
                        ),
                )

                val failure =
                    assertFailsWith<IllegalStateException> {
                        L10nConfigLoader().load(projectRoot, configPath = null)
                    }

                assertTrue(failure.message.orEmpty().contains("Invalid l10n config"))
                assertTrue(
                    failure.message.orEmpty().contains("source.repository must be configured")
                )
            }

            test("fails when config JSON is malformed") { projectRoot ->
                writeText(Path(projectRoot, "l10n-config.json"), "{")

                val failure =
                    assertFailsWith<IllegalStateException> {
                        L10nConfigLoader().load(projectRoot, configPath = null)
                    }

                assertTrue(failure.message.orEmpty().contains("Failed to load configuration file"))
            }

            test("resolves project root from settings file") { projectRoot ->
                val nested = Path(projectRoot, "nested", "child")
                SystemFileSystem.createDirectories(nested)
                writeText(Path(projectRoot, "settings.gradle.kts"), "")

                assertEquals(
                    SystemFileSystem.resolve(projectRoot),
                    ProjectRootResolver.resolve(nested),
                )
            }

            test("resolves project root from git directory") { projectRoot ->
                val nested = Path(projectRoot, "nested", "child")
                SystemFileSystem.createDirectories(Path(projectRoot, ".git"))
                SystemFileSystem.createDirectories(nested)

                assertEquals(
                    SystemFileSystem.resolve(projectRoot),
                    ProjectRootResolver.resolve(nested),
                )
            }
        }

        test("validates required config values") {
            val config =
                L10nProjectConfig(
                    source = SourceConfig(repository = Repository(""), branches = emptyList()),
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
                assertFailsWith<IllegalStateException> {
                    L10nProjectConfigValidator.validate(config)
                }

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

        test("treats placeholder repository as unconfigured") {
            val config =
                validConfig()
                    .copy(
                        source =
                            SourceConfig(
                                repository = Repository("CHANGE-ME"),
                                branches = listOf(Branch("main")),
                            )
                    )

            val failure =
                assertFailsWith<IllegalStateException> {
                    L10nProjectConfigValidator.validate(config)
                }

            assertTrue(failure.message.orEmpty().contains("source.repository must be configured"))
        }

        test("accepts ssh repository clone URLs") {
            val config =
                validConfig()
                    .copy(
                        source =
                            SourceConfig(
                                repository = Repository("git@example.com:owner/repo.git"),
                                branches = listOf(Branch("main")),
                            )
                    )

            L10nProjectConfigValidator.validate(config)
        }

        test("accepts Git remote forms without a .git suffix") {
            listOf(
                    "https://github.com/thunderbird/thunderbird-android",
                    "git@example.com:owner/repo",
                    "ssh://git@example.com/owner/repo",
                )
                .forEach { repository ->
                    L10nProjectConfigValidator.validate(
                        validConfig()
                            .copy(
                                source =
                                    SourceConfig(
                                        repository = Repository(repository),
                                        branches = listOf(Branch("main")),
                                    )
                            )
                    )
                }
        }
    }

private fun writeText(file: Path, text: String) {
    file.parent?.let { parent -> SystemFileSystem.createDirectories(parent) }
    val sink = SystemFileSystem.sink(file).buffered()
    try {
        sink.writeString(text)
    } finally {
        sink.close()
    }
}

private fun validConfig(): L10nProjectConfig =
    L10nProjectConfig(
        source =
            SourceConfig(
                repository = Repository("https://github.com/thunderbird/thunderbird-android.git"),
                branches = listOf(Branch("main")),
            ),
        import =
            ImportConfig(
                sourceFilePatterns = listOf("**/res/values/strings.xml"),
                translatedFilePatterns = listOf("**/res/values*/strings.xml"),
                excludedPaths = emptyList(),
            ),
        weblate =
            WeblateConfig(
                baseUrl = "https://hosted.weblate.org/api/",
                projectSlug = "thunderbird",
                defaultLinkedComponent = "app-common",
                componentRepo = "weblate://thunderbird/thunderbird-android-l10n/app-common",
                discovery = ComponentDiscoveryConfig(resources = listOf(ResourceKind.ANDROID)),
            ),
    )

private fun validConfigJson(extraTopLevel: String = ""): String =
    """
    {
      "source": {
        "repository": "https://github.com/thunderbird/thunderbird-android.git",
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
      $extraTopLevel
    }
    """
        .trimIndent()
