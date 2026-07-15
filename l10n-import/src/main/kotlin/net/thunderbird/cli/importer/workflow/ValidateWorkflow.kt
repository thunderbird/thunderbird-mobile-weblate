package net.thunderbird.cli.importer.workflow

import java.io.File
import net.thunderbird.cli.importer.Config
import net.thunderbird.cli.importer.command.Command
import net.thunderbird.cli.importer.git.Branch
import net.thunderbird.cli.importer.git.GitClient
import net.thunderbird.cli.importer.translation.TranslationFileWriter
import net.thunderbird.cli.importer.translation.TranslationKeyConsolidator

class ValidateWorkflow(
    private val config: Config,
    private val gitClient: GitClient,
) {
    suspend fun run(command: Command.Validate, observer: WorkflowObserver) {
        observer.status("🔑 Starting source key validation...")
        val branches = requireNotNull(command.branches)

        val outputRoot = resolveOutputRoot(command.outputDir, config.targetDir)

        observer.status("📦 Checking available branches...")
        val availableBranches = gitClient.checkAvailableBranches(branches)

        if (availableBranches.isEmpty()) {
            throw IllegalStateException("No branches found in source repository. Requested: $branches")
        }

        observer.status("📥 Fetching from branches: ${availableBranches.joinToString(", ")}...")
        val branchResults = gitClient.fetchAllBranches(availableBranches, all = false)

        val allFiles = mutableSetOf<String>()
        val branchRootDirs = mutableMapOf<String, File>()
        for (result in branchResults) {
            allFiles.addAll(result.files)
            branchRootDirs[result.branch.value] = config.getTmpBranchRootDir(result.branch)
            observer.log("   ✓ ${result.branch.value}: ${result.files.size} source files")
        }

        observer.status("🔄 Validating translation keys...")
        val consolidator = TranslationKeyConsolidator()
        val result = consolidator.consolidateAllFiles(
            branches = availableBranches.map { it.value },
            allFiles = allFiles,
            branchRootDirs = branchRootDirs,
        )

        if (result.conflicts.isNotEmpty()) {
            observer.log("⚠️  Found ${result.conflicts.size} key conflicts:")
            for (conflict in result.conflicts) {
                observer.log("   ❌ Key '${conflict.id}' in ${conflict.filePath}")
                for (branchContent in conflict.conflicts) {
                    observer.log("      ${branchContent.branch}: ${branchContent.content}")
                }
            }
            val conflictCount = result.conflicts.size
            val fileCount = result.conflicts.map { it.filePath }.toSet().size
            throw IllegalStateException(
                "Key validation failed: $conflictCount key conflicts across $fileCount files. " +
                    "Keys must be identical across branches or be new keys.",
            )
        }

        observer.status("🔄 Writing consolidated files...")
        TranslationFileWriter.writeFiles(outputRoot, result.files)

        observer.status("✅ Validation complete!")
        observer.log("   Files consolidated: ${result.filesConsolidated}")
        observer.log("   Files with conflicts: ${result.filesWithConflicts}")
        observer.log("   Output directory: ${outputRoot.absolutePath}")
    }

    private fun resolveOutputRoot(outputDir: String, projectRoot: File): File {
        if (outputDir == ".") return projectRoot
        val output = File(outputDir)
        return if (output.isAbsolute) output else File(projectRoot, outputDir)
    }
}
