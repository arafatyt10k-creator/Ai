package com.example.agent

import com.example.data.remote.*

object GeminiToolDefinitions {

    val allTools: List<GeminiTool> by lazy {
        listOf(
            GeminiTool(
                functionDeclarations = listOf(
                    // 1. open_app
                    GeminiFunctionDeclaration(
                        name = "open_app",
                        description = "Launch an application installed on the user's Android device by app name or package name.",
                        parameters = GeminiFunctionParameters(
                            properties = mapOf(
                                "appName" to GeminiParameterProperty(
                                    type = "STRING",
                                    description = "The common name of the application, e.g. 'YouTube', 'Chrome', 'WhatsApp', 'Camera', 'Settings', 'Calculator', 'Facebook', 'Spotify', 'Clock'"
                                ),
                                "packageName" to GeminiParameterProperty(
                                    type = "STRING",
                                    description = "Optional explicit Android package name if known (e.g. com.google.android.youtube)"
                                )
                            ),
                            required = listOf("appName")
                        )
                    ),
                    // 2. play_youtube
                    GeminiFunctionDeclaration(
                        name = "play_youtube",
                        description = "Search and automatically play a video or music query directly inside YouTube.",
                        parameters = GeminiFunctionParameters(
                            properties = mapOf(
                                "query" to GeminiParameterProperty(
                                    type = "STRING",
                                    description = "The song title, artist, or video search query to play on YouTube."
                                )
                            ),
                            required = listOf("query")
                        )
                    ),
                    // 3. make_phone_call
                    GeminiFunctionDeclaration(
                        name = "make_phone_call",
                        description = "Place a phone call to a given contact name or raw phone number.",
                        parameters = GeminiFunctionParameters(
                            properties = mapOf(
                                "contactName" to GeminiParameterProperty(
                                    type = "STRING",
                                    description = "The name of the contact to call as stored in phone contacts, e.g. 'Father', 'Rahul', 'Boss'"
                                ),
                                "phoneNumber" to GeminiParameterProperty(
                                    type = "STRING",
                                    description = "The direct phone number digits to dial if known."
                                )
                            )
                        )
                    ),
                    // 4. send_sms
                    GeminiFunctionDeclaration(
                        name = "send_sms",
                        description = "Send a text message (SMS) to a specified contact name or phone number.",
                        parameters = GeminiFunctionParameters(
                            properties = mapOf(
                                "contactName" to GeminiParameterProperty(
                                    type = "STRING",
                                    description = "The recipient's contact name if known."
                                ),
                                "phoneNumber" to GeminiParameterProperty(
                                    type = "STRING",
                                    description = "The recipient's phone number digits."
                                ),
                                "message" to GeminiParameterProperty(
                                    type = "STRING",
                                    description = "The body of the SMS message to send."
                                )
                            ),
                            required = listOf("message")
                        )
                    ),
                    // 5. device_toggle
                    GeminiFunctionDeclaration(
                        name = "device_toggle",
                        description = "Perform native hardware device actions: toggle flashlight, adjust media volume, or mute.",
                        parameters = GeminiFunctionParameters(
                            properties = mapOf(
                                "action" to GeminiParameterProperty(
                                    type = "STRING",
                                    description = "The device toggle action to execute.",
                                    enum = listOf(
                                        "toggle_flashlight",
                                        "turn_on_flashlight",
                                        "turn_off_flashlight",
                                        "increase_volume",
                                        "decrease_volume",
                                        "mute_volume"
                                    )
                                )
                            ),
                            required = listOf("action")
                        )
                    ),
                    // 6. create_note
                    GeminiFunctionDeclaration(
                        name = "create_note",
                        description = "Save information as a quick note for the user in the app database.",
                        parameters = GeminiFunctionParameters(
                            properties = mapOf(
                                "title" to GeminiParameterProperty(
                                    type = "STRING",
                                    description = "Short, concise title of the note."
                                ),
                                "content" to GeminiParameterProperty(
                                    type = "STRING",
                                    description = "The full text content of the note."
                                ),
                                "category" to GeminiParameterProperty(
                                    type = "STRING",
                                    description = "Category name, e.g., 'Work', 'Personal', 'General'."
                                )
                            ),
                            required = listOf("title", "content")
                        )
                    ),
                    // 7. create_task
                    GeminiFunctionDeclaration(
                        name = "create_task",
                        description = "Schedule or create a reminder/task in the user's task manager.",
                        parameters = GeminiFunctionParameters(
                            properties = mapOf(
                                "title" to GeminiParameterProperty(
                                    type = "STRING",
                                    description = "Title or description of the task."
                                ),
                                "description" to GeminiParameterProperty(
                                    type = "STRING",
                                    description = "Optional additional task details."
                                ),
                                "priority" to GeminiParameterProperty(
                                    type = "STRING",
                                    description = "Priority level: 'HIGH', 'MEDIUM', or 'LOW'.",
                                    enum = listOf("HIGH", "MEDIUM", "LOW")
                                ),
                                "dueTimeString" to GeminiParameterProperty(
                                    type = "STRING",
                                    description = "Mentioned due time or date string, e.g., 'Today 5 PM', 'Tomorrow 9 AM'."
                                )
                            ),
                            required = listOf("title")
                        )
                    )
                )
            )
        )
    }
}
