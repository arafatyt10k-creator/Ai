package com.example.utils

import kotlinx.coroutines.flow.StateFlow

interface TTSManager {
    val isSpeaking: StateFlow<Boolean>
    fun speak(text: String, isBengali: Boolean = true, speed: Float = 1.0f)
    fun queueSentences(sentences: List<String>, isBengali: Boolean = true, speed: Float = 1.0f)
    fun stop()
    fun shutdown()
}
