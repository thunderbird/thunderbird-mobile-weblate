package net.thunderbird.cli.l10n.sync.io.git

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals

val GitClientTests by
    testSuite("GitClient") {
        test("preserves slash-separated remote branch names") {
            val branches =
                parseRemoteBranches(
                    listOf("123456\trefs/heads/main", "789abc\trefs/heads/release/1.0")
                        .joinToString("\n")
                )

            assertEquals(setOf("main", "release/1.0"), branches)
        }
    }
