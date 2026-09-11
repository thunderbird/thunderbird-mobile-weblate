# Localization CLI

This command-line tool validates localization source compatibility across the supported release train:

```text
main -> beta -> release
```

## Branch compatibility

Run the check against downstream branches when changing `main`:

```bash
./scripts/validation check-branch-compatibility \
  --base-ref origin/main \
  --downstream-ref origin/beta \
  --downstream-ref origin/release
```

Run it against the closest upstream branch when changing `beta` or `release`:

```bash
./scripts/validation check-branch-compatibility \
  --base-ref origin/beta \
  --upstream-ref origin/main
```

Use `--allow-typo-fix` only for text corrections that do not change placeholders or plural quantities. Removing a key
from `main` is valid while the l10n branch manifest retains it for `beta` or `release`.

The check also validates changed Compose Multiplatform resources, including translated locale files. They must not
declare the `xliff` namespace or contain `xliff` markup, and placeholders must use the indexed `%<number>$s` or
`%<number>$d` syntax supported by Compose
Multiplatform resources. Plurals must define `other` and may only use the `zero`, `one`, `two`, `few`, `many`, and
`other` quantities. Translations must only contain translatable source keys and must preserve the source placeholder
arguments in strings and each plural quantity.
