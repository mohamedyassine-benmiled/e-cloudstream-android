# e-cloudstream Android: instructions for coding agents

## Product and ownership

- This is `mohamedyassine-benmiled/e-cloudstream-android`; the displayed app name is **e-cloudstream**. `main` is the maintained branch.
- Fork features are conceived/architected by the repository owner and intentionally include website-specific behaviour. Do not prepare or submit upstream PRs for them unless explicitly asked. Preserve upstream credits, copyright notices and GPL licensing.
- Version labels no longer use `-eimon`, but the package remains `com.lagradost.cloudstream3.eimon` for installation/data compatibility. Preserve the separate identity from official CloudStream.

## Required before code changes: upstream sync

1. Read this file, `AI-POLICY.md`, the task and relevant design docs. Inspect `git status`, current branch and remotes. Preserve unrelated/uncommitted work; never reset, clean or overwrite it.
2. Fetch `upstream` (`https://github.com/recloudstream/cloudstream.git`) and compare the working branch with **`upstream/master`**, the original app's default branch. Check if upstream changes its default branch; do not follow an arbitrary feature branch.
3. Before implementing new code, merge missing upstream commits into the working branch using a normal, non-destructive merge. Never force-push or rewrite published fork history. If dirty files make this unsafe, ask or isolate work safely; do not silently stash/discard user work.
4. Review conflicts while retaining fork functionality. Stop and ask when the intended resolution is unclear. If the network is unavailable, report that synchronization is unverified rather than claiming it is current.
5. If no upstream commits are missing, record that result in the task report. After a merge, run relevant tests before proceeding. Record the upstream repository, branch and commit checked in the task report or `docs/UPSTREAM_SYNC.md`.

## Architecture and compatibility

- Android app/viewmodels: `app/src/main/java/com/lagradost/cloudstream3`; cloud integration: `ui/resume`; shared contracts: `library/src/commonMain/.../resume`. The fork build is `stableNativeTest`; branding resources are in `app/src/nativeTest`.
- Keep native API version 1 and existing data-class constructors binary compatible. Negotiate optional capabilities; stock/older hosts and older adapters must retain ordinary extension-row behaviour.
- Validate provider/plugin/profile/account/revision ownership. Native account rows belong to their provider, not a global Home shelf. Invalidate account-scoped caches on identity changes.
- Cloud progress is an in-memory projection for exact title/season/audio edition. Do not import inferred watches into local history. New Episodes must revalidate the newest same-edition target and start at zero, never reuse the old timestamp.
- Future episode spoilers are hidden by default with an episode-options toggle. Preserve phone and TV navigation/accessibility. Do not alter the extension's optional upload semantics without an explicit request.
- Private extension source is separate. Never publish it through this repository or bundle host API definitions in extension binaries. Keep API/registry contracts synchronized with Windows and the extension compile-only SDK when applicable.

## Data, signing and releases

- Never expose cookies, credentials, account identifiers, response bodies or raw preference backups. Do not replay old sessions or disturb an existing phone login. Session traces do not prove an opaque server's invalidation policy.
- Follow `docs/ANDROID_SIGNING.md`: published APKs must use the persistent private signing identity. Never generate a new key for a normal update, silently fall back to a debug key in CI, commit secret-value files, or cache signing credentials. Signing keys stay outside source/artifacts; configuration caching is disabled for signed release builds.
- Increment versionCode for new APK updates and retain the package identity. Switching from old ephemeral keys requires a one-time data-protected reinstall; never uninstall without permission and a usable backup. Saved extension sessions may not be covered by app export.
- Do not install/publish/delete releases/change GitHub settings without task authorization. The `native-test-apk.yml` workflow publishes unofficial prereleases from `main`; upstream secret-dependent release workflows are not this fork's release path.

## Validation and collaboration

- Follow surrounding Kotlin/resource conventions, add regression tests, keep changes scoped and explain limitations. Do not delegate to subagents unless asked.
- Baseline: `./gradlew :app:testStableDebugUnitTest :library:jvmTest --tests 'com.lagradost.cloudstream3.resume.*' :app:assembleStableNativeTest --no-configuration-cache --console=plain` (use `gradlew.bat` on Windows and configure a JDK 17 toolchain if needed).
- For signing/branding changes verify the final APK certificate, manifest label, package, versionName and versionCode. Test installed updates only with authorization. Phone/TV signed-in acceptance is separate from unit tests/build success.
- See `docs/NATIVE_EXTENSION_RESUME.md`, `docs/ANDROID_SIGNING.md`, `docs/UPSTREAM_SYNC.md` and `AI-POLICY.md`.
