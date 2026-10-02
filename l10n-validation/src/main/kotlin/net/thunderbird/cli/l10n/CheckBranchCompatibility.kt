package net.thunderbird.cli.l10n

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option

internal class CheckBranchCompatibility :
    ValidationCommand(
        "check-branch-compatibility",
        "Validate localization compatibility across release branches.",
    ) {
    private val upstreamRef by option("--upstream-ref", help = "Closest upstream branch.")
    private val downstreamRefs by
        option("--downstream-ref", help = "Downstream branch (repeatable).").multiple()
    private val allowTypoFix by
        option("--allow-typo-fix", help = "Allow text corrections without structural changes.")
            .flag()

    override fun run() {
        if (upstreamRef == null && downstreamRefs.isEmpty()) {
            echo("One of --upstream-ref or --downstream-ref is required.", err = true)
            throw ProgramResult(2)
        }
        val result =
            BranchCompatibilityChecker(gitClient)
                .check(
                    CompatibilityOptions(
                        baseRef,
                        headRef,
                        upstreamRef,
                        downstreamRefs,
                        allowTypoFix,
                    )
                )
        report(result, "Checked ${result.filesChecked} localization source files.")
    }
}
