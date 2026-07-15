package net.thunderbird.cli.importer.workflow

interface WorkflowObserver {
    suspend fun status(message: String)

    suspend fun log(message: String)

    suspend fun error(message: String)
}
