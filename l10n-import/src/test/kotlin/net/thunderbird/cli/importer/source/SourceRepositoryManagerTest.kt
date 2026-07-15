package net.thunderbird.cli.importer

import de.infix.testBalloon.framework.core.testSuite
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import net.thunderbird.cli.importer.git.Branch
import net.thunderbird.cli.l10n.config.ComponentDiscoveryConfig
import net.thunderbird.cli.l10n.config.ImportConfig
import net.thunderbird.cli.l10n.config.L10nToolsConfig
import net.thunderbird.cli.l10n.config.SourceConfig
import net.thunderbird.cli.l10n.config.WeblateConfig

val ImportContextTests by
    testSuite("ImportContext") {
        test("branch root uses correct repo name") {
            val projectRoot = Files.createTempDirectory("project-").toFile()
            val context =
                ImportContext(
                    projectRoot = projectRoot,
                    toolsConfig = testToolsConfig(repository = "thunderbird/thunderbird-android"),
                )

            assertEquals(
                "thunderbird-android-main",
                context.getTmpBranchRootDir(Branch("main")).name,
            )
            assertTrue(context.getTmpBranchRootDir(Branch("main")).path.contains(".tmp"))
        }

        test("parses repository name correctly") {
            val context =
                ImportContext(
                    projectRoot = Files.createTempDirectory("project-").toFile(),
                    toolsConfig = testToolsConfig(repository = "owner/repository-name"),
                )
            assertEquals("repository-name-main", context.getTmpBranchRootDir(Branch("main")).name)
        }

        testSuite("handles various repository formats") {
            listOf(
                    "owner/repo" to "repo",
                    "org/multi-word-repo" to "multi-word-repo",
                    "user/repo-123" to "repo-123",
                )
                .forEach { (input, expectedName) ->
                    test("$input -> $expectedName") {
                        val context =
                            ImportContext(
                                projectRoot = Files.createTempDirectory("project-").toFile(),
                                toolsConfig = testToolsConfig(repository = input),
                            )
                        assertEquals(
                            "$expectedName-main",
                            context.getTmpBranchRootDir(Branch("main")).name,
                        )
                    }
                }
        }

        test("uses repository URL name for temporary checkout directory") {
            val context =
                ImportContext(
                    projectRoot = Files.createTempDirectory("project-").toFile(),
                    toolsConfig =
                        testToolsConfig(
                            repository = "https://github.com/thunderbird/thunderbird-android.git"
                        ),
                )

            assertEquals(
                "thunderbird-android-beta",
                context.getTmpBranchRootDir(Branch("beta")).name,
            )
        }
    }

private fun testToolsConfig(repository: String = "thunderbird/thunderbird-android") =
    L10nToolsConfig(
        source = SourceConfig(repository = repository, branches = listOf("main")),
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
