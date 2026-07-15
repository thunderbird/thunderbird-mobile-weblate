package net.thunderbird.cli.l10n.terminal

data class TerminalState(
    val title: String,
    val subtitle: String? = null,
    val lines: List<TerminalLine> = emptyList(),
    val currentStatus: TerminalStatus? = null,
    val prompt: TerminalPrompt? = null,
)

sealed interface TerminalLine {
    data class Text(val value: String, val indent: Int = 0) : TerminalLine

    data class Success(val value: String) : TerminalLine

    data class Warning(val value: String, val indent: Int = 0) : TerminalLine

    data class WarningDetail(val value: String, val indent: Int = 0) : TerminalLine

    data class Error(val value: String) : TerminalLine

    data class Diff(val title: String, val entries: List<DiffEntry>) : TerminalLine
}

sealed interface TerminalStatus {
    val value: String

    data class Message(override val value: String) : TerminalStatus

    data class Success(override val value: String) : TerminalStatus
}

enum class TerminalStatusStyle {
    DEFAULT,
    SUCCESS,
}

data class DiffEntry(val label: String, val value: String)

data class TerminalPrompt(val message: String, val default: Boolean = false) {
    val suffix: String
        get() =
            if (default) {
                "[Y/n]"
            } else {
                "[y/N]"
            }
}
