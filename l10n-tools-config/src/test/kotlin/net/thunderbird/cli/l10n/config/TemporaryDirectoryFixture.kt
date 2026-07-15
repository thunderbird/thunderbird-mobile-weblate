package net.thunderbird.cli.l10n.config

import de.infix.testBalloon.framework.core.TestFixture
import de.infix.testBalloon.framework.core.TestSuiteScope
import de.infix.testBalloon.framework.core.testPlatform
import java.nio.file.Files
import java.nio.file.Path

fun TestSuiteScope.temporaryDirectoryFixture(
    prefix: String = "$testSuiteInScope-"
): TestFixture<Path> =
    testFixture { Files.createTempDirectory(prefix) } closeWith
        { testsSucceeded ->
            if (testsSucceeded || testPlatform.environment("CI") != null) {
                toFile().deleteRecursively()
            } else {
                println("Temporary directory: $this")
            }
        }
