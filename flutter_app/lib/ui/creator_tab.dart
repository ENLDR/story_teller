import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../state/horror_story_view_model.dart';
import '../theme/app_colors.dart';

const _categories = [
  'සොහොන් බිම (Graveyard)',
  'විනාශ වූ මන්දිරය (Ruined Mansion)',
  'මූසල වනාන්තරය (Dark Jungle)',
  'අවතාර දුම්රිය (Phantom Train)',
];

const _lengths = ['කෙටි (Short Tale)', 'මධ්‍යම (Medium Tale)'];

class CreatorTab extends StatefulWidget {
  final VoidCallback onNavigateToPlayer;
  const CreatorTab({super.key, required this.onNavigateToPlayer});

  @override
  State<CreatorTab> createState() => _CreatorTabState();
}

class _CreatorTabState extends State<CreatorTab> {
  int _creatorMode = 0; // 0: AI, 1: Custom
  final _customElementController = TextEditingController();
  final _customTitleController = TextEditingController();
  final _customContentController = TextEditingController();
  String _customCategory = _categories.first;

  @override
  void dispose() {
    _customElementController.dispose();
    _customTitleController.dispose();
    _customContentController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final vm = context.watch<HorrorStoryViewModel>();

    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 32),
      children: [
        const Text(
          'භීතිකා නිර්මාතෘ (Horror Creator)',
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
            color: AppColors.ghostlyWhite,
          ),
        ),
        const SizedBox(height: 4),
        const Text(
          'අද්භූත සැකසුම් ඇතුළත් කර බියකරු කතාවක් සජීවීව නිර්මාණය කරන්න.',
          style: TextStyle(fontSize: 12, color: AppColors.shadowGrey),
        ),
        const SizedBox(height: 20),
        _modeSelector(),
        const SizedBox(height: 20),
        if (_creatorMode == 0) ..._aiForm(vm) else ..._customForm(vm),
      ],
    );
  }

  // --- Mode selector segmented control ---
  Widget _modeSelector() {
    return Container(
      padding: const EdgeInsets.all(4),
      decoration: BoxDecoration(
        color: const Color(0x13FFFFFF),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0x0CFFFFFF), width: 0.5),
      ),
      child: Row(
        children: [
          _modeButton('AI නිර්මාතෘ (AI Creator)', 0),
          _modeButton('මගේ නිර්මාණ (Custom)', 1),
        ],
      ),
    );
  }

  Widget _modeButton(String label, int mode) {
    final selected = _creatorMode == mode;
    return Expanded(
      child: GestureDetector(
        onTap: () => setState(() => _creatorMode = mode),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 10),
          decoration: BoxDecoration(
            color: selected ? AppColors.crimsonPrimary : Colors.transparent,
            borderRadius: BorderRadius.circular(20),
          ),
          alignment: Alignment.center,
          child: Text(
            label,
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.bold,
              color: selected ? AppColors.ghostlyWhite : AppColors.shadowGrey,
            ),
          ),
        ),
      ),
    );
  }

  // --- AI creator form ---
  List<Widget> _aiForm(HorrorStoryViewModel vm) {
    return [
      _label('ස්ථානය තෝරන්න (Select Location)'),
      const SizedBox(height: 8),
      ..._categories.map((cat) {
        final key = cat.split(' ').first;
        final selected = vm.selectedCategory.contains(key);
        return Padding(
          padding: const EdgeInsets.only(bottom: 8),
          child: GestureDetector(
            onTap: () => vm.selectCategory(cat),
            child: Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: selected ? const Color(0x28B91C1C) : const Color(0x3318181D),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(
                  color: selected ? AppColors.scarletAccent : const Color(0x0DFFFFFF),
                ),
              ),
              child: Row(
                children: [
                  Icon(
                    selected ? Icons.radio_button_checked : Icons.radio_button_off,
                    color: selected ? AppColors.scarletAccent : AppColors.shadowGrey,
                    size: 20,
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      cat,
                      style: TextStyle(
                        fontSize: 14,
                        color: selected ? AppColors.ghostlyWhite : AppColors.shadowGrey,
                        fontWeight: selected ? FontWeight.bold : FontWeight.normal,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        );
      }),
      const SizedBox(height: 12),
      _label('කතාවේ දිග (Story Length)'),
      const SizedBox(height: 8),
      Row(
        children: [
          for (var i = 0; i < _lengths.length; i++) ...[
            if (i > 0) const SizedBox(width: 12),
            Expanded(child: _lengthChip(vm, _lengths[i])),
          ],
        ],
      ),
      const SizedBox(height: 12),
      _label('භීතියේ සංකේතය (Terrifying Element / Object)'),
      const SizedBox(height: 8),
      _styledTextField(
        controller: _customElementController,
        hint: 'උදා: හඬන බෝනික්කා, ජනේලයේ තට්ටු හඬ...',
        onChanged: vm.setCustomElement,
      ),
      const SizedBox(height: 16),
      _generationArea(vm),
    ];
  }

  Widget _lengthChip(HorrorStoryViewModel vm, String len) {
    final selected = vm.selectedLength.contains(len.split(' ').first);
    return GestureDetector(
      onTap: () => vm.selectLength(len),
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 12),
        alignment: Alignment.center,
        decoration: BoxDecoration(
          color: selected ? AppColors.crimsonPrimary : const Color(0x3318181D),
          borderRadius: BorderRadius.circular(24),
          border: Border.all(
            color: selected ? AppColors.scarletAccent : const Color(0x0DFFFFFF),
          ),
        ),
        child: Text(
          len,
          style: TextStyle(
            fontSize: 13,
            fontWeight: FontWeight.w600,
            color: selected ? AppColors.ghostlyWhite : AppColors.shadowGrey,
          ),
        ),
      ),
    );
  }

  Widget _generationArea(HorrorStoryViewModel vm) {
    final state = vm.generationState;
    switch (state) {
      case GenLoading():
        return Container(
          padding: const EdgeInsets.all(24),
          decoration: BoxDecoration(
            color: AppColors.darkCrimsonSurface,
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: AppColors.crimsonPrimary),
          ),
          child: const Column(
            children: [
              CircularProgressIndicator(color: AppColors.scarletAccent),
              SizedBox(height: 16),
              Text(
                'අඳුරු බලවේග කැඳවමින් පවතී...',
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: AppColors.ghostlyWhite,
                  fontWeight: FontWeight.w600,
                  fontSize: 14,
                ),
              ),
              SizedBox(height: 4),
              Text(
                'Gemini is scripting Sinhala horrors format...',
                textAlign: TextAlign.center,
                style: TextStyle(color: AppColors.shadowGrey, fontSize: 11),
              ),
            ],
          ),
        );
      case GenSuccess(:final story):
        return Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: AppColors.darkCrimsonSurface,
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: AppColors.bloodGold),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Row(
                children: [
                  Icon(Icons.check, color: AppColors.bloodGold),
                  SizedBox(width: 8),
                  Text(
                    'භීෂණය සාර්ථකව උත්පාදනය විය!',
                    style: TextStyle(
                      color: AppColors.bloodGold,
                      fontWeight: FontWeight.bold,
                      fontSize: 14,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Text(
                'නිර්මාණය වූ කතාව: ${story.title}',
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(color: AppColors.ghostlyWhite, fontSize: 12),
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.darkMutedCard,
                        foregroundColor: AppColors.ghostlyWhite,
                      ),
                      onPressed: vm.resetGenerationState,
                      child: const Text('තව එකක් (New)'),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    flex: 1,
                    child: ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.crimsonPrimary,
                        foregroundColor: AppColors.ghostlyWhite,
                      ),
                      onPressed: () {
                        vm.startStoryPlayback(story);
                        vm.resetGenerationState();
                        widget.onNavigateToPlayer();
                      },
                      child: const Text('දැන් අසන්න (Listen)'),
                    ),
                  ),
                ],
              ),
            ],
          ),
        );
      case GenError(:final message):
        return Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: AppColors.darkCrimsonSurface,
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: Colors.red),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'දෝෂයකි: $message',
                style: const TextStyle(
                  color: Colors.red,
                  fontSize: 12,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8),
              const Text(
                'කරුණාකර GEMINI_API_KEY යතුර --dart-define මගින් ලබා දී ඇතිදැයි පරීක්ෂා කරන්න.',
                style: TextStyle(color: AppColors.shadowGrey, fontSize: 11),
              ),
              const SizedBox(height: 12),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.crimsonPrimary,
                    foregroundColor: AppColors.ghostlyWhite,
                  ),
                  onPressed: vm.resetGenerationState,
                  child: const Text('නැවත උත්සාහ කරන්න (Retry)'),
                ),
              ),
            ],
          ),
        );
      case GenIdle():
        return SizedBox(
          width: double.infinity,
          height: 54,
          child: ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.crimsonPrimary,
              foregroundColor: AppColors.ghostlyWhite,
              elevation: 8,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8),
              ),
            ),
            onPressed: vm.generateStory,
            child: const Text(
              'භීෂණය අවදි කරන්න (Incite Terror)',
              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
            ),
          ),
        );
    }
  }

  // --- Custom writer form ---
  List<Widget> _customForm(HorrorStoryViewModel vm) {
    const sfxTags = <(String, String)>[
      ('🎵 පසුබිම් සංගීතය', '[BGM: Low Eerie Drone]\n'),
      ('😱 කෑගැසීම', ' [SFX: Scream] '),
      ('🚪 දොර හඬ', ' [SFX: Creak] '),
      ('💓 හද ගැස්ම', ' [SFX: Heartbeat] '),
      ('🌬️ සුළඟ', ' [SFX: Wind] '),
      ('🌧️ වැස්ස', ' [SFX: Rain] '),
      ('👁️ කොඳුරීම', ' [SFX: Whisper] '),
      ('💥 බිඳීමක්', ' [SFX: Shatter] '),
    ];

    return [
      _label('කතාවේ මාතෘකාව (Story Title)'),
      const SizedBox(height: 8),
      _styledTextField(
        controller: _customTitleController,
        hint: 'උදා: සොහොන් ගැබක අභිරහස...',
      ),
      const SizedBox(height: 16),
      _label('කාණ්ඩය / ස්ථානය (Select Category)'),
      const SizedBox(height: 8),
      Row(
        children: [
          for (var i = 0; i < _categories.length; i++) ...[
            if (i > 0) const SizedBox(width: 8),
            Expanded(child: _customCategoryChip(_categories[i])),
          ],
        ],
      ),
      const SizedBox(height: 16),
      _label('ශබ්ද ප්‍රයෝග ඇතුළත් කිරීම් (Embedded Sound FX)'),
      const SizedBox(height: 2),
      const Text(
        'කතාව අසන විට සජීවීව ශබ්ද සහ සංගීතය වාදනය කිරීමට පහත ටැග් එකතු කරන්න:',
        style: TextStyle(fontSize: 11, color: AppColors.shadowGrey),
      ),
      const SizedBox(height: 8),
      SizedBox(
        height: 34,
        child: ListView.separated(
          scrollDirection: Axis.horizontal,
          itemCount: sfxTags.length,
          separatorBuilder: (_, __) => const SizedBox(width: 8),
          itemBuilder: (_, i) {
            final (label, insertion) = sfxTags[i];
            return GestureDetector(
              onTap: () {
                final text = _customContentController.text + insertion;
                _customContentController.value = TextEditingValue(
                  text: text,
                  selection: TextSelection.collapsed(offset: text.length),
                );
              },
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                decoration: BoxDecoration(
                  color: const Color(0x28B91C1C),
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: const Color(0x55B91C1C), width: 0.5),
                ),
                alignment: Alignment.center,
                child: Text(
                  label,
                  style: const TextStyle(
                    fontSize: 11,
                    color: AppColors.scarletAccent,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
            );
          },
        ),
      ),
      const SizedBox(height: 16),
      _label('කතාවේ විස්තරය (Story Text in Sinhala)'),
      const SizedBox(height: 8),
      _styledTextField(
        controller: _customContentController,
        hint: 'කතාව සිංහලෙන් ලියන්න...\n\n(උදා:\n[BGM: Low Eerie Drone]\n'
            'ඒ පාලු රාත්‍රිය ඉතා සීතලයි... [SFX: Creak] දොර විවෘත වුණා...)',
        minLines: 8,
        maxLines: 15,
      ),
      const SizedBox(height: 16),
      SizedBox(
        width: double.infinity,
        height: 54,
        child: ElevatedButton(
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.crimsonPrimary,
            foregroundColor: AppColors.ghostlyWhite,
            elevation: 8,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          ),
          onPressed: () async {
            if (_customContentController.text.trim().isEmpty) {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('කරුණාකර කතාවේ අන්තර්ගතය ඇතුළත් කරන්න.')),
              );
              return;
            }
            final saved = await vm.saveCustomStory(
              title: _customTitleController.text.trim().isEmpty
                  ? 'මගේ අභිරහස් කතාව'
                  : _customTitleController.text,
              content: _customContentController.text,
              category: _customCategory,
            );
            vm.startStoryPlayback(saved);
            _customTitleController.clear();
            _customContentController.clear();
            widget.onNavigateToPlayer();
          },
          child: const Text(
            'සුරකින්න සහ සවන්දෙන්න (Save & Listen)',
            style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
          ),
        ),
      ),
    ];
  }

  Widget _customCategoryChip(String cat) {
    final selected = _customCategory == cat;
    return GestureDetector(
      onTap: () => setState(() => _customCategory = cat),
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 12),
        alignment: Alignment.center,
        decoration: BoxDecoration(
          color: selected ? AppColors.crimsonPrimary : const Color(0x3318181D),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: selected ? AppColors.scarletAccent : const Color(0x0DFFFFFF),
          ),
        ),
        child: Text(
          cat.split(' ').first,
          textAlign: TextAlign.center,
          style: TextStyle(
            fontSize: 11,
            fontWeight: FontWeight.w600,
            color: selected ? AppColors.ghostlyWhite : AppColors.shadowGrey,
          ),
        ),
      ),
    );
  }

  // --- Shared widgets ---
  Widget _label(String text) => Text(
        text,
        style: const TextStyle(
          fontSize: 14,
          color: AppColors.scarletAccent,
          fontWeight: FontWeight.w600,
        ),
      );

  Widget _styledTextField({
    required TextEditingController controller,
    required String hint,
    int minLines = 1,
    int maxLines = 1,
    ValueChanged<String>? onChanged,
  }) {
    return TextField(
      controller: controller,
      onChanged: onChanged,
      minLines: minLines,
      maxLines: maxLines,
      style: const TextStyle(color: AppColors.ghostlyWhite),
      decoration: InputDecoration(
        hintText: hint,
        hintStyle: const TextStyle(color: Colors.grey, fontSize: 13),
        filled: true,
        fillColor: AppColors.darkCrimsonSurface,
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: AppColors.darkCrimsonSurface),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: const BorderSide(color: AppColors.scarletAccent),
        ),
      ),
    );
  }
}
