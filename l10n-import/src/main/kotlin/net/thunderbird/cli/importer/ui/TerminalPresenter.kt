package net.thunderbird.cli.importer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.Flow

@Composable
fun TerminalPresenter(events: Flow<WorkflowEvent>): TerminalModel {
    val completedLines = remember { mutableStateListOf<String>() }
    var currentStatus by remember { mutableStateOf("") }
    var isComplete by remember { mutableStateOf(false) }

    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is WorkflowEvent.Status -> {
                    if (currentStatus.isNotEmpty()) {
                        completedLines += currentStatus
                    }
                    currentStatus = event.message
                }
                is WorkflowEvent.Log -> completedLines += event.message
                is WorkflowEvent.Error -> completedLines += event.message
                WorkflowEvent.Complete -> {
                    if (currentStatus.isNotEmpty()) {
                        completedLines += currentStatus
                    }
                    currentStatus = ""
                    isComplete = true
                }
            }
        }
    }

    return TerminalModel(
        completedLines = completedLines.toList(),
        currentStatus = currentStatus,
        isComplete = isComplete,
    )
}
