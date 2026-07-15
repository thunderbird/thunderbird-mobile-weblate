package net.thunderbird.cli.l10n.sync.support

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertFalse
import kotlin.test.assertTrue

val PathGlobTests by
    testSuite("PathGlob") {
        test("matches recursive Android string resources") {
            assertTrue(
                "feature/example/src/main/res/values/strings.xml"
                    .matchesPathGlob("**/res/values/strings.xml")
            )
        }

        test("matches a single path segment wildcard") {
            assertTrue("app-metadata/en-US/title.txt".matchesPathGlob("app-metadata/*/title.txt"))
            assertFalse(
                "app-metadata/store/en-US/title.txt".matchesPathGlob("app-metadata/*/title.txt")
            )
        }

        test("matches single character wildcard") {
            assertTrue("values-de/strings.xml".matchesPathGlob("values-??/strings.xml"))
            assertFalse("values-de-rDE/strings.xml".matchesPathGlob("values-??/strings.xml"))
        }

        test("normalizes Windows separators") {
            assertTrue(
                "feature\\example\\src\\main\\res\\values\\strings.xml"
                    .matchesPathGlob("**/res/values/strings.xml")
            )
        }

        test("matches any configured pattern") {
            assertTrue(
                "feature/example/src/main/res/values-de/strings.xml"
                    .matchesAnyPathGlob(
                        listOf("**/res/values/strings.xml", "**/res/values-*/strings.xml")
                    )
            )
            assertFalse(
                "feature/example/src/main/res/drawable/icon.xml"
                    .matchesAnyPathGlob(
                        listOf("**/res/values/strings.xml", "**/res/values-*/strings.xml")
                    )
            )
        }
    }
