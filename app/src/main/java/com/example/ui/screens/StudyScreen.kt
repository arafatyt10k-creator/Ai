package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppLanguage
import com.example.domain.model.StudyMode
import com.example.domain.model.StudySubject
import com.example.ui.components.FuturisticCard
import com.example.ui.components.GlowButton
import com.example.ui.components.HeaderBar
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun StudyScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.userSettings.collectAsState()
    val isBengali = settings.language != AppLanguage.ENGLISH

    val selectedSubject by viewModel.selectedStudySubject.collectAsState()
    val selectedMode by viewModel.selectedStudyMode.collectAsState()
    val studyTopic by viewModel.studyTopic.collectAsState()
    val studyResult by viewModel.studyResult.collectAsState()
    val isLoading by viewModel.isStudyLoading.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        HeaderBar(
            title = if (isBengali) "স্টাডি অ্যাসিস্ট্যান্ট" else "Study Assistant",
            subtitle = if (isBengali) "যেকোনো বিষয় সহজে শিখুন ও বুঝুন" else "AI-powered personalized tutor",
            onBack = { viewModel.navigateBack() }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Subject Picker
            Text(
                text = if (isBengali) "১. বিষয় নির্বাচন করুন" else "1. Select Subject",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StudySubject.values().forEach { subject ->
                    val isSelected = subject == selectedSubject
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setStudySubject(subject) }
                            .border(
                                1.dp,
                                if (isSelected) PurpleNeon else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            ),
                        color = if (isSelected) PurpleNeon.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isBengali) subject.titleBn else subject.titleEn,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PurpleNeon else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            // Study Mode Picker
            Text(
                text = if (isBengali) "২. শেখার ধরন (Mode)" else "2. Select Learning Mode",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StudyMode.values().forEach { mode ->
                    val isSelected = mode == selectedMode
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setStudyMode(mode) }
                            .border(
                                1.dp,
                                if (isSelected) CyanNeon else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            ),
                        color = if (isSelected) CyanNeon.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isBengali) mode.titleBn else mode.titleEn,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CyanNeon else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            // Topic Input
            Text(
                text = if (isBengali) "৩. টপিক বা অধ্যায়ের নাম লিখুন" else "3. Enter Topic or Concept",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon
                )
            )

            OutlinedTextField(
                value = studyTopic,
                onValueChange = { viewModel.setStudyTopic(it) },
                placeholder = {
                    Text(
                        text = if (isBengali) "যেমন: নিউটনের ৩য় সূত্র, পাইথাগোরাস, Tense..." else "e.g., Photosynthesis, Quadratic Equations...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("study_topic_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanNeon,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Suggestion Chips
            val suggestions = if (isBengali) {
                listOf("পাইথাগোরাস উপপাদ্য", "নিউটনের ৩য় সূত্র", "Photosynthesis", "English Tense Rules", "ICT মেমরি সিস্টেম")
            } else {
                listOf("Newton's Laws", "Pythagoras Theorem", "Photosynthesis", "English Tenses", "Binary Search")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                suggestions.forEach { sug ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.setStudyTopic(sug) }
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "+ $sug",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Generate Button
            GlowButton(
                text = if (isLoading) (if (isBengali) "তৈরি হচ্ছে..." else "Generating...")
                       else (if (isBengali) "পাঠ তৈরি করুন (Generate Lesson)" else "Generate Study Content"),
                onClick = { viewModel.generateStudyHelp() },
                modifier = Modifier.fillMaxWidth(),
                enabled = studyTopic.isNotBlank() && !isLoading,
                icon = Icons.Default.AutoAwesome,
                glowColor = PurpleNeon,
                testTag = "generate_study_button"
            )

            // Result Display Card
            if (studyResult != null) {
                FuturisticCard(
                    borderColor = PurpleNeon.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBengali) "ফলাফল ও বিশ্লেষণ" else "Study Lesson & Solution",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PurpleNeon
                            )
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Study Notes", studyResult))
                                    Toast.makeText(context, if (isBengali) "কপি করা হয়েছে" else "Copied", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = CyanNeon,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewModel.saveNote(
                                        title = "${selectedSubject.titleBn}: $studyTopic",
                                        content = studyResult ?: "",
                                        category = "Study"
                                    )
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.BookmarkAdd,
                                    contentDescription = "Save to Notes",
                                    tint = EmeraldNeon,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewModel.ttsManager.speak(
                                        studyResult ?: "",
                                        isBengali = isBengali,
                                        speed = settings.speechSpeed
                                    )
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.VolumeUp,
                                    contentDescription = "Listen",
                                    tint = MagentaNeon,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = studyResult ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
