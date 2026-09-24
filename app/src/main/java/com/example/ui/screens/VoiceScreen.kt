package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppScreen
import com.example.domain.model.OrbState
import com.example.ui.components.AiOrb
import com.example.ui.components.FuturisticCard
import com.example.ui.components.HeaderBar
import com.example.ui.components.OrbStatusPill
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun VoiceScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.userSettings.collectAsState()
    val orbState by viewModel.orbState.collectAsState()
    val isListening by viewModel.speechManager.isListening.collectAsState()
    val rmsLevel by viewModel.speechManager.rmsLevel.collectAsState()
    val recognizedText by viewModel.speechManager.recognizedText.collectAsState()
    val errorMessage by viewModel.speechManager.errorMessage.collectAsState()
    val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()
    val messages by viewModel.activeMessages.collectAsState()

    val lastAiResponse = messages.lastOrNull { it.sender == "ai" }?.text ?: ""
    val isBengali = settings.language != AppLanguage.ENGLISH

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) {
            viewModel.startVoiceListening()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Voice Header
        HeaderBar(
            title = if (isBengali) "ভয়েস মোড" else "Voice Assistant",
            subtitle = if (isBengali) "মুখে কথা বলে নির্দেশনা দিন" else "Speak naturally with NOVA AI",
            onBack = {
                viewModel.stopVoiceListening()
                viewModel.stopSpeaking()
                viewModel.navigateBack()
            },
            trailingContent = {
                // Language quick toggle pill
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            val newLang = if (isBengali) AppLanguage.ENGLISH else AppLanguage.BENGALI
                            viewModel.preferencesManager.updateLanguage(newLang)
                        }
                        .border(1.dp, CyanNeon.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                    color = CyanNeon.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (isBengali) "বাংলা (BN)" else "English (EN)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = CyanNeon,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        )

        // Main Voice Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // AI Orb Centerpiece with sound level reaction
            AiOrb(
                state = orbState,
                size = 190.dp,
                audioLevel = rmsLevel,
                onClick = {
                    if (isSpeaking) {
                        viewModel.stopSpeaking()
                    } else if (isListening) {
                        viewModel.stopVoiceListening()
                    } else {
                        if (hasMicPermission) {
                            viewModel.startVoiceListening()
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OrbStatusPill(state = orbState, isBengali = isBengali)

            Spacer(modifier = Modifier.height(16.dp))

            // Speech Transcript Card
            FuturisticCard(
                borderColor = if (isListening) OrbListeningGreen.copy(alpha = 0.5f) else CyanNeon.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBengali) "আপনার কথা (Speech Input)" else "Your Speech Input",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = CyanNeon,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (isListening) {
                        Text(
                            text = if (isBengali) "শুনছি..." else "Listening...",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OrbListeningGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when {
                        recognizedText.isNotBlank() -> recognizedText
                        isListening -> if (isBengali) "কথা বলুন, শুনছি..." else "Please speak now..."
                        else -> if (isBengali) "মাইক্রোফোন বাটনে ট্যাপ করে কথা বলা শুরু করুন" else "Tap microphone below to start speaking"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (recognizedText.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(color = RoseError)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // AI Voice Answer Card (if available)
            if (lastAiResponse.isNotBlank()) {
                FuturisticCard(
                    borderColor = if (isSpeaking) OrbSpeakingMagenta.copy(alpha = 0.5f) else PurpleNeon.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NOVA AI Response",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = PurpleNeon,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    if (isSpeaking) {
                                        viewModel.stopSpeaking()
                                    } else {
                                        viewModel.ttsManager.speak(
                                            lastAiResponse,
                                            isBengali = isBengali,
                                            speed = settings.speechSpeed
                                        )
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = "TTS Control",
                                    tint = if (isSpeaking) RoseError else CyanNeon
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = lastAiResponse,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        maxLines = 6
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Speech Speed Rate Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBengali) "কণ্ঠের গতি (Speed)" else "Voice Speed",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f)
                    speeds.forEach { speed ->
                        val isSelected = settings.speechSpeed == speed
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.preferencesManager.updateSpeechSpeed(speed)
                                    viewModel.ttsManager.setSpeechRate(speed)
                                }
                                .border(
                                    1.dp,
                                    if (isSelected) CyanNeon else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                ),
                            color = if (isSelected) CyanNeon.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${speed}x",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) CyanNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Big Futuristic Mic Activation Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, CyanNeon.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val newVoiceState = !settings.isVoiceEnabled
                        viewModel.preferencesManager.updateVoiceEnabled(newVoiceState)
                    },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = if (settings.isVoiceEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Toggle TTS",
                        tint = if (settings.isVoiceEnabled) CyanNeon else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Central Glow Mic Button
                FloatingActionButton(
                    onClick = {
                        if (isListening) {
                            viewModel.stopVoiceListening()
                        } else {
                            if (hasMicPermission) {
                                viewModel.startVoiceListening()
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    modifier = Modifier
                        .testTag("voice_main_mic_fab")
                        .size(64.dp),
                    shape = CircleShape,
                    containerColor = if (isListening) OrbListeningGreen else CyanNeon,
                    contentColor = Color(0xFF04101A)
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Voice Record",
                        modifier = Modifier.size(30.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.CHAT) },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Keyboard Mode",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
