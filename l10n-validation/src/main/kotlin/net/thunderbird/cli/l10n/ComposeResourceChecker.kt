package net.thunderbird.cli.l10n

internal class ComposeResourceChecker(
    private val gitClient: GitClient,
    private val resourceParser: ResourceParser,
) {
    fun check(path: String, ref: String): List<String> {
        val content = gitClient.readFile(ref, path)
        val formatFailures = resourceParser.validateCompose(content, path, ref)
        if (!COMPOSE_TRANSLATED_RESOURCE_FILE_PATTERN.containsMatchIn(path)) return formatFailures

        val sourcePath =
            path.replace(COMPOSE_TRANSLATED_VALUES_PATTERN, "/composeResources/values/")
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

                translatedEntry.invalidComposePlaceholders.isNotEmpty() -> Unit

                key.startsWith("plurals:") ->
                    addAll(
                        checkPluralPlaceholders(path, sourcePath, key, translatedEntry, sourceEntry)
                    )

                key.startsWith("string-array:") ->
                    addAll(
                        checkArrayPlaceholders(path, sourcePath, key, translatedEntry, sourceEntry)
                    )

                translatedEntry.placeholders != sourceEntry.placeholders ->
                    add(
                        "$path: translated $key uses placeholders ${translatedEntry.placeholders.render()} " +
                            "instead of ${sourceEntry.placeholders.render()} from $sourcePath."
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
        translatedEntry.placeholdersByQuantity.toSortedMap().forEach {
            (quantity, translatedPlaceholders) ->
            val sourcePlaceholders =
                sourceEntry.placeholdersByQuantity[quantity]
                    ?: sourceEntry.placeholdersByQuantity["other"]
                    ?: sourceEntry.placeholders
            if (translatedPlaceholders != sourcePlaceholders) {
                add(
                    "$path: translated $key quantity $quantity uses placeholders ${translatedPlaceholders.render()} " +
                        "instead of ${sourcePlaceholders.render()} from $sourcePath."
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
        val translatedItems = translatedEntry.placeholdersByArrayItem
        val sourceItems = sourceEntry.placeholdersByArrayItem
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
                    "$path: translated $key item ${index + 1} uses placeholders ${translatedPlaceholders.render()} " +
                        "instead of ${sourcePlaceholders.render()} from $sourcePath."
                )
            }
        }
    }

    private fun Set<String>.render(): String = sorted().joinToString(prefix = "[", postfix = "]")

    private companion object {
        val COMPOSE_TRANSLATED_RESOURCE_FILE_PATTERN =
            Regex("""/composeResources/values-[^/]+/[^/]+\.xml$""")
        val COMPOSE_TRANSLATED_VALUES_PATTERN = Regex("""/composeResources/values-[^/]+/""")
    }
}
