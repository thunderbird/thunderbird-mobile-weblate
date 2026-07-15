package net.thunderbird.cli.l10n.terminal

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.StaticEffect
import com.jakewharton.mosaic.layout.KeyEvent
import com.jakewharton.mosaic.layout.onKeyEvent
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Text
import net.thunderbird.cli.l10n.terminal.component.Title

@Composable
fun TerminalScreen(state: TerminalState, onKeyEvent: (KeyEvent) -> Boolean) {
    Column(modifier = Modifier.onKeyEvent(onKeyEvent)) {
        if (state.title.isNotEmpty()) {
            StaticEffect { Title(text = state.title) }
        }
        state.subtitle?.let { subtitle -> StaticEffect { Title(subtitle) } }
        state.lines.forEach { line -> TerminalLine(line) }
        state.currentStatus?.let { status -> TerminalStatus(status) }
        state.prompt?.let { prompt -> Text("${prompt.message} ${prompt.suffix}") }
    }
}

@Composable
private fun TerminalLine(line: TerminalLine) {
    when (line) {
        is TerminalLine.Text -> StaticEffect { Text("${line.indent.spaces()}${line.value}") }
        is TerminalLine.Success -> StaticEffect { Text("✓ ${line.value}", color = Color.Green) }
        is TerminalLine.Warning -> StaticEffect { Text("${line.indent.spaces()}⚠️  ${line.value}") }
        is TerminalLine.WarningDetail ->
            StaticEffect { Text("${line.indent.spaces()}${WARNING_DETAIL_INDENT}${line.value}") }
        is TerminalLine.Error -> StaticEffect { Text("❌ ${line.value}") }
        is TerminalLine.Diff -> {
            StaticEffect { Text("   ❌ ${line.title}") }
            line.entries.forEach { entry ->
                StaticEffect { Text("      ${entry.label}: ${entry.value}") }
            }
        }
    }
}

@Composable
private fun TerminalStatus(status: TerminalStatus) {
    when (status) {
        is TerminalStatus.Message -> Text(status.value)
        is TerminalStatus.Success -> Text("✓ ${status.value}", color = Color.Green)
    }
}

private fun Int.spaces(): String = "   ".repeat(coerceAtLeast(0))

private const val WARNING_DETAIL_INDENT = "    "
