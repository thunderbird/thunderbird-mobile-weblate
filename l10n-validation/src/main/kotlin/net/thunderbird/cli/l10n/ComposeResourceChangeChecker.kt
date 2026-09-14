package net.thunderbird.cli.l10n

internal class ComposeResourceChangeChecker(
    private val gitClient: GitClient,
    private val resourceParser: ResourceParser = ResourceParser(),
    private val composeResourceChecker: ComposeResourceChecker =
        ComposeResourceChecker(gitClient, resourceParser),
) {
    fun check(baseRef: String, headRef: String): CompatibilityResult {
        val files = gitClient.changedFiles(baseRef, headRef).filter(::isComposeResourceFile)
        val failures = buildList {
            files.forEach { path ->
                addAll(catchInvalidResource { composeResourceChecker.check(path, headRef) })
            }
        }
        return CompatibilityResult(filesChecked = files.size, failures = failures.distinct())
    }
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
