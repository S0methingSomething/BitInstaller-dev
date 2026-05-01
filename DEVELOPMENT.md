# Development

## Local Phone Workflow

Use the local gate for fast feedback on the phone:

```sh
./gradlew qualityCheck --no-daemon
```

`qualityCheck` runs formatting, detekt, unit tests, Android lint, and dependency analysis. It intentionally does not package an APK.

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

For a full build locally only when needed:

```sh
./gradlew fullBuildCheck --no-daemon
```
