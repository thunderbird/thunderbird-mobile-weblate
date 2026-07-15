package net.thunderbird.cli.l10n.sync.io.output

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import net.thunderbird.cli.l10n.config.readText
import net.thunderbird.cli.l10n.config.resolve
import net.thunderbird.cli.l10n.config.writeText
import net.thunderbird.cli.l10n.sync.support.temporaryDirectoryFixture

val OutputFileWriterTests by
    testSuite("OutputFileWriter") {
        temporaryDirectoryFixture().asParameterForEach {
            test("writes changed files") { temporaryDirectory ->
                val projectRoot = temporaryDirectory
                val changedPath = "feature/example/src/main/res/values/strings.xml"
                val unchangedPath = "feature/other/src/main/res/values/strings.xml"
                val changedOutput = projectRoot.resolve(changedPath)
                val unchangedOutput = projectRoot.resolve(unchangedPath)
                changedOutput.writeText("old")
                unchangedOutput.writeText("same")

                val changedFiles = listOf(OutputFile(relativePath = changedPath, content = "new"))
                OutputFileWriter.writeFiles(projectRoot = projectRoot, files = changedFiles)

                assertEquals("new", changedOutput.readText())
                assertEquals("same", unchangedOutput.readText())
            }
        }
    }
