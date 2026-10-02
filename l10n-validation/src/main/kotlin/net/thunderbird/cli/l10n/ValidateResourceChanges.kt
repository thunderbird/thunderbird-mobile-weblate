package net.thunderbird.cli.l10n

internal class ValidateResourceChanges :
    ValidationCommand(
        "validate-resource-changes",
        "Validate changed Android and Compose resources.",
    ) {
    override fun run() {
        val result = ResourceChangeChecker(gitClient).check(baseRef, headRef)
        report(
            result,
            "Validated ${result.filesChecked} changed Android and Compose resource files.",
        )
    }
}
