package net.thunderbird.cli.l10n.weblate.api

import kotlinx.serialization.Serializable

@Serializable data class ComponentResponse(val next: String?, val results: List<Component>)
