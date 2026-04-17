# MONSTER PACKS
## Game Design Document

`com.sergivm.monsterpacks`

**Version 0.6** — Pack Limit System · Upgrade Expansion · Spectacular Card Reveal · AI Art Prompt Library

*Single source of truth. Includes all design, values, and technical decisions.*

---

## Changelog

| Version | Date | Summary |
|---|---|---|
| 0.1 | — | First idea draft — base concept, stack, initial types and rarities. |
| 0.2 | 2026-04-12 | Full expansion: 10 types, card examples, pack system, SlotRule, Pack Upgrade Event, Bonus Pack, resource table, duplicate policy, visual style, roadmap. |
| 0.3 | 2026-04-12 | Card catalogue with rarities + variant system. User Level system. Pack rules refined. Rarity-Boosted vs Lucky Packs separated. All Bonus Pack parameters are upgradeable. Card scope expanded. |
| 0.4 | 2026-04-13 | Complete UX/UI design: global style, all screens defined, card opening flow, complete upgrade system with costs. CardType colour palette defined. Collection screen sort order. Settings screen. |
| 0.5 | 2026-04-13 | Art Style Guide added (§13): global style definition, base prompt string, per-type style modifiers, per-rarity layout modifiers, card frame system by rarity tier. Card Illustration Prompts added (§14): complete Midjourney/Stable Diffusion prompt for all 142 cards. |
| 0.6 | 2026-04-17 | Pack Limit System (cap 50, regen 1/30 min). Free Pack replaces Bonus Pack (10 min cooldown). New drop rates. Massive upgrade expansion (10-tier XP multiplier, Bulk Open, all pack upgrades). Spectacular card reveal: impact entrance, screen shake, physics particles, foil shimmer. Collection card framing (type-colour border, full opacity, rarity bubble). Collector identity replaces Trainer. State machine launch fix. Session reward badge. GameEngine referential equality fix. Room schema v2. §15 added: AI art prompts for Collection Cover and Android Icon. |

---

# 1. Technical Stack

Android-only, Kotlin + Jetpack Compose. No Java, no LiveData, no GlobalScope. All async via Coroutines + Flow/StateFlow.

| Technology | Version | Notes |
|---|---|---|
| Kotlin | 1.9.22 | Primary language. No Java. |
| AGP | 8.3.0 | Do not update without reviewing KSP compatibility. |
| KSP | 1.9.22-1.0.17 | Must match Kotlin version exactly. |
| minSdk | 28 (Android 9) | Minimum supported. |
| targetSdk / compileSdk | 34 | Current target. |
| Jetpack Compose BOM | 2024.02.00 | Manages Compose versions internally. |
| Hilt | 2.50 | Dependency injection. |
| Room | 2.6.1 | Local persistence. Schema version 2 — destructive migration enabled. |
| Coroutines | 1.7.3 | Game loop and async operations. |
| Gson | 2.10.1 | GameState serialisation to JSON for Room. |
| DataStore | 1.0.0 | Lightweight config and preferences. |
| Navigation Compose | 2.7.7 | Screen navigation. |

## 1.1 Architecture — Clean Architecture (Strict)

| Package | Responsibility |
|---|---|
| `domain/model/` | All entities and models. Each in its own file. |
| `domain/engine/` | GameEngine: pure logic, zero Android or Compose imports. |
| `domain/usecase/` | Optional use cases for complex logic. |
| `data/db/` | Room entities, DAOs, Database (schema v2). |
| `data/repository/` | GameRepository: load and save game state. |
| `presentation/viewmodel/` | Hilt ViewModels connecting engine to UI. |
| `presentation/screen/` | Compose screens. |
| `presentation/ui/theme/` | Theme, colours, typography. |
| `app/di/` | Hilt modules. |

> ⚠️ **GameEngine** must NOT import anything from Android or Compose. Pure logic, fully testable with JUnit.

> ⚠️ No business logic in ViewModels. No logic in Compose. Never use LiveData — Flow and StateFlow throughout. Never block main thread with Room — always `Dispatchers.IO`.

---

# 2. Game Overview

Monster Packs is an offline mobile card-collecting game. Players open packs to discover creatures, real animals, plants, objects, landscapes, and legendary beings — completing collections and levelling up. No internet connection required. Visual style: colorful, epic, modern, detailed, non-minimalist.

## 2.1 Core Loop

- Open Basic Packs — limited by the Pack Limit System (§7.1). Packs regenerate automatically over time.
- Cards revealed one by one in ascending rarity order (least rare first, most rare last).
- Earn Coins and Gems from each card obtained.
- Earn XP per pack opened — level up to unlock upgrades.
- Spend resources on Special Packs or upgrades in the Shop.
- Open Free Pack from the Shop (10-min cooldown, upgradeable).
- Complete collections.

## 2.2 First Launch — Collector Setup

On first launch, before accessing any screen, the player must enter a username. The player is referred to as **"Collector"** throughout the UI (replacing "Trainer" from earlier versions). The name is displayed in the top bar across all main screens. It can be changed once from Settings > Account; no further changes are permitted. A state machine (`setupComplete` flag in `MainUiState`) prevents UI flicker on re-launch.

## 2.3 Navigation — Bottom Tab Bar

| Tab | Icon | Description |
|---|---|---|
| Pack | 🎴 | Main pack opening screen. Shows pack counter and regeneration timer. |
| Collection | 📖 | View all cards, unlocked and locked. Type-colour borders, full opacity. |
| Shop | 🛒 | Upgrades, special packs, and Free Pack opening overlay. |
| Settings | ⚙️ | Language, Google Play, account, legal. |

> ℹ️ The bottom navigation bar is hidden from the moment the player taps "Open a Pack" through the entire card reveal sequence, until the summary screen is dismissed.

---

# 3. Models

Cards are pre-defined static entities. Opening a pack reads from the existing pool and updates the player's copy count. Pack sessions are not persisted — only card counts and resources matter.

## 3.1 Card

| Field | Type | Description |
|---|---|---|
| `id` | `Int` | Unique identifier. |
| `name` | `String` | Display name. No rarity suffix (e.g. `"Lava Serpent"`, not `"Lava Serpent (Special)"`). |
| `type` | `CardType` (enum) | Thematic family. Determines background colour. |
| `rarity` | `Rarity` (enum) | Drop probability, shape, animation, rewards. |
| `collection` | `CardCollection` (ref) | Card pool this card belongs to. (Class renamed from `Collection` to avoid Android package conflict.) |
| `description` | `String?` | Short flavour text (1–2 sentences). Present for Common, Rare, Epic. Null for Special, Legendary, God. |
| `imageRes` | `String` / ResId | Dynamic: `card_{id}_design.png` resolved at runtime via `context.resources.getIdentifier()`. |
| `collectionNumber` | `Int` | Card number within the collection (e.g. #042). Shown on card bottom-left. |
| `coinReward` | `Int` | Coins awarded on obtain. |
| `gemReward` | `Int` | Gems awarded (0 for Common / Rare / Epic). |
| `copies` | `Int` | Player-owned copies. 0 = locked (silhouette shown). |
| `isVariant` | `Boolean` | True if this is a Special / Legendary / God variant of a base card. |
| `baseCardId` | `Int?` | Reference to the base card if `isVariant = true`. |

> ℹ️ Card descriptions are unique per card — short flavour text that describes the creature in 1–2 sentences. Shown during opening and in the collection detail view. Not shown on Special, Legendary, or God cards (their art speaks for itself).

## 3.2 PackDefinition

| Field | Type | Description |
|---|---|---|
| `id` | `String` | Unique identifier (e.g. `'pack_basic'`, `'pack_fire'`). |
| `name` | `String` | Display name. |
| `type` | `PackType` (enum) | `BASIC`, `BONUS`, `TYPE_THEMED`, `RARITY_BOOSTED`, `LUCKY`. |
| `collectionId` | `String` | Card pool source. |
| `cardTypeFilter` | `List<CardType>?` | TYPE_THEMED only: restricts to these types. |
| `slotRules` | `List<SlotRule>` | Per-slot rarity rules or roll distribution. |
| `cost` | `PackCost?` | Null for free packs; otherwise Coins or Gems. |
| `coverRes` | `String` / ResId | Collection cover art shown on pack front. |
| `designRes` | `String` / ResId | Pack border/form asset — unique per pack type. |
| `backgroundRes` | `String` / ResId | Background shown during opening — set by collection and pack type. |
| `xpReward` | `Int` | XP granted when this pack is opened. |
| `cardCount` | `Int` | Cards per opening. 5 for Basic Pack, 3 for Free Pack (both upgradeable). |

> ℹ️ Each pack has: (1) a collection cover for the front face, (2) borders and decorative forms that vary by pack type, (3) a background that changes during opening based on collection and type.

> ℹ️ A pack can never contain two copies of the same card. The roll engine must re-roll if a duplicate would appear within the same opening session.

## 3.3 SlotRule

| Field | Type | Description |
|---|---|---|
| `slot` | `Int` | Slot index (1-based). |
| `guaranteedRarity` | `Rarity?` | If set, this slot always yields exactly this rarity. |
| `minimumRarity` | `Rarity?` | If set, roll cannot produce below this rarity (Lucky Packs). |
| `rollTable` | `Map<Rarity, Float>?` | Custom probability table for free-roll slots. |

## 3.4 CardCollection

| Field | Type | Description |
|---|---|---|
| `id` | `String` | Unique identifier. |
| `name` | `String` | Display name. |
| `cards` | `List<Card>` | All cards in this collection (base + variants). |
| `coverRes` | `String` / ResId | Cover image shown on pack face and collection header. |

> ℹ️ The class is named `CardCollection` (not `Collection`) to avoid conflict with Android's `java.util.Collection` package.

## 3.5 PlayerState

| Field | Type | Description |
|---|---|---|
| `username` | `String` | Chosen on first launch (Collector identity). Changeable once from Settings > Account. |
| `usernameChanged` | `Boolean` | True if already changed once. Blocks further changes. |
| `coins` | `Long` | Current coin balance. |
| `gems` | `Long` | Current gem balance. |
| `xp` | `Long` | Total accumulated XP. |
| `level` | `Int` | Current player level. |
| `cardCopies` | `Map<Int, Int>` | `cardId` → copy count. |
| `availablePacks` | `Int` | Current number of openable Basic Packs (max: `maxPacks`). |
| `maxPacks` | `Int` | Storage capacity (starts at 50, upgradeable). |
| `lastPackRegenTimeMs` | `Long` | Epoch ms of last pack regeneration calculation. |
| `freePackReadyAtMs` | `Long?` | Epoch ms when Free Pack is next available. Null = ready immediately. |
| `basicPackRarityLevel` | `Int` | Upgrade tier for Basic Pack rarity quality. |
| `basicPackCapacityLevel` | `Int` | Upgrade tier for Basic Pack storage capacity. |
| `basicPackCardCountLevel` | `Int` | Upgrade tier for Basic Pack cards per opening. |
| `basicPackRegenLevel` | `Int` | Upgrade tier for Basic Pack regeneration speed. |
| `basicPackXpLevel` | `Int` | Upgrade tier for Basic Pack XP reward. |
| `xpMultiplierLevel` | `Int` | Global XP multiplier tier (up to 10 tiers). |
| `bulkOpenLevel` | `Int` | 0 = locked. 1 = Bulk Open ×5 unlocked (requires player Level 5). |
| `freePackCooldownLevel` | `Int` | Free Pack cooldown reduction tier. |
| `freePackStoredLevel` | `Int` | Free Pack stored capacity tier. |
| `freePackGemYieldLevel` | `Int` | Free Pack gem reward tier. |
| `freePackCoinYieldLevel` | `Int` | Free Pack coin reward tier. |
| `freePackCardCountLevel` | `Int` | Free Pack card count tier. |
| `freePackRarityLevel` | `Int` | Free Pack rarity quality tier. |

---

# 4. Card Types

10 types in the initial collection. Card counts are intentionally unequal — some types feel more exclusive. Types can contain fantastical creatures, real animals, plants, objects, and real-world phenomena.

| Type | Theme | Colour (hex) | Card Count |
|---|---|---|---|
| Infernal | Fire, volcanoes, magma, hell | `#C0392B` (deep red) | 15 |
| Abyssal | Deep sea, darkness, pressure | `#1A3A5C` (dark navy) | 14 |
| Spectral | Death, dark magic, undead | `#4A235A` (dark purple) | 14 |
| Celestial | Stars, cosmos, divine light | `#D4AC0D` (gold) | 15 |
| Storm | Lightning, wind, tempests | `#2874A6` (electric blue) | 14 |
| Verdant | Forest, nature, plants, earth | `#1E8449` (forest green) | 13 |
| Frost | Ice, tundra, arctic creatures | `#AED6F1` (ice blue) | 14 |
| Arcane | Pure magic, runes, constructs | `#7D3C98` (violet) | 14 |
| Savage | Wild beasts, primal instinct | `#D35400` (burnt orange) | 14 |
| Mythic | Gods, ancient myths, legends | `#B7950B` (antique gold) | 15 |

---

# 5. Rarities

Rarity controls drop probability, card visual shape/frame, animation tier, rewards, and icon. Cards are always revealed in ascending rarity order within a pack — least rare first, most rare last. Cards are grouped by rarity within each type in `CardDataSource.kt` (Common → Rare → Epic → Variants).

| Rarity | Icon | Coin Reward | Gem Reward | Base Drop % (free slots) | Animation Tier |
|---|---|---|---|---|---|
| Common | ❇️ | 1 | 0 | ~40% | Simple fade-in |
| Rare | ⚡ | 2 | 0 | ~37% | Glow pulse |
| Epic | 💥 | 10 | 0 | ~15% | Impact slam + physics particle burst |
| Special | ⭐ | 100 | 10 | ~5% | Impact slam + particles + foil shimmer |
| Legendary | ☄️ | 500 | 50 | ~2.5% | Impact slam + screen shake + particles + foil shimmer |
| God | 🪐 | 2000 | 200 | ~0.5% | Full sequence + intense screen shake + particles + foil shimmer |

Rarity icons are displayed on the card face (bottom-right bubble) and in the Collection screen progress counters.

## 5.1 Variant Card Rules

| Variant Tier | Based on | Visual Treatment | Rule |
|---|---|---|---|
| Special | Rare or Epic | Enhanced illustration, special background effects | Selected Rares and Epics get a Special variant |
| Legendary | Epic only | Full-art — card border dissolves into the artwork | Only selected Epics get a Legendary variant |
| God | Common, Rare, or Epic | Unique layout, representative of entire type | Exactly one God per type (10 total) |

> ℹ️ Common cards never have Special or Legendary variants. Descriptions (flavour text) are shown only on Common, Rare, and Epic cards. Special, Legendary, and God cards have no description — their artwork is the statement.

---

# 6. Card Catalogue — Initial Collection

Complete card list. Cards are ordered strictly by rarity within each type in `CardDataSource.kt`: Common → Rare → Epic → Special/Legendary/God variants. Rarity suffixes have been removed from card names — visual distinction (border, art, frame) communicates rarity.

The Description column contains the flavour text shown on the card during opening and in the collection detail. Special, Legendary, and God cards have no description — the art direction is noted instead.

## 6.1 Infernal (15 cards)

| Card Name | Rarity | Description / Art Direction |
|---|---|---|
| Ember Lizard | Common | Small fire lizard, base creature. Skitters across volcanic rock leaving scorch marks. |
| Ash Crow | Common | Charred black crow with ember eyes. Feeds on the remains of those consumed by flame. |
| Cinder Beetle | Common | Volcanic beetle with a glowing carapace. Its shell is hot enough to brand skin. |
| Smolder Toad | Common | Volcanic toad that spits bursts of fire. Found near geothermal vents. |
| Flamehorn Ram | Common | Ram with permanently burning horns. Charges headfirst into anything that moves. |
| Pyro Hellhound | Rare | Classic hellhound with a mane of living fire. Its howl ignites the air around it. |
| Magma Crab | Rare | Crab armoured in cooled lava rock. Crushes prey with claws that reheat on impact. |
| Lava Serpent | Rare | A serpent that swims through magma as if it were water. |
| Cinderclaw Wyvern | Epic | Wyvern with talons of molten rock. Leaves rivers of fire wherever it lands. |
| Molten Serpent | Epic | A massive serpent of pure magma. Older than the volcanoes it inhabits. |
| Ember Phoenix | Epic | A phoenix reborn endlessly from its own ash. Its tears are liquid fire. |
| Lava Serpent | Special | Full-art redesign. Glowing lava veins pulse beneath obsidian scales. |
| Molten Serpent | Special | Enhanced art with heat distortion effects rippling across the frame. |
| Cinderclaw Wyvern | Legendary | Full-art, obsidian and fire legendary frame. The sky behind it is permanently ash. |
| Magma Titan | God | God of Infernal. A colossal volcanic titan. Its footsteps open new craters. |

## 6.2 Abyssal (14 cards)

| Card Name | Rarity | Description / Art Direction |
|---|---|---|
| Brine Shrimp | Common | A tiny deep-sea shrimp with bioluminescent spots. Harmless alone. Lethal in swarms. |
| Cave Eel | Common | A blind eel from flooded underwater caverns. Navigates by pressure alone. |
| Deepwater Crab | Common | A real deep-sea crab grown to impossible size under crushing pressure. |
| Fangtooth Fish | Common | A real deep-sea fish with teeth too large to close its mouth. Pure terror, small scale. |
| Pressure Jellyfish | Common | A translucent jellyfish that implodes violently if brought to the surface. |
| Abyssal Manta | Rare | An enormous manta ray gliding through total darkness. Its wingspan blocks the sonar. |
| Voidfin Shark | Rare | A shark adapted to lightless depths. Its skin absorbs all light around it. |
| Lurker Eel | Rare | A massive predatory eel that waits motionless for centuries before striking. |
| Bioluminescent Jellyfish | Rare | A stunning glowing jellyfish inspired by real deep-sea species. Beautiful and lethal. |
| Deep Horror | Epic | A Lovecraftian deep-sea monstrosity. Its shape changes depending on who is watching. |
| Leviathan | Epic | The classic sea serpent of legend. Long enough to encircle a continent. |
| Bioluminescent Jellyfish | Special | Full-art. Glowing tentacles fill the entire frame in electric blue and violet. |
| Deep Horror | Legendary | Full-art legendary. Tentacles extend beyond the card border into the void. |
| The Kraken | God | God of Abyssal. The legendary kraken. Its tentacles have sunk entire fleets. |

## 6.3 Spectral (14 cards)

| Card Name | Rarity | Description / Art Direction |
|---|---|---|
| Shadow Bat | Common | A small bat made of living shadow. Disappears completely in darkness. |
| Grave Moth | Common | A moth that feeds on the moss growing on ancient tombstones. Ancient and quiet. |
| Pale Wisp | Common | A floating ghostly orb of cold light. Lures travelers away from safe paths. |
| Bone Rat | Common | A skeletal rat. Undead vermin that gnaws on things it can no longer taste. |
| Dusk Crow | Common | A crow that vanishes completely at daybreak. Only its call remains at dawn. |
| Specter Wolf | Rare | A translucent ghostly wolf. Its howl can only be heard by those about to die. |
| Grave Gargoyle | Rare | A stone gargoyle animated by dark magic. Returns to stone when it believes it is watched. |
| Phantom Cat | Rare | A cat that phases through walls. It has lived in the same house for three centuries. |
| Duskborn Vampire | Epic | A classic vampire, elegant and deadly. Has outlived every civilization it has fed from. |
| The Wraith | Epic | An incorporeal dark entity that passes through all matter. It has no memory of what it was. |
| Pale Banshee | Epic | A wailing spirit that foretells death. Its scream is heard three days before the event. |
| Duskborn Vampire | Special | Full-art, crimson moon backdrop. Fangs catch the light of a dying star. |
| The Wraith | Legendary | Full-art. Shadow tendrils consume the entire frame. Only eyes remain. |
| Lich of the Abyss | God | God of Spectral. An undying lich robed in the souls of ten thousand fallen. |

## 6.4 Celestial (15 cards)

| Card Name | Rarity | Description / Art Direction |
|---|---|---|
| Stardust Moth | Common | A moth with wings patterned like a distant galaxy. Navigates by starlight alone. |
| Comet Fox | Common | A small fox trailing a permanent stream of stardust. Appears only during meteor showers. |
| Moon Hare | Common | A rabbit that only becomes visible under direct moonlight. Invisible at noon. |
| Astral Firefly | Common | A firefly glowing in nebula colours — violet, gold, and deep blue. |
| Cloud Turtle | Common | A turtle drifting slowly through the upper atmosphere. It never descends. |
| Nebula Serpent | Rare | A serpent with galaxy-patterned scales. Every scale reflects a different star system. |
| Astral Pegasus | Rare | A winged horse with a mane of star-trail light. Leaves constellations in its wake. |
| Meteor Golem | Rare | A golem built entirely from meteorite fragments. Hotter to the touch than it appears. |
| Lunar Moth | Rare | A giant moth drawn powerfully to lunar energy. Grows larger each full moon. |
| Stardust Unicorn | Epic | A unicorn with a horn of condensed starlight. Its blood is liquid aurora. |
| Solar Griffin | Epic | A griffin radiating solar energy. Blind to look at directly. Warm to stand near. |
| Lunar Moth | Special | Full-art. The moon glows through translucent wings mapped with light. |
| Stardust Unicorn | Special | Enhanced art. A galaxy is reflected perfectly in both of its eyes. |
| Solar Griffin | Legendary | Full-art. An eclipse halo surrounds the legendary frame. Light bends around it. |
| Dawn Phoenix | God | God of Celestial. A phoenix of pure sunlight. Its rebirth creates new stars. |

## 6.5 Storm (14 cards)

| Card Name | Rarity | Description / Art Direction |
|---|---|---|
| Gale Sparrow | Common | A small bird that rides the leading edge of storm fronts. Never lands willingly. |
| Storm Cloud | Common | A living storm cloud — a real meteorological phenomenon given consciousness and hunger. |
| Thunder Gecko | Common | A spiny gecko with electric blue static crackling along its tail and spine. |
| Squall Bat | Common | A bat flying through an active lightning storm, navigating by the light of nearby strikes. |
| Rain Serpent | Common | A serpent that appears to be made of heavy rain itself. Its form is indistinct from the downpour. |
| Gale Hawk | Rare | A powerful hawk in steep dive, small tornado vortices forming at each wingtip. |
| Lightning Eel | Rare | A massive electric eel crackling with stored lightning. Electric arcs leap to the surface above. |
| Cyclone Drake | Rare | A compact drake exhaling a breath that spawns a miniature cyclone. |
| Storm Giant | Epic | A colossal humanoid figure made of dark storm clouds and lightning. Speaks in thunder. |
| Thunderbird | Epic | A vast thunderbird based on Native American mythology. Each wingbeat causes visible shockwaves. |
| Cyclone Drake | Special | Full-art. The card shows the view from inside the eye of the storm looking outward. |
| Storm Giant | Special | Enhanced art with lightning veins tracing paths across skin of dark stone. |
| Thunderbird | Legendary | Full-art. Every feather a living lightning bolt. No border — supreme storm being. |
| Tempest Lynx | God | God of Storm. A lynx made of pure living electricity. It is the storm. |

## 6.6 Verdant (13 cards)

| Card Name | Rarity | Description / Art Direction |
|---|---|---|
| Moss Turtle | Common | A real turtle with a full ecosystem of moss growing on its shell. Very slow. Very old. |
| Bramble Rabbit | Common | A rabbit perfectly camouflaged inside thornbush. Entirely invisible when still. |
| Spore Mushroom | Common | A sentient mushroom that releases clouds of disorienting spores when threatened. |
| Petal Sprite | Common | A tiny fairy made entirely of living petals. Falls apart in autumn. Regrows in spring. |
| Vine Frog | Common | A frog that blends seamlessly into rainforest vines. Hunts by absolute stillness. |
| Thorn Wolf | Rare | A wolf with thorned fur that functions as natural armour. Impossible to grab. |
| Root Serpent | Rare | A serpent that burrows through root systems and emerges from the earth without warning. |
| Mushroom Golem | Rare | A golem of compressed mycelium. Rebuilds itself from any nearby fungi when damaged. |
| Ancient Treant | Epic | A thousand-year-old tree that has woken up. Its voice is the sound of roots splitting stone. |
| Vine Hydra | Epic | A hydra of living vines. Each severed head regrows as two new branches by morning. |
| Mushroom Golem | Special | Full-art. A spore cloud fills the entire background in deep green and gold. |
| Ancient Treant | Legendary | Full-art. An entire old-growth forest exists inside the card frame. |
| Bramble Bear | God | God of Verdant. A bear woven from the entire forest. As old as the soil beneath it. |

## 6.7 Frost (14 cards)

| Card Name | Rarity | Description / Art Direction |
|---|---|---|
| Snowdrift Owl | Common | An owl with feathers of compressed snow. Silent and weightless. Impossible to track. |
| Ice Hare | Common | An arctic hare with a white coat and frost breath. Real species, mythologised. |
| Glacier Crab | Common | A crab encased in living ice armour that continuously regenerates if cracked. |
| Frost Viper | Common | A snake that freezes prey solid with a single bite. Hunts in blizzards. |
| Tundra Fox | Common | An arctic fox, real species, elevated to myth. Said to guide lost travelers to warmth. |
| Permafrost Yeti | Rare | A yeti adapted to the deepest polar ice. Has not been warm since the last ice age. |
| Arctic Kitsune | Rare | A nine-tailed fox of the frozen north. Each tail controls a different aspect of winter. |
| Blizzard Serpent | Rare | A large pale serpent coiling through a blizzard, its body made partly of swirling snow. |
| Ice Sphinx | Epic | A massive sphinx carved from living glacier ice. Ancient beyond measure. |
| Glacier Wyrm | Epic | A massive wyrm of pure glacial ice. Exhales a breath that freezes the ocean surface. |
| Arctic Kitsune | Special | Full-art. Nine tails fanning dramatically across a frozen aurora in silver and pale blue. |
| Ice Sphinx | Special | Enhanced art. Ice crystals refracting light into a full visible spectrum across the frame. |
| Glacier Wyrm | Legendary | Full-art. An entire ice age contained within the illustration. No border. |
| Frost Giant | God | God of Frost. A primordial frost giant of divine scale. The first winter began when it woke. |

## 6.8 Arcane (14 cards)

| Card Name | Rarity | Description / Art Direction |
|---|---|---|
| Mana Wisp | Common | A small floating orb of pure magical energy. Drifts through ancient arcane libraries. |
| Hex Cat | Common | An ordinary-looking cat surrounded by tiny accidental spell effects. Magical chaos, confused expression. |
| Runic Beetle | Common | A beetle with clearly legible glowing runes carved into its carapace. |
| Glyph Moth | Common | A moth with wings covered in shifting living runes, constantly rearranging. |
| Scroll Serpent | Common | A serpent coiled perfectly into a scroll shape, its scales patterned like old parchment. |
| Glyph Drake | Rare | A compact drake breathing fire composed of burning runes. Each flame a legible magical symbol. |
| Spellbound Gargoyle | Rare | A stone gargoyle crackling with magical energy. A glowing spell permanently embedded in its chest. |
| Arcane Chimera | Rare | A three-headed chimera with each head radiating a different type of magic. |
| Ether Serpent | Epic | A large serpent phase-shifting between dimensions. One half solid, one half transparent. |
| Runic Golem | Epic | A colossal golem constructed from layered stacked arcane runes. Violet energy pulses through it. |
| Arcane Chimera | Special | Full-art. Each of three heads radiates a visually distinct magical school, energies colliding. |
| Ether Serpent | Special | Enhanced art. The card frame itself splits to show two different planes of existence. |
| Runic Golem | Legendary | Full-art. Every rune on its body fully legible and telling a complete story. No border. |
| Construct Prime | God | God of Arcane. The source of all spells. An overwhelming geometric and runic form of pure energy. |

## 6.9 Savage (14 cards)

| Card Name | Rarity | Description / Art Direction |
|---|---|---|
| Dire Boar | Common | A massive prehistoric boar with enormous tusks. Sheer unstoppable bulk. |
| Razorback Wolf | Common | A wolf with sharp bone-like dorsal spines. Leaves a trail of cuts in the environment as it runs. |
| Ironhide Armadillo | Common | An armadillo with overlapping armour plates that look indestructible. |
| Tusk Badger | Common | A stocky fierce badger with elongated ivory tusks. Absurdly dangerous for its size. |
| Primal Crow | Common | An ancient crow with feathers wrong for any modern species. Eyes holding millennia of memory. |
| Feral Lynx | Rare | A large lynx crouched in absolute stillness. Supernatural patience. Predator energy. |
| Saber-Tooth Tiger | Rare | A magnificent saber-tooth tiger in a prehistoric landscape. Still alive and hunting. |
| Dire Bear | Rare | A colossal bear standing upright, dwarfing the trees around it. Primeval power. |
| Bone Crusher Ape | Epic | A massive prehistoric ape of overwhelming muscle. Ancient jungle, primal dominance. |
| Primal Raptor | Epic | A lean powerful raptor in a hunting pose. Has survived its own extinction by sheer will. |
| Saber-Tooth Tiger | Special | Full-art. An ice age landscape stretching behind it under a pale prehistoric sky. |
| Bone Crusher Ape | Special | Enhanced art. Knuckles mid-impact, stone fragmenting in every direction. |
| Primal Raptor | Legendary | Full-art. A hunting pack moving in formation through primeval fern. No border. |
| Ironhide Rhino | God | God of Savage. The first beast, older than memory. Prehistoric landscape tiny beneath it. |

## 6.10 Mythic (15 cards)

| Card Name | Rarity | Description / Art Direction |
|---|---|---|
| Harpy | Common | A harpy with eagle wings and talons, perched on a Greek ruin column. Fierce and ancient. |
| Satyr | Common | A satyr caught mid-mischief in an ancient Greek forest, holding a flute. |
| Siren | Common | A siren on coastal rocks, beautiful and dangerous. Ships visible on rocks behind her. |
| Imp | Common | A small imp with tiny wings and a mischievous grin, caught mid-prank. |
| Centaur Scout | Common | A young centaur with a bow ready, standing at the edge of an ancient Greek forest. |
| The Cyclops | Rare | A massive one-eyed giant in a Greek landscape. Slow expression that hides terrifying potential. |
| Medusa | Rare | A beautiful and terrifying Medusa with living snakes for hair. Stone victims visible behind her. |
| Sphinx of Riddles | Rare | A classic Egyptian sphinx, ancient desert setting. Impossibly patient and ancient expression. |
| The Minotaur | Epic | A powerful minotaur at the centre of its labyrinth, waiting with ancient hunger. |
| Chimera | Epic | A chimera with lion head, goat head emerging from its back, serpent tail — all in conflict. |
| Cerberus | Epic | A three-headed Cerberus guarding the gates of the underworld. Each head a different expression. |
| Medusa | Special | Full-art. Stone statues of failed challengers surrounding her, filling the frame. |
| The Minotaur | Special | Enhanced art. Maze corridors receding infinitely in every direction. |
| Chimera | Legendary | Full-art. All three natures visibly pulling in different directions. Ancient mythological landscape. |
| Hydra | God | God of Mythic. Every head a different legend visualised. Truly immortal presence. |

---

# 7. Pack & Economy

## 7.1 Basic Pack — Pack Limit System

Basic Packs are a limited resource. The player begins with 50 packs and one pack regenerates every 30 minutes.

- **Initial storage capacity:** 50 packs (upgradeable).
- **Regeneration interval:** 1 pack every 30 minutes (upgradeable, minimum 1 minute).
- **Timer pause:** When the counter is full, regeneration pauses — no overflow or staged refills.
- **Real-time UI:** Pack counter and regeneration timer update every second in `MainViewModel` via a 1000ms coroutine tick.
- **On open:** 1 pack consumed. Regen timer resumes (or starts) from that moment.

```
GameEngine.getPackRegenIntervalMs(regenLevel) = BASE(30 min) - (tier × 2 min), coerced to min 1 min
```

**Slot rules:**

| Slot | Rule |
|---|---|
| Slot 1 | Guaranteed Common |
| Slot 2 | Guaranteed Rare |
| Slot 3 | Free Roll (default rate table) |
| Slot 4 | Free Roll (default rate table) |
| Slot 5 | Free Roll (default rate table) |

**Drop rates for free-roll slots:**

| Rarity | Weight | ~% |
|---|---|---|
| Common | 40.0 | ~40% |
| Rare | 37.0 | ~37% |
| Epic | 15.0 | ~15% |
| Special | 5.0 | ~5% |
| Legendary | 2.5 | ~2.5% |
| God | 0.5 | ~0.5% |

## 7.2 Free Pack (formerly Bonus Pack)

Available in the Shop tab. Opens via a dedicated overlay within the Shop screen (not a separate screen).

| Parameter | Base Value | Upgradeable? |
|---|---|---|
| Cooldown | 10 minutes | Yes — reduces per tier |
| Cards per opening | 3 | Yes — +1 per tier |
| Slot rules | 3 × Free Roll (default rate table) | — |
| XP reward | 5 | No |
| Coin reward | Base value | Yes — coin yield upgrade |
| Gem reward | Base value | Yes — gem yield upgrade |
| Stored capacity | 1 | Yes — stored capacity upgrade |

## 7.3 Special Packs (Paid)

### 7.3.1 Type-Themed Packs (10 total — one per type)

One pack per CardType. All 5 slots are free rolls restricted to cards of that type. Cost: Gems.

### 7.3.2 Rarity-Boosted Packs

All 5 slots use a shifted rarity table (higher weights for Rare+). Cost: Gems.

### 7.3.3 Lucky Packs

All 5 slots have a minimum rarity floor (e.g. minimum Epic). Cost: Gems.

## 7.4 Pack Upgrade Event (Surprise Pack)

When opening a Basic Pack, there is a 0.5% chance the pack is silently upgraded to a special variant for that session.

| Event | Weight | Effect |
|---|---|---|
| type_themed | 50 | Pack restricted to a single random CardType |
| rarity_boosted | 30 | Rarity table shifted upward |
| lucky_epic | 12 | All slots minimum Epic |
| lucky_special | 6 | All slots minimum Special |
| lucky_legendary | 2 | All slots minimum Legendary |

---

# 8. Resources

## 8.1 Coins

Earned from every card obtained. Used to purchase upgrades and packs.

| Source | Amount |
|---|---|
| Common card obtained | 1 |
| Rare card obtained | 2 |
| Epic card obtained | 10 |
| Special card obtained | 100 |
| Legendary card obtained | 500 |
| God card obtained | 2000 |
| Free Pack opened | Base + upgradeable coin yield |

## 8.2 Gems

Earned from Special, Legendary, and God cards. Used for premium upgrades and packs.

| Source | Amount |
|---|---|
| Special card obtained | 10 |
| Legendary card obtained | 50 |
| God card obtained | 200 |
| Free Pack opened | Base + upgradeable gem yield |

---

# 9. User Level System

XP is accumulated from pack openings. The global XP Multiplier upgrade applies a multiplier to all XP earned.

```
finalXp = xpReward × (1.0 + xpMultiplierLevel × 0.2)
```

`LevelSystem.levelForXp(xp)` determines the current level. Bulk Opening (x5) unlocks at Level 5 when the Bulk Open upgrade is purchased.

## 9.1 XP Sources

| Source | Base XP |
|---|---|
| Basic Pack opened | 10 (upgradeable) |
| Free Pack opened | 5 |
| Special Pack opened | 20 |

## 9.2 Level Thresholds (Initial Proposal — subject to tuning)

| Level | XP Required | Unlock |
|---|---|---|
| 1 | 0 | Starting level |
| 2 | 100 | — |
| 3 | 250 | — |
| 4 | 500 | — |
| 5 | 1000 | Bulk Open ×5 upgrade available |
| 6 | 2000 | — |
| 7 | 3500 | — |
| 8 | 5500 | — |
| 9 | 8000 | — |
| 10 | 11000 | — |

## 9.3 Upgrades (Shop)

### Global Upgrades

| Upgrade | Tiers | Effect | Requirement |
|---|---|---|---|
| XP Multiplier | 10 | +0.2× per tier (Tier 0: 1.0×, Tier 10: 3.0×) | Level 1 |
| Bulk Opening ×5 | 1 (unlock) | Open 5 packs simultaneously | Level 5 |

### Basic Pack Upgrades

| Upgrade | Effect per Tier |
|---|---|
| Regeneration Speed | Reduces regen interval by 2 min per tier. Min: 1 min. |
| Storage Capacity | Increases max pack count (base: 50). |
| Card Count | Adds +1 card per pack opening. |
| Card Rarity Quality | Shifts the rarity roll table upward for free slots. |
| XP Reward | Increases XP earned per pack opening. |

### Free Pack Upgrades

| Upgrade | Effect per Tier |
|---|---|
| Cooldown Reduction | Reduces the 10-minute cooldown. |
| Stored Capacity | Allows more than 1 Free Pack to be stored. |
| Gem Yield | Increases Gems earned per Free Pack. |
| Coin Yield | Increases Coins earned per Free Pack. |
| Card Count | Adds +1 card per Free Pack opening (base: 3). |
| Card Rarity Quality | Shifts the rarity roll table upward for Free Pack slots. |

> ℹ️ The UPGRADE button shows a reddish tint when the player cannot afford the upgrade.
> ℹ️ MAX indicators are aligned for a consistent, professional look.

---

# 10. UX / UI Design

## 10.1 Global Style

- Dark theme throughout. Background: near-black (`#0D0D0D` or similar).
- Accent colour: deep blue (`#1f3864`) for titles; blue (`#2e5da8`) for section headers.
- Typography: bold for headings, regular for body. Clean sans-serif.
- Card cells: 2dp border matching **CardType colour**. Full-opacity artwork. Rarity icon in bottom-right bubble.
- No minimalism — visually rich, dense with information and colour.

## 10.2 Global Top Bar

Appears on all main screens. Contains:
- App name or current screen title (left).
- Collector name (centre or right).
- Coin and Gem balance (right, with icons).

## 10.3 Screen: First Launch — Collector Setup

- Full-screen prompt: "Enter your Collector name".
- Text input field + confirm button.
- Navigation bar hidden until setup complete.
- State machine (`setupComplete` flag) prevents the setup screen from appearing again on subsequent launches, even if the ViewModel rebuilds.

## 10.4 Screen: MAIN (Pack Tab)

| Element | Description |
|---|---|
| Pack visual | Large animated pack image in the centre. Tapping it opens a pack (if available). |
| Pack counter | "X / MAX" showing available packs and storage cap. Updates every second. |
| Regeneration timer | Countdown to next pack regen. Paused when at max. Updates every second. |
| Open button | Disabled (greyed) if `availablePacks == 0`. |
| Bulk Open button | Visible only if `bulkOpenLevel > 0`. Opens 5 packs at once. |

## 10.5 Screen: CARD REVEAL (during pack opening)

Cards are revealed one by one. Bottom navigation bar is hidden for the duration.

| Phase | Description |
|---|---|
| Card back shown | Pack opens. Card back presented. |
| Tap to reveal | Player taps — card flips to front. |
| Entrance animation | Rarity-appropriate animation plays (see §11). |
| Next card | Repeat for each card in order (ascending rarity). |
| Summary | After last card, transitions to Summary screen. |

## 10.6 Screen: SUMMARY (after all cards revealed)

- Grid of all cards obtained in the session.
- **Session reward badge:** Total Coins and Gems earned in this specific pack opening, displayed prominently.
- "Done" button returns to Pack Tab and restores the navigation bar.

## 10.7 Screen: COLLECTION

Grid of all cards in the collection (locked and unlocked).

| Element | Description |
|---|---|
| Card cell | 2dp border matching CardType colour. Full-opacity artwork. No dimming for locked cards — silhouette shown instead. |
| Rarity bubble | Small icon in bottom-right corner of each card cell. |
| Lock state | Locked cards show silhouette only. |
| Detail view | Tap a card → full card detail: art, name, type, rarity, description (if any), copies owned, collection number. |
| Filters | Filter by Type, Rarity, locked/unlocked. |
| Sort | Default: by Type → then by Collection Number within type. |

### 10.7.1 Collection Sort Order

Default sort within type: Common → Rare → Epic → Special → Legendary → God. Within each rarity: by `collectionNumber` ascending.

## 10.8 Screen: SHOP

| Section | Description |
|---|---|
| Free Pack | Timer or "Ready!" status. Tap to open via overlay when ready. |
| Global Upgrades | XP Multiplier (10 tiers), Bulk Open ×5 (Level 5 required). |
| Basic Pack Upgrades | Regen Speed, Storage Capacity, Card Count, Rarity Quality, XP Reward. |
| Free Pack Upgrades | Cooldown, Stored Capacity, Gem Yield, Coin Yield, Card Count, Rarity Quality. |
| Special Packs | Type-Themed, Rarity-Boosted, Lucky — purchased with Gems. |

> ℹ️ Free Pack reveal uses a dedicated overlay within the Shop tab — no screen transition.

## 10.9 Screen: SETTINGS

| Option | Description |
|---|---|
| Language | Toggle Spanish / English. |
| Google Play | Rate the app, achievements (future). |
| Account | Change Collector name (once only). |
| Legal | Privacy Policy, Terms of Service. |

---

# 11. Spectacular Card Reveal System

All animations are implemented in Jetpack Compose using a custom `Particle` data class with physics-based simulation.

## 11.1 Entrance Animation by Rarity

| Rarity | Entrance | Screen Shake | Particles | Foil Shimmer |
|---|---|---|---|---|
| Common | Simple fade-in | No | No | No |
| Rare | Glow pulse | No | No | No |
| Epic | Impact slam (scale + overshoot spring) | No | Physics burst (rarity colour) | No |
| Special | Impact slam | No | Physics burst | Yes — diagonal |
| Legendary | Impact slam | Yes — haptic-style vibration | Physics burst | Yes — diagonal |
| God | Impact slam | Yes — intense | Full physics burst | Yes — diagonal |

## 11.2 Animation Detail

- **Impact entrance:** Card scales in from 0 with a spring overshoot — the "slam" effect.
- **Screen shake:** Implemented as rapid X/Y translation offsets in a coroutine loop. Legendary and God cards trigger this on reveal.
- **Particle system:** Physics-based burst of coloured energy particles (colour matches `Rarity`). Special+ rarities trigger this.
- **Foil shimmer:** A diagonal animated overlay simulating a holographic foil effect. Continuous loop while the card is on screen.

## 11.3 Card Opening Flow

1. Pack back shown. Player taps.
2. Each card revealed one by one, ascending rarity order.
3. Card back → flip → front → entrance animation.
4. Player taps "Next" or waits for auto-advance.
5. After last card: Summary screen with session reward badge.

---

# 12. Planned for v0.7

| Feature | Description |
|---|---|
| Particle Type Theming | Unique particle shapes/behaviours based on CardType (flames for Infernal, stars for Celestial, frost crystals for Frost, etc.). |
| Animated Pack Opening Background | Dreamy/oniric animated backgrounds during the pack opening sequence. |
| Bulk Opening Logic | Roll 25 cards at once (requires `bulkOpenLevel > 0`). |
| Localization | Spanish / English toggle. Strings partially in `values-es/`. |
| Push Notifications | Notify player when the pack counter reaches maximum. |
| Dev Settings Mode | Hidden debug panel: equal drop rates for all rarities (for animation testing) + resources ×100 multiplier for rapid Shop testing. |

---

# 13. Art Style Guide

## 13.1 Global Style Definition

Visual style: **colorful, epic, modern, detailed, non-minimalist**. All illustrations are semi-realistic digital paintings — clearly painted, not photographic — with rich textures, dramatic lighting, dark atmospheric backgrounds, and vibrant saturated colours.

## 13.2 Base Prompt (include in every card prompt)

```
semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic,
rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors,
epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation
```

## 13.3 Type Style Modifiers

| Type | Palette & Atmosphere | Environment Keywords |
|---|---|---|
| Infernal | deep red and orange | volcanic environment, molten rock, ember particles, heat haze, dark ashen atmosphere, hellish dramatic lighting from below |
| Abyssal | dark navy and deep teal | bioluminescent accents, crushing ocean darkness, pressure distortion, cold light from unknown sources, deep sea horror |
| Spectral | dark purple and cold blue | ghostly translucency, wisps of dark energy, moonlit shadow, ethereal fog, haunted atmosphere, death magic visual language |
| Celestial | gold and deep space | starfield, cosmic energy, nebula colours, divine radiance, star-trail light, astronomical backdrop |
| Storm | electric blue and silver | lightning, storm clouds, lightning arcs, dynamic wind motion, dramatic atmospheric energy, charged particles in the air |
| Verdant | forest green and earth tones | dense vegetation, bioluminescent flora, root systems, ancient forest atmosphere, dappled light, spores floating in air |
| Frost | ice blue and silver white | frozen tundra, aurora borealis, crystalline ice, blizzard atmosphere, arctic stillness, frozen breath condensation |
| Arcane | violet and arcane gold | floating magical runes, spell energy trails, arcane sparks, mystical geometric patterns, magical laboratory or ancient library |
| Savage | burnt orange and raw earth | raw nature, ancient wilderness, dramatic natural light, primal energy, prehistoric landscape |
| Mythic | antique gold and ancient stone | Greek or ancient Mediterranean environment, mythological drama, classical epic atmosphere |

## 13.4 Rarity Layout Modifiers

| Rarity | Layout Modifier |
|---|---|
| Common | classic card illustration layout, subject centered, clear simple background, clean composition |
| Rare | dynamic composition, detailed background environment, subject with slight dramatic pose, atmospheric depth |
| Epic | full dramatic composition, highly detailed background, epic pose, particle effects, intense lighting |
| Special | enhanced full-art composition with special border effects, glowing elements, premium quality artwork |
| Legendary | full-art illustration filling entire card with no traditional border, cinematic scale, legendary presence |
| God | full-art divine illustration, overwhelming scale and presence, godlike visual language, fills the entire frame |

## 13.5 Card Frame System by Rarity

| Rarity | Frame & Layout Design | Design Intent |
|---|---|---|
| Common | Classic portrait frame. Card name at top. Illustration ~60% of card height. Description text below. Card number bottom-left, rarity bubble bottom-right. Border: type colour, thin and clean. | Simple, clean, classic card game layout. |
| Rare | Same base as Common with enhanced decorative border — subtle filigree or geometric in type colour. Illustration ~65%. Text box has gradient or texture. Name has faint glow. | Classic layout with added decorative detail. |
| Epic | Border significantly more elaborate — ornate design with rarity-specific elements. Illustration ~70%. Name text styled prominently. | Premium feel building toward full-art. |
| Special | Illustration ~80%. Border dissolves into decorative elements at corners. Name overlaps slightly into illustration. Enhanced glow and particle effects integrated into card design. | Border is dissolving. Illustration pushes its boundaries. |
| Legendary | Full-art layout. Illustration 90%+ of card. Name as elegant text overlay integrated into art. Card number and rarity in corners of artwork. | Near-full-art. Text overlaid on illustration. |
| God | True full-art. Illustration fills 100% of card — no traditional border. Name, number, and rarity icon typographically integrated into the artwork, styled to feel part of the illustration. | Pure full-art. Zero border. The card IS the art. |

> ℹ️ The progression Common → Rare → Epic → Special → Legendary → God should feel like a continuous dissolve: the border retreats, the illustration advances, and by God tier the card IS the art.

---

# 14. Card Illustration Prompts

Complete Midjourney / Stable Diffusion prompts for all 142 cards. Each prompt is structured as:

```
[SUBJECT DESCRIPTION], [TYPE STYLE MODIFIER], [RARITY LAYOUT MODIFIER], [BASE STYLE]
```

For Midjourney append `--ar 2:3 --v 6`. For Stable Diffusion use portrait aspect ratio (512×768 or 768×1152).

## 14.1 Infernal (15 cards)

| Card | Rarity | Prompt |
|---|---|---|
| Ember Lizard | Common | *a small fire lizard with glowing ember scales, skittering across black volcanic rock, leaving tiny scorch marks, orange and red glow from its body, deep red and orange color palette, volcanic environment, molten rock and ember particles, heat haze, dark ashen atmosphere, hellish dramatic lighting from below, classic card illustration layout, subject centered, clear simple background, clean composition, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Ash Crow | Common | *a charred black crow with glowing orange ember eyes, perched on a scorched volcanic rock, ash swirling around it, dark and atmospheric, deep red and orange color palette, volcanic environment, molten rock and ember particles, heat haze, dark ashen atmosphere, hellish dramatic lighting from below, classic card illustration layout, subject centered, clear simple background, clean composition, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Cinder Beetle | Common | *a large volcanic beetle with a glowing orange and red carapace radiating intense heat, standing on black volcanic rock, steam rising from the stone beneath its feet, deep red and orange color palette, volcanic environment, molten rock and ember particles, heat haze, dark ashen atmosphere, hellish dramatic lighting from below, classic card illustration layout, subject centered, clear simple background, clean composition, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Smolder Toad | Common | *a stocky volcanic toad with cracked glowing skin mid-spit of a burst of fire, sitting near a geothermal vent, heat shimmer and volcanic smoke around it, deep red and orange color palette, volcanic environment, molten rock and ember particles, heat haze, dark ashen atmosphere, hellish dramatic lighting from below, classic card illustration layout, subject centered, clear simple background, clean composition, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Flamehorn Ram | Common | *a powerful ram with horns permanently on fire, mid-charge, impact dust rising, expression of absolute fury, deep red and orange color palette, volcanic environment, molten rock and ember particles, heat haze, dark ashen atmosphere, hellish dramatic lighting from below, classic card illustration layout, subject centered, clear simple background, clean composition, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Pyro Hellhound | Rare | *a large aggressive hellhound with a full mane of living fire instead of fur, mid-howl, firelight illuminating volcanic ground around it, deep red and orange color palette, volcanic environment, molten rock and ember particles, heat haze, dark ashen atmosphere, hellish dramatic lighting from below, dynamic composition, detailed background environment, subject with slight dramatic pose, atmospheric depth, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Magma Crab | Rare | *a massive crab armoured entirely in cooled lava rock, claws raised and radiating intense heat, cracks of orange magma visible at the joints, standing in a volcanic landscape, deep red and orange color palette, volcanic environment, molten rock and ember particles, heat haze, dark ashen atmosphere, hellish dramatic lighting from below, dynamic composition, detailed background environment, subject with slight dramatic pose, atmospheric depth, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Lava Serpent | Rare | *a large serpent swimming fluidly through a river of magma as if it were water, obsidian scales with orange glowing veins, entirely at home in the lava, deep red and orange color palette, volcanic environment, molten rock and ember particles, heat haze, dark ashen atmosphere, hellish dramatic lighting from below, dynamic composition, detailed background environment, subject with slight dramatic pose, atmospheric depth, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Cinderclaw Wyvern | Epic | *a powerful wyvern landing with talons of molten rock, rivers of fire spreading from the impact point, volcanic eruption in the background, wings spread wide, deep red and orange color palette, volcanic environment, molten rock and ember particles, heat haze, dark ashen atmosphere, hellish dramatic lighting from below, full dramatic composition, highly detailed background, epic pose, particle effects, intense lighting, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Molten Serpent | Epic | *a massive ancient serpent made of pure magma, body glowing orange-white with internal heat, coiling through volcanic caverns, older than the mountains around it, deep red and orange color palette, volcanic environment, molten rock and ember particles, heat haze, dark ashen atmosphere, hellish dramatic lighting from below, full dramatic composition, highly detailed background, epic pose, particle effects, intense lighting, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Ember Phoenix | Epic | *a phoenix in mid-rebirth from its own ash, fire taking the form of wings, tears of liquid fire falling as it rises from the embers of its last death, deep red and orange color palette, volcanic environment, molten rock and ember particles, heat haze, dark ashen atmosphere, hellish dramatic lighting from below, full dramatic composition, highly detailed background, epic pose, particle effects, intense lighting, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Lava Serpent | Special | *a premium full-art lava serpent in close detail, obsidian scales covering the entire frame, glowing lava veins pulsing beneath them like living circuitry, deep red and orange color palette, volcanic environment, molten rock and ember particles, heat haze, dark ashen atmosphere, hellish dramatic lighting from below, enhanced full-art composition with special border effects, glowing elements, premium quality artwork, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Molten Serpent | Special | *a molten serpent in premium special composition, heat distortion rippling visibly across every surface of the card frame, the serpent partially dissolved into pure magma, deep red and orange color palette, volcanic environment, molten rock and ember particles, heat haze, dark ashen atmosphere, hellish dramatic lighting from below, enhanced full-art composition with special border effects, glowing elements, premium quality artwork, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Cinderclaw Wyvern | Legendary | *a legendary cinderclaw wyvern in full-art filling the entire card, obsidian body and fire-river talons, the sky permanently ash and fire behind it, no border, pure elemental fire power, deep red and orange color palette, volcanic environment, molten rock and ember particles, heat haze, dark ashen atmosphere, hellish dramatic lighting from below, full-art illustration filling entire card with no traditional border, cinematic scale, legendary presence, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Magma Titan | God | *a colossal volcanic titan godlike in scale, made of living rock and magma, volcanic eruptions small beside it, fills the entire frame, divine destructive presence, full-art god card, deep red and orange color palette, volcanic environment, molten rock and ember particles, heat haze, dark ashen atmosphere, hellish dramatic lighting from below, full-art divine illustration, overwhelming scale and presence, godlike visual language, fills the entire frame, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |

## 14.2 Abyssal (14 cards)

| Card | Rarity | Prompt |
|---|---|---|
| Brine Shrimp | Common | *a tiny deep-sea shrimp with bioluminescent spots along its translucent body, hovering in complete ocean darkness, eerie cold light from its own glow, dark navy and deep teal palette, bioluminescent accents, crushing ocean darkness, pressure distortion, cold light from unknown sources, deep sea horror atmosphere, classic card illustration layout, subject centered, clear simple background, clean composition, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Cave Eel | Common | *a blind eel from flooded underwater caverns, elongated and pale, navigating total darkness by pressure sensing, a faint trail of disturbed water behind it, dark navy and deep teal palette, bioluminescent accents, crushing ocean darkness, pressure distortion, cold light from unknown sources, deep sea horror atmosphere, classic card illustration layout, subject centered, clear simple background, clean composition, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Deepwater Crab | Common | *a real deep-sea crab grown to impossible size under crushing ocean pressure, claws spread wide, standing on the abyss floor, deep cold darkness behind it, dark navy and deep teal palette, bioluminescent accents, crushing ocean darkness, pressure distortion, cold light from unknown sources, deep sea horror atmosphere, classic card illustration layout, subject centered, clear simple background, clean composition, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Fangtooth Fish | Common | *a real fangtooth fish with teeth far too large to close its mouth, tiny body, enormous teeth, deep sea darkness, terrifying at close range despite small size, dark navy and deep teal palette, bioluminescent accents, crushing ocean darkness, pressure distortion, cold light from unknown sources, deep sea horror atmosphere, classic card illustration layout, subject centered, clear simple background, clean composition, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Pressure Jellyfish | Common | *a translucent jellyfish with long trailing tentacles glowing faint blue, floating in deep black water, inner organs faintly visible, dark navy and deep teal palette, bioluminescent accents, crushing ocean darkness, pressure distortion, cold light from unknown sources, deep sea horror atmosphere, classic card illustration layout, subject centered, clear simple background, clean composition, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Abyssal Manta | Rare | *an enormous manta ray with dark blue-black skin gliding silently through complete underwater darkness, faint bioluminescent patterns on its wings, dark navy and deep teal palette, bioluminescent accents, crushing ocean darkness, pressure distortion, cold light from unknown sources, deep sea horror atmosphere, dynamic composition, detailed background environment, subject with slight dramatic pose, atmospheric depth, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Voidfin Shark | Rare | *a sleek shark with matte black skin that seems to absorb all surrounding light, swimming through the abyss, only its white teeth visible in darkness, dark navy and deep teal palette, bioluminescent accents, crushing ocean darkness, pressure distortion, cold light from unknown sources, deep sea horror atmosphere, dynamic composition, detailed background environment, subject with slight dramatic pose, atmospheric depth, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Lurker Eel | Rare | *a massive ancient eel coiled motionlessly in the deep, barnacles on its skin, ancient and immense, a single glowing lure protruding from its head, dark navy and deep teal palette, bioluminescent accents, crushing ocean darkness, pressure distortion, cold light from unknown sources, deep sea horror atmosphere, dynamic composition, detailed background environment, subject with slight dramatic pose, atmospheric depth, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Bioluminescent Jellyfish | Rare | *a breathtakingly beautiful jellyfish with long trailing tentacles glowing in electric blue and violet, floating in total darkness, real deep-sea species inspired, dark navy and deep teal palette, bioluminescent accents, crushing ocean darkness, pressure distortion, cold light from unknown sources, deep sea horror atmosphere, dynamic composition, detailed background environment, subject with slight dramatic pose, atmospheric depth, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Deep Horror | Epic | *a massive Lovecraftian abyssal creature of impossible anatomy, multiple eyes, flowing tentacles, deep navy and black, its shape difficult to comprehend, terrifying scale, dark navy and deep teal palette, bioluminescent accents, crushing ocean darkness, pressure distortion, cold light from unknown sources, deep sea horror atmosphere, full dramatic composition, highly detailed background, epic pose, particle effects, intense lighting, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Leviathan | Epic | *a colossal ancient sea serpent emerging from the deepest ocean trench, dark blue-black scales, glowing eyes, its body curving across the frame suggesting impossible length, dark navy and deep teal palette, bioluminescent accents, crushing ocean darkness, pressure distortion, cold light from unknown sources, deep sea horror atmosphere, full dramatic composition, highly detailed background, epic pose, particle effects, intense lighting, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Bioluminescent Jellyfish | Special | *a stunning bioluminescent jellyfish filling the entire frame with glowing tentacles in electric blue and violet, premium full-art illustration, deep black ocean void behind it, dark navy and deep teal palette, bioluminescent accents, crushing ocean darkness, pressure distortion, cold light from unknown sources, deep sea horror atmosphere, enhanced full-art composition with special border effects, glowing elements, premium quality artwork, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| Deep Horror | Legendary | *a full-art legendary deep horror filling the entire card frame, tentacles extending beyond the visual boundary into void, overwhelming incomprehensible scale, dark navy abyss, dark navy and deep teal palette, bioluminescent accents, crushing ocean darkness, pressure distortion, cold light from unknown sources, deep sea horror atmosphere, full-art illustration filling entire card with no traditional border, cinematic scale, legendary presence, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |
| The Kraken | God | *a god-tier kraken of divine scale, tentacles spanning the full frame, crushing ocean darkness, sunken ships tiny beside its arms, full-art god card, overwhelming abyssal presence, dark navy and deep teal palette, bioluminescent accents, crushing ocean darkness, pressure distortion, cold light from unknown sources, deep sea horror atmosphere, full-art divine illustration, overwhelming scale and presence, godlike visual language, fills the entire frame, semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic, rich textures, dramatic lighting, dark atmospheric background, vibrant saturated colors, epic fantasy card game art, high detail, professional quality, no text, no watermark, portrait orientation* |

> ℹ️ Prompts for §14.3 through §14.10 (Spectral, Celestial, Storm, Verdant, Frost, Arcane, Savage, Mythic) are unchanged from GDD v0.5 — refer to that document for the complete set. All prompts remain valid in v0.6.

---

# 15. AI Art Prompts — Collection Cover & Android Icon *(New in v0.6)*

This section contains the Stable Diffusion / Midjourney prompts for the two key graphic assets not covered by the per-card prompt library: the Collection Cover Art and the Android Launcher Icon. Both follow the visual style defined in §13.

## 15.1 Collection Cover Art

**Used as:** `coverRes` field in `PackDefinition` (pack face art and collection header).  
**Format:** Portrait — 512×768 or 768×1152. For Midjourney: `--ar 2:3 --v 6`.

```
a dramatic collection cover art featuring iconic monsters from ten elemental realms —
a volcanic titan of magma, an abyssal kraken rising from darkness, a celestial phoenix
of pure starlight, a frost giant of primordial ice, and a spectral lich robed in souls —
arranged in an epic ensemble composition, all ten types represented with their signature
color palettes radiating outward: deep red, dark navy, dark purple, gold, electric blue,
forest green, ice blue, violet, burnt orange, antique gold,
divine epic composition, all creatures facing forward with overwhelming presence,
radiant energy emanating from center, legendary card game cover quality,
god-tier visual language, fills the entire frame,
semi-realistic digital painting, detailed fantasy illustration, clearly painted not photographic,
rich textures, dramatic lighting, dark atmospheric background,
vibrant saturated colors, epic fantasy card game art, high detail, professional quality,
no text, no watermark, portrait orientation
```

> 💡 **Tip:** Generate several variants. Choose the one with the best balance of all 10 types visible. If one creature dominates, add negative prompt: `one creature dominant, single monster, minimalist`.

## 15.2 Android Launcher Icon

**Used as:** mipmap app launcher icon.  
**Format:** Square — 512×512 (1:1 ratio). Must work at 48dp minimum (Android launcher size) and with OS-applied rounded corners.

```
a single iconic monster pack card emblem for a mobile app icon,
a glowing card symbol at center radiating multicolored elemental energy —
fire red, ocean navy, purple spectral, gold celestial, electric blue —
bursting outward like a shockwave,
bold centered composition, dark deep blue-black background with radiant glow,
premium card game logo quality, symbolic and immediately readable at small size,
clean geometric energy burst, no clutter, strong silhouette,
semi-realistic digital painting, vibrant saturated colors, epic fantasy card game art,
professional quality, no text, no watermark, square orientation, app icon composition
```

> 💡 **Tip:** If the result is too cluttered at small sizes, add: `centered subject, minimal background detail, high contrast`. Target: clearly recognisable at 48×48dp.

> ℹ️ Both assets should be visually consistent with §13 — colorful, epic, non-minimalist, dramatic lighting, vibrant saturated colors.
