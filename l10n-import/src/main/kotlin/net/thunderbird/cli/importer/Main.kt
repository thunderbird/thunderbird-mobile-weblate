package net.thunderbird.cli.importer

import com.jakewharton.mosaic.NonInteractivePolicy
import com.jakewharton.mosaic.runMosaicBlocking
import net.thunderbird.cli.importer.command.Command
import net.thunderbird.cli.importer.command.CommandDefaults
import net.thunderbird.cli.importer.command.CommandParser
import net.thunderbird.cli.importer.ui.ImportApp
import net.thunderbird.cli.l10n.config.L10nToolsConfigLoader
import java.io.File

fun main(args: Array<String>) {
    val parsedCommand = CommandParser.parse(args)
    val projectRoot = ProjectRootResolver.resolve(File(System.getProperty("user.dir")))
    val toolsConfig = L10nToolsConfigLoader().load(projectRoot, parsedCommand.configFile())
    val command = CommandDefaults.apply(parsedCommand, toolsConfig)
    val runtime = CliRuntimeFactory.create(command, projectRoot, toolsConfig)

    runMosaicBlocking(onNonInteractive = NonInteractivePolicy.Ignore) {
        ImportApp(
            command = command,
            importWorkflow = runtime?.importWorkflow,
            validateWorkflow = runtime?.validateWorkflow,
        )
    }
}

private fun Command.configFile(): String? {
    return when (this) {
        is Command.Import -> configFile
        is Command.Validate -> configFile
        is Command.Help -> null
    }
}
