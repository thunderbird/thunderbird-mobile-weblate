package net.thunderbird.cli.l10n.weblate.command

sealed interface WeblateCommandResult

data class UpdateResult(
    val managedComponents: Int,
    val skippedComponents: Int,
    val ignoredOutsideCategory: Int,
) : WeblateCommandResult

data class CreateResult(
    val localComponents: Int,
    val missingComponents: Int,
    val ignoredOutsideCategory: Int,
) : WeblateCommandResult

data class ListResult(
    val weblateComponents: Int,
    val localComponents: Int,
    val missingComponents: Int,
    val ignoredOutsideCategory: Int,
) : WeblateCommandResult

data class DeleteResult(
    val slug: String,
    val found: Boolean,
    val deletionAttempted: Boolean,
    val deleted: Boolean,
) : WeblateCommandResult
