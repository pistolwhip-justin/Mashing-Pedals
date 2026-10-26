# Mashing Pedals

**Current test release:** [Mashing Pedals 0.2.4 APK](https://github.com/pistolwhip-justin/Mashing-Pedals/releases/tag/v0.2.4) — GPS ride metrics, configurable metric sections, trip controls/history, global statistics, synchronized color preview, precise ride-panel overlays, borderless ride controls, and six module tabs.

Native Android cycling dashboard project for the Samsung Galaxy A15 5G.

The approved scope combines a persistent, customizable cycling-computer display with switchable in-app modules for Maps, Music, Health, and Settings, plus voice-activated controls for supported ride and media actions.

## Phase 1 — Dashboard foundation and settings

Phase 1 implements the native app shell, persistent cycling-computer placeholders, app-owned module navigation, saved theme/layout/metric visibility settings, and manual command entry for exercising command routing. Placeholder metrics are not live ride data.

## APK releases

- **Current test build:** [Download Mashing Pedals 0.2.4 APK](https://github.com/pistolwhip-justin/Mashing-Pedals/releases/download/v0.2.4/Mashing-Pedals-0.2.4.apk) · [Release notes](https://github.com/pistolwhip-justin/Mashing-Pedals/releases/tag/v0.2.4)
- Previous UI baseline: [Mashing Pedals 0.1.1 APK](https://github.com/pistolwhip-justin/Mashing-Pedals/releases/download/v0.1.1/Mashing-Pedals-0.1.1.apk)

## Project documentation

- [Approved development plan](DEVELOPMENT_PLAN.md)
- [Global development protocols](https://github.com/pistolwhip-justin/development-protocols-n-assets/blob/main/DEVELOPMENT_PROTOCOLS.md)

**Current status:** Version 0.2.4 automated tests passed, the debug APK assembled successfully, and the APK asset is published in GitHub Release v0.2.4. GPS behavior, trip accounting, and Gear S3 Bluetooth compatibility still require device testing. The Gear S3 connection currently supports the standard BLE Heart Rate Service only; cadence remains unavailable.

**Testing note:** APKs are CI debug-signed. Stable signing across releases is not configured, so Android may require uninstalling a previous build before installing this one. Uninstalling removes local app data.
