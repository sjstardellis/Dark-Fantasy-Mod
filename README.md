# Dark Fantasy

A dark-fantasy adventure mod for **Minecraft 26.1.2** ([NeoForge](https://neoforged.net/)). Built around a celestial theme — shadow, moon, dawn, and eclipse — with two new dimensions, custom biomes, a full magic system of staffs and spell tomes, alchemy, brewing, and a cast of original creatures to fight, befriend, or trade with.

> **Minecraft** 26.1.2 · **Loader** NeoForge · **Version** 1.0.0

## 🌑 Dimensions

- **The Skylands** — a floating-island realm reached through a custom portal.
- **The Twilight Forest** — a permanently dusk woodland dimension with its own portal, biome, and ambient creatures.

## 🌿 Biomes

Three rare biomes are woven into the **Overworld**, each hiding its own gem ore underground:

- **Cinderbark Forest** — hot, ashen woodland of fire-immune cinderbark; home of **Emberstone**.
- **Gravewood Grove** — cold, dark forest of gravewood; **Mercuryglass** lies in the deepslate beneath.
- **Ghostwillow Marsh** — pale, misty wetland of ghostwillow; **Larimar Pearl** rests in the shallow stone below.

## ⚔️ Materials & Equipment

Four metal tiers, each with a full tool + armor set and a unique **signature weapon** and **set bonus**:

| Metal | Tier | Set Bonus                                | Signature Weapon |
|---|---|------------------------------------------|---|
| **Shadowsteel** | Dark / stealth | Invisibility & Speed while sneaking      | **Daggers** — bonus damage from behind or while sneaking |
| **Moonsilver** | Lunar | Night Vision; Strength after dark        | **Scythe** — wide sweep hits all nearby foes |
| **Dawnmetal** | Solar | Fire Resistance; Absorption in daylight  | **Sunlance** — extra reach, ignites foes, smites undead |
| **Eclipsium** | Fused endgame (Epic, fully fireproof gear) | Night Vision, Fire Resistance & Strength | — |

- **Biome Gems** — *Mercuryglass, Emberstone, Larimar Pearl*: mined only in their home biomes; power staffs and fuel alchemy.
- **Alchemy Reagents** — *Arcane Ash, Storm Scale, Fairy Dust, Lytebug Dust*: dropped by creatures, used in brewing and crafting.

## ✨ Magic — Staffs & Spell Tomes

**Staffs** (projectile / burst spells):

- **Lightning Staff** & **Fireball Staff** — bolt and blast.
- **Frost Staff** — a frost bolt that slows and freezes.
- **Blink Staff** — short-range teleport to where you look.
- **Cinder Staff** — a fire nova around you.

**Spell Tomes** (ritual & utility magic, crafted at the Alchemy Stand):

- **Warding Tome** — Resistance, Absorption & Fire Immunity ward.
- **Maelstrom Tome** — drags nearby foes to you and mires them.
- **Wayfarer's Tome** — bind a waypoint and recall to it.
- **Wither Skull Tome** — hurls an explosive, withering skull.
- **Prospector's Tome** — dowses for nearby ore veins.
- **Stasis Tome** — freezes nearby foes in place.
- **Evoker Claw Tome** — erupts a marching line of biting fangs.

## 🧪 Alchemy, Brewing & Consumables

- **Alchemy Stand** — a custom station that brews **elixirs**, gem-core **staffs**, and **spell tomes** from reagents.
- **Elixirs** — *Moonlight* (night vision + invisibility), *Stoneskin* (Resistance II), *Emberblood* (fire resistance + strength), *Wispstep* (speed + jump boost).
- **Beer Brewing** — grow **Hops**, ferment in the **Brewing Keg** over a heat source, and draw drinks off with a **Stein Glass**. Beer grants Resistance (with a tipsy haze) — and doubles as goblin currency.
- **Elixir** — a brewable liquid you can bucket and place in the world.

## 👹 Mobs & Creatures

- **Electro Dragon** — a storm-wreathed, flying boss-class monster.
- **Wizard** — a hostile spellcaster that guards several structures.
- **Goblin** — a scrappy melee + rock-throwing raider that goes **berserk** at low health, and **trades** when calm.
- **Gnome** — a tiny potion-lobbing menace that ignores anyone wearing leather, freezes when watched, and nests in **Gnome Burrows**.
- **Fairy** — a small winged Twilight Forest creature.
- **Lytebug** — a passive glowing firefly drawn to **Lytestone**.

## 🪙 Trading & Economy

- **Goblin barter** — trade **Fairy Dust** (everyday goods) or **Beer** (a prized pool with a small chance at mod metals) with neutral goblins.
- **Beer is special** — the only currency that pacifies a hostile goblin, leaving it tipsy and unable to fight for a short time.

## 🏛️ Structures

Naturally generated, biome-gated landmarks:

- **Grand Archive** — a giant multi-floor gothic library with a basement vault, corner towers, hundreds of bookshelves (23 of them *enchanted*), guardians, and treasure.
- **Eclipse Temple**, **Wizard Tower**, **Crypt**, **Ring Ruins**, **Charred Spire**, and **Ruined Church** — themed ruins with their own loot and inhabitants.
- **Gnome Hollow** & **Goblin Camp** — dwellings for the mod's namesake creatures.

## 🧱 Blocks & Worldbuilding

- **Three custom wood sets** — *Ghostwillow*, *Gravewood*, and fire-immune nether-style *Cinderbark*, each with the full range of planks, stairs, slabs, fences, gates, doors, trapdoors, buttons, and pressure plates (plus leaves & saplings for the two trees).
- **Enchanted Bookshelf** — counts as two vanilla bookshelves for enchanting and frames the Twilight portal.
- **Lytestone** — a glowstone-grade light source that attracts lytebugs.
- **Ores** — Shadowsteel, Moonsilver, and Dawnmetal (stone + deepslate variants), plus the three biome gems.

## Building

Requires **JDK 25** (the Gradle toolchain fetches it automatically if it isn't installed).

```bash
./gradlew build          # produces the mod jar in build/libs
./gradlew runClient      # launch a dev client
./gradlew runData        # regenerate datagen assets/data into src/generated
```

## License

Released under the [MIT License](LICENSE).

## Links

- Issues: https://github.com/sjstardellis/Dark-Fantasy-Mod/issues
- NeoForge docs: https://docs.neoforged.net/
