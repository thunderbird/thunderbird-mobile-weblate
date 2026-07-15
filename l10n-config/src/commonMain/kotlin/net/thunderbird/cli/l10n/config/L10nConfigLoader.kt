package net.thunderbird.cli.l10n.config

import kotlinx.io.files.Path

class L10nConfigLoader {

    fun load(projectRoot: Path, configPath: String? = null): L10nProjectConfig {
        return L10nProjectConfigLoader.load(projectRoot, configPath)
    }

    companion object {
        fun load(): L10nConfig {
            val projectRoot = ProjectRootResolver.resolve(Path("."))
            val projectConfig = L10nProjectConfigLoader.load(projectRoot)
            return L10nConfig(projectRoot, projectConfig)
        }
    }
}
