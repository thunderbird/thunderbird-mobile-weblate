# Localization validation

Validates changed Compose Multiplatform resources and localization compatibility across release branches.

## Validate resource changes

```bash
l10n-validation validate-resource-changes \
  --repository-root /path/to/repository \
  --base-ref origin/main \
  --head-ref HEAD
```

The command validates every changed XML file in a Compose `values` directory. It checks supported indexed placeholders,
XLIFF exclusion, plural structure, duplicate keys, and source-to-translation compatibility for strings, plurals, and
string arrays.

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
