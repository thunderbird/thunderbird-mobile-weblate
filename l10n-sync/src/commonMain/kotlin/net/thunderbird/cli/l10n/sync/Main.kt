package net.thunderbird.cli.l10n.sync

import androidx.compose.runtime.rememberCoroutineScope
import com.jakewharton.mosaic.runMosaicMain
import net.thunderbird.cli.l10n.sync.ui.SyncTerminalModel
import net.thunderbird.cli.l10n.terminal.TerminalApp

fun main(args: Array<String>) = runMosaicMain {
    val scope = rememberCoroutineScope()
    val model = SyncTerminalModel(scope, args)
    TerminalApp(model)
}
