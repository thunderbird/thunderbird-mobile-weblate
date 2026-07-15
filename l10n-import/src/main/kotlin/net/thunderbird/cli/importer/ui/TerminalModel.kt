package net.thunderbird.cli.importer.ui

data class TerminalModel(
    val completedLines: List<String> = emptyList(),
    val currentStatus: String = "",
    val isComplete: Boolean = false,
)
