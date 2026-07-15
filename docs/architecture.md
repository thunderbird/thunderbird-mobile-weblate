# Architecture overview

The tools have two independent command-line applications that share project configuration and terminal rendering.
Commands own presentation and delegate work to small task classes. Tasks own the operation flow and use I/O classes for
Git, files, the sync manifest, and Weblate HTTP calls.

```mermaid
%%{init: {"theme": "base", "themeVariables": {"lineColor": "#767676", "textColor": "#767676", "primaryTextColor": "#767676", "secondaryTextColor": "#767676", "tertiaryTextColor": "#767676", "labelTextColor": "#767676", "actorLineColor": "#767676", "actorTextColor": "#767676", "signalColor": "#767676", "signalTextColor": "#767676"}}}%%
flowchart TB
    Config[l10n-config.json]
    Terminal[l10n-terminal]
    Source[Source repository branches]
    Mirror[L10n mirror<br/>merged resources and translations]
    Checkout[Target source checkout]
    Api[Weblate API]

    subgraph Sync[l10n-sync]
        SyncCli[CLI command]
        Import[Import task]
        Export[Export task]
        SyncCli --> Import
        SyncCli --> Export
    end

    subgraph Weblate[l10n-weblate]
        WeblateCli[CLI command]
        Discovery[Component discovery]
        WeblateCli --> Discovery
    end

    Config -->|configures| SyncCli
    Config -->|configures| WeblateCli
    SyncCli -->|renders to| Terminal
    WeblateCli -->|renders to| Terminal
    Import -->|fetches from| Source
    Import -->|writes merged files to| Mirror
    Export -->|reads from| Mirror
    Export -->|writes to| Checkout
    Discovery -->|scans| Mirror
    WeblateCli -->|manages| Api
```

## Import

Import creates one merged l10n mirror from the configured source branches. Resource keys are unioned by key ID: a key
that exists on only one branch is retained. When the same key has different content, the first configured branch wins
and the command reports every branch variant. Text files use the same first-branch selection and report a conflict when
their contents differ.

```mermaid
%%{init: {"theme": "base", "themeVariables": {"lineColor": "#767676", "textColor": "#767676", "primaryTextColor": "#767676", "secondaryTextColor": "#767676", "tertiaryTextColor": "#767676", "labelTextColor": "#767676", "actorLineColor": "#767676", "actorTextColor": "#767676", "signalColor": "#767676", "signalTextColor": "#767676"}}}%%
sequenceDiagram
    participant C as Import command
    participant T as Import task
    participant G as Git client
    participant M as File merger
    participant O as Output and cleanup

    C->>T: validateInput()
    T->>G: check configured branches
    G-->>T: available branches
    C->>T: readInput(all)
    T->>G: fetch configured branches in parallel
    G-->>T: branch files and contents
    C->>T: calculateChanges(input)
    T->>M: merge files by path and key
    M-->>T: merged files, resolutions, conflicts, manifest
    T-->>C: output files and changed files
    alt apply and changes exist
        C->>T: writeOutput(changes)
        T->>O: write files and manifest
    else no changes
        C-->>C: report skipped output write
    end
    C->>T: cleanup(changes, apply)
    T->>O: remove or report stale files
```

The import task exposes these operation boundaries:

1. `validateInput` verifies all configured branches before any import work continues.
2. `readInput` fetches and reads files.
3. `calculateChanges` merges internally and produces `ImportChanges` for reporting, writing, and cleanup.
4. `writeOutput` writes changed output files and the manifest only when there are changes.
5. `cleanup` removes stale mirror files only when their source no longer exists in every covered branch.

## Export

The l10n mirror is the local repository that holds the merged resources and translations managed through Weblate. It is
the import destination and the directory passed to export as `--l10n-repo`; it is distinct from both the tools checkout
and the target source checkout.

Export is driven by `l10n-sync-manifest.json`, produced by a prior applied import. The manifest records which source
keys belong to each branch. Export reads the mirror, selects that branch's keys from source and translation resource
files, and compares them with the target checkout before writing.

```mermaid
%%{init: {"theme": "base", "themeVariables": {"lineColor": "#767676", "textColor": "#767676", "primaryTextColor": "#767676", "secondaryTextColor": "#767676", "tertiaryTextColor": "#767676", "labelTextColor": "#767676", "actorLineColor": "#767676", "actorTextColor": "#767676", "signalColor": "#767676", "signalTextColor": "#767676"}}}%%
flowchart TD
    Start[Export command] --> Read[Read sync manifest]
    Read --> Validate{Requested branch exists?}
    Validate -- no --> Error[Show available branches]
    Validate -- yes --> Select[Select branch file and key inventory]
    Select --> Mirror[Read l10n mirror source and translations]
    Mirror --> Compare[Calculate changed output files]
    Compare --> Apply{--apply?}
    Apply -- no --> Plan[Report planned changes]
    Apply -- yes --> Write[Write target checkout]
```

## Weblate management

The Weblate CLI discovers Android and Compose resource components under the l10n mirror, excluding configured ignored
modules. It compares the local component set with Weblate and can list, create, update, or delete components. Network
operations are dry runs by default; `--apply` authorizes the corresponding API mutation.
