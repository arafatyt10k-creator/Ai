package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.local.UserSettings
import com.example.data.local.entity.*
import com.example.data.repository.*
import com.example.domain.model.*
import com.example.utils.AppUtils
import com.example.utils.SpeechManager
import com.example.utils.TtsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val preferencesManager = PreferencesManager(application)

    val chatRepository: ChatRepository = ChatRepositoryImpl(db.chatDao())
    val notesRepository: NotesRepository = NotesRepositoryImpl(db.noteDao())
    val taskRepository: TaskRepository = TaskRepositoryImpl(db.taskDao())
    val memoryRepository: MemoryRepository = MemoryRepositoryImpl(db.memoryDao())
    val aiRepository: AIRepository = AIRepositoryImpl()

    val speechManager = SpeechManager(application)
    val ttsManager = TtsManager(application)

    // App Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _screenHistory = mutableListOf<AppScreen>()

    // Orb State
    private val _orbState = MutableStateFlow(OrbState.IDLE)
    val orbState: StateFlow<OrbState> = _orbState.asStateFlow()

    // Settings
    val userSettings: StateFlow<UserSettings> = preferencesManager.settings

    // Chat State
    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    private val _selectedImageBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedImageBitmap: StateFlow<Bitmap?> = _selectedImageBitmap.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    // Active Chat Messages Flow
    val activeMessages: StateFlow<List<ChatMessageEntity>> = _activeConversationId
        .flatMapLatest { convId ->
            if (convId == null) flowOf(emptyList())
            else chatRepository.getMessagesForConversation(convId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Conversations Flow
    val conversations: StateFlow<List<ConversationEntity>> = chatRepository.getAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notes Flow
    private val _notesSearchQuery = MutableStateFlow("")
    val notesSearchQuery: StateFlow<String> = _notesSearchQuery.asStateFlow()

    val notes: StateFlow<List<NoteEntity>> = _notesSearchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) notesRepository.getAllNotes()
            else notesRepository.searchNotes(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val noteCategories: StateFlow<List<String>> = notesRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tasks Flow
    val tasks: StateFlow<List<TaskEntity>> = taskRepository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Memories Flow
    val memories: StateFlow<List<MemoryEntity>> = memoryRepository.getAllMemories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Study Assistant State
    private val _selectedStudySubject = MutableStateFlow(StudySubject.MATHEMATICS)
    val selectedStudySubject: StateFlow<StudySubject> = _selectedStudySubject.asStateFlow()

    private val _selectedStudyMode = MutableStateFlow(StudyMode.EXPLAIN_SIMPLE)
    val selectedStudyMode: StateFlow<StudyMode> = _selectedStudyMode.asStateFlow()

    private val _studyTopic = MutableStateFlow("")
    val studyTopic: StateFlow<String> = _studyTopic.asStateFlow()

    private val _studyResult = MutableStateFlow<String?>(null)
    val studyResult: StateFlow<String?> = _studyResult.asStateFlow()

    private val _isStudyLoading = MutableStateFlow(false)
    val isStudyLoading: StateFlow<Boolean> = _isStudyLoading.asStateFlow()

    // Creator Assistant State
    private val _selectedCreatorTool = MutableStateFlow(CreatorToolType.YOUTUBE_TITLE)
    val selectedCreatorTool: StateFlow<CreatorToolType> = _selectedCreatorTool.asStateFlow()

    private val _creatorInput = MutableStateFlow("")
    val creatorInput: StateFlow<String> = _creatorInput.asStateFlow()

    private val _creatorResult = MutableStateFlow<String?>(null)
    val creatorResult: StateFlow<String?> = _creatorResult.asStateFlow()

    private val _isCreatorLoading = MutableStateFlow(false)
    val isCreatorLoading: StateFlow<Boolean> = _isCreatorLoading.asStateFlow()

    // Writing Assistant State
    private val _selectedWritingTemplate = MutableStateFlow(WritingTemplate.EMAIL_DRAFT)
    val selectedWritingTemplate: StateFlow<WritingTemplate> = _selectedWritingTemplate.asStateFlow()

    private val _selectedWritingTone = MutableStateFlow(WritingTone.PROFESSIONAL)
    val selectedWritingTone: StateFlow<WritingTone> = _selectedWritingTone.asStateFlow()

    private val _selectedWritingLength = MutableStateFlow(WritingLength.MEDIUM)
    val selectedWritingLength: StateFlow<WritingLength> = _selectedWritingLength.asStateFlow()

    private val _writingTopic = MutableStateFlow("")
    val writingTopic: StateFlow<String> = _writingTopic.asStateFlow()

    private val _writingResult = MutableStateFlow<String?>(null)
    val writingResult: StateFlow<String?> = _writingResult.asStateFlow()

    private val _isWritingLoading = MutableStateFlow(false)
    val isWritingLoading: StateFlow<Boolean> = _isWritingLoading.asStateFlow()

    // Translator State
    private val _translatorSourceText = MutableStateFlow("")
    val translatorSourceText: StateFlow<String> = _translatorSourceText.asStateFlow()

    private val _translatorSourceLang = MutableStateFlow("Bengali")
    val translatorSourceLang: StateFlow<String> = _translatorSourceLang.asStateFlow()

    private val _translatorTargetLang = MutableStateFlow("English")
    val translatorTargetLang: StateFlow<String> = _translatorTargetLang.asStateFlow()

    private val _translatorResult = MutableStateFlow<String?>(null)
    val translatorResult: StateFlow<String?> = _translatorResult.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    // Task NLP state
    private val _taskNlpParsed = MutableStateFlow<ParsedTaskInfo?>(null)
    val taskNlpParsed: StateFlow<ParsedTaskInfo?> = _taskNlpParsed.asStateFlow()

    // Global snackbar / error message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        // Check onboarding
        if (!userSettings.value.isOnboardingCompleted) {
            _currentScreen.value = AppScreen.ONBOARDING
        }

        // Monitor speech & TTS states to sync with Orb
        viewModelScope.launch {
            combine(
                speechManager.isListening,
                ttsManager.isSpeaking,
                _isAiGenerating
            ) { listening, speaking, thinking ->
                when {
                    listening -> OrbState.LISTENING
                    speaking -> OrbState.SPEAKING
                    thinking -> OrbState.THINKING
                    else -> OrbState.IDLE
                }
            }.collect { newState ->
                _orbState.value = newState
            }
        }
    }

    // Navigation
    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            _screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (_screenHistory.isNotEmpty()) {
            val prev = _screenHistory.removeAt(_screenHistory.lastIndex)
            _currentScreen.value = prev
            return true
        }
        if (_currentScreen.value != AppScreen.HOME) {
            _currentScreen.value = AppScreen.HOME
            return true
        }
        return false
    }

    fun completeOnboarding(selectedLanguage: AppLanguage) {
        preferencesManager.updateLanguage(selectedLanguage)
        preferencesManager.completeOnboarding()
        _currentScreen.value = AppScreen.HOME
    }

    // Chat Operations
    fun setChatInput(text: String) {
        _chatInput.value = text
    }

    fun setSelectedImage(uri: Uri?, bitmap: Bitmap?) {
        _selectedImageUri.value = uri
        _selectedImageBitmap.value = bitmap
    }

    fun startNewChat(languageCode: String = "bn") {
        viewModelScope.launch {
            val title = if (userSettings.value.language == AppLanguage.ENGLISH) "New Chat" else "নতুন কথোপকথন"
            val id = chatRepository.createNewConversation(title, languageCode)
            _activeConversationId.value = id
            navigateTo(AppScreen.CHAT)
        }
    }

    fun selectConversation(id: String) {
        _activeConversationId.value = id
        navigateTo(AppScreen.CHAT)
    }

    fun sendMessage(userText: String? = null) {
        val textToSend = (userText ?: _chatInput.value).trim()
        if (textToSend.isBlank() && _selectedImageBitmap.value == null) return

        val imageToSend = _selectedImageBitmap.value
        val imageUriToSend = _selectedImageUri.value?.toString()

        _chatInput.value = ""
        _selectedImageBitmap.value = null
        _selectedImageUri.value = null

        viewModelScope.launch {
            var convId = _activeConversationId.value
            if (convId == null) {
                val title = if (textToSend.length > 25) textToSend.take(25) + "..." else textToSend
                convId = chatRepository.createNewConversation(title.ifBlank { "চ্যাট" })
                _activeConversationId.value = convId
            }

            // Save user message
            val langCode = userSettings.value.language.code
            chatRepository.saveMessage(convId, "user", textToSend, langCode, imageUriToSend)

            val lower = textToSend.lowercase()
            val assistantName = userSettings.value.assistantName.lowercase()
            
            // Check if it's just the wake word
            val isWakeOnly = lower == assistantName || 
                             lower == "hey $assistantName" || 
                             lower == "হে $assistantName" ||
                             lower == "নোভা" || 
                             lower == "হে নোভা" ||
                             lower == "নোভা কি শুনতে পাচ্ছ" ||
                             lower == "$assistantName, are you there"
            
            if (isWakeOnly) {
                handleWakeWordOnly(convId, langCode)
                return@launch
            }

            // AI Generation
            _isAiGenerating.value = true
            _orbState.value = OrbState.THINKING

            val history = activeMessages.value
            val result = aiRepository.generateResponse(
                prompt = textToSend,
                conversationHistory = history,
                imageBitmap = imageToSend,
                language = userSettings.value.language,
                aiStyle = userSettings.value.aiStyle,
                assistantName = userSettings.value.assistantName,
                preferredAddress = userSettings.value.preferredAddress
            )

            _isAiGenerating.value = false

            result.onSuccess { replyText ->
                var isAction = false
                try {
                    val cleanReply = replyText.replace("```json", "").replace("```", "").trim()
                    if (cleanReply.startsWith("{") && cleanReply.endsWith("}")) {
                        if (cleanReply.contains("\"action\"")) {
                            if (cleanReply.contains("\"create_note\"")) {
                                handleCreateNoteAction(cleanReply, convId, langCode)
                                isAction = true
                            } else if (cleanReply.contains("\"create_task\"")) {
                                handleCreateTaskAction(cleanReply, convId, langCode)
                                isAction = true
                            } else if (cleanReply.contains("\"open_app\"")) {
                                handleOpenAppAction(cleanReply, convId, langCode)
                                isAction = true
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore JSON parse errors
                }
                
                if (!isAction) {
                    chatRepository.saveMessage(convId, "ai", replyText, langCode)
                    if (userSettings.value.isVoiceEnabled) {
                        ttsManager.speak(
                            text = replyText,
                            isBengali = userSettings.value.language != AppLanguage.ENGLISH,
                            speed = userSettings.value.speechSpeed
                        )
                    }
                }
            }.onFailure { error ->
                val errorMsg = if (error.message == "MISSING_API_KEY") {
                    if (userSettings.value.language == AppLanguage.ENGLISH) {
                        "API Key is missing. Please configure GEMINI_API_KEY in the Secrets panel."
                    } else {
                        "API Key পাওয়া যায়নি। দয়া করে সিক্রেটস প্যানেলে GEMINI_API_KEY কনফিগার করুন।"
                    }
                } else {
                    if (userSettings.value.language == AppLanguage.ENGLISH) {
                        "Network or service error: ${error.localizedMessage ?: "Unknown error"}. Please check your connection."
                    } else {
                        "ইন্টারনেট সংযোগ পাওয়া যাচ্ছে না বা সার্ভার ত্রুটি। অনুগ্রহ করে সংযোগ পরীক্ষা করুন।"
                    }
                }
                chatRepository.saveMessage(convId, "ai", errorMsg, langCode)
                _orbState.value = OrbState.ERROR
            }
        }
    }

    private suspend fun handleWakeWordOnly(convId: String, langCode: String) {
        val address = userSettings.value.preferredAddress
        val isBn = userSettings.value.language != AppLanguage.ENGLISH
        
        val acknowledgementsBn = listOf(
            "জি $address, বলুন। কীভাবে সাহায্য করতে পারি?",
            "জি $address, বলুন।",
            "জি $address, আমি শুনছি।",
            "বলুন $address, কী করতে পারি?",
            "জি, শুনছি $address।",
            "অবশ্যই $address। বলুন।"
        )
        
        val acknowledgementsEn = listOf(
            "Yes $address, how can I help you?",
            "I'm listening $address.",
            "Yes $address, I'm here.",
            "Tell me $address, what can I do for you?",
            "Ready $address, please go ahead."
        )
        
        val reply = if (isBn) {
            acknowledgementsBn[Random.nextInt(acknowledgementsBn.size)]
        } else {
            acknowledgementsEn[Random.nextInt(acknowledgementsEn.size)]
        }
        
        _orbState.value = OrbState.WAKE_DETECTED
        delay(500)
        chatRepository.saveMessage(convId, "ai", reply, langCode)
        
        if (userSettings.value.isVoiceEnabled) {
            ttsManager.speak(reply, isBn, userSettings.value.speechSpeed)
            // Wait for speaking to finish or a timeout
            delay(1500)
        }
        
        // Auto start listening
        startVoiceListening()
    }

    private suspend fun handleCreateNoteAction(json: String, convId: String, langCode: String) {
        val titleMatch = "\"title\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(json)
        val contentMatch = "\"content\"\\s*:\\s*\"([^\"]*)\"".toRegex().find(json)
        val categoryMatch = "\"category\"\\s*:\\s*\"([^\"]*)\"".toRegex().find(json)
        
        val t = titleMatch?.groupValues?.get(1) ?: "Saved Note"
        val c = contentMatch?.groupValues?.get(1) ?: ""
        val cat = categoryMatch?.groupValues?.get(1) ?: "AI Saved"
        
        val address = userSettings.value.preferredAddress
        val ack = if (userSettings.value.language == AppLanguage.ENGLISH) {
            "Of course $address, saving that note for you."
        } else {
            "অবশ্যই $address, আপনার নোটটি সেভ করছি।"
        }
        
        chatRepository.saveMessage(convId, "ai", ack, langCode)
        if (userSettings.value.isVoiceEnabled) ttsManager.speak(ack, userSettings.value.language != AppLanguage.ENGLISH)
        
        delay(1000)
        notesRepository.addNote(t, c, cat)
        
        val done = if (userSettings.value.language == AppLanguage.ENGLISH) {
            "✅ Note saved: '$t'"
        } else {
            "✅ নোট সেভ হয়েছে: '$t'"
        }
        chatRepository.saveMessage(convId, "ai", done, langCode)
    }

    private suspend fun handleCreateTaskAction(json: String, convId: String, langCode: String) {
        val titleMatch = "\"title\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(json)
        val descMatch = "\"description\"\\s*:\\s*\"([^\"]*)\"".toRegex().find(json)
        val prioMatch = "\"priority\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(json)
        val timeMatch = "\"dueTimeString\"\\s*:\\s*\"([^\"]*)\"".toRegex().find(json)
        
        val t = titleMatch?.groupValues?.get(1) ?: "Task"
        val d = descMatch?.groupValues?.get(1) ?: ""
        val p = prioMatch?.groupValues?.get(1) ?: "MEDIUM"
        val timeStr = timeMatch?.groupValues?.get(1)?.takeIf { it.isNotBlank() && it != "null" }
        
        val address = userSettings.value.preferredAddress
        val ack = if (userSettings.value.language == AppLanguage.ENGLISH) {
            "Sure $address, I'm setting a reminder for that."
        } else {
            "অবশ্যই $address, আমি রিমাইন্ডার সেট করছি।"
        }
        
        chatRepository.saveMessage(convId, "ai", ack, langCode)
        if (userSettings.value.isVoiceEnabled) ttsManager.speak(ack, userSettings.value.language != AppLanguage.ENGLISH)
        
        delay(1000)
        taskRepository.addTask(t, d, p, null, timeStr)
        
        val done = if (userSettings.value.language == AppLanguage.ENGLISH) {
            "📌 Task created: '$t'" + (if (timeStr != null) " ($timeStr)" else "")
        } else {
            "📌 নতুন টাস্ক যুক্ত হয়েছে: '$t'" + (if (timeStr != null) " ($timeStr)" else "")
        }
        chatRepository.saveMessage(convId, "ai", done, langCode)
    }

    private suspend fun handleOpenAppAction(json: String, convId: String, langCode: String) {
        val packageMatch = "\"package\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(json)
        val appName = packageMatch?.groupValues?.get(1) ?: ""
        
        if (appName.isNotBlank()) {
            val address = userSettings.value.preferredAddress
            val ack = if (userSettings.value.language == AppLanguage.ENGLISH) {
                "Certainly $address, opening $appName."
            } else {
                "অবশ্যই $address, $appName খুলছি।"
            }
            
            chatRepository.saveMessage(convId, "ai", ack, langCode)
            if (userSettings.value.isVoiceEnabled) ttsManager.speak(ack, userSettings.value.language != AppLanguage.ENGLISH)
            
            delay(800)
            withContext(Dispatchers.Main) {
                AppUtils.openApp(getApplication(), appName)
            }
        }
    }

    fun regenerateLastMessage() {
        val messages = activeMessages.value
        val lastUserMsg = messages.lastOrNull { it.sender == "user" }
        if (lastUserMsg != null) {
            sendMessage(lastUserMsg.text)
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            chatRepository.deleteMessage(id)
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            chatRepository.deleteConversation(id)
            if (_activeConversationId.value == id) {
                _activeConversationId.value = null
            }
        }
    }

    fun renameConversation(id: String, newTitle: String) {
        viewModelScope.launch {
            chatRepository.updateConversationTitle(id, newTitle)
        }
    }

    fun clearAllChatHistory() {
        viewModelScope.launch {
            chatRepository.clearAllHistory()
            _activeConversationId.value = null
        }
    }

    // Voice Mode
    fun startVoiceListening() {
        val langCode = if (userSettings.value.language == AppLanguage.ENGLISH) "en-US" else "bn-BD"
        speechManager.startListening(
            languageCode = langCode,
            onFinalResult = { recognized ->
                if (recognized.isNotBlank()) {
                    sendMessage(recognized)
                }
            }
        )
    }

    fun stopVoiceListening() {
        speechManager.stopListening()
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    // Study Assistant
    fun setStudySubject(subject: StudySubject) {
        _selectedStudySubject.value = subject
    }

    fun setStudyMode(mode: StudyMode) {
        _selectedStudyMode.value = mode
    }

    fun setStudyTopic(topic: String) {
        _studyTopic.value = topic
    }

    fun generateStudyHelp() {
        if (_studyTopic.value.isBlank()) return
        viewModelScope.launch {
            _isStudyLoading.value = true
            _orbState.value = OrbState.THINKING
            val res = aiRepository.generateStudyContent(
                subject = _selectedStudySubject.value,
                mode = _selectedStudyMode.value,
                topic = _studyTopic.value,
                language = userSettings.value.language
            )
            _isStudyLoading.value = false
            _orbState.value = OrbState.IDLE
            _studyResult.value = res.getOrNull() ?: "দুঃখিত, তথ্য প্রক্রিয়া করতে সমস্যা হয়েছে।"
        }
    }

    // Creator Assistant
    fun setCreatorTool(tool: CreatorToolType) {
        _selectedCreatorTool.value = tool
    }

    fun setCreatorInput(input: String) {
        _creatorInput.value = input
    }

    fun generateCreatorContent() {
        if (_creatorInput.value.isBlank()) return
        viewModelScope.launch {
            _isCreatorLoading.value = true
            _orbState.value = OrbState.THINKING
            val res = aiRepository.generateCreatorContent(
                tool = _selectedCreatorTool.value,
                input = _creatorInput.value,
                language = userSettings.value.language
            )
            _isCreatorLoading.value = false
            _orbState.value = OrbState.IDLE
            _creatorResult.value = res.getOrNull() ?: "কনটেন্ট তৈরি করা সম্ভব হয়নি।"
        }
    }

    // Writing Assistant
    fun setWritingTemplate(template: WritingTemplate) {
        _selectedWritingTemplate.value = template
    }

    fun setWritingTone(tone: WritingTone) {
        _selectedWritingTone.value = tone
    }

    fun setWritingLength(length: WritingLength) {
        _selectedWritingLength.value = length
    }

    fun setWritingTopic(topic: String) {
        _writingTopic.value = topic
    }

    fun generateWritingDraft() {
        if (_writingTopic.value.isBlank()) return
        viewModelScope.launch {
            _isWritingLoading.value = true
            _orbState.value = OrbState.THINKING
            val res = aiRepository.generateWritingContent(
                template = _selectedWritingTemplate.value,
                topic = _writingTopic.value,
                tone = _selectedWritingTone.value,
                length = _selectedWritingLength.value,
                language = userSettings.value.language
            )
            _isWritingLoading.value = false
            _orbState.value = OrbState.IDLE
            _writingResult.value = res.getOrNull() ?: "লেখা তৈরি করা সম্ভব হয়নি।"
        }
    }

    // Translator
    fun setTranslatorSourceText(text: String) {
        _translatorSourceText.value = text
    }

    fun swapTranslationLanguages() {
        val temp = _translatorSourceLang.value
        _translatorSourceLang.value = _translatorTargetLang.value
        _translatorTargetLang.value = temp
        val prevResult = _translatorResult.value
        if (!prevResult.isNullOrBlank()) {
            _translatorSourceText.value = prevResult
            _translatorResult.value = ""
        }
    }

    fun translate() {
        if (_translatorSourceText.value.isBlank()) return
        viewModelScope.launch {
            _isTranslating.value = true
            _orbState.value = OrbState.THINKING
            val res = aiRepository.translateText(
                text = _translatorSourceText.value,
                sourceLang = _translatorSourceLang.value,
                targetLang = _translatorTargetLang.value
            )
            _isTranslating.value = false
            _orbState.value = OrbState.IDLE
            _translatorResult.value = res.getOrNull() ?: "অনুবাদ সম্পন্ন করা যায়নি।"
        }
    }

    // Notes
    fun setNotesSearch(query: String) {
        _notesSearchQuery.value = query
    }

    fun saveNote(title: String, content: String, category: String = "General") {
        viewModelScope.launch {
            notesRepository.addNote(title, content, category)
            _toastMessage.value = if (userSettings.value.language == AppLanguage.ENGLISH) "Note saved" else "নোট সংরক্ষিত হয়েছে"
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            notesRepository.updateNote(note)
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            notesRepository.deleteNote(id)
        }
    }

    fun togglePinNote(id: Long) {
        viewModelScope.launch {
            notesRepository.togglePin(id)
        }
    }

    fun toggleFavoriteNote(id: Long) {
        viewModelScope.launch {
            notesRepository.toggleFavorite(id)
        }
    }

    // Tasks
    fun addTask(
        title: String,
        description: String = "",
        priority: String = "MEDIUM",
        dueDateMillis: Long? = null,
        dueTimeString: String? = null
    ) {
        viewModelScope.launch {
            taskRepository.addTask(title, description, priority, dueDateMillis, dueTimeString)
            _toastMessage.value = if (userSettings.value.language == AppLanguage.ENGLISH) "Task added" else "টাস্ক যুক্ত হয়েছে"
        }
    }

    fun toggleTaskCompletion(id: Long, completed: Boolean) {
        viewModelScope.launch {
            taskRepository.setTaskCompleted(id, completed)
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            taskRepository.deleteTask(id)
        }
    }

    fun parseNaturalLanguageTask(input: String) {
        viewModelScope.launch {
            val res = aiRepository.parseTaskFromNaturalLanguage(input, userSettings.value.language)
            _taskNlpParsed.value = res.getOrNull()
        }
    }

    fun clearTaskNlpParsed() {
        _taskNlpParsed.value = null
    }

    // Memory
    fun addMemory(category: String, fact: String) {
        viewModelScope.launch {
            memoryRepository.addMemory(category, fact)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            memoryRepository.deleteMemory(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            memoryRepository.clearAllMemories()
        }
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    fun loadBitmapFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(getApplication<Application>().contentResolver, uri)
                _selectedImageBitmap.value = bitmap
                _selectedImageUri.value = uri
            } catch (e: Exception) {
                _toastMessage.value = "Failed to load image"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
        ttsManager.shutdown()
    }
}
