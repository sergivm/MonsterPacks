# MonsterPacks - Project Context & Architecture

This document serves as a "shared memory" for Gemini to understand the project structure, design philosophy, and core mechanics.

## 🚀 Project Overview
**MonsterPacks** is a card-collection Android game built with modern Android practices.

## 🏗 Architecture & Tech Stack
- **Architecture:** Domain-Driven Design (DDD) with layers: `domain`, `data`, and `presentation`.
- **UI:** Jetpack Compose (Material 3).
- **Navigation:** Navigation Compose.
- **Dependency Injection:** Hilt.
- **Database:** Room (for card/collection data).
- **Preferences:** DataStore (for player settings/state).
- **State Management:** ViewModel + Kotlin Flows.

## 🃏 Core Domain Logic (Source of Truth)
- **`Card.kt`:** A static entity. Cards are definitions and never mutated. 
- **`Rarity.kt`:** Enum defining drop rates (Weights), rewards (Coins/Gems), and visual tiers (Common to God).
- **`PlayerState.kt`:** Tracks mutable progress (inventory, currency, levels).
- **`GameEngine.kt`:** Contains core mechanics like pack opening and reward calculation.

## 📐 Design Principles
1. **Immutability:** Use `data class` with `val` for domain models where possible.
2. **Separation of Concerns:** Keep UI (Compose) decoupled from business logic (Domain/Engine).
3. **Reactive UI:** ViewModels expose `StateFlow` for the UI to consume.

## 📝 Ongoing Notes & Roadmap
- [ ] Implement core pack opening logic in `GameEngine`.
- [ ] Connect `CollectionViewModel` to Room database.
- [ ] Design the "Open Pack" animation sequence.

---
*Note: This file is for AI context. Feel free to edit it as the project evolves!*
