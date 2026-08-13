# Changelog

## 1.4.0 - Fabric 1.21.1

Initial Fabric release for Minecraft 1.21.1.

### Added

- Fabric Loader, Fabric API, and Loom build support.
- Fabric mod metadata and access widener configuration.
- Client-side Fabric lifecycle, keybinding, networking, and HUD event wiring.
- In-game bundle deletion button with confirmation.
- Save-on-change behavior for bundle edits, entry selection, deletion, reordering, and client shutdown.

### Changed

- Ported NeoForge registration, config, rendering, and input handling to Fabric-compatible APIs.
- Reworked keybinding registration so radial bundle key mappings are inserted and unregistered correctly.
- Updated bundle entry icons to store `ItemStack` data compatible with Minecraft 1.21.1.
- Updated Controlling compatibility for the Fabric keybind list.
- Moved client config from NeoForge TOML registration to a Fabric-side properties file.

### Fixed

- Fixed crashes when opening the keybind menu on Fabric.
- Fixed the Edit and Select buttons not responding inside the vanilla and Controlling keybind screens.
- Fixed bundle changes not being written reliably after editing or closing the game.
- Fixed mouse clicks leaking through the radial menu while a bundle is open.

### Notes

- Controlling is optional but supported when installed.
- Client config is written to `config/keybindbundles-client.properties`.
- Bundle data is stored in `keybind_bundles.json` in the Minecraft game directory.
