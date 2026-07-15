package net.thunderbird.cli.l10n.sync.io.input

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import net.thunderbird.cli.l10n.config.Branch
import net.thunderbird.cli.l10n.sync.model.L10nFileType
import net.thunderbird.cli.l10n.sync.model.SourceResourceFile
import net.thunderbird.cli.l10n.sync.model.SourceTextFile
import net.thunderbird.cli.l10n.sync.model.TranslationResourceFile
import net.thunderbird.cli.l10n.sync.model.TranslationTextFile

val InputFileMapperTests by
    testSuite("InputFileMapper") {
        val main = Branch("main")
        val mapper = InputFileMapper()

        test("maps Android XML resources") {
            val file =
                InputFile(
                    relativePath = "feature/example/src/main/res/values/strings.xml",
                    sourceBranch = main,
                    content =
                        """
                        <resources>
                            <string name="example">Example</string>
                        </resources>
                        """
                            .trimIndent(),
                )

            val result = mapper.mapFile(file = file, branch = main, source = true)

            assertTrue(result is SourceResourceFile)
            assertEquals(L10nFileType.ANDROID_RESOURCE, result.type)
            assertEquals(main, result.branch)
        }

        test("maps Compose XML resources") {
            val file =
                InputFile(
                    relativePath =
                        "feature/example/src/commonMain/composeResources/values/strings.xml",
                    sourceBranch = main,
                    content =
                        """
                        <resources>
                            <string name="example">Example</string>
                        </resources>
                        """
                            .trimIndent(),
                )

            val result = mapper.mapFile(file = file, branch = main, source = true)

            assertTrue(result is SourceResourceFile)
            assertEquals(L10nFileType.COMPOSE_RESOURCE, result.type)
            assertEquals(main, result.branch)
        }

        test("maps store metadata text") {
            val file =
                InputFile(
                    relativePath = "app-metadata/net.thunderbird.android/en-US/title.txt",
                    sourceBranch = main,
                    content = "Thunderbird",
                )

            val result = mapper.mapFile(file = file, branch = main, source = true)

            assertTrue(result is SourceTextFile)
            assertEquals(L10nFileType.STORE_METADATA, result.type)
            assertEquals(main, result.branch)
        }

        test("maps raw text") {
            val file =
                InputFile(
                    relativePath = "other/example.txt",
                    sourceBranch = main,
                    content = "Example",
                )

            val result = mapper.mapFile(file = file, branch = main, source = true)

            assertTrue(result is SourceTextFile)
            assertEquals(L10nFileType.RAW_TEXT, result.type)
            assertEquals(main, result.branch)
        }

        test("maps translation XML resources") {
            val file =
                InputFile(
                    relativePath = "feature/example/src/main/res/values-de/strings.xml",
                    sourceBranch = main,
                    content =
                        """
                        <resources>
                            <string name="example">Beispiel</string>
                        </resources>
                        """
                            .trimIndent(),
                )

            val result = mapper.mapFile(file = file, branch = main, source = false)

            assertTrue(result is TranslationResourceFile)
            assertEquals(L10nFileType.ANDROID_RESOURCE, result.type)
            assertEquals(main, result.branch)
            assertEquals("example", result.keys.single().id)
        }

        test("maps translation text") {
            val file =
                InputFile(
                    relativePath = "app-metadata/net.thunderbird.android/de/title.txt",
                    sourceBranch = main,
                    content = "Thunderbird",
                )

            val result = mapper.mapFile(file = file, branch = main, source = false)

            assertTrue(result is TranslationTextFile)
            assertEquals(L10nFileType.STORE_METADATA, result.type)
            assertEquals(main, result.branch)
        }

        test("rejects unsupported file type") {
            val file =
                InputFile(relativePath = "other/example.json", sourceBranch = main, content = "{}")

            assertFailsWith<IllegalStateException> {
                mapper.mapFile(file = file, branch = main, source = true)
            }
        }
    }
