# l10n-import

`l10n-import` imports localization source files from configured source repository branches and validates source keys
across release branches.

## Commands

### `import`

Imports localization files from `main`, `beta`, and `release`, then writes the consolidated files into the repository root.

```bash
./gradlew :l10n-import:run --args="import"
```

Options:

- `--all` — import both source-language files and translations
- `--source-repo=<owner/repo>` — source repository to clone
- `--branches=<branches>` — comma-separated branches to import
- `--config=<path>` — project config file

### `validate`

Collects source string keys from `main`, `beta`, and `release`, merges them per file, and fails if the same key has different content across branches.

```bash
./gradlew :l10n-import:run --args="validate"
```

Options:

- `--source-repo=<owner/repo>` — source repository to clone
- `--branches=<branches>` — comma-separated branches to validate
- `--config=<path>` — project config file
- `--output-dir=<path>` — where validated files are written (default: repository root)

## Notes

- The tool expects a project config file named `l10n-tools.json` by default.
- Source files are discovered from configured import patterns.
- Duplicate string keys inside a source file fail the import.
- If a key differs between `main`, `beta`, and `release`, that is treated as a source-repository violation and the command fails.
- The Mosaic UI is used when the CLI runs in a real terminal; non-interactive runs fall back to plain console output.
