package net.thunderbird.cli.l10n.terminal

import com.jakewharton.mosaic.layout.KeyEvent
import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import kotlin.test.assertFalse

val TerminalModelTests by
    testSuite("TerminalModel") {
        test("runs commands through the model") {
            val model = TestTerminalModel()

            model.runTestCommand(TerminalState(title = "Command")) { model.writeLine("Done") }

            assertEquals(0, model.exitState.value)
            assertEquals(
                TerminalState(title = "Command", lines = listOf(TerminalLine.Text("Done"))),
                model.state.value,
            )
        }

        test("forwards key events to the state store") {
            val model = TestTerminalModel()

            assertFalse(
                model.handleKeyEvent(KeyEvent("x", alt = false, ctrl = false, shift = false))
            )
        }
    }

private class TestTerminalModel : TerminalModel(TerminalState(title = "Initial")) {
    suspend fun runTestCommand(state: TerminalState, command: suspend () -> Unit) {
        runCommand(state, command)
    }

    fun writeLine(message: String) {
        store.line(message)
    }
}
