import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/horror_story.dart';
import '../state/horror_story_view_model.dart';
import '../theme/app_colors.dart';

class LibraryTab extends StatelessWidget {
  final void Function(HorrorStory) onPlayStory;
  const LibraryTab({super.key, required this.onPlayStory});

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<HorrorStoryViewModel>();
    final stories = vm.allStories;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Padding(
            padding: EdgeInsets.symmetric(vertical: 12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'භීෂණ මංජුසාව (The Anthology Catalog)',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: AppColors.ghostlyWhite,
                  ),
                ),
                Text(
                  'ඔබ විසින් තෝරාගත් හෝ නිර්මාණය කළ සියලු කතා.',
                  style: TextStyle(fontSize: 12, color: AppColors.shadowGrey),
                ),
              ],
            ),
          ),
          Expanded(
            child: stories.isEmpty
                ? const Center(
                    child: Text(
                      'කථා කිසිවක් නැත. \'මවන්න\' වෙතින් නව කතාවක් ජනිත කරන්න.',
                      textAlign: TextAlign.center,
                      style: TextStyle(color: AppColors.shadowGrey, fontSize: 13),
                    ),
                  )
                : ListView.separated(
                    padding: const EdgeInsets.only(bottom: 32),
                    itemCount: stories.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 14),
                    itemBuilder: (_, i) => _storyCard(context, vm, stories[i]),
                  ),
          ),
        ],
      ),
    );
  }

  Widget _storyCard(BuildContext context, HorrorStoryViewModel vm, HorrorStory story) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0x3318181D),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: const Color(0x0DFFFFFF)),
      ),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  story.title,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: AppColors.ghostlyWhite,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  'ස්ථානය: ${story.category}',
                  style: const TextStyle(fontSize: 11, color: AppColors.shadowGrey),
                ),
              ],
            ),
          ),
          IconButton(
            onPressed: () => vm.toggleFavorite(story),
            icon: Icon(
              story.isFavorite ? Icons.favorite : Icons.favorite_border,
              color: story.isFavorite ? AppColors.crimsonPrimary : AppColors.shadowGrey,
            ),
          ),
          Container(
            decoration: BoxDecoration(
              color: AppColors.crimsonPrimary.withValues(alpha: 0.15),
              borderRadius: BorderRadius.circular(24),
            ),
            child: IconButton(
              onPressed: () => onPlayStory(story),
              icon: const Icon(Icons.play_arrow, color: AppColors.scarletAccent),
            ),
          ),
          IconButton(
            onPressed: () => vm.downloadStoryAsFile(story),
            icon: const Icon(Icons.share, color: AppColors.shadowGrey),
          ),
          IconButton(
            onPressed: () => vm.deleteStory(story),
            icon: const Icon(Icons.delete, color: AppColors.shadowGrey),
          ),
        ],
      ),
    );
  }
}
