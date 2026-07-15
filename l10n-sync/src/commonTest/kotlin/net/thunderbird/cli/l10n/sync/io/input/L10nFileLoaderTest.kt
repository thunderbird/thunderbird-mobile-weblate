package net.thunderbird.cli.l10n.sync.io.input

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
import net.thunderbird.cli.l10n.config.resolve
import net.thunderbird.cli.l10n.config.writeText
import net.thunderbird.cli.l10n.sync.io.git.BranchFiles
import net.thunderbird.cli.l10n.sync.io.git.GitClient
import net.thunderbird.cli.l10n.sync.model.SourceTextFile
import net.thunderbird.cli.l10n.sync.support.temporaryDirectoryFixture
import net.thunderbird.cli.l10n.sync.task.ImportFileMerger

val L10nFileLoaderTests by
    testSuite("L10nFileLoader") {
        temporaryDirectoryFixture().asParameterForEach {
            test("uses the first available branch for a file") { temporaryDirectory ->
                val config = loaderTestConfig(temporaryDirectory)
                val mainRoot = config.getBranchWorkDir(Branch("main"))
                val betaRoot = config.getBranchWorkDir(Branch("beta"))

                val filePath = "app-metadata/en-US/title.txt"
                mainRoot.resolve(filePath).writeText("main")
                betaRoot.resolve(filePath).writeText("beta")

                val result =
                    L10nFileLoader(
                            gitClient =
                                FakeGitClient(
                                    listOf(
                                        BranchFiles(Branch("main"), listOf(filePath)),
                                        BranchFiles(Branch("beta"), listOf(filePath)),
                                    )
                                ),
                            config = config,
                        )
                        .load(branches = listOf(Branch("main"), Branch("beta")), all = false)
                val resolved = result.files

                assertEquals(2, resolved.size)
                val sourceFiles = resolved.filterIsInstance<SourceTextFile>()
                assertEquals(listOf("main", "beta"), sourceFiles.map { file -> file.content })
                assertTrue(sourceFiles.all { file -> file.relativePath.endsWith("title.txt") })
            }

            test("merges XML resource keys by branch precedence") { temporaryDirectory ->
                val config = loaderTestConfig(temporaryDirectory)
                val mainRoot = config.getBranchWorkDir(Branch("main"))
                val betaRoot = config.getBranchWorkDir(Branch("beta"))

                val filePath = "app/src/main/res/values/strings.xml"
                mainRoot
                    .resolve(filePath)
                    .writeText(
                        """
                        <?xml version="1.0" encoding="utf-8"?>
                        <resources>
                            <!-- Main comment -->
                            <string name="shared">Main</string>
                            <string name="main_only">Main only</string>
                        </resources>
                        """
                            .trimIndent()
                    )
                betaRoot
                    .resolve(filePath)
                    .writeText(
                        """
                        <?xml version="1.0" encoding="utf-8"?>
                        <resources>
                            <string name="shared">Beta</string>
                            <string name="beta_only">Beta only</string>
                        </resources>
                        """
                            .trimIndent()
                    )

                val result =
                    L10nFileLoader(
                            gitClient =
                                FakeGitClient(
                                    listOf(
                                        BranchFiles(Branch("main"), listOf(filePath)),
                                        BranchFiles(Branch("beta"), listOf(filePath)),
                                    )
                                ),
                            config = config,
                        )
                        .load(branches = listOf(Branch("main"), Branch("beta")), all = false)

                val mergeResult = ImportFileMerger().merge(result.files)
                val resolved = mergeResult.outputFiles.single()
                assertTrue(resolved.content.contains("<!-- Main comment -->"))
                assertTrue(resolved.content.contains("""<string name="shared">Main</string>"""))
                assertTrue(
                    resolved.content.contains("""<string name="beta_only">Beta only</string>""")
                )
                assertEquals(1, mergeResult.conflicts.size)
                val conflict = mergeResult.conflicts.single()
                assertEquals("shared", conflict.key)
                assertEquals("""<string name="shared">Main</string>""", conflict.selectedContent)
                assertEquals("""<string name="shared">Beta</string>""", conflict.conflictingContent)

                val report = mergeResult.reports.single()
                assertEquals(filePath, report.path)
                assertEquals(
                    listOf(Branch("main"), Branch("beta")),
                    report.presentBranches.toList(),
                )
                assertEquals(3, report.keyResolutions.size)
                val sharedProvenance =
                    report.keyResolutions.single { resolution -> resolution.key == "shared" }
                assertEquals(Branch("main"), sharedProvenance.selectedBranch)
                assertEquals(filePath, sharedProvenance.selectedSourceFile)
                assertEquals(
                    listOf(Branch("main"), Branch("beta")),
                    sharedProvenance.availableIn.map { source -> source.branch },
                )
            }

            test("merges translated XML resource keys without conflict reports") {
                temporaryDirectory ->
                val config = loaderTestConfig(temporaryDirectory)
                val mainRoot = config.getBranchWorkDir(Branch("main"))
                val releaseRoot = config.getBranchWorkDir(Branch("release"))

                val filePath = "app/src/main/res/values-de/strings.xml"
                mainRoot
                    .resolve(filePath)
                    .writeText(
                        """
                        <?xml version="1.0" encoding="utf-8"?>
                        <resources>
                            <string name="shared">Neu</string>
                            <string name="main_only">Nur main</string>
                        </resources>
                        """
                            .trimIndent()
                    )
                releaseRoot
                    .resolve(filePath)
                    .writeText(
                        """
                        <?xml version="1.0" encoding="utf-8"?>
                        <resources>
                            <string name="shared">Alt</string>
                            <string name="release_only">Nur release</string>
                        </resources>
                        """
                            .trimIndent()
                    )

                val result =
                    L10nFileLoader(
                            gitClient =
                                FakeGitClient(
                                    listOf(
                                        BranchFiles(Branch("main"), listOf(filePath)),
                                        BranchFiles(Branch("release"), listOf(filePath)),
                                    )
                                ),
                            config = config,
                        )
                        .load(branches = listOf(Branch("main"), Branch("release")), all = false)

                val mergeResult = ImportFileMerger().merge(result.files)
                val resolved = mergeResult.outputFiles.single()
                assertTrue(resolved.content.contains("""<string name="shared">Neu</string>"""))
                assertTrue(
                    resolved.content.contains(
                        """<string name="release_only">Nur release</string>"""
                    )
                )
                assertEquals(emptyList(), mergeResult.conflicts)
                assertEquals(emptyList(), mergeResult.reports)
            }
        }
    }

private class FakeGitClient(private val branchFiles: List<BranchFiles>) : GitClient {
    override suspend fun checkAvailableBranches(branches: List<Branch>): List<Branch> = branches

    override suspend fun fetchAllBranches(branches: List<Branch>, all: Boolean): List<BranchFiles> {
        require(!all)
        return branchFiles
    }
}

private fun loaderTestConfig(projectRoot: kotlinx.io.files.Path): L10nConfig =
    L10nConfig(
        projectRoot = projectRoot,
        project =
            L10nProjectConfig(
                source =
                    SourceConfig(
                        repository = Repository("https://example.com/source.git"),
                        branches = listOf(Branch("main"), Branch("beta"), Branch("release")),
                    ),
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
            ),
    )
