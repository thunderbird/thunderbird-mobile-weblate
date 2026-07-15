package net.thunderbird.cli.l10n.weblate.project

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import net.thunderbird.cli.l10n.config.resolve
import net.thunderbird.cli.l10n.config.writeText
import net.thunderbird.cli.l10n.weblate.support.temporaryDirectoryFixture

val ComponentConfigLoaderTests by
    testSuite("ComponentConfigLoader") {
        temporaryDirectoryFixture().asParameterForEach {
            test("loads component config and ignores unknown fields") { root ->
                val file = root.resolve("component.json")
                file.writeText(
                    """
                    {
                      "license": "MPL-2.0",
                      "priority": 100,
                      "file_format_params": {
                        "xml_closing_tags": true
                      },
                      "unknown": "ignored"
                    }
                    """
                        .trimIndent()
                )

                val config = ComponentConfigLoader().load(file)

                assertEquals("MPL-2.0", config.license)
                assertEquals(100, config.priority)
                assertEquals(true, config.fileFormatParams.xmlClosingTags)
            }
        }
    }
