# Changelog

## 1.4.1 - Fabric 1.21.1

### Added

- Added a Cloth Config screen exposed through Mod Menu when installed.
- Added in-game editing for client config values that were previously only editable in the properties file.
- Added debounced item ID autocomplete for bundle entry icons, including keyboard navigation and a clickable icon picker.
- Added a client config option to keep bundle radial menus open as a screen until an entry is selected.
- Added bundle-aware keybind text fallback so unbound keybind prompts can show the first assigned bundle shortcut containing that key.
- Added Cobblemon compatibility for prompts that read Cobblemon's raw bound-key helper instead of Minecraft's normal keybind text.
- Added repository-wide UTF-8 and line-ending defaults through `.editorconfig` and `.gitattributes`.

### Changed

- Bundle edit screens now return to the Controls keybind screen when closed.
- Bundle icon search now uses a deterministic item registry ID search tree.
- Bundle release-triggered selections now hold the simulated key press across client ticks so release-driven keybinds can observe the press.
- Suppressed invalid key polling during bundle key selection to avoid log spam from unbound keys.
- Quieted invalid key polling compatibility diagnostics to debug logging.

### Fixed

- Fixed selecting keys for a bundle in the vanilla and Controlling keybind lists.
- Fixed bundle keybind rows showing raw translation keys instead of the bundle name.
- Fixed bundle deletion returning all the way to the main menu.
- Fixed deleted bundles remaining visible in the Controls keybind screen until reopening it.
- Fixed item icon autocomplete clicks sometimes selecting the previous or first suggestion instead of the hovered row.
- Fixed radial mouse clipping crashing when the cursor was kept inside the menu.
- Fixed Cobblemon's "Press Not Bound to battle" style prompts for bundled throw/battle keybinds.
- Fixed held bundle keys leaking repeat key presses into screens opened from a bundle entry.
- Fixed bundle overlays retriggering an entry when releasing the bundle key after that entry opened a screen.

## 1.4.0 - Fabric 1.21.1

Initial Fabric release for Minecraft 1.21.1.

### Added

- Fabric Loader, Fabric API, and Loom build support.
- Fabric mod metadata and access widener configuration.
- Client-side Fabric lifecycle, keybinding, networking, and HUD event wiring.
- Cloth Config screen support exposed through Mod Menu when installed.
- In-game bundle deletion button with confirmation.
- Save-on-change behavior for bundle edits, entry selection, deletion, reordering, and client shutdown.

### Changed

- Ported NeoForge registration, config, rendering, and input handling to Fabric-compatible APIs.
- Reworked keybinding registration so radial bundle key mappings are inserted and unregistered correctly.
- Updated bundle entry icons to store `ItemStack` data compatible with Minecraft 1.21.1.
- Updated Controlling compatibility for the Fabric keybind list.
- Moved client config from NeoForge TOML registration to a Fabric-side properties file.

### Fixed

- Fixed saved shortcut keys for bundles not being restored after restarting the game.
- Fixed corrupt or invalid bundle data crashing the client during startup.
- Fixed held bundle shortcut keys leaking into screens opened by bundle actions.
- Fixed radial mouse clipping using the cursor X coordinate for both axes.
- Fixed icon autocomplete suggestions staying visible after clearing the search field.
- Fixed registry-backed icon search list array conversion and indexed iteration behavior.
- Fixed crashes when opening the keybind menu on Fabric.
- Fixed the Edit and Select buttons not responding inside the vanilla and Controlling keybind screens.
- Fixed bundle changes not being written reliably after editing or closing the game.
- Fixed mouse clicks leaking through the radial menu while a bundle is open.

### Notes

- Controlling is optional but supported when installed.
- Client config is written to `config/keybindbundles-client.properties`.
- Bundle data is stored in `keybind_bundles.json` in the Minecraft game directory.
