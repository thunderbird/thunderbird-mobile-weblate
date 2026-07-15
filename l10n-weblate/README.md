# l10n-weblate

This is a command line interface that inspects Weblate project components and applies a
"default" component configuration. It's intended for maintainers to review component configuration
consistency and, when appropriate, patch components to match the component config.

## Usage

You need a Weblate API token (available from your Weblate account profile). A convenience wrapper script
is provided at `./scripts/weblate` which builds and runs the CLI.

The CLI uses a subcommand pattern: `weblate [OPTIONS] COMMAND [ARGS]...`

Available commands:
- `update`: Update locally discovered components with the standard configuration.
- `create`: Create missing components on Weblate based on local source string files.
- `delete`: Delete a component from Weblate.

Basic examples:

```bash
# Dry-run using the default configuration
./scripts/weblate --token YOUR_WEBLATE_TOKEN --dry-run update

# Apply changes to locally discovered components
./scripts/weblate --token YOUR_WEBLATE_TOKEN update

# Create missing components
./scripts/weblate --token YOUR_WEBLATE_TOKEN create

# Delete a component by slug
./scripts/weblate --token YOUR_WEBLATE_TOKEN delete --slug component-slug-to-delete

# Use a custom component config and set log level to ALL
./scripts/weblate --token YOUR_WEBLATE_TOKEN --component-config-file ./l10n-weblate/default-component-config.json --log-level ALL --dry-run update
```

## Available options

- `--token`: Weblate API token (required).
- `--config`: Project config file (default: `l10n-tools.json`).
- `--component-config-file`: Path to component config JSON.
- `--dry-run`: Dry run the command without making any changes.
- `--log-level`: Log level for the Weblate API client (`NONE`, `INFO`, `HEADERS`, `BODY`, `ALL`). Default: `NONE`.

## Defaults

- Project config: `./l10n-tools.json`
- Component config: configured by `weblate.componentConfigFile`, or the bundled default when run through the wrapper.

## Safety notes

- Always run with `--dry-run` first to verify diffs before applying changes to the live Weblate instance.
- The `update` command only processes components discovered from source-language string files in the root mirror.
- Use `./scripts/weblate --help` to see all available commands and options.
