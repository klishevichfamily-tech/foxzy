# Foxzy

Experimental adaptive chunk-distance mod for **Minecraft 1.21.1 / NeoForge 21.1.235+ (21.1.x) / Java 21** by Foxed Team.

## What it does

- **Server:** samples tick work in five-second windows. After sustained overload, lowers player view distance one chunk at a time; recovers toward the original distance after sustained healthy ticks.
- **Client:** samples FPS every five seconds and adjusts the render distance within the player's starting setting. Restores the original distance on disconnect or when disabled.
- Both sides have a 60-second startup warmup and change cooldown, a default six-chunk floor, and separate enable switches.
- Manual or other-mod distance changes become the new baseline. Foxzy never raises a baseline below its configured floor.
- No mixins, chunk deletion, asynchronous world access, changes to simulation distance, or manipulation of forced chunk tickets.

This reduces work by temporarily showing/loading less terrain. It does **not** speed up terrain generation, replace the renderer, or guarantee higher FPS/TPS. Forced chunks and ship simulation may still dominate load.

## Client and server installation

Install the main (non-`sources`) JAR into `mods/` with Minecraft 1.21.1 and NeoForge 21.1.235 or newer 21.1.x. One JAR supports both physical sides. No custom network protocol or mandatory remote-side mod dependency is added. Server-only installation controls the server; client-only installation controls rendering and the integrated server in single player.

Config: `config/foxzy-common.toml`. Restart after editing. Set `serverEnabled=false` or `clientEnabled=false` to disable the corresponding controller. Disable one controller if another adaptive-distance mod controls the same setting. For low FPS caps or low refresh-rate VSync, tune `clientReduceBelowFps` / `clientRecoverAboveFps` or disable client adaptation.

Server operator commands:

```text
/foxzy status
/foxzy disable
/foxzy enable
```

`disable` immediately restores the baseline. Command toggles last until the next server start; the config controls persistence. Sampled tick work excludes the tick wait/sleep and is not a complete profiler measurement.

## Sable compatibility status

Designed to coexist with [Sable](https://modrinth.com/mod/sable). Sable is optional, not bundled. Foxzy does not patch its classes, alter plot chunks, remove ship tickets, or stop sublevel physics. Sable's public source was reviewed for the initial design, but **runtime compatibility and performance with Sable/Create Aeronautics are not yet verified**. Changing terrain distance can still affect visibility and terrain availability around moving ships.

Before production use, test on a copy of a world: assemble/disassemble ships, cross chunk and dimension boundaries, fly near unloaded terrain, check collisions and chunk reloads, reconnect, and compare Spark profiles with and without Foxzy at identical starting distances. Test dedicated server startup separately from a client. Do not claim a benchmark or verified compatibility until these checks have been performed.

## Build and download

GitHub Actions → **Build Foxzy** → successful run → artifact **foxzy-neoforge-1.21.1**. Download and unzip it, then use the JAR without `-sources` in its name.

Local build with Java 21 and Gradle 9.2.1:

```sh
gradle build
gradle runClient
gradle runServer
```

The server run requires you to read and accept Mojang's EULA yourself. Build output: `build/libs/`. `gradle build` also runs the dependency-free controller regression tests (bounds, cooldown, recovery, hysteresis, invalid inputs, and rebasing). A successful build proves compilation, not runtime compatibility or measured performance.

## License and development status

All Rights Reserved — Foxed Team. This is an early alpha with AI-assisted code. No telemetry or external API keys are used.
