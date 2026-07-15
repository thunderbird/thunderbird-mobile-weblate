package net.thunderbird.cli.l10n.terminal

import kotlin.system.exitProcess

internal actual fun exitTerminalProcess(status: Int) {
    exitProcess(status)
}
