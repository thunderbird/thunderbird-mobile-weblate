package net.thunderbird.cli.l10n.sync

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import net.thunderbird.cli.l10n.config.Branch
import net.thunderbird.cli.l10n.config.ComponentDiscoveryConfig
import net.thunderbird.cli.l10n.config.ImportConfig
import net.thunderbird.cli.l10n.config.L10nConfig
import net.thunderbird.cli.l10n.config.L10nProjectConfig
import net.thunderbird.cli.l10n.config.Repository
import net.thunderbird.cli.l10n.config.SourceConfig
import net.thunderbird.cli.l10n.config.WeblateConfig
import net.thunderbird.cli.l10n.sync.support.temporaryDirectoryFixture

val L10nConfigTests by
    testSuite("L10nConfig") {
        temporaryDirectoryFixture().asParameterForEach {
            test("branch root uses branch name") { temporaryDirectory ->
                val config =
                    L10nConfig(
                        projectRoot = temporaryDirectory,
                        project =
                            testToolsConfig(
                                repository =
                                    "https://github.com/thunderbird/thunderbird-android.git"
                            ),
                    )

                assertEquals("main", config.getBranchWorkDir(Branch("main")).name)
                assertTrue(config.getBranchWorkDir(Branch("main")).toString().contains(".tmp"))
            }

            test("branch root does not depend on repository name") { temporaryDirectory ->
                val config =
                    L10nConfig(
                        projectRoot = temporaryDirectory,
                        project =
                            testToolsConfig(
                                repository = "https://example.com/owner/repository-name.git"
                            ),
                    )

                assertEquals("main", config.getBranchWorkDir(Branch("main")).name)
            }

            testSuite("keeps repository URLs in the tools config") {
                listOf(
                        "https://github.com/owner/repo.git",
                        "https://example.com/org/multi-word-repo.git",
                        "git@example.com:user/repo-123.git",
                    )
                    .forEach { repository ->
                        test(repository) { temporaryDirectory ->
                            val config =
                                L10nConfig(
                                    projectRoot = temporaryDirectory,
                                    project = testToolsConfig(repository = repository),
                                )

                            assertEquals(repository, config.project.source.repository.url)
                        }
                    }
            }
        }
    }

private fun testToolsConfig(
    repository: String = "https://github.com/thunderbird/thunderbird-android.git"
) =
    L10nProjectConfig(
        source =
            SourceConfig(repository = Repository(repository), branches = listOf(Branch("main"))),
        import =
            ImportConfig(
                sourceFilePatterns = emptyList(),
                translatedFilePatterns = emptyList(),
                excludedPaths = emptyList(),
            ),
        weblate =
            WeblateConfig(
                baseUrl = "https://hosted.weblate.org/api/",
                projectSlug = "thunderbird",
                defaultLinkedComponent = "app-common",
                componentRepo = "weblate://thunderbird/app-common",
                discovery = ComponentDiscoveryConfig(resources = emptyList()),
            ),
    )
