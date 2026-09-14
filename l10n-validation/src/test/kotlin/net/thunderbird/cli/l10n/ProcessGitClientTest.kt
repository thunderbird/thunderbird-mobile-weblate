package net.thunderbird.cli.l10n

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test

class ProcessGitClientTest {
    private val repository = Files.createTempDirectory("l10n-validation-git").toFile()

    @AfterTest
    fun tearDown() {
        repository.deleteRecursively()
    }

    @Test
    fun `reads files and changed paths from Git refs`() {
        runGit("init", "--quiet")
        runGit("config", "user.name", "Test")
        runGit("config", "user.email", "test@example.invalid")
        runGit("config", "commit.gpgsign", "false")
        val path = "feature/example/src/main/res/values/strings.xml"
        repository.resolve(path).apply {
            parentFile.mkdirs()
            writeText("<resources><string name=\"example\">Base</string></resources>\n")
        }
        runGit("add", path)
        runGit("commit", "--quiet", "--message", "base")
        val baseRef = runGit("rev-parse", "HEAD").trim()

        repository
            .resolve(path)
            .writeText("<resources><string name=\"example\">Head</string></resources>\n")
        runGit("add", path)
        runGit("commit", "--quiet", "--message", "head")
        val headRef = runGit("rev-parse", "HEAD").trim()
        val testSubject = ProcessGitClient(repository)

        assertThat(testSubject.changedFiles(baseRef, headRef)).containsExactly(path)
        assertThat(testSubject.files(headRef)).containsExactly(path)
        assertThat(testSubject.readFile(baseRef, path))
            .isEqualTo("<resources><string name=\"example\">Base</string></resources>\n")
    }

    @Test
    fun `resource validation command checks another repository`() {
        runGit("init", "--quiet")
        runGit("config", "user.name", "Test")
        runGit("config", "user.email", "test@example.invalid")
        runGit("config", "commit.gpgsign", "false")
        runGit("commit", "--quiet", "--allow-empty", "--message", "base")
        val baseRef = runGit("rev-parse", "HEAD").trim()
        val path = "feature/example/src/commonMain/composeResources/values-de/messages.xml"
        repository.resolve(path).apply {
            parentFile.mkdirs()
            writeText("<resources><string name=\"example\">Invalid %s</string></resources>\n")
        }
        val sourcePath = "feature/example/src/commonMain/composeResources/values/messages.xml"
        repository.resolve(sourcePath).apply {
            parentFile.mkdirs()
            writeText("<resources><string name=\"example\">Valid %1\$s</string></resources>\n")
        }
        runGit("add", path, sourcePath)
        runGit("commit", "--quiet", "--message", "head")
        val headRef = runGit("rev-parse", "HEAD").trim()
        val testSubject = L10nValidationCli()

        val result =
            testSubject.run(
                listOf(
                    "validate-resource-changes",
                    "--repository-root",
                    repository.absolutePath,
                    "--base-ref",
                    baseRef,
                    "--head-ref",
                    headRef,
                )
            )

        assertThat(result.exitCode).isEqualTo(1)
        assertThat(result.message).contains("uses invalid placeholder %s")
    }

    private fun runGit(vararg arguments: String): String {
        val process =
            ProcessBuilder(listOf("git", "-C", repository.absolutePath) + arguments)
                .redirectErrorStream(true)
                .start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        check(process.waitFor() == 0) { output }
        return output
    }
}
