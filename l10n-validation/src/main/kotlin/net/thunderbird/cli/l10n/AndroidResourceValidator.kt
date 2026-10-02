package net.thunderbird.cli.l10n

import java.math.BigInteger
import java.util.Date
import java.util.Formattable
import java.util.Formatter
import java.util.IllegalFormatException
import java.util.Locale

internal fun ResourceParser.validateAndroid(
    content: String?,
    path: String,
    ref: String,
): List<String> {
    val document = parseDocument(content, path, ref)
    return buildList {
        document.duplicateResourceKeys.sorted().forEach { key ->
            add("$path: $key is defined more than once at $ref.")
        }
        document.entries.forEach { (key, entry) ->
            entry.invalidAndroidPlaceholders.sorted().forEach { placeholder ->
                add(
                    "$path: $key uses invalid Android placeholder $placeholder at $ref. " +
                        "Use Android-compatible java.util.Formatter placeholders."
                )
            }
            if (entry.hasInvalidAndroidXliffMarkup) {
                add(
                    "$path: $key uses invalid Android xliff markup at $ref. " +
                        "Only xliff:g elements with a non-empty id are supported."
                )
            }
            if (key.startsWith("plurals:")) {
                addAll(validateAndroidPlural(path, ref, key, entry))
            }
        }
    }
}

private fun validateAndroidPlural(
    path: String,
    ref: String,
    key: String,
    entry: ResourceEntry,
): List<String> = buildList {
    if (entry.hasPluralItemWithoutQuantity) {
        add("$path: $key has an item without quantity at $ref.")
    }
    entry.duplicatePluralQuantities.sorted().forEach { quantity ->
        add("$path: $key repeats quantity $quantity at $ref.")
    }
    if ("other" !in entry.pluralQuantities) {
        add("$path: $key must define quantity other at $ref.")
    }
    (entry.pluralQuantities - ANDROID_PLURAL_QUANTITIES).sorted().forEach { quantity ->
        add(
            "$path: $key uses invalid quantity $quantity at $ref. " +
                "Android plural quantities must be one of ${ANDROID_PLURAL_QUANTITIES.sorted()}."
        )
    }
}

private val ANDROID_PLURAL_QUANTITIES = setOf("zero", "one", "two", "few", "many", "other")

internal object AndroidFormatParser {
    fun parse(text: String): AndroidFormatAnalysis {
        val state = AndroidFormatState()
        var percentIndex = text.indexOf('%')
        while (percentIndex >= 0) {
            val result = parseAt(text, percentIndex, state)
            result.placeholder?.let(state.placeholders::add)
            result.invalidPlaceholder?.let(state.invalidPlaceholders::add)
            percentIndex = text.indexOf('%', result.nextOffset)
        }
        return AndroidFormatAnalysis(state.placeholders, state.invalidPlaceholders)
    }

    private fun parseAt(
        text: String,
        percentIndex: Int,
        state: AndroidFormatState,
    ): AndroidFormatTokenResult {
        if (text.getOrNull(percentIndex + 1) in setOf('%', 'n')) {
            return AndroidFormatTokenResult(nextOffset = percentIndex + 2)
        }

        val match = ANDROID_FORMAT_PATTERN.find(text, percentIndex)
        return if (match == null || match.range.first != percentIndex) {
            AndroidFormatTokenResult(
                nextOffset = percentIndex + 1,
                invalidPlaceholder = androidPlaceholderCandidate(text, percentIndex),
            )
        } else {
            parseMatch(match, state)
        }
    }

    private fun parseMatch(
        match: MatchResult,
        state: AndroidFormatState,
    ): AndroidFormatTokenResult {
        val explicitIndexText = match.groups[1]?.value
        val explicitIndex = explicitIndexText?.toIntOrNull()
        val hasValidExplicitIndex = explicitIndexText == null || explicitIndex != null
        val flags = match.groups[2]?.value.orEmpty()
        val datePrefix = match.groups[5]?.value
        val conversion = match.groups[6]?.value.orEmpty()
        if (conversion == "%") return parsePercentConversion(match)

        val argumentIndex =
            if (hasValidExplicitIndex) resolveArgumentIndex(explicitIndex, flags, state) else null
        val hasValidFormat =
            hasSupportedFlags(match) &&
                isValidConversion(datePrefix, conversion) &&
                hasValidFormatterSyntax(match, conversion)
        return if (argumentIndex == null || !hasValidFormat) {
            AndroidFormatTokenResult(
                nextOffset = match.range.last + 1,
                invalidPlaceholder = match.value,
            )
        } else {
            state.previousIndex = argumentIndex
            AndroidFormatTokenResult(
                nextOffset = match.range.last + 1,
                placeholder = "%$argumentIndex\$${datePrefix.orEmpty()}$conversion",
            )
        }
    }

    private fun parsePercentConversion(match: MatchResult): AndroidFormatTokenResult =
        if (hasSupportedFlags(match) && hasValidFormatterSyntax(match, "%")) {
            AndroidFormatTokenResult(nextOffset = match.range.last + 1)
        } else {
            AndroidFormatTokenResult(
                nextOffset = match.range.last + 1,
                invalidPlaceholder = match.value,
            )
        }

    private fun hasSupportedFlags(match: MatchResult): Boolean =
        '0' !in match.groups[2]?.value.orEmpty()

    private fun hasValidFormatterSyntax(match: MatchResult, conversion: String): Boolean {
        val flags = match.groups[2]?.value.orEmpty().replace("<", "")
        val width = match.groups[3]?.value?.let { "1" }.orEmpty()
        val precision = match.groups[4]?.value?.let { ".1" }.orEmpty()
        val datePrefix = match.groups[5]?.value.orEmpty()
        val normalized = "%$flags$width$precision$datePrefix$conversion"
        return try {
            Formatter(StringBuilder(), Locale.ROOT).use { formatter ->
                if (conversion == "%") {
                    formatter.format(normalized)
                } else {
                    formatter.format(normalized, formatterArgument(datePrefix, conversion))
                }
            }
            true
        } catch (_: IllegalFormatException) {
            false
        }
    }

    private fun formatterArgument(datePrefix: String, conversion: String): Any =
        when {
            datePrefix.isNotEmpty() -> Date(0)
            conversion.equals("s", ignoreCase = true) -> FORMATTABLE_ARGUMENT
            conversion.equals("c", ignoreCase = true) -> 'A'.code
            conversion.lowercase() in setOf("d", "o", "x") -> BigInteger.ONE
            conversion.lowercase() in setOf("e", "f", "g", "a") -> 1.0
            else -> Any()
        }

    private fun resolveArgumentIndex(
        explicitIndex: Int?,
        flags: String,
        state: AndroidFormatState,
    ): Int? {
        val reusesPrevious = '<' in flags
        return when {
            explicitIndex != null && (explicitIndex <= 0 || reusesPrevious) -> null
            explicitIndex != null -> explicitIndex
            reusesPrevious -> state.previousIndex
            else -> state.nextOrdinaryIndex++
        }
    }

    private fun isValidConversion(datePrefix: String?, conversion: String): Boolean =
        if (datePrefix == null) conversion.singleOrNull() in ANDROID_CONVERSIONS
        else conversion.singleOrNull() in ANDROID_DATE_TIME_CONVERSIONS
}

private fun androidPlaceholderCandidate(text: String, startIndex: Int): String {
    val endIndex =
        (startIndex + 1 until text.length).firstOrNull { index ->
            text[index].isWhitespace() || text[index] == '%'
        } ?: text.length
    return text.substring(startIndex, endIndex).ifEmpty { "%" }
}

private val ANDROID_FORMAT_PATTERN =
    Regex("""%(?:(\d+)\$)?([-#+ 0,(<]*)(\d+)?(?:\.(\d+))?([tT])?([A-Za-z%])""")
private val ANDROID_CONVERSIONS =
    setOf(
        'b',
        'B',
        'h',
        'H',
        's',
        'S',
        'c',
        'C',
        'd',
        'o',
        'x',
        'X',
        'e',
        'E',
        'f',
        'g',
        'G',
        'a',
        'A',
    )
private val ANDROID_DATE_TIME_CONVERSIONS =
    setOf(
        'H',
        'I',
        'k',
        'l',
        'M',
        'S',
        'L',
        'N',
        'p',
        'z',
        'Z',
        's',
        'Q',
        'B',
        'b',
        'h',
        'A',
        'a',
        'C',
        'Y',
        'y',
        'j',
        'm',
        'd',
        'e',
        'R',
        'T',
        'r',
        'D',
        'F',
        'c',
    )

private data class AndroidFormatState(
    val placeholders: MutableSet<String> = mutableSetOf(),
    val invalidPlaceholders: MutableSet<String> = mutableSetOf(),
    var nextOrdinaryIndex: Int = 1,
    var previousIndex: Int? = null,
)

private data class AndroidFormatTokenResult(
    val nextOffset: Int,
    val placeholder: String? = null,
    val invalidPlaceholder: String? = null,
)

private val FORMATTABLE_ARGUMENT = Formattable { formatter, _, _, _ ->
    formatter.out().append("value")
}

internal data class AndroidFormatAnalysis(
    val placeholders: Set<String>,
    val invalidPlaceholders: Set<String>,
)
