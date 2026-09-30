# Building OTP Relay

This is a zero-config fork of SMS2Telegram: the Telegram bot token and chat ID
are baked in at build time, and the setup screen only asks for a device name
and phone number.

## Prerequisites

- **JDK 17** (Gradle 8.x does not run on newer JDKs for this project)
- **Android SDK** with: `platform-tools`, `platforms;android-36`,
  `build-tools;35.0.0` (or newer)
- **Gradle 8.13** distribution — note the repo has no `gradle-wrapper.jar`,
  so `./gradlew` will not work. Download the binary dist from
  https://gradle.org/releases/ and put `bin/gradle` on your PATH.
- Set `ANDROID_HOME` (or `ANDROID_SDK_ROOT`) to your SDK location.

## Build

The bot token and chat ID come from environment variables — they are compiled
into `BuildConfig` and never committed to the repo:

```bash
export OTP_RELAY_BOT_TOKEN="<bot-token-from-BotFather>"
export OTP_RELAY_CHAT_ID="<your-chat-id>"

gradle assembleDebug      # -> app/build/outputs/apk/debug/app-debug.apk
```

For a signed release build you also need a keystore:

```bash
# one-time: create a keystore
keytool -genkeypair -keystore otp-relay.keystore -alias otprelay \
  -keyalg RSA -keysize 2048 -validity 10950

export OTPRELAY_KEYSTORE=/path/to/otp-relay.keystore
export OTPRELAY_STORE_PASS="<store-password>"
export OTPRELAY_KEY_ALIAS=otprelay
export OTPRELAY_KEY_PASS="<key-password>"

gradle assembleRelease    # -> app/build/outputs/apk/release/app-release.apk
```

Keep the keystore and its passwords out of the repo.

## Build times

- **First build**: ~5–8 minutes (downloads all Gradle/Android dependencies).
  Point `GRADLE_USER_HOME` at a persistent directory so a fresh machine
  doesn't re-download everything.
- **Incremental builds**: ~1 minute.

## Troubleshooting

- **Java HTTPS fails / dependencies won't download**: the JDK truststore may be
  a dangling symlink. Regenerate it: `update-ca-certificates -f` (Linux).
- **Gradle daemon handshake fails** ("Unexpected type tag") on sandboxed
  networks: force IPv4 with `-Djava.net.preferIPv4Stack=true` in both
  `GRADLE_OPTS` (client) and `org.gradle.jvmargs` (daemon).
- **Manifest component names**: the Gradle `namespace` (`com.kashif.otprelay`)
  differs from the Kotlin package (`com.tigerworkshop.sms2telegram`). Always
  use fully-qualified class names in `AndroidManifest.xml` — relative names
  (`.ui.MainActivity`) resolve against the namespace and silently point at
  non-existent classes. In release builds R8 then strips all app code as
  unreachable.

## Building with Docker (no local Java/Gradle/SDK needed)

A `Dockerfile` is included that bundles JDK 17, the Android SDK (platform 36,
build-tools 36.0.0) and Gradle 8.13, so you don't install anything on your
machine except Docker.

```bash
# one-time: build the image (~5-10 min, ~1-2 GB)
docker build -t otp-relay-builder .

# debug APK — secrets are passed as env vars, never baked into the image
docker run --rm \
  -v "$PWD":/src \
  -v otp-gradle-cache:/opt/gradle-home \
  -e OTP_RELAY_BOT_TOKEN="<bot-token>" \
  -e OTP_RELAY_CHAT_ID="<chat-id>" \
  otp-relay-builder assembleDebug --no-daemon
# -> app/build/outputs/apk/debug/app-debug.apk
```

The named volume `otp-gradle-cache` persists downloaded dependencies between
runs: only the first build takes ~5-8 minutes, later builds take ~1 minute.

Release build (signed) — also mount your keystore and pass its passwords:

```bash
docker run --rm \
  -v "$PWD":/src \
  -v otp-gradle-cache:/opt/gradle-home \
  -v /path/to/otp-relay.keystore:/keystore/otp-relay.keystore:ro \
  -e OTP_RELAY_BOT_TOKEN="<bot-token>" \
  -e OTP_RELAY_CHAT_ID="<chat-id>" \
  -e OTPRELAY_KEYSTORE=/keystore/otp-relay.keystore \
  -e OTPRELAY_STORE_PASS="<store-password>" \
  -e OTPRELAY_KEY_ALIAS=otprelay \
  -e OTPRELAY_KEY_PASS="<key-password>" \
  otp-relay-builder assembleRelease --no-daemon
# -> app/build/outputs/apk/release/app-release.apk
```

## Distributing the APK

- A release-signed APK is the right artifact to share (smaller, optimized).
- Sideloaded apps requesting SMS permission are hard-blocked by Google Play
  Protect's enhanced fraud protection on ~2.8B devices: recipients must
  temporarily turn off **Play Store → profile → Play Protect → gear icon →
  "Scan apps with Play Protect"**, install, then turn it back on.
- Each phone needs working access to `https://api.telegram.org` — the app
  talks to Telegram's servers directly from the device. Where Telegram is
  network-blocked, messages queue in the outbox and retry but never deliver.
