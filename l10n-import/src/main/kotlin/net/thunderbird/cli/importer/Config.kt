package net.thunderbird.cli.importer

import java.io.File
import net.thunderbird.cli.importer.git.Branch
import net.thunderbird.cli.importer.git.GitRepository
import net.thunderbird.cli.l10n.config.ImportConfig

data class Config(
    val tmpDir: File,
    val targetDir: File,
    val repository: GitRepository,
    val import: ImportConfig = ImportConfig(),
) {
    fun getTmpBranchRootDir(branch: Branch): File {
        return File(tmpDir, "${repository.name}-${branch.value}")
    }
}
