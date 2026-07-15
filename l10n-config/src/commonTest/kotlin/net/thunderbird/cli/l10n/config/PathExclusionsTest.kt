package net.thunderbird.cli.l10n.config

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertFalse
import kotlin.test.assertTrue

val PathExclusionsTests by
    testSuite("PathExclusions") {
        test("matches excluded paths by glob or path fragment") {
            val patterns = listOf("app-metadata/**/changelogs/**", "openpgp-api")

            assertTrue(
                "app-metadata/net.thunderbird.android/en-US/changelogs/1.txt"
                    .isExcludedPath(patterns)
            )
            assertTrue(
                "plugins/openpgp-api-lib/openpgp-api/src/main/res/values/strings.xml"
                    .isExcludedPath(patterns)
            )
            assertFalse(
                "feature/account/setup/src/main/res/values/strings.xml".isExcludedPath(patterns)
            )
        }

        test("matches single star within one path segment") {
            val patterns = listOf("app-metadata/*/en-US/*.txt")

            assertTrue(
                "app-metadata/net.thunderbird.android/en-US/title.txt".isExcludedPath(patterns)
            )
            assertFalse(
                "app-metadata/net.thunderbird.android/nightly/en-US/title.txt"
                    .isExcludedPath(patterns)
            )
        }
    }
