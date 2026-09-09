package net.thunderbird.cli.l10n.sync.io.output

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import net.thunderbird.cli.l10n.config.Branch
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
    }
