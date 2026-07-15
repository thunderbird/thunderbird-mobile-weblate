package net.thunderbird.cli.l10n.sync.io.input

import kotlinx.io.files.Path
import net.thunderbird.cli.l10n.config.Branch
import net.thunderbird.cli.l10n.config.L10nConfig
import net.thunderbird.cli.l10n.sync.io.git.BranchFiles
import net.thunderbird.cli.l10n.sync.io.git.GitClient
import net.thunderbird.cli.l10n.sync.model.L10nFile

class L10nFileLoader(
    private val gitClient: GitClient,
    private val config: L10nConfig,
    private val inputFileReader: InputFileReader = InputFileReader(),
    private val inputFileMapper: InputFileMapper = InputFileMapper(),
) {
    suspend fun load(branches: List<Branch>, all: Boolean): L10nFileLoadResult {
        val branchResults = gitClient.fetchAllBranches(branches, all)
        val branchRoots = branchResults.associate { result ->
            result.branch to config.getBranchWorkDir(result.branch)
        }
        return load(branchResults = branchResults, branchRoots = branchRoots)
    }

    private fun load(
        branchResults: List<BranchFiles>,
        branchRoots: Map<Branch, Path>,
    ): L10nFileLoadResult {
        val filesByPath = linkedMapOf<String, MutableList<Branch>>()

        branchResults.forEach { branchResult ->
            branchResult.files.forEach { relativePath ->
                filesByPath.getOrPut(relativePath) { mutableListOf() } += branchResult.branch
            }
        }

        val branches = branchResults.map { it.branch }
        val files = mutableListOf<L10nFile>()

        for (relativePath in filesByPath.keys.sorted()) {
            val inputFiles =
                inputFileReader.readFiles(
                    relativePath = relativePath,
                    branches = branches,
                    branchRoots = branchRoots,
                )
            files += inputFileMapper.mapFiles(inputFiles, source = true)
        }

        return L10nFileLoadResult(
            files = files,
            fileCountsByBranch =
                branchResults.associate { result -> result.branch to result.files.size },
        )
    }
}

data class L10nFileLoadResult(val files: List<L10nFile>, val fileCountsByBranch: Map<Branch, Int>)
