package com.example.data.repository

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.agent.GeminiToolDefinitions
import com.example.config.AppConfig
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.remote.*
import com.example.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

data class AgenticAiResponse(
    val replyText: String,
    val functionCall: GeminiFunctionCall? = null
)

interface AIRepository {
    suspend fun generateAgenticResponse(
        prompt: String,
        conversationHistory: List<ChatMessageEntity> = emptyList(),
        imageBitmap: Bitmap? = null,
        systemInstruction: String? = null,
        language: AppLanguage = AppLanguage.BENGALI,
        aiStyle: AiResponseStyle = AiResponseStyle.BALANCED,
        assistantName: String = "NOVA",
        preferredAddress: String = "Boss"
    ): Result<AgenticAiResponse>

    suspend fun generateResponse(
        prompt: String,
        conversationHistory: List<ChatMessageEntity> = emptyList(),
        imageBitmap: Bitmap? = null,
        systemInstruction: String? = null,
        language: AppLanguage = AppLanguage.BENGALI,
        aiStyle: AiResponseStyle = AiResponseStyle.BALANCED,
        assistantName: String = "NOVA",
        preferredAddress: String = "Boss"
    ): Result<String>

    suspend fun sendFunctionResponse(
        functionName: String,
        functionResult: String,
        conversationHistory: List<ChatMessageEntity> = emptyList(),
        assistantName: String = "NOVA",
        preferredAddress: String = "Boss",
        language: AppLanguage = AppLanguage.BENGALI
    ): Result<String>

    suspend fun translateText(
        text: String,
        sourceLang: String,
        targetLang: String
    ): Result<String>

    suspend fun generateStudyContent(
        subject: StudySubject,
        mode: StudyMode,
        topic: String,
        language: AppLanguage = AppLanguage.BENGALI
    ): Result<String>

    suspend fun generateCreatorContent(
        tool: CreatorToolType,
        input: String,
        language: AppLanguage = AppLanguage.BENGALI
    ): Result<String>

    suspend fun generateWritingContent(
        template: WritingTemplate,
        topic: String,
        tone: WritingTone,
        length: WritingLength,
        language: AppLanguage = AppLanguage.BENGALI
    ): Result<String>

    suspend fun parseTaskFromNaturalLanguage(
        input: String,
        language: AppLanguage = AppLanguage.BENGALI
    ): Result<ParsedTaskInfo>
}

data class ParsedTaskInfo(
    val title: String,
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDateMillis: Long? = null,
    val timeString: String? = null,
    val originalText: String = ""
)

class AIRepositoryImpl(
    private val apiService: GeminiApiService = GeminiApiService.create()
) : AIRepository {

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
    
    private fun getApiKey(): String {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            throw Exception("MISSING_API_KEY")
        }
        return apiKey
    }

    override suspend fun generateAgenticResponse(
        prompt: String,
        conversationHistory: List<ChatMessageEntity>,
        imageBitmap: Bitmap?,
        systemInstruction: String?,
        language: AppLanguage,
        aiStyle: AiResponseStyle,
        assistantName: String,
        preferredAddress: String
    ): Result<AgenticAiResponse> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            val model = if (imageBitmap != null) AppConfig.DEFAULT_IMAGE_MODEL else AppConfig.DEFAULT_TEXT_MODEL
            
            var baseSysText = (systemInstruction ?: if (language == AppLanguage.ENGLISH) {
                AppConfig.DEFAULT_SYSTEM_INSTRUCTION_EN
            } else {
                AppConfig.DEFAULT_SYSTEM_INSTRUCTION_BN
            })
            
            baseSysText = baseSysText
                .replace("{{ASSISTANT_NAME}}", assistantName)
                .replace("{{ADDRESS}}", preferredAddress)
            
            val sysText = baseSysText + "\n" + aiStyle.promptModifier

            val contents = mutableListOf<GeminiContent>()
            
            // Add recent history turns for context
            val recentHistory = conversationHistory.takeLast(6)
            for (msg in recentHistory) {
                val role = if (msg.sender == "user") "user" else "model"
                contents.add(
                    GeminiContent(
                        role = role,
                        parts = listOf(GeminiPart(text = msg.text))
                    )
                )
            }
            
            // Current user message
            val currentParts = mutableListOf<GeminiPart>()
            currentParts.add(GeminiPart(text = prompt))
            if (imageBitmap != null) {
                val base64Image = imageBitmap.toBase64()
                currentParts.add(
                    GeminiPart(
                        inlineData = GeminiInlineData(
                            mimeType = "image/jpeg",
                            data = base64Image
                        )
                    )
                )
            }
            
            contents.add(GeminiContent(role = "user", parts = currentParts))

            val request = GeminiRequest(
                contents = contents,
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = sysText))
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.7f,
                    maxOutputTokens = if (aiStyle == AiResponseStyle.SHORT) 512 else 2048
                ),
                tools = GeminiToolDefinitions.allTools
            )

            val response = apiService.generateContent(model, apiKey, request)
            val candidateParts = response.candidates?.firstOrNull()?.content?.parts ?: emptyList()
            
            // 1. Check for native Gemini function call
            val funcPart = candidateParts.firstOrNull { it.functionCall != null }
            if (funcPart?.functionCall != null) {
                val call = funcPart.functionCall
                val text = candidateParts.firstOrNull { !it.text.isNullOrBlank() }?.text ?: ""
                return@withContext Result.success(
                    AgenticAiResponse(
                        replyText = text.trim(),
                        functionCall = call
                    )
                )
            }

            // 2. Standard text response
            val textResult = candidateParts.firstOrNull { !it.text.isNullOrBlank() }?.text
            if (!textResult.isNullOrBlank()) {
                Result.success(AgenticAiResponse(replyText = textResult.trim()))
            } else {
                Result.failure(Exception("EMPTY_RESPONSE"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateResponse(
        prompt: String,
        conversationHistory: List<ChatMessageEntity>,
        imageBitmap: Bitmap?,
        systemInstruction: String?,
        language: AppLanguage,
        aiStyle: AiResponseStyle,
        assistantName: String,
        preferredAddress: String
    ): Result<String> {
        val result = generateAgenticResponse(
            prompt = prompt,
            conversationHistory = conversationHistory,
            imageBitmap = imageBitmap,
            systemInstruction = systemInstruction,
            language = language,
            aiStyle = aiStyle,
            assistantName = assistantName,
            preferredAddress = preferredAddress
        )
        return result.map { it.replyText }
    }

    override suspend fun sendFunctionResponse(
        functionName: String,
        functionResult: String,
        conversationHistory: List<ChatMessageEntity>,
        assistantName: String,
        preferredAddress: String,
        language: AppLanguage
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            val sysInstruction = if (language == AppLanguage.ENGLISH) {
                AppConfig.DEFAULT_SYSTEM_INSTRUCTION_EN
            } else {
                AppConfig.DEFAULT_SYSTEM_INSTRUCTION_BN
            }.replace("{{ASSISTANT_NAME}}", assistantName).replace("{{ADDRESS}}", preferredAddress)

            val contents = mutableListOf<GeminiContent>()
            val recentHistory = conversationHistory.takeLast(4)
            for (msg in recentHistory) {
                val role = if (msg.sender == "user") "user" else "model"
                contents.add(GeminiContent(role = role, parts = listOf(GeminiPart(text = msg.text))))
            }

            // Send tool result back as function turn
            contents.add(
                GeminiContent(
                    role = "function",
                    parts = listOf(
                        GeminiPart(
                            functionResponse = GeminiFunctionResponse(
                                name = functionName,
                                response = mapOf("result" to functionResult)
                            )
                        )
                    )
                )
            )

            val request = GeminiRequest(
                contents = contents,
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = sysInstruction))),
                generationConfig = GeminiGenerationConfig(temperature = 0.7f, maxOutputTokens = 512),
                tools = GeminiToolDefinitions.allTools
            )

            val response = apiService.generateContent(AppConfig.DEFAULT_TEXT_MODEL, apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                Result.success(text.trim())
            } else {
                Result.success(functionResult)
            }
        } catch (e: Exception) {
            Result.success(functionResult)
        }
    }

    override suspend fun translateText(
        text: String,
        sourceLang: String,
        targetLang: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            val prompt = "Translate the following text from $sourceLang to $targetLang accurately, keeping tone natural:\n\n$text"
            val request = GeminiRequest(
                contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt)))),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = "You are an expert bilingual translator for Bengali and English.")))
            )
            val response = apiService.generateContent(AppConfig.DEFAULT_TEXT_MODEL, apiKey, request)
            val translation = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!translation.isNullOrBlank()) {
                Result.success(translation.trim())
            } else {
                Result.failure(Exception("EMPTY_RESPONSE"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateStudyContent(
        subject: StudySubject,
        mode: StudyMode,
        topic: String,
        language: AppLanguage
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            val isBn = language == AppLanguage.BENGALI
            val prompt = if (isBn) {
                "বিষয়: ${subject.titleBn}\nটপিক: $topic\nমোড: ${mode.titleBn}\nএই বিষয়ে বিস্তারিত ও গোছানো তথ্য দিন।"
            } else {
                "Subject: ${subject.titleEn}\nTopic: $topic\nMode: ${mode.titleEn}\nPlease provide detailed and structured information."
            }
            val request = GeminiRequest(
                contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt)))),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = "You are an expert tutor. Provide accurate, clear, and structured educational content.")))
            )
            val response = apiService.generateContent(AppConfig.DEFAULT_TEXT_MODEL, apiKey, request)
            val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!resultText.isNullOrBlank()) {
                Result.success(resultText.trim())
            } else {
                Result.failure(Exception("EMPTY_RESPONSE"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateCreatorContent(
        tool: CreatorToolType,
        input: String,
        language: AppLanguage
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            val isBn = language == AppLanguage.BENGALI
            val prompt = if (isBn) {
                "টুল: ${tool.titleBn}\nবিষয়বস্তু: $input\nএই বিষয়ের উপর আকর্ষণীয় ও ভাইরাল কনটেন্ট তৈরি করুন।"
            } else {
                "Tool: ${tool.titleEn}\nInput: $input\nCreate engaging and viral content for this."
            }
            val request = GeminiRequest(
                contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt)))),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = "You are an expert social media and YouTube content creator.")))
            )
            val response = apiService.generateContent(AppConfig.DEFAULT_TEXT_MODEL, apiKey, request)
            val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!resultText.isNullOrBlank()) {
                Result.success(resultText.trim())
            } else {
                Result.failure(Exception("EMPTY_RESPONSE"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateWritingContent(
        template: WritingTemplate,
        topic: String,
        tone: WritingTone,
        length: WritingLength,
        language: AppLanguage
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            val isBn = language == AppLanguage.BENGALI
            val prompt = if (isBn) {
                "টেমপ্লেট: ${template.titleBn}\nটোন: ${tone.titleBn}\nদৈর্ঘ্য: ${length.titleBn}\nবিষয়: $topic\nঅনুগ্রহ করে সুন্দর ও গোছানো লেখা তৈরি করুন।"
            } else {
                "Template: ${template.titleEn}\nTone: ${tone.titleEn}\nLength: ${length.titleEn}\nTopic: $topic\nPlease write a well-structured text based on these parameters."
            }
            val request = GeminiRequest(
                contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt)))),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = "You are a professional writer.")))
            )
            val response = apiService.generateContent(AppConfig.DEFAULT_TEXT_MODEL, apiKey, request)
            val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!resultText.isNullOrBlank()) {
                Result.success(resultText.trim())
            } else {
                Result.failure(Exception("EMPTY_RESPONSE"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun parseTaskFromNaturalLanguage(
        input: String,
        language: AppLanguage
    ): Result<ParsedTaskInfo> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            val prompt = """
                Extract task details from this natural language input: "$input"
                Return ONLY a JSON object (no markdown, no backticks) with keys:
                "title" (string, short descriptive title)
                "description" (string, optional extra details)
                "priority" (string, one of: LOW, MEDIUM, HIGH)
                "timeString" (string, e.g., "Tomorrow 5 PM" or null if not specified)
            """.trimIndent()
            val request = GeminiRequest(
                contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt)))),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = "You are a task extractor. Only return raw JSON.")))
            )
            val response = apiService.generateContent(AppConfig.DEFAULT_TEXT_MODEL, apiKey, request)
            val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            
            // Simple parsing to avoid adding new JSON dependencies
            val titleRegex = """"title"\s*:\s*"([^"]+)"""".toRegex()
            val descRegex = """"description"\s*:\s*"([^"]*)"""".toRegex()
            val prioRegex = """"priority"\s*:\s*"([^"]+)"""".toRegex()
            val timeRegex = """"timeString"\s*:\s*"([^"]+)"""".toRegex()
            
            val title = titleRegex.find(resultText)?.groupValues?.get(1) ?: return@withContext Result.failure(Exception("Parsing failed"))
            val desc = descRegex.find(resultText)?.groupValues?.get(1) ?: ""
            val prioStr = prioRegex.find(resultText)?.groupValues?.get(1) ?: "MEDIUM"
            val priority = try { TaskPriority.valueOf(prioStr.uppercase()) } catch (e: Exception) { TaskPriority.MEDIUM }
            val timeString = timeRegex.find(resultText)?.groupValues?.get(1)
            
            Result.success(ParsedTaskInfo(
                title = title,
                description = desc,
                priority = priority,
                timeString = if (timeString == "null") null else timeString,
                originalText = input
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
