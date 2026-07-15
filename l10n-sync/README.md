# l10n-sync

`l10n-sync` syncs localization files between configured source repository branches and an l10n mirror.

## Commands

### `import`

Reads localization files from the configured branches, merges XML resource keys by branch order, and reports the changes
that would be written to the repository root. Conflicting key content is reported as a warning and the first configured
branch wins.

With `--apply`, the command writes `l10n-sync-manifest.json` with a per-branch file/key inventory. Commit this
manifest with imported localization files so later export tooling can split merged Weblate translations back to the
supported application branches. Dry-run reports that the manifest would be written without changing it.

```bash
./gradlew :l10n-sync:run --args="import"
```

Options:

- `--all` — import both source-language files and translations
- `--apply` — write files and clean stale files. Without this option, the command is a dry run.

### `export`

Exports localization files from a translations repository into the current source repository checkout.

```bash
./gradlew :l10n-sync:run --args="export --branch release --translations-repo ../tfa-l10n"
```

Options:

- `--branch` — source branch inventory to export
- `--translations-repo` — l10n/Weblate repository containing merged translations and `l10n-sync-manifest.json`
- `--apply` — write files to the current source repository checkout. Without this option, the command is a dry run.

The export command is currently parsed and documented; file export implementation is pending.

## Notes

- The tool expects a project config file named `l10n-config.json` by default.
- The source repository is configured as a Git clone URL.
- Source branches are configured in the project config.
- Files are discovered from configured import patterns. By default only source-language files are imported; `--all`
  imports source-language and translated files.
- XML resource files are merged by resource key across all configured branches.
- Source-language XML resources produce the branch inventory manifest and conflict warnings. Translated XML resources are
  merged by branch precedence without conflict warnings and are excluded from the manifest.
- XML resource output is written in alphabetical key order.
- XML comments directly preceding a resource entry are preserved with that entry.
- Non-XML files use the first available branch in configured branch order.
- Duplicate resource keys inside an XML file fail the import.
- If a key differs between configured branches, the command warns and keeps the value from the first configured branch.
- Stale cleanup preserves the source string resource file of the configured default linked component.
- The Mosaic UI requires an interactive terminal.
