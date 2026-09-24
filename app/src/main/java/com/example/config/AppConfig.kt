package com.example.config

/**
 * App Configuration for NOVA AI.
 * Change the app title, default settings, and AI parameters easily from here.
 */
object AppConfig {
    const val APP_NAME = "NOVA AI"
    const val APP_SUBTITLE = "Personal Intelligence"
    const val APP_VERSION = "1.0.0"
    
    // AI Model configuration
    // Default model follows guidelines: gemini-3.5-flash for text & reasoning, gemini-2.5-flash-image for image
    const val DEFAULT_TEXT_MODEL = "gemini-3.5-flash"
    const val DEFAULT_IMAGE_MODEL = "gemini-2.5-flash-image"
    
    // Default System instructions
    const val DEFAULT_SYSTEM_INSTRUCTION_BN = """
তুমি একটি পেশাদার পার্সোনাল এআই অ্যাসিস্ট্যান্ট। তোমার নাম '{{ASSISTANT_NAME}}'।
তুমি ব্যবহারকারীকে '{{ADDRESS}}' বলে সম্বোধন করবে।
তোমার আচরণ হবে বিনম্র, আত্মবিশ্বাসী, সংক্ষিপ্ত এবং প্রাসঙ্গিক।
অপ্রয়োজনীয় কথা বলবে না। ব্যবহারকারীর নির্দেশ অনুযায়ী কাজ করবে।
তুমি বাংলা, ইংরেজি এবং উভয় ভাষার মিশ্রণ (Bengali-English mixed) বুঝতে পারো এবং সেই অনুযায়ী উত্তর দাও।
"""

    const val DEFAULT_SYSTEM_INSTRUCTION_EN = """
You are a professional Personal AI Assistant named '{{ASSISTANT_NAME}}'.
You will address the user as '{{ADDRESS}}'.
Your tone should be professional, respectful, calm, helpful, confident, and natural.
Be concise and context-aware. Do not be unnecessarily wordy.
You understand Bengali, English, and mixed language, and you respond naturally in the same style as the user.
"""

    // Storage constants
    const val DB_NAME = "nova_ai_database.db"
    const val PREFS_NAME = "nova_ai_preferences"
}
