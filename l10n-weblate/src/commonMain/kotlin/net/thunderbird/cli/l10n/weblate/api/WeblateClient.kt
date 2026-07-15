package net.thunderbird.cli.l10n.weblate.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.IOException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import net.thunderbird.cli.l10n.config.readText
import net.thunderbird.cli.l10n.config.writeText

interface WeblateClient {
    suspend fun loadComponents(): List<Component>

    suspend fun patchComponent(url: String, patch: ComponentPatch): Boolean

    suspend fun createComponent(create: ComponentCreate): Boolean

    suspend fun deleteComponent(url: String): Boolean
}

class DefaultWeblateClient(
    private val token: String,
    private val config: net.thunderbird.cli.l10n.weblate.api.WeblateConfig,
    private val logLevel: LogLevel = LogLevel.INFO,
    private val client: HttpClient = createWeblateHttpClient(logLevel, json),
) : WeblateClient {

    override suspend fun loadComponents(): List<net.thunderbird.cli.l10n.weblate.api.Component> {
        if (config.cacheEnabled) {
            val cached = readComponentsFromCache()
            if (cached.isNotEmpty()) {
                return cached
            }
        }

        val components = mutableListOf<net.thunderbird.cli.l10n.weblate.api.Component>()
        var page = 1
        var hasNextPage = true

        while (hasNextPage) {
            val componentPage = loadComponentPage(page)
            components.addAll(componentPage.results)

            hasNextPage = componentPage.next != null
            page++
        }

        if (config.cacheEnabled) {
            writeComponentsToCache(components)
        }

        return components
    }

    override suspend fun patchComponent(
        url: String,
        patch: net.thunderbird.cli.l10n.weblate.api.ComponentPatch,
    ): Boolean {
        val response =
            client.patch(url) {
                header(key = HttpHeaders.Authorization, value = "Token $token")
                contentType(ContentType.Application.Json)
                setBody(patch)
            }

        return response.status.value in SUCCESS
    }

    override suspend fun createComponent(
        create: net.thunderbird.cli.l10n.weblate.api.ComponentCreate
    ): Boolean {
        val url = "${config.baseUrl}projects/${config.projectName}/components/"
        val response =
            client.post(url) {
                header(key = HttpHeaders.Authorization, value = "Token $token")
                contentType(ContentType.Application.Json)
                setBody(create)
            }

        return response.status.value in SUCCESS
    }

    override suspend fun deleteComponent(url: String): Boolean {
        val response =
            client.delete(url) { header(key = HttpHeaders.Authorization, value = "Token $token") }

        return response.status.value in SUCCESS
    }

    private suspend fun loadComponentPage(
        page: Int
    ): net.thunderbird.cli.l10n.weblate.api.ComponentResponse {
        return client
            .get(config.componentsUrl(page)) {
                headers {
                    config.getDefaultHeaders(token).forEach { (key, value) -> append(key, value) }
                }
            }
            .body()
    }

    private fun readComponentsFromCache(): List<net.thunderbird.cli.l10n.weblate.api.Component> {
        try {
            val cacheFile = Path(CACHE_FILE_PATH)
            if (SystemFileSystem.exists(cacheFile)) {
                return json.decodeFromString<List<Component>>(cacheFile.readText())
            }
        } catch (e: IOException) {
            println("Failed to read cache file: ${e.message}")
        }
        return emptyList()
    }

    private fun writeComponentsToCache(components: List<Component>) {
        if (components.isEmpty()) return

        try {
            val cacheFile = Path(CACHE_FILE_PATH)
            cacheFile.writeText(json.encodeToString(components))
        } catch (e: IOException) {
            println("Failed to write cache file: ${e.message}")
        }
    }

    private companion object {
        val SUCCESS = 200..299

        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            explicitNulls = false
        }

        private fun WeblateConfig.componentsUrl(page: Int) =
            "${baseUrl}projects/$projectName/components/?page=$page"

        private const val CACHE_FILE_PATH = ".tmp/l10n-weblate-components-cache.json"
    }
}

internal expect fun createWeblateHttpClient(logLevel: LogLevel, json: Json): HttpClient

internal fun HttpClientConfig<*>.installWeblateDefaults(logLevel: LogLevel, json: Json) {
    if (logLevel != LogLevel.NONE) {
        install(Logging) {
            logger = StdoutLogger
            level = logLevel
        }
    }
    install(ContentNegotiation) { json(json) }
}

private object StdoutLogger : Logger {
    override fun log(message: String) {
        println(message)
    }
}
