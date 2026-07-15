package net.thunderbird.cli.importer

import de.infix.testBalloon.framework.core.testSuite
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import net.thunderbird.cli.importer.git.Branch
import net.thunderbird.cli.importer.git.GitRepository

val ConfigTests by testSuite("Config") {
  test("branch root uses correct repo name") {
    val config = Config(
      tmpDir = Files.createTempDirectory("cfg-").toFile(),
      targetDir = Files.createTempDirectory("target-").toFile(),
      repository = GitRepository("thunderbird", "thunderbird-android"),
    )

    assertEquals("thunderbird-android-main", config.getTmpBranchRootDir(Branch("main")).name)
    assertTrue(config.getTmpBranchRootDir(Branch("main")).path.contains("cfg-"))
  }

  test("parses repository name correctly") {
    val config = Config(
      tmpDir = Files.createTempDirectory("cfg-").toFile(),
      targetDir = Files.createTempDirectory("target-").toFile(),
      repository = GitRepository("owner", "repository-name"),
    )
    assertEquals("repository-name-main", config.getTmpBranchRootDir(Branch("main")).name)
  }

  testSuite("handles various repository formats") {
    listOf(
        "owner/repo" to "repo",
        "org/multi-word-repo" to "multi-word-repo",
        "user/repo-123" to "repo-123",
    )
        .forEach { (input, expectedName) ->
          test("$input -> $expectedName") {
            val parts = input.split("/")
            val config = Config(
              tmpDir = Files.createTempDirectory("cfg-").toFile(),
              targetDir = Files.createTempDirectory("target-").toFile(),
              repository = GitRepository(parts[0], parts[1]),
            )
            assertEquals("$expectedName-main", config.getTmpBranchRootDir(Branch("main")).name)
          }
        }
  }

  test("uses provided temporary directory") {
    val tmpDir = Files.createTempDirectory("test-")
    try {
      val config = Config(
        tmpDir = tmpDir.toFile(),
        targetDir = Files.createTempDirectory("target-").toFile(),
        repository = GitRepository("test", "repo"),
      )
      assertEquals("repo-beta", config.getTmpBranchRootDir(Branch("beta")).name)
    } finally {
      tmpDir.toFile().deleteRecursively()
    }
  }
}
