/// App configuration.
///
/// The Gemini API key is provided at build/run time via a Dart define:
///
///   flutter run --dart-define=GEMINI_API_KEY=your_key_here
///
/// (This replaces the Android `BuildConfig.GEMINI_API_KEY` / `.env` flow.)
class AppConfig {
  static const String geminiApiKey = String.fromEnvironment('GEMINI_API_KEY');

  /// Gemini model used for generation. The original project referenced
  /// `gemini-3.5-flash`, which is not a published model id; `gemini-2.0-flash`
  /// is a valid, fast default. Override with --dart-define=GEMINI_MODEL=...
  static const String geminiModel = String.fromEnvironment(
    'GEMINI_MODEL',
    defaultValue: 'gemini-2.0-flash',
  );

  static bool get hasApiKey =>
      geminiApiKey.isNotEmpty && geminiApiKey != 'MY_GEMINI_API_KEY';
}
