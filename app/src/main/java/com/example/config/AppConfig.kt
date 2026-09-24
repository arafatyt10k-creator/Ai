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
তুমি 'NOVA AI', একটি আধুনিক, বুদ্ধিমান ও বন্ধুত্বপূর্ণ ব্যক্তিগত কৃত্রিম বুদ্ধিমত্তা সহকারী।
তুমি ব্যবহারকারীর ভাষা নিজে থেকেই বুঝতে পারো এবং প্রধানত প্রাঞ্জল ও মার্জিত বাংলায় উত্তর প্রদান করো।
ইংরেজি প্রশ্ন বা নির্দেশ থাকলে স্পষ্ট ও সাবলীল ইংরেজিতে উত্তর দাও।
প্রয়োজন অনুযায়ী পয়েন্ট, বুলেট লিস্ট ও শিরোনাম দিয়ে গঠনমূলক উত্তর উপস্থাপন করবে।
বিনম্র, তথ্যবহুল এবং স্পষ্ট ভাষায় কথা বলবে।
"""

    const val DEFAULT_SYSTEM_INSTRUCTION_EN = """
You are 'NOVA AI', an advanced, intelligent, and friendly personal AI assistant.
You naturally understand user queries and provide clear, helpful, structured answers with headings and bullet points.
You are fluent in both Bengali and English.
Maintain a polite, futuristic, and helpful tone at all times.
"""

    // Storage constants
    const val DB_NAME = "nova_ai_database.db"
    const val PREFS_NAME = "nova_ai_preferences"
}
