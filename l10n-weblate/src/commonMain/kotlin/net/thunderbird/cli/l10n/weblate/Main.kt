package net.thunderbird.cli.l10n.weblate

import androidx.compose.runtime.rememberCoroutineScope
import com.jakewharton.mosaic.runMosaicMain
import net.thunderbird.cli.l10n.terminal.TerminalApp
import net.thunderbird.cli.l10n.weblate.ui.WeblateTerminalModel

fun main(args: Array<String>) = runMosaicMain {
    val scope = rememberCoroutineScope()
    val model = WeblateTerminalModel(scope, args)
    TerminalApp(model)
}
