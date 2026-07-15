package net.thunderbird.cli.l10n.sync.task

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import net.thunderbird.cli.l10n.config.Branch
import net.thunderbird.cli.l10n.sync.model.L10nFileType
import net.thunderbird.cli.l10n.sync.model.SourceKey
import net.thunderbird.cli.l10n.sync.model.SourceResourceFile

val BranchDifferencesTests by
    testSuite("BranchDifferences") {
        test("reports files and keys available in exactly one branch") {
            val report =
                BranchDifferences.calculate(
                    listOf(
                        resourceFile(
                            "main",
                            "module/src/main/res/values/strings.xml",
                            "shared",
                            "main_only",
                        ),
                        resourceFile(
                            "beta",
                            "module/src/main/res/values/strings.xml",
                            "shared",
                            "beta_only",
                        ),
                        resourceFile("beta", "other/src/main/res/values/strings.xml", "other_key"),
                    )
                )

            assertEquals(
                listOf(
                    BranchFileDifference(Branch("beta"), "other/src/main/res/values/strings.xml")
                ),
                report.uniqueFiles,
            )
            assertEquals(
                listOf(
                    BranchKeyDifference(
                        Branch("beta"),
                        "module/src/main/res/values/strings.xml",
                        "beta_only",
                    ),
                    BranchKeyDifference(
                        Branch("beta"),
                        "other/src/main/res/values/strings.xml",
                        "other_key",
                    ),
                    BranchKeyDifference(
                        Branch("main"),
                        "module/src/main/res/values/strings.xml",
                        "main_only",
                    ),
                ),
                report.uniqueKeys,
            )
        }
    }

private fun resourceFile(branch: String, path: String, vararg keys: String): SourceResourceFile =
    SourceResourceFile(
        relativePath = path,
        branch = Branch(branch),
        type = L10nFileType.ANDROID_RESOURCE,
        keys = keys.map { key -> SourceKey(key, "<string name=\"$key\" />", emptyList()) },
    )
