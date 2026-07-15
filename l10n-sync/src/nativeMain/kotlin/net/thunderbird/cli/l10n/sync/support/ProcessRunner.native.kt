package net.thunderbird.cli.l10n.sync.support

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.fgets
import platform.posix.pclose
import platform.posix.popen

actual class ProcessRunner actual constructor() {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun run(command: List<String>): ProcessResult {
        val shellCommand = command.joinToString(" ") { it.shellQuote() } + " 2>&1"
        val output = StringBuilder()
        val status = memScoped {
            val pipe =
                popen(shellCommand, "r")
                    ?: return ProcessResult(127, "Unable to run: $shellCommand")
            val buffer = allocArray<ByteVar>(BUFFER_SIZE)
            try {
                while (fgets(buffer, BUFFER_SIZE, pipe) != null) {
                    output.append(buffer.toKString())
                }
            } finally {
                return@memScoped pclose(pipe)
            }
        }

        return ProcessResult(exitCode = if (status == 0) 0 else 1, output = output.toString())
    }

    private fun String.shellQuote(): String = "'${replace("'", "'\"'\"'")}'"

    private companion object {
        const val BUFFER_SIZE = 4096
    }
}
