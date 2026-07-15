package net.thunderbird.cli.importer.workflow

import java.io.File
import net.thunderbird.cli.importer.Config
import net.thunderbird.cli.importer.command.Command
import net.thunderbird.cli.importer.git.Branch
import net.thunderbird.cli.importer.git.GitClient

class ImportWorkflow(
    private val config: Config,
    private val gitClient: GitClient,
) {
    suspend fun run(command: Command.Import, observer: WorkflowObserver) {
        observer.status("🚀 Starting localization import")
        val branches = requireNotNull(command.branches)

        observer.status("📦 Checking available branches...")
        val availableBranches = gitClient.checkAvailableBranches(branches)

        if (availableBranches.isEmpty()) {
            throw IllegalStateException("No branches found in source repository. Requested: $branches")
        } else if (availableBranches.size < branches.size) {
            val missingBranches = branches - availableBranches
            throw IllegalStateException("Some requested branches are not available in source repository: $missingBranches")
        }

        observer.status("📥 Fetching from branches: ${branches.joinToString(", ")}...")
        val branchResults = gitClient.fetchAllBranches(branches, command.all)
        val branchRootDirs = mutableMapOf<Branch, File>()

        for (result in branchResults) {
            branchRootDirs[result.branch] = config.getTmpBranchRootDir(result.branch)
            observer.log("   ✓ ${result.branch.value}: ${result.files.size} files")
        }

        observer.status("🧠 Resolving files in memory...")
        val resolvedFiles = RepositoryFileResolver.resolve(branchResults, branchRootDirs)

        observer.status("🔄 Writing files to project root...")
        val copiedCount = RepositoryFileWriter.writeFiles(config.targetDir, resolvedFiles)

        observer.status("🧹 Cleaning up stale files...")
        val cleanupResult = cleanupStaleFiles(config.targetDir, resolvedFiles.map { it.relativePath }.toSet())
        observer.log("   Removed ${cleanupResult.filesDeleted} stale files")
        observer.log("   Removed ${cleanupResult.dirsDeleted} empty directories")

        observer.status("✅ Import complete!")
        observer.log("   Total unique files: ${resolvedFiles.size}")
        observer.log("   Files written: $copiedCount")
        observer.log("   Files cleaned: ${cleanupResult.filesDeleted}")
        observer.log("   Project root: ${config.targetDir.absolutePath}")
    }

    private fun cleanupStaleFiles(projectRoot: File, currentFiles: Set<String>): CleanupResult {
        val trackedPrefixes = config.import.cleanupRoots.map { root ->
            if (root.endsWith("/")) root else "$root/"
        }

        val existingFiles = trackedPrefixes
            .map { File(projectRoot, it) }
            .filter { it.exists() }
            .flatMap { dir ->
                dir.walkTopDown()
                    .filter { it.isFile && (it.name == "strings.xml" || it.name.endsWith(".txt")) }
                    .map { it.relativeTo(projectRoot).path }
            }
            .toSet()

        val staleFiles = existingFiles - currentFiles
        var filesDeleted = 0
        var dirsDeleted = 0

        for (staleFile in staleFiles) {
            val file = File(projectRoot, staleFile)
            if (file.exists() && file.isFile) {
                file.delete()
                filesDeleted++
            }
        }

        for (staleFile in staleFiles.sortedByDescending { it.length }) {
            val parentDir = File(projectRoot, staleFile).parentFile ?: continue
            if (parentDir == projectRoot || !parentDir.isDirectory) continue
            val relativePath = parentDir.relativeTo(projectRoot).path
            if (trackedPrefixes.none { relativePath.startsWith(it) }) continue
            try {
                if (parentDir.listFiles()?.isEmpty() == true) {
                    parentDir.deleteRecursively()
                    dirsDeleted++
                }
            } catch (_: Exception) {
            }
        }

        return CleanupResult(filesDeleted, dirsDeleted)
    }

    private data class CleanupResult(val filesDeleted: Int, val dirsDeleted: Int)
}
