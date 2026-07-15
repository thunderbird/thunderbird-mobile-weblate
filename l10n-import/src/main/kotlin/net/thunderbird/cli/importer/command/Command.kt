package net.thunderbird.cli.importer.command

sealed interface Command {
    data object Import : Command

    data object Validate : Command

    data class Help(val type: HelpType, val message: String? = null) : Command {
        enum class HelpType {
            IMPORT,
            VALIDATE,
            UNKNOWN,
        }
    }
}
