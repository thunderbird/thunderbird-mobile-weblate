package net.thunderbird.cli.l10n.sync.command

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import net.thunderbird.cli.l10n.config.Branch
import net.thunderbird.cli.l10n.config.ComponentDiscoveryConfig
import net.thunderbird.cli.l10n.config.ImportConfig
import net.thunderbird.cli.l10n.config.L10nConfig
import net.thunderbird.cli.l10n.config.L10nProjectConfig
import net.thunderbird.cli.l10n.config.Repository
import net.thunderbird.cli.l10n.config.SourceConfig
import net.thunderbird.cli.l10n.config.WeblateConfig
import net.thunderbird.cli.l10n.config.readText
import net.thunderbird.cli.l10n.config.resolve
import net.thunderbird.cli.l10n.config.writeText
import net.thunderbird.cli.l10n.sync.io.git.BranchFiles
import net.thunderbird.cli.l10n.sync.io.git.GitClient
import net.thunderbird.cli.l10n.sync.io.manifest.SyncManifest.Companion.SYNC_MANIFEST_FILE
import net.thunderbird.cli.l10n.sync.support.temporaryDirectoryFixture
import net.thunderbird.cli.l10n.sync.task.DefaultImportTask
import net.thunderbird.cli.l10n.terminal.TerminalLine
import net.thunderbird.cli.l10n.terminal.TerminalState
import net.thunderbird.cli.l10n.terminal.TerminalStateStore

val ImportCommandTests by
    testSuite("ImportCommand") {
        temporaryDirectoryFixture().asParameterForEach {
            test("apply imports branch files, writes manifest, and cleans stale files") {
                temporaryDirectory ->
                val config = importTestConfig(temporaryDirectory)
                val gitClient = FakeGitClient(config)
                val store = TerminalStateStore(TerminalState("test"))
                temporaryDirectory
                    .resolve(STALE_SOURCE_FILE)
                    .writeText("<resources><string name=\"old\">Old</string></resources>")
                temporaryDirectory
                    .resolve(STALE_TRANSLATION_FILE)
                    .writeText("<resources><string name=\"old\">Old</string></resources>")
                temporaryDirectory
                    .resolve(PROTECTED_FILE)
                    .writeText("<resources><string name=\"keep\">Keep</string></resources>")

                ImportCommand(
                        task = DefaultImportTask(config, gitClient),
                        terminal = store,
                        all = false,
                        applyChanges = true,
                    )
                    .run()
                assertEquals(
                    """
                    <?xml version="1.0" encoding="utf-8"?>
                    <resources>
                        <string name="beta_only">Beta</string>
                        <string name="main_only">Main</string>
                        <string name="shared">Main shared</string>
                    </resources>

                    """
                        .trimIndent(),
                    temporaryDirectory.resolve(SOURCE_FILE).readText(),
                )
                assertTrue(temporaryDirectory.resolve(SYNC_MANIFEST_FILE).readText().isNotBlank())
                assertFalse(
                    kotlinx.io.files.SystemFileSystem.exists(
                        temporaryDirectory.resolve(STALE_SOURCE_FILE)
                    )
                )
                assertFalse(
                    kotlinx.io.files.SystemFileSystem.exists(
                        temporaryDirectory.resolve(STALE_TRANSLATION_FILE)
                    )
                )
                assertTrue(
                    kotlinx.io.files.SystemFileSystem.exists(
                        temporaryDirectory.resolve(PROTECTED_FILE)
                    )
                )

                val lines = store.state.value.lines
                assertContains(
                    lines,
                    TerminalLine.Warning(
                        "$SOURCE_FILE: 'shared' differs across branches; using main",
                        indent = 1,
                    ),
                )
                assertContains(
                    lines,
                    TerminalLine.WarningDetail(
                        "main: <string name=\"shared\">Main shared</string>",
                        indent = 1,
                    ),
                )
                assertContains(
                    lines,
                    TerminalLine.WarningDetail(
                        "beta: <string name=\"shared\">Beta shared</string>",
                        indent = 1,
                    ),
                )
                assertContains(lines, TerminalLine.Text("Removed 2 stale files", indent = 1))
                assertContains(lines, TerminalLine.Text("- $STALE_SOURCE_FILE", indent = 2))
                assertContains(lines, TerminalLine.Text("- $STALE_TRANSLATION_FILE", indent = 2))
                assertContains(lines, TerminalLine.Text("Total unique files: 1", indent = 1))
                assertContains(lines, TerminalLine.Text("Files changed: 1", indent = 1))
                assertContains(lines, TerminalLine.Text("Conflicting keys: 1", indent = 1))
            }
        }
    }

private const val SOURCE_FILE = "feature/example/src/main/res/values/strings.xml"
private const val STALE_SOURCE_FILE = "feature/stale/src/main/res/values/strings.xml"
private const val STALE_TRANSLATION_FILE = "feature/stale/src/main/res/values-de/strings.xml"
private const val PROTECTED_FILE = "app-common/src/main/res/values/strings.xml"

private fun importTestConfig(projectRoot: kotlinx.io.files.Path): L10nConfig =
    L10nConfig(
        projectRoot = projectRoot,
        project =
            L10nProjectConfig(
                source =
                    SourceConfig(
                        repository = Repository("https://example.com/source.git"),
                        branches = listOf(Branch("main"), Branch("beta")),
                    ),
                import =
                    ImportConfig(
                        sourceFilePatterns = listOf("**/res/values/strings.xml"),
                        translatedFilePatterns = listOf("**/res/values-*/strings.xml"),
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

private class FakeGitClient(private val config: L10nConfig) : GitClient {
    override suspend fun checkAvailableBranches(branches: List<Branch>): List<Branch> = branches

    override suspend fun fetchAllBranches(branches: List<Branch>, all: Boolean): List<BranchFiles> {
        require(!all)
        writeBranchFile(
            branch = Branch("main"),
            content =
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <string name="main_only">Main</string>
                    <string name="shared">Main shared</string>
                </resources>

                """
                    .trimIndent(),
        )
        writeBranchFile(
            branch = Branch("beta"),
            content =
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <string name="beta_only">Beta</string>
                    <string name="shared">Beta shared</string>
                </resources>

                """
                    .trimIndent(),
        )

        return branches.map { branch -> BranchFiles(branch, listOf(SOURCE_FILE)) }
    }

    private fun writeBranchFile(branch: Branch, content: String) {
        config.getBranchWorkDir(branch).resolve(SOURCE_FILE).writeText(content)
    }
}
