package net.thunderbird.cli.l10n.terminal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos

@Composable
fun TerminalApp(model: TerminalModel) {
    val state by model.state.collectAsState()
    val exitState by model.exitState.collectAsState()

    TerminalScreen(state, onKeyEvent = model::handleKeyEvent)

    LaunchedEffect(exitState) {
        exitState?.let {
            withFrameNanos {}
            exitTerminalProcess(it)
        }
    }
}

internal expect fun exitTerminalProcess(status: Int)
