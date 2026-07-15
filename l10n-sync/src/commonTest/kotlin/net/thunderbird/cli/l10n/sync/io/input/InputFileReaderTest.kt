package net.thunderbird.cli.l10n.sync.io.input

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import net.thunderbird.cli.l10n.config.Branch
import net.thunderbird.cli.l10n.config.resolve
import net.thunderbird.cli.l10n.config.writeText
import net.thunderbird.cli.l10n.sync.support.temporaryDirectoryFixture

val InputFileReaderTests by
    testSuite("InputFileReader") {
        temporaryDirectoryFixture().asParameterForEach {
            test("reads files by branch order") { temporaryDirectory ->
                val beta = Branch("beta")
                val release = Branch("release")
                val betaRoot = temporaryDirectory.resolve("beta")
                val releaseRoot = temporaryDirectory.resolve("release")
                val path = "app-metadata/en-US/title.txt"
                betaRoot.resolve(path).writeText("Beta")
                releaseRoot.resolve(path).writeText("Release")

                val result =
                    InputFileReader()
                        .readFiles(
                            relativePath = path,
                            branches = listOf(beta, release),
                            branchRoots = mapOf(beta to betaRoot, release to releaseRoot),
                        )

                assertEquals(listOf(beta, release), result.map { file -> file.sourceBranch })
                assertEquals(listOf("Beta", "Release"), result.map { file -> file.content })
            }

            test("returns an empty list when file is not available") { temporaryDirectory ->
                val main = Branch("main")

                val result =
                    InputFileReader()
                        .readFiles(
                            relativePath = "missing.txt",
                            branches = listOf(main),
                            branchRoots = mapOf(main to temporaryDirectory.resolve("main")),
                        )

                assertEquals(emptyList(), result)
            }

            test("skips branches where file is not available") { temporaryDirectory ->
                val main = Branch("main")
                val beta = Branch("beta")
                val betaRoot = temporaryDirectory.resolve("beta")
                val path = "app-metadata/en-US/title.txt"
                betaRoot.resolve(path).writeText("Beta")

                val result =
                    InputFileReader()
                        .readFiles(
                            relativePath = path,
                            branches = listOf(main, beta),
                            branchRoots =
                                mapOf(main to temporaryDirectory.resolve("main"), beta to betaRoot),
                        )

                assertEquals(listOf(beta), result.map { file -> file.sourceBranch })
                assertEquals(listOf("Beta"), result.map { file -> file.content })
            }
        }
    }
