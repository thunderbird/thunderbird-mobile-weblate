package net.thunderbird.cli.l10n.terminal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TerminalCommandRunner(private val store: TerminalStateStore) {
    val exitState: StateFlow<Int?>
        field = MutableStateFlow<Int?>(null)

    suspend fun run(state: TerminalState, command: suspend () -> Unit) {
        store.reset(state)
        try {
            command()
            store.complete()
            exitState.value = 0
        } catch (e: IllegalStateException) {
            store.error("Error: ${e.message ?: e}")
            store.complete()
            exitState.value = 1
        }
    }
}
