import 'dart:math' as math;
import 'dart:typed_data';

import 'package:audioplayers/audioplayers.dart';

/// Procedurally synthesizes the background drone and one-shot sound effects as
/// PCM/WAV and plays them through audioplayers — the `SpookyAudioEngine`
/// equivalent (Android's raw `AudioTrack` has no Flutter counterpart, so we
/// build WAV byte buffers instead).
class SpookyAudioEngine {
  static const int _sampleRate = 22050;
  final math.Random _rng = math.Random();

  final AudioPlayer _bgmPlayer = AudioPlayer(playerId: 'spooky_bgm');
  final AudioPlayer _sfxPlayer = AudioPlayer(playerId: 'spooky_sfx');

  bool _bgmActive = false;

  // --- Background music: low multi-tone spooky drone, looped ---

  Future<void> startBgm(String type) async {
    await stopBgm();
    _bgmActive = true;

    // One full swell-modulation period (0.15 Hz -> ~6.667s) loops seamlessly.
    const double durationSeconds = 1.0 / 0.15;
    final int numSamples = (_sampleRate * durationSeconds).round();
    final samples = Int16List(numSamples);

    for (var i = 0; i < numSamples; i++) {
      final t = i / _sampleRate;
      const freq1 = 55.0;
      const freq2 = 57.0;
      final modulation = 0.55 + 0.45 * math.sin(2.0 * math.pi * 0.15 * t);
      final value =
          (math.sin(2.0 * math.pi * freq1 * t) +
              math.sin(2.0 * math.pi * freq2 * t)) /
          2.0;
      final floatSample = value * 0.25 * modulation;
      final noise = (_rng.nextDouble() * 2.0 - 1.0) * 0.012;
      samples[i] = _toPcm16(floatSample + noise);
    }

    try {
      await _bgmPlayer.setReleaseMode(ReleaseMode.loop);
      await _bgmPlayer.play(BytesSource(_buildWav(samples), mimeType: 'audio/wav'));
    } catch (_) {
      // Ignore playback failures (e.g. no audio device).
    }
  }

  Future<void> stopBgm() async {
    _bgmActive = false;
    try {
      await _bgmPlayer.stop();
    } catch (_) {}
  }

  bool get isBgmActive => _bgmActive;

  // --- One-shot sound effects ---

  Future<void> playSfx(String type) async {
    final lower = type.toLowerCase();
    final double durationSeconds;
    if (lower.contains('scream')) {
      durationSeconds = 1.5;
    } else if (lower.contains('creak')) {
      durationSeconds = 1.3;
    } else if (lower.contains('wind') || lower.contains('rain')) {
      durationSeconds = 2.0;
    } else if (lower.contains('heartbeat')) {
      durationSeconds = 1.6;
    } else if (lower.contains('whisper')) {
      durationSeconds = 1.8;
    } else if (lower.contains('shatter')) {
      durationSeconds = 0.8;
    } else {
      durationSeconds = 1.0;
    }

    final numSamples = (_sampleRate * durationSeconds).round();
    final samples = Int16List(numSamples);

    for (var i = 0; i < numSamples; i++) {
      final t = i / _sampleRate;
      double sample;
      if (lower.contains('scream')) {
        final freq = 700.0 + 250.0 * math.sin(2.0 * math.pi * 30.0 * t) - 300.0 * t;
        final fadeOut = (1.0 - t / durationSeconds).clamp(0.0, 1.0);
        final noise = (_rng.nextDouble() * 2.0 - 1.0) * 0.12;
        final sweep = math.sin(2.0 * math.pi * freq * t) * 0.20;
        sample = (sweep + noise) * fadeOut;
      } else if (lower.contains('creak')) {
        const baseFreq = 160.0;
        final jitter = math.sin(2.0 * math.pi * 55.0 * t) * 25.0;
        final freq = baseFreq + jitter + (t * 40.0);
        final amplitude = (t * (durationSeconds - t) * 1.5).clamp(0.0, 1.0);
        sample = math.sin(2.0 * math.pi * freq * t) * 0.14 * amplitude;
      } else if (lower.contains('heartbeat')) {
        final pulse1 = math.sin(2.0 * math.pi * 50.0 * t) * math.exp(-16.0 * t);
        final t2 = t - 0.45;
        final pulse2 =
            t2 > 0 ? math.sin(2.0 * math.pi * 50.0 * t2) * math.exp(-16.0 * t2) : 0.0;
        sample = (pulse1 + pulse2 * 0.85) * 0.55;
      } else if (lower.contains('whisper')) {
        final noise = (_rng.nextDouble() * 2.0 - 1.0) * 0.08;
        final hiss = math.sin(
              2.0 * math.pi * (1200.0 + 300.0 * math.sin(2.0 * math.pi * 4.0 * t)) * t,
            ) *
            0.015;
        final fadeOut = (1.0 - t / durationSeconds).clamp(0.0, 1.0);
        sample = (noise + hiss) * fadeOut;
      } else if (lower.contains('shatter')) {
        final noise = (_rng.nextDouble() * 2.0 - 1.0) * 0.15;
        final ring = math.sin(2.0 * math.pi * 1800.0 * t) * 0.05;
        sample = (noise + ring) * math.exp(-22.0 * t);
      } else {
        // Default howling wind / rain rumble.
        final noise = (_rng.nextDouble() * 2.0 - 1.0) * 0.05;
        final rum = math.sin(2.0 * math.pi * 70.0 * t) * 0.03;
        sample = (noise + rum) * (1.0 - t / durationSeconds);
      }
      samples[i] = _toPcm16(sample);
    }

    try {
      await _sfxPlayer.stop();
      await _sfxPlayer.setReleaseMode(ReleaseMode.stop);
      await _sfxPlayer.play(BytesSource(_buildWav(samples), mimeType: 'audio/wav'));
    } catch (_) {}
  }

  Future<void> shutdown() async {
    await stopBgm();
    await _bgmPlayer.dispose();
    await _sfxPlayer.dispose();
  }

  // --- Helpers ---

  int _toPcm16(double sample) {
    final clamped = sample.clamp(-1.0, 1.0);
    return (clamped * 32767).round();
  }

  /// Wraps mono 16-bit PCM samples in a minimal RIFF/WAV container.
  Uint8List _buildWav(Int16List samples) {
    const channels = 1;
    const bitsPerSample = 16;
    final byteRate = _sampleRate * channels * bitsPerSample ~/ 8;
    final blockAlign = channels * bitsPerSample ~/ 8;
    final dataSize = samples.length * 2;
    final fileSize = 44 + dataSize;

    final bytes = ByteData(fileSize);
    var offset = 0;

    void writeString(String s) {
      for (final unit in s.codeUnits) {
        bytes.setUint8(offset++, unit);
      }
    }

    void writeUint32(int v) {
      bytes.setUint32(offset, v, Endian.little);
      offset += 4;
    }

    void writeUint16(int v) {
      bytes.setUint16(offset, v, Endian.little);
      offset += 2;
    }

    writeString('RIFF');
    writeUint32(fileSize - 8);
    writeString('WAVE');
    writeString('fmt ');
    writeUint32(16); // PCM fmt chunk size
    writeUint16(1); // audio format = PCM
    writeUint16(channels);
    writeUint32(_sampleRate);
    writeUint32(byteRate);
    writeUint16(blockAlign);
    writeUint16(bitsPerSample);
    writeString('data');
    writeUint32(dataSize);

    for (final s in samples) {
      bytes.setInt16(offset, s, Endian.little);
      offset += 2;
    }

    return bytes.buffer.asUint8List();
  }
}
