package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.AppConfig
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppScreen
import com.example.ui.components.FuturisticCard
import com.example.ui.components.GlowButton
import com.example.ui.components.HeaderBar
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.userSettings.collectAsState()
    val isBengali = settings.language != AppLanguage.ENGLISH

    val notes by viewModel.notes.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val conversations by viewModel.conversations.collectAsState()

    var showEditNameDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        HeaderBar(
            title = if (isBengali) "প্রোফাইল (Profile)" else "User Profile",
            subtitle = if (isBengali) "ব্যবহারকারী তথ্য ও পরিসংখ্যান" else "Guest Mode & Statistics",
            onBack = { viewModel.navigateBack() }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User Avatar Banner
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(CyanNeon, PurpleNeon)))
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(DarkSurfaceCard),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = CyanNeon,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = settings.userName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    IconButton(onClick = { showEditNameDialog = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit Name", tint = CyanNeon, modifier = Modifier.size(16.dp))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldNeon.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = if (isBengali) "🔒 অতিথি মোড (Guest Mode)" else "🔒 Guest Mode (On-Device)",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = EmeraldNeon,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Statistics Grid Card
            FuturisticCard(
                borderColor = CyanNeon.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isBengali) "📊 আপনার পরিসংখ্যান" else "📊 Your On-Device Stats",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatBox(label = if (isBengali) "নোট" else "Notes", count = notes.size.toString(), color = CyanNeon)
                    StatBox(label = if (isBengali) "টাস্ক" else "Tasks", count = tasks.size.toString(), color = EmeraldNeon)
                    StatBox(label = if (isBengali) "মেমরি" else "Memories", count = memories.size.toString(), color = Color(0xFF818CF8))
                    StatBox(label = if (isBengali) "চ্যাট" else "Chats", count = conversations.size.toString(), color = MagentaNeon)
                }
            }

            // Quick Shortcut Actions
            FuturisticCard(
                borderColor = PurpleNeon.copy(alpha = 0.25f),
                modifier = Modifier.fillMaxWidth()
            ) {
                ProfileOptionRow(
                    title = if (isBengali) "চ্যাট ইতিহাস" else "Chat History",
                    icon = Icons.Outlined.History,
                    onClick = { viewModel.navigateTo(AppScreen.CHAT_HISTORY) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))
                ProfileOptionRow(
                    title = if (isBengali) "এআই মেমরি নিয়ন্ত্রণ" else "Memory Center",
                    icon = Icons.Outlined.Psychology,
                    onClick = { viewModel.navigateTo(AppScreen.MEMORY) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))
                ProfileOptionRow(
                    title = if (isBengali) "সেটিংস" else "Settings",
                    icon = Icons.Outlined.Settings,
                    onClick = { viewModel.navigateTo(AppScreen.SETTINGS) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showEditNameDialog) {
        var tempName by remember { mutableStateOf(settings.userName) }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text(if (isBengali) "নাম পরিবর্তন" else "Edit Name") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text(if (isBengali) "আপনার নাম" else "Your Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotBlank()) {
                            viewModel.preferencesManager.updateUserName(tempName)
                            showEditNameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Color(0xFF04101A))
                ) {
                    Text(if (isBengali) "সংরক্ষণ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text(if (isBengali) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatBox(label: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun ProfileOptionRow(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
