package net.thunderbird.cli.l10n.weblate.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.LogLevel
import kotlinx.serialization.json.Json

internal actual fun createWeblateHttpClient(logLevel: LogLevel, json: Json): HttpClient =
    HttpClient(CIO) { installWeblateDefaults(logLevel = logLevel, json = json) }
