package net.thunderbird.cli.l10n.sync.command

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import net.thunderbird.cli.l10n.config.readText
import net.thunderbird.cli.l10n.config.resolve
import net.thunderbird.cli.l10n.config.writeText
import net.thunderbird.cli.l10n.sync.io.manifest.BranchInventory
import net.thunderbird.cli.l10n.sync.io.manifest.SyncManifest.Companion.SYNC_MANIFEST_FILE
import net.thunderbird.cli.l10n.sync.io.manifest.SyncManifestStore
import net.thunderbird.cli.l10n.sync.support.temporaryDirectoryFixture
import net.thunderbird.cli.l10n.sync.task.DefaultExportTask
import net.thunderbird.cli.l10n.sync.task.ExportInput
import net.thunderbird.cli.l10n.sync.task.TargetBranchValidator
import net.thunderbird.cli.l10n.terminal.TerminalState
import net.thunderbird.cli.l10n.terminal.TerminalStateStore

val ExportCommandTests by
    testSuite("ExportCommand") {
        temporaryDirectoryFixture().asParameterForEach {
            test("dry-run reports changed files without writing") { temporaryDirectory ->
                val l10nRepo = temporaryDirectory.resolve("l10n")
                val targetRepo = temporaryDirectory.resolve("source")
                writeExportFixture(l10nRepo)

                runBlocking {
                    ExportCommand(
                            command =
                                Command.Export(
                                    "release",
                                    l10nRepo.toString(),
                                    applyChanges = false,
                                ),
                            task = exportTask(l10nRepo, targetRepo),
                            terminal = TerminalStateStore(TerminalState("test")),
                        )
                        .run()
                }
                assertFalse(SystemFileSystem.exists(targetRepo.resolve(SOURCE_FILE)))
                assertFalse(SystemFileSystem.exists(targetRepo.resolve(TRANSLATION_FILE)))
            }

            test("apply writes branch-filtered source and translation files") { temporaryDirectory
                ->
                val l10nRepo = temporaryDirectory.resolve("l10n")
                val targetRepo = temporaryDirectory.resolve("source")
                writeExportFixture(l10nRepo)

                runBlocking {
                    ExportCommand(
                            command =
                                Command.Export("release", l10nRepo.toString(), applyChanges = true),
                            task = exportTask(l10nRepo, targetRepo),
                            terminal = TerminalStateStore(TerminalState("test")),
                        )
                        .run()
                }
                assertEquals(
                    """
                    <?xml version="1.0" encoding="utf-8"?>
                    <resources>
                        <string name="release_only">Release only</string>
                        <string name="shared">Shared</string>
                    </resources>

                    """
                        .trimIndent(),
                    targetRepo.resolve(SOURCE_FILE).readText(),
                )
                assertEquals(
                    """
                    <?xml version="1.0" encoding="utf-8"?>
                    <resources>
                        <string name="release_only">Nur Release</string>
                        <string name="shared">Geteilt</string>
                    </resources>

                    """
                        .trimIndent(),
                    targetRepo.resolve(TRANSLATION_FILE).readText(),
                )
            }

            test("stops export when the target is not based on the requested branch") {
                temporaryDirectory ->
                val l10nRepo = temporaryDirectory.resolve("l10n")
                val targetRepo = temporaryDirectory.resolve("source")
                writeExportFixture(l10nRepo)
                val error =
                    assertFailsWith<IllegalStateException> {
                        runBlocking {
                            DefaultExportTask(
                                    "release",
                                    l10nRepo,
                                    targetRepo,
                                    targetBranchValidator =
                                        TargetBranchValidator { _, _ ->
                                            error("Target branch is not based on release")
                                        },
                                )
                                .validateInput(ExportInput(SyncManifestStore.read(l10nRepo)))
                        }
                    }

                assertEquals("Target branch is not based on release", error.message)
            }

            test("rejects a manifest path outside the l10n and target roots") { temporaryDirectory
                ->
                val task =
                    exportTask(
                        temporaryDirectory.resolve("l10n"),
                        temporaryDirectory.resolve("source"),
                    )
                val error =
                    assertFailsWith<IllegalArgumentException> {
                        task.mergeInput(
                            BranchInventory(files = mapOf("../outside.xml" to listOf("key")))
                        )
                    }

                assertEquals("Invalid manifest path: ../outside.xml", error.message)
            }
        }
    }

private const val SOURCE_FILE = "feature/example/src/main/res/values/strings.xml"
private const val TRANSLATION_FILE = "feature/example/src/main/res/values-de/strings.xml"

private fun writeExportFixture(l10nRepo: Path) {
    l10nRepo
        .resolve(SYNC_MANIFEST_FILE)
        .writeText(
            """
            {
              "version": 1,
              "branches": {
                "release": {
                  "files": {
                    "$SOURCE_FILE": ["release_only", "shared"]
                  }
                }
              }
            }

            """
                .trimIndent()
        )
    l10nRepo
        .resolve(SOURCE_FILE)
        .writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <string name="main_only">Main only</string>
                <string name="release_only">Release only</string>
                <string name="shared">Shared</string>
            </resources>

            """
                .trimIndent()
        )
    l10nRepo
        .resolve(TRANSLATION_FILE)
        .writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <string name="main_only">Nur Main</string>
                <string name="release_only">Nur Release</string>
                <string name="shared">Geteilt</string>
            </resources>

            """
                .trimIndent()
        )
}

private fun exportTask(l10nRepo: Path, targetRepo: Path) =
    DefaultExportTask(
        branch = "release",
        l10nRoot = l10nRepo,
        targetRoot = targetRepo,
        targetBranchValidator = TargetBranchValidator { _, _ -> },
    )
