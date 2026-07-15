package net.thunderbird.cli.l10n.sync.support

expect class ProcessRunner() {
    suspend fun run(command: List<String>): ProcessResult
}

data class ProcessResult(val exitCode: Int, val output: String)
