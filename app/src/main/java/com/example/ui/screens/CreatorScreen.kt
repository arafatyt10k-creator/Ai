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
import com.example.domain.model.CreatorToolType
import com.example.ui.components.FuturisticCard
import com.example.ui.components.GlowButton
import com.example.ui.components.HeaderBar
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun CreatorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.userSettings.collectAsState()
    val isBengali = settings.language != AppLanguage.ENGLISH

    val selectedTool by viewModel.selectedCreatorTool.collectAsState()
    val creatorInput by viewModel.creatorInput.collectAsState()
    val creatorResult by viewModel.creatorResult.collectAsState()
    val isLoading by viewModel.isCreatorLoading.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        HeaderBar(
            title = if (isBengali) "কনটেন্ট ক্রিয়েটর" else "Content Creator AI",
            subtitle = if (isBengali) "ইউটিউব, রিলস ও সোশ্যাল পোস্ট সহকারী" else "Viral scripts, titles, and captions",
            onBack = { viewModel.navigateBack() }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tool Picker
            Text(
                text = if (isBengali) "টুল নির্বাচন করুন" else "Select Creator Tool",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MagentaNeon
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CreatorToolType.values().forEach { tool ->
                    val isSelected = tool == selectedTool
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setCreatorTool(tool) }
                            .border(
                                1.dp,
                                if (isSelected) MagentaNeon else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            ),
                        color = if (isSelected) MagentaNeon.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isBengali) tool.titleBn else tool.titleEn,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MagentaNeon else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            // Input description
            Text(
                text = if (isBengali) "বিষয় বা কী-ওয়ার্ড লিখুন" else "Topic or Keywords",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MagentaNeon
                )
            )

            OutlinedTextField(
                value = creatorInput,
                onValueChange = { viewModel.setCreatorInput(it) },
                placeholder = {
                    Text(
                        text = if (isBengali) selectedTool.hintBn else "Enter your topic, channel theme, or context...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
                    .testTag("creator_topic_input"),
                shape = RoundedCornerShape(14.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MagentaNeon,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Generate Button
            GlowButton(
                text = if (isLoading) (if (isBengali) "তৈরি হচ্ছে..." else "Creating...")
                       else (if (isBengali) "কনটেন্ট তৈরি করুন (Generate)" else "Generate Content"),
                onClick = { viewModel.generateCreatorContent() },
                modifier = Modifier.fillMaxWidth(),
                enabled = creatorInput.isNotBlank() && !isLoading,
                icon = Icons.Default.AutoAwesome,
                glowColor = MagentaNeon,
                testTag = "generate_creator_button"
            )

            // Result Display Card
            if (creatorResult != null) {
                FuturisticCard(
                    borderColor = MagentaNeon.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBengali) "জেনারেট করা ফলাফল" else "Generated Output",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MagentaNeon
                            )
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Creator Output", creatorResult))
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
                                        title = "${selectedTool.titleBn}: $creatorInput",
                                        content = creatorResult ?: "",
                                        category = "Creator"
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
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = creatorResult ?: "",
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
