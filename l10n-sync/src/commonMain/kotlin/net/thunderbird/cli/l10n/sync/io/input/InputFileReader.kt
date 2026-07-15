package net.thunderbird.cli.l10n.sync.io.input

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import net.thunderbird.cli.l10n.config.Branch
import net.thunderbird.cli.l10n.config.readText
import net.thunderbird.cli.l10n.config.resolve

class InputFileReader {
    fun readFiles(
        relativePath: String,
        branches: List<Branch>,
        branchRoots: Map<Branch, Path>,
    ): List<InputFile> {
        return branches.mapNotNull { branch ->
            val branchRoot = branchRoots[branch] ?: return@mapNotNull null
            val branchFile = branchRoot.resolve(relativePath)
            if (!SystemFileSystem.exists(branchFile)) return@mapNotNull null

            InputFile(
                relativePath = relativePath,
                sourceBranch = branch,
                content = branchFile.readText(),
            )
        }
    }
}
