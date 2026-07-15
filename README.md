# Thunderbird Mobile Weblate

CLI tools and automation for managing Thunderbird Mobile localization workflows with Weblate.

## Tools

- `l10n-sync`: syncs localization files between source repository branches and an l10n mirror.
- `l10n-weblate`: discovers local Android/Compose string components and manages their Weblate component configuration.

## Using the tools

The wrapper scripts build the CLI if needed. Import and Weblate commands use the current directory as the l10n mirror
root. Export uses the current directory as the target source checkout.

### Repositories and directories

The tools work with three distinct locations:

| Location | Purpose                                                                                                                                                                                      | Used by |
| --- |----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------| --- |
| Tools checkout | This repository, containing the Gradle project and `scripts/sync` / `scripts/weblate`. It can be a submodule in the l10n mirror.                                                             | Builds and starts the CLIs. |
| L10n mirror | The local repository holding the merged source resource files and their translations. It owns `l10n-config.json`, receives imports, and is the repository passed as `--l10n-repo` to export. | Import, Weblate, export input. |
| Source checkout | A checkout of one source branch, such as `release`.                                                                                                                                          | Export output. |

The l10n mirror is the handoff point between the tools and Weblate: import updates it from all configured source
branches, Weblate manages its components and translations, and export copies a selected branch's keys from it back to a
source checkout. It is not the temporary source-branch clones under `.tmp/`.

### Setup

Create `l10n-config.json` in the l10n mirror root. It defines the source repository and branches, file patterns to
import, Weblate settings, and optional ignored modules. Start with
`templates/l10n-tools.json.template`, replace the `__…__` placeholders, and keep Weblate placeholders such as
`{{branch}}` and `{{filename}}` unchanged.

`ignoredModules` uses module paths such as `components/ui/catalog`. Files beneath an ignored module are excluded from
import, stale-file cleanup, and Weblate discovery.

### Sync

All sync commands are dry runs unless `--apply` is present.

```bash
# From the l10n mirror root, show the merged import and cleanup plan.
cd ../l10n-mirror

./path/to/thunderbird-mobile-weblate/scripts/sync import

# Import source-language files and apply the result.
./path/to/thunderbird-mobile-weblate/scripts/sync import --apply

# Include translation files in the import.
./path/to/thunderbird-mobile-weblate/scripts/sync import --all --apply

# From a checkout of the target source branch, preview an export.
cd ../thunderbird-android-release
../thunderbird-mobile-weblate/scripts/sync export \
  --branch release --l10n-repo ../l10n-mirror

# Apply the planned export to that checkout.
../thunderbird-mobile-weblate/scripts/sync export \
  --branch release --l10n-repo ../l10n-mirror --apply
```

Import fetches every configured source branch, merges source resource keys, and reports branch variants that disagree.
Unique keys from every branch are included. The sync manifest is written to record the applied import; it is
then used by export to select the keys belonging to a branch.

Export requires an existing `l10n-sync-manifest.json` in `--l10n-repo`. Run an applied import first to create or update
that manifest. The `--branch` value must be present in it, and the current directory must be either that branch or a
branch created from it. Export writes the branch's recorded source keys and matching translations from the l10n mirror
into that checkout.

### Weblate

Weblate commands require an API token. Use a dry run first, then add `--apply` to make the requested remote changes.

```bash
# From the l10n mirror root.
cd ../l10n-mirror

# Compare locally discovered components with Weblate.
./path/to/thunderbird-mobile-weblate/scripts/weblate list --token "$WEBLATE_TOKEN"

# Update existing Weblate components from local configuration.
./path/to/thunderbird-mobile-weblate/scripts/weblate update --token "$WEBLATE_TOKEN" --apply

# Create missing components.
./path/to/thunderbird-mobile-weblate/scripts/weblate create --token "$WEBLATE_TOKEN" --apply

# Delete one component.
./path/to/thunderbird-mobile-weblate/scripts/weblate delete \
  --token "$WEBLATE_TOKEN" --slug component-slug --apply
```

Use `sync help`, `sync import --help`, `sync export --help`, or `weblate help` for command-line help.

See [the architectural overview](docs/architecture.md) for the command and data flows.

## Development

Run all checks from this repository root:

```bash
./gradlew check
```

Format Kotlin and Gradle Kotlin DSL sources:

```bash
./gradlew ktfmtFormat
```

Generate the aggregate Kover XML coverage report:

```bash
./gradlew koverXmlReport
```

Build runnable distributions:

```bash
./gradlew :l10n-sync:installDist :l10n-weblate:installDist
```
