package net.thunderbird.cli.l10n.config

fun String.isInModule(modulePath: String): Boolean =
    this == modulePath || startsWith("$modulePath/")
