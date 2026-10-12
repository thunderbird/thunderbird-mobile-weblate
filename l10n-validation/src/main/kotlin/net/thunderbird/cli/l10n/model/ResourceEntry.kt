package net.thunderbird.cli.l10n.model

internal data class ResourceEntry(
    val serialized: String,
    val placeholders: Set<String>,
    val pluralQuantities: Set<String>,
    val placeholdersByQuantity: Map<String, Set<String>>,
    val placeholdersByArrayItem: List<Set<String>>,
    val androidPlaceholders: Set<String>,
    val androidPlaceholdersByQuantity: Map<String, Set<String>>,
    val androidPlaceholdersByArrayItem: List<Set<String>>,
    val hasPluralItemWithoutQuantity: Boolean,
    val duplicatePluralQuantities: Set<String>,
    val isTranslatable: Boolean,
    val hasXliffMarkup: Boolean,
    val hasInvalidAndroidXliffMarkup: Boolean,
    val invalidComposePlaceholders: Set<String>,
    val invalidAndroidPlaceholders: Set<String>,
)
