# Mashing Pedals — Development Plan

**Status:** Phase 1 implementation and UI refinement are released for device acceptance testing  
**Plan baseline:** 2026-10-25  
**Repository:** https://github.com/pistolwhip-justin/Mashing-Pedals  
**Global development standard:** [Development Protocols](https://github.com/pistolwhip-justin/development-protocols-n-assets/blob/main/DEVELOPMENT_PROTOCOLS.md) (v1.1.1 at plan approval)

## 1. Purpose

Build a native Android cycling dashboard for the user's Samsung Galaxy A15 5G. It combines a persistent, customizable cycling-computer display with switchable feature modules and voice-activated controls.

The app's own cycling-computer layer remains visible while the user switches between modules implemented inside this app. The dashboard must not assume it can keep that layer over arbitrary third-party apps; integrations must use supported APIs, app-owned module screens, or an explicit handoff to another app.

## 2. Approved scope and requirements

### Core requirements

- Native Android implementation, using Kotlin unless repository inspection identifies a concrete reason to change.
- A persistent cycling-computer layer showing configurable ride metrics, including GPS-derived speed, distance, and elapsed time.
- Ride-state controls: start, pause, resume, stop/end, and reset, with clear state feedback. Destructive reset must be guarded against accidental activation.
- User-customizable dashboard theme, layout, and visibility of individual data points; preferences persist across launches.
- An in-app module area/switcher for moving between Maps, Music, Health, and Settings without discarding the active ride state.
- Voice activation for supported app actions, including cycling-computer controls, module switching, and supported music controls such as play/pause, stop where supported, next, and previous. Command availability must be explicit; unsupported actions must not be falsely reported as completed.
- A shared command path that routes recognized phrases to validated actions and provides feedback. Voice control should be usable without relying on a touch-only flow for routine riding actions.
- Prefer on-device wake-phrase detection and evaluate offline speech recognition because the target phone is Wi-Fi-only and may be used without a network connection.
- Each development phase produces an installable test APK attached to its corresponding GitHub Release, subject to the release gates below.
- Keep the repository self-explanatory for a future AI coding agent and follow the global development protocols.

### Integration targets

- **Maps:** an app-owned map module using an appropriate map SDK/data source; current position and route/navigation capabilities to be evaluated. External navigation may be handed off to another app where necessary.
- **Music:** first investigate Android media-session integration with Pulsar. Evaluate Spotify's supported SDK/integration path separately; do not assume either app's complete native UI can be embedded.
- **Health:** investigate Samsung Health and/or Health Connect based on current supported APIs, permissions, approval requirements, and whether data is available during an active ride. Do not present historical measurements as live readings.

### Constraints and exclusions

- Target: Android, with the Samsung Galaxy A15 5G as the initial device for user testing.
- The phone is Wi-Fi-only/no mobile data. Core cycling-computer functions and voice-command fundamentals should not require internet when technically feasible. Map downloads, service authorization, or third-party integrations may still require connectivity.
- Do not make screen wake-up a requirement.
- Do not promise that a third-party app's full UI can be embedded or that the dashboard can remain over arbitrary external apps.
- Do not assume Samsung Health provides real-time heart rate through a particular API until verified against current documentation and device behavior.
- Do not add unrelated features or paid dependencies without explicit approval.
- This plan does not authorize Play Store publication or production signing. Initial APKs are for direct installation/testing and may be debug-signed.

## 3. Architecture direction

1. **Dashboard shell:** owns the persistent cycling-computer area, module switcher, settings entry, and app-level status/feedback.
2. **Ride data/state layer:** a single source of truth for speed, distance, elapsed time, and ride state. GPS tracking must be managed so it can continue with the screen off when permissions and Android restrictions permit.
3. **Module interfaces:** Maps, Music, Health, and Settings are app-owned modules. Module changes must not reset or duplicate the active ride state.
4. **Voice pipeline:** wake-phrase/listening service → speech recognition → command parsing/mapping → validated command dispatcher → module/ride action → confirmation or clear failure feedback. Reuse components from [Android Wake Phrase Service](https://github.com/pistolwhip-justin/Android-Wake-Phrase-Service) only after inspecting their current implementation and suitability; do not duplicate or transplant blindly.
5. **Integration adapters:** keep external APIs and media-session/health/map data sources behind clear interfaces so an unavailable integration does not break the dashboard.
6. **Privacy and permissions:** request location, microphone, notification-listener, health, or service permissions only when required, explain their purpose, and provide visible listening/recording state where applicable.

This is an architecture direction, not a claim that these components are already implemented. Validate current Android, Samsung, Spotify, map SDK, and data-source documentation before committing to specific APIs.

## 4. Phase plan and readiness gates

Each phase is a usable/testable increment. Do not begin the next phase until the current phase's gate is met or the user explicitly approves a documented exception. Every phase must have a dedicated version-matched GitHub Actions release workflow and a versioned GitHub Release containing its installable APK.

### Phase 1 — Dashboard foundation and settings
**Target version:** 0.1.0

**Deliverables**
- Native app skeleton and dashboard shell.
- Persistent cycling-computer layout with clearly labeled placeholder values until live data is implemented.
- Initial theme, layout, and show/hide metric settings, with preferences persisted.
- Placeholder module screens and working in-app module navigation.
- Central command-dispatch architecture and a visible/manual voice-control entry point to test the interaction path.
- Basic project/build instructions and initial tests.

**Test and readiness gate**
- Debug APK builds and installs on the target device.
- Dashboard renders at target screen dimensions; navigation among modules works.
- Settings survive app restart and visibly affect the dashboard.
- Placeholder values are clearly distinguished from real measurements.
- Automated tests available for settings persistence and command dispatch pass; release workflow publishes the APK successfully.

### Phase 2 — GPS cycling computer and ride commands
**Target version:** 0.2.0

**Deliverables**
- GPS-derived speed and distance, plus elapsed ride time.
- Start, pause, resume, stop/end, and reset state transitions.
- Foreground location/tracking behavior and required permission handling.
- Ride state retained while navigating among in-app modules.
- Voice commands for supported ride-state actions, initially through a testable listening/recognition path.

**Test and readiness gate**
- Compare speed/distance behavior against a reasonable real-world ride or independent reference.
- Verify each state transition, including pause/resume and reset safeguards.
- Confirm the active ride survives module changes.
- Test screen-off tracking and recovery from permission denial or location loss.
- Automated state/command tests pass; APK is installed and user-testable.

### Phase 3 — Hands-free wake phrase and voice engine
**Target version:** 0.3.0

**Deliverables**
- Background wake-phrase detection integrated with the dashboard's command pipeline.
- Speech command recognition and mapping for the commands implemented so far.
- Audible and/or spoken confirmation, plus clear listening-state controls and status.
- Service lifecycle/recovery behavior and documented privacy/battery behavior.
- Evaluate and, if suitable, reuse components from Android Wake Phrase Service.

**Test and readiness gate**
- Test on the home screen, inside the dashboard, with the screen off, and on the lock screen where Android permits.
- Record false activations, missed detections, recognition errors, battery impact, and behavior after service interruption.
- Verify unavailable/offline recognition behavior and make network requirements explicit.
- Do not treat lock-screen behavior as guaranteed: Android background microphone, authentication, and launch restrictions may prevent some actions until unlock.
- APK is installable; user tests the actual device behavior before this phase is considered complete.

### Phase 4 — Module navigation and Maps
**Target version:** 0.4.0

**Deliverables**
- Functional Maps module with an appropriate embedded map implementation.
- Current-position display and a scoped initial route/navigation capability, based on verified SDK/data-source support.
- Voice commands to switch modules and invoke supported map actions.
- External navigation handoff where an embedded implementation cannot provide the required behavior.
- Investigation/documentation of offline map feasibility, data licensing, attribution, and storage needs.

**Test and readiness gate**
- Map renders, location updates, and module switching does not reset ride state.
- Verify behavior with weak/no network and clarify which map functions require downloaded data or connectivity.
- Confirm required attribution and permissions are handled.
- Test voice navigation commands and external handoff where included.
- APK is released and user-testable.

### Phase 5 — Music module and voice commands
**Target version:** 0.5.0

**Deliverables**
- Music module showing supported playback state and track metadata.
- Media-session integration with Pulsar investigated and implemented where supported.
- Spotify integration evaluated against its current official SDK, authorization, and policy requirements; include only the supported, feasible scope.
- Voice commands for supported play/pause, stop, next, and previous actions; volume only if a reliable supported route is available.
- Clear feedback when no compatible player is active or a requested action is unsupported.

**Test and readiness gate**
- Test with Pulsar and, if implemented, Spotify on the target device.
- Verify metadata updates and each supported playback command, including when the screen is off.
- Verify permissions/authorization and failure behavior.
- Never claim an action succeeded without observing the resulting state or a reliable API result.
- APK is released and user-testable.

### Phase 6 — Health and additional ride data
**Target version:** 0.6.0

**Deliverables**
- Health data source selected after current API and device validation.
- Heart-rate or other approved health metrics displayed only when the source actually provides them.
- Data source, freshness, permission state, and unavailable/stale states made clear.
- Voice commands to open the relevant module or query supported metrics, if reliable and in scope.

**Test and readiness gate**
- Verify actual readings during an active ride; historical health records alone do not satisfy a live-data requirement.
- Document Samsung Health/Health Connect compatibility, required consent/approval, and any API limitations.
- Test denied permissions, stale data, disconnected sources, and missing values.
- APK is released and user-testable.

### Phase 7 — Customization and unified voice controls
**Target version:** 0.7.0

**Deliverables**
- Complete the agreed theme, layout, and metric visibility settings across supported dashboard layouts.
- Consistent voice command routing across ride controls and implemented modules.
- Custom voice aliases where practical without compromising predictable command recognition.
- Voice commands for supported module switching and interface controls; clear response to unknown or ambiguous commands.
- Finalize the dashboard's persistent cycling-computer presentation while switching among the app's own modules.

**Test and readiness gate**
- Verify customization persistence and usability across supported layouts.
- Exercise voice commands across modules, including ambiguous commands, unavailable modules, and false activations.
- Confirm that switching modules never corrupts ride state.
- Confirm unsupported actions are reported honestly.
- APK is released and user-testable.

### Phase 8 — Field testing and stabilization
**Target version:** 1.0.0

**Deliverables**
- Real-ride test pass and fixes for reproducible critical issues.
- Battery, GPS accuracy, background operation, wake phrase, command reliability, and recovery review.
- Regression test pass for core ride states, customization, module switching, Maps, Music, and available Health integration.
- Final setup/build/release instructions, known limitations, permissions/privacy notes, and test record.
- Versioned 1.0.0 GitHub Release with installable APK.

**Test and readiness gate**
- The agreed field-test checklist passes, or remaining failures are explicitly documented and accepted by the user.
- No known data-loss or ride-state corruption issue remains unresolved.
- Release workflow, automated tests, release asset, and release notes are verified.
- User installation and device behavior are recorded separately from automated build/test results. A successful CI build alone does not establish real-world readiness.

## 5. Release and build protocol

Follow the Android APK release rules in the global [Development Protocols](https://github.com/pistolwhip-justin/development-protocols-n-assets/blob/main/DEVELOPMENT_PROTOCOLS.md#71-android-apk-build-and-release-process).

- Use the repository's Gradle wrapper and a GitHub Actions runner with an explicitly configured supported JDK/Android toolchain.
- Create a dedicated workflow for each phase version, for example .github/workflows/release-0.1.0.yml, release-0.2.0.yml, and so on.
- Keep workflow version, release tag/title, APK filename, and app version metadata aligned.
- Run available automated tests as well as APK assembly.
- Publish the APK as an asset on the corresponding versioned GitHub Release; do not commit APK binaries to the source tree.
- Use debug signing for sideload/testing unless a separately approved signing requirement is established. Do not imply Play Store readiness.
- Release workflows must be safe to retry: an existing complete release is a no-op; an inconsistent existing release fails safely; never overwrite an immutable release.
- In status reports, distinguish: source changes, automated tests, APK build, GitHub Release publication, user installation, and real-device behavior testing.
- Each phase is considered delivered only when its release asset and release notes are verified. Device-only checks remain pending until actually tested.

## 6. Test strategy

- Add automated tests alongside implementation for ride-state transitions, distance/time calculations where practical, customization persistence, command parsing/routing, and integration-adapter failure states.
- Run automated tests and APK assembly in each release workflow.
- Perform device tests on the Samsung Galaxy A15 5G for layout, permissions, GPS, background behavior, media control, voice detection, and integration compatibility as features arrive.
- Keep a phase-specific checklist of expected behavior, results, failures, and retests.
- Never substitute an automated build for user installation or real-world ride testing.

## 7. Initial risks and unresolved decisions

These are investigation items, not confirmed blockers or implemented features:

1. **Background microphone and lock screen:** Android restrictions may limit wake-word detection, recognition, or launching actions while locked.
2. **Offline speech recognition:** supported languages, model availability, latency, and device resource use need on-device testing.
3. **Battery use:** continuous location and wake-word listening may materially affect ride duration.
4. **Map implementation:** select a map SDK/data source after evaluating offline use, licensing, attribution, routing support, and maintenance.
5. **Health data:** verify whether Samsung Health or Health Connect exposes the required active-ride measurements and what approvals/permissions are needed.
6. **Music integrations:** Pulsar's media-session behavior and Spotify's current integration/auth requirements must be tested; arbitrary UI embedding is not assumed.
7. **Voice command safety:** destructive ride reset and ambiguous commands need safeguards; command feedback must reflect actual outcomes.
8. **Release infrastructure:** no app implementation or release workflow is present at plan approval; confirm build tooling and repository structure before phase 1 implementation.

## 8. Decision record

- **MP-DEC-001 — Approved phased delivery:** The user approved a phased development plan in which every phase delivers an installable APK for testing.
- **MP-DEC-002 — Persistent dashboard and switchable modules:** The cycling-computer data area remains static/persistent and customizable while the user switches among modules inside this app. This does not imply overlaying the dashboard over arbitrary third-party apps.
- **MP-DEC-003 — Voice-activated controls:** Actions such as ride start/stop/pause/reset and supported music controls must be voice-activated. Support and success feedback depend on what the platform/player actually permits.
- **MP-DEC-004 — Initial device/network constraint:** The initial target is the Samsung Galaxy A15 5G; the phone is Wi-Fi-only, so offline-capable core behavior is prioritized where feasible.

These decisions reflect the approved plan as of 2026-10-25. Future changes should update the current requirements and preserve superseded decisions rather than silently erasing them.


## 9. Optional project infrastructure

The initial-project defaults in the shared development prompt are recorded as follows. These are infrastructure choices, not additional product features.

| Infrastructure | Selection | Application to this repository |
|---|---|---|
| GitHub Projects / ProjectV2 board | NO | Do not create a project board unless requested later. |
| Formal resource planning | NO | Do not add a separate resource-planning document unless requested or needed by a later approved decision. |
| Formal knowledge management / AI-chat archive | NO | Do not create a private conversation archive; keep the product decisions required for development in this plan. |
| Formal phase-based development plan | YES | This document defines phases and readiness gates. |
| Formal research / data provenance system | NO | Research map, health, and SDK choices as required, but do not create a separate provenance subsystem unless approved. |
| Separate formal testing / validation plan | NO | Testing and acceptance criteria are maintained in this development plan and phase records unless later requested. |
| Deployment / release management | YES | Follow the explicit per-version GitHub Actions and GitHub Release process in Section 5. |
| Public documentation / knowledge vault | NO | Keep project documentation in this repository; no separate public documentation site is planned. |
| Separate security / privacy document | NO | Apply Android security and privacy requirements; do not add a standalone security document unless a later review shows it is necessary and scope is agreed. |

These selections may be changed by the user later. A change that adds infrastructure must be recorded before implementing it.


## 10. Phase 1 execution record

**Target release:** `0.1.0`

- **Scope implemented in source:** dashboard shell, persistent placeholder metric panel, app-owned module navigation, saved theme/layout/metric visibility settings, and typed command entry through the shared command dispatcher.
- **Not included:** live GPS, real ride-state management, always-listening wake phrase, live maps/music/health integrations, screen wake-up, or production signing.
- **Build/test gate:** GitHub Actions runs unit tests and assembles the debug APK from committed source using the repository Gradle wrapper. The release workflow publishes `Mashing-Pedals-0.1.0.apk` to immutable release `v0.1.0` only if the tag does not already exist.
- **Device acceptance checks:** install on Samsung Galaxy A15 5G; confirm module navigation; change theme/layout/metric visibility; force-close/reopen and confirm settings persist; verify placeholders are not presented as real data; try module commands and an unsupported ride command; report UI/UX issues as testing feedback.
- **Verification status:** source is committed; GitHub Actions automated tests passed and `assembleDebug` succeeded on commit `4e03cb77d30f06e3b7dc191897ba3f7d392e4e39`; immutable GitHub Release `v0.1.0` was published with the `Mashing-Pedals-0.1.0.apk` asset. Physical-device installation and behavior are not yet verified.
- **Signing caveat:** CI currently uses its ephemeral Android debug-signing key. Stable signing across phase releases is not configured; later APKs may require uninstalling the previous build before installing the next, which deletes local app data. A persistent test keystore supplied securely through GitHub Actions secrets would be needed for seamless in-place upgrades.


## 11. Phase 1 UI revision — 0.1.1

**User-approved design requirements (2026-10-25):**
- Force the app into landscape orientation.
- Use a split-screen layout: the module workspace occupies the left two thirds and the persistent ride computer occupies the right third.
- Keep module selection buttons in a single horizontal row at the bottom of the left workspace, beneath the module content area.
- Make the dark theme true AMOLED black (#000000) for the main background and dashboard surface, using subtle dark-gray card fills and borders for separation.

**Release:** v0.1.1, APK asset Mashing-Pedals-0.1.1.apk, with a dedicated version-matched GitHub Actions workflow. This is a UI/layout refinement only; live GPS, ride execution, wake phrase, and Maps/Music/Health integrations remain out of scope for Phase 1.

**Device acceptance:** Install the new APK on the Galaxy A15 5G; confirm landscape-only behavior, the left 2/3 and right 1/3 split, bottom horizontal module selector, AMOLED-black appearance, module switching, and settings persistence. The previous release uses a different debug-signing key, so Android may require uninstalling it first; uninstalling clears app data.


### 0.1.1 verification record

- Automated tests: passed in GitHub Actions.
- APK assembly: passed.
- GitHub Release: v0.1.1 published; the Mashing-Pedals-0.1.1.apk asset is uploaded and verified.
- Workflow: https://github.com/pistolwhip-justin/Mashing-Pedals/actions/runs/40234083015
- Release: https://github.com/pistolwhip-justin/Mashing-Pedals/releases/tag/v0.1.1
- Phone installation and on-device layout/theme behavior: pending user test; not claimed as verified.
