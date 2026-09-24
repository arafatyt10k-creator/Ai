package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.config.AppConfig
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppScreen
import com.example.domain.model.OrbState
import com.example.ui.components.AiOrb
import com.example.ui.components.FuturisticCard
import com.example.ui.components.OrbStatusPill
import com.example.ui.components.QuickActionTile
import com.example.ui.theme.*
import com.example.utils.DateUtils
import com.example.viewmodel.MainViewModel

data class HomeQuickAction(
    val titleEn: String,
    val titleBn: String,
    val subtitleEn: String,
    val subtitleBn: String,
    val icon: ImageVector,
    val color: Color,
    val targetScreen: AppScreen
)

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.userSettings.collectAsState()
    val orbState by viewModel.orbState.collectAsState()
    val isBengali = settings.language != AppLanguage.ENGLISH

    var quickInputText by remember { mutableStateOf("") }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.navigateTo(AppScreen.VOICE)
            viewModel.startVoiceListening()
        }
    }

    val quickActions = remember {
        listOf(
            HomeQuickAction("Ask AI", "এআই চ্যাট", "Instant Intelligence", "স্মার্ট কথোপকথন", Icons.Default.ChatBubble, CyanNeon, AppScreen.CHAT),
            HomeQuickAction("Voice Chat", "ভয়েস মোড", "Talk with AI", "কথা বলে জানুন", Icons.Default.Mic, EmeraldNeon, AppScreen.VOICE),
            HomeQuickAction("Study Help", "পড়াশোনা", "Smart Learning", "সহজ শিক্ষা সহকারী", Icons.Default.School, PurpleNeon, AppScreen.STUDY),
            HomeQuickAction("Create Content", "কনটেন্ট তৈরি", "Viral scripts & titles", "ইউটিউব ও ক্যাপশন", Icons.Default.AutoAwesome, MagentaNeon, AppScreen.CREATOR),
            HomeQuickAction("Translate", "অনুবাদ", "Bengali <-> English", "বাংলা ও ইংরেজি", Icons.Default.Translate, AmberGlow, AppScreen.TRANSLATOR),
            HomeQuickAction("Take Note", "নোট সংরক্ষণ", "Quick ideas & thoughts", "গুরুত্বপূর্ণ বিষয়", Icons.Default.EditNote, CyanGlow, AppScreen.NOTES),
            HomeQuickAction("Tasks", "টাস্ক ও রিমাইন্ডার", "Natural language tasks", "কাজের পরিকল্পনা", Icons.Default.CheckCircleOutline, Color(0xFF60A5FA), AppScreen.TASKS),
            HomeQuickAction("Analyze Image", "ছবি বিশ্লেষণ", "Ask anything about photos", "ছবির তথ্য অনুসন্ধান", Icons.Default.ImageSearch, Color(0xFFF472B6), AppScreen.IMAGE_ANALYSIS)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = DateUtils.getTimeBasedGreeting(settings.userName, isBengali),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = AppConfig.APP_NAME,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = CyanNeon,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PurpleNeon.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isBengali) "বাংলা" else "PRO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PurpleNeon,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.CHAT_HISTORY) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Chat History",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Scrollable Body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // AI Orb Centerpiece
            AiOrb(
                state = orbState,
                size = 150.dp,
                onClick = {
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPerm) {
                        viewModel.navigateTo(AppScreen.VOICE)
                        viewModel.startVoiceListening()
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Orb Status Indicator Pill
            OrbStatusPill(state = orbState, isBengali = isBengali)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isBengali) "আমি আপনাকে কীভাবে সহায়তা করতে পারি?" else "How can I assist you today?",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBengali) "কুইক সার্ভিসেস" else "Quick Actions",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = if (isBengali) "সব দেখুন" else "View All",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = CyanNeon,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.TOOLS) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Actions Grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (i in quickActions.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val item1 = quickActions[i]
                        QuickActionTile(
                            title = if (isBengali) item1.titleBn else item1.titleEn,
                            subtitle = if (isBengali) item1.subtitleBn else item1.subtitleEn,
                            icon = item1.icon,
                            accentColor = item1.color,
                            onClick = { viewModel.navigateTo(item1.targetScreen) },
                            modifier = Modifier.weight(1f)
                        )

                        if (i + 1 < quickActions.size) {
                            val item2 = quickActions[i + 1]
                            QuickActionTile(
                                title = if (isBengali) item2.titleBn else item2.titleEn,
                                subtitle = if (isBengali) item2.subtitleBn else item2.subtitleEn,
                                icon = item2.icon,
                                accentColor = item2.color,
                                onClick = { viewModel.navigateTo(item2.targetScreen) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom Fast Prompt Input Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, CyanNeon.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val hasPerm = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPerm) {
                            viewModel.navigateTo(AppScreen.VOICE)
                            viewModel.startVoiceListening()
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier
                        .testTag("home_mic_button")
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyanNeon.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = CyanNeon
                    )
                }

                TextField(
                    value = quickInputText,
                    onValueChange = { quickInputText = it },
                    placeholder = {
                        Text(
                            text = if (isBengali) "কিছু লিখুন বা প্রশ্ন করুন..." else "Ask NOVA AI anything...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_text_input"),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (quickInputText.isNotBlank()) {
                            viewModel.setChatInput(quickInputText)
                            viewModel.sendMessage(quickInputText)
                            quickInputText = ""
                            viewModel.navigateTo(AppScreen.CHAT)
                        }
                    },
                    enabled = quickInputText.isNotBlank(),
                    modifier = Modifier
                        .testTag("home_send_button")
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (quickInputText.isNotBlank()) CyanNeon else CyanNeon.copy(alpha = 0.2f)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Send",
                        tint = if (quickInputText.isNotBlank()) Color(0xFF04101A) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
