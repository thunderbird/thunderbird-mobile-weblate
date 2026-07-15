package net.thunderbird.cli.importer.git

data class GitRepository(val organization: String?, val name: String, val url: String) {
    constructor(
        organization: String,
        name: String,
    ) : this(
        organization = organization,
        name = name,
        url = "$SOURCE_URL_PREFIX/$organization/$name",
    )

    companion object {
        private const val SOURCE_URL_PREFIX = "https://github.com"
    }
}
