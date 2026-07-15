package net.thunderbird.cli.l10n.config

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

object ProjectRootResolver {
    fun resolve(startDir: Path): Path {
        val resolvedStartDir = SystemFileSystem.resolve(startDir)
        var current = resolvedStartDir
        while (current.parent != null) {
            if (
                SystemFileSystem.exists(Path(current.toString(), "settings.gradle.kts")) ||
                    SystemFileSystem.exists(Path(current.toString(), ".git"))
            ) {
                return current
            }
            current = current.parent ?: break
        }
        return resolvedStartDir
    }
}
