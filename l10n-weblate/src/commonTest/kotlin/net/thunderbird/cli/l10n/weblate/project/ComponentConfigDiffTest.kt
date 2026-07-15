package net.thunderbird.cli.l10n.weblate.project

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import net.thunderbird.cli.l10n.weblate.api.ComponentConfig
import net.thunderbird.cli.l10n.weblate.api.FileFormatParams

val ComponentConfigDiffTests by
    testSuite("ComponentConfigDiff") {
        test("returns empty diff for equal configs") {
            val config = componentConfig()

            assertEquals(emptyList(), ComponentConfigDiff.computeConfigDiff(config, config))
        }

        test("reports value set and multiline differences") {
            val expected =
                componentConfig(
                    license = "MPL-2.0",
                    enforcedChecks = listOf("plurals", "placeholders"),
                    commitMessage = "Commit\nmessage",
                    fileFormatParams = FileFormatParams(xmlClosingTags = true),
                )
            val actual =
                componentConfig(
                    license = "GPL-3.0",
                    enforcedChecks = listOf("plurals", "same"),
                    commitMessage = "Commit\nchanged",
                    fileFormatParams = FileFormatParams(xmlClosingTags = false),
                )

            assertEquals(
                listOf(
                    "  license: expected=MPL-2.0, actual=GPL-3.0",
                    """
                    |  enforced_checks:
                    |    missing:
                    |      - placeholders
                    |    unexpected:
                    |      + same
                    """
                        .trimMargin(),
                    """
                    |  commit_message:
                    |         [2] expected: message
                    |         [2] actual  : changed
                    """
                        .trimMargin(),
                    "  file_format_params.xml_closing_tags: expected=true, actual=false",
                ),
                ComponentConfigDiff.computeConfigDiff(expected, actual, indentLevel = 1),
            )
        }
    }

private fun componentConfig(
    license: String = "MPL-2.0",
    enforcedChecks: List<String> = listOf("plurals"),
    commitMessage: String = "Commit",
    fileFormatParams: FileFormatParams = FileFormatParams(xmlClosingTags = true),
): ComponentConfig =
    ComponentConfig(
        license = license,
        enforcedChecks = enforcedChecks,
        commitMessage = commitMessage,
        fileFormatParams = fileFormatParams,
    )
