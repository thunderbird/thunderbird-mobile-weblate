package net.thunderbird.cli.l10n.config

import kotlinx.io.files.Path

data class L10nConfig(val projectRoot: Path, val project: L10nProjectConfig) {
    val workDir: Path = Path(projectRoot.toString(), ".tmp")

    fun getBranchWorkDir(branch: Branch): Path {
        return Path(workDir.toString(), branch.value)
    }
}
