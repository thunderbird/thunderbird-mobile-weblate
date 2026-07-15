package net.thunderbird.cli.importer

import net.thunderbird.cli.importer.git.GitClient
import net.thunderbird.cli.importer.workflow.ImportWorkflow
import net.thunderbird.cli.importer.workflow.ValidateWorkflow

data class CliRuntime(
    val config: Config,
    val gitClient: GitClient,
    val importWorkflow: ImportWorkflow,
    val validateWorkflow: ValidateWorkflow,
)
