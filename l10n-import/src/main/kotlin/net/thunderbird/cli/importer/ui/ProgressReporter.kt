package net.thunderbird.cli.importer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.yield
import net.thunderbird.cli.importer.workflow.WorkflowObserver

class ProgressReporter : WorkflowObserver {
    var currentStatus by mutableStateOf("")
        private set
    val completedLines = mutableStateListOf<String>()

    override suspend fun status(message: String) {
        if (currentStatus.isNotEmpty()) {
            completedLines += currentStatus
        }
        currentStatus = message
        yield()
    }

    override suspend fun log(message: String) {
        completedLines += message
        yield()
    }

    override suspend fun error(message: String) {
        completedLines += message
        yield()
    }
}
