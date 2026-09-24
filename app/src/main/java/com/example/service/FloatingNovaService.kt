package com.example.service

import android.animation.ValueAnimator
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.savedstate.*
import com.example.MainActivity
import com.example.R
import com.example.agent.DeviceActionDispatcher
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.local.UserSettings
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.repository.AIRepository
import com.example.data.repository.AIRepositoryImpl
import com.example.domain.model.AppLanguage
import com.example.domain.model.OrbState
import com.example.ui.floating.FloatingOrbUI
import com.example.ui.theme.NovaTheme
import com.example.utils.NeuralTtsManager
import com.example.utils.SpeechManager
import com.example.utils.TTSManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class FloatingNovaService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null
    private lateinit var windowParams: WindowManager.LayoutParams

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var database: AppDatabase
    private lateinit var aiRepository: AIRepository
    private lateinit var speechManager: SpeechManager
    private lateinit var ttsManager: TTSManager
    private lateinit var deviceDispatcher: DeviceActionDispatcher

    private val composeLifecycleOwner = StandaloneComposeLifecycleOwner()

    // State for Floating Overlay UI
    private val orbState = mutableStateOf(OrbState.IDLE)
    private val audioRmsLevel = mutableStateOf(0f)
    private val displayMessage = mutableStateOf("")
    private val statusText = mutableStateOf("")
    private val isSnappedToRight = mutableStateOf(false)

    private var dismissMessageJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        preferencesManager = PreferencesManager(this)
        database = AppDatabase.getDatabase(this)
        aiRepository = AIRepositoryImpl()
        speechManager = SpeechManager(this)
        ttsManager = NeuralTtsManager(this)
        deviceDispatcher = DeviceActionDispatcher(this)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        FloatingNovaController.setOverlayRunning(true)

        composeLifecycleOwner.onCreate()

        initFloatingView()
        observeSpeechAndTts()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START_VOICE -> {
                startVoiceRecognition()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun initFloatingView() {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val displayMetrics = resources.displayMetrics
        val initialY = (displayMetrics.heightPixels * 0.35f).toInt()

        windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = initialY
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(composeLifecycleOwner)
            setViewTreeViewModelStoreOwner(composeLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(composeLifecycleOwner)

            setContent {
                NovaTheme(darkTheme = true) {
                    val currentOrbState by orbState
                    val currentAudioLevel by audioRmsLevel
                    val currentMessage by displayMessage
                    val currentStatus by statusText
                    val snappedRight by isSnappedToRight

                    FloatingOrbUI(
                        orbState = currentOrbState,
                        audioLevel = currentAudioLevel,
                        displayMessage = currentMessage,
                        statusText = currentStatus,
                        isSnappedToRight = snappedRight,
                        onOrbTap = {
                            handleOrbTap()
                        },
                        onOrbDoubleTap = {
                            openMainActivity()
                        },
                        onOrbLongPress = {
                            // Quick options menu toggled inside FloatingOrbUI
                        },
                        onDragDelta = { dx, dy ->
                            handleDragDelta(dx, dy)
                        },
                        onDragEnded = {
                            handleDragEnded()
                        },
                        onOpenApp = {
                            openMainActivity()
                        },
                        onDismissPill = {
                            displayMessage.value = ""
                            statusText.value = ""
                        },
                        onCloseOverlay = {
                            stopSelf()
                        }
                    )
                }
            }
        }

        try {
            windowManager.addView(composeView, windowParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleDragDelta(dx: Float, dy: Float) {
        windowParams.x += dx.toInt()
        windowParams.y += dy.toInt()

        val displayMetrics = resources.displayMetrics
        val minY = 60
        val maxY = displayMetrics.heightPixels - 200

        if (windowParams.y < minY) windowParams.y = minY
        if (windowParams.y > maxY) windowParams.y = maxY

        if (composeView?.isAttachedToWindow == true) {
            try {
                windowManager.updateViewLayout(composeView, windowParams)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleDragEnded() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val orbMidX = windowParams.x + 40

        val snapRight = orbMidX > screenWidth / 2
        isSnappedToRight.value = snapRight

        val targetX = if (snapRight) {
            screenWidth - 100
        } else {
            16
        }

        animateSnapToEdge(windowParams.x, targetX)
    }

    private fun animateSnapToEdge(fromX: Int, toX: Int) {
        val animator = ValueAnimator.ofInt(fromX, toX).apply {
            duration = 260
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                windowParams.x = anim.animatedValue as Int
                if (composeView?.isAttachedToWindow == true) {
                    try {
                        windowManager.updateViewLayout(composeView, windowParams)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        animator.start()
    }

    private fun handleOrbTap() {
        // If speaking, cancel speech
        if (ttsManager.isSpeaking.value) {
            ttsManager.stop()
            orbState.value = OrbState.IDLE
            displayMessage.value = ""
            return
        }

        // If listening, cancel listening
        if (speechManager.isListening.value) {
            speechManager.stopListening()
            orbState.value = OrbState.IDLE
            displayMessage.value = ""
            return
        }

        // Start listening
        startVoiceRecognition()
    }

    private fun startVoiceRecognition() {
        val hasMicPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasMicPermission) {
            displayMessage.value = "Microphone permission required. Open app to grant."
            schedulePillDismissal()
            return
        }

        ttsManager.stop()
        orbState.value = OrbState.LISTENING
        statusText.value = "Listening to Boss..."
        displayMessage.value = ""

        val userSettings = preferencesManager.settings.value
        val langCode = if (userSettings.language == AppLanguage.ENGLISH) "en-US" else "bn-BD"

        speechManager.startListening(
            languageCode = langCode,
            onPartialResult = { partial ->
                displayMessage.value = partial
                statusText.value = "Listening..."
            },
            onFinalResult = { finalTranscript ->
                if (finalTranscript.isNotBlank()) {
                    displayMessage.value = finalTranscript
                    processAgenticQuery(finalTranscript, userSettings)
                } else {
                    orbState.value = OrbState.IDLE
                    statusText.value = ""
                }
            }
        )
    }

    private fun processAgenticQuery(query: String, settings: UserSettings) {
        orbState.value = OrbState.THINKING
        statusText.value = "Thinking..."

        serviceScope.launch {
            try {
                val isBengali = settings.language != AppLanguage.ENGLISH
                val result = aiRepository.generateAgenticResponse(
                    prompt = query,
                    conversationHistory = emptyList(),
                    language = settings.language,
                    aiStyle = settings.aiStyle,
                    assistantName = settings.assistantName,
                    preferredAddress = settings.preferredAddress
                )

                result.onSuccess { agentResponse ->
                    var finalReply = agentResponse.replyText

                    // Handle tool / device action call if Gemini requested one
                    if (agentResponse.functionCall != null) {
                        val fn = agentResponse.functionCall
                        val toolResult = deviceDispatcher.execute(fn.name, fn.args)
                        
                        if (finalReply.isBlank()) {
                            finalReply = if (isBengali) {
                                "${fn.name} সম্পন্ন হয়েছে, ${settings.preferredAddress}! ${toolResult.message}"
                            } else {
                                "${fn.name} executed successfully, ${settings.preferredAddress}! ${toolResult.message}"
                            }
                        }
                    }

                    displayMessage.value = finalReply
                    statusText.value = "NOVA"
                    orbState.value = OrbState.SPEAKING

                    // Save to local history database so it shows up in conversation history
                    saveInteractionToHistory(query, finalReply)

                    // Speak the response using Ultra-Realistic Neural TTS
                    if (settings.isVoiceEnabled) {
                        ttsManager.speak(
                            text = finalReply,
                            isBengali = isBengali,
                            speed = settings.speechSpeed
                        )
                    } else {
                        orbState.value = OrbState.IDLE
                        schedulePillDismissal()
                    }
                }.onFailure { err ->
                    orbState.value = OrbState.ERROR
                    displayMessage.value = "Error: ${err.localizedMessage ?: "Failed to connect"}"
                    delay(3000)
                    orbState.value = OrbState.IDLE
                    schedulePillDismissal()
                }
            } catch (e: Exception) {
                orbState.value = OrbState.ERROR
                displayMessage.value = "Error: ${e.message}"
                delay(3000)
                orbState.value = OrbState.IDLE
                schedulePillDismissal()
            }
        }
    }

    private fun saveInteractionToHistory(userPrompt: String, aiReply: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()
                val convId = "floating_overlay_session"
                val conv = com.example.data.local.entity.ConversationEntity(
                    id = convId,
                    title = "Jarvis Floating Session",
                    updatedAt = now,
                    previewText = aiReply.take(60),
                    language = preferencesManager.settings.value.language.code
                )
                database.chatDao().insertConversation(conv)

                val userMsg = ChatMessageEntity(
                    conversationId = convId,
                    sender = "user",
                    text = userPrompt,
                    timestamp = now
                )
                val aiMsg = ChatMessageEntity(
                    conversationId = convId,
                    sender = "ai",
                    text = aiReply,
                    timestamp = now + 1
                )
                database.chatDao().insertMessage(userMsg)
                database.chatDao().insertMessage(aiMsg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun observeSpeechAndTts() {
        // Track audio rms level
        serviceScope.launch {
            speechManager.rmsLevel.collectLatest { level ->
                audioRmsLevel.value = level
            }
        }

        // Track TTS playback state
        serviceScope.launch {
            ttsManager.isSpeaking.collectLatest { speaking ->
                if (!speaking && orbState.value == OrbState.SPEAKING) {
                    orbState.value = OrbState.IDLE
                    statusText.value = ""
                    schedulePillDismissal()
                }
            }
        }
    }

    private fun schedulePillDismissal() {
        dismissMessageJob?.cancel()
        dismissMessageJob = serviceScope.launch {
            delay(5000)
            if (orbState.value == OrbState.IDLE) {
                displayMessage.value = ""
                statusText.value = ""
            }
        }
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NOVA AI Floating Jarvis Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Displays the floating Jarvis orb overlay on top of all apps"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, FloatingNovaService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NOVA AI Assistant Active")
            .setContentText("Jarvis Floating Orb is active on your screen")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Open NOVA", openIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Close Overlay", stopIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        FloatingNovaController.setOverlayRunning(false)
        preferencesManager.updateFloatingOverlayEnabled(false)

        speechManager.destroy()
        ttsManager.shutdown()
        serviceJob.cancel()

        composeView?.let { view ->
            if (view.isAttachedToWindow) {
                try {
                    windowManager.removeView(view)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        composeView = null
        composeLifecycleOwner.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.example.service.action.START_FLOATING"
        const val ACTION_STOP = "com.example.service.action.STOP_FLOATING"
        const val ACTION_START_VOICE = "com.example.service.action.START_VOICE"
        private const val NOTIFICATION_ID = 2024
        private const val CHANNEL_ID = "nova_floating_overlay_channel"
    }
}

/**
 * Standalone Lifecycle, ViewModelStore, and SavedStateRegistry owner for ComposeView inside an Android Service.
 */
class StandaloneComposeLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    fun onCreate() {
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}
