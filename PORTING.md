# Unofficial Minecraft 1.20.1 port

This Fabric build is a backport of [Cape Provider](https://github.com/litetex-oss/mcm-cape-provider) by litetex, with code and project contributions from the [Cael & Capes contributors](https://github.com/CaelTheColher/Capes). The port is maintained by JustHari01 and is not an official upstream release.

The upstream project is licensed under GNU LGPL 2.1 or later. This port keeps the upstream `LICENSE`, copyright notices, original cape assets, and source attribution. Changes in this port are also distributed under LGPL-2.1-or-later. The complete corresponding port source is provided alongside the release.

## Target and features

- Minecraft 1.20.1, Fabric Loader 0.16.14 or later, Java 17 bytecode.
- Client-side cape provider selection and ordering, local and remote custom providers, provider metadata from mods, animated GIF/sprite textures, preview controls, and the elytra texture option.
- Includes upstream provider integrations for OptiFine, Cosmetica, LabyMod, and MinecraftCapes.
- Uses the 1.20.1 player texture, menu, rendering, and Java 17 APIs.

The optional mixin targeting `dev.imb11.skinshuffle.compat.CapesCompat` is not included: that class is absent from the 1.20.1 SkinShuffle 2.4.0 artifact. This does not affect Cape Provider's built-in cape providers or normal player cape rendering.

## Build

Run `gradlew clean build` with JDK 17 or newer. The release JAR targets Java 17 and Minecraft 1.20.1.

## Verification boundary

The port has passed `clean build` and a Fabric 1.20.1 development-client startup check with its core mixins applied and the provider manager initialized. This does not claim authenticated multiplayer cape rendering or compatibility with every third-party client mod has been exercised.
