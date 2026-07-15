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
