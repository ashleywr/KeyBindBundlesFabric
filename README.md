# Key Bind Bundles Fabric

Key Bind Bundles Fabric is an unofficial Fabric port of
[Key Bind Bundles](https://github.com/MatyrobbrtMods/KeyBindBundles) by
Matyrobbrt. It is a client-side Minecraft mod for grouping multiple keybinds
behind a single shortcut.

Press a bundle key to trigger a bookmarked action, or hold the radial modifier
while pressing the bundle key to pick from the full bundle menu.

This fork targets Fabric on Minecraft 1.21.1. It is maintained separately from
the original NeoForge project, is not a multiloader port, and is not endorsed by
or affiliated with Matyrobbrt unless stated otherwise by the original project.

## Usage

Open Minecraft's Controls screen and use the KeyBind Bundles controls there to
create, edit, delete, and bind bundles. Bundle shortcuts are assigned like any
other Minecraft keybind.

Each bundle entry points at one existing keybind. Entries can have a custom
label and an item icon. The icon field accepts item IDs such as
`minecraft:compass`, and the edit screen includes debounced autocomplete with
keyboard navigation.

By default, holding a bundle key opens the radial overlay and releasing the key
can trigger the highlighted entry when that config option is enabled. Sticky
selection mode keeps the radial menu open as a screen until an entry is clicked.

## Requirements

- Minecraft 1.21.1
- Fabric Loader
- Fabric API
- Cloth Config API
- Java 21

Mod Menu is optional. When installed, KeyBind Bundles exposes its Cloth Config
screen from the Mods menu so client config values can be changed in game.

Controlling is optional. When installed, the bundle edit and delete controls are
shown in Controlling's keybind list as well as vanilla's controls screen.

## Configuration

Client config is stored in `config/keybindbundles-client.properties` and can be
edited in game through Mod Menu when Mod Menu is installed.

| Option | Default | Description |
| --- | --- | --- |
| `clipMouseToMenu` | `false` | Keeps the mouse cursor inside the radial menu while a bundle is open. |
| `triggerKeymappingOnRelease` | `false` | Triggers the highlighted entry when the bundle key is released. |
| `ignoreInvalidKeyChecks` | `false` | Returns `false` for invalid key polling instead of letting noisy compatibility logs appear. |
| `stickyBundleSelection` | `false` | Opens bundles as a screen that stays open until an entry is selected. |

## Compatibility

KeyBind Bundles works by letting the bundle own the physical shortcut while the
entries inside the bundle can remain unbound in Minecraft's normal Controls
screen. That avoids ordinary keybind conflicts, but some mods render prompts by
reading the raw bound key instead of Minecraft's translated keybind text.

The Fabric port includes compatibility for:

- Vanilla Controls screen
- Controlling's keybind screen
- Searchables autofocus behavior when returning from bundle edit screens
- Cobblemon prompts that read `CurrentKeyAccessorKt.boundKey(...)`

If another mod still displays "Not Bound" for a key that is inside a bundle, it
is probably using a similar raw-key path and may need a small compatibility
hook.

## Build

```powershell
.\gradlew.bat build
```

The built jar is written to `build/libs/`.

## Release

Releases are published through the GitHub Actions `Release` workflow. The
workflow builds the Fabric jar, publishes to Modrinth and CurseForge when the
required credentials are configured, then creates or updates the GitHub release
for the matching `v<mod_version>` tag.

Repository configuration required for publishing:

- Secret `MODRINTH_TOKEN`
- Variable `MODRINTH_PROJECT_ID`
- Secret `CURSEFORGE_TOKEN`
- Variable `CURSEFORGE_PROJECT_ID`

The Modrinth and CurseForge projects must be created and approved in those
services before the first upload can succeed.

## Files

- Client config: `config/keybindbundles-client.properties`
- Bundle data: `keybind_bundles.json` in the Minecraft game directory
- Key assignments: Minecraft's normal `options.txt`

Bundle definitions are intentionally separate from key assignments. This keeps
the bundle contents portable while letting players rebind the bundle shortcut in
the normal Controls screen.

## Credits

- Original mod: [MatyrobbrtMods/KeyBindBundles](https://github.com/MatyrobbrtMods/KeyBindBundles)
- Original author: Matyrobbrt
- Fabric port: [ashleywr/KeyBindBundlesFabric](https://github.com/ashleywr/KeyBindBundlesFabric)

This project is distributed under the MIT License. The original copyright notice
is preserved in `LICENSE`.
