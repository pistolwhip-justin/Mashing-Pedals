# Mashing Pedals

**Current test release:** [Mashing Pedals 0.1.1 APK](https://github.com/pistolwhip-justin/Mashing-Pedals/releases/tag/v0.1.1) — landscape-only split layout, module workspace on the left, ride computer on the right, and true AMOLED-black dark theme.

Native Android cycling dashboard project for the Samsung Galaxy A15 5G.

The approved scope combines a persistent, customizable cycling-computer display with switchable in-app modules for Maps, Music, Health, and Settings, plus voice-activated controls for supported ride and media actions.

## Phase 1 — Dashboard foundation and settings

Phase 1 implements the native app shell, persistent cycling-computer placeholders, app-owned module navigation, saved theme/layout/metric visibility settings, and manual command entry for exercising command routing. Placeholder metrics are not live ride data.

## Phase 1 APKs

- **Current UI revision:** [Download Mashing Pedals 0.1.1 APK](https://github.com/pistolwhip-justin/Mashing-Pedals/releases/download/v0.1.1/Mashing-Pedals-0.1.1.apk) · [Release notes](https://github.com/pistolwhip-justin/Mashing-Pedals/releases/tag/v0.1.1)
- Previous baseline: [Mashing Pedals 0.1.0 APK](https://github.com/pistolwhip-justin/Mashing-Pedals/releases/download/v0.1.0/Mashing-Pedals-0.1.0.apk)

## Project documentation

- [Approved development plan](DEVELOPMENT_PLAN.md)
- [Global development protocols](https://github.com/pistolwhip-justin/development-protocols-n-assets/blob/main/DEVELOPMENT_PROTOCOLS.md)

**Current status:** The landscape/AMOLED UI revision is version 0.1.1. Automated tests, APK build, release publication, phone installation, and device behavior are tracked separately; phone testing remains pending.

**Testing note:** APKs are CI debug-signed. Stable signing across releases is not configured, so Android may require uninstalling a previous build before installing this one. Uninstalling removes local app data.
