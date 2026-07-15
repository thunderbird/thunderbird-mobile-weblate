package net.thunderbird.cli.l10n.sync.task

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import net.thunderbird.cli.l10n.sync.support.ProcessResult
import net.thunderbird.cli.l10n.sync.support.ProcessRunner

fun interface TargetBranchValidator {
    suspend fun validate(targetRoot: Path, branch: String)
}

class GitTargetBranchValidator(
    private val runCommand: suspend (List<String>) -> ProcessResult = ProcessRunner()::run
) : TargetBranchValidator {
    override suspend fun validate(targetRoot: Path, branch: String) {
        val gitRoot = SystemFileSystem.resolve(targetRoot).toString()
        require(runGit(gitRoot, "rev-parse", "--is-inside-work-tree").output.trim() == "true") {
            "Export target is not a Git checkout: $targetRoot"
        }

        val currentBranch = runGit(gitRoot, "branch", "--show-current").output.trim()
        if (currentBranch == branch) return

        val ancestor = runGit(gitRoot, "merge-base", "--is-ancestor", branch, "HEAD")
        require(ancestor.exitCode == 0) {
            "Export target branch '${currentBranch.ifBlank { "detached HEAD" }}' is not based on '$branch'. " +
                "Check out '$branch' or a branch created from it before exporting."
        }
    }

    private suspend fun runGit(root: String, vararg arguments: String): ProcessResult =
        runCommand(listOf("git", "-C", root) + arguments)
}
