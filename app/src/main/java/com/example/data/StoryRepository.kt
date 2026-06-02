package com.example.data

import com.example.BuildConfig
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GenerationConfig
import com.example.api.Part
import com.example.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class StoryRepository(private val storyDao: StoryDao) {

    val allStories: Flow<List<HorrorStory>> = storyDao.getAllStories()
    val favoriteStories: Flow<List<HorrorStory>> = storyDao.getFavoriteStories()

    suspend fun getStoryById(id: Long): HorrorStory? {
        return storyDao.getStoryById(id)
    }

    suspend fun insertStory(story: HorrorStory): Long {
        return storyDao.insertStory(story)
    }

    suspend fun updateStory(story: HorrorStory) {
        storyDao.updateStory(story)
    }

    suspend fun deleteStoryById(id: Long) {
        storyDao.deleteStoryById(id)
    }

    suspend fun deleteAllStories() {
        storyDao.deleteAllStories()
    }

    suspend fun generateAndSaveStory(
        category: String,
        length: String,
        customElement: String,
        suspenseLevel: String
    ): HorrorStory = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("API key is missing! Please configure the GEMINI_API_KEY secret in Google AI Studio.")
        }

        val prompt = """
            Generate a terrifying horror story based on:
            - Location/Category: $category
            - Story Length: $length
            - Key terrifying element or object: $customElement
            - Intensity/Suspense: $suspenseLevel
            
            Remember to write strictly in authentic Sinhala. The story should start with a gripping title on the first line. Embed BGM and SFX cues correctly in English brackets (e.g., [BGM: Low Eerie Drone], [SFX: Door Creaking], etc.). Ensure dramatic ellipses are used.
        """.trimIndent()

        val systemInstruction = Content(
            parts = listOf(Part(text = STORY_SYSTEM_PROMPT))
        )

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = 0.85f,
                maxOutputTokens = if (length.contains("Short")) 1000 else 2000
            ),
            systemInstruction = systemInstruction
        )

        val response = RetrofitClient.service.generateContent(apiKey, request)
        val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw IllegalStateException("Received empty content from Gemini API.")

        // Parse title and content
        val lines = responseText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val title = lines.firstOrNull()?.replace("#", "")?.trim() ?: "භීතිකාවක ඇරඹුම (A Terror Begins)"
        val content = lines.drop(1).joinToString("\n\n")

        val newStory = HorrorStory(
            title = title,
            content = content,
            category = category,
            isFavorite = false,
            systemPromptUsed = "Category: $category | Element: $customElement"
        )

        val insertedId = storyDao.insertStory(newStory)
        newStory.copy(id = insertedId)
    }

    companion object {
        private const val STORY_SYSTEM_PROMPT = """You are an expert horror storyteller and creative writer specializing in dark, atmospheric, and terrifying Sinhala horror stories (භීතිකා කථා). Your goal is to generate gripping horror narratives in Sinhala, specifically formatted for a deep-voiced Text-to-Speech (TTS) engine.

Structure and Style Guidelines:
1. Language: Write exclusively in authentic, natural-sounding Sinhala (using Sinhala script). The vocabulary should be cinematic, mysterious, and suspenseful.
2. Voice Tuning for Deep TTS: Use punctuation effectively (commas, ellipses "...") to create dramatic pauses, allowing a deep TTS voice to sound ominous, slow, and powerful.
3. Audio Cues (Sound Effects): Embed explicit atmospheric and sound effects cues in English brackets at the exact moments they should occur to guide the app's audio mixing. Examples: [BGM: Low Eerie Drone], [SFX: Distant Scream], [SFX: Door Creaking], [SFX: Heavy Breathing], [SFX: Heartbeat], [SFX: Rain Chills], [SFX: Owl Cry], [SFX: Glass Shattering], [SFX: Ghostly Whisper], [SFX: Wind Howl].
4. Pacing: Start slow, build intense psychological suspense, and deliver a chilling climax.

Format details: On the very first line, write a short gripping Sinhala title. Do not add labels like "Title:". Next, start the story on a new line. Try to mix in high-vibe Sinhala gothic words. Do not wrap paragraphs in markdown code blocks. Just output raw text with the cues embedded.
"""
    }
}
