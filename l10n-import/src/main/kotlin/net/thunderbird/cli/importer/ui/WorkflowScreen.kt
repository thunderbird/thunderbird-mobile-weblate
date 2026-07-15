package net.thunderbird.cli.importer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.jakewharton.mosaic.StaticEffect
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Text
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("ImportCliWorkflowScreen")

@Composable
fun WorkflowScreen(
    run: suspend (ProgressReporter) -> Unit,
) {
    val reporter = remember { ProgressReporter() }

    Column {
        reporter.completedLines.forEach { line ->
            StaticEffect { Text(line) }
        }
        if (reporter.currentStatus.isNotEmpty()) {
            Text(reporter.currentStatus)
        }
    }

    LaunchedEffect(Unit) {
        try {
            run(reporter)
        } catch (e: Exception) {
            logger.error("Workflow failed", e)
            reporter.error("Error: ${e.message ?: e.toString()}")
            exitProcess(1)
        }
    }
}
