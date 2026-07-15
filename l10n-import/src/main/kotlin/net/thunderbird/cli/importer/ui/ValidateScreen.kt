package net.thunderbird.cli.importer.ui

import androidx.compose.runtime.Composable
import net.thunderbird.cli.importer.command.Command
import net.thunderbird.cli.importer.workflow.ValidateWorkflow

@Composable
fun ValidateScreen(command: Command.Validate, validateWorkflow: ValidateWorkflow) {
    WorkflowScreen { reporter ->
        validateWorkflow.run(command, reporter)
    }
}
