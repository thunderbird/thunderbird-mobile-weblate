package net.thunderbird.cli.l10n.weblate.command

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import net.thunderbird.cli.l10n.config.Branch
import net.thunderbird.cli.l10n.config.ComponentDiscoveryConfig
import net.thunderbird.cli.l10n.config.ImportConfig
import net.thunderbird.cli.l10n.config.L10nConfig
import net.thunderbird.cli.l10n.config.L10nProjectConfig
import net.thunderbird.cli.l10n.config.Repository
import net.thunderbird.cli.l10n.config.SourceConfig
import net.thunderbird.cli.l10n.config.WeblateConfig
import net.thunderbird.cli.l10n.terminal.TerminalLine
import net.thunderbird.cli.l10n.terminal.TerminalState
import net.thunderbird.cli.l10n.terminal.TerminalStateStore
import net.thunderbird.cli.l10n.weblate.api.Component
import net.thunderbird.cli.l10n.weblate.api.ComponentConfig
import net.thunderbird.cli.l10n.weblate.api.ComponentCreate
import net.thunderbird.cli.l10n.weblate.api.ComponentInfo
import net.thunderbird.cli.l10n.weblate.api.ComponentPatch
import net.thunderbird.cli.l10n.weblate.api.WeblateClient
import net.thunderbird.cli.l10n.weblate.project.ComponentInfo as LocalComponentInfo

val WeblateCommandTests by
    testSuite("WeblateCommand") {
        test("list reports local and remote component differences") {
            val store = TerminalStateStore(TerminalState("test"))
            val result =
                ListCommand(config = commandConfig(), options = options(), store = store)
                    .onRun(
                        client = FakeWeblateClient(),
                        defaultComponentConfig = ComponentConfig(),
                        localComponents =
                            listOf(
                                LocalComponentInfo("app-common", "app-common", androidType()),
                                LocalComponentInfo("feature-chat", "feature/chat", androidType()),
                            ),
                    )

            assertEquals(2, result.weblateComponents)
            assertEquals(2, result.localComponents)
            assertEquals(1, result.missingComponents)
            assertEquals(1, result.ignoredOutsideCategory)
        }

        test("create dry-run reports missing components without calling API") {
            val client = FakeWeblateClient()
            val store = TerminalStateStore(TerminalState("test"))

            val result =
                CreateCommand(config = commandConfig(), options = options(), store = store)
                    .onRun(
                        client = client,
                        defaultComponentConfig = ComponentConfig(editTemplate = true),
                        localComponents =
                            listOf(
                                LocalComponentInfo("app-common", "app-common", androidType()),
                                LocalComponentInfo("feature-chat", "feature/chat", androidType()),
                            ),
                    )

            assertEquals(2, result.localComponents)
            assertEquals(1, result.missingComponents)
            assertEquals(1, result.ignoredOutsideCategory)
            assertEquals(emptyList(), client.createdComponents)
        }

        test("update dry-run reports managed and skipped components") {
            val store = TerminalStateStore(TerminalState("test"))

            val result =
                UpdateCommand(config = commandConfig(), options = options(), store = store)
                    .onRun(
                        client = FakeWeblateClient(),
                        defaultComponentConfig = ComponentConfig(license = "MPL-2.0"),
                        localComponents =
                            listOf(LocalComponentInfo("app-common", "app-common", androidType())),
                    )

            assertEquals(1, result.managedComponents)
            assertEquals(1, result.skippedComponents)
            assertEquals(1, result.ignoredOutsideCategory)
        }

        test("delete dry-run finds component without deleting it") {
            val client = FakeWeblateClient()
            val store = TerminalStateStore(TerminalState("test"))

            val result =
                DeleteCommand(
                        config = commandConfig(),
                        options = options(),
                        store = store,
                        slugToDelete = "app-common",
                    )
                    .onRun(
                        client = client,
                        defaultComponentConfig = ComponentConfig(),
                        localComponents = emptyList(),
                    )

            assertEquals("app-common", result.slug)
            assertEquals(true, result.found)
            assertFalse(result.deletionAttempted)
            assertFalse(result.deleted)
            assertEquals(emptyList(), client.deletedUrls)
        }

        test("delete reports available slugs when component is missing") {
            val store = TerminalStateStore(TerminalState("test"))

            val result =
                DeleteCommand(
                        config = commandConfig(),
                        options = options(),
                        store = store,
                        slugToDelete = "missing",
                    )
                    .onRun(
                        client = FakeWeblateClient(),
                        defaultComponentConfig = ComponentConfig(),
                        localComponents = emptyList(),
                    )

            assertEquals(false, result.found)
            assertContains(
                store.state.value.lines,
                TerminalLine.Error("    Could not find component with slug in category: missing"),
            )
        }
    }

private fun androidType(): net.thunderbird.cli.l10n.config.ResourceKind =
    net.thunderbird.cli.l10n.config.ResourceKind.ANDROID

private fun options(): WeblateCommandOptions =
    WeblateCommandOptions(token = "token", applyChanges = false)

private fun commandConfig(): L10nConfig =
    L10nConfig(
        projectRoot = kotlinx.io.files.Path("."),
        project =
            L10nProjectConfig(
                source =
                    SourceConfig(
                        repository = Repository("https://example.com/source.git"),
                        branches = listOf(Branch("main")),
                    ),
                import =
                    ImportConfig(
                        sourceFilePatterns = emptyList(),
                        translatedFilePatterns = emptyList(),
                        excludedPaths = emptyList(),
                    ),
                weblate =
                    WeblateConfig(
                        baseUrl = "https://hosted.weblate.org/api/",
                        projectSlug = "thunderbird",
                        defaultLinkedComponent = "app-common",
                        componentRepo = "weblate://thunderbird/app-common",
                        discovery = ComponentDiscoveryConfig(resources = emptyList()),
                    ),
            ),
    )

private class FakeWeblateClient : WeblateClient {
    val createdComponents = mutableListOf<ComponentCreate>()
    val deletedUrls = mutableListOf<String>()
    val patches = mutableListOf<ComponentPatch>()

    override suspend fun loadComponents(): List<Component> =
        listOf(
            component(
                id = 1,
                slug = "app-common",
                name = "app:common",
                category = "category/default",
                license = "GPL-3.0",
            ),
            component(
                id = 2,
                slug = "remote-only",
                name = "remote:only",
                category = "category/default",
            ),
            component(id = 3, slug = "outside", name = "outside", category = "category/other"),
        )

    override suspend fun patchComponent(url: String, patch: ComponentPatch): Boolean {
        patches += patch
        return true
    }

    override suspend fun createComponent(create: ComponentCreate): Boolean {
        createdComponents += create
        return true
    }

    override suspend fun deleteComponent(url: String): Boolean {
        deletedUrls += url
        return true
    }
}

private fun component(
    id: Int,
    slug: String,
    name: String,
    category: String?,
    license: String = "",
): Component =
    Component(
        info =
            ComponentInfo(
                id = id,
                name = name,
                slug = slug,
                url = "https://hosted.weblate.org/api/components/$slug/",
                category = category,
                linkedComponent = "https://hosted.weblate.org/api/components/app-common/",
            ),
        config = ComponentConfig(license = license),
    )
