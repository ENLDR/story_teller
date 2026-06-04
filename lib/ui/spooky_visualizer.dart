import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../theme/app_colors.dart';

/// Animated oscilloscope waveform reacting to playback / SFX state.
class SpookyVisualizer extends StatefulWidget {
  final bool isPlaying;
  final String activeBgm;
  final String activeSfx;

  const SpookyVisualizer({
    super.key,
    required this.isPlaying,
    required this.activeBgm,
    required this.activeSfx,
  });

  @override
  State<SpookyVisualizer> createState() => _SpookyVisualizerState();
}

class _SpookyVisualizerState extends State<SpookyVisualizer>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 2500),
    )..repeat();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final hasSfx = widget.activeSfx.isNotEmpty;
    return Container(
      height: 72,
      margin: const EdgeInsets.symmetric(vertical: 4),
      decoration: BoxDecoration(
        color: AppColors.darkCrimsonSurface,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Stack(
        children: [
          Positioned.fill(
            child: AnimatedBuilder(
              animation: _controller,
              builder: (_, __) {
                return CustomPaint(
                  painter: _WavePainter(
                    phase: _controller.value * 2 * math.pi,
                    isPlaying: widget.isPlaying,
                    hasSfx: hasSfx,
                  ),
                );
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  widget.activeBgm,
                  style: const TextStyle(
                    fontSize: 11,
                    color: AppColors.bloodGold,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                if (hasSfx)
                  Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                    decoration: BoxDecoration(
                      color: AppColors.scarletAccent.withValues(alpha: 0.15),
                      borderRadius: BorderRadius.circular(4),
                    ),
                    child: Text(
                      widget.activeSfx,
                      style: const TextStyle(
                        fontSize: 11,
                        color: AppColors.scarletAccent,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _WavePainter extends CustomPainter {
  final double phase;
  final bool isPlaying;
  final bool hasSfx;
  final math.Random _rng = math.Random();

  _WavePainter({
    required this.phase,
    required this.isPlaying,
    required this.hasSfx,
  });

  @override
  void paint(Canvas canvas, Size size) {
    const points = 150;
    final path = Path()..moveTo(0, size.height / 2);

    for (var i = 0; i <= points; i++) {
      final x = (i / points) * size.width;
      final ratio = math.sin(i / points * math.pi); // fade at edges

      final double amp;
      if (!isPlaying) {
        amp = 2;
      } else if (hasSfx) {
        amp = 35 * (_rng.nextDouble() * 0.4 + 0.8); // spikes
      } else {
        amp = 12 + 8 * math.sin(2.5 * i / points + phase); // slow hum
      }

      final double freq;
      if (hasSfx) {
        freq = 0.25;
      } else if (isPlaying) {
        freq = 0.09;
      } else {
        freq = 0.02;
      }

      final y = (size.height / 2) + math.sin(i * freq + phase) * amp * ratio;
      path.lineTo(x, y);
    }

    final paint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = hasSfx ? 4 : 2.5
      ..color = hasSfx
          ? AppColors.scarletAccent
          : isPlaying
              ? AppColors.crimsonPrimary
              : AppColors.shadowGrey.withValues(alpha: 0.4);

    canvas.drawPath(path, paint);
  }

  @override
  bool shouldRepaint(covariant _WavePainter oldDelegate) => true;
}
