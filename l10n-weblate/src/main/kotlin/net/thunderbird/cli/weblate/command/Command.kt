package net.thunderbird.cli.weblate.command

sealed interface Command {
    data object Update : Command

    data object Create : Command

    data class Delete(val slug: String) : Command

    data class Help(val message: String? = null) : Command
}
