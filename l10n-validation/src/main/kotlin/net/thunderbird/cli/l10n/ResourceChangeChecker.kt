package net.thunderbird.cli.l10n

internal class ResourceChangeChecker(
    private val gitClient: GitClient,
    private val resourceParser: ResourceParser = ResourceParser(),
    private val composeResourceChecker: ComposeResourceChecker =
        ComposeResourceChecker(gitClient, resourceParser),
    private val androidResourceChecker: AndroidResourceChecker =
        AndroidResourceChecker(gitClient, resourceParser),
) {
    fun check(baseRef: String, headRef: String): CompatibilityResult {
        val changedFiles = gitClient.changedFiles(baseRef, headRef)
        val changedSourceFiles = changedFiles.filter { path ->
            isComposeSourceResourceFile(path) || isAndroidSourceResourceFile(path)
        }
        val relatedTranslationFiles =
            if (changedSourceFiles.isEmpty()) {
                emptyList()
            } else {
                gitClient.files(headRef).filter { path ->
                    translationSourcePath(path) in changedSourceFiles
                }
            }
        val files =
            (changedFiles.filter { path ->
                    isComposeResourceFile(path) || isAndroidResourceFile(path)
                } + relatedTranslationFiles)
                .distinct()
                .sorted()
        val failures = buildList {
            files.forEach { path ->
                addAll(
                    catchInvalidResource {
                        if (isComposeResourceFile(path)) {
                            composeResourceChecker.check(path, headRef)
                        } else {
                            androidResourceChecker.check(path, headRef)
                        }
                    }
                )
            }
        }
        return CompatibilityResult(filesChecked = files.size, failures = failures.distinct())
    }

    private fun translationSourcePath(path: String): String? =
        composeSourceResourcePath(path) ?: androidSourceResourcePath(path)
}

internal fun isComposeResourceFile(path: String): Boolean =
    !isValidationFixture(path) && COMPOSE_RESOURCE_FILE_PATTERN.containsMatchIn(path)

internal fun isComposeSourceResourceFile(path: String): Boolean =
    !isValidationFixture(path) && COMPOSE_RESOURCE_SOURCE_FILE_PATTERN.containsMatchIn(path)

internal fun isValidationFixture(path: String): Boolean =
    path.contains("/src/test/resources/fixtures/")

private val COMPOSE_RESOURCE_FILE_PATTERN =
    Regex("""/composeResources/values(?:-[^/]+)?/[^/]+\.xml$""")
private val COMPOSE_RESOURCE_SOURCE_FILE_PATTERN = Regex("""/composeResources/values/[^/]+\.xml$""")
