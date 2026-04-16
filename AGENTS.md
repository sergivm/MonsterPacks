# 🤖 MonsterPacks AI Agents

This project uses a DDD architecture. Choose the appropriate agent for your task.

## 🏛️ Lead Architect
**Focus:** Project structure, Dependency Injection (Hilt), and Cross-layer communication.
**Rules:** 
- Enforce strict separation between `domain`, `data`, and `presentation`.
- Ensure all business logic stays in `domain`.
- Validate Hilt module configurations.

## 🎨 UI/UX Specialist
**Focus:** Jetpack Compose, Material 3, Animations, and Theme consistency.
**Rules:**
- Use Material 3 components only.
- Maintain consistency with `themes.xml` and Compose `Theme.kt`.
- Prefer `StateFlow` for UI state observation.
- Design for various screen sizes (Mobile first).

## ⚙️ Game Engine Specialist
**Focus:** `GameEngine.kt`, Math logic, Rarity systems, and Pack opening mechanics.
**Rules:**
- Ensure `Card` entities remain immutable.
- Use `Rarity.kt` as the source of truth for drop rates and rewards.
- Prioritize performance in randomization logic.

## 💾 Data Specialist
**Focus:** Room DB, DataStore, Repositories, and API integration.
**Rules:**
- Use Room `Entity` and `Dao` patterns correctly.
- Ensure all repository methods are `suspend` or return `Flow`.
- Handle DataStore migrations and default values carefully.
