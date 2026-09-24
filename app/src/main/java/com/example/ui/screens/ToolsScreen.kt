package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppScreen
import com.example.ui.components.HeaderBar
import com.example.ui.components.QuickActionTile
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun ToolsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.userSettings.collectAsState()
    val isBengali = settings.language != AppLanguage.ENGLISH

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        HeaderBar(
            title = if (isBengali) "এআই টুলস হাব" else "AI Tools Hub",
            subtitle = if (isBengali) "পড়াশোনা, কনটেন্ট ও ব্যক্তিগত সহকারী" else "Productivity, study, and smart utilities",
            onBack = { viewModel.navigateBack() }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Learning & Writing
            Text(
                text = if (isBengali) "📚 শিক্ষা ও কনটেন্ট" else "📚 Learning & Creation",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionTile(
                    title = if (isBengali) "স্টাডি অ্যাসিস্ট্যান্ট" else "Study Assistant",
                    subtitle = if (isBengali) "গণিত, বিজ্ঞান ও কুইজ" else "Math, Science & MCQ",
                    icon = Icons.Default.School,
                    accentColor = PurpleNeon,
                    onClick = { viewModel.navigateTo(AppScreen.STUDY) },
                    modifier = Modifier.weight(1f)
                )

                QuickActionTile(
                    title = if (isBengali) "কনটেন্ট ক্রিয়েটর" else "Content Creator",
                    subtitle = if (isBengali) "ইউটিউব ও সোশ্যাল মিডিয়া" else "YouTube & Captions",
                    icon = Icons.Default.AutoAwesome,
                    accentColor = MagentaNeon,
                    onClick = { viewModel.navigateTo(AppScreen.CREATOR) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Section 2: Utilities & Vision
            Text(
                text = if (isBengali) "🌐 স্মার্ট ইউটিলিটিজ" else "🌐 Smart Utilities",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionTile(
                    title = if (isBengali) "অনুবাদক" else "Translator",
                    subtitle = if (isBengali) "বাংলা <-> English" else "Instant Translation",
                    icon = Icons.Default.Translate,
                    accentColor = AmberGlow,
                    onClick = { viewModel.navigateTo(AppScreen.TRANSLATOR) },
                    modifier = Modifier.weight(1f)
                )

                QuickActionTile(
                    title = if (isBengali) "ছবি বিশ্লেষণ" else "Image Analysis",
                    subtitle = if (isBengali) "ছবি থেকে তথ্য জানুন" else "Visual Q&A",
                    icon = Icons.Default.ImageSearch,
                    accentColor = Color(0xFFF472B6),
                    onClick = { viewModel.navigateTo(AppScreen.IMAGE_ANALYSIS) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Section 3: Personal Productivity
            Text(
                text = if (isBengali) "📝 ব্যক্তিগত প্রোডাক্টিভিটি" else "📝 Productivity & Storage",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionTile(
                    title = if (isBengali) "নোটবুক" else "Smart Notes",
                    subtitle = if (isBengali) "অফলাইন নোট সংরক্ষণ" else "Offline notes store",
                    icon = Icons.Default.EditNote,
                    accentColor = CyanNeon,
                    onClick = { viewModel.navigateTo(AppScreen.NOTES) },
                    modifier = Modifier.weight(1f)
                )

                QuickActionTile(
                    title = if (isBengali) "টাস্ক ও রিমাইন্ডার" else "Tasks & NLP",
                    subtitle = if (isBengali) "কাজের তালিকা ও সময়" else "Task manager",
                    icon = Icons.Default.CheckCircleOutline,
                    accentColor = EmeraldNeon,
                    onClick = { viewModel.navigateTo(AppScreen.TASKS) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Section 4: Privacy & History
            Text(
                text = if (isBengali) "🔒 প্রাইভেসি ও মেমরি" else "🔒 Privacy & Memory",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionTile(
                    title = if (isBengali) "এআই মেমরি" else "AI Memory",
                    subtitle = if (isBengali) "ব্যক্তিগত তথ্য নিয়ন্ত্রণ" else "Manage saved facts",
                    icon = Icons.Default.Psychology,
                    accentColor = Color(0xFF818CF8),
                    onClick = { viewModel.navigateTo(AppScreen.MEMORY) },
                    modifier = Modifier.weight(1f)
                )

                QuickActionTile(
                    title = if (isBengali) "চ্যাট ইতিহাস" else "Chat History",
                    subtitle = if (isBengali) "পূর্ববর্তী কথোপকথন" else "Past conversations",
                    icon = Icons.Default.History,
                    accentColor = Color(0xFF38BDF8),
                    onClick = { viewModel.navigateTo(AppScreen.CHAT_HISTORY) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
