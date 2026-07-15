package net.thunderbird.cli.l10n.sync.io.git

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import net.thunderbird.cli.l10n.config.Branch
import net.thunderbird.cli.l10n.config.L10nConfig
import net.thunderbird.cli.l10n.config.isExcludedPath
import net.thunderbird.cli.l10n.config.isInModule
import net.thunderbird.cli.l10n.sync.support.ProcessRunner
import net.thunderbird.cli.l10n.sync.support.matchesAnyPathGlob

interface GitClient {
    suspend fun checkAvailableBranches(branches: List<Branch>): List<Branch>

    suspend fun fetchAllBranches(branches: List<Branch>, all: Boolean): List<BranchFiles>
}

class DefaultGitClient(
    private val context: L10nConfig,
    private val processRunner: ProcessRunner = ProcessRunner(),
) : GitClient {
    override suspend fun checkAvailableBranches(branches: List<Branch>): List<Branch> {
        val output =
            runGit(projectRoot(), "ls-remote", "--heads", context.project.source.repository.url)
        val remoteBranches = parseRemoteBranches(output)

        return branches.filter { it.value in remoteBranches }
    }

    override suspend fun fetchAllBranches(branches: List<Branch>, all: Boolean): List<BranchFiles> =
        coroutineScope {
            branches
                .map { branch ->
                    async {
                        val branchRoot = context.getBranchWorkDir(branch)
                        fetchBranch(branch, branchRoot)
                        fetchBranchFiles(branch, branchRoot, all)
                    }
                }
                .awaitAll()
                .sortedBy { branches.indexOf(it.branch) }
        }

    private suspend fun fetchBranch(branch: Branch, branchRoot: Path) {
        if (!branchExists(branchRoot)) {
            branchRoot.parent?.let { parent -> SystemFileSystem.createDirectories(parent) }
            runGit(
                workDir = projectRoot(),
                "clone",
                "--depth",
                "1",
                "--branch",
                branch.value,
                "--single-branch",
                context.project.source.repository.url,
                branchRoot.toString(),
            )
        } else {
            runGit(branchRoot, "fetch", "--depth", "1", "origin", branch.value)
            runGit(branchRoot, "reset", "--hard", "origin/${branch.value}")
        }
    }

    private fun projectRoot(): Path = context.projectRoot

    private suspend fun fetchBranchFiles(
        branch: Branch,
        branchRoot: Path,
        all: Boolean,
    ): BranchFiles {
        val output = runGit(branchRoot, "ls-files")
        val includePatterns =
            if (all) {
                context.project.import.sourceFilePatterns +
                    context.project.import.translatedFilePatterns
            } else {
                context.project.import.sourceFilePatterns
            }
        val excludePatterns = context.project.import.excludedPaths
        val files =
            output
                .lines()
                .filter { it.isNotBlank() }
                .filter {
                    it.matchesAnyPathGlob(includePatterns) && !it.isExcludedPath(excludePatterns)
                }
                .filter { path -> context.project.ignoredModules.none { path.isInModule(it) } }
        return BranchFiles(branch, files)
    }

    private suspend fun runGit(workDir: Path, vararg args: String): String {
        val command = listOf("git", "-C", SystemFileSystem.resolve(workDir).toString()) + args
        val result = processRunner.run(command)
        if (result.exitCode != 0) {
            throw IllegalStateException(
                "Git command failed: git ${args.joinToString(" ")}\n${result.output}"
            )
        }
        return result.output
    }

    private fun branchExists(branchRoot: Path): Boolean =
        SystemFileSystem.exists(branchRoot) &&
            SystemFileSystem.exists(Path(branchRoot.toString(), ".git"))
}

internal fun parseRemoteBranches(output: String): Set<String> =
    output
        .lineSequence()
        .mapNotNull { line ->
            line.substringAfter("\trefs/heads/", missingDelimiterValue = "").takeIf {
                it.isNotBlank()
            }
        }
        .toSet()
