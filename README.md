# thunderbird-mobile-weblate

CLI tools and automation for managing Thunderbird Mobile localization workflows with Weblate.

## Tools

- `l10n-import`: imports localization source files from source repository branches into an l10n mirror.
- `l10n-weblate`: discovers local Android/Compose string components and manages their Weblate component configuration.

## Development

Run all checks from this repository root:

```bash
./gradlew :l10n-import:check :l10n-weblate:check
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

Each l10n repository can provide `l10n-tools.json` at its root. See:

- `examples/thunderbird-android-l10n.json`
- `examples/thunderbird-mobile-components-l10n.json`

The config owns source repository, branch train, import patterns, cleanup roots, Weblate API/project settings, component
repository, linked component, discovery exclusions, and component format rules.

## Current Limitations

The tools are buildable in this shared repository and can read project config. The remaining work is to validate the
configured import/discovery rules against each concrete l10n repository before publishing binaries.
