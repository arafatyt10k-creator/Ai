package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.AppConfig
import com.example.domain.model.AppLanguage
import com.example.domain.model.OrbState
import com.example.ui.components.AiOrb
import com.example.ui.components.FuturisticCard
import com.example.ui.components.GlowButton
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun OnboardingScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableStateOf(0) }
    var selectedLanguage by remember { mutableStateOf(AppLanguage.BENGALI) }
    val isBengali = selectedLanguage != AppLanguage.ENGLISH

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Step Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0..2) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(6.dp)
                        .width(if (currentPage == i) 28.dp else 10.dp)
                        .clip(CircleShape)
                        .background(if (currentPage == i) CyanNeon else Color.Gray.copy(alpha = 0.3f))
                )
            }
        }

        // Center Content based on page
        when (currentPage) {
            0 -> {
                // Page 0: Welcome & Orb
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AiOrb(state = OrbState.IDLE, size = 180.dp)

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = AppConfig.APP_NAME,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = CyanNeon,
                            letterSpacing = 2.sp
                        )
                    )

                    Text(
                        text = "Next-Generation Personal AI",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = PurpleNeon,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "স্বাগতম! আপনার দৈনন্দিন পড়াশোনা, কনটেন্ট তৈরি, অনুবাদ ও কাজের স্মার্ট এআই সঙ্গী।",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            1 -> {
                // Page 1: Select Language
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "ভাষা নির্বাচন করুন\nChoose Your Language",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Bengali Card Option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedLanguage = AppLanguage.BENGALI }
                            .border(
                                1.5.dp,
                                if (selectedLanguage == AppLanguage.BENGALI) CyanNeon else Color.Transparent,
                                RoundedCornerShape(18.dp)
                            ),
                        shape = RoundedCornerShape(18.dp),
                        color = if (selectedLanguage == AppLanguage.BENGALI) CyanNeon.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🇧🇩", fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "বাংলা (Bengali)",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedLanguage == AppLanguage.BENGALI) CyanNeon else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "প্রাঞ্জল ও স্বাভাবিক বাংলায় উত্তর পান",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // English Card Option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedLanguage = AppLanguage.ENGLISH }
                            .border(
                                1.5.dp,
                                if (selectedLanguage == AppLanguage.ENGLISH) CyanNeon else Color.Transparent,
                                RoundedCornerShape(18.dp)
                            ),
                        shape = RoundedCornerShape(18.dp),
                        color = if (selectedLanguage == AppLanguage.ENGLISH) CyanNeon.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🌐", fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "English",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedLanguage == AppLanguage.ENGLISH) CyanNeon else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Get answers and insights in clear English",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
            2 -> {
                // Page 2: Features Showcase
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isBengali) "শক্তিশালী এআই ফিচারসমূহ" else "Powerful AI Superpowers",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    FuturisticCard(borderColor = CyanNeon.copy(alpha = 0.3f)) {
                        FeatureRow("🎙️", if (isBengali) "দ্বিভাষিক ভয়েস সহকারী" else "Bilingual Voice Assistant", if (isBengali) "কথা বলে দ্রুত উত্তর পান" else "Talk naturally in Bengali & English")
                        Spacer(modifier = Modifier.height(12.dp))
                        FeatureRow("📚", if (isBengali) "স্টাডি ও কুইজ মোড" else "Study & MCQ Quiz Mode", if (isBengali) "যেকোনো জটিল বিষয় সহজে শিখুন" else "Step-by-step learning tutor")
                        Spacer(modifier = Modifier.height(12.dp))
                        FeatureRow("✨", if (isBengali) "কনটেন্ট ও চিত্রনাট্য" else "Content & Viral Scripts", if (isBengali) "ইউটিউব ও সোশ্যাল মিডিয়া পোস্ট" else "Craft YouTube titles & posts")
                        Spacer(modifier = Modifier.height(12.dp))
                        FeatureRow("🔒", if (isBengali) "অফলাইন নোট ও মেমরি" else "Offline Notes & Safe Memory", if (isBengali) "১০০% ডিভাইসে সংরক্ষিত গোপনীয়তা" else "100% On-device privacy protection")
                    }
                }
            }
        }

        // Bottom Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentPage > 0) {
                TextButton(
                    onClick = { currentPage-- },
                    modifier = Modifier.testTag("onboarding_back_button")
                ) {
                    Text(
                        text = if (isBengali) "পেছনে" else "Back",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            GlowButton(
                text = if (currentPage == 2) (if (isBengali) "শুরু করুন (Get Started)" else "Get Started")
                       else (if (isBengali) "পরবর্তী (Next)" else "Next"),
                onClick = {
                    if (currentPage < 2) {
                        currentPage++
                    } else {
                        viewModel.completeOnboarding(selectedLanguage)
                    }
                },
                glowColor = CyanNeon,
                testTag = "onboarding_next_button"
            )
        }
    }
}

@Composable
private fun FeatureRow(emoji: String, title: String, description: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )
        }
    }
}
