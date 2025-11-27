package com.airnettie.mobile.child.ui

import android.app.Activity
import android.os.Bundle
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale // <-- FIX: Import added
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class BlockedAppActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            ComposeView(this).apply {
                setContent {
                    var exploded by remember { mutableStateOf(false) }
                    var colorIndex by remember { mutableStateOf(0) }

                    val neonColors = listOf(
                        Color.Magenta,
                        Color.Cyan,
                        Color(0xFF32CD32) // lime green
                    )

                    // Flicker animation for glow alpha
                    val infiniteTransition = rememberInfiniteTransition(label = "glow")
                    val glowAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.6f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "glowAlpha"
                    )

                    // Explosion animation (scale + fade out)
                    val explosionScale by animateFloatAsState(
                        targetValue = if (exploded) 3f else 1f,
                        animationSpec = tween(600, easing = FastOutSlowInEasing),
                        label = "explosionScale"
                    )
                    val explosionAlpha by animateFloatAsState(
                        targetValue = if (exploded) 0f else 1f,
                        animationSpec = tween(600, easing = LinearEasing),
                        label = "explosionAlpha"
                    )

                    // Cycle: flicker → explode → reanimate with new color
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(3000)
                            exploded = true
                            delay(800)
                            colorIndex = (colorIndex + 1) % neonColors.size
                            exploded = false
                        }
                    }

                    val currentColor = neonColors[colorIndex]

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CLOSED",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentColor.copy(alpha = if (exploded) explosionAlpha else glowAlpha),
                            style = TextStyle(
                                shadow = Shadow(
                                    color = currentColor.copy(alpha = 0.8f),
                                    blurRadius = 25f
                                )
                            ),
                            modifier = Modifier.scale(if (exploded) explosionScale else 1f)
                        )
                    }
                }
            }
        )
    }
}