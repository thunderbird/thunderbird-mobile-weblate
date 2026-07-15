package net.thunderbird.cli.l10n.config

private val globCharacters = setOf('*', '?', '[', ']')

fun String.isExcludedPath(patterns: List<String>): Boolean {
    val path = invariantSeparatorsPath()
    return patterns.any { pattern ->
        path.contains(pattern.invariantSeparatorsPath()) || path.matchesGlob(pattern)
    }
}

private fun String.matchesGlob(pattern: String): Boolean {
    return globToRegex(pattern.invariantSeparatorsPath()).matches(invariantSeparatorsPath())
}

private fun String.invariantSeparatorsPath(): String = replace('\\', '/')

private fun globToRegex(pattern: String): Regex {
    if (pattern.none { it in globCharacters }) {
        return Regex(Regex.escape(pattern))
    }

    val regex = buildString {
        append("^")
        var index = 0
        while (index < pattern.length) {
            val char = pattern[index]
            when (char) {
                '*' -> {
                    if (index + 1 < pattern.length && pattern[index + 1] == '*') {
                        append(".*")
                        index++
                    } else {
                        append("[^/]*")
                    }
                }
                '?' -> append("[^/]")
                else -> append(Regex.escape(char.toString()))
            }
            index++
        }
        append("$")
    }
    return Regex(regex)
}
