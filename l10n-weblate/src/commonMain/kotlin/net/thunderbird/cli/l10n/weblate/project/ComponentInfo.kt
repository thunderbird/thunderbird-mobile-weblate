package net.thunderbird.cli.l10n.weblate.project

import net.thunderbird.cli.l10n.config.ResourceKind

data class ComponentInfo(val slug: String, val path: String, val type: ResourceKind)
