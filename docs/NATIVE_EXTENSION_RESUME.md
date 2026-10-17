# Optional native extension resume — experimental groundwork

The additive API lives in `library/src/commonMain/kotlin/com/lagradost/cloudstream3/resume` and is kept identical in the Eimon Android and Windows forks.

Normal build variants still advertise no native capabilities. The in-progress `stableNativeTest` variant enables READ_LIST/RESOLVE_RESUME for mobile testing, with registered-provider ownership checks and plugin-unload cleanup. Player progress dispatch remains unavailable. Do not disable a stock-host fallback just because the bridge class exists.

## Android test APK

### Unreleased: cloud title progress and Watching – New Episodes

Optional `TITLE_PROGRESS` / `NEW_EPISODES` capability negotiation enables provider-owned latest-episode cards and a cloud-backed series-page frontier without importing it into local history. Earlier same-season/same-edition episodes are inferred watched, the saved episode resumes at the cloud timestamp, and future episode spoilers are hidden by default; episode options can show/hide them. New Episodes launches the latest eligible same-edition episode at zero, independently of the saved frontier. Stock/older hosts retain the extension's ordinary row. Matching extension support and signed-in device acceptance tests are required; these changes are not yet released.

- Separate package `com.lagradost.cloudstream3.eimon`, label **Eimon Cloudstream Test**, debug-signed. It does not replace the official installation or migrate its private settings/session.
- Generic remote cache under `ui/resume`, separate from local history; bounded calls, pagination, account/profile invalidation, and source status cards. Existing Home shelves/cards and expanded lists are reused.
- Remote cards expose episode/audio/progress/remaining time, Resume, Open title page, and device-only Hide. No website deletion is offered.
- Fragment arguments carry bounded JSON metadata into the existing result/generator/player navigation. A typed initial seek is consumed once, preserving the normal completion/restart threshold and normal local player saves.
- Native resume explicitly uses the built-in player. Casting, downloads, and external-player paths are unchanged and do not gain native reporting.
- Remote account shelves are ordinary rows inside their owning provider's Home page, not global rows. Both provider name and plugin ownership must match the selected provider. Switching providers removes the other provider's account row; progress cards, notices, pagination and typed exact resume are retained. No website-specific logic is added to the host.
- AniKoto Account v8 supplies the adapter and owns login/audio policy. No Anikoto endpoints or settings were added to this app.

Build (JDK 17 toolchain, Android SDK 37, build tools 36.0.0 required):

```powershell
.\gradlew.bat :app:assembleStableNativeTest :app:testStableDebugUnitTest
```

On 2026-10-15 the `stableNativeTest` variant compiled and packaged successfully, and unit tests passed:
`./gradlew :app:assembleStableNativeTest :app:testStableDebugUnitTest` — BUILD SUCCESSFUL (JDK 17 toolchain,
Android SDK platform 37 + build-tools 36.0.0). Test suites: 15 tests, 0 failures (7 new `NativeResumeTest`
cases covering presentation parity, JSON transport, invalid inputs, episode/audio selection, completion
threshold, and unknown duration). This verifies compilation and helper logic, not on-device behaviour.

## Minimal host integration policy

- Reuse the existing local Continue Watching card presentation and resume/player flow.
- Add generic inputs for remote episode identity, exact position/duration, display metadata, and scoped callbacks.
- Preserve existing local-history behaviour; fetching remote entries must not create local watches.
- Keep provider HTTP/authentication logic, website matching, and **prefer dub over sub** settings inside the extension, not the app.
- A resolver may choose another audio edition of the requested episode/season, not another episode.
- Expose capabilities only after their UI/player behaviour is installed and tested.
- Preserve old plugin constructors/interfaces. An updated extension must load its optional adapter only after successful version/capability detection on a supported host.

## Current contract

- `RemoteResumeEntry`: stable account/item/episode identities, artwork, episode/season/audio, exact timestamp/duration, optional remote ordering metadata, and a bounded opaque resolver token.
- `ResumeProgress`: retains raw milliseconds while providing clamped seek position, known-duration progress fraction, and remaining time. Unknown duration remains unknown.
- `ContinueWatchingSource`: optional list, exact episode resolver, and player-progress handler.
- `ResumeProviderRegistry`: host-owned source keys, capability/version validation, per-source error isolation, account response validation, bounded calls, and cancellation/invalidation when a source is closed/unregistered.
- No credentials or expiring stream URLs belong in list records or logs. Cancellation cannot undo a network request already accepted by a remote server.

## Tests

```powershell
.\gradlew.bat :library:jvmTest --tests 'com.lagradost.cloudstream3.resume.*'
```

The tests cover version negotiation, raw/clamped timestamps, duration handling, malformed/cross-account records, provider isolation, timeout, caller cancellation, unregister cleanup, stale registration handles, and extension-controlled audio substitution. They do not constitute native UI, full-app, or stock-extension binary compatibility tests.

Next: install the test APK next to the official app (separate package, fresh login required), verify signed-in
native shelves and exact resume against extension v8, then port the same verification to stock-app compatibility.
No alternative player is planned.

## Test APK (2026-10-15)

- File: `dist/Eimon-Cloudstream-Test-4.8.0-native-test.apk` (87.7 MB, debug-signed test key, not for redistribution).
- Package `com.lagradost.cloudstream3.eimon`, label **Eimon Cloudstream Test**, version `4.8.0-Eimon-native-test`.
- Installs alongside the official app; does not migrate its settings/session. Add the Eimon + Phisher repos and sign in again inside the test app.
- Prerequisites installed during this build: Temurin JDK 17 (toolchain only), SDK `platforms;android-36`,
  `platforms;android-37.0` (copied to `android-37` for compileSdk 37), `build-tools;36.0.0`.
