package net.thunderbird.cli.importer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.system.exitProcess
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("ImportCliWorkflowScreen")

@Composable
fun WorkflowScreen(run: suspend (WorkflowEventObserver) -> Unit) {
    val scope = rememberCoroutineScope()
    val session = remember(scope) { TerminalSession(scope) }
    val model = session.models.collectAsState()

    TerminalScreen(model.value)

    LaunchedEffect(session) {
        try {
            run(session.observer)
            session.complete()
        } catch (e: Exception) {
            logger.error("Workflow failed", e)
            session.error("Error: ${e.message ?: e.toString()}")
            exitProcess(1)
        }
    }
}
