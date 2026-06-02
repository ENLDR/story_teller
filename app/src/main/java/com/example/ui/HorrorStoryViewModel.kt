package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.NarrativeElement
import com.example.audio.NarrativeParser
import com.example.audio.SpookyAudioEngine
import com.example.audio.SpookyTextToSpeech
import com.example.data.AppDatabase
import com.example.data.HorrorStory
import com.example.data.StoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class GenerationState {
    object Idle : GenerationState()
    object Loading : GenerationState()
    data class Success(val story: HorrorStory) : GenerationState()
    data class Error(val message: String) : GenerationState()
}

data class VoiceConfig(val id: String, val displayName: String, val pitch: Float, val rate: Float)

class HorrorStoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StoryRepository
    private val audioEngine = SpookyAudioEngine()
    private var tts: SpookyTextToSpeech? = null

    // Room Flows
    val allStories: StateFlow<List<HorrorStory>>
    val favoriteStories: StateFlow<List<HorrorStory>>

    // Options UI State
    val selectedCategory = MutableStateFlow("සොහොන් බිම (Haunted Cemetery)")
    val selectedLength = MutableStateFlow("මධ්‍යම (Medium Tale)")
    val customElement = MutableStateFlow("")
    val selectedSuspense = MutableStateFlow("අතිශය බය හිතෙන (Extreme Suspense)")

    // Generation State
    private val _generationState = MutableStateFlow<GenerationState>(GenerationState.Idle)
    val generationState = _generationState.asStateFlow()

    // Listening Player State
    private val _currentPlayingStory = MutableStateFlow<HorrorStory?>(null)
    val currentPlayingStory = _currentPlayingStory.asStateFlow()

    private val _parsedElements = MutableStateFlow<List<NarrativeElement>>(emptyList())
    val parsedElements = _parsedElements.asStateFlow()

    private val _currentElementIndex = MutableStateFlow(-1)
    val currentElementIndex = _currentElementIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _activeBgm = MutableStateFlow("BGM: Silent")
    val activeBgm = _activeBgm.asStateFlow()

    private val _activeSfx = MutableStateFlow("")
    val activeSfx = _activeSfx.asStateFlow()

    private var playbackJob: Job? = null

    val voices = listOf(
        VoiceConfig("deep", "Deep Shadow (Bass)", 0.60f, 0.72f),
        VoiceConfig("witch", "High Witch (Screech)", 1.45f, 0.88f),
        VoiceConfig("ghoul", "Whispering Ghoul", 0.40f, 0.55f),
        VoiceConfig("normal", "Spectral Normal", 1.00f, 1.00f)
    )

    private val _currentVoiceIndex = MutableStateFlow(0)
    val currentVoiceIndex = _currentVoiceIndex.asStateFlow()

    fun cycleVoice() {
        val nextIndex = (_currentVoiceIndex.value + 1) % voices.size
        _currentVoiceIndex.value = nextIndex
        val config = voices[nextIndex]
        tts?.setTuning(config.pitch, config.rate)
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = StoryRepository(database.storyDao())

        allStories = repository.allStories
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        favoriteStories = repository.favoriteStories
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Prepopulate database if empty on startup
        viewModelScope.launch {
            allStories.collect { stories ->
                if (stories.isEmpty()) {
                    prepopulateDatabase()
                }
            }
        }

        // Initialize Speech engine
        tts = SpookyTextToSpeech(
            context = application,
            onInitSuccess = { /* Speech Engine Ready */ },
            onLineFinished = {
                // When TTS line finishes reading, advance the playback state machine!
                viewModelScope.launch {
                    advancePlayback()
                }
            }
        )
    }

    private suspend fun prepopulateDatabase() = withContext(Dispatchers.IO) {
        val sample1 = HorrorStory(
            title = "සොහොන් පියස (Graveyard's Shadow)",
            content = """[BGM: Low Eerie Drone]
එදා මහා මූසල මධ්‍යම රාත්‍රියක්... කිසිවෙකුත් නොවූ පාලු සොහොන් පොළ මැදින්... සීතල සුළඟක් හමා ගියා...
[SFX: Rain Chills]
මම බොහෝ වේලාවක් අතරමං වී සිටියා... හදිසියේම, කැලෑව දෙසින් මහ හඬක් ඇසුණා...
[SFX: Distant Scream]
පස් කන්දක් යටින්... කාගේදෝ කෙඳිරිලි හඬක්... ඉතා සෙමින් මතු වන්නට වුණා...
[SFX: Heavy Breathing]
මම... බියෙන් සලිත වෙමින්... ආපසු හැරී දිව යන්නට උත්සාහ කළා... එහෙත්... මගේ දෙපා පණ නැති වී තිබුණා...
[SFX: Heartbeat]
රාත්‍රී අඳුර මාව ගිලගනිද්දී... මට ඇසුණේ... ඇගේ අවසන් සිනහ හඬ පමණයි...
[SFX: Distant Scream]""",
            category = "සොහොන් බිම (Graveyard)",
            isFavorite = true
        )

        val sample2 = HorrorStory(
            title = "අවතාර මන්දිරය (The Phantom Manor)",
            content = """[BGM: Low Eerie Drone]
පාලු වී ගිය ඒ පැරණි මන්දිරයේ දොර විවර වූයේ ඉතා සෙමින්...
[SFX: Door Creaking]
ඇතුළත තිබුණේ දැඩි මෘත ශරීර සුවඳක්... පියවර තබන විට ලිහිල් ලෑලි පුවරු හඬ නැඟුවා...
[SFX: Footsteps on dry leaves]
ජනේලය මතින් අමුතු සෙවනැල්ලක් මතු වී... ක්ෂණයකින් සැඟවී ගියා...
[SFX: Ghostly Whisper]
"යන්න... මෙතැනින් යන්න..." කියා රහසක් මගේ කනට ඇසුණාක් මෙන් දැනුණා...
[SFX: Heavy Breathing]
එසැණින්... මුළු ලෝකයම නිහඬ කරමින්... රතු ලේ බින්දු ජනේලය පුරා ගලා යන්නට විය...
[SFX: Shatter]""",
            category = "විනාශ වූ මන්දිරය (Ruined Mansion)",
            isFavorite = false
        )

        repository.insertStory(sample1)
        repository.insertStory(sample2)
    }

    fun selectCategory(category: String) { selectedCategory.value = category }
    fun selectLength(length: String) { selectedLength.value = length }
    fun setCustomElement(element: String) { customElement.value = element }
    fun selectSuspense(suspense: String) { selectedSuspense.value = suspense }

    fun generateStory() {
        viewModelScope.launch {
            _generationState.value = GenerationState.Loading
            try {
                val story = repository.generateAndSaveStory(
                    category = selectedCategory.value,
                    length = selectedLength.value,
                    customElement = customElement.value.ifEmpty { "කෙඳිරිලි හඬක් (A low crying sound)" },
                    suspenseLevel = selectedSuspense.value
                )
                _generationState.value = GenerationState.Success(story)
            } catch (e: Exception) {
                _generationState.value = GenerationState.Error(e.message ?: "නොදනනා දෝෂයක් (Unknown Error occurred)")
            }
        }
    }

    fun resetGenerationState() {
        _generationState.value = GenerationState.Idle
    }

    fun saveCustomStory(
        title: String,
        content: String,
        category: String,
        onCompleted: (HorrorStory) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newStory = HorrorStory(
                title = title.ifBlank { "මගේ නිර්මාණය (Custom Horror)" },
                content = content.ifBlank { "පාලු රාත්‍රියක්..." },
                category = category,
                isFavorite = false,
                systemPromptUsed = "භව්‍යමය රචනය (Manual Entry)"
            )
            val id = repository.insertStory(newStory)
            val savedStory = newStory.copy(id = id)
            withContext(Dispatchers.Main) {
                onCompleted(savedStory)
            }
        }
    }

    fun downloadStoryAsFile(story: HorrorStory) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val title = story.title.ifBlank { "story_${story.id}" }
                // Remove special character formatting from file name
                val safeFileName = title.replace(Regex("[\\\\/:*?\"<>|]"), "_") + ".txt"
                val fileContent = """
                    ========================================
                    ${story.title}
                    ========================================
                    කාණ්ඩය (Category): ${story.category}
                    දිනය (Created on): ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(story.timestamp))}
                    
                    ${story.title} - Story Narrative:
                    ----------------------------------------
                    ${story.content}
                    ========================================
                """.trimIndent()

                val context = getApplication<Application>()
                var fileSaved = false

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val contentValues = android.content.ContentValues().apply {
                        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, safeFileName)
                        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                        put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                    }
                    val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(fileContent.toByteArray())
                            fileSaved = true
                        }
                    }
                } else {
                    val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                    if (downloadsDir != null) {
                        val file = java.io.File(downloadsDir, safeFileName)
                        file.writeText(fileContent)
                        fileSaved = true
                    }
                }

                withContext(Dispatchers.Main) {
                    if (fileSaved) {
                        android.widget.Toast.makeText(
                            context,
                            "කතාව 'Downloads' ෆෝල්ඩරයට සාර්ථකව භාගත කරන ලදී!\n($safeFileName)",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            "භාගත කිරීම අසාර්ථක විය.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        getApplication(),
                        "භාගත කිරීම අසාර්ථක විය: ${e.localizedMessage}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun toggleFavorite(story: HorrorStory) {
        viewModelScope.launch {
            repository.updateStory(story.copy(isFavorite = !story.isFavorite))
        }
    }

    fun deleteStory(story: HorrorStory) {
        viewModelScope.launch {
            // If deleting currently active playing story, stop playback
            if (_currentPlayingStory.value?.id == story.id) {
                stopPlayback()
            }
            repository.deleteStoryById(story.id)
        }
    }

    // --- Story Player Logic ---

    fun startStoryPlayback(story: HorrorStory) {
        stopPlayback()
        _currentPlayingStory.value = story
        val elements = NarrativeParser.parse(story.content)
        _parsedElements.value = elements
        _currentElementIndex.value = 0
        _isPlaying.value = true

        playbackJob = viewModelScope.launch {
            executeCurrentElement()
        }
    }

    fun togglePlayPause() {
        if (!_isPlaying.value) {
            val story = _currentPlayingStory.value
            if (story != null) {
                _isPlaying.value = true
                playbackJob?.cancel()
                playbackJob = viewModelScope.launch {
                    executeCurrentElement()
                }
            }
        } else {
            _isPlaying.value = false
            playbackJob?.cancel()
            tts?.stop()
            audioEngine.stopBgm()
        }
    }

    fun stopPlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        tts?.stop()
        audioEngine.stopBgm()
        _currentPlayingStory.value = null
        _parsedElements.value = emptyList()
        _currentElementIndex.value = -1
        _activeBgm.value = "BGM: Silent"
        _activeSfx.value = ""
    }

    fun nextElement() {
        val nextIndex = _currentElementIndex.value + 1
        if (nextIndex < _parsedElements.value.size) {
            playbackJob?.cancel()
            tts?.stop()
            _currentElementIndex.value = nextIndex
            if (_isPlaying.value) {
                playbackJob = viewModelScope.launch {
                    executeCurrentElement()
                }
            }
        }
    }

    fun previousElement() {
        val prevIndex = _currentElementIndex.value - 1
        if (prevIndex >= 0) {
            playbackJob?.cancel()
            tts?.stop()
            _currentElementIndex.value = prevIndex
            if (_isPlaying.value) {
                playbackJob = viewModelScope.launch {
                    executeCurrentElement()
                }
            }
        }
    }

    fun restartPlayback() {
        if (_currentPlayingStory.value == null) return
        playbackJob?.cancel()
        tts?.stop()
        _currentElementIndex.value = 0
        if (_isPlaying.value) {
            playbackJob = viewModelScope.launch {
                executeCurrentElement()
            }
        }
    }

    private suspend fun advancePlayback() {
        if (!_isPlaying.value) return
        
        val nextIndex = _currentElementIndex.value + 1
        if (nextIndex < _parsedElements.value.size) {
            _currentElementIndex.value = nextIndex
            executeCurrentElement()
        } else {
            // Story completes!
            stopPlayback()
        }
    }

    private suspend fun executeCurrentElement() {
        val index = _currentElementIndex.value
        val elements = _parsedElements.value

        if (index in elements.indices) {
            when (val element = elements[index]) {
                is NarrativeElement.BackgroundMusic -> {
                    _activeBgm.value = "BGM: ${element.music}"
                    audioEngine.startBgm(element.music)
                    // BGM commands advance instantly to the next element
                    advancePlayback()
                }
                is NarrativeElement.SoundEffect -> {
                    _activeSfx.value = "⚠️ ${element.effect.uppercase()}"
                    audioEngine.playSfx(element.effect)
                    // Pause briefly to amplify the atmospheric horror of the sound effect!
                    delay(1300)
                    _activeSfx.value = "" // clear flash
                    advancePlayback()
                }
                is NarrativeElement.Text -> {
                    // TTS speaking
                    tts?.speak(element.content, "story_line_$index")
                    
                    // Fallback progress support: If TTS is not ready or failed to run, automate progress based on average speed!
                    if (tts?.isReady != true) {
                        // Dynamic estimated reading duration
                        val estimateWordsLen = element.content.split(" ").size
                        val delayMs = (estimateWordsLen * 500L + 1500L).coerceAtLeast(3000L)
                        delay(delayMs)
                        advancePlayback()
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.shutdown()
        tts?.shutdown()
    }
}
