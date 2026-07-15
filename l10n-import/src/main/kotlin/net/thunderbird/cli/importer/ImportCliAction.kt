package net.thunderbird.cli.importer

import net.thunderbird.cli.importer.command.Command
import net.thunderbird.cli.importer.workflow.ImportWorkflow
import net.thunderbird.cli.importer.workflow.ValidateWorkflow

sealed interface ImportCliAction {
    data class Help(val command: Command.Help) : ImportCliAction

    data class Import(val fullImport: Boolean, val workflow: ImportWorkflow) : ImportCliAction

    data class Validate(val workflow: ValidateWorkflow) : ImportCliAction
}
