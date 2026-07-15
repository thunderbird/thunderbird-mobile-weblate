package net.thunderbird.cli.l10n.sync.support

import de.infix.testBalloon.framework.core.TestFixture
import de.infix.testBalloon.framework.core.TestSuiteScope
import de.infix.testBalloon.framework.core.testPlatform
import kotlin.random.Random
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory

fun TestSuiteScope.temporaryDirectoryFixture(
    prefix: String = "$testSuiteInScope-"
): TestFixture<Path> =
    testFixture {
        Path(SystemTemporaryDirectory, "$prefix${Random.nextLong().toString().replace("-", "n")}")
            .also { directory -> SystemFileSystem.createDirectories(directory, mustCreate = true) }
    } closeWith
        { testsSucceeded ->
            if (testsSucceeded || testPlatform.environment("CI") != null) {
                deleteRecursively(this)
            } else {
                println("Temporary directory: $this")
            }
        }

private fun deleteRecursively(path: Path) {
    val metadata = SystemFileSystem.metadataOrNull(path) ?: return
    if (metadata.isDirectory) {
        SystemFileSystem.list(path).forEach { child -> deleteRecursively(child) }
    }
    SystemFileSystem.delete(path, mustExist = false)
}
