package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.AppConfig
import com.example.domain.model.AiResponseStyle
import com.example.domain.model.AppLanguage
import com.example.domain.model.ThemeMode
import com.example.ui.components.FuturisticCard
import com.example.ui.components.HeaderBar
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.userSettings.collectAsState()
    val isBengali = settings.language != AppLanguage.ENGLISH

    var showClearDataDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        HeaderBar(
            title = if (isBengali) "সেটিংস (Settings)" else "Settings & Preferences",
            subtitle = if (isBengali) "ভাষা, কণ্ঠ ও সিস্টেম কনফিগারেশন" else "Configure AI, voice, and system",
            onBack = { viewModel.navigateBack() }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Assistant Identity Card
            FuturisticCard(
                borderColor = CyanNeon.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isBengali) "🤖 অ্যাসিস্ট্যান্ট আইডেন্টিটি (Assistant Identity)" else "🤖 Assistant Identity",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = CyanNeon)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = settings.assistantName,
                    onValueChange = { viewModel.preferencesManager.updateAssistantName(it) },
                    label = { Text(if (isBengali) "অ্যাসিস্ট্যান্ট এর নাম" else "Assistant Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = CyanNeon.copy(alpha = 0.4f),
                        focusedLabelColor = CyanNeon
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = settings.preferredAddress,
                    onValueChange = { viewModel.preferencesManager.updatePreferredAddress(it) },
                    label = { Text(if (isBengali) "আপনাকে কী বলে সম্বোধন করবে" else "Preferred Address (Sir/Boss/etc.)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = CyanNeon.copy(alpha = 0.4f),
                        focusedLabelColor = CyanNeon
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "ওয়েক ওয়ার্ড (Wake Word)" else "Wake Word Activation",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = if (isBengali) "নাম ধরে ডাকলে অ্যাসিস্ট্যান্ট সাড়া দিবে" else "Assistant responds when called by name",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Switch(
                        checked = settings.isWakeWordEnabled,
                        onCheckedChange = { viewModel.preferencesManager.updateWakeWordEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyanNeon,
                            checkedTrackColor = CyanNeon.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // Language Selection Card
            FuturisticCard(
                borderColor = CyanNeon.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isBengali) "🌐 প্রাথমিক ভাষা (Primary Language)" else "🌐 Primary Language",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = CyanNeon)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.values().forEach { lang ->
                        val isSelected = settings.language == lang
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.preferencesManager.updateLanguage(lang) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) CyanNeon.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CyanNeon) else null
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = lang.nativeName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) CyanNeon else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = lang.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Theme Selection Card
            FuturisticCard(
                borderColor = PurpleNeon.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isBengali) "🎨 থিম ও দর্শন (Appearance)" else "🎨 Theme & Appearance",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PurpleNeon)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.values().forEach { mode ->
                        val isSelected = settings.themeMode == mode
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.preferencesManager.updateTheme(mode) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) PurpleNeon.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, PurpleNeon) else null
                        ) {
                            Text(
                                text = if (isBengali) mode.titleBn else mode.titleEn,
                                modifier = Modifier.padding(vertical = 10.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) PurpleNeon else MaterialTheme.colorScheme.onSurface,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }

            // AI Response Style Card
            FuturisticCard(
                borderColor = MagentaNeon.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isBengali) "⚡ এআই উত্তরের ধরন (AI Response Style)" else "⚡ AI Response Style",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MagentaNeon)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AiResponseStyle.values().forEach { style ->
                        val isSelected = settings.aiStyle == style
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.preferencesManager.updateAiStyle(style) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MagentaNeon.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MagentaNeon) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = if (isBengali) style.titleBn else style.titleEn,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MagentaNeon else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = style.promptModifier,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MagentaNeon,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Voice & Speech Settings Card
            FuturisticCard(
                borderColor = EmeraldNeon.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isBengali) "🎙️ ভয়েস ও উচ্চারণ (Voice & Speech)" else "🎙️ Voice & Speech",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldNeon)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "ভয়েস রিডআউট (TTS)" else "Voice Output (TTS)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = if (isBengali) "এআই উত্তরের স্বয়ংক্রিয় অডিও পাঠ" else "Automatically speaks out AI answers",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Switch(
                        checked = settings.isVoiceEnabled,
                        onCheckedChange = { viewModel.preferencesManager.updateVoiceEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldNeon,
                            checkedTrackColor = EmeraldNeon.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBengali) "কণ্ঠের গতি: ${settings.speechSpeed}x" else "Speech Speed: ${settings.speechSpeed}x",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { spd ->
                            val isSel = settings.speechSpeed == spd
                            Surface(
                                modifier = Modifier
                                    .clickable {
                                        viewModel.preferencesManager.updateSpeechSpeed(spd)
                                        viewModel.ttsManager.setSpeechRate(spd)
                                    },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) EmeraldNeon.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSel) androidx.compose.foundation.BorderStroke(1.dp, EmeraldNeon) else null
                            ) {
                                Text(
                                    text = "${spd}x",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSel) EmeraldNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // About & System Info Card
            FuturisticCard(
                borderColor = CyanNeon.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ℹ️ ${AppConfig.APP_NAME} System Info",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = CyanNeon)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "App Version: ${AppConfig.APP_VERSION}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = "Core Intelligence: ${AppConfig.DEFAULT_TEXT_MODEL}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = "Vision Model: ${AppConfig.DEFAULT_IMAGE_MODEL}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = "Architecture: Native Android (Kotlin & Compose)",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
