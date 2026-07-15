package net.thunderbird.cli.l10n.weblate.command

sealed interface Command {
    data class Update(val options: WeblateCommandOptions) : Command

    data class Create(val options: WeblateCommandOptions) : Command

    data class List(val options: WeblateCommandOptions) : Command

    data class Delete(val slug: String, val options: WeblateCommandOptions) : Command

    data class Help(val type: HelpType, val message: String? = null) : Command {
        enum class HelpType {
            UPDATE,
            CREATE,
            LIST,
            DELETE,
            UNKNOWN,
        }
    }
}
