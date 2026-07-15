package net.thunderbird.cli.importer.workflow

import net.thunderbird.cli.importer.git.BranchFiles
import net.thunderbird.cli.importer.git.Branch
import java.io.File

object RepositoryFileResolver {
    fun resolve(
        branchResults: List<BranchFiles>,
        branchRoots: Map<Branch, File>,
    ): List<ResolvedRepositoryFile> {
        val resolvedFiles = linkedMapOf<String, ResolvedRepositoryFile>()

        for (branchResult in branchResults) {
            val branchRoot = branchRoots[branchResult.branch] ?: continue

            for (relativePath in branchResult.files) {
                if (resolvedFiles.containsKey(relativePath)) continue

                val sourceFile = File(branchRoot, relativePath)
                if (!sourceFile.exists()) continue

                resolvedFiles[relativePath] = ResolvedRepositoryFile(
                    relativePath = relativePath,
                    sourceBranch = branchResult.branch,
                    content = sourceFile.readText(),
                )
            }
        }

        return resolvedFiles.values.sortedBy { it.relativePath }
    }
}
