package com.example.service

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Interface and boilerplate structure for Picovoice Porcupine Wake Word Engine.
 * Listens for wake words like "Hey NOVA" or "NOVA" in real-time.
 */
class PorcupineWakeWordManager(
    private val context: Context,
    private val accessKey: String = "" // Optional Picovoice AccessKey
) {
    private var isListening = false
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _wakeWordDetected = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val wakeWordDetected: SharedFlow<String> = _wakeWordDetected.asSharedFlow()

    companion object {
        const val SAMPLE_RATE = 16000
        const val FRAME_LENGTH = 512
        const val KEYWORD_NOVA = "NOVA"
        const val KEYWORD_HEY_NOVA = "Hey NOVA"
    }

    fun start(onWakeWord: (String) -> Unit) {
        if (isListening) return
        isListening = true

        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(minBufferSize, FRAME_LENGTH * 2)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            audioRecord?.startRecording()

            recordingJob = coroutineScope.launch {
                val buffer = ShortArray(FRAME_LENGTH)
                while (isActive && isListening) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        // In a full Picovoice integration:
                        // val keywordIndex = porcupine.process(buffer)
                        // if (keywordIndex >= 0) emit detection
                        
                        // Energy calculation and keyword framing
                        var sum = 0.0
                        for (i in 0 until read) {
                            sum += buffer[i] * buffer[i]
                        }
                        val rms = Math.sqrt(sum / read)

                        // If energy peak matches wake signature pattern (placeholder for Picovoice engine binary)
                        if (rms > 8000) {
                            // High vocal energy trigger check
                        }
                    }
                    delay(30)
                }
            }
        } catch (e: SecurityException) {
            isListening = false
        } catch (e: Exception) {
            isListening = false
        }
    }

    fun simulateWakeTrigger(keyword: String = KEYWORD_NOVA) {
        coroutineScope.launch {
            _wakeWordDetected.emit(keyword)
        }
    }

    fun stop() {
        isListening = false
        recordingJob?.cancel()
        recordingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            // ignore
        } finally {
            audioRecord = null
        }
    }

    fun destroy() {
        stop()
        coroutineScope.cancel()
    }
}
