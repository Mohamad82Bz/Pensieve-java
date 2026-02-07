# Pensieve

Pensieve is a Minecraft server plugin that records player/entity activity tick-by-tick and replays it using NPCs and synthetic entities.
It captures movement, combat, item use, block changes, and interactions, then plays them back with timeline controls (pause, resume, skip, rewind, speed).

## What This Project Is

Pensieve is a Spigot/Paper plugin (Java 17) built around two core runtime systems:

- `Recorder`: captures gameplay into a `RecordContainer`
- `Replayer`: reconstructs that recording in-world with controllable playback

The plugin stores recordings as `.pensieve` files under the plugin data folder and provides command-driven management for recorder/replayer lifecycles.

## Feature Overview

- Records players and supported non-player entities on a per-tick timeline
- Captures block placements, block breaks, and block-state interactions (including door/bed handling)
- Captures combat context (normal, critical, sprint, projectile, burn)
- Captures item and action context (swing, eating/drinking, projectile throws, trident/fishing/firework behavior, crossbow charge/shoot states)
- Captures equipment, body arrows, potion effect color, pose flags (sneak/sprint/swim/glide/invisible/glow/burn)
- Replays through NPC/entity abstractions with sound and particle effects
- Playback controls (pause/resume, skip, rewind, speed presets: `x025`, `x050`, `x1`, `x2`, `x5`)
- PlaceholderAPI expansion support
- Public API and Bukkit events for integrations

## Supported Tracked Entities

Current recorder entity handling includes:

- Dropped items
- Area effect clouds
- Throwables represented as projectile records (splash potion, experience bottle, snowball, ender pearl, egg)
- Arrow and spectral arrow
- Trident
- Fishing hook
- Firework rocket

## How It Works

1. A recorder is created and populated with players/entities.
2. On `start`, a tick task snapshots relevant deltas into `RecordTick` objects.
3. On `stop`, all collected tracks are bundled into `RecordContainer`.
4. `PensieveGsonSerializer` writes that data to `.pensieve`.
5. A replayer loads the container, precomputes "last known non-null tick states", then spawns and updates NPCs/entities per tick.

## Commands

Main command from `plugin.yml`: `pensieve`  
Aliases: `pensieve-noreply`, `pensieve-panel`  
Debug command: `record` (temporary/dev command in current code)

### Recorder Commands

- `/pensieve recorder create <name> [x y z]`
- `/pensieve recorder restart <name>`
- `/pensieve recorder delete <name>`
- `/pensieve recorder addplayer <name> <player>`
- `/pensieve recorder removeplayer <name> <player>`
- `/pensieve recorder start <name>`
- `/pensieve recorder stop <name>`
- `/pensieve recorder save <name> [file]`
- `/pensieve recorder list`
- `/pensieve recorder <name>` (opens panel UI in chat)

### Replayer Commands

- `/pensieve replayer load <file> <name> [x y z]`
- `/pensieve replayer start <name>`
- `/pensieve replayer pause <name>`
- `/pensieve replayer resume <name>`
- `/pensieve replayer stop <name>`
- `/pensieve replayer skip <name> <ticks>`
- `/pensieve replayer rewind <name> <ticks>`
- `/pensieve replayer speed <name> <x025|x050|x1|x2|x5>`
- `/pensieve replayer delete <name>`
- `/pensieve replayer list`
- `/pensieve replayer <name>` (opens panel UI in chat)

## PlaceholderAPI

If PlaceholderAPI is installed, Pensieve registers `%pensieve_*%` placeholders:

- `%pensieve_recorder_exist_<recorderName>%`
- `%pensieve_recorder_<recorderName>_contain_player_<playerName>%`
- `%pensieve_recorder_any_contain_player_<playerName>%`
- `%pensieve_replayer_exist_<replayerName>%`
- `%pensieve_replayer_progress_<replayerName>%`
- `%pensieve_replayer_formatted_progress_<replayerName>%`
- `%pensieve_replayer_max_progress_<replayerName>%`
- `%pensieve_replayer_speed_<replayerName>%`

## Developer API

Static access:

- `PensieveAPI.getPlayerRecorders(Player)`
- `PensieveAPI.getEntityRecorders(Entity)`
- `PensieveAPI.getSerializer()`

Main event hooks include:

- `PensieveRecorderStartEvent`
- `PensieveRecorderStopEvent`
- `PensievePlayerAddEvent`
- `PensievePlayerRemoveEvent`
- `PensieveEntityAddEvent`
- `PensieveEntityRemoveEvent`
- `PensieveRecorderPlayerTickEvent`
- `PensieveRecorderEntityTickEvent`
- `PensieveRecorderRawTickEvent`
- `PensieveReplayerPreTickEvent`
- `PensieveReplayerAddEntityEvent`
- `PensieveNPCMoveAndLookEvent`
- `PensieveNPCLookEvent`

## Build And Run

### Requirements

- Java 17
- Gradle wrapper (`./gradlew`)

Build:

```bash
./gradlew shadowJar
```

Output jar is produced by `shadowJar` with relocated/shaded dependencies.

Local run task in this project is configured as:

- Minecraft version `1.20.1`
- Server jar expected at `run/purpur-1.20.1-2056.jar`

## Project Structure

- `src/main/java/me/mohamad82/pensieve/Pensieve.java`: plugin bootstrap
- `.../commands`: command executor and tab completion
- `.../recording`: recorder runtime, listeners, record model
- `.../replaying`: replay runtime, timeline control, block modifications, UI
- `.../serializer`: Gson-based save/load (`.pensieve`)
- `.../api`: public API and events
- `src/main/resources/plugin.yml`: plugin metadata/commands

## Current Notes And Limitations

- Compression is currently disabled in command flow (`compress = false` in `PensieveCommand`), although serializer supports gzip paths.

## Compatibility Notes

- Code contains multiple version guards (`1.9+`, `1.11+`, `1.13+`, `1.14+`) and NMS-backed utilities from RUoM.
- This plugin is not refined enough for production use. Use at your own risk.
