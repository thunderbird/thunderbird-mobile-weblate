package net.thunderbird.cli.importer.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Text
import net.thunderbird.cli.importer.command.Command

@Composable
fun HelpScreen(command: Command.Help) {
    Column {
        if (command.message != null) {
            Text(command.message)
            Text("")
        }
        HelpCommand(command.type)
    }
}

@Composable
private fun HelpCommand(type: Command.Help.HelpType) {
    when (type) {
        Command.Help.HelpType.IMPORT -> {
            Text("Usage: import [options]")
            Text("Options:")
            Text("  --all              Import both source-language files and translations")
            Text("  --help             Show this help message")
        }

        Command.Help.HelpType.VALIDATE -> {
            Text("Usage: validate [options]")
            Text("Options:")
            Text("  --help             Show this help message")
        }

        Command.Help.HelpType.UNKNOWN -> {
            Text("Usage: <command> [options]")
            Text("Commands:")
            Text("  import    Import localization files from source branches")
            Text("  validate  Validate translation keys across branches")
            Text("  help      Show this help message")
        }
    }
}
