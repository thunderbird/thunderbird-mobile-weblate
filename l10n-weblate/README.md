# l10n-weblate

This is a command line interface that inspects Weblate project components, lists local/Weblate
component status, and applies a "default" component configuration. It's intended for maintainers to
review component configuration consistency and, when appropriate, patch components to match the
component config.

## Usage

You need a Weblate API token (available from your Weblate account profile). A convenience wrapper script
is provided at `./scripts/weblate` which builds and runs the CLI.

The CLI uses a subcommand pattern: `weblate COMMAND [OPTIONS]...`

Available commands:
- `update`: Update locally discovered components with the standard configuration.
- `create`: Create missing components on Weblate based on local source string files.
- `list`: List Weblate components and local discovery status.
- `delete`: Delete a component from Weblate.

Basic examples:

```bash
# Dry-run using the default configuration
./scripts/weblate update --token YOUR_WEBLATE_TOKEN

# Apply changes to locally discovered components
./scripts/weblate update --token YOUR_WEBLATE_TOKEN --apply

# Preview missing component creation
./scripts/weblate create --token YOUR_WEBLATE_TOKEN

# List Weblate components and local discovery status
./scripts/weblate list --token YOUR_WEBLATE_TOKEN

# Preview deleting a component by slug
./scripts/weblate delete --token YOUR_WEBLATE_TOKEN --slug component-slug-to-delete

# Set log level to ALL
./scripts/weblate update --token YOUR_WEBLATE_TOKEN --log-level ALL
```

## Native executable

The build configures a native target for the current host. Build its release executable with the matching task:

```bash
# Apple Silicon macOS
./gradlew :l10n-weblate:linkReleaseExecutableMacosArm64

# x86-64 Linux
./gradlew :l10n-weblate:linkReleaseExecutableLinuxX64

# x86-64 Windows
./gradlew :l10n-weblate:linkReleaseExecutableMingwX64
```

The artifact is written below `l10n-weblate/build/bin/<target>/releaseExecutable/`. For example, on Apple Silicon:

```bash
./l10n-weblate/build/bin/macosArm64/releaseExecutable/l10n-weblate.kexe \
    list --token YOUR_WEBLATE_TOKEN
```

Run it from the l10n mirror directory containing `l10n-config.json` and `l10n-component-config.json`. Native targets
are built on their corresponding host; the project does not currently configure cross-compilation.

## Releases

Pushing a tag beginning with `v` runs the
[`Publish - Release`](../.github/workflows/publish-release.yml) workflow and creates a GitHub Release containing both
`l10n-weblate` and `l10n-sync` as:

- A platform-independent JVM distribution
- A Linux x86-64 native executable
- A macOS Arm64 native executable
- A Windows x86-64 native executable

For example:

```bash
git tag v1.0.0
git push origin v1.0.0
```

`Publish - Release` can also be started manually for an existing tag from the GitHub Actions page.

Each release asset has a keyless Sigstore bundle named `<asset>.sigstore.json`. Verify a downloaded asset with
[Cosign](https://docs.sigstore.dev/cosign/system_config/installation/):

```bash
cosign verify-blob \
    --bundle l10n-weblate-v1.0.0-macos-arm64.tar.gz.sigstore.json \
    --certificate-identity-regexp \
        'https://github.com/thunderbird/thunderbird-mobile-weblate/.github/workflows/publish-release.yml@refs/.*' \
    --certificate-oidc-issuer https://token.actions.githubusercontent.com \
    l10n-weblate-v1.0.0-macos-arm64.tar.gz
```

## Available options

- `--token`: Weblate API token (required).
- `--apply`: Apply changes to Weblate for mutating commands. Without this option commands run as dry runs.
- `--log-level`: Log level for the Weblate API client (`NONE`, `INFO`, `HEADERS`, `BODY`, `ALL`). Default: `NONE`.
- `--help`, `-h`: Show help.

## Defaults

- Project config: `./l10n-config.json`
- Component config: `./l10n-component-config.json`

## Safety notes

- Always run without `--apply` first to verify diffs before applying changes to the live Weblate instance.
- The `update` command only processes components discovered from source-language string files in the root mirror.
- The `list` command is read-only.
- `create --apply` and `delete --apply` use Mosaic-native confirmation prompts.
- Use `./scripts/weblate --help` to see all available commands and options.
