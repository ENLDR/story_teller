import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../audio/narrative_element.dart';
import '../state/horror_story_view_model.dart';
import '../theme/app_colors.dart';
import 'spooky_visualizer.dart';

class ListenerTab extends StatefulWidget {
  const ListenerTab({super.key});

  @override
  State<ListenerTab> createState() => _ListenerTabState();
}

class _ListenerTabState extends State<ListenerTab>
    with SingleTickerProviderStateMixin {
  final ScrollController _scrollController = ScrollController();
  late final AnimationController _pulseController;
  int _lastIndex = -1;

  @override
  void initState() {
    super.initState();
    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1200),
    )..repeat(reverse: true);
  }

  @override
  void dispose() {
    _scrollController.dispose();
    _pulseController.dispose();
    super.dispose();
  }

  void _maybeScroll(HorrorStoryViewModel vm) {
    final idx = vm.currentElementIndex;
    if (idx == _lastIndex) return;
    _lastIndex = idx;
    if (idx < 0 || idx >= vm.parsedElements.length) return;
    if (vm.parsedElements[idx] is! TextElement) return;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!_scrollController.hasClients) return;
      // Estimated per-element extent; good enough to keep focus visible.
      final target = (idx * 90.0).clamp(0.0, _scrollController.position.maxScrollExtent);
      _scrollController.animateTo(
        target,
        duration: const Duration(milliseconds: 400),
        curve: Curves.easeInOut,
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<HorrorStoryViewModel>();
    final story = vm.currentPlayingStory;

    if (story == null) {
      return _emptyState();
    }

    _maybeScroll(vm);

    return DecoratedBox(
      decoration: const BoxDecoration(
        gradient: RadialGradient(
          center: Alignment(0, -0.3),
          radius: 1.1,
          colors: [Color(0x1FC62828), AppColors.obsidianBackground],
        ),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
        child: Column(
          children: [
            _header(vm, story.title),
            _locationRow(story.category),
            Expanded(child: _narrativeCard(vm)),
            const SizedBox(height: 16),
            SpookyVisualizer(
              isPlaying: vm.isPlaying,
              activeBgm: vm.activeBgm,
              activeSfx: vm.activeSfx,
            ),
            const SizedBox(height: 16),
            _timeline(vm),
            const SizedBox(height: 18),
            _controls(vm),
            const SizedBox(height: 18),
            _voiceCard(vm),
            const SizedBox(height: 8),
          ],
        ),
      ),
    );
  }

  Widget _emptyState() {
    return const Center(
      child: Padding(
        padding: EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.play_arrow, color: AppColors.darkCrimsonSurface, size: 72),
            SizedBox(height: 16),
            Text(
              'තවමත් කතාවක් තෝරා නොමැත',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: AppColors.ghostlyWhite,
              ),
            ),
            SizedBox(height: 4),
            Text(
              'පුස්තකාලයෙන් කතාවක් තෝරන්න හෝ කථා නිර්මාතෘ වෙතින් අලුත් එකක් උත්පාදනය කරන්න.',
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 12, color: AppColors.shadowGrey),
            ),
          ],
        ),
      ),
    );
  }

  Widget _header(HorrorStoryViewModel vm, String title) {
    return Padding(
      padding: const EdgeInsets.only(top: 8, bottom: 16),
      child: Row(
        children: [
          GestureDetector(
            onTap: vm.stopPlayback,
            child: Container(
              width: 40,
              height: 40,
              decoration: BoxDecoration(
                color: const Color(0x19B91C1C),
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: const Color(0x33B91C1C)),
              ),
              alignment: Alignment.center,
              child: const Text(
                '←',
                style: TextStyle(
                  color: AppColors.scarletAccent,
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'සජීවී විකාශය',
                  style: TextStyle(
                    fontSize: 11,
                    letterSpacing: 2,
                    fontWeight: FontWeight.bold,
                    color: AppColors.crimsonPrimary,
                  ),
                ),
                Text(
                  title,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    fontSize: 17,
                    fontWeight: FontWeight.bold,
                    color: AppColors.ghostlyWhite,
                  ),
                ),
              ],
            ),
          ),
          _circleIconButton(
            child: const Icon(Icons.share, color: AppColors.ghostlyWhite, size: 20),
            onTap: () => vm.downloadStoryAsFile(vm.currentPlayingStory!),
          ),
          const SizedBox(width: 8),
          _circleIconButton(
            child: const Text(
              '⋮',
              style: TextStyle(
                color: AppColors.ghostlyWhite,
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            onTap: vm.cycleVoice,
          ),
        ],
      ),
    );
  }

  Widget _circleIconButton({required Widget child, required VoidCallback onTap}) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: const Color(0x0FFFFFFF),
          borderRadius: BorderRadius.circular(20),
        ),
        alignment: Alignment.center,
        child: child,
      ),
    );
  }

  Widget _locationRow(String category) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        children: [
          const Icon(Icons.location_on, color: AppColors.scarletAccent, size: 14),
          const SizedBox(width: 4),
          Text(
            category,
            style: const TextStyle(
              fontSize: 11,
              color: AppColors.shadowGrey,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
  }

  Widget _narrativeCard(HorrorStoryViewModel vm) {
    final elements = vm.parsedElements;
    return Container(
      width: double.infinity,
      decoration: BoxDecoration(
        color: const Color(0x3318181D),
        borderRadius: BorderRadius.circular(28),
        border: Border.all(color: const Color(0x0DFFFFFF)),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 24),
      child: ListView.separated(
        controller: _scrollController,
        itemCount: elements.length + 2,
        separatorBuilder: (_, __) => const SizedBox(height: 20),
        itemBuilder: (_, i) {
          if (i == 0) {
            return Text(
              '--- සන්නිවේදනය ක්‍රියාත්මකයි (Audio Link Active) ---',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 9,
                letterSpacing: 1,
                color: AppColors.crimsonPrimary.withValues(alpha: 0.7),
                fontWeight: FontWeight.bold,
              ),
            );
          }
          if (i == elements.length + 1) {
            return Text(
              '--- සම්පූර්ණයි (End of Narrative) ---',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 9,
                color: AppColors.crimsonPrimary.withValues(alpha: 0.5),
              ),
            );
          }
          final idx = i - 1;
          return _elementWidget(elements[idx], idx == vm.currentElementIndex);
        },
      ),
    );
  }

  Widget _elementWidget(NarrativeElement element, bool isCurrent) {
    switch (element) {
      case TextElement(:final content):
        return Container(
          width: double.infinity,
          decoration: BoxDecoration(
            color: isCurrent ? const Color(0x11FFFFFF) : Colors.transparent,
            borderRadius: BorderRadius.circular(12),
          ),
          padding: const EdgeInsets.all(14),
          child: Text(
            content,
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: isCurrent ? 22 : 16,
              fontWeight: FontWeight.w300,
              height: 1.5,
              fontFamily: 'serif',
              color: AppColors.ghostlyWhite.withValues(alpha: isCurrent ? 1.0 : 0.35),
            ),
          ),
        );
      case SoundEffectElement(:final effect):
        if (!isCurrent) return const SizedBox.shrink();
        return Center(
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 5),
            decoration: BoxDecoration(
              color: const Color(0x3C1F1F22),
              borderRadius: BorderRadius.circular(16),
            ),
            child: Text(
              '🔊 [SFX: ${effect.toUpperCase()}]',
              style: const TextStyle(
                fontSize: 11,
                fontFamily: 'monospace',
                color: AppColors.shadowGrey,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
        );
      case BackgroundMusicElement(:final music):
        if (!isCurrent) return const SizedBox.shrink();
        return Center(
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 5),
            decoration: BoxDecoration(
              color: const Color(0x28B91C1C),
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: const Color(0x55B91C1C), width: 0.5),
            ),
            child: Text(
              '🎵 [BGM: ${music.toUpperCase()}]',
              style: const TextStyle(
                fontSize: 11,
                fontFamily: 'monospace',
                color: AppColors.scarletAccent,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
        );
    }
  }

  Widget _timeline(HorrorStoryViewModel vm) {
    final total = vm.parsedElements.length;
    final current = (vm.currentElementIndex + 1).clamp(0, total);
    final totalSeconds = total * 15;
    final currentSeconds = current * 15;
    final ratio = total > 0 ? current / total : 0.0;

    String fmt(int s) =>
        '${(s ~/ 60).toString().padLeft(2, '0')}:${(s % 60).toString().padLeft(2, '0')}';

    return Row(
      children: [
        Text(
          fmt(currentSeconds),
          style: const TextStyle(
            fontSize: 11,
            fontFamily: 'monospace',
            color: AppColors.crimsonPrimary,
            fontWeight: FontWeight.bold,
          ),
        ),
        Expanded(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 14),
            child: LayoutBuilder(
              builder: (_, constraints) {
                final w = constraints.maxWidth;
                return SizedBox(
                  height: 16,
                  child: Stack(
                    alignment: Alignment.centerLeft,
                    children: [
                      Container(
                        height: 4,
                        decoration: BoxDecoration(
                          color: const Color(0xFF212124),
                          borderRadius: BorderRadius.circular(2),
                        ),
                      ),
                      Container(
                        width: w * ratio,
                        height: 4,
                        decoration: BoxDecoration(
                          gradient: const LinearGradient(
                            colors: [AppColors.crimsonPrimary, AppColors.scarletAccent],
                          ),
                          borderRadius: BorderRadius.circular(2),
                        ),
                      ),
                      Positioned(
                        left: (w * ratio - 5).clamp(0.0, w - 10),
                        child: Container(
                          width: 10,
                          height: 10,
                          decoration: const BoxDecoration(
                            color: AppColors.ghostlyWhite,
                            shape: BoxShape.circle,
                          ),
                        ),
                      ),
                    ],
                  ),
                );
              },
            ),
          ),
        ),
        Text(
          fmt(totalSeconds),
          style: const TextStyle(
            fontSize: 11,
            fontFamily: 'monospace',
            color: AppColors.shadowGrey,
            fontWeight: FontWeight.bold,
          ),
        ),
      ],
    );
  }

  Widget _controls(HorrorStoryViewModel vm) {
    final idx = vm.currentElementIndex;
    final lastIdx = vm.parsedElements.length - 1;
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceAround,
      children: [
        _textButton('↺', AppColors.shadowGrey, vm.restartPlayback),
        _textButton(
          '⏮',
          idx > 0 ? AppColors.ghostlyWhite : AppColors.shadowGrey.withValues(alpha: 0.3),
          vm.previousElement,
        ),
        ScaleTransition(
          scale: vm.isPlaying
              ? Tween(begin: 1.0, end: 1.08).animate(_pulseController)
              : const AlwaysStoppedAnimation(1.0),
          child: GestureDetector(
            onTap: vm.togglePlayPause,
            child: Container(
              width: 74,
              height: 74,
              decoration: const BoxDecoration(
                shape: BoxShape.circle,
                gradient: RadialGradient(
                  colors: [AppColors.scarletAccent, AppColors.crimsonPrimary],
                ),
              ),
              alignment: Alignment.center,
              child: Text(
                vm.isPlaying ? '⏸' : '▶',
                style: const TextStyle(
                  color: AppColors.ghostlyWhite,
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
          ),
        ),
        _textButton(
          '⏭',
          idx < lastIdx ? AppColors.ghostlyWhite : AppColors.shadowGrey.withValues(alpha: 0.3),
          vm.nextElement,
        ),
        _textButton('↻', AppColors.shadowGrey, vm.cycleVoice),
      ],
    );
  }

  Widget _textButton(String glyph, Color color, VoidCallback onTap) {
    return SizedBox(
      width: 44,
      height: 44,
      child: IconButton(
        padding: EdgeInsets.zero,
        onPressed: onTap,
        icon: Text(
          glyph,
          style: TextStyle(color: color, fontSize: 24, fontWeight: FontWeight.bold),
        ),
      ),
    );
  }

  Widget _voiceCard(HorrorStoryViewModel vm) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: const Color(0x3B18181C),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0x0DFFFFFF)),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              Container(
                width: 42,
                height: 42,
                decoration: BoxDecoration(
                  color: const Color(0x19FFFFFF),
                  borderRadius: BorderRadius.circular(10),
                ),
                alignment: Alignment.center,
                child: const Text('🎙️', style: TextStyle(fontSize: 20)),
              ),
              const SizedBox(width: 12),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'VOICE ENGINE',
                    style: TextStyle(
                      fontSize: 9,
                      fontWeight: FontWeight.bold,
                      color: AppColors.shadowGrey,
                      letterSpacing: 1,
                    ),
                  ),
                  Text(
                    vm.activeVoice.displayName,
                    style: const TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                      color: AppColors.ghostlyWhite,
                    ),
                  ),
                ],
              ),
            ],
          ),
          OutlinedButton(
            onPressed: vm.cycleVoice,
            style: OutlinedButton.styleFrom(
              foregroundColor: AppColors.ghostlyWhite,
              backgroundColor: const Color(0x0FFFFFFF),
              side: const BorderSide(color: Color(0x22FFFFFF), width: 0.5),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 6),
            ),
            child: const Text(
              'මාරු කරන්න',
              style: TextStyle(fontSize: 11, fontWeight: FontWeight.w600),
            ),
          ),
        ],
      ),
    );
  }
}
