package net.thunderbird.cli.l10n

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import java.io.File

internal abstract class ValidationCommand(name: String, private val description: String) :
    CliktCommand(name = name) {
    override fun help(context: Context) = description

    private val repositoryRoot by
        option("--repository-root", help = "Git repository (default: current directory).")
            .default(".")
    protected val baseRef by option("--base-ref", help = "Base Git revision.").required()
    protected val headRef by
        option("--head-ref", help = "Head Git revision (default: HEAD).").default("HEAD")

    protected val gitClient: GitClient
        get() = ProcessGitClient(File(repositoryRoot))

    protected fun report(result: CompatibilityResult, successMessage: String) {
        echo(
            if (result.failures.isEmpty()) successMessage else result.renderFailure(),
            err = result.failures.isNotEmpty(),
        )
        if (result.failures.isNotEmpty()) throw ProgramResult(1)
    }
}
