# Thunderbird Mobile Weblate

CLI tools and automation for managing Thunderbird Mobile localization workflows with Weblate.

## Tools

- `l10n-import`: imports localization source files from source repository branches into an l10n mirror.
- `l10n-weblate`: discovers local Android/Compose string components and manages their Weblate component configuration.

## Development

Run all checks from this repository root:

```bash
./gradlew :l10n-tools-config:check :l10n-import:check :l10n-weblate:check
```

Format Kotlin and Gradle Kotlin DSL sources:

```bash
./gradlew :l10n-tools-config:ktfmtFormat :l10n-import:ktfmtFormat :l10n-weblate:ktfmtFormat
```

Generate the aggregate Kover XML coverage report:

```bash
./gradlew koverXmlReport
```

Build runnable distributions:

```bash
./gradlew :l10n-import:installDist :l10n-weblate:installDist
```

## Submodule Usage

Concrete l10n repositories can add this repository as a submodule and call the wrapper scripts from their own root:

```bash
./path/to/thunderbird-mobile-weblate/scripts/import-from-source import
./path/to/thunderbird-mobile-weblate/scripts/weblate --token WEBLATE_TOKEN --dry-run update
```

The wrappers build the relevant CLI in this repository and run it with the caller's current working directory, so
component discovery and imports operate on the concrete l10n repository.

## Project Config

Each l10n repository must provide `l10n-tools.json` at its root.

The config owns source repository, branch train, import patterns and exclusions, Weblate API/project settings, component
repository, linked component, and component format rules. Temporary files always live under `.tmp/` in the l10n
repository root.

Templates for new l10n repositories are available in `templates/`. Replace placeholders named like
`__SOURCE_REPOSITORY__`; leave Weblate template variables such as `{{branch}}` and `{{filename}}` intact.
