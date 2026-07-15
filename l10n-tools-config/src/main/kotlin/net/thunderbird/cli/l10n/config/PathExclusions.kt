package net.thunderbird.cli.l10n.config

import java.nio.file.FileSystems
import java.nio.file.Path

fun String.isExcludedPath(patterns: List<String>): Boolean {
    val path = Path.of(this)
    return patterns.any { pattern ->
        contains(pattern) || FileSystems.getDefault().getPathMatcher("glob:$pattern").matches(path)
    }
}
