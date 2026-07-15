package net.thunderbird.cli.l10n.config

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString

fun Path.listRegularFilesRecursively(skipDirectory: (Path) -> Boolean = { false }): List<Path> {
    val results = mutableListOf<Path>()

    fun visit(directory: Path) {
        if (skipDirectory(directory)) return
        SystemFileSystem.list(directory).forEach { path ->
            val metadata = SystemFileSystem.metadataOrNull(path) ?: return@forEach
            when {
                metadata.isRegularFile -> results += path
                metadata.isDirectory -> visit(path)
            }
        }
    }

    visit(this)
    return results
}

fun Path.parentDirectoriesUntil(root: Path): List<Path> {
    val directories = mutableListOf<Path>()
    val resolvedRoot = SystemFileSystem.resolve(root)
    var directory = parent

    while (directory != null && SystemFileSystem.resolve(directory) != resolvedRoot) {
        directories += directory
        directory = directory.parent
    }

    return directories
}

fun Path.readText(): String {
    val source = SystemFileSystem.source(this).buffered()
    return source.use { it.readString() }
}

fun Path.writeText(text: String) {
    parent?.let { parent -> SystemFileSystem.createDirectories(parent) }
    val sink = SystemFileSystem.sink(this).buffered()
    sink.use { it.writeString(text) }
}

fun Path.relativeTo(root: Path): String {
    val rootPath = SystemFileSystem.resolve(root).toString().trimEnd('/')
    val path = SystemFileSystem.resolve(this).toString()
    return path.removePrefix("$rootPath/").replace('\\', '/')
}

fun Path.resolve(relativePath: String): Path = Path(toString(), relativePath)
