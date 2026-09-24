package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppScreen
import com.example.domain.model.ThemeMode
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.userSettings.collectAsState()

            val isDark = when (settings.themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            NovaTheme(darkTheme = isDark) {
                NovaAppRoot(viewModel = viewModel)
            }
        }
    }
}

data class BottomNavItem(
    val screen: AppScreen,
    val titleEn: String,
    val titleBn: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun NovaAppRoot(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()
    val isBengali = settings.language != AppLanguage.ENGLISH

    // Toast listener
    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    // System Back Handler
    BackHandler(enabled = currentScreen != AppScreen.HOME && currentScreen != AppScreen.ONBOARDING) {
        viewModel.navigateBack()
    }

    val navItems = remember {
        listOf(
            BottomNavItem(AppScreen.HOME, "Home", "হোম", Icons.Filled.Home, Icons.Outlined.Home),
            BottomNavItem(AppScreen.CHAT, "Chat", "চ্যাট", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline),
            BottomNavItem(AppScreen.VOICE, "Voice", "ভয়েস", Icons.Filled.Mic, Icons.Outlined.Mic),
            BottomNavItem(AppScreen.TOOLS, "Tools", "টুলস", Icons.Filled.GridView, Icons.Outlined.GridView),
            BottomNavItem(AppScreen.PROFILE, "Profile", "প্রোফাইল", Icons.Filled.Person, Icons.Outlined.Person)
        )
    }

    val showBottomBar = currentScreen in listOf(
        AppScreen.HOME,
        AppScreen.CHAT,
        AppScreen.VOICE,
        AppScreen.TOOLS,
        AppScreen.PROFILE
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier
                        .testTag("main_bottom_nav")
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                    tonalElevation = 8.dp
                ) {
                    navItems.forEach { item ->
                        val isSelected = currentScreen == item.screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (item.screen == AppScreen.VOICE) {
                                    viewModel.navigateTo(AppScreen.VOICE)
                                } else {
                                    viewModel.navigateTo(item.screen)
                                }
                            },
                            icon = {
                                if (item.screen == AppScreen.VOICE) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) CyanNeon else CyanNeon.copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = item.selectedIcon,
                                            contentDescription = item.titleEn,
                                            tint = if (isSelected) Color(0xFF04101A) else CyanNeon,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.titleEn,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = if (isBengali) item.titleBn else item.titleEn,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanNeon,
                                selectedTextColor = CyanNeon,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = CyanNeon.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                    AppScreen.CHAT -> ChatScreen(viewModel = viewModel)
                    AppScreen.VOICE -> VoiceScreen(viewModel = viewModel)
                    AppScreen.TOOLS -> ToolsScreen(viewModel = viewModel)
                    AppScreen.PROFILE -> ProfileScreen(viewModel = viewModel)
                    AppScreen.STUDY -> StudyScreen(viewModel = viewModel)
                    AppScreen.CREATOR -> CreatorScreen(viewModel = viewModel)
                    AppScreen.TRANSLATOR -> TranslatorScreen(viewModel = viewModel)
                    AppScreen.NOTES -> NotesScreen(viewModel = viewModel)
                    AppScreen.TASKS -> TasksScreen(viewModel = viewModel)
                    AppScreen.IMAGE_ANALYSIS -> ImageAnalysisScreen(viewModel = viewModel)
                    AppScreen.MEMORY -> MemoryScreen(viewModel = viewModel)
                    AppScreen.CHAT_HISTORY -> ChatHistoryScreen(viewModel = viewModel)
                    AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    AppScreen.ONBOARDING -> OnboardingScreen(viewModel = viewModel)
                }
            }
        }
    }
}

// Kept for preview and unit test compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
