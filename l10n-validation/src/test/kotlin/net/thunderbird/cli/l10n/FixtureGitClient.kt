package net.thunderbird.cli.l10n

import java.io.File

internal class FixtureGitClient(private val root: File) : GitClient {
    override fun changedFiles(baseRef: String, headRef: String): List<String> {
        val baseFiles = filesAt(baseRef)
        val headFiles = filesAt(headRef)
        return (baseFiles.keys + headFiles.keys)
            .filter { path -> baseFiles[path] != headFiles[path] }
            .sorted()
    }

    override fun files(ref: String): List<String> = filesAt(ref).keys.sorted()

    override fun readFile(ref: String, path: String): String? = filesAt(ref)[path]

    private fun filesAt(ref: String): Map<String, String> {
        val refRoot = root.resolve(ref)
        if (!refRoot.exists()) return emptyMap()
        return refRoot.walkTopDown().filter(File::isFile).associate { file ->
            file.relativeTo(refRoot).invariantSeparatorsPath to file.readText()
        }
    }
}
