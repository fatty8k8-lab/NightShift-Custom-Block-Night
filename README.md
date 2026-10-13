# NightShift: Custom Block Night

**Source-only prototype. No playable Minecraft build has been produced or tested. Nothing has been uploaded or published.**

Intended first version: a solo three-minute night in a separate Minecraft office. Watch the camera, close the left door, and cool the room while conserving power. Freddy, Chica, and Foxy's actual portraits, plus door and jumpscare audio, are read directly into memory from the player's installed Ultimate Custom Night executable. The character encounters are new mashup rules, not a port of UCN's complete behavior.

## Required games

- Minecraft: Java Edition, target version 1.21.5, Fabric Loader 0.16.14 and Fabric API 0.128.2+1.21.5.
- Ultimate Custom Night, Steam app 871720. The locally inspected build is 3046141, Fusion build 291. Other builds are not verified.
- Single player only.

## Intended controls

N starts or retries a night. Z toggles the left door. C toggles the camera. F toggles the fan. Freddy requires the left door, Chica requires heat below 80, and Foxy requires watching the camera during his approach. These rules need in-game testing and balancing.

The mod activates only in an integrated-server world named `NightShift`. It does not activate in existing worlds with other names. A fresh NightShift world must be included in the eventual release; it has not been generated yet.

## Source of truth

`sheets/design.json` defines the first slice. `preflight.py` checks missing values and references, then generates one Java record per sheet row in `Design.java`. These are static checks, not proof of gameplay. The room-generation code and some HUD behavior still need to be moved into sheets before release.

## Validation completed

- Located both installed games and confirmed UCN's executable structure.
- Read metadata from 2,115 images and 308 sound entries without exporting their contents.
- Compiled and ran the independent Java UCN reader against the installed game. All three 150x200 portraits and both WAV sounds decoded successfully in memory.
- Design-sheet static preflight passed.
- Melty accepted a *proposed* portable Prism recipe in an early check using placeholder file sizes. This is not a finished-package validation or an installation test.

## Build blocker

Minecraft compilation did not reach Java source compilation. Fabric Loom's merged JAR creation failed with `java.nio.file.ReadOnlyFileSystemException`. A probe found `Files.isWritable=false` while ordinary writes succeeded in the workspace under this Windows sandbox. Explicit write permission grants did not resolve it. Moving the build cache to the sandbox's temporary folder then failed because Gradle could not rename its temporary workspace into its cache destination.

The remaining prerequisite is a build environment where Java NIO can write ZIP files and Gradle can rename its cache directories. Once compilation works, source/API errors may still need fixing.

## Reproduce the checks

Use Python 3.10+ to run `preflight.py`. With JDK 21, compile `UcnReader.java` and `UcnProbe.java`, then run `UcnProbe` with the path to your installed `Ultimate Custom Night.exe`. The probe never writes game content.

Build the Minecraft mod with Gradle 8.12 and JDK 21 using `gradle build`. Network access is needed for dependencies. The eventual Prism launcher must supply `NIGHTSHIFT_UCN_DIR` from Melty's `{game:custom-ultimate-custom-night}` token. The source must not silently substitute an unrelated game or fabricated assets when the required game is missing.

## Credits and intended license

Recommended default selected: AGPL-3.0-only for the new source, with remixes allowed subject to that license. Game content remains owned by its respective creators and is excluded from this project and all release uploads.

- Minecraft: Mojang / Microsoft.
- Ultimate Custom Night: Scott Cawthon and the game's credited contributors.
- Clickteam bank-layout research: CTFAK 2.0, Kostya, Yunivers, and contributors (AGPL-3.0).
- Modding workflow: universal-modder, rehan-remade and contributors (MIT).
- Runtime/build dependencies for the proposed release: Fabric, Prism Launcher, Eclipse Temurin, and Gradle. Their licenses and required notices must accompany any redistributed components.
- Implementation prepared with OpenAI Codex.

## Still required before publishing

Compile and repair the Minecraft mod; complete sheet coverage; generate the dedicated world; finish the portable instance and dependency/license bundle; verify gameplay and failure/retry behavior; inspect the real archive; validate its recipe and actual file sizes; obtain a real gameplay screenshot; upload a draft; pass Melty's installation/gameplay test; ask the creator for publication approval.

No source-only artifact, extracted portrait preview, or initial recipe check should be presented as a working release.
