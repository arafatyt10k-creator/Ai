package com.example.utils

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

/**
 * TtsManager implements the TTSManager interface using the NeuralTtsManager engine.
 * Provides backward-compatible API while powering ultra-realistic Neural Voice synthesis.
 */
class TtsManager(context: Context) : TTSManager {

    private val neuralEngine = NeuralTtsManager(context)

    override val isSpeaking: StateFlow<Boolean>
        get() = neuralEngine.isSpeaking

    override fun speak(text: String, isBengali: Boolean, speed: Float) {
        neuralEngine.speak(text, isBengali, speed)
    }

    override fun queueSentences(sentences: List<String>, isBengali: Boolean, speed: Float) {
        neuralEngine.queueSentences(sentences, isBengali, speed)
    }

    override fun stop() {
        neuralEngine.stop()
    }

    override fun shutdown() {
        neuralEngine.shutdown()
    }

    fun setSpeechRate(rate: Float) {
        // Handled dynamically per speak call
    }
}
