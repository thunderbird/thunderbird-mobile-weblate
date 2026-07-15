package net.thunderbird.cli.importer

import java.io.File
import net.thunderbird.cli.importer.command.Command
import net.thunderbird.cli.importer.command.CommandParser
import net.thunderbird.cli.importer.git.GitClient
import net.thunderbird.cli.importer.workflow.ImportWorkflow
import net.thunderbird.cli.importer.workflow.ValidateWorkflow
import net.thunderbird.cli.l10n.config.L10nToolsConfig
import net.thunderbird.cli.l10n.config.L10nToolsConfigLoader
import net.thunderbird.cli.l10n.config.L10nToolsConfigValidator

object ImportCliActionFactory {
    fun create(args: Array<String>): ImportCliAction {
        val command = CommandParser.parse(args)
        if (command is Command.Help) {
            return ImportCliAction.Help(command)
        }

        val projectRoot = ProjectRootResolver.resolve(File(System.getProperty("user.dir")))
        val loadedConfig =
            L10nToolsConfigLoader().load(projectRoot, configPath = null).also {
                L10nToolsConfigValidator.validate(it)
            }

        return when (command) {
            Command.Import ->
                ImportCliAction.Import(
                    fullImport = CommandParser.isFullImport(args),
                    workflow = createImportWorkflow(projectRoot, loadedConfig),
                )
            Command.Validate ->
                ImportCliAction.Validate(
                    workflow = createValidateWorkflow(projectRoot, loadedConfig)
                )
            is Command.Help -> ImportCliAction.Help(command)
        }
    }

    private fun createImportWorkflow(
        projectRoot: File,
        toolsConfig: L10nToolsConfig,
    ): ImportWorkflow {
        val context = createContext(projectRoot, toolsConfig)
        return ImportWorkflow(context, GitClient(context))
    }

    private fun createValidateWorkflow(
        projectRoot: File,
        toolsConfig: L10nToolsConfig,
    ): ValidateWorkflow {
        val context = createContext(projectRoot, toolsConfig)
        return ValidateWorkflow(context, GitClient(context))
    }

    private fun createContext(projectRoot: File, toolsConfig: L10nToolsConfig): ImportContext =
        ImportContext(projectRoot = projectRoot, toolsConfig = toolsConfig)
}
