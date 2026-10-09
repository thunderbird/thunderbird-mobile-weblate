package net.thunderbird.cli.l10n.sync.io.output

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import net.thunderbird.cli.l10n.config.Branch
import net.thunderbird.cli.l10n.sync.io.input.InputFile
import net.thunderbird.cli.l10n.sync.io.input.InputFileMapper
import net.thunderbird.cli.l10n.sync.model.L10nFileType
import net.thunderbird.cli.l10n.sync.model.SourceKey
import net.thunderbird.cli.l10n.sync.model.SourceResourceFile

val OutputFileMapperTests by
    testSuite("OutputFileMapper") {
        test("declares the xliff namespace when a resource uses xliff elements") {
            val file =
                SourceResourceFile(
                    relativePath = "feature/example/src/main/res/values/strings.xml",
                    branch = Branch("main"),
                    type = L10nFileType.ANDROID_RESOURCE,
                    keys =
                        listOf(
                            SourceKey(
                                id = "example",
                                content =
                                    """<string name="example"><xliff:g id="value">%s</xliff:g></string>""",
                                comments = emptyList(),
                            )
                        ),
                )

            val result = OutputFileMapper.mapFile(file)

            assertEquals(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">
                    <string name="example"><xliff:g id="value">%s</xliff:g></string>
                </resources>

                """
                    .trimIndent(),
                result.content,
            )
        }

        test("renders plural indentation idempotently") {
            val original =
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">
                    <plurals name="notification_new_messages_title">
                        <item quantity="other"><xliff:g id="count">%d</xliff:g> messages</item>
                    </plurals>
                </resources>
                """
                    .trimIndent()

            val firstRender = parseAndRender(original)
            val secondRender = parseAndRender(firstRender)

            assertEquals(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">
                    <plurals name="notification_new_messages_title">
                        <item quantity="other"><xliff:g id="count">%d</xliff:g> messages</item>
                    </plurals>
                </resources>

                """
                    .trimIndent(),
                firstRender,
            )
            assertEquals(firstRender, secondRender)
        }
    }

private fun parseAndRender(content: String): String {
    val file =
        InputFileMapper()
            .mapFile(
                file =
                    InputFile(
                        relativePath = "feature/example/src/main/res/values/strings.xml",
                        sourceBranch = Branch("main"),
                        content = content,
                    ),
                branch = Branch("main"),
                source = true,
            )
    return OutputFileMapper.mapFile(file).content
}
