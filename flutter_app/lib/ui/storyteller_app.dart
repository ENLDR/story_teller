import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../state/horror_story_view_model.dart';
import '../theme/app_colors.dart';
import 'creator_tab.dart';
import 'library_tab.dart';
import 'listener_tab.dart';

class StorytellerApp extends StatefulWidget {
  const StorytellerApp({super.key});

  @override
  State<StorytellerApp> createState() => _StorytellerAppState();
}

class _StorytellerAppState extends State<StorytellerApp> {
  int _activeTab = 0; // 0: Create, 1: Listen, 2: Library

  void _goToTab(int index) => setState(() => _activeTab = index);

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<HorrorStoryViewModel>();
    final hidePlayerHeader = _activeTab == 1 && vm.currentPlayingStory != null;

    return Scaffold(
      backgroundColor: AppColors.obsidianBackground,
      appBar: hidePlayerHeader ? null : _buildHeader(),
      body: SafeArea(
        top: hidePlayerHeader,
        child: IndexedStack(
          index: _activeTab,
          children: [
            CreatorTab(onNavigateToPlayer: () => _goToTab(1)),
            const ListenerTab(),
            LibraryTab(
              onPlayStory: (story) {
                vm.startStoryPlayback(story);
                _goToTab(1);
              },
            ),
          ],
        ),
      ),
      bottomNavigationBar: _buildBottomBar(vm),
    );
  }

  PreferredSizeWidget _buildHeader() {
    return PreferredSize(
      preferredSize: const Size.fromHeight(112),
      child: SafeArea(
        bottom: false,
        child: Container(
          color: AppColors.obsidianBackground,
          padding: const EdgeInsets.only(top: 12, bottom: 8),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Text(
                'භීතිකා',
                style: TextStyle(
                  fontSize: 32,
                  fontWeight: FontWeight.bold,
                  color: AppColors.crimsonPrimary,
                  fontFamily: 'serif',
                ),
              ),
              const Padding(
                padding: EdgeInsets.only(top: 2),
                child: Text(
                  'Sinhala Horror Storyteller & Audio Mixer',
                  style: TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.w500,
                    color: AppColors.shadowGrey,
                  ),
                ),
              ),
              const SizedBox(height: 12),
              FractionallySizedBox(
                widthFactor: 0.5,
                child: Container(
                  height: 1,
                  decoration: const BoxDecoration(
                    gradient: LinearGradient(
                      colors: [
                        Colors.transparent,
                        AppColors.crimsonPrimary,
                        Colors.transparent,
                      ],
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildBottomBar(HorrorStoryViewModel vm) {
    return Container(
      decoration: const BoxDecoration(
        color: AppColors.obsidianBackground,
        border: Border(
          top: BorderSide(color: AppColors.darkCrimsonSurface, width: 0.5),
        ),
      ),
      child: NavigationBarTheme(
        data: NavigationBarThemeData(
          backgroundColor: AppColors.obsidianBackground,
          indicatorColor: AppColors.darkCrimsonSurface,
          labelTextStyle: WidgetStateProperty.resolveWith((states) {
            final selected = states.contains(WidgetState.selected);
            return TextStyle(
              fontSize: 11,
              color: selected ? AppColors.scarletAccent : AppColors.shadowGrey,
            );
          }),
          iconTheme: WidgetStateProperty.resolveWith((states) {
            final selected = states.contains(WidgetState.selected);
            return IconThemeData(
              color: selected ? AppColors.scarletAccent : AppColors.shadowGrey,
            );
          }),
        ),
        child: NavigationBar(
          selectedIndex: _activeTab,
          onDestinationSelected: _goToTab,
          height: 68,
          destinations: [
            const NavigationDestination(
              icon: Icon(Icons.add),
              label: 'මවන්න (Create)',
            ),
            NavigationDestination(
              icon: Badge(
                isLabelVisible: vm.isPlaying,
                backgroundColor: AppColors.scarletAccent,
                label: const Text('🔊', style: TextStyle(fontSize: 8)),
                child: const Icon(Icons.play_arrow),
              ),
              label: 'සවන්දෙන්න (View)',
            ),
            const NavigationDestination(
              icon: Icon(Icons.list),
              label: 'පුස්තකාලය (Library)',
            ),
          ],
        ),
      ),
    );
  }
}
