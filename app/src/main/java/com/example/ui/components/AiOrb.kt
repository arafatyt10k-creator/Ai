package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.OrbState
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AiOrb(
    state: OrbState,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    audioLevel: Float = 0f,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_infinite")

    // Rotation animation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.THINKING -> 1800
                    OrbState.SPEAKING -> 3200
                    OrbState.LISTENING -> 2400
                    else -> 6000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulse animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.LISTENING -> 800
                    OrbState.SPEAKING -> 600
                    OrbState.THINKING -> 1000
                    OrbState.ERROR -> 500
                    else -> 2000
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Secondary ring pulse
    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.SPEAKING -> 1200
                    OrbState.LISTENING -> 1000
                    else -> 2500
                },
                easing = EaseInOutQuad
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_pulse"
    )

    // Dynamic Colors based on State
    val primaryCoreColor by animateColorAsState(
        targetValue = when (state) {
            OrbState.IDLE -> OrbIdleCyan
            OrbState.LISTENING -> OrbListeningGreen
            OrbState.THINKING -> OrbThinkingBlue
            OrbState.SPEAKING -> OrbSpeakingMagenta
            OrbState.ERROR -> OrbErrorRed
        },
        animationSpec = tween(400),
        label = "core_color"
    )

    val secondaryCoreColor by animateColorAsState(
        targetValue = when (state) {
            OrbState.IDLE -> OrbIdlePurple
            OrbState.LISTENING -> Color(0xFF00B0FF)
            OrbState.THINKING -> CyanNeon
            OrbState.SPEAKING -> Color(0xFFFF80AB)
            OrbState.ERROR -> Color(0xFFFF8A80)
        },
        animationSpec = tween(400),
        label = "secondary_color"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .testTag("ai_orb_component")
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2f

            // Dynamic scale with audio boost if listening or speaking
            val audioBoost = if (state == OrbState.LISTENING || state == OrbState.SPEAKING) {
                audioLevel * 0.15f
            } else 0f
            val dynamicRadius = baseRadius * (pulseScale + audioBoost) * 0.72f

            // 1. Outermost Ambient Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryCoreColor.copy(alpha = 0.35f),
                        secondaryCoreColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.1f
                ),
                radius = baseRadius * 1.05f,
                center = center
            )

            // 2. Rotating Outer Orbiting Tech Ring
            rotate(degrees = rotationAngle, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            primaryCoreColor.copy(alpha = 0.9f),
                            Color.Transparent,
                            secondaryCoreColor.copy(alpha = 0.8f),
                            Color.Transparent,
                            primaryCoreColor.copy(alpha = 0.9f)
                        ),
                        center = center
                    ),
                    radius = baseRadius * 0.88f * ringPulse,
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // Small orbiting satellites / particles
                val satRadius = baseRadius * 0.88f * ringPulse
                val angleRad1 = 0.0
                val angleRad2 = Math.PI
                drawCircle(
                    color = primaryCoreColor,
                    radius = 3.5.dp.toPx(),
                    center = Offset(
                        (center.x + satRadius * cos(angleRad1)).toFloat(),
                        (center.y + satRadius * sin(angleRad1)).toFloat()
                    )
                )
                drawCircle(
                    color = secondaryCoreColor,
                    radius = 3.dp.toPx(),
                    center = Offset(
                        (center.x + satRadius * cos(angleRad2)).toFloat(),
                        (center.y + satRadius * sin(angleRad2)).toFloat()
                    )
                )
            }

            // 3. Reverse Rotating Inner Geometric Ring
            rotate(degrees = -rotationAngle * 1.5f, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            primaryCoreColor.copy(alpha = 0.7f),
                            Color.Transparent,
                            secondaryCoreColor.copy(alpha = 0.7f)
                        ),
                        center = center
                    ),
                    radius = dynamicRadius * 1.08f,
                    center = center,
                    style = Stroke(width = 1.8.dp.toPx())
                )
            }

            // 4. Main Luminous Core Sphere (Radial Gradient)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        primaryCoreColor.copy(alpha = 0.9f),
                        secondaryCoreColor.copy(alpha = 0.85f),
                        Color(0xFF070B14).copy(alpha = 0.7f)
                    ),
                    center = Offset(center.x - dynamicRadius * 0.2f, center.y - dynamicRadius * 0.2f),
                    radius = dynamicRadius * 1.15f
                ),
                radius = dynamicRadius,
                center = center
            )

            // 5. Specular Highlight / Lens Flare
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.8f),
                        Color.Transparent
                    ),
                    center = Offset(center.x - dynamicRadius * 0.35f, center.y - dynamicRadius * 0.35f),
                    radius = dynamicRadius * 0.45f
                ),
                radius = dynamicRadius * 0.35f,
                center = Offset(center.x - dynamicRadius * 0.35f, center.y - dynamicRadius * 0.35f)
            )

            // 6. Voice Waveform ripples when speaking or listening
            if (state == OrbState.SPEAKING || state == OrbState.LISTENING) {
                drawVoiceRipples(center, dynamicRadius, primaryCoreColor, pulseScale, audioLevel)
            }
        }
    }
}

private fun DrawScope.drawVoiceRipples(
    center: Offset,
    radius: Float,
    color: Color,
    pulse: Float,
    audioLevel: Float
) {
    val barCount = 16
    for (i in 0 until barCount) {
        val angle = (i.toDouble() / barCount) * 2 * Math.PI
        val barLength = (10.dp.toPx() + (audioLevel * 25.dp.toPx())) * pulse
        val startX = (center.x + radius * cos(angle)).toFloat()
        val startY = (center.y + radius * sin(angle)).toFloat()
        val endX = (center.x + (radius + barLength) * cos(angle)).toFloat()
        val endY = (center.y + (radius + barLength) * sin(angle)).toFloat()

        drawLine(
            color = color.copy(alpha = 0.75f),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 2.5.dp.toPx()
        )
    }
}
