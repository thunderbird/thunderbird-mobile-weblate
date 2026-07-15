package net.thunderbird.cli.importer.workflow

import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import net.thunderbird.cli.importer.ImportContext
import net.thunderbird.cli.importer.git.Branch
import net.thunderbird.cli.importer.git.GitClient

class ImportWorkflow(private val context: ImportContext, private val gitClient: GitClient) {
    suspend fun run(all: Boolean, observer: WorkflowObserver) {
        observer.status("🚀 Starting localization import")
        val branches = context.branches

        observer.status("📦 Checking available branches...")
        val availableBranches = gitClient.checkAvailableBranches(branches)

        if (availableBranches.isEmpty()) {
            throw IllegalStateException(
                "No branches found in source repository. Requested: $branches"
            )
        } else if (availableBranches.size < branches.size) {
            val missingBranches = branches - availableBranches
            throw IllegalStateException(
                "Some requested branches are not available in source repository: $missingBranches"
            )
        }

        observer.status("📥 Fetching from branches: ${branches.joinToString(", ")}...")
        val branchResults = gitClient.fetchAllBranches(branches, all)
        val branchRootDirs = mutableMapOf<Branch, File>()

        for (result in branchResults) {
            branchRootDirs[result.branch] = context.getTmpBranchRootDir(result.branch)
            observer.log("   ✓ ${result.branch.value}: ${result.files.size} files")
        }

        observer.status("🧠 Resolving files in memory...")
        val resolvedFiles = RepositoryFileResolver.resolve(branchResults, branchRootDirs)

        observer.status("🔄 Writing files to project root...")
        val copiedCount = RepositoryFileWriter.writeFiles(context.projectRoot, resolvedFiles)

        observer.status("🧹 Cleaning up stale files...")
        val cleanupResult =
            cleanupStaleFiles(context.projectRoot, resolvedFiles.map { it.relativePath }.toSet())
        observer.log("   Removed ${cleanupResult.filesDeleted} stale files")
        observer.log("   Removed ${cleanupResult.dirsDeleted} empty directories")

        observer.status("✅ Import complete!")
        observer.log("   Total unique files: ${resolvedFiles.size}")
        observer.log("   Files written: $copiedCount")
        observer.log("   Files cleaned: ${cleanupResult.filesDeleted}")
        observer.log("   Project root: ${context.projectRoot.absolutePath}")
    }

    private fun cleanupStaleFiles(projectRoot: File, currentFiles: Set<String>): CleanupResult {
        val importPatterns =
            context.toolsConfig.import.sourceFilePatterns +
                context.toolsConfig.import.translatedFilePatterns
        val existingFiles =
            projectRoot
                .walkTopDown()
                .onEnter { dir -> !dir.isProtectedDirectory(projectRoot) }
                .filter { file -> file.isFile }
                .map { file -> file.relativeTo(projectRoot).invariantSeparatorsPath }
                .filter { relativePath -> relativePath.matchesAnyGlob(importPatterns) }
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
            if (parentDir.isProtectedDirectory(projectRoot)) continue
            try {
                if (parentDir.listFiles()?.isEmpty() == true) {
                    parentDir.deleteRecursively()
                    dirsDeleted++
                }
            } catch (_: Exception) {}
        }

        return CleanupResult(filesDeleted, dirsDeleted)
    }

    private fun File.isProtectedDirectory(projectRoot: File): Boolean {
        return this != projectRoot && name in PROTECTED_DIRECTORIES
    }

    private fun String.matchesAnyGlob(patterns: List<String>): Boolean {
        val path = Path.of(this)
        return patterns.any { pattern ->
            FileSystems.getDefault().getPathMatcher("glob:$pattern").matches(path)
        }
    }

    private data class CleanupResult(val filesDeleted: Int, val dirsDeleted: Int)

    private companion object {
        val PROTECTED_DIRECTORIES =
            setOf(
                ".git",
                ".github",
                ".gradle",
                ".idea",
                ".kotlin",
                ".tmp",
                "build",
                "gradle",
                "weblate",
            )
    }
}
