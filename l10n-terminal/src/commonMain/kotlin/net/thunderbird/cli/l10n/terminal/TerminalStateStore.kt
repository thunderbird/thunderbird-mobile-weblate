package net.thunderbird.cli.l10n.terminal

import com.jakewharton.mosaic.layout.KeyEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface Terminal {
    fun status(message: String, style: TerminalStatusStyle = TerminalStatusStyle.DEFAULT)

    fun line(message: String, indent: Int = 0)

    fun warning(message: String, indent: Int = 0)

    fun warningDetail(message: String, indent: Int = 0)

    fun error(message: String)

    fun diff(title: String, entries: List<DiffEntry>)

    fun complete()

    suspend fun confirm(message: String, default: Boolean = false): Boolean
}

class TerminalStateStore(initialState: TerminalState) : Terminal {
    private var promptResponse: CompletableDeferred<Boolean>? = null

    val state: StateFlow<TerminalState>
        field = MutableStateFlow(initialState)

    fun reset(state: TerminalState) {
        this.state.value = state
    }

    override fun status(message: String, style: TerminalStatusStyle) {
        state.value =
            state.value
                .withCurrentStatusLine()
                .copy(
                    currentStatus =
                        when (style) {
                            TerminalStatusStyle.DEFAULT -> TerminalStatus.Message(message)
                            TerminalStatusStyle.SUCCESS -> TerminalStatus.Success(message)
                        }
                )
    }

    override fun line(message: String, indent: Int) {
        appendLine(TerminalLine.Text(message, indent))
    }

    override fun warning(message: String, indent: Int) {
        appendLine(TerminalLine.Warning(value = message, indent = indent))
    }

    override fun warningDetail(message: String, indent: Int) {
        appendLine(TerminalLine.WarningDetail(value = message, indent = indent))
    }

    override fun error(message: String) {
        appendLine(TerminalLine.Error(message))
    }

    override fun diff(title: String, entries: List<DiffEntry>) {
        appendLine(TerminalLine.Diff(title = title, entries = entries))
    }

    override fun complete() {
        state.value = state.value.withCurrentStatusLine()
    }

    override suspend fun confirm(message: String, default: Boolean): Boolean {
        val response = CompletableDeferred<Boolean>()
        promptResponse = response
        state.value =
            state.value.withCurrentStatusLine().copy(prompt = TerminalPrompt(message, default))

        return response.await()
    }

    fun handleKeyEvent(event: KeyEvent): Boolean {
        val prompt = state.value.prompt
        val response = promptResponse
        val confirmed = prompt?.confirmationValue(event)

        return if (prompt == null || response == null || confirmed == null) {
            false
        } else {
            state.value =
                state.value.copy(
                    lines =
                        state.value.lines +
                            TerminalLine.Text(
                                "${prompt.message} ${prompt.suffix} " +
                                    if (confirmed) "yes" else "no"
                            ),
                    prompt = null,
                )
            promptResponse = null
            response.complete(confirmed)
            true
        }
    }

    private fun appendLine(line: TerminalLine) {
        val currentState = state.value.withCurrentStatusLine()
        state.value = currentState.copy(lines = currentState.lines + line)
    }
}

private fun TerminalState.withCurrentStatusLine(): TerminalState {
    val currentStatus = currentStatus ?: return this
    val line =
        when (currentStatus) {
            is TerminalStatus.Message -> TerminalLine.Text(currentStatus.value)
            is TerminalStatus.Success -> TerminalLine.Success(currentStatus.value)
        }
    return copy(lines = lines + line, currentStatus = null)
}

private fun TerminalPrompt.confirmationValue(event: KeyEvent): Boolean? =
    when (event.key.lowercase()) {
        "y" -> true
        "n" -> false
        "\n",
        "\r",
        "enter",
        "return" -> default
        else -> null
    }
