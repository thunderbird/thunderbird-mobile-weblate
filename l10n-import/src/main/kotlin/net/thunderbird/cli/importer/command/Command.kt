package net.thunderbird.cli.importer.command

import net.thunderbird.cli.importer.git.Branch

sealed interface Command {
    data class Import(
        val all: Boolean = false,
        val sourceRepository: String? = null,
        val branches: List<Branch>? = null,
        val configFile: String? = null,
    ) : Command

    data class Validate(
        val sourceRepository: String? = null,
        val outputDir: String = ".",
        val branches: List<Branch>? = null,
        val configFile: String? = null,
    ) : Command

    data class Help(
        val type: HelpType,
        val message: String? = null,
    ) : Command {
        enum class HelpType {
            IMPORT,
            VALIDATE,
            UNKNOWN,
        }
    }
}
