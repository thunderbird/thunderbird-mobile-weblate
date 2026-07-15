package net.thunderbird.cli.l10n.sync.task

import net.thunderbird.cli.l10n.config.Branch
import net.thunderbird.cli.l10n.sync.model.L10nFile
import net.thunderbird.cli.l10n.sync.model.SourceResourceFile

object BranchDifferences {
    fun calculate(files: List<L10nFile>): BranchDifferenceReport {
        val uniqueFiles =
            files
                .groupBy { it.relativePath }
                .mapNotNull { (path, filesForPath) ->
                    filesForPath
                        .map { it.branch }
                        .distinct()
                        .singleOrNull()
                        ?.let { branch -> BranchFileDifference(branch, path) }
                }
                .sortedWith(compareBy<BranchFileDifference> { it.branch.value }.thenBy { it.path })

        val uniqueKeys =
            files
                .filterIsInstance<SourceResourceFile>()
                .filter { it.relativePath.isSourceResourceFile() }
                .flatMap { file ->
                    file.keys.map { key ->
                        BranchKeyDifference(file.branch, file.relativePath, key.id)
                    }
                }
                .groupBy { it.path to it.key }
                .mapNotNull { (_, keySources) ->
                    keySources
                        .map { it.branch }
                        .distinct()
                        .singleOrNull()
                        ?.let { branch -> keySources.first().copy(branch = branch) }
                }
                .sortedWith(
                    compareBy<BranchKeyDifference> { it.branch.value }
                        .thenBy { it.path }
                        .thenBy { it.key }
                )

        return BranchDifferenceReport(uniqueFiles = uniqueFiles, uniqueKeys = uniqueKeys)
    }

    private fun String.isSourceResourceFile(): Boolean =
        endsWith("/res/values/strings.xml") || endsWith("/composeResources/values/strings.xml")
}

data class BranchDifferenceReport(
    val uniqueFiles: List<BranchFileDifference>,
    val uniqueKeys: List<BranchKeyDifference>,
)

data class BranchFileDifference(val branch: Branch, val path: String)

data class BranchKeyDifference(val branch: Branch, val path: String, val key: String)
