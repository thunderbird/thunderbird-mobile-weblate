package net.thunderbird.cli.importer.translation

import de.infix.testBalloon.framework.core.testSuite
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

val TranslationKeyParserTests by
    testSuite("TranslationKeyParser") {
        test("parses unique string keys") {
            val parser = TranslationKeyParser()
            val file = Files.createTempFile("strings-", ".xml").toFile()
            try {
                file.writeText(
                    """
                    <?xml version="1.0" encoding="utf-8"?>
                    <resources>
                        <string name="hello">Hello</string>
                        <string name="goodbye">Goodbye</string>
                    </resources>
                    """
                        .trimIndent()
                )

                val keys = parser.parseStringsXml(file)

                assertEquals(2, keys.size)
                assertEquals("Hello", keys["hello"])
                assertEquals("Goodbye", keys["goodbye"])
            } finally {
                file.delete()
            }
        }

        test("fails on duplicate string keys") {
            val parser = TranslationKeyParser()
            val file = Files.createTempFile("strings-", ".xml").toFile()
            try {
                file.writeText(
                    """
                    <?xml version="1.0" encoding="utf-8"?>
                    <resources>
                        <string name="dup">One</string>
                        <string name="dup">Two</string>
                    </resources>
                    """
                        .trimIndent()
                )

                assertFailsWith<IllegalStateException> { parser.parseStringsXml(file) }
            } finally {
                file.delete()
            }
        }
    }
