package net.thunderbird.cli.l10n.sync.io.manifest

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import net.thunderbird.cli.l10n.sync.support.temporaryDirectoryFixture

val SyncManifestStoreTests by
    testSuite("SyncManifestStore") {
        temporaryDirectoryFixture().asParameterForEach {
            test("writes and reads sync manifest") { temporaryDirectory ->
                val manifest =
                    SyncManifest(
                        version = 1,
                        branches =
                            mapOf(
                                "release" to
                                    BranchInventory(
                                        files =
                                            mapOf(
                                                "feature/example/src/main/res/values/strings.xml" to
                                                    listOf("release_only", "shared")
                                            )
                                    )
                            ),
                    )

                SyncManifestStore.write(temporaryDirectory, manifest)

                assertEquals(manifest, SyncManifestStore.read(temporaryDirectory))
            }

            test("detects a changed manifest even when no output file changed") { temporaryDirectory
                ->
                val previous =
                    SyncManifest(
                        version = 1,
                        branches = mapOf("main" to BranchInventory(files = emptyMap())),
                    )
                val updated =
                    previous.copy(
                        branches =
                            mapOf(
                                "main" to
                                    BranchInventory(
                                        files =
                                            mapOf("module/res/values/strings.xml" to listOf("key"))
                                    )
                            )
                    )

                SyncManifestStore.write(temporaryDirectory, previous)

                assertTrue(SyncManifestStore.hasChanged(temporaryDirectory, updated))
                assertFalse(SyncManifestStore.hasChanged(temporaryDirectory, previous))
            }
        }
    }
