package net.thunderbird.cli.importer.workflow

import de.infix.testBalloon.framework.core.testSuite
import net.thunderbird.cli.importer.git.BranchFiles
import net.thunderbird.cli.importer.git.Branch
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertTrue

val RepositoryFileResolverTests by testSuite("RepositoryFileResolver") {
    test("uses the first available branch for a file") {
        val tmpRoot = Files.createTempDirectory("resolver-")
        try {
            val mainRoot = Files.createDirectories(tmpRoot.resolve("repo-main"))
            val betaRoot = Files.createDirectories(tmpRoot.resolve("repo-beta"))

            val filePath = "app/src/main/res/values/strings.xml"
            Files.createDirectories(mainRoot.resolve("app/src/main/res/values"))
            Files.createDirectories(betaRoot.resolve("app/src/main/res/values"))
            Files.writeString(mainRoot.resolve(filePath), "main")
            Files.writeString(betaRoot.resolve(filePath), "beta")

            val resolved = RepositoryFileResolver.resolve(
                branchResults = listOf(
                    BranchFiles(Branch("main"), listOf(filePath)),
                    BranchFiles(Branch("beta"), listOf(filePath)),
                ),
                branchRoots = mapOf(
                    Branch("main") to mainRoot.toFile(),
                    Branch("beta") to betaRoot.toFile(),
                ),
            )

            assertEquals(1, resolved.size)
            assertEquals(Branch("main"), resolved.single().sourceBranch)
            assertEquals("main", resolved.single().content)
            assertTrue(resolved.single().relativePath.endsWith("strings.xml"))
        } finally {
            tmpRoot.toFile().deleteRecursively()
        }
    }
}
