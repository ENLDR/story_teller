package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class SpookyTextToSpeech(
    context: Context,
    private val onInitSuccess: () -> Unit,
    private val onLineFinished: () -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    var isReady = false
        private set

    private var currentPitch = 0.60f
    private var currentRate = 0.72f

    fun setTuning(pitch: Float, rate: Float) {
        currentPitch = pitch
        currentRate = rate
        if (isReady) {
            tts?.setPitch(pitch)
            tts?.setSpeechRate(rate)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Attempt to set Sinhala (si-LK) region
            val result = tts?.setLanguage(Locale("si", "LK"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("SpookyTTS", "Sinhala locale not supported, using system default.")
                tts?.language = Locale.getDefault()
            }
            
            // Apply slow and deep voice tuning guidelines (slow pacing, sinister tone)
            tts?.setPitch(currentPitch)      // Deep, mystical pitch
            tts?.setSpeechRate(currentRate) // Slow dramatic pacing

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    onLineFinished()
                }
                @Deprecated("Deprecated")
                override fun onError(utteranceId: String?) {
                    onLineFinished()
                }
                override fun onError(utteranceId: String?, errorCode: Int) {
                    onLineFinished()
                }
            })
            
            isReady = true
            onInitSuccess()
        } else {
            Log.e("SpookyTTS", "Failed to initialize TTS.")
        }
    }

    fun speak(text: String, utteranceId: String) {
        if (isReady) {
            // Clean up text of punctuation that might cause weird voice sounds, but preserve commas and ellipses
            val cleanText = text.replace(Regex("[\\[\\]]"), "")
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            onLineFinished()
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
