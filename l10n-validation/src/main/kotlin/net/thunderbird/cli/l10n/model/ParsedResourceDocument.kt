package net.thunderbird.cli.l10n.model

internal data class ParsedResourceDocument(
    val entries: Map<String, ResourceEntry>,
    val hasXliffNamespace: Boolean,
    val duplicateResourceKeys: Set<String>,
)
