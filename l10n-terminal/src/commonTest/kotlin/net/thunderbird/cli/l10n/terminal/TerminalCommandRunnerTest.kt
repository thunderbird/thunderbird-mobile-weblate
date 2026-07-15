package net.thunderbird.cli.l10n.terminal

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals

val TerminalCommandRunnerTests by
    testSuite("TerminalCommandRunner") {
        test("sets successful exit state after command completes") {
            val store = TerminalStateStore(TerminalState(title = "Initial"))
            val runner = TerminalCommandRunner(store)

            runner.run(TerminalState(title = "Command")) { store.status("Done") }

            assertEquals(0, runner.exitState.value)
            assertEquals(
                TerminalState(title = "Command", lines = listOf(TerminalLine.Text("Done"))),
                store.state.value,
            )
        }

        test("records error and failed exit state for illegal state failures") {
            val store = TerminalStateStore(TerminalState(title = "Initial"))
            val runner = TerminalCommandRunner(store)

            runner.run(TerminalState(title = "Command")) { error("Missing config") }

            assertEquals(1, runner.exitState.value)
            assertEquals(
                TerminalState(
                    title = "Command",
                    lines = listOf(TerminalLine.Error("Error: Missing config")),
                ),
                store.state.value,
            )
        }
    }
