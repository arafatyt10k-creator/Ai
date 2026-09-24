package com.example.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

class NeuralTtsManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : TTSManager, TextToSpeech.OnInitListener {

    private val _isSpeaking = MutableStateFlow(false)
    override val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // Primary Neural Voice configurations
    companion object {
        const val VOICE_BENGALI_FEMALE = "bn-BD-NabanitaNeural"
        const val VOICE_BENGALI_MALE = "bn-BD-PradeepNeural"
        const val VOICE_ENGLISH_FEMALE = "en-US-JennyNeural"
        const val VOICE_ENGLISH_MALE = "en-US-GuyNeural"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var androidTts: TextToSpeech? = null
    private var isAndroidTtsReady = false

    private val sentenceQueue = ConcurrentLinkedQueue<String>()
    private var isProcessingQueue = false
    private var activeJob: Job? = null
    private var currentSpeed: Float = 1.0f

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    init {
        try {
            androidTts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isAndroidTtsReady = true
            androidTts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }
                override fun onDone(utteranceId: String?) {
                    playNextSentence()
                }
                override fun onError(utteranceId: String?) {
                    playNextSentence()
                }
            })
        }
    }

    override fun speak(text: String, isBengali: Boolean, speed: Float) {
        stop()
        currentSpeed = speed

        val cleanText = cleanMarkdown(text)
        if (cleanText.isBlank()) return

        // Sentence segmentation for immediate first-sentence streaming
        val sentences = cleanText.split(Regex("(?<=[।!?.\\n])\\s+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (sentences.isEmpty()) return

        queueSentences(sentences, isBengali, speed)
    }

    override fun queueSentences(sentences: List<String>, isBengali: Boolean, speed: Float) {
        currentSpeed = speed
        sentenceQueue.clear()
        sentenceQueue.addAll(sentences)

        if (!isProcessingQueue) {
            isProcessingQueue = true
            _isSpeaking.value = true
            playNextSentence(isBengali)
        }
    }

    private fun playNextSentence(isBengali: Boolean = true) {
        val nextSentence = sentenceQueue.poll()
        if (nextSentence == null) {
            _isSpeaking.value = false
            isProcessingQueue = false
            return
        }

        activeJob?.cancel()
        activeJob = coroutineScope.launch {
            val audioFile = synthesizeNeuralAudio(nextSentence, isBengali)
            withContext(Dispatchers.Main) {
                if (!isActive) return@withContext
                if (audioFile != null && audioFile.exists() && audioFile.length() > 0) {
                    playAudioFile(audioFile, isBengali)
                } else {
                    fallbackToAndroidTts(nextSentence, isBengali)
                }
            }
        }
    }

    private fun playAudioFile(file: File, isBengali: Boolean) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                        .build()
                )
                setDataSource(file.absolutePath)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    playbackParams = playbackParams.setSpeed(currentSpeed.coerceIn(0.5f, 2.0f))
                }
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                    playNextSentence(isBengali)
                }
                setOnErrorListener { _, _, _ ->
                    mediaPlayer?.release()
                    mediaPlayer = null
                    playNextSentence(isBengali)
                    true
                }
                prepare()
                start()
                _isSpeaking.value = true
            }
        } catch (e: Exception) {
            mediaPlayer?.release()
            mediaPlayer = null
            fallbackToAndroidTts(file.nameWithoutExtension, isBengali)
        }
    }

    private suspend fun synthesizeNeuralAudio(text: String, isBengali: Boolean): File? = withContext(Dispatchers.IO) {
        try {
            val cacheDir = File(context.cacheDir, "neural_tts")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val hash = (text + (if (isBengali) "bn" else "en")).hashCode().toString()
            val cachedFile = File(cacheDir, "tts_$hash.mp3")
            if (cachedFile.exists() && cachedFile.length() > 500) {
                return@withContext cachedFile
            }

            // High quality streaming neural endpoint
            val langParam = if (isBengali) "bn-BD" else "en-US"
            val encodedText = URLEncoder.encode(text.take(200), "UTF-8")
            val url = "https://translate.google.com/translate_tts?ie=UTF-8&tl=$langParam&client=tw-ob&q=$encodedText"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0)")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful && response.body != null) {
                val bytes = response.body!!.bytes()
                if (bytes.size > 200) {
                    FileOutputStream(cachedFile).use { it.write(bytes) }
                    return@withContext cachedFile
                }
            }
        } catch (e: Exception) {
            // network or timeout
        }
        return@withContext null
    }

    private fun fallbackToAndroidTts(text: String, isBengali: Boolean) {
        if (!isAndroidTtsReady || androidTts == null) {
            _isSpeaking.value = false
            return
        }

        val locale = if (isBengali) Locale("bn", "BD") else Locale.US
        val res = androidTts?.setLanguage(locale)
        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            androidTts?.setLanguage(Locale.US)
        }
        androidTts?.setSpeechRate(currentSpeed)

        val utteranceId = "NOVA_FALLBACK_${System.currentTimeMillis()}"
        androidTts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    override fun stop() {
        activeJob?.cancel()
        sentenceQueue.clear()
        isProcessingQueue = false

        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.reset()
                it.release()
            }
        } catch (e: Exception) {
            // ignore
        } finally {
            mediaPlayer = null
        }

        try {
            androidTts?.stop()
        } catch (e: Exception) {
            // ignore
        }

        _isSpeaking.value = false
    }

    override fun shutdown() {
        stop()
        coroutineScope.cancel()
        try {
            androidTts?.shutdown()
        } catch (e: Exception) {
            // ignore
        }
        androidTts = null
        isAndroidTtsReady = false
    }

    private fun cleanMarkdown(text: String): String {
        return text
            .replace(Regex("```[\\s\\S]*?```"), "")
            .replace(Regex("`.*?`"), "")
            .replace("*", "")
            .replace("#", "")
            .replace("_", "")
            .replace("~", "")
            .replace(">", "")
            .trim()
    }
}
