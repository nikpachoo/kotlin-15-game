# Kotlin 15 Year Anniversary Game

A celebration of Kotlin's 15th birthday: a browser based 2D sandbox game written entirely in Kotlin, with a Kotlin backend for highscores.

## Projects

This repository contains two independent Gradle projects.

### [`game/`](./game) — Koita

A 2D sandbox game built with **Kotlin Multiplatform Compose** targeting **WebAssembly**. It runs directly in the browser and showcases what Kotlin/Wasm can do: procedural world generation, chunk based terrain, fluid simulation, particle effects, a sprite animated player, a roster of enemies with distinct AI, an unlock system with Kotlin themed names (Elvis Operator, Smart Casts, Coroutines…), ultimate attacks, a portal, and a final boss.

Run locally:

```shell
cd game
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

See [`game/README.md`](./game/README.md) for more build targets and [`game/CLAUDE.md`](./game/CLAUDE.md) for architecture details.

### [`highscore-server/`](./highscore-server) — Koita Server

A **Ktor 3** backend (Kotlin/JVM 21) that stores and serves highscores for Koita. Uses Exposed ORM against PostgreSQL (H2 for embedded/testing), kotlinx.serialization for JSON, and HTTP Basic Auth. Tests boot the full app against a Testcontainers Postgres instance.

Run locally:

```shell
cd highscore-server
./gradlew run      # starts on localhost:8080
./gradlew test
```

See [`highscore-server/CLAUDE.md`](./highscore-server/CLAUDE.md) for API and architecture details. A `Dockerfile` and `docker-compose.yml` are also provided.

## How To Play

* **Move:** `A` / `D` (or arrow keys)
* **Jump:** `Space` (tap again mid air once you unlock Double Jump, hold for Jetpack or Hover)
* **Dash:** `Shift` (after unlocking Dash)
* **Anchor / become invulnerable:** `S` (after unlocking Immutability)
* **Aim & attack:** move the mouse, **left click** to use your current weapon
* **Switch weapon / tool:** number keys `1` … for hotkey slots
* **Ultimate attack:** `R` (when one is available)
* **Pause:** `Esc`

Explore the world, mine resources with the pickaxe, build with the hammer, and fight enemies with the staff. Find shrines to pick one of three random unlocks, collect ultimate combos, pass through the portal, and face the final void.

## Tech Stack Highlights

* Kotlin Multiplatform + Compose Multiplatform (Wasm target)
* Ktor 3 + Exposed + PostgreSQL
* Gradle (Kotlin DSL) in both subprojects

Happy 15th, Kotlin!
