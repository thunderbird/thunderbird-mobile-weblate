package net.thunderbird.cli.importer.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.StaticEffect
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Text

@Composable
fun TerminalScreen(model: TerminalModel) {
    Column {
        model.completedLines.forEach { line -> StaticEffect { Text(line) } }
        if (model.currentStatus.isNotEmpty()) {
            Text(model.currentStatus)
        }
    }
}
