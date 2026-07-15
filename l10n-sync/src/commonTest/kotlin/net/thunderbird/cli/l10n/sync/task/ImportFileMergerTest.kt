package net.thunderbird.cli.l10n.sync.task

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import net.thunderbird.cli.l10n.config.Branch
import net.thunderbird.cli.l10n.sync.model.L10nFileType
import net.thunderbird.cli.l10n.sync.model.SourceTextFile

val ImportFileMergerTests by
    testSuite("ImportFileMerger") {
        test("reports conflicting text files and keeps the first branch content") {
            val result =
                ImportFileMerger()
                    .merge(
                        listOf(textFile("main", "Main content"), textFile("beta", "Beta content"))
                    )

            assertEquals("Main content", result.outputFiles.single().content)
            val conflict = assertNotNull(result.reports.single().textFileConflict)
            assertEquals(Branch("main"), conflict.selectedBranch)
            assertEquals(listOf(Branch("main"), Branch("beta")), conflict.sources.map { it.branch })
        }
    }

private const val TEXT_FILE = "feature/example/src/main/fastlane/metadata/android/en-US/title.txt"

private fun textFile(branch: String, content: String) =
    SourceTextFile(
        relativePath = TEXT_FILE,
        branch = Branch(branch),
        type = L10nFileType.STORE_METADATA,
        content = content,
    )
