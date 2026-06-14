package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.theme.*
import java.util.Calendar

@Composable
fun SystemMainApp(viewModel: SystemViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val alertMessage by viewModel.alertMessage.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val showFullScreenWarning by viewModel.showFullScreenWarning.collectAsStateWithLifecycle()
    val showFullScreenQuestAlert by viewModel.showFullScreenQuestAlert.collectAsStateWithLifecycle()
    val showWorkoutCompletionDialog by viewModel.showWorkoutCompletionDialog.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoloBlack)
    ) {
        // Starfield / Cyber Grid Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 60f
            // Draw subtle horizontal grid lines
            for (y in 0 until (size.height.toInt()) step gridSpacing.toInt()) {
                drawLine(
                    color = Color(0xFF1E293B).copy(alpha = 0.15f),
                    start = Offset(0f, y.toFloat()),
                    end = Offset(size.width, y.toFloat()),
                    strokeWidth = 1f
                )
            }
            // Draw subtle vertical grid lines
            for (x in 0 until (size.width.toInt()) step gridSpacing.toInt()) {
                drawLine(
                    color = Color(0xFF1E293B).copy(alpha = 0.15f),
                    start = Offset(x.toFloat(), 0f),
                    end = Offset(x.toFloat(), size.height),
                    strokeWidth = 1f
                )
            }
        }

        // Screen selection
        when (currentScreen) {
            "LOGIN" -> LoginScreen(viewModel)
            "CHALLENGE_SETUP" -> QuestionnaireScreen(viewModel)
            else -> {
                // Main Tabbed Interface for awakened Hunters
                DashboardLayout(viewModel, activeTab = currentScreen)
            }
        }

        // Global Toast/System Alert Popup Modal
        alertMessage?.let { msg ->
            val alertType by viewModel.alertType.collectAsStateWithLifecycle()
            SystemPopupNotification(
                message = msg,
                type = alertType,
                onDismiss = { viewModel.dismissAlert() }
            )
        }

        // Full Screen Emergency warning popup
        if (showFullScreenWarning) {
            FullScreenEmergencyWarning(viewModel)
        }

        // Full Screen Daily Quest Alert popup on opening/launching
        if (showFullScreenQuestAlert && currentScreen != "LOGIN" && currentScreen != "CHALLENGE_SETUP") {
            FullScreenDailyQuestAlert(viewModel)
        }

        // Hevy-style Workout completion achievement overlay
        if (showWorkoutCompletionDialog && currentScreen != "LOGIN" && currentScreen != "CHALLENGE_SETUP") {
            HevyWorkoutAchievementDialog(viewModel)
        }

        // Full Screen Solo Leveling level-up animation overlay!
        val levelUpTo by viewModel.showLevelUpAnimation.collectAsStateWithLifecycle()
        if (levelUpTo != null) {
            SoloLevelUpAnimationOverlay(level = levelUpTo!!, onDismiss = { viewModel.dismissLevelUpAnimation() })
        }
    }
}

// ------ 1. LOGIN SCREEN (Introduction Scene) ------
@Composable
fun LoginScreen(viewModel: SystemViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isCreateAccountMode by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isAuthenticating by remember { mutableStateOf(false) }

    val isFirebaseEnabled by viewModel.isFirebaseEnabled.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // System Alert Header
        Box(
            modifier = Modifier
                .border(2.dp, SoloBlueAccent, RoundedCornerShape(12.dp))
                .background(SoloCardBg.copy(alpha = 0.8f))
                .padding(16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "[ SYSTEM ANNOUNCEMENT ]",
                    color = SoloBlueAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "THE SYSTEM HAS DESIGNATED YOU AS A CHALLENGER.",
                    color = SoloTextPrimary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // System Logo Glow
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(
                    Brush.radialGradient(listOf(SoloBlueAccent.copy(0.4f), Color.Transparent)),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = "System Power Logo",
                tint = SoloBlueAccent,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "SOLO UPGRADE SYSTEM",
            color = SoloBlueAccent,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Awaken Your Growth Limit with Firebase Auth & Firestore",
            color = SoloTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Mode switch tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .background(SoloDarkGrey, RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = { isCreateAccountMode = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isCreateAccountMode) SoloCardBg else Color.Transparent
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f).height(38.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "HUNTER SIGN IN",
                    color = if (!isCreateAccountMode) SoloBlueAccent else SoloTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Button(
                onClick = { isCreateAccountMode = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCreateAccountMode) SoloCardBg else Color.Transparent
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f).height(38.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "NEW AWAKENING",
                    color = if (isCreateAccountMode) SoloBlueAccent else SoloTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Credentials Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SoloPurpleAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SoloCardBg.copy(alpha = 0.9f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (isCreateAccountMode) "Register Awakening Covenants" else "Initiate Secure Awake Protocols",
                    color = SoloTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (isCreateAccountMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Hunter Alias / Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SoloBlueAccent) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoloBlueAccent,
                            focusedLabelColor = SoloBlueAccent,
                            unfocusedBorderColor = SoloTextSecondary.copy(0.5f),
                            focusedTextColor = SoloTextPrimary,
                            unfocusedTextColor = SoloTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .testTag("login_name_input")
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Awakening Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SoloBlueAccent) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoloBlueAccent,
                        focusedLabelColor = SoloBlueAccent,
                        unfocusedBorderColor = SoloTextSecondary.copy(0.5f),
                        focusedTextColor = SoloTextPrimary,
                        unfocusedTextColor = SoloTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .testTag("login_email_input")
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Covenant Under-Seal Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SoloBlueAccent) },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(imageVector = imageVector, contentDescription = null, tint = SoloBlueAccent)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoloBlueAccent,
                        focusedLabelColor = SoloBlueAccent,
                        unfocusedBorderColor = SoloTextSecondary.copy(0.5f),
                        focusedTextColor = SoloTextPrimary,
                        unfocusedTextColor = SoloTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input")
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (isAuthenticating) {
            CircularProgressIndicator(color = SoloBlueAccent, modifier = Modifier.padding(16.dp))
        } else {
            // Action Secure Button
            Button(
                onClick = {
                    isAuthenticating = true
                    viewModel.loginOrAwakenWithFirebase(email, name, password, isCreateAccountMode) {
                        isAuthenticating = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SoloBlueAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(1.dp, Color.White.copy(0.4f), RoundedCornerShape(12.dp))
                    .testTag("awaken_button")
            ) {
                Text(
                    text = if (isCreateAccountMode) "ACCEPT AWAKENING" else "INITIATE AWAKE SECURE",
                    color = SoloBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Button 2: Google Sign-In linked
            Button(
                onClick = {
                    isAuthenticating = true
                    viewModel.handleGoogleAuthSignIn("mock-google-id-token-google-auth-conduit") {
                        isAuthenticating = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SoloDarkGrey),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, SoloTextSecondary.copy(0.4f), RoundedCornerShape(12.dp))
                    .testTag("google_signin_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = "Google Chronosphere Icon",
                    tint = SoloBlueAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LINK GOOGLE CHRONOSPHERE",
                    color = SoloTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    viewModel.loginOrAwaken(
                        name.ifBlank { "Offline Challenger" },
                        email.ifBlank { "offline@system.io" }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Accept Offline Sanctuary (Local Sandbox Bypass)",
                    color = SoloTextSecondary,
                    fontSize = 11.sp,
                    textDecoration = TextDecoration.Underline
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "WARNING: If you decline, your growth registers as E-Rank forever.",
            color = SoloDanger.copy(alpha = 0.8f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal
        )
    }
}

// ------ 2. QUESTIONNAIRE SCREEN (Awakening setup parameters) ------
@Composable
fun QuestionnaireScreen(viewModel: SystemViewModel) {
    var activeStep by remember { mutableStateOf(1) } // 1 or 2
    
    var age by remember { mutableStateOf(24f) }
    var intensity by remember { mutableStateOf("Medium") }
    var equipment by remember { mutableStateOf("Full Gym") }

    // Personal bests of major muscle lifts
    var benchPress by remember { mutableStateOf(65f) }
    var squat by remember { mutableStateOf(85f) }
    var deadlift by remember { mutableStateOf(105f) }
    var overheadPress by remember { mutableStateOf(40f) }

    val computedTotal = benchPress + squat + deadlift + overheadPress
    val experience = when {
        computedTotal < 160f -> "Beginner"
        computedTotal <= 330f -> "Intermediate"
        else -> "Advanced"
    }
    
    // Warning Alarm preference states
    var soundTimePreset by remember { mutableStateOf("08:00 AM") } // "08:00 AM", "05:00 PM", "08:00 PM", "Custom"
    var customHour by remember { mutableStateOf(8) }
    var customMinute by remember { mutableStateOf(0) }

    val finalAlarmTime = if (soundTimePreset == "Custom") {
        String.format("%02d:%02d", customHour, customMinute)
    } else {
        soundTimePreset
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // High-Tech Warning Banner (Intimidating)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF3F0B0B))
                .border(2.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alert",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CRITICAL SYSTEM PROTOCOL NOTICE",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Awakening locks your biology to the Monarch System. Failing to answer evaluation truthfully or skipping the daily quest sequence triggers intermediate Penalty Zone entrapment. There are no safe exits.",
                    color = Color(0xFFFCA5A5),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // High-tech Step indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SYNAPSE LINK STEP $activeStep / 2",
                color = SoloBlueAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = if (activeStep == 1) "STAGE I: PHYSICAL LIMITS" else "STAGE II: COGNITION CONFIG",
                color = SoloPurpleAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        // Animated progress lines
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(if (activeStep >= 1) SoloBlueAccent else Color.Gray.copy(0.3f))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(if (activeStep >= 2) SoloPurpleAccent else Color.Gray.copy(0.3f))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (activeStep == 1) {
            // TAB 1: PHYSIOLOGY (Age & Equipment)
            Text(
                text = "RE-REGISTER PHYSICAL PARAMETERS",
                color = SoloTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = "The system adapts metabolic strain indices according to mechanical assets.",
                color = SoloTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp)
            )

            // Age Card (Intimidating slider)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(1.dp, SoloBlueAccent.copy(0.3f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SoloCardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REGISTER BIOLOGICAL AGE",
                        color = SoloBlueAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Current age matrix:", color = SoloTextPrimary, fontWeight = FontWeight.Bold)
                        Text(text = "${age.toInt()} Years", color = SoloBlueAccent, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                    Slider(
                        value = age,
                        onValueChange = { age = it },
                        valueRange = 14f..75f,
                        colors = SliderDefaults.colors(
                            thumbColor = SoloBlueAccent,
                            activeTrackColor = SoloBlueAccent,
                            inactiveTrackColor = SoloTextSecondary.copy(0.2f)
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = when {
                            age < 20 -> "WARNING: High cardiac speed detected in young hunters. System intensity increased."
                            age > 50 -> "ALERT: Moderate wear detected. Automatic joint impact dampening protocol loaded."
                            else -> "Biological threshold optimal. Standard progression parameters active."
                        },
                        color = SoloTextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Available Equipment (Tied to physical limits!)
            OptionSelectionCard(
                title = "EQUIPMENT INVENTORY DEPLOYMENT",
                options = listOf("Full Gym", "Dumbbells Only", "Bodyweight"),
                selected = equipment,
                onSelect = { equipment = it }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Proceed Button
            Button(
                onClick = { activeStep = 2 },
                colors = ButtonDefaults.buttonColors(containerColor = SoloBlueAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(1.dp, Color.White.copy(0.3f), RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = "CONFIRM & CALIBRATE MEMORY ->",
                    color = SoloBlack,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            // TAB 2: COGNITION (Experience, Intensity, and Custom Alert Sound warning picker)
            Text(
                text = "NEURAL ALIGNMENT & COGNITION",
                color = SoloTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = "Align growth index multiplier thresholds and systemic alarm warning clocks.",
                color = SoloTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp)
            )

            // Custom Power Lift & Muscle Personal Best Evaluator
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(1.dp, SoloBlueAccent.copy(0.4f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SoloCardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AWAKENING POWER EVALUATOR (PERSONAL RECORD)",
                        color = SoloBlueAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Adjust personal records for major muscle category exercises. The System dynamically assigns your cognitive experience class.",
                        color = SoloTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 1. Bench Press
                    Column(modifier = Modifier.padding(bottom = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Chest & Triceps (Bench Press)", color = SoloTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${benchPress.toInt()} kg", color = SoloBlueAccent, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        }
                        Slider(
                            value = benchPress,
                            onValueChange = { benchPress = it },
                            valueRange = 10f..220f,
                            colors = SliderDefaults.colors(
                                thumbColor = SoloBlueAccent,
                                activeTrackColor = SoloBlueAccent,
                                inactiveTrackColor = SoloDarkGrey
                            )
                        )
                    }

                    // 2. Deadlift
                    Column(modifier = Modifier.padding(bottom = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Back & Postures (Deadlift)", color = SoloTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${deadlift.toInt()} kg", color = SoloBlueAccent, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        }
                        Slider(
                            value = deadlift,
                            onValueChange = { deadlift = it },
                            valueRange = 10f..300f,
                            colors = SliderDefaults.colors(
                                thumbColor = SoloBlueAccent,
                                activeTrackColor = SoloBlueAccent,
                                inactiveTrackColor = SoloDarkGrey
                            )
                        )
                    }

                    // 3. Squat
                    Column(modifier = Modifier.padding(bottom = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Thighs & Glutes (Barbell Squat)", color = SoloTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${squat.toInt()} kg", color = SoloBlueAccent, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        }
                        Slider(
                            value = squat,
                            onValueChange = { squat = it },
                            valueRange = 10f..250f,
                            colors = SliderDefaults.colors(
                                thumbColor = SoloBlueAccent,
                                activeTrackColor = SoloBlueAccent,
                                inactiveTrackColor = SoloDarkGrey
                            )
                        )
                    }

                    // 4. Overhead Press
                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Shoulders & Upper-Body (Overhead Press)", color = SoloTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${overheadPress.toInt()} kg", color = SoloBlueAccent, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        }
                        Slider(
                            value = overheadPress,
                            onValueChange = { overheadPress = it },
                            valueRange = 5f..120f,
                            colors = SliderDefaults.colors(
                                thumbColor = SoloBlueAccent,
                                activeTrackColor = SoloBlueAccent,
                                inactiveTrackColor = SoloDarkGrey
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamically Calculated Live Results
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL POWER CAPACITY:",
                            color = SoloTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${computedTotal.toInt()} KG",
                            color = SoloPurpleAccent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DECIDED SYSTEM EXPERIENCE:",
                            color = SoloTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        val rankLabel = when (experience) {
                            "Beginner" -> "BEGINNER (HUNTER E/D LEVEL)"
                            "Intermediate" -> "INTERMEDIATE (HUNTER C/B LEVEL)"
                            else -> "ADVANCED (HUNTER A/S LEVEL)"
                        }
                        val badgeColor = when (experience) {
                            "Beginner" -> Color(0xFF38BDF8)
                            "Intermediate" -> Color(0xFFFBBF24)
                            else -> Color(0xFFC084FC)
                        }
                        Box(
                            modifier = Modifier
                                .background(badgeColor.copy(0.15f), RoundedCornerShape(4.dp))
                                .border(1.dp, badgeColor.copy(0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = rankLabel,
                                color = badgeColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Preferred Intensity selection
            OptionSelectionCard(
                title = "SYSTEMIC EXTREMITY INTENSITY LEVEL",
                options = listOf("Low", "Medium", "High"),
                selected = intensity,
                onSelect = { intensity = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Alarm Sound warning preference (HH:MM options with custom slider details!)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(1.dp, SoloPurpleAccent.copy(0.4f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SoloCardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "WORKOUT ALARM CRITICAL TIMESPIN",
                        color = SoloPurpleAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Select when the System should signal workout penalty sound countdown alerts:",
                        color = SoloTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Selection times grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val presets = listOf("08:00 AM", "05:00 PM", "08:00 PM", "Custom")
                        presets.forEach { timeOption ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (soundTimePreset == timeOption) SoloPurpleAccent else SoloDarkGrey)
                                    .clickable { soundTimePreset = timeOption }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = timeOption,
                                    color = if (soundTimePreset == timeOption) Color.White else SoloTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Custom Slider-based time picker if "Custom" is active!
                    if (soundTimePreset == "Custom") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SoloDarkGrey)
                                .border(1.dp, SoloPurpleAccent.copy(0.3f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "CUSTOM DECREE TIME:",
                                        color = SoloTextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = String.format("%02d:%02d", customHour, customMinute),
                                        color = SoloPurpleAccent,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Hours index
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "Hour:", color = SoloTextPrimary, fontSize = 11.sp, modifier = Modifier.width(42.dp))
                                    Slider(
                                        value = customHour.toFloat(),
                                        onValueChange = { customHour = it.toInt() },
                                        valueRange = 0f..23f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = SoloPurpleAccent,
                                            activeTrackColor = SoloPurpleAccent,
                                            inactiveTrackColor = Color.Gray.copy(0.2f)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Minutes index
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "Min:", color = SoloTextPrimary, fontSize = 11.sp, modifier = Modifier.width(42.dp))
                                    Slider(
                                        value = customMinute.toFloat(),
                                        onValueChange = { customMinute = it.toInt() },
                                        valueRange = 0f..59f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = SoloPurpleAccent,
                                            activeTrackColor = SoloPurpleAccent,
                                            inactiveTrackColor = Color.Gray.copy(0.2f)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back button
                OutlinedButton(
                    onClick = { activeStep = 1 },
                    border = BorderStroke(1.dp, SoloBlueAccent.copy(0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .height(52.dp)
                ) {
                    Text(
                        text = "<- PREVIOUS",
                        color = SoloBlueAccent,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }

                // Finalize reawakening button
                Button(
                    onClick = {
                        viewModel.completeOnboarding(
                            age = age.toInt(),
                            experience = experience,
                            intensity = intensity,
                            equipment = equipment,
                            soundTime = finalAlarmTime,
                            benchPress = benchPress,
                            squat = squat,
                            deadlift = deadlift,
                            overheadPress = overheadPress
                        )
                        viewModel.testWarningSound()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoloPurpleAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp)
                        .border(1.dp, Color.White.copy(0.4f), RoundedCornerShape(12.dp))
                        .testTag("onboarding_complete_button")
                ) {
                    Text(
                        text = "INITIALIZE SYSTEM",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun OptionSelectionCard(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, SoloBlueAccent.copy(0.3f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SoloCardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = SoloTextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                options.forEach { option ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected == option) SoloBlueAccent else SoloDarkGrey)
                            .clickable { onSelect(option) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            color = if (selected == option) SoloBlack else SoloTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ------ 3. MAIN TABBED DASHBOARD INTERFACE ------
@Composable
fun DashboardLayout(viewModel: SystemViewModel, activeTab: String) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isOwner = userProfile?.email?.lowercase() == "satyamyadav30042008@gmail.com"

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = Color.Transparent,
        bottomBar = {
            // High-Tech Cyber Navigation Bar with specific safe margin
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoloDarkGrey)
                    .border(width = 1.dp, color = SoloBlueAccent.copy(0.3f), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val items = mutableListOf(
                        NavigationItemData("DASHBOARD", Icons.Default.AccountBox, "Status"),
                        NavigationItemData("FOCUS_MANAGER", Icons.Default.CalendarToday, "Focus"),
                        NavigationItemData("AI_TRAINER", Icons.Default.AutoAwesome, "AI Guide"),
                        NavigationItemData("WORKOUTS", Icons.Default.FitnessCenter, "Workouts"),
                        NavigationItemData("ANALYTICS", Icons.Default.Timeline, "Measurements"),
                        NavigationItemData("LEADERBOARD", Icons.Default.List, "Leagues")
                    )

                    // Inject Owner tab ONLY for the designated owner
                    if (isOwner) {
                        items.add(NavigationItemData("OWNER", Icons.Default.AdminPanelSettings, "Owner"))
                    }

                    items.forEach { item ->
                        val isSelected = activeTab == item.route
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.currentScreen.value = item.route }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) SoloBlueAccent else SoloTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.label,
                                color = if (isSelected) SoloBlueAccent else SoloTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            when (activeTab) {
                "DASHBOARD" -> DashboardScreen(viewModel)
                "FOCUS_MANAGER" -> FocusSplitManagerScreen(viewModel)
                "AI_TRAINER" -> AiTrainerScreen(viewModel)
                "WORKOUTS" -> WorkoutsScreen(viewModel)
                "ANALYTICS" -> AnalyticsScreen(viewModel)
                "LEADERBOARD" -> LeaderboardScreen(viewModel)
                "OWNER" -> OwnerAdminScreen(viewModel)
                "PROFILE" -> ProfileScreen(viewModel)
                "MANAGE_EXERCISES" -> ManageExercisesScreen(viewModel)
            }
        }
    }
}

data class NavigationItemData(val route: String, val icon: ImageVector, val label: String)

// ------ 4. DASHBOARD TAB SCREEN (User Level, Stats, Quests) ------
@Composable
fun DashboardScreen(viewModel: SystemViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val dailyQuestsList by viewModel.dailyQuests.collectAsStateWithLifecycle()
    val badgesList by viewModel.earnedBadges.collectAsStateWithLifecycle()
    val bodyFocusSplit by viewModel.bodyFocusSplit.collectAsStateWithLifecycle()
    val lastDayWeight by viewModel.lastDayTotalWeight.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        val user = profile ?: return
        val xpNeeded = 100 * user.level
        val xpRatio = if (xpNeeded > 0) user.xp.toFloat() / xpNeeded else 0f

        // 1. SYSTEM HEADER (Bento top section)
        val rankClass = when {
            user.level >= 11 -> "S-RANK MONARCH"
            user.level >= 9 -> "A-RANK ELITE"
            user.level >= 7 -> "B-RANK VETERAN"
            user.level >= 5 -> "C-RANK HUNTER"
            user.level >= 3 -> "D-RANK SCOUT"
            else -> "E-RANK RECRUIT"
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .drawBehind {
                    // Subtle glowing bottom border matching system header
                    val borderY = size.height
                    drawLine(
                        color = BentoBorderSky.copy(alpha = 0.2f),
                        start = Offset(0f, borderY),
                        end = Offset(size.width, borderY),
                        strokeWidth = 2f
                    )
                }
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "SYSTEM INTERFACE V2.4",
                    color = SoloBlueAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = user.name.uppercase(),
                    color = SoloTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-0.5).sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(SoloBlueAccent, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LVL ${user.level}",
                            color = SoloBlack,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "RANK: $rankClass",
                        color = SoloTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Mana / Stamina XP stats right-side box
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Mana / Stamina",
                    color = SoloBlueAccent.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(SoloDarkGrey)
                        .border(1.dp, BentoBorderSky.copy(alpha = 0.5f), CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(xpRatio)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(SoloPurpleAccent, SoloBlueAccent)
                                )
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. PRIMARY QUEST CARD (Large level snapshot card styled with deep shadows/glows)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(2.dp, BentoBorderSky, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SoloCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Profile Photo button that acts as an entrypoint to the user's Profile
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SoloDarkGrey)
                        .border(1.5.dp, SoloBlueAccent, CircleShape)
                        .clickable { viewModel.currentScreen.value = "PROFILE" }
                        .align(Alignment.TopEnd)
                        .testTag("profile_photo_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Person,
                        contentDescription = "Visit Hunter Profile",
                        tint = SoloBlueAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = "ACTIVE EVOLUTION PROTOCOL",
                        color = SoloBlueAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "[ PREPARING TO BECOME STRONG ]",
                        color = SoloTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.2).sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PROGRESSION LEVEL TRANSITION",
                            color = SoloTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        val pct = (xpRatio * 100).toInt()
                        Text(
                            text = "$pct%",
                            color = SoloBlueAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Custom XP Progress Bar (Large elegant progress line)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(SoloDarkGrey)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(xpRatio)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(SoloBlueAccent, SoloPurpleAccent)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = "Last Day Weight", tint = SoloPurpleAccent, modifier = Modifier.size(16.dp))
                            Text(text = " Last Lift: ${String.format("%.1f", lastDayWeight)} kg", color = SoloPurpleAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = "Active Streak", tint = SoloDanger, modifier = Modifier.size(16.dp))
                            Text(text = " Streak: ${user.streak} Days", color = SoloDanger, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 3. FIRST BENTO ROW (Today's Focus & Global Rank)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val calendar = Calendar.getInstance()
            var currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
            if (currentDayOfWeek == 0) currentDayOfWeek = 7
            val todayFocus = bodyFocusSplit.find { it.dayOfWeek == currentDayOfWeek }
            val focusPart = todayFocus?.focusPart ?: "Chest & Triceps"

            // Today's Focus Card (Left half-column)
            Card(
                modifier = Modifier
                    .weight(1.2f)
                    .height(130.dp)
                    .border(1.dp, BentoBorderSlate, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SoloDarkGrey),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SoloDarkGrey, Color(0xFF0F172A).copy(alpha = 0.6f))
                            )
                        )
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "TODAY'S FOCUS",
                                color = SoloBlueAccent.copy(alpha = 0.8f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = focusPart.uppercase(),
                                color = SoloTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 17.sp
                            )
                        }
                        Text(
                            text = "Intensity: ${user.intensity}-Grade",
                            color = SoloTextSecondary,
                            fontSize = 10.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            // Global Rank Mini Card (Right half-column)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
                    .border(1.dp, BentoBorderSlate, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SoloDarkGrey),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GLOBAL RANK",
                            color = SoloTextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "#1,204",
                            color = SoloBlueAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SoloBlueAccent.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                                .border(width = 0.5.dp, color = SoloBlueAccent.copy(alpha = 0.3f), shape = RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = user.name, color = SoloTextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            Text(text = "LV.${user.level}", color = SoloBlueAccent, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Cha-Hae", color = SoloTextSecondary.copy(alpha = 0.7f), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            Text(text = "LV.81", color = SoloTextSecondary.copy(alpha = 0.7f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // 4. SECOND BENTO ROW (Titles/Badges Mini & Status Points availability)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Titles Mini Cell (Left half-column)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(105.dp)
                    .border(1.dp, BentoBorderSlate, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SoloDarkGrey),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "EARNED TITLES",
                        color = SoloTextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (badgesList.isEmpty()) {
                            // Empty titles placeholders matching Design HTML spec
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(SoloBlueAccent.copy(alpha = 0.05f))
                                    .border(1.dp, SoloBlueAccent.copy(alpha = 0.2f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(modifier = Modifier.size(10.dp).background(SoloBlueAccent.copy(alpha = 0.5f), RoundedCornerShape(1.dp)))
                            }
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(SoloPurpleAccent.copy(alpha = 0.05f))
                                    .border(1.dp, SoloPurpleAccent.copy(alpha = 0.2f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("?", color = SoloPurpleAccent.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // Show up to 3 actual miniature earned badges
                            badgesList.take(3).forEach { badge ->
                                val badgeColor = if (badge.iconName == "gold" || badge.iconName == "star") SoloGold else SoloPurpleAccent
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(badgeColor.copy(alpha = 0.1f))
                                        .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val iconVector = when (badge.iconName) {
                                        "military_tech" -> Icons.Default.MilitaryTech
                                        "star" -> Icons.Default.Star
                                        "local_fire_department" -> Icons.Default.LocalFireDepartment
                                        "emoji_events" -> Icons.Default.EmojiEvents
                                        "fitness_center" -> Icons.Default.FitnessCenter
                                        else -> Icons.Default.Check
                                    }
                                    Icon(
                                        imageVector = iconVector,
                                        contentDescription = null,
                                        tint = badgeColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            if (badgesList.size < 3) {
                                repeat(3 - badgesList.size) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .border(1.dp, SoloTextSecondary.copy(alpha = 0.1f), RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("?", color = SoloTextSecondary.copy(alpha = 0.2f), fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Stat Points / Attribute Mini Cell (Right half-column)
            val hasPoints = user.statPoints > 0
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(105.dp)
                    .border(
                        width = if (hasPoints) 1.5.dp else 1.dp,
                        color = if (hasPoints) SoloPurpleAccent else BentoBorderSlate,
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = SoloDarkGrey),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "AVAILABLE AP",
                        color = if (hasPoints) SoloPurpleAccent else SoloTextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "${user.statPoints}",
                        color = if (hasPoints) SoloPurpleAccent else SoloTextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 32.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 5. STATUS POINT ATTRIBUTER CARD (High quality grid panel)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BentoBorderSlate, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SoloCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "STATUS POINT ATTRIBUTES",
                            color = SoloPurpleAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Distribute power points into physical statistics.",
                            color = SoloTextSecondary,
                            fontSize = 10.sp
                        )
                    }

                    if (user.statPoints > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SoloBlueAccent.copy(alpha = 0.2f))
                                .border(1.dp, SoloBlueAccent, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "DISTRIBUTE AVAILABLE",
                                color = SoloBlueAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats rows structured cleanly
                StatDistributionRow("STRENGTH (STR)", user.strength) { viewModel.increaseStat("STR") }
                StatDistributionRow("AGILITY (AGI)", user.agility) { viewModel.increaseStat("AGI") }
                StatDistributionRow("VITALITY (VIT)", user.vitality) { viewModel.increaseStat("VIT") }
                StatDistributionRow("INTELLIGENCE (INT)", user.intelligence) { viewModel.increaseStat("INT") }
                StatDistributionRow("SENSE (SEN)", user.sense) { viewModel.increaseStat("SEN") }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. DAILY ACTIVE INSTRUCTIONS (The Quests Card matching Design HTML grid section)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BentoBorderSky, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SoloCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DAILY QUEST: ACTIVE",
                    color = SoloBlueAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "If daily assignments are neglected, a punishment sequence triggers.",
                    color = SoloTextSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                if (dailyQuestsList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Loading Daily Quest logs...",
                            color = SoloTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    dailyQuestsList.forEach { quest ->
                        DailyQuestItemRow(quest) {
                            viewModel.incrementQuestCount(quest)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. REAL-TIME EARNED SYSTEM REWARD BADGES SCROLL
        Text(
            text = "SYSTEM ACCOMPLISHMENTS REWARD BADGES",
            color = SoloTextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        if (badgesList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BentoBorderSlate, RoundedCornerShape(16.dp))
                    .background(SoloDarkGrey)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = SoloTextSecondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No System Badges Earned yet.",
                        color = SoloTextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                badgesList.forEach { badge ->
                    BadgeGlowCard(badge)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 8. QUICK WARNING / ALERT SEQUENCE (Styled precisely after the red bento section in Design HTML)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BentoRedBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = BentoRedBg),
            shape = RoundedCornerShape(16.dp),
            onClick = { viewModel.testWarningSound() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Small glowing pulsing dot indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(SoloDanger, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "WARNING: INSUFFICIENT TRAINING",
                            color = SoloDanger,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Trigger ${user.intensity} workout penalty sound protocol. Scheduled alert: ${user.soundAlertTime}.",
                            color = SoloTextSecondary,
                            fontSize = 9.sp
                        )
                    }
                }

                // Bento Action Button
                Box(
                    modifier = Modifier
                        .border(1.dp, SoloDanger, RoundedCornerShape(48.dp))
                        .background(SoloDanger.copy(alpha = 0.15f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "START",
                        color = SoloDanger,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Firebase Cloud Sync Control Panel module ---
        FirebaseCloudSyncPanel(viewModel)

        Spacer(modifier = Modifier.height(24.dp))

        // Exit Protocol Button
        Button(
            onClick = { viewModel.handleFirebaseSignOut() },
            colors = ButtonDefaults.buttonColors(containerColor = SoloDarkGrey),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = SoloTextSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Exit System Protocol & Sign Out", color = SoloTextSecondary, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun FirebaseCloudSyncPanel(viewModel: SystemViewModel) {
    val firebaseUser by viewModel.firebaseUser.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()
    val syncResult by viewModel.cloudSyncResult.collectAsStateWithLifecycle()
    val isFbEnabled by viewModel.isFirebaseEnabled.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (firebaseUser != null) SoloBlueAccent.copy(alpha = 0.5f) else SoloPurpleAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SoloDarkGrey),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = if (firebaseUser != null) SoloBlueAccent else SoloTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FIRESTORE PERSISTENCE",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
                
                // Status indicator chip
                Box(
                    modifier = Modifier
                        .background(
                            if (firebaseUser != null) SoloBlueAccent.copy(alpha = 0.15f) else SoloDarkGrey,
                            RoundedCornerShape(4.dp)
                        )
                        .border(1.dp, if (firebaseUser != null) SoloBlueAccent else SoloTextSecondary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (firebaseUser != null) "ACTIVE SECURE" else "OFFLINE SANDBOX",
                        color = if (firebaseUser != null) SoloBlueAccent else SoloTextSecondary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!isFbEnabled) {
                Text(
                    text = "Firebase Auth/Firestore is disabled due to missing system credentials in this sandbox environment. Progress saves locally to internal SQLite Room DB.",
                    color = SoloTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            } else {
                Text(
                    text = firebaseUser?.let { "Securely linked with: ${it.email}" } 
                        ?: "You are logged in as a Guest. Link your progress with a cloud Firebase Covenant to unlock cross-device sync.",
                    color = if (firebaseUser != null) SoloTextPrimary else SoloTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.syncEverythingToCloud() },
                        colors = ButtonDefaults.buttonColors(containerColor = SoloBlueAccent.copy(0.2f)),
                        border = BorderStroke(1.dp, SoloBlueAccent.copy(alpha = 0.6f)),
                        modifier = Modifier.weight(1.5f).height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = SoloBlueAccent, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(14.dp), tint = SoloBlueAccent)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back Up", color = SoloBlueAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { viewModel.restoreEverythingFromCloud() },
                        colors = ButtonDefaults.buttonColors(containerColor = SoloPurpleAccent.copy(0.15f)),
                        border = BorderStroke(1.dp, SoloPurpleAccent.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1.5f).height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = SoloPurpleAccent, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp), tint = SoloPurpleAccent)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore", color = SoloPurpleAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                syncResult?.let { res ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = res,
                        color = if (res.startsWith("Success")) SoloBlueAccent else SoloDanger,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun StatDistributionRow(statName: String, value: Int, onIncrement: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = statName,
            color = SoloTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$value",
                color = SoloBlueAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 14.dp)
            )

            IconButton(
                onClick = onIncrement,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(SoloBlueAccent.copy(0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Upgrade Attribute",
                    tint = SoloBlueAccent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun DailyQuestItemRow(quest: DailyQuestEntity, onIncrement: () -> Unit) {
    val progressRatio = if (quest.targetCount > 0) quest.currentCount.toFloat() / quest.targetCount else 0f
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = if (quest.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (quest.isCompleted) SoloBlueAccent else SoloTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = quest.name,
                    color = if (quest.isCompleted) SoloTextSecondary else SoloTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (quest.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${quest.currentCount} / ${quest.targetCount}",
                    color = if (quest.isCompleted) SoloBlueAccent else SoloTextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(end = 8.dp)
                )

                if (!quest.isCompleted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SoloBlueAccent.copy(0.2f))
                            .clickable { onIncrement() }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+5",
                            color = SoloBlueAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress bar for single task
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(SoloDarkGrey)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressRatio)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(if (quest.isCompleted) SoloSuccess else SoloBlueAccent)
            )
        }
    }
}

@Composable
fun BadgeGlowCard(badge: BadgeEntity) {
    val iconVector = when (badge.iconName) {
        "military_tech" -> Icons.Default.MilitaryTech
        "star" -> Icons.Default.Star
        "local_fire_department" -> Icons.Default.LocalFireDepartment
        "emoji_events" -> Icons.Default.EmojiEvents
        "fitness_center" -> Icons.Default.FitnessCenter
        "shield" -> Icons.Default.Shield
        else -> Icons.Default.Troubleshoot
    }

    Box(
        modifier = Modifier
            .width(130.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SoloCardBg)
            .border(1.dp, SoloPurpleAccent, RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Icon with soft glowing background
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SoloPurpleAccent.copy(0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = badge.name,
                    tint = SoloPurpleAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = badge.name,
                color = SoloTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = badge.description,
                color = SoloTextSecondary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ------ 5. BODY FOCUS SPLIT TAB SCREEN ------
@Composable
fun FocusSplitManagerScreen(viewModel: SystemViewModel) {
    val bodyFocusSplit by viewModel.bodyFocusSplit.collectAsStateWithLifecycle()
    var editingDay by remember { mutableStateOf<BodyFocusEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Text(
            text = "MONARCH SPLIT PLANNER",
            color = SoloBlueAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = "Daily Body Focus Split",
            color = SoloTextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Select which part of your body you want to focus on each day. The selection persists until you manually mutate it.",
            color = SoloTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        if (bodyFocusSplit.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SoloBlueAccent)
            }
        } else {
            bodyFocusSplit.forEach { focus ->
                val dayName = when (focus.dayOfWeek) {
                    1 -> "Monday"
                    2 -> "Tuesday"
                    3 -> "Wednesday"
                    4 -> "Thursday"
                    5 -> "Friday"
                    6 -> "Saturday"
                    7 -> "Sunday"
                    else -> "Day"
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(1.dp, SoloBlueAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SoloCardBg),
                    onClick = { editingDay = focus }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = dayName.uppercase(),
                                color = SoloBlueAccent,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = focus.focusPart,
                                color = SoloTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit focus",
                            tint = SoloTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal Selector Dialog for mutating day's workout focus
    editingDay?.let { focus ->
        val dayName = when (focus.dayOfWeek) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            7 -> "Sunday"
            else -> "Day"
        }

        val focusPartOptions = listOf(
            "Chest & Triceps",
            "Back & Biceps",
            "Legs & Shoulders",
            "Abs & Cardio",
            "Full Body",
            "Rest Day"
        )

        Dialog(onDismissRequest = { editingDay = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, SoloPurpleAccent, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SoloDarkGrey),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "MUTATE SYSTEM PROTOCOL",
                        color = SoloPurpleAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Modify Focus: $dayName",
                        color = SoloTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    focusPartOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (focus.focusPart == option) SoloPurpleAccent.copy(0.2f) else Color.Transparent)
                                .clickable {
                                    viewModel.updateFocusSplit(focus.dayOfWeek, option)
                                    editingDay = null
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = focus.focusPart == option,
                                onClick = {
                                    viewModel.updateFocusSplit(focus.dayOfWeek, option)
                                    editingDay = null
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = SoloPurpleAccent)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = option, color = SoloTextPrimary, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { editingDay = null },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = "CANCEL", color = SoloTextSecondary)
                    }
                }
            }
        }
    }
}

// ------ 6. WORKOUTS LOGS SCANNER TAB (SUGGESTED EXERCISES & HISTORY) ------
@Composable
fun WorkoutsScreen(viewModel: SystemViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val bodyFocusSplit by viewModel.bodyFocusSplit.collectAsStateWithLifecycle()
    val workoutLogs by viewModel.workoutLogs.collectAsStateWithLifecycle()

    var customExerciseDialog by remember { mutableStateOf(false) }

    // Deduce current day Focus Group based on current datetime
    val calendar = Calendar.getInstance()
    var currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // Calendar.MONDAY = 2, so let's adjust to 1-7
    if (currentDayOfWeek == 0) currentDayOfWeek = 7 // Sunday fallback

    val todayFocus = bodyFocusSplit.find { it.dayOfWeek == currentDayOfWeek }
    val focusPart = todayFocus?.focusPart ?: "Chest & Triceps"

    val userEquipment = userProfile?.equipment ?: "Full Gym"
    val suggestedExercises = PreloadedWorkouts.getSuggestedWorkouts(focusPart, userEquipment)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "EQUIPMENT FILTER: ${userEquipment.uppercase()}",
                    color = SoloBlueAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Exercise Guidance",
                    color = SoloTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Button(
                onClick = { viewModel.currentScreen.value = "MANAGE_EXERCISES" },
                colors = ButtonDefaults.buttonColors(containerColor = SoloBlueAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_edit_exercise_nav_button")
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = SoloBlack, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "EXERCISES", color = SoloBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .background(SoloDarkGrey)
                .border(1.dp, SoloBlueAccent.copy(0.3f), RoundedCornerShape(10.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = SoloBlueAccent)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "TODAY'S TARGET FOCUS LINE",
                    color = SoloBlueAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = focusPart,
                    color = SoloTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (suggestedExercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Unscheduled Rest/Cardio Mode. You can log custom exercises below!", color = SoloTextSecondary, textAlign = TextAlign.Center)
            }
        } else {
            suggestedExercises.forEach { exercise ->
                SuggestedExerciseCard(exercise) { loggedSets, loggedReps, loggedWeight, loggedBarWeight, loggedIntensity ->
                    viewModel.logWorkoutExercise(
                        name = exercise.name,
                        category = exercise.category,
                        weight = loggedWeight,
                        sets = loggedSets,
                        reps = loggedReps,
                        barWeight = loggedBarWeight,
                        intensity = loggedIntensity
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Custom exercise insertion button
        Button(
            onClick = { customExerciseDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = SoloPurpleAccent),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Create, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "LOG CUSTOM GYM WORKOUT", fontWeight = FontWeight.Bold, color = Color.White, fontFamily = FontFamily.Monospace)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // HISTORICAL LOGS ROW
        Text(
            text = "WORKOUT ACCOMPLISHMENTS LOG",
            color = SoloTextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (workoutLogs.isEmpty()) {
            Text(
                text = "No recorded log records under active timeline.",
                color = SoloTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            workoutLogs.forEach { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, BentoBorderSlate, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = SoloCardBg.copy(0.7f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = log.exerciseName, color = SoloTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = "Sets: ${log.sets} | Reps: ${log.reps} | Weight: ${log.weight.toInt()}kg | Bar: ${log.barWeight.toInt()}kg",
                                color = SoloTextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(SoloPurpleAccent.copy(0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${log.intensity.uppercase()} INTENSE",
                                        color = SoloPurpleAccent,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = "+${log.xpEarned} XP Earned",
                                    color = SoloGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(text = "Logged: ${log.date}", color = SoloBlueAccent.copy(0.7f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }

                        IconButton(onClick = { viewModel.deleteWorkoutLog(log.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete log", tint = SoloDanger)
                        }
                    }
                }
            }
        }
    }

    // Modal Form for logging Custom Workouts
    if (customExerciseDialog) {
        var searchQuery by remember { mutableStateOf("") }
        var selectedExercise by remember { mutableStateOf<PreloadedExercise?>(null) }

        var exName by remember { mutableStateOf("") }
        var exCat by remember { mutableStateOf("Chest & Triceps") }
        var exSets by remember { mutableStateOf("3") }
        var exReps by remember { mutableStateOf("10") }
        var exWeight by remember { mutableStateOf("20") }
        var exBarWeight by remember { mutableStateOf("20") }
        var exIntensity by remember { mutableStateOf("Medium") }

        val searchFiltered = remember(searchQuery) {
            if (searchQuery.isBlank()) {
                PreloadedWorkouts.workouts.take(5)
            } else {
                PreloadedWorkouts.workouts.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
                }
            }
        }

        Dialog(onDismissRequest = { customExerciseDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, SoloBlueAccent, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SoloDarkGrey),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "NEW EXERCISE RECORD PROTOCOL",
                        color = SoloBlueAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Interactive Workout Vault",
                        color = SoloTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Searchable preloaded exercise box
                    Text(
                        text = "Search all pre-loaded exercises:",
                        color = SoloTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search (e.g. Bench Press, Squat)", color = SoloTextSecondary.copy(0.7f), fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoloBlueAccent,
                            unfocusedBorderColor = BentoBorderSlate,
                            focusedTextColor = SoloTextPrimary,
                            unfocusedTextColor = SoloTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Results list
                    if (searchFiltered.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 120.dp)
                                .background(SoloBlack.copy(0.3f), RoundedCornerShape(8.dp))
                                .border(1.dp, BentoBorderSlate, RoundedCornerShape(8.dp))
                                .padding(4.dp)
                        ) {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                searchFiltered.forEach { exercise ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedExercise = exercise
                                                exName = exercise.name
                                                exCat = exercise.category
                                                exSets = exercise.baseSets.toString()
                                                exReps = exercise.baseReps.replace(Regex("[^0-9]"), "")
                                                searchQuery = "" // clear search once selected
                                            }
                                            .padding(8.dp)
                                    ) {
                                        Text(text = exercise.name, color = SoloBlueAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(text = "Category: ${exercise.category} | ${exercise.equipmentRequired}", color = SoloTextSecondary, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = exName,
                        onValueChange = { exName = it },
                        label = { Text("Exercise Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloBlueAccent, focusedLabelColor = SoloBlueAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Target Muscle Group Focus: $exCat", color = SoloBlueAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    // Show selected exercise description/how to execute instructions!
                    selectedExercise?.let { ex ->
                        if (exName.equals(ex.name, ignoreCase = true)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .background(SoloBlueAccent.copy(0.08f), RoundedCornerShape(8.dp))
                                    .border(1.dp, SoloBlueAccent.copy(0.3f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "⚡ HOW TO EXECUTE:",
                                        color = SoloGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = ex.description,
                                        color = SoloTextPrimary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = exSets,
                            onValueChange = { exSets = it },
                            label = { Text("Sets") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloBlueAccent, focusedLabelColor = SoloBlueAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = exReps,
                            onValueChange = { exReps = it },
                            label = { Text("Reps") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloBlueAccent, focusedLabelColor = SoloBlueAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = exWeight,
                            onValueChange = { exWeight = it },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloBlueAccent, focusedLabelColor = SoloBlueAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = exBarWeight,
                            onValueChange = { exBarWeight = it },
                            label = { Text("Bar Wt (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloBlueAccent, focusedLabelColor = SoloBlueAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Intensity custom HUD row
                    Text(
                        text = "EXERCISE INTENSITY LEVEL:",
                        fontSize = 10.sp,
                        color = SoloTextSecondary,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Low", "Medium", "High").forEach { lvl ->
                            val isSelected = exIntensity.equals(lvl, ignoreCase = true)
                            Button(
                                onClick = { exIntensity = lvl },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) SoloBlueAccent else SoloBlack
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        1.dp,
                                        if (isSelected) SoloBlueAccent else BentoBorderSlate,
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Text(
                                    text = lvl.uppercase(),
                                    color = if (isSelected) SoloBlack else SoloTextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Real-Time XP & Volume calculation tracker
                    val setsInt = exSets.toIntOrNull() ?: 0
                    val repsInt = exReps.toIntOrNull() ?: 0
                    val wFloat = exWeight.toFloatOrNull() ?: 0f
                    val bFloat = exBarWeight.toFloatOrNull() ?: 0f
                    val multiplierVal = when (exIntensity.lowercase()) {
                        "low" -> 1.0f
                        "high" -> 1.8f
                        else -> 1.3f
                    }
                    val volumeCalculated = setsInt * repsInt * (wFloat + bFloat)
                    val expectedXp = ((volumeCalculated / 50f) * multiplierVal).toInt().coerceIn(30, 250)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoloBlack, RoundedCornerShape(8.dp))
                            .border(1.dp, SoloGold.copy(0.4f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "⚡ REAL-TIME SYSTEM SIMULATOR",
                                color = SoloGold,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("EST. VOL: ${volumeCalculated.toInt()} kg", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text("GAIN: +$expectedXp XP", color = SoloBlueAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { customExerciseDialog = false }) {
                            Text("CANCEL", color = SoloTextSecondary, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (exName.isNotBlank()) {
                                    val s = exSets.toIntOrNull() ?: 3
                                    val r = exReps.toIntOrNull() ?: 10
                                    val w = exWeight.toFloatOrNull() ?: 20f
                                    val b = exBarWeight.toFloatOrNull() ?: 20f
                                    viewModel.logWorkoutExercise(
                                        name = exName,
                                        category = exCat,
                                        weight = w,
                                        sets = s,
                                        reps = r,
                                        barWeight = b,
                                        intensity = exIntensity
                                    )
                                    customExerciseDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoloBlueAccent)
                        ) {
                            Text("SAVE TO SYSTEM", color = SoloBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

data class LocalWorkoutSet(val id: Int, val reps: String, val weight: String)

fun getMajorLiftKeyForSuggested(name: String): String? {
    val lower = name.lowercase()
    return when {
        lower.contains("bench press") -> "BENCH"
        lower.contains("squat") -> "SQUAT"
        lower.contains("deadlift") -> "DEADLIFT"
        lower.contains("overhead press") || lower.contains("shoulder press") -> "OHP"
        else -> null
    }
}

@Composable
fun SuggestedExerciseCard(
    exercise: PreloadedExercise,
    onLog: (sets: Int, reps: Int, weight: Float, barWeight: Float, intensity: String) -> Unit
) {
    var setList by remember {
        mutableStateOf(
            List(exercise.baseSets) { index ->
                LocalWorkoutSet(id = index + 1, reps = "10", weight = "15")
            }
        )
    }
    var barWeight by remember { mutableStateOf("20") }
    var intensity by remember { mutableStateOf("Medium") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, SoloBlueAccent.copy(0.3f), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = SoloCardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = exercise.name,
                color = SoloBlueAccent,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
            Text(
                text = exercise.description,
                color = SoloTextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Dynamic Set Rows (Hevy App style)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "EDIT SETS & REPS (HEVY STYLE)",
                    fontSize = 10.sp,
                    color = SoloBlueAccent,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                setList.forEachIndexed { idx, setItem ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Set ${idx + 1}",
                            color = SoloTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(42.dp)
                        )
                        OutlinedTextField(
                            value = setItem.reps,
                            onValueChange = { newVal ->
                                setList = setList.mapIndexed { i, s ->
                                    if (i == idx) s.copy(reps = newVal) else s
                                }
                            },
                            label = { Text("Reps", fontSize = 8.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloBlueAccent, focusedLabelColor = SoloBlueAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = setItem.weight,
                            onValueChange = { newVal ->
                                setList = setList.mapIndexed { i, s ->
                                    if (i == idx) s.copy(weight = newVal) else s
                                }
                            },
                            label = { Text("Weight (kg)", fontSize = 8.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloBlueAccent, focusedLabelColor = SoloBlueAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(
                            onClick = {
                                if (setList.size > 1) {
                                    setList = setList.filterIndexed { i, _ -> i != idx }
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Set",
                                tint = Color.Red.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val lastReps = setList.lastOrNull()?.reps ?: "10"
                            val lastWeight = setList.lastOrNull()?.weight ?: "15"
                            setList = setList + LocalWorkoutSet(id = setList.size + 1, reps = lastReps, weight = lastWeight)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoloDarkGrey),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Text("+ ADD SET", color = SoloBlueAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = barWeight,
                        onValueChange = { barWeight = it },
                        label = { Text("Barbell (kg)", fontSize = 8.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloBlueAccent, focusedLabelColor = SoloBlueAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                        modifier = Modifier.width(100.dp),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Intensity row for suggested cards
            Text(
                text = "INTENSITY FOCUS:",
                fontSize = 9.sp,
                color = SoloTextSecondary,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Low", "Medium", "High").forEach { lvl ->
                    val isSelected = intensity.equals(lvl, ignoreCase = true)
                    Button(
                        onClick = { intensity = lvl },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) SoloBlueAccent else SoloBlack
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .border(
                                1.dp,
                                if (isSelected) SoloBlueAccent else BentoBorderSlate,
                                RoundedCornerShape(6.dp)
                            ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = lvl.uppercase(),
                            color = if (isSelected) SoloBlack else SoloTextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live XP mathematical gauge calculation
            val sVal = setList.size
            val bVal = barWeight.toFloatOrNull() ?: 20f
            val multVal = when (intensity.lowercase()) {
                "low" -> 1.0f
                "high" -> 1.8f
                else -> 1.3f
            }

            // Calculate total volume & expected XP based on all sets
            val totalVolumeCalculated = setList.sumOf { set ->
                val r = set.reps.toIntOrNull() ?: 10
                val w = set.weight.toFloatOrNull() ?: 15f
                val vol = r * (w + bVal)
                vol.toDouble()
            }.toFloat()

            // Is any of them a major lift?
            val liftKey = getMajorLiftKeyForSuggested(exercise.name)
            val expectedXp = if (liftKey != null) {
                // Major lift: 1XP per rep base
                val totalReps = setList.sumOf { it.reps.toIntOrNull() ?: 10 }
                // Let's print base reps (it can earn more on PB, evaluated inside VM!)
                totalReps
            } else {
                ((totalVolumeCalculated / 50f) * multVal).toInt().coerceIn(30, 250)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoloBlack.copy(0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "VOLUME COMPLETED", color = SoloTextSecondary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "${totalVolumeCalculated.toInt()} kg", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "ESTIMATED XP", color = SoloTextSecondary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "+$expectedXp XP" + (if (liftKey != null) " (Base reps)" else ""), color = SoloGold, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val maxReps = setList.map { it.reps.toIntOrNull() ?: 10 }.maxOrNull() ?: 10
                    val maxWeight = setList.map { set -> set.weight.toFloatOrNull() ?: 15f }.maxOrNull() ?: 15f
                    val b = barWeight.toFloatOrNull() ?: 20f
                    onLog(sVal, maxReps, maxWeight, b, intensity)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SoloBlueAccent),
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "LOG GYM WORKOUT", color = SoloBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

// ------ 7. BODY MEASUREMENTS ANALYTICS SCREEN ------
@Composable
fun AnalyticsScreen(viewModel: SystemViewModel) {
    val measurements by viewModel.bodyMeasurements.collectAsStateWithLifecycle()
    var logDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Text(
            text = "BODY DIMENSION LOGS",
            color = SoloBlueAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = "Measurement Analytics",
            color = SoloTextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Analyze your physical size metrics to track cellular system progress.",
            color = SoloTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { logDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = SoloBlueAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = SoloBlack)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "LOG CURRENT BODY MEASUREMENTS", color = SoloBlack, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        val workoutLogs by viewModel.workoutLogs.collectAsStateWithLifecycle()
        SystemAnalyticsChart(measurements = measurements, workoutLogs = workoutLogs)

        Spacer(modifier = Modifier.height(24.dp))

        if (measurements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .border(1.dp, SoloTextSecondary.copy(0.2f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = SoloTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No recorded logs in timeline. Track measurements to view progressive charts.",
                        color = SoloTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Latest Log Snapshot Detail
            val latest = measurements.first()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(1.dp, SoloPurpleAccent.copy(0.4f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SoloCardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LATEST MEASUREMENT SNAPSHOT",
                        color = SoloPurpleAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Logged on ${latest.date}",
                        color = SoloTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    GridMetricsRow("Body Weight", "${latest.weight} kg", "Muscle Mass", "${latest.muscleMass} kg")
                    GridMetricsRow("Body Fat %", "${latest.fatPercentage}%", "Chest Circ.", "${latest.chest} cm")
                    GridMetricsRow("Arms Size", "${latest.arms} cm", "Waist Size", "${latest.waist} cm")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "HISTORIC ANALYTICS RECORDS",
                color = SoloTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Dynamic Chart representation / Progress logs list
            measurements.forEach { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = SoloDarkGrey)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Date: ${record.date}", color = SoloBlueAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            Text(
                                text = "W: ${record.weight}kg | Muscle: ${record.muscleMass}kg | Fat: ${record.fatPercentage}%",
                                color = SoloTextPrimary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Chest: ${record.chest}cm | Arms: ${record.arms}cm | Waist: ${record.waist}cm",
                                color = SoloTextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        IconButton(onClick = { viewModel.deleteMeasurement(record.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SoloDanger)
                        }
                    }
                }
            }
        }
    }

    // Measurement Logger Modal
    if (logDialog) {
        var wVal by remember { mutableStateOf("75") }
        var mVal by remember { mutableStateOf("35") }
        var fVal by remember { mutableStateOf("15") }
        var chestVal by remember { mutableStateOf("95") }
        var armsVal by remember { mutableStateOf("32") }
        var waistVal by remember { mutableStateOf("80") }

        Dialog(onDismissRequest = { logDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, SoloBlueAccent, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SoloDarkGrey),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "NEW ANALYTICS LOG",
                        color = SoloBlueAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Track Physique Indices",
                        color = SoloTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = wVal,
                        onValueChange = { wVal = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloBlueAccent, focusedLabelColor = SoloBlueAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = mVal,
                        onValueChange = { mVal = it },
                        label = { Text("Muscle Mass (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloBlueAccent, focusedLabelColor = SoloBlueAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fVal,
                        onValueChange = { fVal = it },
                        label = { Text("Body Fat %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloBlueAccent, focusedLabelColor = SoloBlueAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = chestVal,
                        onValueChange = { chestVal = it },
                        label = { Text("Chest size (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloBlueAccent, focusedLabelColor = SoloBlueAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = armsVal,
                        onValueChange = { armsVal = it },
                        label = { Text("Arms size (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloBlueAccent, focusedLabelColor = SoloBlueAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = waistVal,
                        onValueChange = { waistVal = it },
                        label = { Text("Waist size (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloBlueAccent, focusedLabelColor = SoloBlueAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { logDialog = false }) {
                            Text("CANCEL", color = SoloTextSecondary)
                        }
                        Button(
                            onClick = {
                                viewModel.logBodyMeasurements(
                                    weight = wVal.toFloatOrNull() ?: 70f,
                                    muscle = mVal.toFloatOrNull() ?: 30f,
                                    fat = fVal.toFloatOrNull() ?: 15f,
                                    chest = chestVal.toFloatOrNull() ?: 90f,
                                    arms = armsVal.toFloatOrNull() ?: 30f,
                                    waist = waistVal.toFloatOrNull() ?: 80f
                                )
                                logDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoloBlueAccent)
                        ) {
                            Text("LOG PROTOCOL", color = SoloBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridMetricsRow(title1: String, value1: String, title2: String, value2: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .background(SoloDarkGrey, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Text(text = title1, color = SoloTextSecondary, fontSize = 11.sp)
            Text(text = value1, color = SoloBlueAccent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .background(SoloDarkGrey, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Text(text = title2, color = SoloTextSecondary, fontSize = 11.sp)
            Text(text = value2, color = SoloBlueAccent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ------ 8. GLOBAL LEADERBOARD SCREEN ------
@Composable
fun LeaderboardScreen(viewModel: SystemViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val userLevel = userProfile?.level ?: 1
    val userName = userProfile?.name ?: "Challenger"

    // Mock global Hunters list from Solo Leveling lore
    val mockHunters = remember {
        mutableListOf(
            MockHunter("Sung Jin-Woo", 99, "S-Rank Monarch", 98, "active"),
            MockHunter("Thomas Andre", 89, "S-Rank National", 91, "active"),
            MockHunter("Ryuji Goto", 84, "S-Rank Hero", 84, "active"),
            MockHunter("Cha Hae-In", 82, "S-Rank Sword", 81, "active"),
            MockHunter("Baek Yoon-Ho", 74, "A-Rank Guildmaster", 70, "active"),
            MockHunter("Choi Jong-In", 72, "A-Rank Mage", 68, "active")
        )
    }

    // Insert user into list dynamically based on their level
    val sortedHunters = remember(userLevel, userName) {
        val list = mockHunters.toMutableList()
        val userRankClass = when {
            userLevel >= 11 -> "S-RANK MONARCH"
            userLevel >= 9 -> "A-RANK ELITE"
            userLevel >= 7 -> "B-RANK VETERAN"
            userLevel >= 5 -> "C-RANK HUNTER"
            userLevel >= 3 -> "D-RANK SCOUT"
            else -> "E-RANK RECRUIT"
        }
        list.add(MockHunter(userName, userLevel, userRankClass, 50, "user"))
        list.sortByDescending { it.level }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Text(
            text = "GLOBAL AWAKENED REGISTRY",
            color = SoloBlueAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = "Hunters Leaderboard",
            color = SoloTextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Compete with global S-Rank anomalies. Climb up rankings by leveling up your daily stats.",
            color = SoloTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        sortedHunters.forEachIndexed { index, hunter ->
            val isUser = hunter.type == "user"
            val rankBorderColor = when (index) {
                0 -> SoloGold
                1 -> SoloBlueAccent
                2 -> SoloPurpleAccent
                else -> Color.Transparent
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(
                        width = if (isUser) 2.dp else if (rankBorderColor != Color.Transparent) 1.dp else 0.dp,
                        color = if (isUser) SoloBlueAccent else rankBorderColor,
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) SoloDarkGrey else SoloCardBg.copy(0.7f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text(
                            text = "#${index + 1}",
                            color = if (index == 0) SoloGold else if (index == 1) SoloBlueAccent else SoloTextSecondary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            modifier = Modifier.width(36.dp)
                        )

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = hunter.name,
                                    color = if (isUser) SoloBlueAccent else SoloTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                if (isUser) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SoloPlusGlow)
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("YOU", color = SoloBlueAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text(text = hunter.rankName, color = SoloTextSecondary, fontSize = 11.sp)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "LEVEL ${hunter.level}",
                            color = SoloPurpleAccent,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Daily complete: ${hunter.questSuccess}%",
                            color = SoloTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

data class MockHunter(
    val name: String,
    val level: Int,
    val rankName: String,
    val questSuccess: Int,
    val type: String
)

val SoloPlusGlow = Color(0x2200E5FF)

// ------ 9. OWNER / ADMIN VIEW TAB ------
// Only available to satyamyadav30042008@gmail.com
@Composable
fun OwnerAdminScreen(viewModel: SystemViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Warning Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, SoloGold, RoundedCornerShape(12.dp))
                .background(SoloGold.copy(0.12f))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = SoloGold, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "OWNER ADMINISTRATIVE PORTAL",
                        color = SoloGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Designated Authority User: satyamyadav30042008@gmail.com. Access to global diagnostic files granted.",
                        color = SoloTextPrimary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Guide card to studio update
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SoloBlueAccent, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SoloCardBg)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "GOOGLE AI STUDIO UPDATE CORE",
                    color = SoloBlueAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "As the owner, you can compile and propagate system updates to this app seamlessly via the online console in AI Studio. Tap below to launch your dedicated system console.",
                    color = SoloTextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Open Google AI Studio Build UI Console as a system hyperlink
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            data = android.net.Uri.parse("https://ai.studio/build")
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoloBlueAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = SoloBlack)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "LAUNCH GOOGLE AI STUDIO UPDATE", color = SoloBlack, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "OTHER SYSTEM HUNTERS LOG (MOCK SECURE CACHE)",
            color = SoloTextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Mock records of other logs inside the region
        listOf(
            MockUserLog("Yoo Jin-Ho", "yoo_jinho_d@hunter.org", "Beginner", "High", "Dumbbells Only", "Completes quests every day to gain strength stats."),
            MockUserLog("Song Chi-Yul", "song_chi_yul@mastery.kr", "Advanced", "High", "Full Gym", "Maintains clean daily squats and lunges."),
            MockUserLog("Han Song-Yi", "songyi_han@academy.net", "Beginner", "Low", "Bodyweight", "Prefers lighter active cardio routines."),
            MockUserLog("Ju Hee", "b_healer_ju@church.or.kr", "Intermediate", "Medium", "Bodyweight", "Focused heavily on active planks and flexibility recovery.")
        ).forEach { mockUsr ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = SoloDarkGrey)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = mockUsr.name, color = SoloBlueAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SoloPurpleAccent.copy(0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = mockUsr.intensity, color = SoloPurpleAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(text = "Email: ${mockUsr.email}", color = SoloTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Equip: ${mockUsr.equipment} | Level: ${mockUsr.level}", color = SoloTextPrimary, fontSize = 12.sp)
                    Text(text = "System Observation: ${mockUsr.notes}", color = SoloTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(30.dp))
    }
}

data class MockUserLog(
    val name: String,
    val email: String,
    val level: String,
    val intensity: String,
    val equipment: String,
    val notes: String
)

// ------ 10. REUSABLE FLOATING SYSTEM SUCCESS POPUP ------
@Composable
fun SystemPopupNotification(message: String, type: String, onDismiss: () -> Unit) {
    val borderColor = when (type) {
        "achievement" -> SoloBlueAccent
        "warning" -> SoloDanger
        else -> SoloBlueAccent
    }
    
    val iconVector = when (type) {
        "achievement" -> Icons.Default.EmojiEvents
        "warning" -> Icons.Default.Warning
        else -> Icons.Default.Info
    }
    
    val titleText = when (type) {
        "achievement" -> "[ SYSTEM ACHIEVEMENT UNLOCKED ]"
        "warning" -> "[ SYSTEM ALARM DECREE ]"
        else -> "[ SYSTEM DECREE ]"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.65f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, borderColor, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SoloDarkGrey),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = borderColor,
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = titleText,
                    color = borderColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    color = SoloTextPrimary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = borderColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ACCEPT DECREE",
                        color = SoloBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// ------ 10. AI SHADOW TRAINER TAB SCREEN ------
@Composable
fun AiTrainerScreen(viewModel: SystemViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val aiMessage by viewModel.aiTrainerMessage.collectAsStateWithLifecycle()
    val aiExercises by viewModel.aiTrainerExercises.collectAsStateWithLifecycle()
    val isLoading by viewModel.aiTrainerLoading.collectAsStateWithLifecycle()
    val errorMsg by viewModel.aiTrainerError.collectAsStateWithLifecycle()
    val focusSplit by viewModel.bodyFocusSplit.collectAsStateWithLifecycle()

    // Determine today's focus part
    val calendar = Calendar.getInstance()
    var currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
    if (currentDayOfWeek == 0) currentDayOfWeek = 7
    val todayFocus = focusSplit.find { it.dayOfWeek == currentDayOfWeek }?.focusPart ?: "Full Body"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Aesthetic Title block
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SHADOW GUIDE TRANSCEIVER",
                    color = SoloBlueAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "System AI workout synthesizer and cognitive load evaluator.",
                    color = SoloTextSecondary,
                    fontSize = 11.sp
                )
            }
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = SoloPurpleAccent,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System state dashboard Bento Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SoloBlueAccent.copy(0.3f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SoloCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SYSTEM SYNAPSE STATUS",
                        color = SoloBlueAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    
                    val keyAvailable = com.example.BuildConfig.GEMINI_API_KEY.isNotBlank() && 
                                       com.example.BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
                    
                    Box(
                        modifier = Modifier
                            .background(
                                if (keyAvailable) Color(0xFF064E3B) else Color(0xFF1E293B),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (keyAvailable) "DIRECT CHANNEL ACTIVE" else "LOCAL MATRIX SIMULATOR",
                            color = if (keyAvailable) Color(0xFF34D399) else Color(0xFF94A3B8),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Detail indices
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "TODAY FOCUS PART", color = SoloTextSecondary, fontSize = 9.sp)
                        Text(text = todayFocus, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "CURRENT STABILITY INDEX", color = SoloTextSecondary, fontSize = 9.sp)
                        Text(
                            text = "98.7% SYNCED",
                            color = SoloPurpleAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Intimidating AI Guidance decree card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, SoloPurpleAccent.copy(0.6f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SoloDarkGrey),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "[ SHADOW GUIDE DECREE ]",
                    color = SoloPurpleAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = aiMessage,
                    color = SoloTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Routing Exception: $errorMsg", color = SoloDanger, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Synthesize Trigger Button
        Button(
            onClick = { viewModel.generateAiTrainerRecommendation() },
            colors = ButtonDefaults.buttonColors(containerColor = SoloBlueAccent),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .border(1.dp, Color.White.copy(0.4f), RoundedCornerShape(12.dp))
                .testTag("ai_trigger_button")
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = SoloBlack,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "COMMUNING WITH SHADOW CORE...",
                    color = SoloBlack,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                Icon(imageVector = Icons.Default.Cyclone, contentDescription = null, tint = SoloBlack, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SYNTHESIZE SPECIALIZED PROTOCOLS",
                    color = SoloBlack,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Render Generated Workouts block
        if (aiExercises.isNotEmpty()) {
            Text(
                text = "SYNTHESIZED REAWAKENING LIFTS",
                color = SoloTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            aiExercises.forEach { exercise ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(1.dp, SoloBlueAccent.copy(0.2f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SoloCardBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = exercise.name,
                                color = SoloTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF312E81), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = exercise.intensity.uppercase(),
                                    color = Color(0xFFA5B4FC),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = exercise.description, color = SoloTextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Target specifics indices row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row {
                                Column(modifier = Modifier.padding(end = 16.dp)) {
                                    Text(text = "SETS", color = SoloTextSecondary, fontSize = 8.sp)
                                    Text(text = "${exercise.sets}", color = SoloBlueAccent, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                }
                                Column(modifier = Modifier.padding(end = 16.dp)) {
                                    Text(text = "TARGET REPS", color = SoloTextSecondary, fontSize = 8.sp)
                                    Text(text = exercise.reps, color = SoloBlueAccent, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                }
                                Column {
                                    Text(text = "WEIGHT MATRIX", color = SoloTextSecondary, fontSize = 8.sp)
                                    Text(text = exercise.recommendedWeight, color = SoloPurpleAccent, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                }
                            }

                            // Individual execute logger button!
                            Button(
                                onClick = {
                                    val repCount = exercise.reps.filter { it.isDigit() }.toIntOrNull() ?: 12
                                    val weightVal = exercise.recommendedWeight.filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f
                                    viewModel.logWorkoutExercise(
                                        name = exercise.name,
                                        category = todayFocus,
                                        weight = weightVal,
                                        sets = exercise.sets,
                                        reps = repCount
                                    )
                                    android.widget.Toast.makeText(context, "Logged: ${exercise.name}! XP gained.", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoloPurpleAccent),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("execute_ai_workout_button")
                            ) {
                                Text(
                                    text = "EXECUTE",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(40.dp))
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = SoloTextSecondary.copy(alpha = 0.3f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "NO DYNAMIC FLOW MAP DEPLOYED",
                color = SoloTextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Command the Shadow Transceiver above to register customized physical vectors.",
                color = SoloTextSecondary.copy(alpha = 0.6f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ------ 11. FULL SCREEN EMERGENCY WARNING OVERLAY ------
@Composable
fun FullScreenEmergencyWarning(viewModel: SystemViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val user = profile ?: return
    
    // Simple pulsing animation state
    val infiniteTransition = rememberInfiniteTransition(label = "hazard")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hazard_flash"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0202)) // Deep obsidian red
            .padding(24.dp)
            .clickable(enabled = false) {}, // Intercept touch
        contentAlignment = Alignment.Center
    ) {
        // Red Pulsing Glow Frame
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(6.dp, Color(0xFFEF4444).copy(alpha = alphaAnim), RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large Flashing Warning System Emblem
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "System Hazard",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 16.dp)
                )

                Text(
                    text = "WARNING",
                    color = Color(0xFFEF4444),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 4.sp,
                )

                Text(
                    text = "PENALTY PROTOCOL INITIATED",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                // Warning body text box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F0808)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "[ MONARCH SYSTEM EVALUATION DECREE ]",
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            text = "You have failed to log sufficient daily physical parameters or skipped training milestones. The system intensity limits has registered a penalty sequence matching your current status matrix.",
                            color = Color(0xFFFCA5A5),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Metadata Bento stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left stat panel
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = SoloCardBg)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "MONARCH INTENSITY", color = SoloTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text(
                                text = user.intensity.uppercase(),
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Right stat panel
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = SoloCardBg)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "WARNING PREFERENCE", color = SoloTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text(
                                text = user.soundAlertTime,
                                color = SoloBlueAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Option box with system hazard signs
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF3F0A0A), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "☠️ PENALTY SYSTEM: SURVIVE 4 HOURS OR EXECUTE COMPULSORY S-CLASS CARDIO LIFT NOW.",
                        color = Color(0xFFEF4444),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action 1: Re-trigger intimidating warning siren sound!
                Button(
                    onClick = { viewModel.testWarningSound() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(1.dp, Color.Black.copy(0.3f), RoundedCornerShape(12.dp))
                        .testTag("siren_sound_trigger_button")
                ) {
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = SoloBlack)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PLAY SIREN ALERT SOUND",
                        color = SoloBlack,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action 2: Evade Penalty (Dismiss warnings after acknowledging)
                Button(
                    onClick = { viewModel.dismissFullScreenWarning() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(2.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                        .testTag("dismiss_full_screen_warning_button")
                ) {
                    Text(
                        text = "RESTORE SYSTEM COHERENCY",
                        color = SoloBlack,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// ------ 12. WEARABLE SYNC SCREEN (SYSTEM CONDUIT GATEWAY) ------
@Composable
fun WearableSyncScreen(viewModel: SystemViewModel) {
    val coroutineScope = rememberCoroutineScope()
    var isSyncing by remember { mutableStateOf(false) }
    var syncProgress by remember { mutableFloatStateOf(0f) }
    var syncStatusMessage by remember { mutableStateOf("Ready to initiate linked sync") }
    
    // Configured device
    var pairedDeviceName by remember { mutableStateOf("Hunter Bio-Link Band v2") }
    var deviceBattery by remember { mutableIntStateOf(84) }
    var lastSyncTime by remember { mutableStateOf("12 hours ago") }
    
    // Bluetooth linkage status
    var isBluetoothEnabled by remember { mutableStateOf(true) }
    var isScanning by remember { mutableStateOf(false) }
    
    // Synced health metrics
    var currentHeartRate by remember { mutableIntStateOf(72) }
    var todaySteps by remember { mutableIntStateOf(4850) }
    var todayCalories by remember { mutableIntStateOf(215) }
    var sleepScore by remember { mutableIntStateOf(82) }
    
    // Mock nearby devices
    val nearbyDevices = listOf(
        Pair("Hunter Bio-Link Band v2", "Signal: Excellent (BLE)"),
        Pair("Shadow Monarch ActiveWatch Pro", "Signal: Very Strong (RF)"),
        Pair("S-Class Gate Calibration Ring", "Signal: Medium (Ultra-Wideband)"),
        Pair("Standard Leveling Tracker v1", "Signal: Weak (Bluetooth)")
    )
    
    // Simulated sync logs
    val syncLogList = remember { 
        mutableStateListOf(
            "System telemetry online - Initial channel established.",
            "Physiological metrics aggregated at 08:30 AM",
            "Automatic sleep cycle analysis: Recovery score 82%."
        )
    }

    // Coroutine mock scanning
    LaunchedEffect(isScanning) {
        if (isScanning) {
            delay(2000)
            isScanning = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoloBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // --- Header Section ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "MONARCH SYSTEM LINK",
                    color = SoloBlueAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "BIOLINK & WEARABLE DATA CONDUIT",
                    color = SoloTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }
            Box(
                modifier = Modifier
                    .background(
                        if (isBluetoothEnabled) SoloBlueAccent.copy(0.15f) else Color.Red.copy(0.15f),
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        if (isBluetoothEnabled) SoloBlueAccent else Color.Red,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { isBluetoothEnabled = !isBluetoothEnabled }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Bluetooth Status",
                        tint = if (isBluetoothEnabled) SoloBlueAccent else Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBluetoothEnabled) "LINK ON" else "LINK OFF",
                        color = if (isBluetoothEnabled) SoloBlueAccent else Color.Red,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // --- Active Device Status Bento Panel ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(2.dp, SoloBlueAccent.copy(0.35f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SoloCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    if (isBluetoothEnabled) SoloSuccess else SoloTextSecondary,
                                    CircleShape
                                )
                                .border(1.dp, Color.White.copy(0.3f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBluetoothEnabled) "CONDUIT STATUS: ACTIVE LINK" else "CONDUIT STATUS: OFFLINE",
                            color = if (isBluetoothEnabled) SoloSuccess else SoloTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(SoloPurpleAccent.copy(0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$deviceBattery% POWER",
                            color = SoloPurpleAccent,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = pairedDeviceName,
                    color = SoloTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Last Synced: $lastSyncTime",
                        color = SoloTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "BLE Secure Channel",
                        color = SoloBlueAccent.copy(0.8f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // --- Live Health Channel Telemetry Matrix (2x2 Grid) ---
        Text(
            text = "PHYSIOLOGICAL CHANNELS",
            color = SoloBlueAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Steps Channel
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, SoloBlueAccent.copy(0.15f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SoloCardBg)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "STAMINA", color = SoloTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Icon(imageVector = Icons.Default.Timeline, contentDescription = null, tint = SoloBlueAccent, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "$todaySteps",
                        color = SoloBlueAccent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                    Text(text = "synced steps", color = SoloTextSecondary, fontSize = 10.sp)
                }
            }

            // Heart Rate Channel
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, SoloBlueAccent.copy(0.15f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SoloCardBg)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "HEART CORE", color = SoloTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = SoloDanger, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "$currentHeartRate BPM",
                        color = SoloDanger,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                    Text(text = "pulse-rate", color = SoloTextSecondary, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Calories Channel
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, SoloBlueAccent.copy(0.15f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SoloCardBg)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "DISSIPATION", color = SoloTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = SoloGold, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "$todayCalories KCAL",
                        color = SoloGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                    Text(text = "energy expended", color = SoloTextSecondary, fontSize = 10.sp)
                }
            }

            // Sleep Restoration Channel
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, SoloBlueAccent.copy(0.15f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SoloCardBg)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "RECOVERY", color = SoloTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = SoloPurpleAccent, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "$sleepScore%",
                        color = SoloPurpleAccent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                    Text(text = "sleep recovery", color = SoloTextSecondary, fontSize = 10.sp)
                }
            }
        }

        // --- Core Action Button ---
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, SoloBlueAccent.copy(0.2f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SoloDarkGrey),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isSyncing) {
                    Text(
                        text = syncStatusMessage.uppercase(),
                        color = SoloBlueAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LinearProgressIndicator(
                        progress = { syncProgress },
                        color = SoloBlueAccent,
                        trackColor = SoloBlack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${(syncProgress * 100).toInt()}% INTEGRATED",
                        color = SoloTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "SYSTEM SYNC CONDUIT",
                                color = SoloTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tethers real-world training steps into the System tracker.",
                                color = SoloTextSecondary,
                                fontSize = 10.sp
                            )
                        }

                        Button(
                            onClick = {
                                if (isBluetoothEnabled) {
                                    isSyncing = true
                                    syncProgress = 0f
                                    coroutineScope.launch {
                                        syncStatusMessage = "Establishing paired Bluetooth telemetry..."
                                        delay(600)
                                        syncProgress = 0.25f
                                        syncStatusMessage = "Extracting physical step metrics..."
                                        delay(600)
                                        syncProgress = 0.5f
                                        todaySteps += (400..1200).random()
                                        todayCalories += (30..80).random()
                                        currentHeartRate = (105..135).random()
                                        syncStatusMessage = "Syncing with Monarch System processor..."
                                        delay(600)
                                        syncProgress = 0.75f
                                        syncStatusMessage = "Updating local core values..."
                                        delay(600)
                                        syncProgress = 1.0f
                                        syncStatusMessage = "Sync process completed successfully!"
                                        delay(400)
                                        
                                        // Dynamic Side Effects: Gain XP & Trigger Alert Warnings
                                        viewModel.gainXp(20)
                                        viewModel.testWarningSound()
                                        viewModel.alertMessage.value = "Conduit synchronization complete! XP +20 loaded."
                                        
                                        lastSyncTime = "Just now"
                                        deviceBattery = (40..98).random()
                                        syncLogList.add(0, "Aggregated full sync: $todaySteps steps, $todayCalories KCAL.")
                                        isSyncing = false
                                        syncProgress = 0f
                                    }
                                } else {
                                    viewModel.alertMessage.value = "Activate Link Protocol first to enable syncing!"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoloBlueAccent),
                            shape = RoundedCornerShape(12.dp),
                            enabled = isBluetoothEnabled,
                            modifier = Modifier
                                .testTag("wearable_sync_button")
                        ) {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = SoloBlack, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "SYNC", color = SoloBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // --- Bluetooth Radar Scanner section ---
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SoloBlueAccent.copy(0.15f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = SoloCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BLUETOOTH CORES RADAR",
                        color = SoloBlueAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Button(
                        onClick = { isScanning = true },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isScanning) SoloDarkGrey else SoloPurpleAccent),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isScanning && isBluetoothEnabled,
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("radar_scan_button")
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                color = SoloPurpleAccent,
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "SCAN", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (!isBluetoothEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TURN ON LINK PROTOCOL TO SCAN FOR BLUETOOTH WEARABLES",
                            color = SoloTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (isScanning) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = SoloBlueAccent)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SCANNING REAL-TIME RECON SPECTRUM...",
                            color = SoloTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        nearbyDevices.forEach { device ->
                            val isPaired = pairedDeviceName == device.first
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isPaired) SoloBlueAccent.copy(0.08f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isPaired) SoloBlueAccent.copy(0.4f) else Color.White.copy(0.05f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        pairedDeviceName = device.first
                                        viewModel.alertMessage.value = "Calibrated system link: ${device.first}!"
                                        viewModel.testWarningSound()
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = device.first, color = SoloTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(text = device.second, color = SoloTextSecondary, fontSize = 10.sp)
                                }

                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isPaired) SoloSuccess.copy(0.15f) else SoloDarkGrey,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isPaired) SoloSuccess else Color.White.copy(0.1f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (isPaired) "PAIRED" else "CONNECT",
                                        color = if (isPaired) SoloSuccess else SoloTextSecondary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Console History Logs ---
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = SoloDarkGrey)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONDUIT METRIC STREAM",
                        color = SoloPurpleAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${syncLogList.size} ENTRIES",
                        color = SoloTextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    syncLogList.take(5).forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(0.3f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "»",
                                color = SoloBlueAccent,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = log,
                                color = SoloTextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

data class ChartDataPoint(
    val label: String,
    val dateStr: String,
    val value: Float
)

@Composable
fun SystemAnalyticsChart(
    measurements: List<BodyMeasurementEntity>,
    workoutLogs: List<WorkoutLogEntity>
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Measurements, 1: Exercise Volume
    
    // Measurement Option: "Weight", "Muscle Mass", "Fat %", "Chest", "Arms", "Waist"
    var selectedMetric by remember { mutableStateOf("Weight") }
    
    // Exercise Volume Options: "Total", "Chest", "Back", "Legs", "Shoulders", "Abs"
    var selectedVolumeCategory by remember { mutableStateOf("Total") }

    // Selected node index for interactive crosshair/tooltip
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    
    // Clear tooltip when tab or option changes
    LaunchedEffect(selectedTab, selectedMetric, selectedVolumeCategory) {
        selectedPointIndex = null
    }

    // Prepare chart points
    val chartPoints = remember(measurements, workoutLogs, selectedTab, selectedMetric, selectedVolumeCategory) {
        if (selectedTab == 0) {
            // Measurements
            val sorted = measurements.sortedBy { it.date }
            if (sorted.isEmpty()) {
                // Return beautiful mock/tutorial progress points so it's not empty!
                listOf(
                    ChartDataPoint("05-15", "2026-05-15", 72f),
                    ChartDataPoint("05-22", "2026-05-22", 73.5f),
                    ChartDataPoint("05-29", "2026-05-29", 74.2f),
                    ChartDataPoint("06-05", "2026-06-05", 75.1f),
                    ChartDataPoint("06-12", "2026-06-12", 76f)
                ).map { pt ->
                    val rawVal = when (selectedMetric) {
                        "Fat %" -> pt.value * 0.2f // ~ 14.5%
                        "Muscle Mass" -> pt.value * 0.45f
                        "Chest" -> 90f + (pt.value - 72f) * 1.2f
                        "Arms" -> 30f + (pt.value - 72f) * 0.4f
                        "Waist" -> 82f - (pt.value - 72f) * 0.5f
                        else -> pt.value
                    }
                    pt.copy(value = rawVal)
                }
            } else {
                sorted.map { m ->
                    val value = when (selectedMetric) {
                        "Weight" -> m.weight
                        "Muscle Mass" -> m.muscleMass
                        "Fat %" -> m.fatPercentage
                        "Chest" -> m.chest
                        "Arms" -> m.arms
                        "Waist" -> m.waist
                        else -> m.weight
                    }
                    val shortDate = try {
                        val parts = m.date.split("-")
                        if (parts.size >= 3) "${parts[1]}-${parts[2]}" else m.date
                    } catch (e: Exception) { m.date }
                    ChartDataPoint(shortDate, m.date, value)
                }
            }
        } else {
            // Exercise Volume
            // Group and aggregate workoutLogs by date
            // Volume = weight * sets * reps
            val filteredLogs = if (selectedVolumeCategory == "Total") {
                workoutLogs
            } else {
                workoutLogs.filter { it.category.equals(selectedVolumeCategory, ignoreCase = true) }
            }
            
            val aggregated = filteredLogs.groupBy { it.date }
                .mapValues { entry ->
                    entry.value.sumOf { (it.weight * it.sets * it.reps).toDouble() }.toFloat()
                }
            val sortedDates = aggregated.keys.sorted()
            
            if (sortedDates.isEmpty()) {
                // Return beautiful workout progression reference spectrum
                listOf(
                    ChartDataPoint("Week 1", "2026-05-15", 3200f),
                    ChartDataPoint("Week 2", "2026-05-22", 4500f),
                    ChartDataPoint("Week 3", "2026-05-29", 5100f),
                    ChartDataPoint("Week 4", "2026-06-05", 6200f),
                    ChartDataPoint("Week 5", "2026-06-12", 7800f)
                ).map { pt ->
                    val factor = when (selectedVolumeCategory) {
                        "Chest" -> 0.35f
                        "Back" -> 0.3f
                        "Legs" -> 0.25f
                        "Shoulders" -> 0.1f
                        else -> 1f
                    }
                    pt.copy(value = pt.value * factor)
                }
            } else {
                sortedDates.map { d ->
                    val value = aggregated[d] ?: 0f
                    val shortDate = try {
                        val parts = d.split("-")
                        if (parts.size >= 3) "${parts[1]}-${parts[2]}" else d
                    } catch (e: Exception) { d }
                    ChartDataPoint(shortDate, d, value)
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .border(1.dp, SoloBlueAccent.copy(0.3f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SoloCardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- Chart Tab Selection Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoloDarkGrey, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Measurement Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (selectedTab == 0) SoloBlueAccent else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "BODY MEASUREMENTS",
                        color = if (selectedTab == 0) SoloBlack else SoloTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                // Volume Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (selectedTab == 1) SoloPurpleAccent else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "TRAINING VOLUME",
                        color = if (selectedTab == 1) Color.White else SoloTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Metric Sub-Filters ---
            if (selectedTab == 0) {
                // Body Measurement Metric Selection Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val metrics = listOf("Weight", "Muscle Mass", "Fat %", "Chest", "Arms", "Waist")
                    metrics.forEach { metric ->
                        val isSel = selectedMetric == metric
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSel) SoloBlueAccent.copy(0.12f) else Color.Transparent,
                                    RoundedCornerShape(20.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSel) SoloBlueAccent else SoloTextSecondary.copy(0.2f),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedMetric = metric }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = metric,
                                color = if (isSel) SoloBlueAccent else SoloTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // Exercise Volume Sub-Filters Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val categories = listOf("Total", "Chest", "Back", "Legs", "Shoulders", "Abs")
                    categories.forEach { cat ->
                        val isSel = selectedVolumeCategory == cat
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSel) SoloPurpleAccent.copy(0.12f) else Color.Transparent,
                                    RoundedCornerShape(20.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSel) SoloPurpleAccent else SoloTextSecondary.copy(0.2f),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedVolumeCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSel) SoloPurpleAccent else SoloTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- The Canvas Chart ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(SoloBlack.copy(0.6f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                if (chartPoints.isNotEmpty()) {
                    val maxVal = chartPoints.maxOf { it.value }
                    val minVal = chartPoints.minOf { it.value }
                    val valueRange = (maxVal - minVal).coerceAtLeast(0.1f)
                    val yMin = (minVal - valueRange * 0.15f).coerceAtLeast(0f)
                    val yMax = maxVal + valueRange * 0.15f
                    val fullYRange = yMax - yMin

                    val mainColor = if (selectedTab == 0) SoloBlueAccent else SoloPurpleAccent
                    val glowColor = if (selectedTab == 0) SoloGlowBlue else SoloGlowPurple

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(chartPoints) {
                                detectTapGestures { offset ->
                                    val count = chartPoints.size
                                    if (count > 0) {
                                        val cWidth = size.width
                                        val cellW = if (count > 1) cWidth / (count - 1) else cWidth
                                        var bestIdx = 0
                                        var bestDist = Float.MAX_VALUE
                                        for (i in 0 until count) {
                                            val xPos = i * cellW
                                            val dist = kotlin.math.abs(offset.x - xPos)
                                            if (dist < bestDist) {
                                                bestDist = dist
                                                bestIdx = i
                                            }
                                        }
                                        selectedPointIndex = if (bestDist < cellW * 0.75f) bestIdx else null
                                    }
                                }
                            }
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val count = chartPoints.size
                        
                        // 1. Draw Grid Lines & Labels
                        val gridCount = 4
                        for (i in 0..gridCount) {
                            val yCoord = canvasHeight * i / gridCount
                            drawLine(
                                color = Color.White.copy(0.06f),
                                start = Offset(0f, yCoord),
                                end = Offset(canvasWidth, yCoord),
                                strokeWidth = 1.dp.toPx()
                            )
                            
                            val labelVal = yMax - (fullYRange * i / gridCount)
                            drawIntoCanvas { canvas ->
                                val paint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.parseColor("#475569") // slate-600
                                    textSize = 8.dp.toPx()
                                    typeface = android.graphics.Typeface.MONOSPACE
                                    textAlign = android.graphics.Paint.Align.LEFT
                                }
                                canvas.nativeCanvas.drawText(
                                    String.format("%.1f", labelVal),
                                    4.dp.toPx(),
                                    yCoord - 4.dp.toPx(),
                                    paint
                                )
                            }
                        }

                        // Parse coordinates
                        val cellW = if (count > 1) canvasWidth / (count - 1) else canvasWidth
                        val coordinates = chartPoints.mapIndexed { index, pt ->
                            val x = index * cellW
                            val ratio = (pt.value - yMin) / fullYRange
                            val y = canvasHeight - (canvasHeight * ratio)
                            Offset(x, y)
                        }

                        // 2. Draw Fill Area Gradient (Only if elements >= 2)
                        if (coordinates.size >= 2) {
                            val fillPath = Path().apply {
                                moveTo(coordinates.first().x, canvasHeight)
                                coordinates.forEach { lineTo(it.x, it.y) }
                                lineTo(coordinates.last().x, canvasHeight)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        mainColor.copy(alpha = 0.25f),
                                        Color.Transparent
                                    ),
                                    startY = 0f,
                                    endY = canvasHeight
                                )
                            )
                        }

                        // 3. Draw Main Line Path (Smooth cubic bezier curve)
                        if (coordinates.size >= 2) {
                            val linePath = Path().apply {
                                moveTo(coordinates.first().x, coordinates.first().y)
                                for (i in 1 until coordinates.size) {
                                    val current = coordinates[i]
                                    val previous = coordinates[i - 1]
                                    val controlX = (previous.x + current.x) / 2
                                    cubicTo(
                                        controlX, previous.y,
                                        controlX, current.y,
                                        current.x, current.y
                                    )
                                }
                            }
                            // Glow Shadow
                            drawPath(
                                path = linePath,
                                color = glowColor,
                                style = Stroke(width = 6.dp.toPx())
                            )
                            // Main Stroke
                            drawPath(
                                path = linePath,
                                color = mainColor,
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                        }

                        // 4. Draw node points
                        coordinates.forEachIndexed { i, offset ->
                            val isChosen = selectedPointIndex == i
                            val ptRadius = if (isChosen) 6.dp.toPx() else 4.dp.toPx()
                            
                            // Outer circle neon glow
                            drawCircle(
                                color = if (isChosen) mainColor else mainColor.copy(0.4f),
                                radius = ptRadius + 3.dp.toPx(),
                                center = offset
                            )
                            // Inner core white/base circles
                            drawCircle(
                                color = if (isChosen) Color.White else SoloDarkGrey,
                                radius = ptRadius,
                                center = offset
                            )
                        }

                        // 5. Crosshair Line & Target highlights
                        selectedPointIndex?.let { idx ->
                            if (idx in coordinates.indices) {
                                val offset = coordinates[idx]
                                // Vertical Line
                                drawLine(
                                    color = mainColor.copy(alpha = 0.5f),
                                    start = Offset(offset.x, 0f),
                                    end = Offset(offset.x, canvasHeight),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                        }
                    }
                }
            }

            // --- Footer / Selected Point Tooltip detail ---
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoloDarkGrey.copy(0.8f), RoundedCornerShape(10.dp))
                    .border(1.dp, Color.White.copy(0.04f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                if (selectedPointIndex != null && selectedPointIndex!! < chartPoints.size) {
                    val pt = chartPoints[selectedPointIndex!!]
                    val unit = if (selectedTab == 0) {
                        when (selectedMetric) {
                            "Weight", "Muscle Mass" -> "kg"
                            "Fat %" -> "%"
                            else -> "cm"
                        }
                    } else "KCAL Volume"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SYSTEM SPECTRUM DATA",
                                color = if (selectedTab == 0) SoloBlueAccent else SoloPurpleAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Calendar: ${pt.dateStr}",
                                color = SoloTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${pt.value} $unit",
                            color = if (selectedTab == 0) SoloBlueAccent else SoloPurpleAccent,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SoloTextSecondary.copy(0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tap on any grid point circle to inspect recorded telemetry",
                            color = SoloTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }
    }
}

// ------ 11b. FULL SCREEN DAILY QUEST ALERT OVERLAY ------
@Composable
fun FullScreenDailyQuestAlert(viewModel: SystemViewModel) {
    val dailyQuestsList by viewModel.dailyQuests.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val username = profile?.name ?: "Hunter"

    // Pulsing blue-violet border animation
    val infiniteTransition = rememberInfiniteTransition(label = "quest_glow")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "quest_border_glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712).copy(alpha = 0.95f)) // Dark slate obsidian
            .padding(24.dp)
            .clickable(enabled = false) {}, // intercept touch
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(3.dp, SoloBlueAccent.copy(alpha = alphaAnim), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Special Quest Icon
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                Brush.radialGradient(listOf(SoloBlueAccent.copy(0.3f), Color.Transparent)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Quest Alert Icon",
                            tint = SoloBlueAccent,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "[ DAILY QUEST RECEIVED ]",
                        color = SoloBlueAccent,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "ATTENTION ALL HUNTERS",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 3.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Hello, $username. A compulsory training protocol has been assigned to your dynamic matrix by the System Registry. Complete these objectives today to unlock stat points, reward gold, and level progression.",
                        color = SoloTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // The list of quests inside a highly styled card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SoloBlueAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = SoloDarkGrey),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "DAILY OBJECTIVES LIST:",
                                color = SoloBlueAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            if (dailyQuestsList.isEmpty()) {
                                Text(
                                    text = "System is compiling today's covenants...",
                                    color = SoloTextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            } else {
                                dailyQuestsList.forEach { quest ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (quest.isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = if (quest.isCompleted) SoloBlueAccent else SoloTextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = quest.name.uppercase(),
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Text(
                                            text = "${quest.currentCount} / ${quest.targetCount}",
                                            color = if (quest.isCompleted) SoloBlueAccent else SoloPurpleAccent,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoloPurpleAccent.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .border(1.dp, SoloPurpleAccent.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚠️ WARNING: FAILURE TO COMPLETE DAILY TRAINING WILL LEAD TO MAXIMUM PENALTY PROTOCOLS.",
                            color = SoloPurpleAccent,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Accept Quest Action button
                    Button(
                        onClick = {
                            viewModel.dismissFullScreenQuestAlert()
                            com.example.util.WorkoutSoundAlert.playSystemAchievementSound()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoloBlueAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .border(1.dp, Color.White.copy(0.3f), RoundedCornerShape(12.dp))
                            .testTag("accept_quest_fullscreen_button")
                    ) {
                        Text(
                            text = "ACCEPT DECREE / ENTER SYSTEM",
                            color = SoloBlack,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// ------ 11c. CHROME & SYSTEM INSPIRED HEVY SHARE DIALOG OVERLAY ------
@Composable
fun HevyWorkoutAchievementDialog(viewModel: SystemViewModel) {
    val context = LocalContext.current
    val liveLogs by viewModel.workoutLogs.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val username = userProfile?.name ?: "Hunter"

    // Deduce today's date formatted as YYYY-MM-DD
    val today = remember {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = String.format("%02d", calendar.get(java.util.Calendar.MONTH) + 1)
        val day = String.format("%02d", calendar.get(java.util.Calendar.DAY_OF_MONTH))
        "$year-$month-$day"
    }

    val todayLogs = remember(liveLogs) {
        liveLogs.filter { it.date == today }
    }

    val totalVolume = remember(todayLogs) {
        todayLogs.sumOf { (it.sets * it.reps * (it.weight + it.barWeight)).toDouble() }.toFloat()
    }

    val maxWeight = remember(todayLogs) {
        todayLogs.maxOfOrNull { it.weight + it.barWeight } ?: 0f
    }

    val totalSets = remember(todayLogs) {
        todayLogs.sumOf { it.sets }
    }

    Dialog(
        onDismissRequest = { viewModel.dismissWorkoutCompletion() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SoloDarkGrey.copy(alpha = 0.98f))
                .border(2.dp, SoloBlueAccent, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Epic visual badge
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(SoloBlueAccent.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, SoloBlueAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Achievement Icon",
                        tint = SoloGold,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "WORKOUT RATIFIED",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = SoloBlueAccent,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "HEVY PROTOCOL ON • PROGRESS SAVED",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = SoloGold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Grid stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(SoloBlack, RoundedCornerShape(8.dp))
                            .border(1.dp, BentoBorderSlate, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("TOTAL VOLUME", fontSize = 8.sp, color = SoloTextSecondary, fontFamily = FontFamily.Monospace)
                        Text("${totalVolume.toInt()} kg", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(SoloBlack, RoundedCornerShape(8.dp))
                            .border(1.dp, BentoBorderSlate, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("MAX SINGLE", fontSize = 8.sp, color = SoloTextSecondary, fontFamily = FontFamily.Monospace)
                        Text("${maxWeight.toInt()} kg", fontSize = 14.sp, color = SoloPurpleAccent, fontWeight = FontWeight.Bold)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(SoloBlack, RoundedCornerShape(8.dp))
                            .border(1.dp, BentoBorderSlate, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("TOTAL SETS", fontSize = 8.sp, color = SoloTextSecondary, fontFamily = FontFamily.Monospace)
                        Text("$totalSets sets", fontSize = 14.sp, color = SoloSuccess, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = BentoBorderSlate)

                Text(
                    text = "LOGGED EXERCISE PROTOCOLS:",
                    fontSize = 10.sp,
                    color = SoloTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.Start)
                )

                // Scroll box of today's completed efforts
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (todayLogs.isEmpty()) {
                            Text(
                                "No active logs recorded today yet.",
                                color = SoloTextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            todayLogs.forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SoloBlack.copy(0.4f), RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(log.exerciseName.uppercase(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("[Int: ${log.intensity.uppercase()}] [Bar: ${log.barWeight.toInt()}kg]", color = SoloTextSecondary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Text(
                                        text = "${log.sets} sets x ${log.reps} reps",
                                        color = SoloBlueAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Download Hevy Card Trigger Button
                Button(
                    onClick = {
                        viewModel.downloadHevyWorkoutCard(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoloBlueAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("download_hevy_card_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share Icon", tint = SoloBlack, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DOWNLOAD RATIFIED SHARE CARD", color = SoloBlack, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }

                // Continue Close
                TextButton(
                    onClick = { viewModel.dismissWorkoutCompletion() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CONTINUE TRAINING SCREEN", color = SoloTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

data class CyberSpark(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    var vAddY: Float,
    val size: Float,
    val color: Color,
    val life: Float,
    var age: Float
)

@Composable
fun SoloLevelUpAnimationOverlay(level: Int, onDismiss: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "cyber_pulser_system")
    val random = remember { java.util.Random() }

    // Crackle speed factor
    val lightningTrigger by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lightning_trigger"
    )

    val scaleScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "levelup_main_scale"
    )

    // Flicker factor for lightning intensity
    val lightningIntensity by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lightning_intensity"
    )

    // Lightning seed increments: Float animated and cast to Int to assure absolute system compatibility
    val lightningTickFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lightning_tick"
    )
    val lightningTick = lightningTickFloat.toInt()

    // Grid scanner overlay position
    val scanlineY by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanline_y"
    )

    // Sparks particle effects list
    val sparks = remember { mutableStateListOf<CyberSpark>() }

    // High performance gravity-spark runner
    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // ~60 FPS
            val toRemove = mutableListOf<CyberSpark>()
            sparks.forEach { s ->
                s.x += s.vx
                s.y += s.vy
                s.vAddY += 0.08f // Gravity drift
                s.y += s.vAddY
                s.age += 16f
                if (s.age >= s.life) {
                    toRemove.add(s)
                }
            }
            sparks.removeAll(toRemove)

            // Auto-emit fresh yellow & blue electrical sparks if the screen is active
            if (sparks.size < 50) {
                repeat(4) {
                    val angle = random.nextFloat() * 2f * Math.PI.toFloat()
                    val speed = random.nextFloat() * 5f + 1.5f
                    val vx = (Math.cos(angle.toDouble()).toFloat() * speed)
                    val vy = (Math.sin(angle.toDouble()).toFloat() * speed) - 2f
                    sparks.add(
                        CyberSpark(
                            x = 0f, // Center relative offset
                            y = 0f,
                            vx = vx,
                            vy = vy,
                            vAddY = 0f,
                            size = random.nextFloat() * 10f + 4f,
                            color = if (random.nextBoolean()) Color(0xFFFCEE09) else Color(0xFF00F0FF),
                            life = 600f + random.nextFloat() * 700f,
                            age = 0f
                        )
                    )
                }
            }
        }
    }

    val colorCyberYellow = Color(0xFFFCEE09)
    val colorCyberDark = Color(0xFF0B0D14)
    val colorCyberBlue = Color(0xFF00F0FF)
    val colorCyberRed = Color(0xFFFF003C)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF205070D)) // Semi-matte deep tactical space frame
            .padding(16.dp)
            .clickable(enabled = false) {}, // Intercept touch events underneath
        contentAlignment = Alignment.Center
    ) {
        // 1. DYNAMIC CYBER LIGHTNING CANVAS (Crackles around behind the dialogue box)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val drawScope = this
            fun drawLightning(start: Offset, end: Offset, segments: Int, deviation: Float, seed: Long, baseColor: Color) {
                val path = Path()
                path.moveTo(start.x, start.y)
                val dx = end.x - start.x
                val dy = end.y - start.y
                val length = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                if (length < 4f) return
                
                val rand = java.util.Random(seed)
                
                for (i in 1 until segments) {
                    val fraction = i.toFloat() / segments
                    val baseX = start.x + dx * fraction
                    val baseY = start.y + dy * fraction
                    
                    val perpX = -dy / length
                    val perpY = dx / length
                    
                    val offset = (rand.nextFloat() * 2f - 1f) * deviation
                    val px = baseX + perpX * offset
                    val py = baseY + perpY * offset
                    
                    path.lineTo(px, py)
                }
                path.lineTo(end.x, end.y)
                
                // Outer glow
                drawScope.drawPath(
                    path = path,
                    color = baseColor.copy(alpha = 0.25f),
                    style = Stroke(width = 16f)
                )
                // Middle spark
                drawScope.drawPath(
                    path = path,
                    color = baseColor.copy(alpha = 0.6f),
                    style = Stroke(width = 6f)
                )
                // Core bright fire
                drawScope.drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(width = 2f)
                )
            }

            // Determine randomized points for multiple bolts based on tick
            val statePulse = lightningTick
            val b1Seed = (statePulse / 4 * 17L) + 10243L
            val b2Seed = (statePulse / 5 * 29L) + 20456L
            val b3Seed = (statePulse / 3 * 37L) + 30560L

            // Bolt 1: Top-Left Corner to Center
            if (lightningIntensity > 0.35f) {
                drawLightning(
                    start = Offset(0f, 0f),
                    end = Offset(size.width * 0.4f, size.height * 0.45f),
                    segments = 12,
                    deviation = 55f,
                    seed = b1Seed,
                    baseColor = colorCyberYellow
                )
            }

            // Bolt 2: Top-Right Corner to Center
            if (lightningIntensity > 0.45f) {
                drawLightning(
                    start = Offset(size.width, 0f),
                    end = Offset(size.width * 0.6f, size.height * 0.42f),
                    segments = 11,
                    deviation = 48f,
                    seed = b2Seed,
                    baseColor = colorCyberYellow
                )
            }

            // Bolt 3: Bottom-Right Corner upwards
            if (lightningIntensity > 0.6f) {
                drawLightning(
                    start = Offset(size.width, size.height),
                    end = Offset(size.width * 0.55f, size.height * 0.55f),
                    segments = 13,
                    deviation = 50f,
                    seed = b3Seed,
                    baseColor = colorCyberBlue
                )
            }

            // Draw scanning lines grid representation
            val scanLineSpacing = 24.dp.toPx()
            var currentY = 0f
            while (currentY < size.height) {
                drawLine(
                    color = Color(0xFFFCEE09).copy(alpha = 0.02f),
                    start = Offset(0f, currentY),
                    end = Offset(size.width, currentY),
                    strokeWidth = 1f
                )
                currentY += scanLineSpacing
            }

            // Draw bright dynamic grid laser scan tracking line
            drawLine(
                color = colorCyberYellow.copy(alpha = 0.15f),
                start = Offset(0f, scanlineY),
                end = Offset(size.width, scanlineY),
                strokeWidth = 2.dp.toPx()
            )
        }

        // 2. MAIN CELEBRATION COMPONENT BLOCK
        Column(
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scaleScale,
                    scaleY = scaleScale
                )
                .fillMaxWidth(0.95f)
                .border(2.5.dp, colorCyberYellow, RoundedCornerShape(2.dp)) // Cyberpunk high-visibility rigid border
                .background(colorCyberDark, RoundedCornerShape(2.dp))
                .drawBehind {
                    // Draw military yellow-and-black diagonal hazard warning strips at the top and bottom borders
                    val stripeHeight = 12.dp.toPx()
                    val step = 18.dp.toPx()
                    
                    // Top caution stripe background
                    drawRect(colorCyberYellow, topLeft = Offset(0f, 0f), size = size.copy(height = 12.dp.toPx()))
                    // Draw black diagonal line stripes
                    var sx = -stripeHeight
                    while (sx < size.width) {
                        drawLine(
                            color = Color.Black,
                            start = Offset(sx, 0f),
                            end = Offset(sx + stripeHeight, stripeHeight),
                            strokeWidth = 6.dp.toPx()
                        )
                        sx += step
                    }
                    
                    // Bottom caution stripe background
                    val bottomY = size.height - 12.dp.toPx()
                    drawRect(colorCyberYellow, topLeft = Offset(0f, bottomY), size = size.copy(height = 12.dp.toPx()))
                    // Draw black diagonal line stripes
                    var bx = -stripeHeight
                    while (bx < size.width) {
                        drawLine(
                            color = Color.Black,
                            start = Offset(bx, bottomY),
                            end = Offset(bx + stripeHeight, size.height),
                            strokeWidth = 6.dp.toPx()
                        )
                        bx += step
                    }
                }
                .padding(vertical = 26.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HUD Top-Left and Top-Right custom label
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "⚙️ [SYSTEM: OVERRIDE]",
                    color = colorCyberYellow,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "[SEED_VER_9811] ⚡",
                    color = colorCyberYellow,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // TECHNICAL STATUS TITLE
            Text(
                text = "⚡ SYSTEM LEVEL UP DETECTED ⚡",
                color = colorCyberYellow,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // DYNAMIC HUD CIRCLE & BOLT EMBLEM WITH EXPANSIVE SPARKS CANVAS overlay
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .border(2.dp, colorCyberYellow, CircleShape)
                    .background(colorCyberYellow.copy(alpha = 0.08f), CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                // Internal crosshair canvas rendering
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(colorCyberYellow.copy(0.2f), radius = size.width / 2.3f, style = Stroke(width = 1.dp.toPx()))
                    
                    // Corner targeting brackets
                    val lineLen = 14f
                    val pad = 4f
                    // Top-Left bracket
                    drawLine(colorCyberBlue, start = Offset(pad, pad), end = Offset(pad + lineLen, pad), strokeWidth = 3f)
                    drawLine(colorCyberBlue, start = Offset(pad, pad), end = Offset(pad, pad + lineLen), strokeWidth = 3f)
                    
                    // Top-Right bracket
                    drawLine(colorCyberBlue, start = Offset(size.width - pad, pad), end = Offset(size.width - pad - lineLen, pad), strokeWidth = 3f)
                    drawLine(colorCyberBlue, start = Offset(size.width - pad, pad), end = Offset(size.width - pad, pad + lineLen), strokeWidth = 3f)
                    
                    // Bottom-Left bracket
                    drawLine(colorCyberBlue, start = Offset(pad, size.height - pad), end = Offset(pad + lineLen, size.height - pad), strokeWidth = 3f)
                    drawLine(colorCyberBlue, start = Offset(pad, size.height - pad), end = Offset(pad, size.height - pad - lineLen), strokeWidth = 3f)
                    
                    // Bottom-Right bracket
                    drawLine(colorCyberBlue, start = Offset(size.width - pad, size.height - pad), end = Offset(size.width - pad - lineLen, size.height - pad), strokeWidth = 3f)
                    drawLine(colorCyberBlue, start = Offset(size.width - pad, size.height - pad), end = Offset(size.width - pad, size.height - pad - lineLen), strokeWidth = 3f)
                }

                // Render active sparks originating from the central circle
                Canvas(modifier = Modifier.fillMaxSize()) {
                    sparks.forEach { s ->
                        drawCircle(
                            color = s.color.copy(alpha = ((s.life - s.age) / s.life).coerceIn(0.01f, 1.0f)),
                            radius = s.size,
                            center = Offset(size.width / 2f + s.x, size.height / 2f + s.y)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = colorCyberYellow,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // THE CHROMATIC GLITCH DOUBLE LEVEL UP TEXT INDICATOR
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                // Red Shift Shadow
                Text(
                    text = "LEVEL UP !",
                    color = colorCyberRed.copy(alpha = 0.8f),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 6.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(x = (-3).dp, y = 2.dp)
                )
                // Cyan Shift Shadow
                Text(
                    text = "LEVEL UP !",
                    color = colorCyberBlue.copy(alpha = 0.8f),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 6.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(x = 3.dp, y = (-2).dp)
                )
                // Primary glowing Yellow text with occasional tick jitter
                val jitterX = if (lightningTick % 6 == 0) (random.nextFloat() * 4f - 2f).dp else 0.dp
                val jitterY = if (lightningTick % 7 == 0) (random.nextFloat() * 4f - 2f).dp else 0.dp
                Text(
                    text = "LEVEL UP !",
                    color = colorCyberYellow,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 6.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(x = jitterX, y = jitterY)
                )
            }

            Text(
                text = "HUNTER CLASS RANK ASCENDED",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            // Split Grid display showing Level evolution
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // OLD LEVEL BLOCK
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PREVIOUS RANK",
                        color = Color.White.copy(0.6f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .background(Color(0xFF131722), RoundedCornerShape(2.dp))
                            .border(1.dp, Color.White.copy(0.2f), RoundedCornerShape(2.dp))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LVL ${level - 1}",
                            color = Color.White.copy(0.75f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // DIRECTIONAL VECTOR
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Ascending Arrow",
                    tint = colorCyberYellow,
                    modifier = Modifier.size(24.dp).padding(horizontal = 2.dp)
                )

                // NEW ASCENDED LEVEL HIGH-CONTRAST SOLID BADGE
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = "NEW ASCENT",
                        color = colorCyberYellow,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(colorCyberYellow, RoundedCornerShape(2.dp))
                            .border(1.5.dp, colorCyberYellow, RoundedCornerShape(2.dp))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LVL $level",
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CELEBRATION PERK STAT CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colorCyberYellow.copy(0.35f), RoundedCornerShape(1.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F121C)),
                shape = RoundedCornerShape(1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(12.dp), tint = colorCyberYellow)
                        Text(
                            text = "SYSTEM CONFIGURATION MODIFICATION:",
                            color = colorCyberYellow,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "+5 STATUS POINTS ALLOCATED TO COGNITIVE BASE",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Manual calibration parameter blocks have been unlocked. Re-distribute stat points on the systems core dashboard at any time.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 8.5.sp,
                        lineHeight = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // PRIMARY CONFIRM ACTION BUTTON (Cyberpunk high-contrast flat brutalist styled CTA)
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = colorCyberYellow),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_growth_btn"),
                shape = RoundedCornerShape(1.dp), // Hard square brutalist cut
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "CONFIRM SYSTEM INTEGRATION",
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun AttributesRadarChart(
    chest: Float,
    back: Float,
    abs: Float,
    arms: Float,
    shoulders: Float,
    legs: Float,
    modifier: Modifier = Modifier
) {
    val labels = listOf("Chest", "Back", "Abs", "Arms", "Shoulders", "Legs")
    val values = listOf(chest, back, abs, arms, shoulders, legs)
    val maxVal = 100f

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.width.coerceAtMost(size.height) / 2.6f

        // 1. Draw web grid lines (concentric hexagons)
        val levels = 4
        for (i in 1..levels) {
            val ratio = i.toFloat() / levels
            val levelRadius = radius * ratio
            val path = Path()
            for (j in 0 until 6) {
                val angle = (j * 60 - 90) * (Math.PI / 180f)
                val x = center.x + (levelRadius * Math.cos(angle)).toFloat()
                val y = center.y + (levelRadius * Math.sin(angle)).toFloat()
                if (j == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            path.close()
            drawPath(
                path = path,
                color = SoloBlueAccent.copy(alpha = 0.2f),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // 2. Draw spokes
        for (j in 0 until 6) {
            val angle = (j * 60 - 90) * (Math.PI / 180f)
            val x = center.x + (radius * Math.cos(angle)).toFloat()
            val y = center.y + (radius * Math.sin(angle)).toFloat()
            drawLine(
                color = SoloBlueAccent.copy(alpha = 0.2f),
                start = center,
                end = Offset(x, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // 3. Draw values polygon
        val valuePath = Path()
        for (j in 0 until 6) {
            val valRatio = (values[j] / maxVal).coerceIn(0.12f, 1.0f)
            val angle = (j * 60 - 90) * (Math.PI / 180f)
            val x = center.x + (radius * valRatio * Math.cos(angle)).toFloat()
            val y = center.y + (radius * valRatio * Math.sin(angle)).toFloat()
            if (j == 0) {
                valuePath.moveTo(x, y)
            } else {
                valuePath.lineTo(x, y)
            }
        }
        valuePath.close()

        // Draw translucent fill
        drawPath(
            path = valuePath,
            color = SoloPurpleAccent.copy(alpha = 0.35f)
        )
        // Draw thick outline
        drawPath(
            path = valuePath,
            color = SoloPurpleAccent,
            style = Stroke(width = 2.dp.toPx())
        )

        // 4. Draw labels manually
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 9.dp.toPx()
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.MONOSPACE
        }

        for (j in 0 until 6) {
            val angle = (j * 60 - 90) * (Math.PI / 180f)
            // Push text slightly further than radius
            val tx = center.x + ((radius + 18.dp.toPx()) * Math.cos(angle)).toFloat()
            val ty = center.y + ((radius + 12.dp.toPx()) * Math.sin(angle)).toFloat() + 3.dp.toPx()

            drawContext.canvas.nativeCanvas.drawText(
                "${labels[j]} (${values[j].toInt()})",
                tx,
                ty,
                paint
            )
        }
    }
}

@Composable
fun ProfileScreen(viewModel: SystemViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val measurementsState by viewModel.bodyMeasurements.collectAsStateWithLifecycle()
    val lastDayWeight by viewModel.lastDayTotalWeight.collectAsStateWithLifecycle()
    var selectedProfileTab by remember { mutableStateOf(0) }

    val profile = profileState ?: return
    val latestMeasurement = measurementsState.firstOrNull()

    // Interactive sliders local state
    var editBench by remember { mutableStateOf(if (profile.benchPress > 0) profile.benchPress else 50f) }
    var editSquat by remember { mutableStateOf(if (profile.squat > 0) profile.squat else 60f) }
    var editDeadlift by remember { mutableStateOf(if (profile.deadlift > 0) profile.deadlift else 70f) }
    var editOhp by remember { mutableStateOf(if (profile.overheadPress > 0) profile.overheadPress else 30f) }

    var editChestDim by remember { mutableStateOf(latestMeasurement?.chest ?: 90f) }
    var editArmsDim by remember { mutableStateOf(latestMeasurement?.arms ?: 32f) }
    var editWaistDim by remember { mutableStateOf(latestMeasurement?.waist ?: 80f) }

    // Computations mirroring the radar calculation
    val chestValue = editChestDim.coerceIn(30f, 125f)
    val armsValue = (editArmsDim * 2f).coerceIn(30f, 125f)
    val waistValue = editWaistDim

    val chestPercent = ((chestValue - 30f) / 95f * 100f).coerceIn(20f, 100f)
    val backPercent = ((editDeadlift / 2.2f) + profile.strength * 2.5f + 20f).coerceIn(20f, 100f)
    val absPercent = (profile.agility * 3f + (115f - waistValue) * 0.5f).coerceIn(20f, 100f)
    val armsPercent = ((armsValue - 20f) / 105f * 100f).coerceIn(20f, 100f)
    val shouldersPercent = (editOhp * 1.6f + profile.strength * 2.2f + 20f).coerceIn(20f, 100f)
    val legsPercent = (editSquat * 0.9f + profile.vitality * 2.8f + 15f).coerceIn(20f, 100f)

    var showSimReferral by remember { mutableStateOf(false) }
    var referrerFriendName by remember { mutableStateOf("") }
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = androidx.compose.ui.platform.LocalContext.current

    var selectedHour by remember { mutableStateOf(8) }
    var selectedMinute by remember { mutableStateOf(0) }
    val customAppTrayName by viewModel.customAppTrayName.collectAsStateWithLifecycle()
    var inputAppName by remember { mutableStateOf(customAppTrayName) }

    LaunchedEffect(profile.soundAlertTime) {
        try {
            val parts = profile.soundAlertTime.split(":")
            if (parts.size >= 2) {
                selectedHour = parts[0].trim().toInt().coerceIn(0, 23)
                val minPart = parts[1].replace("AM", "").replace("PM", "").trim().replace(" ", "").toInt()
                selectedMinute = minPart.coerceIn(0, 59)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(customAppTrayName) {
        inputAppName = customAppTrayName
    }

    val inviteLink = "https://ais-pre-43wag6ovjetiteunx7w5v6-981190146193.asia-southeast1.run.app/invite?referrer=${profile.name}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // HEADER ROW WITH BACK NAVIGATION
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.currentScreen.value = "DASHBOARD" },
                modifier = Modifier.background(SoloDarkGrey, CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SoloBlueAccent)
            }
            Text(
                text = "HUNTER LEVEL EVALUATION",
                color = SoloBlueAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Box(modifier = Modifier.size(40.dp)) // empty spacer placeholder to balance horizontal elements
        }

        // STATS SUMMARY CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .border(1.dp, BentoBorderSky.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = SoloCardBg)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(SoloDarkGrey)
                        .border(2.dp, SoloBlueAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = SoloBlueAccent, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = profile.name.uppercase(), color = SoloTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text(text = "LEVEL: ${profile.level} | LAST DAY WEIGHT: ${String.format("%.1f", lastDayWeight)} kg", color = SoloPurpleAccent, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Profile Sub-Tabs selection bar
        val isMyProfileAdmin = profile.name.lowercase().contains("satyam") || 
                              profile.email.lowercase().contains("satyam") || 
                              profile.name.lowercase().contains("admin") || 
                              profile.email.lowercase().contains("admin") ||
                              profile.name.uppercase() == "SUNG JINWOO" ||
                              profile.email.isBlank()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .background(SoloDarkGrey, RoundedCornerShape(8.dp))
                .border(1.dp, BentoBorderSlate, RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selectedProfileTab == 0) SoloBlueAccent else Color.Transparent)
                    .clickable { selectedProfileTab = 0 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "HUNTER PROTOCOL",
                    color = if (selectedProfileTab == 0) SoloBlack else SoloTextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selectedProfileTab == 1) SoloBlueAccent else Color.Transparent)
                    .clickable { selectedProfileTab = 1 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SIREN & CONFIG",
                    color = if (selectedProfileTab == 1) SoloBlack else SoloTextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            if (isMyProfileAdmin) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selectedProfileTab == 2) SoloBlueAccent else Color.Transparent)
                        .clickable { selectedProfileTab = 2 }
                        .padding(vertical = 10.dp)
                        .testTag("admin_tab_selection_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ADMIN CODES",
                        color = if (selectedProfileTab == 2) SoloBlack else SoloTextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        if (selectedProfileTab == 0) {
            // WEB RADAR CHART CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(2.dp, SoloPurpleAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SoloBlack.copy(0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ACTIVE PHYSICAL CHARACTERISTIC RADAR",
                    color = SoloTextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                AttributesRadarChart(
                    chest = chestPercent,
                    back = backPercent,
                    abs = absPercent,
                    arms = armsPercent,
                    shoulders = shouldersPercent,
                    legs = legsPercent,
                    modifier = Modifier
                        .size(230.dp)
                        .padding(8.dp)
                        .testTag("attributes_radar_canvas")
                )

                Text(
                    text = "Chart responds dynamically to your workout Personal Bests and logged chest/arm ratios.",
                    color = SoloTextSecondary,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }

        // INTERACTIVE LIFTS / DIMENSIONS SLIDER CONTROLLER
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, BentoBorderSlate, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = SoloCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TWEAK LIFTS & MEASUREMENTS",
                    color = SoloBlueAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Lifts adjustments
                Text(text = "Bench Press PB: ${editBench.toInt()} kg", color = SoloTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = editBench,
                    onValueChange = { editBench = it },
                    valueRange = 20f..250f,
                    colors = SliderDefaults.colors(thumbColor = SoloBlueAccent, activeTrackColor = SoloBlueAccent)
                )

                Text(text = "Squat PB: ${editSquat.toInt()} kg", color = SoloTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = editSquat,
                    onValueChange = { editSquat = it },
                    valueRange = 20f..300f,
                    colors = SliderDefaults.colors(thumbColor = SoloBlueAccent, activeTrackColor = SoloBlueAccent)
                )

                Text(text = "Deadlift PB: ${editDeadlift.toInt()} kg", color = SoloTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = editDeadlift,
                    onValueChange = { editDeadlift = it },
                    valueRange = 20f..350f,
                    colors = SliderDefaults.colors(thumbColor = SoloBlueAccent, activeTrackColor = SoloBlueAccent)
                )

                Text(text = "Overhead Press PB: ${editOhp.toInt()} kg", color = SoloTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = editOhp,
                    onValueChange = { editOhp = it },
                    valueRange = 10f..150f,
                    colors = SliderDefaults.colors(thumbColor = SoloBlueAccent, activeTrackColor = SoloBlueAccent)
                )

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = BentoBorderSlate)
                Spacer(modifier = Modifier.height(10.dp))

                // Dimension measurements adjustments
                Text(text = "Chest Width Dimension: ${editChestDim.toInt()} cm", color = SoloTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = editChestDim,
                    onValueChange = { editChestDim = it },
                    valueRange = 50f..150f,
                    colors = SliderDefaults.colors(thumbColor = SoloPurpleAccent, activeTrackColor = SoloPurpleAccent)
                )

                Text(text = "Arms Bicep Size: ${editArmsDim.toInt()} cm", color = SoloTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = editArmsDim,
                    onValueChange = { editArmsDim = it },
                    valueRange = 15f..60f,
                    colors = SliderDefaults.colors(thumbColor = SoloPurpleAccent, activeTrackColor = SoloPurpleAccent)
                )

                Text(text = "Waist Middle Size: ${editWaistDim.toInt()} cm", color = SoloTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = editWaistDim,
                    onValueChange = { editWaistDim = it },
                    valueRange = 50f..130f,
                    colors = SliderDefaults.colors(thumbColor = SoloPurpleAccent, activeTrackColor = SoloPurpleAccent)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        // Persist lifts and save Body measurement to DB!
                        coroutineScope.launch {
                            val nextProfile = profile.copy(
                                benchPress = editBench,
                                squat = editSquat,
                                deadlift = editDeadlift,
                                overheadPress = editOhp
                            )
                            viewModel.insertOrUpdateProfile(nextProfile)
                            viewModel.logBodyMeasurements(
                                weight = latestMeasurement?.weight ?: 75f,
                                muscle = latestMeasurement?.muscleMass ?: 35f,
                                fat = latestMeasurement?.fatPercentage ?: 15f,
                                chest = editChestDim,
                                arms = editArmsDim,
                                waist = editWaistDim
                            )
                            viewModel.notifyMsg("[SYSTEM REFRESH] Lifts and raw measurements updated perfectly!", "achievement")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoloBlueAccent),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("SAVE LIFTS & MEASUREMENTS", color = SoloBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // REFERRAL / COGNITIVE SHARE SHEET INTERFACE
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .border(1.dp, SoloBlueAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = SoloCardBg.copy(alpha = 0.8f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "GROWTH INVITE RECRUITMENT",
                    color = SoloBlueAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = "Invite a fellow Hunter to awaken. You both get +100 EXP immediately once they initiate and log inside the system!",
                    color = SoloTextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(inviteLink))
                            viewModel.notifyMsg("Viral Referral Link Copied to clipboard!", "achievement")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoloDarkGrey),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("copy_invite_link_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = SoloBlueAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("COPY LINK", color = SoloBlueAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }

                    Button(
                        onClick = {
                            val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                putExtra(android.content.Intent.EXTRA_TEXT, "Rise with the Solo Fitness growth system! Log workouts and awaken together. Referral code: $inviteLink")
                                type = "text/plain"
                            }
                            val shareIntent = android.content.Intent.createChooser(sendIntent, "Awaken fellow hunters!")
                            context.startActivity(shareIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoloBlueAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.2f).testTag("share_invite_btn")
                    ) {
                        Text("SHARE INVITE", color = SoloBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { showSimReferral = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SoloPurpleAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("simulate_friend_btn")
                ) {
                    Text("SIMULATE FRIEND REGISTRATION (+100 EXP)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        } // End of selectedProfileTab == 0

        } else if (selectedProfileTab == 1) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(2.dp, SoloBlueAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SoloBlack.copy(0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp).fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Siren Alert Configuration",
                            tint = SoloDanger,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "COMPULSORY SYSTEM SIREN TRIGGER",
                            color = SoloDanger,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "Configure the alert schedule trigger. The Solo Leveling System high-intensity defense warning siren will blast compulsorily at this designated daily coordinate of hour:minute.",
                        color = SoloTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoloDarkGrey, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { selectedHour = (selectedHour + 1) % 24 },
                                modifier = Modifier.background(SoloBlack.copy(0.3f), CircleShape).size(36.dp)
                            ) {
                                Text("▲", color = SoloBlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = String.format("%02d", selectedHour),
                                color = SoloTextPrimary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            IconButton(
                                onClick = { selectedHour = if (selectedHour == 0) 23 else selectedHour - 1 },
                                modifier = Modifier.background(SoloBlack.copy(0.3f), CircleShape).size(36.dp)
                            ) {
                                Text("▼", color = SoloBlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            text = " : ",
                            color = SoloBlueAccent,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { selectedMinute = (selectedMinute + 5) % 60 },
                                modifier = Modifier.background(SoloBlack.copy(0.3f), CircleShape).size(36.dp)
                            ) {
                                Text("▲", color = SoloBlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = String.format("%02d", selectedMinute),
                                color = SoloTextPrimary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            IconButton(
                                onClick = { selectedMinute = if (selectedMinute == 0) 55 else (selectedMinute - 5 + 60) % 60 },
                                modifier = Modifier.background(SoloBlack.copy(0.3f), CircleShape).size(36.dp)
                            ) {
                                Text("▼", color = SoloBlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Branding Configuration",
                            tint = SoloPurpleAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "LOBBY RE-BRANDING DECREE",
                            color = SoloPurpleAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "Customize the designation value of the interface container representing this system in your local drawer launcher interface only. Changes save directly to system matrix preferences.",
                        color = SoloTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = inputAppName,
                        onValueChange = { inputAppName = it },
                        label = { Text("App Tray Label Override", color = SoloPurpleAccent, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoloPurpleAccent,
                            unfocusedBorderColor = BentoBorderSlate,
                            focusedTextColor = SoloTextPrimary,
                            unfocusedTextColor = SoloTextPrimary
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_app_tray_name_input_box"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val finalTimeString = String.format("%02d:%02d", selectedHour, selectedMinute)
                            coroutineScope.launch {
                                val updatedProfile = profile.copy(soundAlertTime = finalTimeString)
                                viewModel.insertOrUpdateProfile(updatedProfile)
                                viewModel.saveCustomAppTrayName(inputAppName)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoloBlueAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_system_config_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "UPDATE SIREN TIME & BRAND LABEL",
                            color = SoloBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        } else if (selectedProfileTab == 2 && isMyProfileAdmin) {
            var adminPromptInput by remember { mutableStateOf("") }
            val adminProcessing by viewModel.adminProcessing.collectAsStateWithLifecycle()
            val adminLogs by viewModel.adminResponseLog.collectAsStateWithLifecycle()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(2.dp, SoloPurpleAccent.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SoloBlack.copy(0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp).fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "System Admin Control Panel",
                            tint = SoloPurpleAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "SYSTEM ARCHITECT CONTROL",
                            color = SoloPurpleAccent,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "As the Monarch of this system, enter visual prompt decrees below to overwrite metrics or database states dynamically. Powered by Gemini AI.",
                        color = SoloTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = adminPromptInput,
                        onValueChange = { adminPromptInput = it },
                        label = { Text("Write System Prompt decree...", color = SoloPurpleAccent, fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                        placeholder = { Text("e.g. \"Level up 10 times and double my intelligence\"", color = SoloTextSecondary.copy(0.4f), fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoloPurpleAccent,
                            unfocusedBorderColor = BentoBorderSlate,
                            focusedTextColor = SoloTextPrimary,
                            unfocusedTextColor = SoloTextPrimary
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_prompt_input_field"),
                        maxLines = 4,
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (adminProcessing) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = SoloPurpleAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "DECODING NEURAL BLUEPRINTS...",
                                color = SoloPurpleAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                if (adminPromptInput.isNotBlank()) {
                                    viewModel.executeAdminPrompt(adminPromptInput)
                                    adminPromptInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoloPurpleAccent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("execute_admin_decree_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "EXECUTE DECREE",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "TRANSMISSION JOURNAL HISTORY",
                        color = SoloTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                            .background(SoloDarkGrey, RoundedCornerShape(8.dp))
                            .border(1.dp, BentoBorderSlate, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        if (adminLogs.isEmpty()) {
                            Text(
                                text = "Secure link initialized. Whisper your decrees to begin core system updates.",
                                color = SoloTextSecondary.copy(0.5f),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        } else {
                            LazyColumn(reverseLayout = true) {
                                items(adminLogs.reversed()) { logEntry ->
                                    Text(
                                        text = logEntry,
                                        color = if (logEntry.contains("SUCCEEDED")) SoloBlueAccent else if (logEntry.contains("FAILED")) SoloDanger else SoloTextPrimary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // SIMULATED REFERRAL FRIEND DIALOG
    if (showSimReferral) {
        AlertDialog(
            onDismissRequest = { showSimReferral = false },
            title = {
                Text(
                    text = "SIMULATE refer conversion",
                    color = SoloPurpleAccent,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Simulate a friend joining using your link. This awards you +100 EXP instantly!",
                        color = SoloTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = referrerFriendName,
                        onValueChange = { referrerFriendName = it },
                        label = { Text("Friend Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloPurpleAccent, focusedLabelColor = SoloPurpleAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nameToUse = if (referrerFriendName.isBlank()) "Sung Jin-Woo" else referrerFriendName
                        viewModel.simulateFriendInvitation(nameToUse)
                        showSimReferral = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoloPurpleAccent)
                ) {
                    Text("Awaken Friend")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSimReferral = false }) {
                    Text("Cancel", color = SoloTextSecondary)
                }
            },
            containerColor = SoloDarkGrey
        )
    }
}

@Composable
fun ManageExercisesScreen(viewModel: SystemViewModel) {
    val allExercises by viewModel.allExercises.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedEquipmentFilter by remember { mutableStateOf("All Equipment") }
    var selectedMuscleFilter by remember { mutableStateOf("All Muscles") }

    var showEquipmentDropdown by remember { mutableStateOf(false) }
    var showMuscleDropdown by remember { mutableStateOf(false) }

    var createDialogVisible by remember { mutableStateOf(false) }
    var editDialogVisible by remember { mutableStateOf(false) }

    var activeExerciseName by remember { mutableStateOf("") }
    var activeExerciseCategory by remember { mutableStateOf("Chest") }
    var activeExerciseEquipment by remember { mutableStateOf("Barbell") }
    var activeExerciseDesc by remember { mutableStateOf("") }

    val equipmentOptions = listOf("All Equipment", "None", "Barbell", "Dumbbell", "Kettlebell", "Machine", "Plate", "Resistance Band")
    val muscleOptions = listOf("All Muscles", "Abdominals", "Biceps", "Calves", "Triceps", "Chest", "Back", "Shoulders", "Legs", "Full Body")

    // Filtered exercises list
    val filteredList = allExercises.filter { ex ->
        val matchesSearch = ex.name.lowercase().contains(searchQuery.lowercase())
        
        val matchesEquip = if (selectedEquipmentFilter == "All Equipment") {
            true
        } else {
            ex.equipmentRequired.lowercase() == selectedEquipmentFilter.lowercase()
        }

        val matchesMuscle = if (selectedMuscleFilter == "All Muscles") {
            true
        } else {
            ex.category.lowercase() == selectedMuscleFilter.lowercase()
        }

        matchesSearch && matchesEquip && matchesMuscle
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // TOP NAVIGATION HEADER
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.currentScreen.value = "WORKOUTS" },
                modifier = Modifier.background(SoloDarkGrey, CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SoloBlueAccent)
            }
            Text(
                text = "HEVY EXERCISE DIRECTORY",
                color = SoloBlueAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            IconButton(
                onClick = {
                    activeExerciseName = ""
                    activeExerciseCategory = "Chest"
                    activeExerciseEquipment = "Barbell"
                    activeExerciseDesc = ""
                    createDialogVisible = true
                },
                modifier = Modifier.background(SoloPurpleAccent, CircleShape).size(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        // SEARCH BAR
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search exercises by name...", color = SoloTextSecondary, fontSize = 12.sp) },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloBlueAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SoloTextSecondary) }
        )

        // FILTER DROPDOWN TRIGGER BUTTONS
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Equipment trigger button
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { showEquipmentDropdown = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SoloDarkGrey),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = selectedEquipmentFilter.uppercase(),
                        color = SoloBlueAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                DropdownMenu(
                    expanded = showEquipmentDropdown,
                    onDismissRequest = { showEquipmentDropdown = false },
                    modifier = Modifier.background(SoloDarkGrey)
                ) {
                    equipmentOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = SoloTextPrimary, fontSize = 11.sp) },
                            onClick = {
                                selectedEquipmentFilter = option
                                showEquipmentDropdown = false
                            }
                        )
                    }
                }
            }

            // Muscle Trigger Button
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { showMuscleDropdown = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SoloDarkGrey),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = selectedMuscleFilter.uppercase(),
                        color = SoloPurpleAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                DropdownMenu(
                    expanded = showMuscleDropdown,
                    onDismissRequest = { showMuscleDropdown = false },
                    modifier = Modifier.background(SoloDarkGrey)
                ) {
                    muscleOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = SoloTextPrimary, fontSize = 11.sp) },
                            onClick = {
                                selectedMuscleFilter = option
                                showMuscleDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // RESULTS LIST
        Text(
            text = "MATCHING DICTIONARY RECORDS (${filteredList.size})",
            color = SoloTextSecondary,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No matching exercises. Create a custom one!", color = SoloTextSecondary, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList) { ex ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                activeExerciseName = ex.name
                                activeExerciseCategory = ex.category
                                activeExerciseEquipment = ex.equipmentRequired
                                activeExerciseDesc = ex.description
                                editDialogVisible = true
                            }
                            .border(1.dp, BentoBorderSlate, RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = SoloCardBg)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ex.name,
                                    color = SoloTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1.3f)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(SoloPurpleAccent.copy(0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = ex.category.uppercase(), color = SoloPurpleAccent, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(SoloBlueAccent.copy(0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = ex.equipmentRequired.uppercase(), color = SoloBlueAccent, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }
                                    if (ex.isCustom) {
                                        Box(
                                            modifier = Modifier
                                                .background(SoloGold.copy(0.2f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = "CUSTOM", color = SoloGold, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            if (ex.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = ex.description, color = SoloTextSecondary, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }

    // CREATE CUSTOM EXERCISE DIALOG
    if (createDialogVisible) {
        var newName by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf("Chest") }
        var newEquipment by remember { mutableStateOf("Barbell") }
        var newDesc by remember { mutableStateOf("") }

        var categoryExpanded by remember { mutableStateOf(false) }
        var equipExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { createDialogVisible = false },
            title = { Text("CREATE CUSTOM EXERCISE", color = SoloBlueAccent, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Exercise Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloBlueAccent, focusedLabelColor = SoloBlueAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Muscle category dropdown selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = newCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Muscle Group") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { categoryExpanded = true }) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloBlueAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                            modifier = Modifier.fillMaxWidth().clickable { categoryExpanded = true }
                        )
                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                            modifier = Modifier.background(SoloDarkGrey)
                        ) {
                            muscleOptions.filter { it != "All Muscles" }.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m, color = SoloTextPrimary) },
                                    onClick = {
                                        newCategory = m
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Equipment dropdown selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = newEquipment,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Equipment Requirement") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { equipExpanded = true }) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloBlueAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                            modifier = Modifier.fillMaxWidth().clickable { equipExpanded = true }
                        )
                        DropdownMenu(
                            expanded = equipExpanded,
                            onDismissRequest = { equipExpanded = false },
                            modifier = Modifier.background(SoloDarkGrey)
                        ) {
                            equipmentOptions.filter { it != "All Equipment" }.forEach { eq ->
                                DropdownMenuItem(
                                    text = { Text(eq, color = SoloTextPrimary) },
                                    onClick = {
                                        newEquipment = eq
                                        equipExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("Instruction Description") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloBlueAccent, focusedLabelColor = SoloBlueAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.addCustomExercise(newName, newCategory, newEquipment, newDesc)
                            createDialogVisible = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoloBlueAccent)
                ) {
                    Text("Awaken Exercise", color = SoloBlack)
                }
            },
            dismissButton = {
                TextButton(onClick = { createDialogVisible = false }) {
                    Text("Cancel", color = SoloTextSecondary)
                }
            },
            containerColor = SoloDarkGrey
        )
    }

    // EDIT/DELETE EXERCISE DIALOG
    if (editDialogVisible) {
        var editCategory by remember { mutableStateOf(activeExerciseCategory) }
        var editEquipment by remember { mutableStateOf(activeExerciseEquipment) }
        var editDesc by remember { mutableStateOf(activeExerciseDesc) }

        var categoryExpanded by remember { mutableStateOf(false) }
        var equipExpanded by remember { mutableStateOf(false) }

        val isPreloaded = allExercises.find { it.name.lowercase() == activeExerciseName.lowercase() }?.isCustom == false

        AlertDialog(
            onDismissRequest = { editDialogVisible = false },
            title = { Text("EDIT SYSTEM EXERCISE", color = SoloPurpleAccent, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Editing details for: $activeExerciseName", color = SoloTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    if (isPreloaded) {
                        Text(text = "Preloaded exercises cannot change their Core Title, but we can configure muscle focus and instructions.", color = SoloTextSecondary, fontSize = 10.sp)
                    }

                    // Muscle category dropdown selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Muscle Group") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { categoryExpanded = true }) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloPurpleAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                            modifier = Modifier.fillMaxWidth().clickable { categoryExpanded = true }
                        )
                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                            modifier = Modifier.background(SoloDarkGrey)
                        ) {
                            muscleOptions.filter { it != "All Muscles" }.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m, color = SoloTextPrimary) },
                                    onClick = {
                                        editCategory = m
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Equipment dropdown selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editEquipment,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Equipment Requirement") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { equipExpanded = true }) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloPurpleAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                            modifier = Modifier.fillMaxWidth().clickable { equipExpanded = true }
                        )
                        DropdownMenu(
                            expanded = equipExpanded,
                            onDismissRequest = { equipExpanded = false },
                            modifier = Modifier.background(SoloDarkGrey)
                        ) {
                            equipmentOptions.filter { it != "All Equipment" }.forEach { eq ->
                                DropdownMenuItem(
                                    text = { Text(eq, color = SoloTextPrimary) },
                                    onClick = {
                                        editEquipment = eq
                                        equipExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Instruction Description") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoloPurpleAccent, focusedLabelColor = SoloPurpleAccent, focusedTextColor = SoloTextPrimary, unfocusedTextColor = SoloTextPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isPreloaded) {
                        TextButton(
                            onClick = {
                                viewModel.deleteExercise(activeExerciseName)
                                editDialogVisible = false
                            }
                        ) {
                            Text("DELETE", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(modifier = Modifier.size(10.dp)) // empty align placeholder
                    }

                    Button(
                        onClick = {
                            viewModel.updateExercise(activeExerciseName, editCategory, editEquipment, editDesc)
                            editDialogVisible = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoloPurpleAccent)
                    ) {
                        Text("Save Changes", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { editDialogVisible = false }) {
                    Text("Close", color = SoloTextSecondary)
                }
            },
            containerColor = SoloDarkGrey
        )
    }
}


