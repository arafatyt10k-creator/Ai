# NOVA AI - Android Voice Assistant

NOVA AI is a futuristic Android voice assistant supporting English and Bengali, powered by the Gemini API.

## 🚀 Setup & Configuration

To make the AI features functional, you **MUST** provide a valid Gemini API Key. The application is configured to read this key from a `.env` file during the build process using the Secrets Gradle Plugin.

### API Key Configuration

1. Locate or create the `.env` file in the root directory (or use `.env.example`).
2. Add your API key as follows:
   ```env
   GEMINI_API_KEY=YOUR_ACTUAL_API_KEY_HERE
   ```
3. If you are using Google AI Studio's web environment, enter your Gemini API Key in the **Secrets panel** on the left sidebar. The system will automatically inject it into the environment variables when compiling.

**Note:** If the API key is missing or invalid, the app will gracefully show an error message ("API Key is missing") within the chat interface, and no AI requests will be made.

## 🛠 Features

- **Voice Recognition:** Speak directly to the assistant in English or Bengali.
- **Multimodal AI:** Ask questions or upload images for analysis.
- **Smart Actions:** Create notes or tasks directly through voice commands (e.g., "Create a task to buy groceries").
- **Local Persistence:** Your chat history, notes, and tasks are saved securely on your device using a Room Database.
