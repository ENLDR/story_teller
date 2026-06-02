import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart';

import '../audio/narrative_element.dart';
import '../audio/spooky_audio_engine.dart';
import '../audio/spooky_tts.dart';
import '../data/story_repository.dart';
import '../models/horror_story.dart';

/// Mirrors the Kotlin `GenerationState` sealed class.
sealed class GenerationState {
  const GenerationState();
}

class GenIdle extends GenerationState {
  const GenIdle();
}

class GenLoading extends GenerationState {
  const GenLoading();
}

class GenSuccess extends GenerationState {
  final HorrorStory story;
  const GenSuccess(this.story);
}

class GenError extends GenerationState {
  final String message;
  const GenError(this.message);
}

class VoiceConfig {
  final String id;
  final String displayName;
  final double pitch;
  final double rate;
  const VoiceConfig(this.id, this.displayName, this.pitch, this.rate);
}

class HorrorStoryViewModel extends ChangeNotifier {
  final StoryRepository _repository;
  final SpookyAudioEngine _audioEngine = SpookyAudioEngine();
  late final SpookyTextToSpeech _tts;

  HorrorStoryViewModel({StoryRepository? repository})
      : _repository = repository ?? StoryRepository() {
    _tts = SpookyTextToSpeech(
      onInitSuccess: () {},
      onLineFinished: _advancePlayback,
    );
    _bootstrap();
  }

  // --- Library data ---
  List<HorrorStory> _allStories = [];
  List<HorrorStory> get allStories => _allStories;

  List<HorrorStory> get favoriteStories =>
      _allStories.where((s) => s.isFavorite).toList();

  // --- Creator options ---
  String selectedCategory = 'සොහොන් බිම (Haunted Cemetery)';
  String selectedLength = 'මධ්‍යම (Medium Tale)';
  String customElement = '';
  String selectedSuspense = 'අතිශය බය හිතෙන (Extreme Suspense)';

  void selectCategory(String c) {
    selectedCategory = c;
    notifyListeners();
  }

  void selectLength(String l) {
    selectedLength = l;
    notifyListeners();
  }

  void setCustomElement(String e) {
    customElement = e;
    notifyListeners();
  }

  void selectSuspense(String s) {
    selectedSuspense = s;
    notifyListeners();
  }

  // --- Generation state ---
  GenerationState _generationState = const GenIdle();
  GenerationState get generationState => _generationState;

  // --- Player state ---
  HorrorStory? _currentPlayingStory;
  HorrorStory? get currentPlayingStory => _currentPlayingStory;

  List<NarrativeElement> _parsedElements = [];
  List<NarrativeElement> get parsedElements => _parsedElements;

  int _currentElementIndex = -1;
  int get currentElementIndex => _currentElementIndex;

  bool _isPlaying = false;
  bool get isPlaying => _isPlaying;

  String _activeBgm = 'BGM: Silent';
  String get activeBgm => _activeBgm;

  String _activeSfx = '';
  String get activeSfx => _activeSfx;

  /// Incremented whenever playback is interrupted, to cancel pending
  /// delayed callbacks (replaces Kotlin's Job cancellation).
  int _playToken = 0;

  // --- Voices ---
  final List<VoiceConfig> voices = const [
    VoiceConfig('deep', 'Deep Shadow (Bass)', 0.60, 0.72),
    VoiceConfig('witch', 'High Witch (Screech)', 1.45, 0.88),
    VoiceConfig('ghoul', 'Whispering Ghoul', 0.40, 0.55),
    VoiceConfig('normal', 'Spectral Normal', 1.00, 1.00),
  ];

  int _currentVoiceIndex = 0;
  int get currentVoiceIndex => _currentVoiceIndex;
  VoiceConfig get activeVoice => voices[_currentVoiceIndex];

  void cycleVoice() {
    _currentVoiceIndex = (_currentVoiceIndex + 1) % voices.length;
    final config = voices[_currentVoiceIndex];
    _tts.setTuning(config.pitch, config.rate);
    notifyListeners();
  }

  // --- Bootstrap ---
  Future<void> _bootstrap() async {
    await _refreshStories();
    if (_allStories.isEmpty) {
      await _prepopulate();
      await _refreshStories();
    }
  }

  Future<void> _refreshStories() async {
    _allStories = await _repository.getAllStories();
    notifyListeners();
  }

  Future<void> _prepopulate() async {
    await _repository.insertStory(
      HorrorStory(
        title: 'සොහොන් පියස (Graveyard\'s Shadow)',
        category: 'සොහොන් බිම (Graveyard)',
        isFavorite: true,
        content: '''[BGM: Low Eerie Drone]
එදා මහා මූසල මධ්‍යම රාත්‍රියක්... කිසිවෙකුත් නොවූ පාලු සොහොන් පොළ මැදින්... සීතල සුළඟක් හමා ගියා...
[SFX: Rain Chills]
මම බොහෝ වේලාවක් අතරමං වී සිටියා... හදිසියේම, කැලෑව දෙසින් මහ හඬක් ඇසුණා...
[SFX: Distant Scream]
පස් කන්දක් යටින්... කාගේදෝ කෙඳිරිලි හඬක්... ඉතා සෙමින් මතු වන්නට වුණා...
[SFX: Heavy Breathing]
මම... බියෙන් සලිත වෙමින්... ආපසු හැරී දිව යන්නට උත්සාහ කළා... එහෙත්... මගේ දෙපා පණ නැති වී තිබුණා...
[SFX: Heartbeat]
රාත්‍රී අඳුර මාව ගිලගනිද්දී... මට ඇසුණේ... ඇගේ අවසන් සිනහ හඬ පමණයි...
[SFX: Distant Scream]''',
      ),
    );

    await _repository.insertStory(
      HorrorStory(
        title: 'අවතාර මන්දිරය (The Phantom Manor)',
        category: 'විනාශ වූ මන්දිරය (Ruined Mansion)',
        isFavorite: false,
        content: '''[BGM: Low Eerie Drone]
පාලු වී ගිය ඒ පැරණි මන්දිරයේ දොර විවර වූයේ ඉතා සෙමින්...
[SFX: Door Creaking]
ඇතුළත තිබුණේ දැඩි මෘත ශරීර සුවඳක්... පියවර තබන විට ලිහිල් ලෑලි පුවරු හඬ නැඟුවා...
[SFX: Footsteps on dry leaves]
ජනේලය මතින් අමුතු සෙවනැල්ලක් මතු වී... ක්ෂණයකින් සැඟවී ගියා...
[SFX: Ghostly Whisper]
"යන්න... මෙතැනින් යන්න..." කියා රහසක් මගේ කනට ඇසුණාක් මෙන් දැනුණා...
[SFX: Heavy Breathing]
එසැණින්... මුළු ලෝකයම නිහඬ කරමින්... රතු ලේ බින්දු ජනේලය පුරා ගලා යන්නට විය...
[SFX: Shatter]''',
      ),
    );
  }

  // --- Generation ---
  Future<void> generateStory() async {
    _generationState = const GenLoading();
    notifyListeners();
    try {
      final story = await _repository.generateAndSaveStory(
        category: selectedCategory,
        length: selectedLength,
        customElement: customElement.isEmpty
            ? 'කෙඳිරිලි හඬක් (A low crying sound)'
            : customElement,
        suspenseLevel: selectedSuspense,
      );
      await _refreshStories();
      _generationState = GenSuccess(story);
    } catch (e) {
      _generationState = GenError(e.toString());
    }
    notifyListeners();
  }

  void resetGenerationState() {
    _generationState = const GenIdle();
    notifyListeners();
  }

  Future<HorrorStory> saveCustomStory({
    required String title,
    required String content,
    required String category,
  }) async {
    final newStory = HorrorStory(
      title: title.trim().isEmpty ? 'මගේ නිර්මාණය (Custom Horror)' : title,
      content: content.trim().isEmpty ? 'පාලු රාත්‍රියක්...' : content,
      category: category,
      isFavorite: false,
      systemPromptUsed: 'භව්‍යමය රචනය (Manual Entry)',
    );
    final id = await _repository.insertStory(newStory);
    await _refreshStories();
    return newStory.copyWith(id: id);
  }

  /// Exports the story as a .txt file and opens the share sheet.
  /// Returns the saved file name on success.
  Future<String> downloadStoryAsFile(HorrorStory story) async {
    final title = story.title.trim().isEmpty ? 'story_${story.id}' : story.title;
    final safeName = '${title.replaceAll(RegExp(r'[\\/:*?"<>|]'), '_')}.txt';
    final created = DateTime.fromMillisecondsSinceEpoch(story.timestamp);
    final dateStr =
        '${created.year}-${_two(created.month)}-${_two(created.day)} '
        '${_two(created.hour)}:${_two(created.minute)}';

    final fileContent = '''
========================================
${story.title}
========================================
කාණ්ඩය (Category): ${story.category}
දිනය (Created on): $dateStr

${story.title} - Story Narrative:
----------------------------------------
${story.content}
========================================''';

    final dir = await getApplicationDocumentsDirectory();
    final file = File('${dir.path}/$safeName');
    await file.writeAsString(fileContent);

    await Share.shareXFiles(
      [XFile(file.path, mimeType: 'text/plain')],
      subject: story.title,
    );
    return safeName;
  }

  String _two(int v) => v.toString().padLeft(2, '0');

  Future<void> toggleFavorite(HorrorStory story) async {
    await _repository.updateStory(story.copyWith(isFavorite: !story.isFavorite));
    await _refreshStories();
  }

  Future<void> deleteStory(HorrorStory story) async {
    if (_currentPlayingStory?.id == story.id) {
      await stopPlayback();
    }
    await _repository.deleteStoryById(story.id);
    await _refreshStories();
  }

  // --- Playback state machine ---

  void startStoryPlayback(HorrorStory story) {
    _interrupt();
    _currentPlayingStory = story;
    _parsedElements = NarrativeParser.parse(story.content);
    _currentElementIndex = 0;
    _isPlaying = true;
    notifyListeners();
    _executeCurrentElement(_playToken);
  }

  void togglePlayPause() {
    if (!_isPlaying) {
      if (_currentPlayingStory != null) {
        _isPlaying = true;
        _interrupt();
        notifyListeners();
        _executeCurrentElement(_playToken);
      }
    } else {
      _isPlaying = false;
      _interrupt();
      _tts.stop();
      _audioEngine.stopBgm();
      notifyListeners();
    }
  }

  Future<void> stopPlayback() async {
    _isPlaying = false;
    _interrupt();
    await _tts.stop();
    await _audioEngine.stopBgm();
    _currentPlayingStory = null;
    _parsedElements = [];
    _currentElementIndex = -1;
    _activeBgm = 'BGM: Silent';
    _activeSfx = '';
    notifyListeners();
  }

  void nextElement() {
    final nextIndex = _currentElementIndex + 1;
    if (nextIndex < _parsedElements.length) {
      _interrupt();
      _tts.stop();
      _currentElementIndex = nextIndex;
      notifyListeners();
      if (_isPlaying) _executeCurrentElement(_playToken);
    }
  }

  void previousElement() {
    final prevIndex = _currentElementIndex - 1;
    if (prevIndex >= 0) {
      _interrupt();
      _tts.stop();
      _currentElementIndex = prevIndex;
      notifyListeners();
      if (_isPlaying) _executeCurrentElement(_playToken);
    }
  }

  void restartPlayback() {
    if (_currentPlayingStory == null) return;
    _interrupt();
    _tts.stop();
    _currentElementIndex = 0;
    notifyListeners();
    if (_isPlaying) _executeCurrentElement(_playToken);
  }

  /// Cancels any pending delayed callbacks by bumping the token.
  void _interrupt() {
    _playToken++;
  }

  void _advancePlayback() {
    if (!_isPlaying) return;
    final nextIndex = _currentElementIndex + 1;
    if (nextIndex < _parsedElements.length) {
      _currentElementIndex = nextIndex;
      notifyListeners();
      _executeCurrentElement(_playToken);
    } else {
      stopPlayback();
    }
  }

  Future<void> _executeCurrentElement(int token) async {
    if (token != _playToken) return;
    final index = _currentElementIndex;
    if (index < 0 || index >= _parsedElements.length) return;

    final element = _parsedElements[index];
    switch (element) {
      case BackgroundMusicElement(:final music):
        _activeBgm = 'BGM: $music';
        notifyListeners();
        await _audioEngine.startBgm(music);
        // BGM cues advance instantly.
        _advancePlayback();
      case SoundEffectElement(:final effect):
        _activeSfx = '⚠️ ${effect.toUpperCase()}';
        notifyListeners();
        await _audioEngine.playSfx(effect);
        await Future.delayed(const Duration(milliseconds: 1300));
        if (token != _playToken) return;
        _activeSfx = '';
        notifyListeners();
        _advancePlayback();
      case TextElement(:final content):
        _tts.speak(content);
        // Fallback if TTS engine is unavailable: estimate read duration.
        if (!_tts.isReady) {
          final words = content.split(' ').length;
          final delayMs = (words * 500 + 1500).clamp(3000, 1 << 31);
          await Future.delayed(Duration(milliseconds: delayMs));
          if (token != _playToken) return;
          _advancePlayback();
        }
    }
  }

  @override
  void dispose() {
    _audioEngine.shutdown();
    _tts.shutdown();
    super.dispose();
  }
}
