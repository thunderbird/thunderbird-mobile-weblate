package net.thunderbird.cli.l10n.sync.io.input

import net.thunderbird.cli.l10n.config.Branch
import net.thunderbird.cli.l10n.sync.model.L10nFile
import net.thunderbird.cli.l10n.sync.model.L10nFileType
import net.thunderbird.cli.l10n.sync.model.SourceResourceFile
import net.thunderbird.cli.l10n.sync.model.SourceTextFile
import net.thunderbird.cli.l10n.sync.model.TranslationResourceFile
import net.thunderbird.cli.l10n.sync.model.TranslationTextFile

class InputFileMapper(private val resourceParser: ResourceParser = XmlResourceParser) {
    fun mapFiles(files: List<InputFile>, source: Boolean): List<L10nFile> {
        return files.map { file ->
            mapFile(file = file, branch = file.sourceBranch, source = source)
        }
    }

    fun mapFile(file: InputFile, branch: Branch, source: Boolean): L10nFile {
        return when (val type = file.relativePath.toL10nFileType()) {
            L10nFileType.ANDROID_RESOURCE,
            L10nFileType.COMPOSE_RESOURCE ->
                mapResourceFile(file = file, branch = branch, type = type, source = source)

            L10nFileType.STORE_METADATA,
            L10nFileType.RAW_TEXT ->
                mapTextFile(file = file, branch = branch, type = type, source = source)
        }
    }

    private fun mapTextFile(
        file: InputFile,
        branch: Branch,
        type: L10nFileType,
        source: Boolean,
    ): L10nFile {
        return if (source) {
            SourceTextFile(
                relativePath = file.relativePath,
                branch = branch,
                type = type,
                content = file.content,
            )
        } else {
            TranslationTextFile(
                relativePath = file.relativePath,
                branch = branch,
                type = type,
                content = file.content,
            )
        }
    }

    private fun mapResourceFile(
        file: InputFile,
        branch: Branch,
        type: L10nFileType,
        source: Boolean,
    ): L10nFile {
        return if (source) {
            SourceResourceFile(
                relativePath = file.relativePath,
                branch = branch,
                type = type,
                keys = resourceParser.parseSource(file.content, relativePath = file.relativePath),
            )
        } else {
            TranslationResourceFile(
                relativePath = file.relativePath,
                branch = branch,
                type = type,
                keys =
                    resourceParser.parseTranslation(file.content, relativePath = file.relativePath),
            )
        }
    }

    private fun String.toL10nFileType(): L10nFileType {
        return when {
            contains("/composeResources/") -> L10nFileType.COMPOSE_RESOURCE
            endsWith(".xml") -> L10nFileType.ANDROID_RESOURCE
            startsWith("app-metadata/") -> L10nFileType.STORE_METADATA
            endsWith(".txt") -> L10nFileType.RAW_TEXT
            else -> error("Unsupported localization file type: $this")
        }
    }
}
