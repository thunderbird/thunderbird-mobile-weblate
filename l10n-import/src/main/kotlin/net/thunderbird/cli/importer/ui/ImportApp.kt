package net.thunderbird.cli.importer.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.StaticEffect
import com.jakewharton.mosaic.ui.Text
import net.thunderbird.cli.importer.ImportCliAction

@Composable
fun ImportApp(action: ImportCliAction) {
    StaticEffect { Text("Thunderbird Localization Importer") }

    when (action) {
        is ImportCliAction.Import ->
            WorkflowScreen { observer -> action.workflow.run(action.fullImport, observer) }
        is ImportCliAction.Validate -> WorkflowScreen { observer -> action.workflow.run(observer) }
        is ImportCliAction.Help -> HelpScreen(action.command)
    }
}
