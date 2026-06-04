# භීතිකා — Sinhala Horror Storyteller (Flutter)

A Flutter port of the original Android (Kotlin/Compose) "Sinhala Horror
Storyteller & Audio Mixer". It generates gripping Sinhala horror stories with
the Gemini API, narrates them with deep-voiced Text-to-Speech, and mixes in
procedurally-synthesized background drones and sound effects.

## Features

- **Create** tab — AI generator (location, length, terrifying element) plus a
  manual writer with one-tap `[BGM:…]` / `[SFX:…]` cue insertion.
- **Listen** tab — immersive player: animated waveform visualizer, per-line
  highlighting, voice cycling (Deep Shadow / High Witch / Whispering Ghoul /
  Spectral Normal), timeline, and transport controls.
- **Library** tab — saved-story anthology with favorite, share/export, delete.

## Running

The Gemini API key is supplied at run/build time via a Dart define (this
replaces the Android `BuildConfig.GEMINI_API_KEY` / `.env` flow).

**Recommended:** keep the key in a gitignored `dart_define.json` (next to
`pubspec.yaml`) so it never lands in source control:

```json
{
  "GEMINI_API_KEY": "your_key_here"
}
```

```bash
flutter run --dart-define-from-file=dart_define.json
```

`dart_define.json` is listed in `.gitignore`. The Android Studio `main.dart`
run config already passes `--dart-define-from-file=dart_define.json`, so the
Run button works once the file exists (reload the project to pick it up).

Alternatively, pass the key (and optionally the model, default
`gemini-2.0-flash`) inline:

```bash
flutter run \
  --dart-define=GEMINI_API_KEY=your_key_here \
  --dart-define=GEMINI_MODEL=gemini-2.0-flash
```

Without a key, the AI generator surfaces an error but the rest of the app
(manual stories, the two prepopulated samples, playback, TTS, audio) works.

## Architecture (mapping from the original)

| Android (Kotlin)            | Flutter (Dart)                                   |
| --------------------------- | ------------------------------------------------ |
| `HorrorStoryViewModel`      | `state/horror_story_view_model.dart` (`ChangeNotifier` + `provider`) |
| Room (`AppDatabase`/DAO)    | `data/story_database.dart` (`sqflite`)           |
| Retrofit `GeminiApiService` | `api/gemini_service.dart` (`http`)               |
| `NarrativeParser`           | `audio/narrative_element.dart`                   |
| `SpookyAudioEngine` (`AudioTrack` PCM) | `audio/spooky_audio_engine.dart` (WAV synth + `audioplayers`) |
| `SpookyTextToSpeech`        | `audio/spooky_tts.dart` (`flutter_tts`)          |
| Compose `StorytellerApp`/tabs | `ui/*.dart`                                    |
| Compose theme colors        | `theme/app_colors.dart`                          |

## Build notes

`android/app/build.gradle.kts` sets `minSdk = 24` (flutter_tts), `compileSdk = 36`
(flutter_tts), and pins `ndkVersion = 28.2.13676358`. Kotlin Gradle plugin is
`2.1.0`.
