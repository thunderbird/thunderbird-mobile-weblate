package net.thunderbird.cli.l10n

import java.io.File

internal class L10nValidationCli {
    fun run(arguments: List<String>): CliResult =
        if (arguments.isEmpty() || arguments.first() in HELP_OPTIONS) {
            CliResult(0, HELP)
        } else {
            runCommand(arguments.first(), arguments.drop(1))
        }

    private fun runCommand(command: String, arguments: List<String>): CliResult {
        val options =
            parseOptions(arguments) ?: return CliResult(2, "Invalid command options.\n\n$HELP")
        return runWithOptions(command, options)
    }

    private fun runWithOptions(command: String, options: ParsedOptions): CliResult {
        val repositoryRoot = File(options.single("--repository-root") ?: ".")
        val baseRef = options.single("--base-ref") ?: return missingOption("--base-ref")
        val headRef = options.single("--head-ref") ?: "HEAD"
        val gitClient = ProcessGitClient(repositoryRoot)

        return when (command) {
            "validate-resource-changes" ->
                validateResourceChanges(gitClient, baseRef, headRef, options)
            "check-branch-compatibility" ->
                checkBranchCompatibility(gitClient, baseRef, headRef, options)
            else -> CliResult(2, "Unknown command: $command\n\n$HELP")
        }
    }

    private fun validateResourceChanges(
        gitClient: GitClient,
        baseRef: String,
        headRef: String,
        options: ParsedOptions,
    ): CliResult {
        val unexpected = options.names - COMMON_OPTIONS
        if (unexpected.isNotEmpty()) return unknownOptions(unexpected)

        val result = ComposeResourceChangeChecker(gitClient).check(baseRef, headRef)
        return result.toCliResult(
            "Validated ${result.filesChecked} changed Compose resource files."
        )
    }

    private fun checkBranchCompatibility(
        gitClient: GitClient,
        baseRef: String,
        headRef: String,
        options: ParsedOptions,
    ): CliResult {
        val unexpected = options.names - BRANCH_OPTIONS
        val upstreamRef = options.single("--upstream-ref")
        val downstreamRefs = options.multiple("--downstream-ref")
        return when {
            unexpected.isNotEmpty() -> unknownOptions(unexpected)
            upstreamRef == null && downstreamRefs.isEmpty() ->
                CliResult(2, "One of --upstream-ref or --downstream-ref is required.")
            else -> {
                val result =
                    BranchCompatibilityChecker(gitClient)
                        .check(
                            CompatibilityOptions(
                                baseRef = baseRef,
                                headRef = headRef,
                                upstreamRef = upstreamRef,
                                downstreamRefs = downstreamRefs,
                                allowTypoFix = options.hasFlag("--allow-typo-fix"),
                            )
                        )
                result.toCliResult("Checked ${result.filesChecked} localization source files.")
            }
        }
    }

    private fun CompatibilityResult.toCliResult(successMessage: String): CliResult =
        if (failures.isEmpty()) CliResult(0, successMessage) else CliResult(1, renderFailure())

    private fun missingOption(name: String): CliResult =
        CliResult(2, "Missing required option: $name")

    private fun unknownOptions(names: Set<String>): CliResult =
        CliResult(2, "Unknown option: ${names.sorted().joinToString()}")

    private fun parseOptions(arguments: List<String>): ParsedOptions? {
        val values = mutableMapOf<String, MutableList<String>>()
        val flags = mutableSetOf<String>()
        var index = 0
        while (index < arguments.size) {
            val option = arguments[index]
            if (option == "--allow-typo-fix") {
                flags.add(option)
                index++
            } else {
                val value =
                    arguments.getOrNull(index + 1)?.takeUnless { it.startsWith("--") }
                        ?: return null
                values.getOrPut(option, ::mutableListOf).add(value)
                index += 2
            }
        }
        return ParsedOptions(values, flags)
    }

    private companion object {
        val HELP_OPTIONS = setOf("help", "--help", "-h")
        val COMMON_OPTIONS = setOf("--repository-root", "--base-ref", "--head-ref")
        val BRANCH_OPTIONS =
            COMMON_OPTIONS + setOf("--upstream-ref", "--downstream-ref", "--allow-typo-fix")
        val HELP =
            """
            Thunderbird Mobile localization validation

            Commands:
              validate-resource-changes    Validate changed Compose source and translation resources.
              check-branch-compatibility   Validate localization compatibility across release branches.

            Common options:
              --repository-root <path>     Git repository to validate. Defaults to the current directory.
              --base-ref <ref>             Base Git revision used to identify changed files.
              --head-ref <ref>             Head Git revision. Defaults to HEAD.
            """
                .trimIndent()
    }
}

internal data class CliResult(val exitCode: Int, val message: String)

private data class ParsedOptions(
    private val values: Map<String, List<String>>,
    private val flags: Set<String>,
) {
    val names: Set<String> = values.keys + flags

    fun single(name: String): String? = values[name]?.singleOrNull()

    fun multiple(name: String): List<String> = values[name].orEmpty()

    fun hasFlag(name: String): Boolean = name in flags
}
