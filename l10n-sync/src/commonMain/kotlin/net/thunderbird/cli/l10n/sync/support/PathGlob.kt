package net.thunderbird.cli.l10n.sync.support

fun String.matchesAnyPathGlob(patterns: List<String>): Boolean {
    return patterns.any { pattern -> matchesPathGlob(pattern) }
}

fun String.matchesPathGlob(pattern: String): Boolean {
    val normalizedPath = replace('\\', '/')
    val normalizedPattern = pattern.replace('\\', '/')
    val regex = buildString {
        append("^")
        var index = 0
        while (index < normalizedPattern.length) {
            val char = normalizedPattern[index]
            when (char) {
                '*' -> {
                    if (
                        index + 1 < normalizedPattern.length && normalizedPattern[index + 1] == '*'
                    ) {
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
    return Regex(regex).matches(normalizedPath)
}
