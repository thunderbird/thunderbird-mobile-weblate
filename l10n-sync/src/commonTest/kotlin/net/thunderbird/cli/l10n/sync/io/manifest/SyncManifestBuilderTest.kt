package net.thunderbird.cli.l10n.sync.io.manifest

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import net.thunderbird.cli.l10n.config.Branch
import net.thunderbird.cli.l10n.sync.io.input.FileInputReport
import net.thunderbird.cli.l10n.sync.model.ResourceKeyConflict
import net.thunderbird.cli.l10n.sync.model.ResourceKeyResolution
import net.thunderbird.cli.l10n.sync.model.ResourceKeySource

val SyncManifestBuilderTests by
    testSuite("SyncManifestBuilder") {
        test("builds branch inventory from file reports") {
            val filePath = "feature/example/src/main/res/values/strings.xml"
            val manifest =
                SyncManifestBuilder.build(
                    reports =
                        listOf(
                            FileInputReport(
                                path = filePath,
                                presentBranches = setOf(Branch("main"), Branch("release")),
                                keyResolutions =
                                    listOf(
                                        ResourceKeyResolution(
                                            key = "shared",
                                            selectedBranch = Branch("main"),
                                            selectedSourceFile = filePath,
                                            availableIn =
                                                listOf(
                                                    ResourceKeySource(
                                                        branch = Branch("main"),
                                                        sourceFile = filePath,
                                                        content =
                                                            """<string name="shared">Main</string>""",
                                                    ),
                                                    ResourceKeySource(
                                                        branch = Branch("release"),
                                                        sourceFile = filePath,
                                                        content =
                                                            """<string name="shared">Release</string>""",
                                                    ),
                                                ),
                                        ),
                                        ResourceKeyResolution(
                                            key = "release_only",
                                            selectedBranch = Branch("release"),
                                            selectedSourceFile = filePath,
                                            availableIn =
                                                listOf(
                                                    ResourceKeySource(
                                                        branch = Branch("release"),
                                                        sourceFile = filePath,
                                                        content =
                                                            """<string name="release_only">Release only</string>""",
                                                    )
                                                ),
                                        ),
                                    ),
                                conflicts =
                                    listOf(
                                        ResourceKeyConflict(
                                            filePath = filePath,
                                            key = "shared",
                                            selectedBranch = Branch("main"),
                                            conflictingBranch = Branch("release"),
                                            selectedContent =
                                                """<string name="shared">Main</string>""",
                                            conflictingContent =
                                                """<string name="shared">Release</string>""",
                                        )
                                    ),
                            )
                        )
                )

            assertEquals(1, manifest.version)
            assertEquals(
                listOf("shared"),
                manifest.branches.getValue("main").files.getValue(filePath),
            )
            assertEquals(
                listOf("release_only", "shared"),
                manifest.branches.getValue("release").files.getValue(filePath),
            )
        }
    }
