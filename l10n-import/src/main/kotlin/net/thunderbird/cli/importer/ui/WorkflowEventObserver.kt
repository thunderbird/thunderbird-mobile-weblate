package net.thunderbird.cli.importer.ui

import kotlinx.coroutines.flow.MutableSharedFlow
import net.thunderbird.cli.importer.workflow.WorkflowObserver

class WorkflowEventObserver(private val events: MutableSharedFlow<WorkflowEvent>) :
    WorkflowObserver {
    override suspend fun status(message: String) {
        events.emit(WorkflowEvent.Status(message))
    }

    override suspend fun log(message: String) {
        events.emit(WorkflowEvent.Log(message))
    }

    override suspend fun error(message: String) {
        events.emit(WorkflowEvent.Error(message))
    }
}
