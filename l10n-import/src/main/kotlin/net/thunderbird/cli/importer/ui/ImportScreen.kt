package net.thunderbird.cli.importer.ui

import androidx.compose.runtime.Composable
import net.thunderbird.cli.importer.command.Command
import net.thunderbird.cli.importer.workflow.ImportWorkflow

@Composable
fun ImportScreen(command: Command.Import, importWorkflow: ImportWorkflow) {
    WorkflowScreen { reporter ->
        importWorkflow.run(command, reporter)
    }
}
