package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.AppLanguage
import com.example.ui.components.FuturisticCard
import com.example.ui.components.GlowButton
import com.example.ui.components.HeaderBar
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun ImageAnalysisScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by viewModel.userSettings.collectAsState()
    val isBengali = settings.language != AppLanguage.ENGLISH

    val selectedBitmap by viewModel.selectedImageBitmap.collectAsState()
    val selectedUri by viewModel.selectedImageUri.collectAsState()

    var imagePrompt by remember { mutableStateOf("") }
    var analysisResult by remember { mutableStateOf<String?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadBitmapFromUri(it) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        HeaderBar(
            title = if (isBengali) "ছবি বিশ্লেষণ (Vision AI)" else "Image Analysis AI",
            subtitle = if (isBengali) "ছবির বিষয়ে যা খুশি প্রশ্ন করুন" else "Multimodal visual reasoning",
            onBack = { viewModel.navigateBack() }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image Upload & Preview Card
            FuturisticCard(
                borderColor = Color(0xFFF472B6).copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (selectedUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        AsyncImage(
                            model = selectedUri,
                            contentDescription = "Selected Image Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        IconButton(
                            onClick = { viewModel.setSelectedImage(null, null) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF472B6).copy(alpha = 0.08f))
                            .border(1.dp, Color(0xFFF472B6).copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = Color(0xFFF472B6),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isBengali) "গ্যালারি থেকে ছবি নির্বাচন করুন" else "Select Image from Gallery",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = if (isBengali) "ট্যাপ করে ছবি আপলোড করুন" else "Tap to upload image",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            // Prompt Question Field
            Text(
                text = if (isBengali) "ছবি সম্পর্কিত প্রশ্ন লিখুন" else "Ask a Question About the Image",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF472B6)
                )
            )

            OutlinedTextField(
                value = imagePrompt,
                onValueChange = { imagePrompt = it },
                placeholder = {
                    Text(
                        text = if (isBengali) "যেমন: 'ছবিটির বিস্তারিত বর্ণনা দাও', 'এখানে কী লেখা আছে?'" else "e.g., 'Describe what is in this photo', 'Explain the diagram'",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("image_prompt_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFF472B6),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Quick Preset Prompts
            val presets = if (isBengali) listOf("ছবিটি বিস্তারিত বুঝাও", "ছবির টেক্সট বের করো", "গুরুত্বপূর্ণ অংশ চিহ্নিত করো")
                          else listOf("Describe this in detail", "Extract all text", "Explain key objects")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEach { preset ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { imagePrompt = preset }
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = preset,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Analyze Action Button
            GlowButton(
                text = if (isAnalyzing) (if (isBengali) "বিশ্লেষণ হচ্ছে..." else "Analyzing Image...")
                       else (if (isBengali) "ছবি বিশ্লেষণ করুন (Analyze)" else "Analyze Image"),
                onClick = {
                    if (selectedBitmap != null) {
                        isAnalyzing = true
                        scope.launch {
                            val promptToSend = imagePrompt.ifBlank { "Describe this image in detail" }
                            val res = viewModel.aiRepository.generateResponse(
                                prompt = promptToSend,
                                imageBitmap = selectedBitmap,
                                language = settings.language,
                                aiStyle = settings.aiStyle
                            )
                            isAnalyzing = false
                            analysisResult = res.getOrNull() ?: "দুঃখিত, ছবিটি বিশ্লেষণ করা সম্ভব হয়নি।"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedBitmap != null && !isAnalyzing,
                icon = Icons.Default.ImageSearch,
                glowColor = Color(0xFFF472B6),
                testTag = "action_analyze_image_button"
            )

            // Analysis Result Card
            if (analysisResult != null) {
                FuturisticCard(
                    borderColor = Color(0xFFF472B6).copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBengali) "বিশ্লেষণের ফলাফল" else "Analysis Result",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF472B6)
                            )
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Image Analysis", analysisResult))
                                    Toast.makeText(context, if (isBengali) "কপি হয়েছে" else "Copied", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = CyanNeon, modifier = Modifier.size(18.dp))
                            }

                            IconButton(
                                onClick = {
                                    viewModel.saveNote(
                                        title = "Image Analysis: $imagePrompt",
                                        content = analysisResult ?: "",
                                        category = "Vision"
                                    )
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Outlined.BookmarkAdd, contentDescription = "Save to Notes", tint = EmeraldNeon, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = analysisResult ?: "",
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
