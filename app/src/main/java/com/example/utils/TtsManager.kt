package com.example.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var currentSpeed: Float = 1.0f

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
            tts?.setSpeechRate(currentSpeed)
        }
    }

    fun speak(text: String, isBengali: Boolean = true, speed: Float = 1.0f) {
        if (!isInitialized || tts == null) return
        stop()

        currentSpeed = speed
        tts?.setSpeechRate(speed)

        val locale = if (isBengali) {
            Locale("bn", "BD")
        } else {
            Locale.US
        }

        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fallback to default
            tts?.setLanguage(Locale.US)
        }

        // Clean markdown symbols for cleaner vocalization
        val cleanText = text
            .replace("*", "")
            .replace("#", "")
            .replace("`", "")
            .replace("_", "")
            .trim()

        val utteranceId = "NOVA_AI_TTS_${System.currentTimeMillis()}"
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        if (isInitialized && tts != null) {
            tts?.stop()
            _isSpeaking.value = false
        }
    }

    fun setSpeechRate(rate: Float) {
        currentSpeed = rate
        if (isInitialized) {
            tts?.setSpeechRate(rate)
        }
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
