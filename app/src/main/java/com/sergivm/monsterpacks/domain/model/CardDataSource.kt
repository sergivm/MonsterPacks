package com.sergivm.monsterpacks.domain.model

/**
 * Static data source for all cards in the initial collection.
 * Images use placeholder resource names — replace with final art resources when ready.
 *
 * Convention for imageRes: "card_{id}_design"
 * Convention for variants: they share the base card's visual slot until final art is added.
 *
 * TODO: Replace all imageRes strings with actual drawable resource names once art is ready.
 */
object CardDataSource {

    const val COLLECTION_GENESIS = "collection_genesis"

    val allCards: List<Card> = buildList {
        var id = 1

        // ── INFERNAL (15 cards) ───────────────────────────────────────────────
        add(card(id++, 1,  "Ember Lizard",             CardType.INFERNAL, Rarity.COMMON,    "Small fire lizard, base creature. Skitters across volcanic rock leaving scorch marks."))
        add(card(id++, 2,  "Ash Crow",                 CardType.INFERNAL, Rarity.COMMON,    "Charred black crow with ember eyes. Feeds on the remains of those consumed by flame."))
        add(card(id++, 3,  "Cinder Beetle",            CardType.INFERNAL, Rarity.COMMON,    "Volcanic beetle with a glowing carapace. Its shell is hot enough to brand skin."))
        add(card(id++, 4,  "Smolder Toad",             CardType.INFERNAL, Rarity.COMMON,    "Volcanic toad that spits bursts of fire. Found near geothermal vents."))
        add(card(id++, 5,  "Flamehorn Ram",            CardType.INFERNAL, Rarity.COMMON,    "Ram with permanently burning horns. Charges headfirst into anything that moves."))
        add(card(id++, 6,  "Pyro Hellhound",           CardType.INFERNAL, Rarity.RARE,      "Classic hellhound with a mane of living fire. Its howl ignites the air around it."))
        add(card(id++, 7,  "Magma Crab",               CardType.INFERNAL, Rarity.RARE,      "Crab armoured in cooled lava rock. Crushes prey with claws that reheat on impact."))
        add(card(id++, 8,  "Lava Serpent",             CardType.INFERNAL, Rarity.RARE,      "A serpent that swims through magma as if it were water."))
        add(card(id++, 9,  "Cinderclaw Wyvern",        CardType.INFERNAL, Rarity.EPIC,      "Wyvern with talons of molten rock. Leaves rivers of fire wherever it lands."))
        add(card(id++, 10, "Molten Serpent",           CardType.INFERNAL, Rarity.EPIC,      "A massive serpent of pure magma. Older than the volcanoes it inhabits."))
        add(card(id++, 11, "Ember Phoenix",            CardType.INFERNAL, Rarity.EPIC,      "A phoenix reborn endlessly from its own ash. Its tears are liquid fire."))
        add(card(id++, 12, "Lava Serpent",             CardType.INFERNAL, Rarity.SPECIAL,   null, isVariant = true, baseCardId = 8))
        add(card(id++, 13, "Molten Serpent",           CardType.INFERNAL, Rarity.SPECIAL,   null, isVariant = true, baseCardId = 10))
        add(card(id++, 14, "Cinderclaw Wyvern",        CardType.INFERNAL, Rarity.LEGENDARY, null, isVariant = true, baseCardId = 9))
        add(card(id++, 15, "Magma Titan",              CardType.INFERNAL, Rarity.GOD,       null, isVariant = false))

        // ── ABYSSAL (14 cards) ────────────────────────────────────────────────
        add(card(id++, 1, "Brine Shrimp",                     CardType.ABYSSAL, Rarity.COMMON,    "A tiny deep-sea shrimp with bioluminescent spots. Harmless alone. Lethal in swarms."))
        add(card(id++, 2, "Cave Eel",                         CardType.ABYSSAL, Rarity.COMMON,    "A blind eel from flooded underwater caverns. Navigates by pressure alone."))
        add(card(id++, 3, "Deepwater Crab",                   CardType.ABYSSAL, Rarity.COMMON,    "A real deep-sea crab grown to impossible size under crushing pressure."))
        add(card(id++, 4, "Fangtooth Fish",                   CardType.ABYSSAL, Rarity.COMMON,    "A real deep-sea fish with teeth too large to close its mouth. Pure terror, small scale."))
        add(card(id++, 5, "Pressure Jellyfish",               CardType.ABYSSAL, Rarity.COMMON,    "A translucent jellyfish that implodes violently if brought to the surface."))
        add(card(id++, 6, "Abyssal Manta",                    CardType.ABYSSAL, Rarity.RARE,      "An enormous manta ray gliding through total darkness. Its wingspan blocks the sonar."))
        add(card(id++, 7, "Voidfin Shark",                    CardType.ABYSSAL, Rarity.RARE,      "A shark adapted to lightless depths. Its skin absorbs all light around it."))
        add(card(id++, 8, "Lurker Eel",                       CardType.ABYSSAL, Rarity.RARE,      "A massive predatory eel that waits motionless for centuries before striking."))
        add(card(id++, 9, "Bioluminescent Jellyfish",         CardType.ABYSSAL, Rarity.RARE,      "A stunning glowing jellyfish inspired by real deep-sea species. Beautiful and lethal."))
        add(card(id++, 10, "Deep Horror",                      CardType.ABYSSAL, Rarity.EPIC,      "A Lovecraftian deep-sea monstrosity. Its shape changes depending on who is watching."))
        add(card(id++, 11, "Leviathan",                        CardType.ABYSSAL, Rarity.EPIC,      "The classic sea serpent of legend. Long enough to encircle a continent."))
        add(card(id++, 12, "Bioluminescent Jellyfish",         CardType.ABYSSAL, Rarity.SPECIAL,   null, isVariant = true, baseCardId = 9))
        add(card(id++, 13, "Deep Horror",                      CardType.ABYSSAL, Rarity.LEGENDARY, null, isVariant = true, baseCardId = 10))
        add(card(id++, 14, "The Kraken",                       CardType.ABYSSAL, Rarity.GOD,       null))

        // ── SPECTRAL (14 cards) ───────────────────────────────────────────────
        add(card(id++, 1, "Shadow Bat",                CardType.SPECTRAL, Rarity.COMMON,    "A small bat made of living shadow. Disappears completely in darkness."))
        add(card(id++, 2, "Grave Moth",                CardType.SPECTRAL, Rarity.COMMON,    "A moth that feeds on the moss growing on ancient tombstones. Ancient and quiet."))
        add(card(id++, 3, "Pale Wisp",                 CardType.SPECTRAL, Rarity.COMMON,    "A floating ghostly orb of cold light. Lures travelers away from safe paths."))
        add(card(id++, 4, "Bone Rat",                  CardType.SPECTRAL, Rarity.COMMON,    "A skeletal rat. Undead vermin that gnaws on things it can no longer taste."))
        add(card(id++, 5, "Dusk Crow",                 CardType.SPECTRAL, Rarity.COMMON,    "A crow that vanishes completely at daybreak. Only its call remains at dawn."))
        add(card(id++, 6, "Specter Wolf",              CardType.SPECTRAL, Rarity.RARE,      "A translucent ghostly wolf. Its howl can only be heard by those about to die."))
        add(card(id++, 7, "Grave Gargoyle",            CardType.SPECTRAL, Rarity.RARE,      "A stone gargoyle animated by dark magic. Returns to stone when it believes it is watched."))
        add(card(id++, 8, "Phantom Cat",               CardType.SPECTRAL, Rarity.RARE,      "A cat that phases through walls. It has lived in the same house for three centuries."))
        add(card(id++, 9, "Duskborn Vampire",          CardType.SPECTRAL, Rarity.EPIC,      "A classic vampire, elegant and deadly. Has outlived every civilization it has fed from."))
        add(card(id++, 10, "The Wraith",                CardType.SPECTRAL, Rarity.EPIC,      "An incorporeal dark entity. It passes through all matter. It has no memory of what it was."))
        add(card(id++, 11, "Pale Banshee",              CardType.SPECTRAL, Rarity.EPIC,      "A wailing spirit that foretells death. Its scream is heard three days before the event."))
        add(card(id++, 12, "Duskborn Vampire",          CardType.SPECTRAL, Rarity.SPECIAL,   null, isVariant = true, baseCardId = 9))
        add(card(id++, 13, "The Wraith",                CardType.SPECTRAL, Rarity.LEGENDARY, null, isVariant = true, baseCardId = 10))
        add(card(id++, 14, "Lich of the Abyss",        CardType.SPECTRAL, Rarity.GOD,       null))

        // ── CELESTIAL (15 cards) ──────────────────────────────────────────────
        add(card(id++, 1, "Stardust Moth",              CardType.CELESTIAL, Rarity.COMMON,    "A moth with wings patterned like a distant galaxy. Navigates by starlight alone."))
        add(card(id++, 2, "Comet Fox",                  CardType.CELESTIAL, Rarity.COMMON,    "A small fox trailing a permanent stream of stardust. Appears only during meteor showers."))
        add(card(id++, 3, "Moon Hare",                  CardType.CELESTIAL, Rarity.COMMON,    "A rabbit that only becomes visible under direct moonlight. Invisible at noon."))
        add(card(id++, 4, "Astral Firefly",             CardType.CELESTIAL, Rarity.COMMON,    "A firefly glowing in nebula colours — violet, gold, and deep blue."))
        add(card(id++, 5, "Cloud Turtle",               CardType.CELESTIAL, Rarity.COMMON,    "A turtle drifting slowly through the upper atmosphere. It never descends."))
        add(card(id++, 6, "Nebula Serpent",             CardType.CELESTIAL, Rarity.RARE,      "A serpent with galaxy-patterned scales. Every scale reflects a different star system."))
        add(card(id++, 7, "Astral Pegasus",             CardType.CELESTIAL, Rarity.RARE,      "A winged horse with a mane of star-trail light. Leaves constellations in its wake."))
        add(card(id++, 8, "Meteor Golem",               CardType.CELESTIAL, Rarity.RARE,      "A golem built entirely from meteorite fragments. Hotter to the touch than it appears."))
        add(card(id++, 9, "Lunar Moth",                 CardType.CELESTIAL, Rarity.RARE,      "A giant moth drawn powerfully to lunar energy. Grows larger each full moon."))
        add(card(id++, 10, "Stardust Unicorn",           CardType.CELESTIAL, Rarity.EPIC,      "A unicorn with a horn of condensed starlight. Its blood is liquid aurora."))
        add(card(id++, 11, "Solar Griffin",              CardType.CELESTIAL, Rarity.EPIC,      "A griffin radiating solar energy. Blind to look at directly. Warm to stand near."))
        add(card(id++, 12, "Lunar Moth",                 CardType.CELESTIAL, Rarity.SPECIAL,   null, isVariant = true, baseCardId = 9))
        add(card(id++, 13, "Stardust Unicorn",           CardType.CELESTIAL, Rarity.SPECIAL,   null, isVariant = true, baseCardId = 10))
        add(card(id++, 14, "Solar Griffin",              CardType.CELESTIAL, Rarity.LEGENDARY, null, isVariant = true, baseCardId = 11))
        add(card(id++, 15, "Dawn Phoenix",               CardType.CELESTIAL, Rarity.GOD,       null))

        // ── STORM (14 cards) ──────────────────────────────────────────────────
        add(card(id++, 1, "Gale Sparrow",               CardType.STORM, Rarity.COMMON,    "A small bird that rides the leading edge of storm fronts. Never lands willingly."))
        add(card(id++, 2, "Storm Cloud",                CardType.STORM, Rarity.COMMON,    "A living storm cloud — a real meteorological phenomenon given consciousness and hunger."))
        add(card(id++, 3, "Thunder Gecko",              CardType.STORM, Rarity.COMMON,    "A lizard that stores static charge in its tail. Shocks anything that touches it."))
        add(card(id++, 4, "Squall Bat",                 CardType.STORM, Rarity.COMMON,    "A bat that navigates by lightning echolocation during active electrical storms."))
        add(card(id++, 5, "Rain Serpent",               CardType.STORM, Rarity.COMMON,    "A serpent that appears only during heavy rainfall. Dissolves into water at storm's end."))
        add(card(id++, 6, "Gale Hawk",                  CardType.STORM, Rarity.RARE,      "A hawk that generates small tornados at its wingtips with every downstroke."))
        add(card(id++, 7, "Lightning Eel",              CardType.STORM, Rarity.RARE,      "A massively oversized electric eel. Generates enough charge to stop a heart at distance."))
        add(card(id++, 8, "Cyclone Drake",              CardType.STORM, Rarity.RARE,      "A small drake that spawns miniature cyclones when it exhales."))
        add(card(id++, 9, "Storm Giant",                CardType.STORM, Rarity.EPIC,      "A colossal humanoid born from a supercell storm. Speaks only in thunder."))
        add(card(id++, 10, "Thunderbird",                CardType.STORM, Rarity.EPIC,      "From Native American myth. Creates thunder with every wingbeat. Cannot be calm."))
        add(card(id++, 11, "Cyclone Drake",              CardType.STORM, Rarity.SPECIAL,   null, isVariant = true, baseCardId = 8))
        add(card(id++, 12, "Storm Giant",                CardType.STORM, Rarity.SPECIAL,   null, isVariant = true, baseCardId = 9))
        add(card(id++, 13, "Thunderbird",                CardType.STORM, Rarity.LEGENDARY, null, isVariant = true, baseCardId = 10))
        add(card(id++, 14, "Tempest Lynx",               CardType.STORM, Rarity.GOD,       null))

        // ── VERDANT (13 cards) ────────────────────────────────────────────────
        add(card(id++, 1, "Moss Turtle",                CardType.VERDANT, Rarity.COMMON,    "A real turtle with a full ecosystem of moss growing on its shell. Very slow. Very old."))
        add(card(id++, 2, "Bramble Rabbit",             CardType.VERDANT, Rarity.COMMON,    "A rabbit perfectly camouflaged inside thornbush. Entirely invisible when still."))
        add(card(id++, 3, "Spore Mushroom",             CardType.VERDANT, Rarity.COMMON,    "A sentient mushroom that releases clouds of disorienting spores when threatened."))
        add(card(id++, 4, "Petal Sprite",               CardType.VERDANT, Rarity.COMMON,    "A tiny fairy made entirely of living petals. Falls apart in autumn. Regrows in spring."))
        add(card(id++, 5, "Vine Frog",                  CardType.VERDANT, Rarity.COMMON,    "A frog that blends seamlessly into rainforest vines. Hunts by absolute stillness."))
        add(card(id++, 6, "Thorn Wolf",                 CardType.VERDANT, Rarity.RARE,      "A wolf with thorned fur that functions as natural armour. Impossible to grab."))
        add(card(id++, 7, "Root Serpent",               CardType.VERDANT, Rarity.RARE,      "A serpent that burrows through root systems and emerges from the earth without warning."))
        add(card(id++, 8, "Mushroom Golem",             CardType.VERDANT, Rarity.RARE,      "A golem of compressed mycelium. Rebuilds itself from any nearby fungi when damaged."))
        add(card(id++, 9, "Ancient Treant",             CardType.VERDANT, Rarity.EPIC,      "A thousand-year-old tree that has woken up. Its voice is the sound of roots splitting stone."))
        add(card(id++, 10, "Vine Hydra",                 CardType.VERDANT, Rarity.EPIC,      "A hydra of living vines. Each severed head regrows as two new branches by morning."))
        add(card(id++, 11, "Mushroom Golem",             CardType.VERDANT, Rarity.SPECIAL,   null, isVariant = true, baseCardId = 8))
        add(card(id++, 12, "Ancient Treant",             CardType.VERDANT, Rarity.LEGENDARY, null, isVariant = true, baseCardId = 9))
        add(card(id++, 13, "Bramble Bear",               CardType.VERDANT, Rarity.GOD,       null))

        // ── FROST (14 cards) ──────────────────────────────────────────────────
        add(card(id++, 1,  "Snowdrift Owl",             CardType.FROST, Rarity.COMMON,    "An owl with feathers of compressed snow. Silent and weightless. Impossible to track."))
        add(card(id++, 2,  "Ice Hare",                  CardType.FROST, Rarity.COMMON,    "An arctic hare with a white coat and frost breath. Real species, mythologised."))
        add(card(id++, 3,  "Glacier Crab",              CardType.FROST, Rarity.COMMON,    "A crab encased in living ice armour that continuously regenerates if cracked."))
        add(card(id++, 4,  "Frost Viper",               CardType.FROST, Rarity.COMMON,    "A snake that freezes prey solid with a single bite. Hunts in blizzards."))
        add(card(id++, 5,  "Tundra Fox",                CardType.FROST, Rarity.COMMON,    "An arctic fox, real species elevated to myth. Said to guide lost travelers to warmth."))
        add(card(id++, 6,  "Permafrost Yeti",           CardType.FROST, Rarity.RARE,      "A yeti adapted to the deepest polar ice. Has not been warm since the last ice age."))
        add(card(id++, 7,  "Arctic Kitsune",            CardType.FROST, Rarity.RARE,      "A nine-tailed fox of the frozen north. Each tail controls a different aspect of winter."))
        add(card(id++, 8,  "Blizzard Serpent",          CardType.FROST, Rarity.RARE,      "A serpent that travels inside blizzards. Indistinguishable from the storm itself."))
        add(card(id++, 9,  "Ice Sphinx",                CardType.FROST, Rarity.EPIC,      "A sphinx carved from a living glacier. Still. Patient. Unspeakably old. Still alive."))
        add(card(id++, 10,  "Glacier Wyrm",              CardType.FROST, Rarity.EPIC,      "A dragon-like wyrm of pure glacial ice. Its breath locks the sea solid in seconds."))
        add(card(id++, 11,  "Arctic Kitsune",            CardType.FROST, Rarity.SPECIAL,   null, isVariant = true, baseCardId = 7))
        add(card(id++, 12,  "Ice Sphinx",                CardType.FROST, Rarity.SPECIAL,   null, isVariant = true, baseCardId = 9))
        add(card(id++, 13,  "Glacier Wyrm",              CardType.FROST, Rarity.LEGENDARY, null, isVariant = true, baseCardId = 10))
        add(card(id++, 14,  "Frost Giant",               CardType.FROST, Rarity.GOD,       null))

        // ── ARCANE (14 cards) ─────────────────────────────────────────────────
        add(card(id++, 1, "Mana Wisp",                 CardType.ARCANE, Rarity.COMMON,    "A floating orb of raw magical energy. Harmless until it destabilises. Then catastrophic."))
        add(card(id++, 2, "Hex Cat",                   CardType.ARCANE, Rarity.COMMON,    "A cat that accidentally casts minor spells when it blinks. No malice. Constant chaos."))
        add(card(id++, 3, "Runic Beetle",              CardType.ARCANE, Rarity.COMMON,    "A beetle with runes carved naturally into its shell. Each rune does something different."))
        add(card(id++, 4, "Glyph Moth",                CardType.ARCANE, Rarity.COMMON,    "A moth whose wing patterns are live, shifting runes. Impossible to study. Always moving."))
        add(card(id++, 5, "Scroll Serpent",            CardType.ARCANE, Rarity.COMMON,    "A serpent that wraps itself into scroll form. Libraries treat them as pests."))
        add(card(id++, 6, "Glyph Drake",               CardType.ARCANE, Rarity.RARE,      "A small drake that breathes runic fire. Whatever it burns becomes marked with power."))
        add(card(id++, 7, "Spellbound Gargoyle",       CardType.ARCANE, Rarity.RARE,      "A gargoyle animated by a spell that has since been forgotten. Cannot be dispelled."))
        add(card(id++, 8, "Arcane Chimera",            CardType.ARCANE, Rarity.RARE,      "A three-headed magical construct chimera. Each head belongs to a different school of magic."))
        add(card(id++, 9, "Ether Serpent",             CardType.ARCANE, Rarity.EPIC,      "A serpent that exists partly in another dimension. You can see through it. It can see through you."))
        add(card(id++, 10, "Runic Golem",               CardType.ARCANE, Rarity.EPIC,      "A massive golem constructed from stacked arcane runes. Each rune is a binding law."))
        add(card(id++, 11, "Arcane Chimera",            CardType.ARCANE, Rarity.SPECIAL,   null, isVariant = true, baseCardId = 8))
        add(card(id++, 12, "Ether Serpent",             CardType.ARCANE, Rarity.SPECIAL,   null, isVariant = true, baseCardId = 9))
        add(card(id++, 13, "Runic Golem",               CardType.ARCANE, Rarity.LEGENDARY, null, isVariant = true, baseCardId = 10))
        add(card(id++, 14, "Construct Prime",           CardType.ARCANE, Rarity.GOD,       null))

        // ── SAVAGE (14 cards) ─────────────────────────────────────────────────
        add(card(id++, 1, "Dire Boar",                 CardType.SAVAGE, Rarity.COMMON,    "A massive wild boar at prehistoric scale. Nothing moves it. Nothing stops it."))
        add(card(id++, 2, "Razorback Wolf",            CardType.SAVAGE, Rarity.COMMON,    "A wolf with blade-like dorsal spines that emerge when it runs. Leaves a trail of cuts."))
        add(card(id++, 3, "Ironhide Armadillo",        CardType.SAVAGE, Rarity.COMMON,    "An armadillo with near-indestructible natural plating. Real species, mythologised."))
        add(card(id++, 4, "Tusk Badger",               CardType.SAVAGE, Rarity.COMMON,    "A badger with elongated ivory tusks. Ferocious beyond any reasonable size."))
        add(card(id++, 5, "Primal Crow",               CardType.SAVAGE, Rarity.COMMON,    "A crow from before recorded history. It remembers things that no longer exist."))
        add(card(id++, 6, "Feral Lynx",                CardType.SAVAGE, Rarity.RARE,      "A lynx that has learned to hunt supernatural prey. Patient. Precise. Relentless."))
        add(card(id++, 7, "Saber-Tooth Tiger",         CardType.SAVAGE, Rarity.RARE,      "The classic prehistoric apex predator. Still alive. Still hunting. Still hungry."))
        add(card(id++, 8, "Dire Bear",                 CardType.SAVAGE, Rarity.RARE,      "A bear the size of a small building. Its growl registers as seismic activity."))
        add(card(id++, 9, "Bone Crusher Ape",          CardType.SAVAGE, Rarity.EPIC,      "An ape of pure prehistoric muscle. Its knuckles have cracked stone floors to rubble."))
        add(card(id++, 10, "Primal Raptor",             CardType.SAVAGE, Rarity.EPIC,      "A raptor that survived beyond its own extinction through pure ferocity and spite."))
        add(card(id++, 11, "Saber-Tooth Tiger",         CardType.SAVAGE, Rarity.SPECIAL,   null, isVariant = true, baseCardId = 7))
        add(card(id++, 12, "Bone Crusher Ape",          CardType.SAVAGE, Rarity.SPECIAL,   null, isVariant = true, baseCardId = 9))
        add(card(id++, 13, "Primal Raptor",             CardType.SAVAGE, Rarity.LEGENDARY, null, isVariant = true, baseCardId = 10))
        add(card(id++, 14, "Ironhide Rhino",            CardType.SAVAGE, Rarity.GOD,       null))

        // ── MYTHIC (15 cards) ─────────────────────────────────────────────────
        add(card(id++, 1, "Harpy",                     CardType.MYTHIC, Rarity.COMMON,    "A winged woman of Greek myth. Steals food and sanity in equal measure."))
        add(card(id++, 2, "Satyr",                     CardType.MYTHIC, Rarity.COMMON,    "Half-man, half-goat. Deeply mischievous. Deeply committed to revelry. Unavoidable."))
        add(card(id++, 3, "Siren",                     CardType.MYTHIC, Rarity.COMMON,    "A dangerous sea singer. Her voice turns ships toward rocks. She never means harm."))
        add(card(id++, 4, "Imp",                       CardType.MYTHIC, Rarity.COMMON,    "A small demonic trickster. Its pranks begin minor. They never end minor."))
        add(card(id++, 5, "Centaur Scout",             CardType.MYTHIC, Rarity.COMMON,    "A young centaur with a bow. First of its kind to venture beyond the forest's edge."))
        add(card(id++, 6, "The Cyclops",               CardType.MYTHIC, Rarity.RARE,      "A one-eyed giant of Greek legend. Slow to anger. Catastrophic when angry."))
        add(card(id++, 7, "Medusa",                    CardType.MYTHIC, Rarity.RARE,      "A gorgon whose gaze petrifies. Her garden of statues is the most visited in the world."))
        add(card(id++, 8, "Sphinx of Riddles",         CardType.MYTHIC, Rarity.RARE,      "The Egyptian sphinx. Keeper of ancient questions. Deadly to those who answer wrong."))
        add(card(id++, 9, "The Minotaur",              CardType.MYTHIC, Rarity.EPIC,      "The labyrinth beast. Half-man, half-bull. Still pacing. Still waiting. Still hungry."))
        add(card(id++, 10, "Chimera",                   CardType.MYTHIC, Rarity.EPIC,      "A lion-goat-serpent, three natures permanently at war inside a single body."))
        add(card(id++, 11, "Cerberus",                  CardType.MYTHIC, Rarity.EPIC,      "Three-headed guardian of the underworld. Has never once let anyone leave."))
        add(card(id++, 12, "Medusa",                    CardType.MYTHIC, Rarity.SPECIAL,   null, isVariant = true, baseCardId = 7))
        add(card(id++, 13, "The Minotaur",              CardType.MYTHIC, Rarity.SPECIAL,   null, isVariant = true, baseCardId = 9))
        add(card(id++, 14, "Chimera",                   CardType.MYTHIC, Rarity.LEGENDARY, null, isVariant = true, baseCardId = 10))
        add(card(id++, 15, "Hydra",                     CardType.MYTHIC, Rarity.GOD,       null))
    }

    val genesisCardCollection = CardCollection(
        id = COLLECTION_GENESIS,
        name = "Genesis",
        coverRes = "collection_genesis_cover", // TODO: replace with final art
        cards = allCards
    )

    val allCardCollections: List<CardCollection> = listOf(genesisCardCollection)

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun card(
        id: Int,
        collectionNumber: Int,
        name: String,
        type: CardType,
        rarity: Rarity,
        description: String?,
        isVariant: Boolean = false,
        baseCardId: Int? = null
    ) = Card(
        id = id,
        collectionId = COLLECTION_GENESIS,
        collectionNumber = collectionNumber,
        name = name,
        type = type,
        rarity = rarity,
        description = description,
        imageRes = "card_${id}_design", // Uses the global unique ID
        isVariant = isVariant,
        baseCardId = baseCardId
    )
}
