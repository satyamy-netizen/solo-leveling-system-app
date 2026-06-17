package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

class ErrorBoundaryState(
    val exception: MutableState<Throwable?>,
    val trigger: (Throwable) -> Unit,
    val reset: () -> Unit
)

val LocalErrorBoundary = staticCompositionLocalOf<ErrorBoundaryState> {
    error("No ErrorBoundaryState provided")
}

@Composable
fun SystemErrorBoundary(
    modifier: Modifier = Modifier,
    fallback: @Composable (Throwable, () -> Unit) -> Unit = { error, onReset ->
        DefaultSystemErrorFallback(error = error, onReset = onReset)
    },
    content: @Composable () -> Unit
) {
    val exception = remember { mutableStateOf<Throwable?>(null) }
    val state = remember {
        ErrorBoundaryState(
            exception = exception,
            trigger = { t ->
                exception.value = t
                SoloErrorLogger.error("SystemErrorBoundary", "Error boundary triggered", t)
            },
            reset = { exception.value = null }
        )
    }

    CompositionLocalProvider(LocalErrorBoundary provides state) {
        val t = exception.value
        if (t != null) {
            fallback(t) {
                state.reset()
            }
        } else {
            content()
        }
    }
}

@Composable
fun DefaultSystemErrorFallback(
    error: Throwable,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var showDetails by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030303))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // PULSING DANGER HEADER
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.5.dp, Color(0xFFFF1A1A).copy(alpha = alpha)), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF140707)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFF1A1A).copy(0.15f))
                            .border(1.dp, Color(0xFFFF1A1A).copy(0.4f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "System Decrypt Error",
                            tint = Color(0xFFFF1A1A),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "CRITICAL SYSTEM WARNING",
                        color = Color(0xFFFF1A1A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "THE MONARCH AWAKENING PROTOCOL DEVIATED INCOMPATIBLY OR DETECTED UNHANDLED NULL INITIALIZER STATE.",
                        color = Color(0xFFFFAAAA),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ERROR DETAIL BOX
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color(0xFF333333)), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E0E0E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ANOMALY EXCEPTION TYPE:",
                        color = Color(0xFFFFCC00),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = error.javaClass.simpleName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = "TELEMETRY MESSAGE:",
                        color = Color(0xFFFFCC00),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = error.localizedMessage ?: error.message ?: "No descriptive payload registered.",
                        color = Color(0xFFCCCCCC),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showDetails = !showDetails },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (showDetails) "HIDE PARMS" else "REVEAL RAW TRACE",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = {
                                val traceLog = error.stackTraceToString()
                                clipboardManager.setText(AnnotatedString(traceLog))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E1A1A)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.1f)
                        ) {
                            Text(
                                text = "COPY STACKTRACE",
                                color = Color(0xFFFFaaaa),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    AnimatedVisibility(visible = showDetails) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .background(Color(0xFF030303), RoundedCornerShape(8.dp))
                                .border(0.5.dp, Color(0xFF222222), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = error.stackTraceToString().take(1200) + if (error.stackTraceToString().length > 1200) "\n... [TRUNCATED FOR MEMORY STABILITY]" else "",
                                color = Color(0xFF888888),
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 12.sp,
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // RECOVERY ACTUATION BUTTONS
            Button(
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("system_auto_recover_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Trigger Decrypt Core Reset",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TRIGGER GATEWAY SYSTEM RECOVERY",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Actuating recovery will force a recomposition of the main gateway, safely bypassing standard render crashes, auto-flushing stale interface buffers.",
                color = Color(0xFF666666),
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
