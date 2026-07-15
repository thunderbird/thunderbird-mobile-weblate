package net.thunderbird.cli.importer.ui

sealed interface WorkflowEvent {
    data class Status(val message: String) : WorkflowEvent

    data class Log(val message: String) : WorkflowEvent

    data class Error(val message: String) : WorkflowEvent

    data object Complete : WorkflowEvent
}
