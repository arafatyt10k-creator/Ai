package com.example.ui.floating

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.OrbState
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FloatingOrbUI(
    orbState: OrbState,
    audioLevel: Float = 0f,
    displayMessage: String = "",
    statusText: String = "",
    isSnappedToRight: Boolean = false,
    onOrbTap: () -> Unit,
    onOrbDoubleTap: () -> Unit,
    onOrbLongPress: () -> Unit,
    onDragDelta: (dx: Float, dy: Float) -> Unit,
    onDragEnded: () -> Unit,
    onOpenApp: () -> Unit,
    onDismissPill: () -> Unit,
    onCloseOverlay: () -> Unit
) {
    var showQuickMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .wrapContentSize()
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isSnappedToRight) Arrangement.End else Arrangement.Start
    ) {
        // If snapped to the right, display the Mini Response Pill to the LEFT of the orb
        if (isSnappedToRight) {
            FloatingResponsePill(
                message = displayMessage,
                statusText = statusText,
                orbState = orbState,
                showQuickMenu = showQuickMenu,
                onOpenApp = onOpenApp,
                onDismissPill = onDismissPill,
                onCloseOverlay = onCloseOverlay,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        // The Glowing Interactive NOVA Orb
        FloatingOrbView(
            orbState = orbState,
            audioLevel = audioLevel,
            onTap = {
                if (showQuickMenu) {
                    showQuickMenu = false
                } else {
                    onOrbTap()
                }
            },
            onDoubleTap = {
                onOrbDoubleTap()
            },
            onLongPress = {
                showQuickMenu = !showQuickMenu
                onOrbLongPress()
            },
            onDragDelta = onDragDelta,
            onDragEnded = onDragEnded
        )

        // If snapped to the left (default), display the Mini Response Pill to the RIGHT of the orb
        if (!isSnappedToRight) {
            FloatingResponsePill(
                message = displayMessage,
                statusText = statusText,
                orbState = orbState,
                showQuickMenu = showQuickMenu,
                onOpenApp = onOpenApp,
                onDismissPill = onDismissPill,
                onCloseOverlay = onCloseOverlay,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun FloatingOrbView(
    orbState: OrbState,
    audioLevel: Float,
    onTap: () -> Unit,
    onDoubleTap: () -> Unit,
    onLongPress: () -> Unit,
    onDragDelta: (dx: Float, dy: Float) -> Unit,
    onDragEnded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "floating_orb_infinite")

    // Rotation animation for particles & orbiting lights
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (orbState) {
                    OrbState.THINKING -> 1500
                    OrbState.SPEAKING -> 2800
                    OrbState.LISTENING -> 2200
                    else -> 5000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing / Pulse animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (orbState) {
                    OrbState.LISTENING -> 750
                    OrbState.SPEAKING -> 600
                    OrbState.THINKING -> 900
                    OrbState.ERROR -> 450
                    else -> 1800
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Animated colors matching NOVA states
    val corePrimaryColor by animateColorAsState(
        targetValue = when (orbState) {
            OrbState.IDLE -> OrbIdleCyan
            OrbState.WAKE_DETECTED -> CyanNeon
            OrbState.LISTENING -> OrbListeningGreen
            OrbState.THINKING -> OrbThinkingBlue
            OrbState.SPEAKING -> OrbSpeakingMagenta
            OrbState.ERROR -> OrbErrorRed
        },
        animationSpec = tween(350),
        label = "core_color"
    )

    val coreSecondaryColor by animateColorAsState(
        targetValue = when (orbState) {
            OrbState.IDLE -> OrbIdlePurple
            OrbState.WAKE_DETECTED -> Color(0xFF00E5FF)
            OrbState.LISTENING -> Color(0xFF00B0FF)
            OrbState.THINKING -> CyanNeon
            OrbState.SPEAKING -> Color(0xFFFF80AB)
            OrbState.ERROR -> Color(0xFFFF8A80)
        },
        animationSpec = tween(350),
        label = "sec_color"
    )

    Box(
        modifier = modifier
            .size(68.dp)
            .testTag("floating_nova_orb")
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDragDelta(dragAmount.x, dragAmount.y)
                    },
                    onDragEnd = {
                        onDragEnded()
                    },
                    onDragCancel = {
                        onDragEnded()
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = { onDoubleTap() },
                    onLongPress = { onLongPress() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension / 2f * 0.72f
            val audioBoost = if (orbState == OrbState.LISTENING || orbState == OrbState.SPEAKING) {
                audioLevel * 0.12f
            } else 0f
            val dynamicRadius = baseRadius * (pulseScale + audioBoost)

            // 1. Ambient Background Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        corePrimaryColor.copy(alpha = 0.45f),
                        coreSecondaryColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.35f
                ),
                radius = baseRadius * 1.3f,
                center = center
            )

            // 2. State-Specific Outer Rings
            when (orbState) {
                OrbState.LISTENING -> {
                    // Expanding vibrant acoustic rings
                    drawCircle(
                        color = corePrimaryColor.copy(alpha = 0.6f),
                        radius = dynamicRadius * 1.15f,
                        center = center,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                    drawCircle(
                        color = CyanNeon.copy(alpha = 0.35f),
                        radius = dynamicRadius * 1.3f,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
                OrbState.THINKING -> {
                    // Orbiting neon dot particles
                    rotate(rotationAngle, pivot = center) {
                        val orbitRadius = dynamicRadius * 1.18f
                        for (i in 0 until 4) {
                            val angleRad = Math.toRadians((i * 90.0))
                            val dotX = center.x + (orbitRadius * cos(angleRad)).toFloat()
                            val dotY = center.y + (orbitRadius * sin(angleRad)).toFloat()
                            drawCircle(
                                color = if (i % 2 == 0) CyanNeon else PurpleNeon,
                                radius = 3.5.dp.toPx(),
                                center = Offset(dotX, dotY)
                            )
                        }
                    }
                }
                OrbState.SPEAKING -> {
                    // Sonic soundwaves radiating outward
                    rotate(rotationAngle * 0.5f, pivot = center) {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    OrbSpeakingMagenta.copy(alpha = 0.7f),
                                    CyanNeon.copy(alpha = 0.6f),
                                    OrbSpeakingMagenta.copy(alpha = 0.7f)
                                )
                            ),
                            radius = dynamicRadius * 1.2f,
                            center = center,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
                else -> {
                    // Subtle glowing ambient orbit ring
                    rotate(rotationAngle, pivot = center) {
                        drawCircle(
                            color = corePrimaryColor.copy(alpha = 0.25f),
                            radius = dynamicRadius * 1.1f,
                            center = center,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
                }
            }

            // 3. Core Vibrant Gradient Sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        corePrimaryColor,
                        coreSecondaryColor,
                        DarkBackground
                    ),
                    center = Offset(center.x - dynamicRadius * 0.25f, center.y - dynamicRadius * 0.25f),
                    radius = dynamicRadius
                ),
                radius = dynamicRadius,
                center = center
            )

            // 4. Center Specular Glow Highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = dynamicRadius * 0.22f,
                center = Offset(center.x - dynamicRadius * 0.28f, center.y - dynamicRadius * 0.28f)
            )
        }

        // Center mic icon indicator if listening or speaking
        if (orbState == OrbState.LISTENING) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Listening",
                tint = Color(0xFF070B14),
                modifier = Modifier.size(16.dp)
            )
        } else if (orbState == OrbState.SPEAKING) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Speaking",
                tint = Color(0xFF070B14),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun FloatingResponsePill(
    message: String,
    statusText: String,
    orbState: OrbState,
    showQuickMenu: Boolean,
    onOpenApp: () -> Unit,
    onDismissPill: () -> Unit,
    onCloseOverlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = message.isNotBlank() || showQuickMenu || orbState != OrbState.IDLE

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(220)) + expandHorizontally(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(180)) + shrinkHorizontally(animationSpec = tween(180)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 140.dp, max = 220.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(DarkSurface.copy(alpha = 0.94f))
                .border(
                    1.2.dp,
                    Brush.linearGradient(
                        listOf(
                            CyanNeon.copy(alpha = 0.6f),
                            PurpleNeon.copy(alpha = 0.4f)
                        )
                    ),
                    RoundedCornerShape(18.dp)
                )
                .padding(10.dp)
        ) {
            // Pill Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                when (orbState) {
                                    OrbState.LISTENING -> OrbListeningGreen
                                    OrbState.THINKING -> CyanNeon
                                    OrbState.SPEAKING -> OrbSpeakingMagenta
                                    OrbState.ERROR -> OrbErrorRed
                                    else -> PurpleNeon
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "NOVA AI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyanNeon,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Row {
                    IconButton(
                        onClick = onDismissPill,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // State/Status Indicator or Transcript/Response
            val displayText = when {
                message.isNotBlank() -> message
                statusText.isNotBlank() -> statusText
                orbState == OrbState.LISTENING -> "Listening to Boss..."
                orbState == OrbState.THINKING -> "Analyzing..."
                orbState == OrbState.SPEAKING -> "Responding..."
                else -> "Ready"
            }

            Text(
                text = displayText,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            // Quick Menu Options (if long pressed or explicitly opened)
            if (showQuickMenu) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Open App Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyanNeon.copy(alpha = 0.2f))
                            .clickable { onOpenApp() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Launch,
                                contentDescription = "Open App",
                                tint = CyanNeon,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Open",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CyanNeon,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Close Overlay Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(RoseError.copy(alpha = 0.2f))
                            .clickable { onCloseOverlay() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = RoseError,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Hide",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = RoseError,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
