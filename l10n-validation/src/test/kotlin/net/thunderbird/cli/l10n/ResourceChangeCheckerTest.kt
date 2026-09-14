package net.thunderbird.cli.l10n

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import java.io.File
import kotlin.test.Test

class ResourceChangeCheckerTest {
    @Test
    fun `validation fixture resources are excluded from repository checks`() {
        val path =
            "l10n-validation/src/test/resources/fixtures/compose-invalid/head/feature/example/" +
                "src/commonMain/composeResources/values/strings.xml"

        assertThat(isComposeResourceFile(path)).isFalse()
        assertThat(isAndroidResourceFile(path)).isFalse()
    }

    @Test
    fun `malformed resource XML is reported`() {
        val result = checkFixture("malformed")

        assertThat(result.failures).hasSize(1)
        assertThat(result.failures.single()).contains("is not valid XML at head")
    }

    @Test
    fun `compose resources reject xliff and invalid placeholders`() {
        val result = checkFixture("compose-invalid")

        assertThat(result.failures).hasSize(10)
        assertThat(result.failures.joinToString("\n"))
            .contains("declares unsupported xliff namespace")
        assertThat(result.failures.joinToString("\n"))
            .contains("string:xliff_placeholder uses unsupported xliff markup")
        assertThat(result.failures.joinToString("\n"))
            .contains("string:unindexed_placeholder uses invalid placeholder %s")
        assertThat(result.failures.joinToString("\n"))
            .contains("string:unsupported_placeholder uses invalid placeholder %1\$f")
        assertThat(result.failures.joinToString("\n"))
            .contains("string:malformed_placeholder uses invalid placeholder %1s")
        assertThat(result.failures.joinToString("\n"))
            .contains("plurals:missing_other must define quantity other")
        assertThat(result.failures.joinToString("\n"))
            .contains("plurals:invalid_quantity uses invalid quantity sometimes")
        assertThat(result.failures.joinToString("\n"))
            .contains("plurals:missing_quantity has an item without quantity")
        assertThat(result.failures.joinToString("\n"))
            .contains("plurals:duplicate_quantity repeats quantity one")
        assertThat(result.failures.joinToString("\n"))
            .contains("string:duplicate_key is defined more than once")
    }

    @Test
    fun `translated compose resources reject xliff namespace aliases and invalid placeholders`() {
        val result = checkFixture("compose-translated-invalid")

        assertThat(result.filesChecked).isEqualTo(1)
        assertThat(result.failures).hasSize(3)
        assertThat(result.failures.joinToString("\n"))
            .contains("declares unsupported xliff namespace")
        assertThat(result.failures.joinToString("\n"))
            .contains("string:xliff_placeholder uses unsupported xliff markup")
        assertThat(result.failures.joinToString("\n"))
            .contains("string:unindexed_placeholder uses invalid placeholder %s")
    }

    @Test
    fun `translated compose resources must match source keys and placeholders`() {
        val result = checkFixture("compose-translation-compatibility")

        assertThat(result.failures).hasSize(5)
        assertThat(result.failures.joinToString("\n")).contains("string:extra has no source entry")
        assertThat(result.failures.joinToString("\n"))
            .contains("string:internal_name is not translatable")
        assertThat(result.failures.joinToString("\n"))
            .contains("string:existing uses placeholders [%2\$s]")
        assertThat(result.failures.joinToString("\n"))
            .contains("plurals:messages quantity few uses placeholders [%1\$d]")
        assertThat(result.failures.joinToString("\n"))
            .contains("string-array:labels item 2 uses placeholders [%2\$s]")
    }

    @Test
    fun `translated compose resources allow reordered placeholders and locale plural quantities`() {
        val result = checkFixture("compose-translation-valid")

        assertThat(result.failures).isEmpty()
    }

    @Test
    fun `compose validation includes arbitrarily named XML resource files`() {
        val result = checkFixture("compose-custom-file")

        assertThat(result.filesChecked).isEqualTo(1)
        assertThat(result.failures.single())
            .contains("string:invalid_placeholder uses invalid placeholder %s")
    }

    @Test
    fun `compose resources accept indexed string and decimal placeholders`() {
        val result = checkFixture("compose-valid")

        assertThat(result.failures).isEmpty()
    }

    @Test
    fun `Android resources reject invalid formatting and structure`() {
        val result = checkFixture("android-invalid")

        assertThat(result.filesChecked).isEqualTo(1)
        assertThat(result.failures.joinToString("\n"))
            .contains("string:invalid_placeholder uses invalid Android placeholder %1\$q")
        assertThat(result.failures.joinToString("\n"))
            .contains("string:invalid_flags uses invalid Android placeholder %1\$#d")
        assertThat(result.failures.joinToString("\n"))
            .contains("string:unsupported_zero_padding uses invalid Android placeholder %1\$02d")
        assertThat(result.failures.joinToString("\n"))
            .contains(
                "string:invalid_index uses invalid Android placeholder " +
                    "%999999999999999999999\$s"
            )
        assertThat(result.failures.joinToString("\n"))
            .contains("string:invalid_xliff uses invalid Android xliff markup")
        assertThat(result.failures.joinToString("\n"))
            .contains("string:missing_xliff_id uses invalid Android xliff markup")
        assertThat(result.failures.joinToString("\n"))
            .contains("string:duplicate_key is defined more than once")
        assertThat(result.failures.joinToString("\n"))
            .contains("plurals:missing_other must define quantity other")
        assertThat(result.failures.joinToString("\n"))
            .contains("plurals:invalid_quantity uses invalid quantity sometimes")
        assertThat(result.failures.joinToString("\n"))
            .contains("plurals:missing_quantity has an item without quantity")
        assertThat(result.failures.joinToString("\n"))
            .contains("plurals:duplicate_quantity repeats quantity one")
    }

    @Test
    fun `translated Android resources must match source keys and placeholders`() {
        val result = checkFixture("android-translation-invalid")

        assertThat(result.filesChecked).isEqualTo(1)
        assertThat(result.failures.joinToString("\n")).contains("string:extra has no source entry")
        assertThat(result.failures.joinToString("\n"))
            .contains("string:internal_name is not translatable")
        assertThat(result.failures.joinToString("\n"))
            .contains("string:existing uses Android placeholders [%1\$d, %2\$s]")
        assertThat(result.failures.joinToString("\n"))
            .contains("string:invalid_format uses invalid Android placeholder %1\$q")
        assertThat(result.failures.joinToString("\n"))
            .contains("plurals:messages quantity few uses Android placeholders [%1\$s]")
        assertThat(result.failures.joinToString("\n"))
            .contains("string-array:labels item 2 uses Android placeholders [%1\$d]")
    }

    @Test
    fun `Android resources allow indexed reordering xliff and unformatted percent signs`() {
        val result = checkFixture("android-valid")

        assertThat(result.filesChecked).isEqualTo(3)
        assertThat(result.failures).isEmpty()
    }

    @Test
    fun `source changes validate unchanged Android translations`() {
        val result = checkFixture("android-stale-after-source-change")

        assertThat(result.filesChecked).isEqualTo(2)
        assertThat(result.failures.single()).contains("string:removed has no source entry")
    }

    @Test
    fun `translated Android resource changes are validated`() {
        val result = checkFixture("translation-only")

        assertThat(result.filesChecked).isEqualTo(1)
        assertThat(result.failures).isEmpty()
    }

    private fun checkFixture(name: String): CompatibilityResult {
        val fixtureRoot =
            checkNotNull(javaClass.getResource("/fixtures/$name")) { "Missing fixture: $name" }
        val gitClient = FixtureGitClient(File(fixtureRoot.toURI()))
        val testSubject = ResourceChangeChecker(gitClient)
        return testSubject.check(baseRef = "base", headRef = "head")
    }
}
