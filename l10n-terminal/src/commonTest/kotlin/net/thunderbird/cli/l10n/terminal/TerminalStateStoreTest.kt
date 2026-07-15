package net.thunderbird.cli.l10n.terminal

import com.jakewharton.mosaic.layout.KeyEvent
import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield

val TerminalStateStoreTests by
    testSuite("TerminalStateStore") {
        test("moves current status to lines before adding a new line") {
            val store = TerminalStateStore(TerminalState(title = "Title"))

            store.status("Working")
            store.line("Done")

            assertEquals(
                TerminalState(
                    title = "Title",
                    lines = listOf(TerminalLine.Text("Working"), TerminalLine.Text("Done")),
                ),
                store.state.value,
            )
        }

        test("records warning and warning detail indentation") {
            val store = TerminalStateStore(TerminalState(title = "Title"))

            store.warning("Conflict", indent = 1)
            store.warningDetail("main: value", indent = 2)

            assertEquals(
                listOf(
                    TerminalLine.Warning("Conflict", indent = 1),
                    TerminalLine.WarningDetail("main: value", indent = 2),
                ),
                store.state.value.lines,
            )
        }

        test("records error and diff lines") {
            val store = TerminalStateStore(TerminalState(title = "Title"))

            store.error("Failed")
            store.diff("Config differs", listOf(DiffEntry("name", "value")))

            assertEquals(
                listOf(
                    TerminalLine.Error("Failed"),
                    TerminalLine.Diff(
                        title = "Config differs",
                        entries = listOf(DiffEntry("name", "value")),
                    ),
                ),
                store.state.value.lines,
            )
        }

        test("flushes current status when completing") {
            val store = TerminalStateStore(TerminalState(title = "Title"))

            store.status("Working")
            store.complete()

            assertEquals(
                TerminalState(title = "Title", lines = listOf(TerminalLine.Text("Working"))),
                store.state.value,
            )
        }

        test("records successful status as a success line") {
            val store = TerminalStateStore(TerminalState(title = "Title"))

            store.status("Import complete!", style = TerminalStatusStyle.SUCCESS)
            store.complete()

            assertEquals(
                TerminalState(
                    title = "Title",
                    lines = listOf(TerminalLine.Success("Import complete!")),
                ),
                store.state.value,
            )
        }

        test("records prompt answer from explicit key") {
            coroutineScope {
                val store = TerminalStateStore(TerminalState(title = "Title"))
                val response = async { store.confirm("Apply changes?", default = false) }
                yield()

                assertEquals(
                    TerminalPrompt("Apply changes?", default = false),
                    store.state.value.prompt,
                )

                assertTrue(store.handleKeyEvent(key("y")))

                assertTrue(response.await())
                assertNull(store.state.value.prompt)
                assertEquals(
                    listOf(TerminalLine.Text("Apply changes? [y/N] yes")),
                    store.state.value.lines,
                )
            }
        }

        test("uses prompt default for enter key") {
            coroutineScope {
                val store = TerminalStateStore(TerminalState(title = "Title"))
                val response = async { store.confirm("Continue?", default = true) }
                yield()

                assertTrue(store.handleKeyEvent(key("enter")))

                assertTrue(response.await())
                assertEquals(
                    listOf(TerminalLine.Text("Continue? [Y/n] yes")),
                    store.state.value.lines,
                )
            }
        }

        test("records declined prompt answer") {
            coroutineScope {
                val store = TerminalStateStore(TerminalState(title = "Title"))
                val response = async { store.confirm("Apply changes?", default = true) }
                yield()

                assertTrue(store.handleKeyEvent(key("n")))

                assertFalse(response.await())
                assertEquals(
                    listOf(TerminalLine.Text("Apply changes? [Y/n] no")),
                    store.state.value.lines,
                )
            }
        }

        test("ignores key events without active prompt") {
            val store = TerminalStateStore(TerminalState(title = "Title"))

            assertFalse(store.handleKeyEvent(key("y")))
        }
    }

private fun key(value: String): KeyEvent =
    KeyEvent(key = value, alt = false, ctrl = false, shift = false)
