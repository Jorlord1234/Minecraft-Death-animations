# Death Animations

Cartoon death animations for Minecraft (Fabric, Minecraft 26.3, Java 25).

## Status
- [x] Animation data for 6 deaths (fall, stalagmite, anvil, cactus, fire, drown) in `src/main/resources/assets/deathanimations/clips/`
- [x] Pure-Java sampler + death router, checked against the browser prototype (`src/test/.../CoreCheck.java`)
- [ ] Death detection hook (written, not yet compiled against Minecraft)
- [ ] 3D player model playback on the death screen

## Build on GitHub (no setup)
Push this folder to a GitHub repo. The `build` workflow compiles the mod on every push; open the run in the Actions tab and download `death-animations-jar`. Push a tag like `v0.1.0` to get a Release with the jar attached. If the run fails, open it and copy the first error.

## Run it locally (optional)
1. Install JDK 25 and IntelliJ IDEA 2025.3 or newer.
2. Open this folder in IntelliJ and let Gradle sync.
3. Run `./gradlew runClient`, start a world, and die (fall from a height).
4. Check the game log for a line starting with `Death Animations: died with message key`.

If the build fails, copy the first error message back to Claude.

## Editing animations
Keyframes live in `tools/export_clips.js`. After changing them run `node tools/export_clips.js`.
