package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MemoryEntity
import com.example.domain.model.AppLanguage
import com.example.ui.components.FuturisticCard
import com.example.ui.components.GlowButton
import com.example.ui.components.HeaderBar
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun MemoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.userSettings.collectAsState()
    val isBengali = settings.language != AppLanguage.ENGLISH
    val memories by viewModel.memories.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        HeaderBar(
            title = if (isBengali) "এআই মেমরি সিস্টেম" else "AI Memory Center",
            subtitle = if (isBengali) "১০০% ব্যক্তিগত ও নিরাপদ ডিভাইস মেমরি" else "100% On-Device & Privacy-Focused",
            onBack = { viewModel.navigateBack() },
            trailingContent = {
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .testTag("add_memory_icon_button")
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF818CF8))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Fact",
                        tint = Color(0xFF04101A)
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Privacy Master Switch Card
            item {
                FuturisticCard(
                    borderColor = Color(0xFF818CF8).copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBengali) "মেমরি চালু রাখুন (Memory Active)" else "Enable AI Memory",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = if (isBengali) "আপনার পছন্দ ও প্রয়োজনীয় তথ্য স্থানীয়ভাবে সংরক্ষিত হবে" else "Saves preferences on-device for tailored conversations",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Switch(
                            checked = settings.isMemoryEnabled,
                            onCheckedChange = { viewModel.preferencesManager.updateMemoryEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF818CF8),
                                checkedTrackColor = Color(0xFF818CF8).copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }

            // Info Explainer
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Security,
                            contentDescription = null,
                            tint = EmeraldNeon,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isBengali) "আপনার সকল মেমরি আপনার নিজস্ব ফোনেই সীমাবদ্ধ থাকে এবং ক্লাউডে প্রেরণ করা হয় না।"
                                   else "All memory is stored strictly on your device and is never shared or synced to external clouds.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Memory Items Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBengali) "সংরক্ষিত মেমরিসমূহ (${memories.size})" else "Saved Memories (${memories.size})",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    if (memories.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearAllMemories() }) {
                            Text(
                                text = if (isBengali) "সব মুছুন" else "Clear All",
                                color = RoseError,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Memory Items List
            if (memories.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Psychology,
                                contentDescription = null,
                                tint = Color(0xFF818CF8).copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isBengali) "কোনো সংরক্ষিত মেমরি নেই" else "No memories stored yet",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            } else {
                items(memories, key = { it.id }) { mem ->
                    MemoryItemCard(
                        memory = mem,
                        onDelete = { viewModel.deleteMemory(mem.id) }
                    )
                }
            }
        }
    }

    // Add Memory Dialog
    if (showAddDialog) {
        var factText by remember { mutableStateOf("") }
        var categoryText by remember { mutableStateOf("Preference") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = if (isBengali) "নতুন মেমরি যুক্ত করুন" else "Add New Memory Fact",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = categoryText,
                        onValueChange = { categoryText = it },
                        label = { Text(if (isBengali) "ক্যাটাগরি (e.g., Preference, Language)" else "Category") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = factText,
                        onValueChange = { factText = it },
                        label = { Text(if (isBengali) "স্মরণীয় তথ্য (e.g., 'Prefer answers in Bengali')" else "Fact to remember") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (factText.isNotBlank()) {
                            viewModel.addMemory(categoryText.ifBlank { "General" }, factText)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF818CF8), contentColor = Color(0xFF04101A))
                ) {
                    Text(if (isBengali) "যুক্ত করুন" else "Save Memory")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(if (isBengali) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun MemoryItemCard(
    memory: MemoryEntity,
    onDelete: () -> Unit
) {
    FuturisticCard(
        borderColor = Color(0xFF818CF8).copy(alpha = 0.25f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF818CF8).copy(alpha = 0.16f)
                ) {
                    Text(
                        text = memory.category,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF818CF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = memory.fact,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
