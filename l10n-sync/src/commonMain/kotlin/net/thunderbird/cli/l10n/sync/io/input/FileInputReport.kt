package net.thunderbird.cli.l10n.sync.io.input

import net.thunderbird.cli.l10n.config.Branch
import net.thunderbird.cli.l10n.sync.model.ResourceKeyConflict
import net.thunderbird.cli.l10n.sync.model.ResourceKeyResolution
import net.thunderbird.cli.l10n.sync.model.TextFileConflict

data class FileInputReport(
    val path: String,
    val presentBranches: Set<Branch>,
    val keyResolutions: List<ResourceKeyResolution>,
    val conflicts: List<ResourceKeyConflict>,
    val textFileConflict: TextFileConflict? = null,
)
