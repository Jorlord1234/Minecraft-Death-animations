# Death Animations (NeoForge 1.21.1)

Cartoon death animations for Minecraft, based on how you died. NeoForge 21.1, Minecraft 1.21.1, Java 21. Client-side only.

## Status
- [x] Animation data for 6 deaths (fall, stalagmite, anvil, cactus, fire, drown): `src/main/resources/assets/deathanimations/clips/`
- [x] Pure-Java sampler + death router, checked against the browser prototype (`src/test/.../CoreCheck.java`, runs in every build)
- [ ] Death detection hook (written, not yet compiled against Minecraft)
- [x] `/deathanim <name>` plays an animation in 3D on your skin (body motion, red tint, ash, flash). Not compiled yet.
- [x] Settings (cave, underwater, desert) and 3D props (stalagmite + stalactites, anvil, cactus, flames, stars, bubbles). Not compiled yet.
- [ ] Face swaps (wide eyes, X eyes), cactus spikes
- [ ] Auto-play when you actually die

## Build on GitHub (no setup)
Push this folder to a GitHub repo. The `build` workflow compiles the mod on every push; open the run in the Actions tab and download `death-animations-jar`. Push a tag like `v0.1.0` for a Release with the jar attached. If a run fails, copy the first error.

## Try it
Put the jar in your NeoForge 1.21.1 `mods` folder, start a world and die (fall from a height). The game log should contain `Death Animations: died with message key`.

## Commands
`/deathanim` lists the animations, `/deathanim stalagmite` plays one. Space replays, Esc closes.

## Editing animations
Keyframes live in `tools/export_clips.js`. After changing them run `node tools/export_clips.js`.
