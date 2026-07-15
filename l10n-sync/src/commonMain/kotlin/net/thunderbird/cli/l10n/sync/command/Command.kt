package net.thunderbird.cli.l10n.sync.command

sealed interface Command {
    data class Import(val fullImport: Boolean = false, val applyChanges: Boolean = false) : Command

    data class Export(val branch: String, val l10nRepo: String, val applyChanges: Boolean = false) :
        Command

    data class Help(val type: HelpType, val message: String? = null) : Command {
        enum class HelpType {
            IMPORT,
            EXPORT,
            UNKNOWN,
        }
    }
}
