---
name: project-minecraft-version
description: This mod targets Minecraft 26.1.2-beta on NeoForge — bleeding-edge with renamed vanilla APIs (Identifier, etc). Critical context before suggesting any code.
metadata:
  type: project
---

DarkFantasyMod targets **Minecraft 26.1.2.60-beta** on **NeoForge** (moddev plugin 2.0.141), **Java 25**.

**Why:** Mojang shipped Java 25 to end users in 26.1.2; the version is post-1.21 with significant API renames. Pre-1.21 / Forge tutorials online will have wrong class names.

**How to apply:** When writing or reviewing code, use the post-26.1.2 names:
- `Identifier` (NOT `ResourceLocation`) — construct via `Identifier.fromNamespaceAndPath(ns, path)`
- `Identifier` lives in `net.minecraft.resources` (same package as before)
- Entity sources show classes like `Avatar`, `Stopwatch`, `Stopwatches`, `EntityProcessor`, `InsideBlockEffectApplier`, `EntityFluidInteraction` — these are new since 1.21
- `EntityType.Builder.of(...).build(ResourceKey)` — build now takes the ResourceKey, not a string
- `InteractionResult.SUCCESS_SERVER` exists alongside `SUCCESS` (server-only success variant)
- `MobCategory` still exists; `SpawnPlacementTypes` (plural) is the holder for ON_GROUND / NO_RESTRICTIONS / etc.

Don't guess at a name — check the extracted vanilla sources first.
