package net.thunderbird.cli.importer

import java.io.File
import net.thunderbird.cli.importer.git.Branch
import net.thunderbird.cli.importer.git.GitRepository
import net.thunderbird.cli.l10n.config.L10nToolsConfig

class ImportContext(val projectRoot: File, val toolsConfig: L10nToolsConfig) {
    val tmpDir: File = File(projectRoot, ".tmp")
    val repository: GitRepository = parseRepository(toolsConfig.source.repository)
    val branches: List<Branch> = toolsConfig.source.branches.map { Branch(it) }

    fun getTmpBranchRootDir(branch: Branch): File {
        return File(tmpDir, "${repository.name}-${branch.value}")
    }

    private fun parseRepository(sourceRepository: String): GitRepository {
        if (sourceRepository.startsWith("https://") || sourceRepository.startsWith("git@")) {
            val name =
                sourceRepository.substringAfterLast("/").removeSuffix(".git").ifBlank { "source" }
            return GitRepository(organization = null, name = name, url = sourceRepository)
        }

        val parts = sourceRepository.split("/", limit = 2)
        require(parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
            "Repository must be in owner/repo format or be a Git URL: $sourceRepository"
        }
        return GitRepository(organization = parts[0], name = parts[1])
    }
}
