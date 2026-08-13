# Key Bind Bundles Fabric

Key Bind Bundles is a client-side Minecraft mod for grouping multiple keybinds
behind a single shortcut. Press a bundle key to trigger a bookmarked action, or
hold the radial modifier while pressing the bundle key to pick from the full
bundle menu.

This fork targets Fabric on Minecraft 1.21.1.

## Requirements

- Minecraft 1.21.1
- Fabric Loader
- Fabric API
- Java 21

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
