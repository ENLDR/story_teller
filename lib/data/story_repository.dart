import '../api/gemini_service.dart';
import '../config.dart';
import '../models/horror_story.dart';
import 'story_database.dart';

/// Bridges the local database and the Gemini API — the `StoryRepository`
/// equivalent.
class StoryRepository {
  final StoryDatabase _db;
  final GeminiService _gemini;

  StoryRepository({StoryDatabase? db, GeminiService? gemini})
      : _db = db ?? StoryDatabase.instance,
        _gemini = gemini ?? GeminiService();

  Future<List<HorrorStory>> getAllStories() => _db.getAllStories();
  Future<List<HorrorStory>> getFavoriteStories() => _db.getFavoriteStories();
  Future<HorrorStory?> getStoryById(int id) => _db.getStoryById(id);
  Future<int> insertStory(HorrorStory story) => _db.insertStory(story);
  Future<void> updateStory(HorrorStory story) => _db.updateStory(story);
  Future<void> deleteStoryById(int id) => _db.deleteStoryById(id);
  Future<void> deleteAllStories() => _db.deleteAllStories();

  Future<HorrorStory> generateAndSaveStory({
    required String category,
    required String length,
    required String customElement,
    required String suspenseLevel,
  }) async {
    if (!AppConfig.hasApiKey) {
      throw StateError(
        'API key is missing! Provide it via '
        '--dart-define=GEMINI_API_KEY=your_key_here',
      );
    }

    final prompt = '''
Generate a terrifying horror story based on:
- Location/Category: $category
- Story Length: $length
- Key terrifying element or object: $customElement
- Intensity/Suspense: $suspenseLevel

Remember to write strictly in authentic Sinhala. The story should start with a gripping title on the first line. Embed BGM and SFX cues correctly in English brackets (e.g., [BGM: Low Eerie Drone], [SFX: Door Creaking], etc.). Ensure dramatic ellipses are used.
'''
        .trim();

    final responseText = await _gemini.generateContent(
      prompt: prompt,
      systemInstruction: _storySystemPrompt,
      temperature: 0.85,
      maxOutputTokens: length.contains('Short') ? 1000 : 2000,
    );

    final lines = responseText
        .split('\n')
        .map((l) => l.trim())
        .where((l) => l.isNotEmpty)
        .toList();
    final title = lines.isNotEmpty
        ? lines.first.replaceAll('#', '').trim()
        : 'භීතිකාවක ඇරඹුම (A Terror Begins)';
    final content = lines.skip(1).join('\n\n');

    final newStory = HorrorStory(
      title: title,
      content: content,
      category: category,
      isFavorite: false,
      systemPromptUsed: 'Category: $category | Element: $customElement',
    );

    final insertedId = await _db.insertStory(newStory);
    return newStory.copyWith(id: insertedId);
  }

  static const String _storySystemPrompt =
      '''You are an expert horror storyteller and creative writer specializing in dark, atmospheric, and terrifying Sinhala horror stories (භීතිකා කථා). Your goal is to generate gripping horror narratives in Sinhala, specifically formatted for a deep-voiced Text-to-Speech (TTS) engine.

Structure and Style Guidelines:
1. Language: Write exclusively in authentic, natural-sounding Sinhala (using Sinhala script). The vocabulary should be cinematic, mysterious, and suspenseful.
2. Voice Tuning for Deep TTS: Use punctuation effectively (commas, ellipses "...") to create dramatic pauses, allowing a deep TTS voice to sound ominous, slow, and powerful.
3. Audio Cues (Sound Effects): Embed explicit atmospheric and sound effects cues in English brackets at the exact moments they should occur to guide the app's audio mixing. Examples: [BGM: Low Eerie Drone], [SFX: Distant Scream], [SFX: Door Creaking], [SFX: Heavy Breathing], [SFX: Heartbeat], [SFX: Rain Chills], [SFX: Owl Cry], [SFX: Glass Shattering], [SFX: Ghostly Whisper], [SFX: Wind Howl].
4. Pacing: Start slow, build intense psychological suspense, and deliver a chilling climax.

Format details: On the very first line, write a short gripping Sinhala title. Do not add labels like "Title:". Next, start the story on a new line. Try to mix in high-vibe Sinhala gothic words. Do not wrap paragraphs in markdown code blocks. Just output raw text with the cues embedded.
''';
}
