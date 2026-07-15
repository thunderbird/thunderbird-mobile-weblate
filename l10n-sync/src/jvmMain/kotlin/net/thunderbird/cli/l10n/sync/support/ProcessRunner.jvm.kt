package net.thunderbird.cli.l10n.sync.support

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class ProcessRunner actual constructor() {
    actual suspend fun run(command: List<String>): ProcessResult =
        withContext(Dispatchers.IO) {
            val process = ProcessBuilder(command).redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            ProcessResult(exitCode = process.waitFor(), output = output)
        }
}
