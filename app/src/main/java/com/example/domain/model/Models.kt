package com.example.domain.model

enum class OrbState {
    IDLE,
    WAKE_DETECTED,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

enum class AppScreen {
    HOME,
    CHAT,
    VOICE,
    TOOLS,
    PROFILE,
    STUDY,
    CREATOR,
    TRANSLATOR,
    NOTES,
    TASKS,
    IMAGE_ANALYSIS,
    MEMORY,
    CHAT_HISTORY,
    SETTINGS,
    ONBOARDING
}

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    BENGALI("bn", "Bengali", "বাংলা"),
    ENGLISH("en", "English", "English"),
    AUTO("auto", "Auto Detect", "স্বয়ংক্রিয়")
}

enum class ThemeMode(val titleEn: String, val titleBn: String) {
    DARK("Dark Theme", "ডার্ক থিম"),
    LIGHT("Light Theme", "লাইট থিম"),
    SYSTEM("System Default", "সিস্টেম ডিফল্ট")
}

enum class AiResponseStyle(val titleEn: String, val titleBn: String, val promptModifier: String) {
    SHORT("Short & Crisp", "সংক্ষিপ্ত ও স্পষ্ট", "Keep answers concise, direct, and under 3 sentences unless asked otherwise."),
    BALANCED("Balanced", "ভারসাম্যপূর্ণ", "Provide clear, standard-length answers with brief key points."),
    DETAILED("In-depth & Detailed", "বিস্তারিত ও গভীর", "Provide comprehensive, deeply detailed explanations with examples, steps, and subheadings.")
}

enum class StudySubject(val titleEn: String, val titleBn: String, val iconName: String) {
    MATHEMATICS("Mathematics", "গণিত", "functions"),
    ENGLISH("English", "ইংরেজি", "language"),
    SCIENCE("Science", "বিজ্ঞান", "science"),
    GENERAL_KNOWLEDGE("General Knowledge", "সাধারণ জ্ঞান", "public"),
    ICT("ICT & Tech", "তথ্য ও যোগাযোগ প্রযুক্তি", "computer"),
    OTHER("Other Subjects", "অন্যান্য বিষয়", "auto_stories")
}

enum class StudyMode(val titleEn: String, val titleBn: String, val descriptionBn: String) {
    EXPLAIN_SIMPLE("Explain Simply", "সহজে বুঝাও", "সহজ ভাষায় শিশুতোষ বা মূল ধারণা ব্যাখ্যা"),
    EXPLAIN_DETAIL("Explain in Detail", "বিস্তারিত ব্যাখ্যা", "গভীর তত্ত্ব, সূত্র ও উদাহরণসহ বিশ্লেষণ"),
    STEP_BY_STEP("Step-by-Step", "ধাপে ধাপে শিক্ষা", "লজিক্যাল ধারাবাহিক ধাপে পাঠ"),
    QUIZ_MCQ("MCQ Quiz", "কুইজ (MCQ)", "৪টি বিকল্পসহ স্বয়ংক্রিয় বহু নির্বাচনী প্রশ্ন"),
    FLASHCARDS("Flashcards", "ফ্ল্যাশ কার্ড", "দ্রুত মুখস্থ ও রিভিশন কার্ড"),
    SUMMARY("Key Summary", "মূল সারাংশ", "গুরুত্বপূর্ণ পয়েন্টের এক নজরে সারসংক্ষেপ"),
    PRACTICE("Practice Questions", "অনুশীলন প্রশ্ন", "পরীক্ষার জন্য মডেল প্রশ্নোত্তর")
}

enum class CreatorToolType(val titleEn: String, val titleBn: String, val hintBn: String) {
    YOUTUBE_TITLE("YouTube Title Generator", "ইউটিউব টাইটেল জেনারেটর", "ভিডিওর মূল বিষয় লিখুন"),
    VIDEO_DESCRIPTION("Video Description", "ভিডিও ডেসক্রিপশন", "ভিডিওর বিষয় ও লিংক বা কী-ওয়ার্ড দিন"),
    CAPTION("Social Media Caption", "সোশ্যাল মিডিয়া ক্যাপশন", "পোস্ট বা ফটোর কনটেক্সট লিখুন"),
    HASHTAGS("Hashtag Suggestions", "হ্যাশট্যাগ সাজেশন", "বিষয় বা ট্রেন্ড উল্লেখ করুন"),
    VIDEO_SCRIPT("Video Script", "ভিডিও চিত্রনাট্য/স্ক্রিপ্ট", "ভিডিওর মূল উদ্দেশ্য ও দৈর্ঘ্য লিখুন"),
    SHORTS_IDEA("Shorts & Reels Idea", "শর্টস ও রিলস আইডিয়া", "আপনার চ্যানেলের ক্যাটাগরি বা বিষয়"),
    THUMBNAIL_TEXT("Thumbnail Text Ideas", "থাম্বনেইল টেক্সট আইডিয়া", "আকর্ষণীয় থাম্বনেইল হুক তৈরি"),
    CONTENT_CALENDAR("Content Calendar", "কনটেন্ট ক্যালেন্ডার", "১ সপ্তাহ বা ১ মাসের প্ল্যানিং")
}

enum class WritingTone(val titleEn: String, val titleBn: String) {
    FRIENDLY("Friendly", "বন্ধুত্বপূর্ণ"),
    PROFESSIONAL("Professional", "পেশাদার"),
    CREATIVE("Creative", "সৃজনশীল"),
    SIMPLE("Simple & Clear", "সহজ ও প্রাঞ্জল")
}

enum class WritingLength(val titleEn: String, val titleBn: String) {
    SHORT("Short", "সংক্ষিপ্ত"),
    MEDIUM("Medium", "মাঝারি"),
    DETAILED("Detailed", "বিস্তারিত")
}

enum class WritingTemplate(val titleEn: String, val titleBn: String) {
    SOCIAL_POST("Social Media Post", "সোশ্যাল পোস্ট"),
    CAPTION("Engaging Caption", "ক্যাপশন"),
    VIDEO_SCRIPT("Video Script", "ভিডিও স্ক্রিপ্ট"),
    FORMAL_MESSAGE("Formal Message", "আনুষ্ঠানিক বার্তা"),
    EMAIL_DRAFT("Email Draft", "ইমেইল ড্রাফট"),
    STORY_IDEA("Story & Fiction Idea", "গল্পের প্লট"),
    PARAGRAPH("Paragraph Writing", "অনুচ্ছেদ"),
    SUMMARY("Document Summary", "সারাংশ")
}

enum class TaskPriority(val titleEn: String, val titleBn: String, val colorHex: Long) {
    LOW("Low", "কম", 0xFF10B981),
    MEDIUM("Medium", "মাঝারি", 0xFFF59E0B),
    HIGH("High", "বেশি", 0xFFF97316),
    URGENT("Urgent", "জরুরি", 0xFFEF4444)
}

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class FlashCardItem(
    val front: String,
    val back: String
)
