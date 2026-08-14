# Key Bind Bundles Fabric

Key Bind Bundles Fabric is an unofficial Fabric port of
[Key Bind Bundles](https://github.com/MatyrobbrtMods/KeyBindBundles) by
Matyrobbrt. It is a client-side Minecraft mod for grouping multiple keybinds
behind a single shortcut.

Press a bundle key to trigger a bookmarked action, or hold the radial modifier
while pressing the bundle key to pick from the full bundle menu.

This fork targets Fabric on Minecraft 1.21.1. It is maintained separately from
the original NeoForge project and is not endorsed by or affiliated with
Matyrobbrt unless stated otherwise by the original project.

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

## Build

```powershell
.\gradlew.bat build
```

The built jar is written to `build/libs/`.

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
