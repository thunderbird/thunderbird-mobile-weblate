# Localization validation

Validates changed Android and Compose Multiplatform resources and localization compatibility across release branches.

## Validate resource changes

```bash
l10n-validation validate-resource-changes \
  --repository-root /path/to/repository \
  --base-ref origin/main \
  --head-ref HEAD
```

The command validates every changed XML file under Android `src/main/res/values*` and Compose `composeResources/values*`
directories. Both formats are checked for well-formed XML, plural structure, duplicate keys, and source-to-translation
compatibility for strings, plurals, and string arrays. Android resources accept Android-compatible formatter placeholders
without zero-padding and allow `xliff:g` markup. Compose resources require indexed `%<number>$s` or `%<number>$d`
placeholders and reject XLIFF.
Missing translations remain valid because runtime source fallback is intentional.

## Check release-branch compatibility

```bash
l10n-validation check-branch-compatibility \
  --repository-root /path/to/repository \
  --base-ref origin/main \
  --downstream-ref origin/beta \
  --downstream-ref origin/release
```

Use `--upstream-ref` instead when validating a release-train branch against its closest upstream branch. Use
`--allow-typo-fix` only for text corrections that do not change the resource structure.
