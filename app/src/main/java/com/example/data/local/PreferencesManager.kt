package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.config.AppConfig
import com.example.domain.model.AiResponseStyle
import com.example.domain.model.AppLanguage
import com.example.domain.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val language: AppLanguage = AppLanguage.BENGALI,
    val isVoiceEnabled: Boolean = true,
    val speechSpeed: Float = 1.0f,
    val voiceLanguage: String = "bn-BD",
    val aiStyle: AiResponseStyle = AiResponseStyle.BALANCED,
    val isMemoryEnabled: Boolean = true,
    val userName: String = "Explorer",
    val assistantName: String = "NOVA",
    val preferredAddress: String = "Boss",
    val isWakeWordEnabled: Boolean = true,
    val isFloatingOverlayEnabled: Boolean = false,
    val isOnboardingCompleted: Boolean = false
)

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(AppConfig.PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val themeStr = prefs.getString(KEY_THEME, ThemeMode.DARK.name) ?: ThemeMode.DARK.name
        val langStr = prefs.getString(KEY_LANG, AppLanguage.BENGALI.name) ?: AppLanguage.BENGALI.name
        val voiceEnabled = prefs.getBoolean(KEY_VOICE_ENABLED, true)
        val speechSpeed = prefs.getFloat(KEY_SPEECH_SPEED, 1.0f)
        val voiceLang = prefs.getString(KEY_VOICE_LANG, "bn-BD") ?: "bn-BD"
        val aiStyleStr = prefs.getString(KEY_AI_STYLE, AiResponseStyle.BALANCED.name) ?: AiResponseStyle.BALANCED.name
        val memoryEnabled = prefs.getBoolean(KEY_MEMORY_ENABLED, true)
        val userName = prefs.getString(KEY_USER_NAME, "User") ?: "User"
        val assistantName = prefs.getString(KEY_ASSISTANT_NAME, "NOVA") ?: "NOVA"
        val preferredAddress = prefs.getString(KEY_PREFERRED_ADDRESS, "Boss") ?: "Boss"
        val wakeWordEnabled = prefs.getBoolean(KEY_WAKE_WORD_ENABLED, true)
        val floatingOverlayEnabled = prefs.getBoolean(KEY_FLOATING_OVERLAY_ENABLED, false)
        val onboarding = prefs.getBoolean(KEY_ONBOARDING, false)

        return UserSettings(
            themeMode = try { ThemeMode.valueOf(themeStr) } catch (e: Exception) { ThemeMode.DARK },
            language = try { AppLanguage.valueOf(langStr) } catch (e: Exception) { AppLanguage.BENGALI },
            isVoiceEnabled = voiceEnabled,
            speechSpeed = speechSpeed,
            voiceLanguage = voiceLang,
            aiStyle = try { AiResponseStyle.valueOf(aiStyleStr) } catch (e: Exception) { AiResponseStyle.BALANCED },
            isMemoryEnabled = memoryEnabled,
            userName = userName,
            assistantName = assistantName,
            preferredAddress = preferredAddress,
            isWakeWordEnabled = wakeWordEnabled,
            isFloatingOverlayEnabled = floatingOverlayEnabled,
            isOnboardingCompleted = onboarding
        )
    }

    fun updateTheme(themeMode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, themeMode.name).apply()
        _settings.value = _settings.value.copy(themeMode = themeMode)
    }

    fun updateLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANG, language.name).apply()
        _settings.value = _settings.value.copy(language = language)
    }

    fun updateVoiceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VOICE_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isVoiceEnabled = enabled)
    }

    fun updateSpeechSpeed(speed: Float) {
        prefs.edit().putFloat(KEY_SPEECH_SPEED, speed).apply()
        _settings.value = _settings.value.copy(speechSpeed = speed)
    }

    fun updateVoiceLanguage(voiceLang: String) {
        prefs.edit().putString(KEY_VOICE_LANG, voiceLang).apply()
        _settings.value = _settings.value.copy(voiceLanguage = voiceLang)
    }

    fun updateAiStyle(style: AiResponseStyle) {
        prefs.edit().putString(KEY_AI_STYLE, style.name).apply()
        _settings.value = _settings.value.copy(aiStyle = style)
    }

    fun updateMemoryEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MEMORY_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isMemoryEnabled = enabled)
    }

    fun updateUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
        _settings.value = _settings.value.copy(userName = name)
    }

    fun updateAssistantName(name: String) {
        prefs.edit().putString(KEY_ASSISTANT_NAME, name).apply()
        _settings.value = _settings.value.copy(assistantName = name)
    }

    fun updatePreferredAddress(address: String) {
        prefs.edit().putString(KEY_PREFERRED_ADDRESS, address).apply()
        _settings.value = _settings.value.copy(preferredAddress = address)
    }

    fun updateWakeWordEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WAKE_WORD_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isWakeWordEnabled = enabled)
    }

    fun updateFloatingOverlayEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FLOATING_OVERLAY_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isFloatingOverlayEnabled = enabled)
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean(KEY_ONBOARDING, true).apply()
        _settings.value = _settings.value.copy(isOnboardingCompleted = true)
    }

    companion object {
        private const val KEY_THEME = "key_theme"
        private const val KEY_LANG = "key_lang"
        private const val KEY_VOICE_ENABLED = "key_voice_enabled"
        private const val KEY_SPEECH_SPEED = "key_speech_speed"
        private const val KEY_VOICE_LANG = "key_voice_lang"
        private const val KEY_AI_STYLE = "key_ai_style"
        private const val KEY_MEMORY_ENABLED = "key_memory_enabled"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_ASSISTANT_NAME = "key_assistant_name"
        private const val KEY_PREFERRED_ADDRESS = "key_preferred_address"
        private const val KEY_WAKE_WORD_ENABLED = "key_wake_word_enabled"
        private const val KEY_FLOATING_OVERLAY_ENABLED = "key_floating_overlay_enabled"
        private const val KEY_ONBOARDING = "key_onboarding"
    }
}
