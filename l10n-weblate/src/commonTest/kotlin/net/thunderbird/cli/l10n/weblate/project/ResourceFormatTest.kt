package net.thunderbird.cli.l10n.weblate.project

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import net.thunderbird.cli.l10n.config.ResourceKind

val ResourceFormatTests by
    testSuite("ResourceFormat") {
        test("returns Android resource format") {
            val format = ResourceFormat.forType(ResourceKind.ANDROID)

            assertEquals(ResourceKind.ANDROID, format.type)
            assertEquals("/src/main/res/values/strings.xml", format.sourceSuffix)
            assertEquals("/src/main/res/values-*/strings.xml", format.fileMaskSuffix)
            assertEquals("aresource", format.fileFormat)
        }

        test("returns Compose resource format") {
            val format = ResourceFormat.forType(ResourceKind.COMPOSE)

            assertEquals(ResourceKind.COMPOSE, format.type)
            assertEquals("/src/commonMain/composeResources/values/strings.xml", format.sourceSuffix)
            assertEquals(
                "/src/commonMain/composeResources/values-*/strings.xml",
                format.fileMaskSuffix,
            )
            assertEquals("cmp-resource", format.fileFormat)
        }
    }
