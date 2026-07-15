package net.thunderbird.cli.l10n.terminal

import platform.posix.exit

internal actual fun exitTerminalProcess(status: Int) {
    exit(status)
}
