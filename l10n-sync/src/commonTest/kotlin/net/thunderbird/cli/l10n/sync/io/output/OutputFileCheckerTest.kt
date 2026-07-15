package net.thunderbird.cli.l10n.sync.io.output

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import net.thunderbird.cli.l10n.config.readText
import net.thunderbird.cli.l10n.config.resolve
import net.thunderbird.cli.l10n.config.writeText
import net.thunderbird.cli.l10n.sync.support.temporaryDirectoryFixture

val OutputFileCheckerTests by
    testSuite("OutputFileChecker") {
        temporaryDirectoryFixture().asParameterForEach {
            test("checks changed files without writing them") { temporaryDirectory ->
                val projectRoot = temporaryDirectory
                val filePath = "feature/example/src/main/res/values/strings.xml"
                val outputFile = projectRoot.resolve(filePath)
                outputFile.writeText("old")

                val result =
                    OutputFileChecker.checkFiles(
                        projectRoot = projectRoot,
                        files = listOf(OutputFile(relativePath = filePath, content = "new")),
                    )

                assertEquals(
                    listOf(filePath),
                    result.changedFiles.map { file -> file.relativePath },
                )
                assertEquals("old", outputFile.readText())
            }
        }
    }
