# Releasing

The release APK is built and published by the
[`Release APK`](../.github/workflows/release.yml) workflow. It builds a single
universal APK from the `apt-android-7` bootstrap, which works on every supported
CPU architecture on Android 7.0 and newer, verifies its signature and attaches it
to a GitHub Release that users can download from. The APK's SHA-256 checksum goes
into the release notes.

Only one APK is published deliberately: a universal APK removes any need for
users to work out which ABI their device has, and the `apt-android-5` bootstrap
(Android 5.0 - 6.0) is not built since it no longer receives package updates
upstream. To publish it as well, add a `TERMUX_PACKAGE_VARIANT=apt-android-5`
build to the workflow.

## Publishing a release

**Tagged release.** Push a semver tag and the workflow does the rest:

```bash
git tag v0.118.0
git push origin v0.118.0
```

The tag must match `v<major>.<minor>.<patch>` and should agree with the
`versionName` in [`app/build.gradle`](../app/build.gradle). Remember to bump both
`versionCode` and `versionName` there before tagging, otherwise Android will
refuse to install the new APK over the old one.

**Snapshot release.** Run the workflow manually from the *Actions* tab. It
publishes a pre-release tagged `v<versionName>+<short commit hash>` built from
whatever the branch currently points at, without needing a tag.

## Signing

The workflow signs with `app/testkey_untrusted.jks` unless a keystore is
configured. That key is public — it is checked into this repository and is the
same one upstream Termux uses for its GitHub builds. That is deliberate:

* Termux and its plugin apps (Termux:API, Termux:Widget, Termux:Boot, ...) share
  a `sharedUserId`, so Android requires them to carry the *same* signature.
  Signing with the upstream test key keeps plugin APKs from upstream installable
  alongside APKs built here.
* An unsigned APK cannot be installed at all, so there has to be some fallback.

It also means anyone can produce an APK that Android accepts as an update to
this one, so it offers no protection against tampering — this is why the release
notes tell users to check `sha256sums.txt`.

### Using your own key

Only do this if you do not need compatibility with upstream-built plugin apps.
Once you publish a release under your own key you cannot go back: users have to
uninstall and reinstall to switch signatures, losing `$HOME` and `$PREFIX`.

Generate a keystore:

```bash
keytool -genkey -v -keystore release.jks -alias termux \
  -keyalg RSA -keysize 4096 -validity 10000
```

Add these four repository secrets under *Settings → Secrets and variables →
Actions*:

| Secret | Value |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | `base64 -w0 release.jks` |
| `ANDROID_KEYSTORE_PASSWORD` | The keystore password |
| `ANDROID_KEY_ALIAS` | The key alias (`termux` above) |
| `ANDROID_KEY_PASSWORD` | The key password |

The workflow picks them up automatically on the next run. Keep `release.jks`
somewhere safe and out of the repository — losing it means you can never ship an
update to anyone who installed a release signed with it.

## Building a release APK locally

```bash
./gradlew assembleRelease
```

This signs with the test key, same as CI. To use your own keystore, point the
build at it through the environment:

```bash
export TERMUX_RELEASE_KEYSTORE=/path/to/release.jks
export TERMUX_RELEASE_KEYSTORE_PASSWORD=...
export TERMUX_RELEASE_KEY_ALIAS=termux
export TERMUX_RELEASE_KEY_PASSWORD=...
./gradlew assembleRelease
```

Release builds produce a single universal APK by default, because F-Droid does
not support split APKs ([#1904](https://github.com/termux/termux-app/issues/1904)).
The release workflow keeps that default. Set
`TERMUX_SPLIT_APKS_FOR_RELEASE_BUILDS=1` if you want per-ABI APKs as well, which
are smaller but require users to pick the right one.
