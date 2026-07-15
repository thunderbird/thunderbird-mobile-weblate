package net.thunderbird.cli.importer.git

import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import net.thunderbird.cli.importer.Config

class GitClient(
    private val config: Config,
) {
    suspend fun checkAvailableBranches(branches: List<Branch>): List<Branch> {
        val output = runGit(config.targetDir, "ls-remote", "--heads", config.repository.url)
        val remoteBranches = output.lines()
            .filter { it.isNotBlank() }
            .map { it.substringAfterLast("/") }
            .toSet()

        return branches.filter { it.value in remoteBranches }
    }

    suspend fun fetchAllBranches(
        branches: List<Branch>,
        all: Boolean,
    ): List<BranchFiles> = coroutineScope {
        branches
            .map { branch ->
                async {
                    val branchRoot = config.getTmpBranchRootDir(branch)
                    fetchBranch(branch, branchRoot)
                    fetchBranchFiles(branch, branchRoot, all)
                }
            }
            .awaitAll()
            .sortedBy { branches.indexOf(it.branch) }
    }


    private suspend fun fetchBranch(branch: Branch, branchRoot: File) {
        if (!branchRoot.exists() || !File(branchRoot, ".git").exists()) {
            runGit(
                workDir = config.targetDir,
                "clone", "--depth", "1", "--branch", branch.value, "--single-branch",
                config.repository.url, branchRoot.absolutePath,
            )
        } else {
            runGit(branchRoot, "fetch", "--depth", "1", "origin", branch.value)
            runGit(branchRoot, "reset", "--hard", "origin/${branch.value}")
        }
    }

    private suspend fun fetchBranchFiles(branch: Branch, branchRoot: File, all: Boolean): BranchFiles {
        val output = runGit(branchRoot, "ls-files")
        val includePatterns = if (all) {
            config.import.translatedFilePatterns
        } else {
            config.import.sourceFilePatterns
        }
        val excludePatterns = config.import.excludedFilePatterns
        val files = output.lines()
            .filter { it.isNotBlank() }
            .filter { it.matchesAnyGlob(includePatterns) && !it.matchesAnyGlob(excludePatterns) }
        return BranchFiles(branch, files)
    }


    private suspend fun runGit(workDir: File, vararg args: String): String = withContext(Dispatchers.IO) {
        val process = ProcessBuilder("git", "-C", workDir.absolutePath, *args)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw IllegalStateException("Git command failed: git ${args.joinToString(" ")}\n$output")
        }
        output
    }

    private fun String.matchesAnyGlob(patterns: List<String>): Boolean {
        val path = Path.of(this)
        return patterns.any { pattern ->
            FileSystems.getDefault().getPathMatcher("glob:$pattern").matches(path)
        }
    }
}
