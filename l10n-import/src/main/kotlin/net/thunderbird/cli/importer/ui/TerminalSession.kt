package net.thunderbird.cli.importer.ui

import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

class TerminalSession(scope: CoroutineScope) {
    private val events = MutableSharedFlow<WorkflowEvent>(extraBufferCapacity = 64)

    val observer = WorkflowEventObserver(events)

    val models: StateFlow<TerminalModel> =
        scope.launchMolecule(mode = RecompositionMode.Immediate) { TerminalPresenter(events) }

    suspend fun complete() {
        events.emit(WorkflowEvent.Complete)
    }

    suspend fun error(message: String) {
        events.emit(WorkflowEvent.Error(message))
    }
}
