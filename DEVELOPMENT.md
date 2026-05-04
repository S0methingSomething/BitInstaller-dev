# Development

## Local Phone Workflow

Use the local gate for fast feedback on the phone:

```sh
./gradlew qualityCheck --no-daemon
```

`qualityCheck` runs Spotless, detekt, unit tests, Android lint, and dependency analysis. It intentionally does not package an APK.

## Formatting

The project uses [Spotless](https://github.com/diffplug/spotless) with ktlint. Run `spotlessApply` to auto-format, `spotlessCheck` to verify.

When ktlint reports `max-line-length` on a line that measures under the limit with `wc -L` or `awk`, the issue is likely ktlint's format-then-lint pipeline: it checks the **post-format** output, not the file on disk. String concatenation (`+`) inside `when` blocks can be reflowed past the limit. Use `buildString { append(...) }` instead — ktlint can't reflow method call chains into single-line operators.

## Architecture

`BitInstallerAppPresenter` holds state management, lifecycle, and UI mapping extracted from the `BitInstallerApp` composable. The composable is now thin wiring that delegates to the presenter. Manifest recovery fires on Shizuku status transitions via `LaunchedEffect(snapshot.status)`.

Commits should stay atomic: one focused behavior, UI, build, or documentation change per commit. Avoid large mixed commits that combine unrelated UI, backend, and tooling changes.

## Pre-Commit

Install the hooks once per clone:

```sh
pre-commit install
```

The repo uses `.pre-commit-config.yaml` with a local hook that runs `qualityCheck` using conservative Termux-friendly Gradle settings. It does not build an APK.

## GitHub APK Builds

Heavy APK packaging runs in GitHub Actions through `.github/workflows/build-apk.yml`.

Trigger it by pushing to `main`, opening a pull request, or running the `Build APK` workflow manually. The workflow uploads `BitInstaller-debug-apk` as an artifact.

The debug APK workflow can use a stable debug signing key when these repository secrets are set:

- `BITINSTALLER_DEBUG_KEYSTORE_BASE64`
- `BITINSTALLER_DEBUG_STORE_PASSWORD`
- `BITINSTALLER_DEBUG_KEY_ALIAS`
- `BITINSTALLER_DEBUG_KEY_PASSWORD`

For a full build locally only when needed:

```sh
./gradlew fullBuildCheck --no-daemon
```

## Releases

Public alpha releases are tag-driven through `.github/workflows/release.yml`. Create a tag such as `v0.1.0-alpha` and push it; GitHub generates release notes from the commits since the previous tag.

The release workflow builds the R8-optimized `release` variant and requires these repository secrets:

- `BITINSTALLER_RELEASE_KEYSTORE_BASE64`
- `BITINSTALLER_RELEASE_STORE_PASSWORD`
- `BITINSTALLER_RELEASE_KEY_ALIAS`
- `BITINSTALLER_RELEASE_KEY_PASSWORD`

For a full release build locally only when needed:

```sh
./gradlew fullReleaseCheck --no-daemon
```
