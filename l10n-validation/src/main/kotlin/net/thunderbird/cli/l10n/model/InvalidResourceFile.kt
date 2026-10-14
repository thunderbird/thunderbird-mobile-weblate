package net.thunderbird.cli.l10n.model

internal class InvalidResourceFile(message: String, cause: Throwable) :
    IllegalArgumentException(message, cause)
