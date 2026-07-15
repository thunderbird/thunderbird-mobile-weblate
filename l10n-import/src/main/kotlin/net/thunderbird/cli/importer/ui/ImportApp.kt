package net.thunderbird.cli.importer.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.StaticEffect
import com.jakewharton.mosaic.ui.Text
import net.thunderbird.cli.importer.command.Command
import net.thunderbird.cli.importer.workflow.ImportWorkflow
import net.thunderbird.cli.importer.workflow.ValidateWorkflow

@Composable
fun ImportApp(
    command: Command,
    importWorkflow: ImportWorkflow? = null,
    validateWorkflow: ValidateWorkflow? = null,
) {
    StaticEffect {
        Text("Thunderbird Localization Importer")
    }

    when (command) {
        is Command.Import -> ImportScreen(command, requireNotNull(importWorkflow))
        is Command.Validate -> ValidateScreen(command, requireNotNull(validateWorkflow))
        is Command.Help -> HelpScreen(command)
    }
}
