package net.thunderbird.cli.l10n.sync.task

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import net.thunderbird.cli.l10n.config.Branch
import net.thunderbird.cli.l10n.sync.model.L10nFileType
import net.thunderbird.cli.l10n.sync.model.SourceKey
import net.thunderbird.cli.l10n.sync.model.SourceResourceFile

val ResourceSourceFileMergerTests by
    testSuite("ResourceSourceFileMerger") {
        test("includes the union of keys from all branches") {
            val mainKeys = (1..10).map { key("main_$it") }
            val betaKeys = mainKeys + listOf(key("beta_1"), key("beta_2"))
            val releaseKeys =
                mainKeys + listOf(key("release_1"), key("release_2"), key("release_3"))

            val result =
                assertNotNull(
                    ResourceSourceFileMerger()
                        .merge(
                            relativePath = SOURCE_FILE,
                            files =
                                listOf(
                                    sourceFile("main", mainKeys),
                                    sourceFile("beta", betaKeys),
                                    sourceFile("release", releaseKeys),
                                ),
                        )
                )

            assertEquals(
                (mainKeys + betaKeys + releaseKeys).map { it.id }.toSet(),
                result.file.keys.map { it.id }.toSet(),
            )
            assertEquals(15, result.file.keys.size)
        }
    }

private const val SOURCE_FILE = "feature/example/src/main/res/values/strings.xml"

private fun sourceFile(branch: String, keys: List<SourceKey>) =
    SourceResourceFile(
        relativePath = SOURCE_FILE,
        branch = Branch(branch),
        type = L10nFileType.ANDROID_RESOURCE,
        keys = keys,
    )

private fun key(id: String) = SourceKey(id = id, content = id, comments = emptyList())
