package net.thunderbird.cli.l10n

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import java.io.File
import kotlin.test.Test

class BranchCompatibilityCheckerTest {
    @Test
    fun `main may remove a key retained downstream`() {
        val result = checkFixture("main-removal", downstreamRefs = listOf("downstream"))

        assertThat(result.filesChecked).isEqualTo(1)
        assertThat(result.failures).isEmpty()
    }

    @Test
    fun `main may add a replacement key not present downstream`() {
        val result = checkFixture("new-key", downstreamRefs = listOf("downstream"))

        assertThat(result.failures).isEmpty()
    }

    @Test
    fun `main rejects incompatible shared resources`() {
        val result = checkFixture("main-incompatible", downstreamRefs = listOf("downstream"))

        assertThat(result.failures).hasSize(3)
        assertThat(result.failures.joinToString("\n"))
            .contains("changed string:placeholder incompatibly")
        assertThat(result.failures.joinToString("\n"))
            .contains("changed plurals:messages incompatibly")
        assertThat(result.failures.joinToString("\n"))
            .contains("changed existing source text for string:label")
    }

    @Test
    fun `typo flag allows text-only shared resource changes`() {
        val rejected = checkFixture("typo-fix", downstreamRefs = listOf("downstream"))
        val accepted =
            checkFixture(
                "typo-fix",
                downstreamRefs = listOf("downstream"),
                allowTypoFix = true,
            )

        assertThat(rejected.failures).hasSize(1)
        assertThat(accepted.failures).isEmpty()
    }

    @Test
    fun `release-train branch accepts changes matching upstream`() {
        val result = checkFixture("upstream-match", upstreamRef = "upstream")

        assertThat(result.failures).isEmpty()
    }

    @Test
    fun `release-train branch changes must match upstream`() {
        val result = checkFixture("upstream-divergence", upstreamRef = "upstream")

        assertThat(result.failures).hasSize(3)
        assertThat(result.failures.joinToString("\n")).contains("removed string:removed")
        assertThat(result.failures.joinToString("\n")).contains("string:changed differs")
        assertThat(result.failures.joinToString("\n")).contains("added string:added")
    }

    @Test
    fun `main may remove store metadata retained downstream`() {
        val result = checkFixture("metadata-removal", downstreamRefs = listOf("downstream"))

        assertThat(result.filesChecked).isEqualTo(1)
        assertThat(result.failures).isEmpty()
    }

    @Test
    fun `release-train store metadata changes must match upstream`() {
        val result = checkFixture("metadata-divergence", upstreamRef = "upstream")

        assertThat(result.failures).hasSize(1)
        assertThat(result.failures.single()).contains("source store listing text differs")
    }

    @Test
    fun `malformed resource XML is reported`() {
        val result = checkFixture("malformed", downstreamRefs = listOf("downstream"))

        assertThat(result.failures).hasSize(1)
        assertThat(result.failures.single()).contains("is not valid XML at head")
    }

    @Test
    fun `compose resources reject xliff and invalid placeholders`() {
        val result = checkFixture("compose-invalid", downstreamRefs = listOf("downstream"))

        assertThat(result.failures).hasSize(7)
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
    }

    @Test
    fun `translated compose resources reject xliff namespace aliases and invalid placeholders`() {
        val result =
            checkFixture("compose-translated-invalid", downstreamRefs = listOf("downstream"))

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
        val result =
            checkFixture("compose-translation-compatibility", downstreamRefs = listOf("downstream"))

        assertThat(result.failures).hasSize(4)
        assertThat(result.failures.joinToString("\n")).contains("string:extra has no source entry")
        assertThat(result.failures.joinToString("\n"))
            .contains("string:internal_name is not translatable")
        assertThat(result.failures.joinToString("\n"))
            .contains("string:existing uses placeholders [%2\$s]")
        assertThat(result.failures.joinToString("\n"))
            .contains("plurals:messages quantity few uses placeholders [%1\$d]")
    }

    @Test
    fun `translated compose resources allow reordered placeholders and locale plural quantities`() {
        val result =
            checkFixture("compose-translation-valid", downstreamRefs = listOf("downstream"))

        assertThat(result.failures).isEmpty()
    }

    @Test
    fun `compose resources accept indexed string and decimal placeholders`() {
        val result = checkFixture("compose-valid", downstreamRefs = listOf("downstream"))

        assertThat(result.failures).isEmpty()
    }

    @Test
    fun `translated Android resource changes are ignored`() {
        val result = checkFixture("translation-only", downstreamRefs = listOf("downstream"))

        assertThat(result.filesChecked).isEqualTo(0)
        assertThat(result.failures).isEmpty()
    }

    private fun checkFixture(
        name: String,
        upstreamRef: String? = null,
        downstreamRefs: List<String> = emptyList(),
        allowTypoFix: Boolean = false,
    ): CompatibilityResult {
        val fixtureRoot =
            checkNotNull(javaClass.getResource("/fixtures/$name")) { "Missing fixture: $name" }
        val gitClient = FixtureGitClient(File(fixtureRoot.toURI()))
        val testSubject = BranchCompatibilityChecker(gitClient)
        return testSubject.check(
            CompatibilityOptions(
                baseRef = "base",
                headRef = "head",
                upstreamRef = upstreamRef,
                downstreamRefs = downstreamRefs,
                allowTypoFix = allowTypoFix,
            )
        )
    }
}

private class FixtureGitClient(private val root: File) : GitClient {
    override fun changedFiles(baseRef: String, headRef: String): List<String> {
        val baseFiles = filesAt(baseRef)
        val headFiles = filesAt(headRef)
        return (baseFiles.keys + headFiles.keys)
            .filter { path -> baseFiles[path] != headFiles[path] }
            .sorted()
    }

    override fun readFile(ref: String, path: String): String? = filesAt(ref)[path]

    private fun filesAt(ref: String): Map<String, String> {
        val refRoot = root.resolve(ref)
        if (!refRoot.exists()) return emptyMap()
        return refRoot.walkTopDown().filter(File::isFile).associate { file ->
            file.relativeTo(refRoot).invariantSeparatorsPath to file.readText()
        }
    }
}
