package net.thunderbird.cli.importer

import java.io.File
import net.thunderbird.cli.importer.command.Command
import net.thunderbird.cli.importer.git.GitClient
import net.thunderbird.cli.importer.git.GitRepository
import net.thunderbird.cli.importer.workflow.ImportWorkflow
import net.thunderbird.cli.importer.workflow.ValidateWorkflow
import net.thunderbird.cli.l10n.config.L10nToolsConfig

object CliRuntimeFactory {
    fun create(command: Command, projectRoot: File, toolsConfig: L10nToolsConfig): CliRuntime? {
        val sourceRepository = when (command) {
            is Command.Import -> requireNotNull(command.sourceRepository)
            is Command.Validate -> requireNotNull(command.sourceRepository)
            is Command.Help -> return null
        }

        val config = Config(
            tmpDir = File(projectRoot, toolsConfig.source.tmpDir),
            targetDir = projectRoot,
            repository = parseRepository(sourceRepository),
            import = toolsConfig.import,
        )
        val gitClient = GitClient(config)

        return CliRuntime(
            config = config,
            gitClient = gitClient,
            importWorkflow = ImportWorkflow(config, gitClient),
            validateWorkflow = ValidateWorkflow(config, gitClient),
        )
    }

    private fun parseRepository(sourceRepository: String): GitRepository {
        if (sourceRepository.startsWith("https://") || sourceRepository.startsWith("git@")) {
            val name = sourceRepository
                .substringAfterLast("/")
                .removeSuffix(".git")
                .ifBlank { "source" }
            return GitRepository(organization = null, name = name, url = sourceRepository)
        }

        val parts = sourceRepository.split("/", limit = 2)
        require(parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
            "Repository must be in owner/repo format or be a Git URL: $sourceRepository"
        }
        return GitRepository(organization = parts[0], name = parts[1])
    }
}
