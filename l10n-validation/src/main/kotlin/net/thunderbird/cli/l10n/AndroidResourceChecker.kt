package net.thunderbird.cli.l10n

internal class AndroidResourceChecker(
    private val gitClient: GitClient,
    private val resourceParser: ResourceParser,
) {
    fun check(path: String, ref: String): List<String> {
        val content = gitClient.readFile(ref, path)
        val formatFailures = resourceParser.validateAndroid(content, path, ref)
        val sourcePath = androidSourceResourcePath(path) ?: return formatFailures
        val sourceEntries =
            resourceParser.parse(gitClient.readFile(ref, sourcePath), sourcePath, ref)
        val translatedEntries = resourceParser.parse(content, path, ref)
        return formatFailures +
            checkTranslationEntries(path, sourcePath, translatedEntries, sourceEntries)
    }

    private fun checkTranslationEntries(
        path: String,
        sourcePath: String,
        translatedEntries: Map<String, ResourceEntry>,
        sourceEntries: Map<String, ResourceEntry>,
    ): List<String> = buildList {
        translatedEntries.toSortedMap().forEach { (key, translatedEntry) ->
            val sourceEntry = sourceEntries[key]
            when {
                sourceEntry == null ->
                    add("$path: translated $key has no source entry in $sourcePath.")

                !sourceEntry.isTranslatable ->
                    add("$path: translated $key is not translatable in $sourcePath.")

                translatedEntry.invalidAndroidPlaceholders.isNotEmpty() -> Unit

                key.startsWith("plurals:") ->
                    addAll(
                        checkPluralPlaceholders(path, sourcePath, key, translatedEntry, sourceEntry)
                    )

                key.startsWith("string-array:") ->
                    addAll(
                        checkArrayPlaceholders(path, sourcePath, key, translatedEntry, sourceEntry)
                    )

                translatedEntry.androidPlaceholders != sourceEntry.androidPlaceholders ->
                    add(
                        "$path: translated $key uses Android placeholders " +
                            "${translatedEntry.androidPlaceholders.render()} instead of " +
                            "${sourceEntry.androidPlaceholders.render()} from $sourcePath."
                    )
            }
        }
    }

    private fun checkPluralPlaceholders(
        path: String,
        sourcePath: String,
        key: String,
        translatedEntry: ResourceEntry,
        sourceEntry: ResourceEntry,
    ): List<String> = buildList {
        translatedEntry.androidPlaceholdersByQuantity.toSortedMap().forEach {
            (quantity, translatedPlaceholders) ->
            val sourcePlaceholders =
                sourceEntry.androidPlaceholdersByQuantity.values.flatten().toSet().ifEmpty {
                    sourceEntry.androidPlaceholders
                }
            if (!sourcePlaceholders.containsAll(translatedPlaceholders)) {
                add(
                    "$path: translated $key quantity $quantity uses Android placeholders " +
                        "${translatedPlaceholders.render()} instead of " +
                        "${sourcePlaceholders.render()} from $sourcePath."
                )
            }
        }
    }

    private fun checkArrayPlaceholders(
        path: String,
        sourcePath: String,
        key: String,
        translatedEntry: ResourceEntry,
        sourceEntry: ResourceEntry,
    ): List<String> = buildList {
        val translatedItems = translatedEntry.androidPlaceholdersByArrayItem
        val sourceItems = sourceEntry.androidPlaceholdersByArrayItem
        if (translatedItems.size != sourceItems.size) {
            add(
                "$path: translated $key has ${translatedItems.size} items instead of " +
                    "${sourceItems.size} from $sourcePath."
            )
        }
        translatedItems.zip(sourceItems).forEachIndexed {
            index,
            (translatedPlaceholders, sourcePlaceholders) ->
            if (translatedPlaceholders != sourcePlaceholders) {
                add(
                    "$path: translated $key item ${index + 1} uses Android placeholders " +
                        "${translatedPlaceholders.render()} instead of " +
                        "${sourcePlaceholders.render()} from $sourcePath."
                )
            }
        }
    }

    private fun Set<String>.render(): String = sorted().joinToString(prefix = "[", postfix = "]")
}

internal fun isAndroidResourceFile(path: String): Boolean =
    !isValidationFixture(path) && ANDROID_RESOURCE_FILE_PATTERN.containsMatchIn(path)

internal fun isAndroidSourceResourceFile(path: String): Boolean =
    !isValidationFixture(path) && ANDROID_SOURCE_RESOURCE_FILE_PATTERN.containsMatchIn(path)

internal fun androidSourceResourcePath(path: String): String? {
    val match = ANDROID_TRANSLATED_VALUES_PATTERN.find(path) ?: return null
    return path.replaceRange(match.range, "/res/values/")
}

private val ANDROID_RESOURCE_FILE_PATTERN =
    Regex("""(?:^|/)src/main/res/values(?:-[^/]+)?/[^/]+\.xml$""")
private val ANDROID_SOURCE_RESOURCE_FILE_PATTERN =
    Regex("""(?:^|/)src/main/res/values/[^/]+\.xml$""")
private val ANDROID_TRANSLATED_VALUES_PATTERN =
    Regex("""/res/values-(?:b\+[A-Za-z0-9+]+|[a-z]{2,3}(?:-r[A-Z]{2})?)(?:-[^/]+)*/""")
