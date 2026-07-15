package net.thunderbird.cli.weblate.command

fun confirm(message: String): Boolean {
    print("$message [y/N] ")
    return readlnOrNull()?.trim()?.lowercase() in setOf("y", "yes")
}
