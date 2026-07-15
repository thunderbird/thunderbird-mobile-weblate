package net.thunderbird.cli.l10n.weblate.api

/**
 * Configuration for Weblate API
 *
 * @property baseUrl Base URL of the Weblate API
 * @property projectName Name of the Weblate project
 * @property defaultComponent Default component to use for translations
 * @property cacheEnabled Whether caching is enabled for API responses
 */
data class WeblateConfig(
    val baseUrl: String,
    val projectName: String,
    val defaultComponent: String,
    val cacheEnabled: Boolean = false,
) {
    fun getDefaultHeaders(token: String): List<Pair<String, String>> =
        DEFAULT_HEADERS.mapValues {
                it.value.replace(oldValue = PLACEHOLDER_TOKEN, newValue = token)
            }
            .map { (key, value) -> key to value }

    private companion object {
        const val PLACEHOLDER_TOKEN = "{weblate_token}"
        val DEFAULT_HEADERS =
            mapOf("Accept" to "application/json", "Authorization" to "Token $PLACEHOLDER_TOKEN")
    }
}
