package net.thunderbird.cli.importer.translation

import de.infix.testBalloon.framework.core.testSuite
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

val TranslationKeyConsolidatorTests by testSuite("TranslationKeyConsolidator") {
    test("keeps the first branch content and records conflicts") {
        val tmpRoot = Files.createTempDirectory("consolidator-")
        try {
            val mainRoot = Files.createDirectories(tmpRoot.resolve("repo-main"))
            val betaRoot = Files.createDirectories(tmpRoot.resolve("repo-beta"))

            val filePath = "app/src/main/res/values/strings.xml"
            val xmlRootPath = "app/src/main/res/values"
            Files.createDirectories(mainRoot.resolve(xmlRootPath))
            Files.createDirectories(betaRoot.resolve(xmlRootPath))

            Files.writeString(
                mainRoot.resolve(filePath),
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <string name="hello">Main</string>
                </resources>
                """.trimIndent(),
            )
            Files.writeString(
                betaRoot.resolve(filePath),
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <string name="hello">Beta</string>
                </resources>
                """.trimIndent(),
            )

            val consolidator = TranslationKeyConsolidator()
            val result = consolidator.consolidateAllFiles(
                branches = listOf("main", "beta"),
                allFiles = setOf(filePath),
                branchRootDirs = mapOf(
                    "main" to mainRoot.toFile(),
                    "beta" to betaRoot.toFile(),
                ),
            )

            assertEquals(1, result.files.size)
            val resolvedFile = result.files.single()
            assertEquals(filePath, resolvedFile.filePath)
            assertEquals("Main", resolvedFile.keys["hello"])
            assertEquals(setOf("main", "beta"), resolvedFile.presentInBranches)

            val conflict = result.conflicts.singleOrNull()
            assertNotNull(conflict)
            assertEquals("hello", conflict.id)
            assertEquals(filePath, conflict.filePath)
            assertTrue(conflict.conflicts.any { it.branch == "main" && it.content == "Main" })
            assertTrue(conflict.conflicts.any { it.branch == "beta" && it.content == "Beta" })
        } finally {
            tmpRoot.toFile().deleteRecursively()
        }
    }
}
