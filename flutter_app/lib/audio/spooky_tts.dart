import 'package:flutter/foundation.dart';
import 'package:flutter_tts/flutter_tts.dart';

/// Wraps flutter_tts with Sinhala locale + spooky pitch/rate tuning — the
/// `SpookyTextToSpeech` equivalent.
class SpookyTextToSpeech {
  final FlutterTts _tts = FlutterTts();
  final VoidCallback onInitSuccess;
  final VoidCallback onLineFinished;

  bool _isReady = false;
  bool get isReady => _isReady;

  double _currentPitch = 0.60;
  double _currentRate = 0.72;

  SpookyTextToSpeech({
    required this.onInitSuccess,
    required this.onLineFinished,
  }) {
    _init();
  }

  Future<void> _init() async {
    try {
      // Attempt Sinhala (si-LK); fall back to system default if unavailable.
      final available = await _tts.isLanguageAvailable('si-LK');
      if (available == true) {
        await _tts.setLanguage('si-LK');
      }

      await _tts.setPitch(_currentPitch);
      await _tts.setSpeechRate(_currentRate);

      // Fire-and-forget speak(); playback advances via the completion handler
      // (mirrors the Android UtteranceProgressListener.onDone flow).
      await _tts.awaitSpeakCompletion(false);
      _tts.setCompletionHandler(onLineFinished);
      _tts.setCancelHandler(() {});
      _tts.setErrorHandler((_) => onLineFinished());

      _isReady = true;
      onInitSuccess();
    } catch (e) {
      debugPrint('SpookyTTS init failed: $e');
    }
  }

  Future<void> setTuning(double pitch, double rate) async {
    _currentPitch = pitch;
    _currentRate = rate;
    if (_isReady) {
      await _tts.setPitch(pitch);
      await _tts.setSpeechRate(rate);
    }
  }

  Future<void> speak(String text) async {
    if (!_isReady) {
      onLineFinished();
      return;
    }
    // Strip bracket characters that produce odd vocalizations.
    final clean = text.replaceAll(RegExp(r'[\[\]]'), '');
    await _tts.speak(clean);
  }

  Future<void> stop() async {
    await _tts.stop();
  }

  Future<void> shutdown() async {
    await _tts.stop();
  }
}
