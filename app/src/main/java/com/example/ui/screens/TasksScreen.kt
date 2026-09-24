package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TaskEntity
import com.example.domain.model.AppLanguage
import com.example.domain.model.TaskPriority
import com.example.ui.components.FuturisticCard
import com.example.ui.components.GlowButton
import com.example.ui.components.HeaderBar
import com.example.ui.theme.*
import com.example.utils.DateUtils
import com.example.viewmodel.MainViewModel

@Composable
fun TasksScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.userSettings.collectAsState()
    val isBengali = settings.language != AppLanguage.ENGLISH

    val tasks by viewModel.tasks.collectAsState()
    val parsedNlp by viewModel.taskNlpParsed.collectAsState()

    var nlpInput by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: All, 1: Pending, 2: Completed

    val filteredTasks = remember(tasks, selectedTab) {
        when (selectedTab) {
            1 -> tasks.filter { !it.isCompleted }
            2 -> tasks.filter { it.isCompleted }
            else -> tasks
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        HeaderBar(
            title = if (isBengali) "টাস্ক ও রিমাইন্ডার" else "Tasks & Reminders",
            subtitle = if (isBengali) "স্মার্ট এআই টাস্ক ও পরিকল্পনা" else "Natural language task scheduler",
            onBack = { viewModel.navigateBack() },
            trailingContent = {
                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .testTag("create_task_header_button")
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(EmeraldNeon)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Task",
                        tint = Color(0xFF04101A)
                    )
                }
            }
        )

        // Natural Language Task Input Box
        FuturisticCard(
            borderColor = EmeraldNeon.copy(alpha = 0.35f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = if (isBengali) "✨ স্মার্ট টাস্ক তৈরি (Natural Language)" else "✨ Smart NLP Task Creator",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldNeon
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = nlpInput,
                    onValueChange = { nlpInput = it },
                    placeholder = {
                        Text(
                            text = if (isBengali) "যেমন: 'Remind me tomorrow at 8 AM to study'" else "e.g., 'Remind me tomorrow at 8 AM to finish assignment'",
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nlp_task_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldNeon,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (nlpInput.isNotBlank()) {
                            viewModel.parseNaturalLanguageTask(nlpInput)
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldNeon)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Parse NLP",
                        tint = Color(0xFF04101A)
                    )
                }
            }

            // NLP Parsed Confirmation Banner
            if (parsedNlp != null) {
                val parsed = parsedNlp!!
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldNeon.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, EmeraldNeon.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Title: ${parsed.title}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "Time: ${parsed.timeString ?: "Today"} | Priority: ${parsed.priority.titleEn}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = EmeraldNeon,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.addTask(
                                    title = parsed.title,
                                    description = parsed.description,
                                    priority = parsed.priority.name,
                                    dueDateMillis = parsed.dueDateMillis,
                                    dueTimeString = parsed.timeString
                                )
                                nlpInput = ""
                                viewModel.clearTaskNlpParsed()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldNeon, contentColor = Color(0xFF04101A)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(if (isBengali) "যুক্ত করুন" else "Add Task", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs: All, Pending, Completed
        val tabTitles = if (isBengali) listOf("সব (${tasks.size})", "চলমান (${tasks.count { !it.isCompleted }})", "সম্পন্ন (${tasks.count { it.isCompleted }})")
                        else listOf("All (${tasks.size})", "Pending (${tasks.count { !it.isCompleted }})", "Completed (${tasks.count { it.isCompleted }})")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabTitles.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedTab = index }
                        .border(
                            1.dp,
                            if (isSelected) EmeraldNeon else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        ),
                    color = if (isSelected) EmeraldNeon.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isSelected) EmeraldNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tasks List
        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldNeon.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isBengali) "কোনো টাস্ক নেই" else "No tasks found",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskItemCard(
                        task = task,
                        isBengali = isBengali,
                        onToggle = { viewModel.toggleTaskCompletion(task.id, !task.isCompleted) },
                        onDelete = { viewModel.deleteTask(task.id) }
                    )
                }
            }
        }
    }

    // Manual Create Task Dialog
    if (showCreateDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var taskDesc by remember { mutableStateOf("") }
        var taskPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
        var taskTimeStr by remember { mutableStateOf("08:00 AM") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = if (isBengali) "নতুন টাস্ক যুক্ত করুন" else "Create New Task",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text(if (isBengali) "টাস্কের নাম (Title)" else "Task Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = taskTimeStr,
                        onValueChange = { taskTimeStr = it },
                        label = { Text(if (isBengali) "সময় (Time e.g., 08:00 AM)" else "Time") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(
                        text = if (isBengali) "অগ্রাধিকার (Priority)" else "Priority",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TaskPriority.values().forEach { prio ->
                            val isSel = prio == taskPriority
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { taskPriority = prio }
                                    .border(
                                        1.dp,
                                        if (isSel) Color(prio.colorHex) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    ),
                                color = if (isSel) Color(prio.colorHex).copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (isBengali) prio.titleBn else prio.titleEn,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSel) Color(prio.colorHex) else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            viewModel.addTask(
                                title = taskTitle,
                                description = taskDesc,
                                priority = taskPriority.name,
                                dueDateMillis = System.currentTimeMillis(),
                                dueTimeString = taskTimeStr
                            )
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldNeon, contentColor = Color(0xFF04101A))
                ) {
                    Text(if (isBengali) "যুক্ত করুন" else "Add Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(if (isBengali) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun TaskItemCard(
    task: TaskEntity,
    isBengali: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val prio = try { TaskPriority.valueOf(task.priority) } catch (e: Exception) { TaskPriority.MEDIUM }
    val prioColor = Color(prio.colorHex)

    FuturisticCard(
        borderColor = if (task.isCompleted) Color.Gray.copy(alpha = 0.2f) else prioColor.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = EmeraldNeon,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = prioColor.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = if (isBengali) prio.titleBn else prio.titleEn,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = prioColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    if (task.dueTimeString != null) {
                        Text(
                            text = "⏰ ${task.dueTimeString}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
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
