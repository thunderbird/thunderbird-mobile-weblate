package net.thunderbird.cli.importer

import com.jakewharton.mosaic.NonInteractivePolicy
import com.jakewharton.mosaic.runMosaicBlocking
import kotlin.system.exitProcess
import net.thunderbird.cli.importer.ui.ImportApp

fun main(args: Array<String>) {
    try {
        val action = ImportCliActionFactory.create(args)

        runMosaicBlocking(onNonInteractive = NonInteractivePolicy.Ignore) { ImportApp(action) }
    } catch (e: IllegalStateException) {
        System.err.println(e.message ?: "Import failed")
        exitProcess(1)
    }
}
