package net.thunderbird.cli.l10n.terminal

import com.jakewharton.mosaic.layout.KeyEvent
import kotlinx.coroutines.flow.StateFlow

abstract class TerminalModel(initialState: TerminalState) {
    protected val store = TerminalStateStore(initialState)
    private val commandRunner = TerminalCommandRunner(store)

    val state: StateFlow<TerminalState> = store.state
    val exitState: StateFlow<Int?> = commandRunner.exitState

    protected suspend fun runCommand(state: TerminalState, command: suspend () -> Unit) {
        commandRunner.run(state = state, command = command)
    }

    fun handleKeyEvent(event: KeyEvent): Boolean = store.handleKeyEvent(event)
}
