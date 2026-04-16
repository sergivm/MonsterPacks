# MonsterPacks - GDD v0.6 Implementation Summary

This document recaps all features, fixes, and architectural changes implemented during the transition from GDD v0.5 to the current state.

## 🃏 Card System & Data
- **Card Naming Refactor**: Removed rarity suffixes (e.g., "(Special)", "(Leg.)") from all card names to rely on visual distinction via artwork.
- **Rarity Grouping**: Reordered all cards in `CardDataSource.kt` to be grouped strictly by Rarity within each Type (Common -> Rare -> Epic -> Variants).
- **Dynamic Image Loading**: Implemented dynamic resource resolution using `context.resources.getIdentifier`. Cards now correctly display artwork named `card_{id}_design.png`.

## 📦 Pack & Economy Mechanics
- **Pack Limit System**:
    - Initial capacity: 50 packs (Upgradable).
    - Regeneration rate: 1 pack every 30 minutes (Upgradable).
    - Stable Regeneration: Logic ensures timer pauses when full (no "staged" refills).
    - Real-time UI: Pack counter and regeneration timer update every second.
- **New Drop Rates**:
    - God: 0.5% | Legendary: 2.5% | Special: 5% | Epic: 15% | Rare: 37% | Common: 40%.
- **Basic Pack Slot Rules**: Slot 1 (Common), Slot 2 (Rare), Slots 3-5 (Free Roll with new rates).
- **Independent Free Pack**:
    - Cooldown: Reduced to 10 minutes (Upgradable).
    - Opening: Now occurs via a dedicated overlay within the Shop tab.
    - Rewards: Correctly awards cards + Gems (upgradable) + Coins (upgradable).

## 🛒 Shop & Upgrades
- **Massive Upgrade Expansion**:
    - **Global**: XP Multiplier (up to 10 tiers), Bulk Opening x5 (Unlocks at Level 5).
    - **Basic Pack**: Regeneration Speed, Storage Capacity, Card Count per pack, Card Rarity quality.
    - **Free Pack**: Cooldown reduction, Stored capacity, Gem yield, Coin yield, Card count, Card Rarity quality.
- **UI Improvements**:
    - Reddish tint on "UPGRADE" button when resources are insufficient.
    - Aligned "MAX" indicators for a cleaner professional look.

## ✨ Visuals & Animations
- **Spectacular Card Reveal**:
    - **Impact Entrance**: High-rarity cards "slam" onto the screen with scale/overshoot effects.
    - **Screen Shake**: Legendary and God cards trigger a haptic-style screen vibration on reveal.
    - **Particle System**: Physics-based burst of rarity-colored energy particles on Special+ reveals.
    - **Diagonal Shimmer**: Continuous "foil" shimmer overlay for premium cards.
- **Framed Art Style**:
    - Collection grid cards are now framed with a 2dp border matching their **Card Type color**.
    - Full-opacity artwork with no dimming.
    - Rarity icon moved to a discrete bottom-right bubble.

## 📱 User Experience & Architecture
- **Persistent Collector Identity**:
    - Renamed "Trainer" to **"Collector"**.
    - Fixed flickering/flashing of the setup screen on launch using a robust state machine.
    - Navigation bar is hidden until the user sets their name.
- **Summary Rewards**:
    - Final pack summary now shows a resource badge with the total Coins and Gems earned in that specific session.
- **Technical Refactor**:
    - **Stability**: Refactored `GameEngine` to use referential equality for state refreshes, preventing infinite save loops.
    - **Database**: Updated Room schema to Version 2 with destructive migration enabled.

## 📝 TODOs for v0.7
- [  ] Implement unique particle shapes/behaviors based on `CardType` (e.g., flames for Infernal, stars for Celestial).
- [  ] Add Dreamy/Oniric animated backgrounds for the pack opening screen.
- [  ] Implement Bulk Opening logic (rolling 25 cards at once).
- [  ] Localization support (Spanish/English toggle).
- [  ] Send notifications when the pack counter is full.
- [X] Enable a "dev settings" behaviour to allow better testing. This "dev settings" will set the same drop rates for every rarity (to test particle and effects) and will multiply the resources outcome x100, so i can buy the Shop Upgrades faster.
