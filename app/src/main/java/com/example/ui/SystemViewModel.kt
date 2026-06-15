package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.SystemRepository
import com.example.util.WorkoutSoundAlert
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray

class SystemViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SystemRepository
    
    // --- UI States ---
    val userProfile = MutableStateFlow<UserProfileEntity?>(null)
    val bodyFocusSplit = MutableStateFlow<List<BodyFocusEntity>>(emptyList())
    val dailyQuests = MutableStateFlow<List<DailyQuestEntity>>(emptyList())
    val workoutLogs = MutableStateFlow<List<WorkoutLogEntity>>(emptyList())
    val bodyMeasurements = MutableStateFlow<List<BodyMeasurementEntity>>(emptyList())
    val earnedBadges = MutableStateFlow<List<BadgeEntity>>(emptyList())
    val allExercises = MutableStateFlow<List<ExerciseEntity>>(emptyList())

    // --- Derived state for last day's total weight lifted instead of gold ---
    val lastDayTotalWeight = workoutLogs.map { logs ->
        if (logs.isEmpty()) return@map 0f
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val todayStr = sdf.format(Date())
            val pastLogs = logs.filter { it.date < todayStr }
            val targetLogs = if (pastLogs.isNotEmpty()) {
                val lastDate = pastLogs.maxOf { it.date }
                pastLogs.filter { it.date == lastDate }
            } else {
                val lastDate = logs.maxOf { it.date }
                logs.filter { it.date == lastDate }
            }
            targetLogs.sumOf { (it.weight * it.sets * it.reps).toDouble() }.toFloat()
        } catch (e: Exception) {
            0f
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    // --- Custom App Tray Name state ---
    val customAppTrayName = MutableStateFlow("Solo Leveling Fitness")

    // --- Monthly Workout Goals State ---
    val monthlyGoalSessions = MutableStateFlow(12)
    val monthlyGoalVolume = MutableStateFlow(10000)

    val monthlySessionsProgress = workoutLogs.map { logs ->
        try {
            val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
            val currentMonthStr = sdf.format(Date())
            logs.filter { it.date.startsWith(currentMonthStr) }
                .map { it.date }
                .distinct()
                .size
        } catch (e: Exception) {
            0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val monthlyVolumeProgress = workoutLogs.map { logs ->
        try {
            val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
            val currentMonthStr = sdf.format(Date())
            logs.filter { it.date.startsWith(currentMonthStr) }
                .sumOf { (it.weight * it.sets * it.reps).toDouble() }
                .toFloat()
        } catch (e: Exception) {
            0f
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    // --- Active Screen Navigation State ---
    val currentScreen = MutableStateFlow("LOGIN") // "LOGIN", "CHALLENGE_SETUP", "DASHBOARD", "FOCUS_MANAGER", "WORKOUTS", "ANALYTICS", "LEADERBOARD", "OWNER"
    
    // Alert feedback message
    val alertMessage = MutableStateFlow<String?>(null)
    val alertType = MutableStateFlow<String>("neutral") // "achievement", "warning", "neutral"
    
    // Firebase Integration States
    val isFirebaseEnabled = MutableStateFlow<Boolean>(false)
    val firebaseUser = MutableStateFlow<com.google.firebase.auth.FirebaseUser?>(null)
    val isCloudSyncing = MutableStateFlow<Boolean>(false)
    val cloudSyncResult = MutableStateFlow<String?>(null)
    
    // Full screen penalty/workout warning overlay state
    val showFullScreenWarning = MutableStateFlow<Boolean>(false)

    // Full screen daily quest popup state
    val showFullScreenQuestAlert = MutableStateFlow<Boolean>(false)
    private var hasCheckedDailyQuestOnOpen = false

    // Hevy-style workout completion card state
    val showWorkoutCompletionDialog = MutableStateFlow<Boolean>(false)

    // Active Live Workout Tracker (Hevy style)
    val activeWorkoutExercises = MutableStateFlow<List<ActiveWorkoutExercise>>(emptyList())

    // Full screen Solo Leveling Level Up alert trigger state
    val showLevelUpAnimation = MutableStateFlow<Int?>(null)

    fun dismissLevelUpAnimation() {
        showLevelUpAnimation.value = null
    }

    init {
        // Load custom app tray name preference on startup
        try {
            val sharedPrefs = application.getSharedPreferences("solo_leveling_prefs", android.content.Context.MODE_PRIVATE)
            customAppTrayName.value = sharedPrefs.getString("custom_app_tray_name", "Solo Leveling Fitness") ?: "Solo Leveling Fitness"
            monthlyGoalSessions.value = sharedPrefs.getInt("monthly_goal_sessions", 12)
            monthlyGoalVolume.value = sharedPrefs.getInt("monthly_goal_volume", 10000)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val database = AppDatabase.getDatabase(application)
        repository = SystemRepository(database.systemDao())

        // Initial check for Firebase Auth state
        isFirebaseEnabled.value = com.example.util.FirebaseHelper.isAvailable(application)
        if (isFirebaseEnabled.value) {
            try {
                firebaseUser.value = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            } catch (e: Exception) {
                android.util.Log.e("SystemViewModel", "FirebaseAuth initial check failed", e)
            }
        }

        // Collect DB updates reactively
        viewModelScope.launch {
            repository.userProfile.collect { profile ->
                userProfile.value = profile
                if (profile != null) {
                    // Load daily quests for today
                    loadQuestsForToday()
                    checkMissedQuestPenalties()
                    if (currentScreen.value == "LOGIN") {
                        currentScreen.value = "DASHBOARD"
                    }
                    if (!hasCheckedDailyQuestOnOpen) {
                        hasCheckedDailyQuestOnOpen = true
                        showFullScreenQuestAlert.value = true
                        // Play intensive warning siren when the daily quest appears on the screen
                        com.example.util.WorkoutSoundAlert.playSystemWarningSound("high")
                        com.example.util.NotificationHelper.showSystemNotification(
                            getApplication(),
                            "🔥 [DAILY QUEST COMPULSORY DECREE]",
                            "The Daily Quests have been loaded! Complete your S-Class training now or suffer penalty trials!"
                        )
                    }
                }
            }
        }

        // Continuous time-check loop for automatic workout alerts
        viewModelScope.launch {
            var lastCheckedMinute: String? = null
            
            // Local helper to normalize time formats (e.g. "08:00 AM" and "8:00 AM" or "08:00" and "20:00")
            fun normalizeTimeStr(t: String): String {
                return t.replace(" ", "")
                    .replace(":", "")
                    .replace(".", "")
                    .lowercase()
                    .trim()
                    .let { if (it.startsWith("0") && it.length > 3) it.substring(1) else it }
            }

            while (true) {
                val profile = userProfile.value
                if (profile != null) {
                    val alertTime = profile.soundAlertTime // e.g. "08:00 AM" or "08:00"
                    if (!alertTime.isNullOrBlank()) {
                        val now = Date()
                        // Format A (12-hour AM/PM: e.g. "08:00 AM")
                        val time12 = SimpleDateFormat("hh:mm a", Locale.US).format(now).uppercase()
                        // Format B (24-hour style: e.g. "08:00" or "20:00")
                        val time24 = SimpleDateFormat("HH:mm", Locale.US).format(now)
                        
                        val currentMinute = time24 // checking "HH:mm" style
                        
                        if (currentMinute != lastCheckedMinute) {
                            val normAlert = normalizeTimeStr(alertTime)
                            val norm12 = normalizeTimeStr(time12)
                            val norm24 = normalizeTimeStr(time24)

                            if (normAlert == norm12 || normAlert == norm24) {
                                lastCheckedMinute = currentMinute
                                // Trigger automated workout warning full screen overlay and sound!
                                showFullScreenWarning.value = true
                                val intensity = profile.intensity
                                notifyMsg("[SYSTEM ALARM] Workout time alert trigger!", "warning")
                                
                                // Automatically start high intensity warning siren sound alert
                                com.example.util.WorkoutSoundAlert.playSystemWarningSound("high")
                                
                                // Find today's focus split representing the workout selected/scheduled by the user
                                val calendar = Calendar.getInstance()
                                var currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
                                if (currentDayOfWeek == 0) currentDayOfWeek = 7
                                val focusSplit = bodyFocusSplit.value
                                val todayFocus = focusSplit.find { it.dayOfWeek == currentDayOfWeek }?.focusPart ?: "Full Body"
                                
                                // Trigger phone system notification
                                com.example.util.NotificationHelper.showSystemNotification(
                                    getApplication(),
                                    "⚔️ [SYSTEM ALARM: WORKOUT TIME]",
                                    "Rise and shine, Hunter! It is time to do your scheduled $todayFocus workout session!"
                                )
                            }
                        }
                    }
                }
                kotlinx.coroutines.delay(5000) // check every 5 seconds
            }
        }

        viewModelScope.launch {
            repository.bodyFocusSplit.collect { split ->
                bodyFocusSplit.value = split
                if (split.isEmpty()) {
                    initializeDefaultFocusSplit()
                }
            }
        }

        viewModelScope.launch {
            repository.allWorkoutLogs.collect { logs ->
                workoutLogs.value = logs
            }
        }

        viewModelScope.launch {
            repository.allBodyMeasurements.collect { measurements ->
                bodyMeasurements.value = measurements
            }
        }

        viewModelScope.launch {
            repository.earnedBadges.collect { badges ->
                earnedBadges.value = badges
            }
        }

        viewModelScope.launch {
            repository.allExercises.collect { list ->
                allExercises.value = list
                if (list.isEmpty()) {
                    initializeDefaultExercises()
                }
            }
        }
    }

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun initializeDefaultExercises() {
        viewModelScope.launch {
            val list = PreloadedWorkouts.workouts.map {
                ExerciseEntity(
                    name = it.name,
                    category = when {
                        it.category.contains("Chest", ignoreCase = true) -> "Chest"
                        it.category.contains("Tricep", ignoreCase = true) -> "Triceps"
                        it.category.contains("Back", ignoreCase = true) -> "Back"
                        it.category.contains("Bicep", ignoreCase = true) -> "Biceps"
                        it.category.contains("Leg", ignoreCase = true) -> "Legs"
                        it.category.contains("Shoulder", ignoreCase = true) -> "Shoulders"
                        it.category.contains("Abs", ignoreCase = true) || it.category.contains("Cardio", ignoreCase = true) -> "Abdominals"
                        else -> "Full Body"
                    },
                    equipmentRequired = when {
                        it.equipmentRequired.contains("Dumbbell", ignoreCase = true) -> "Dumbbell"
                        it.equipmentRequired.contains("Bodyweight", ignoreCase = true) -> "None"
                        else -> "Barbell"
                    },
                    baseSets = it.baseSets,
                    baseReps = it.baseReps,
                    description = it.description,
                    isCustom = false
                )
            }
            repository.insertExercises(list)
        }
    }

    fun addCustomExercise(name: String, category: String, equipment: String, description: String) {
        viewModelScope.launch {
            val newEx = ExerciseEntity(
                name = name,
                category = category,
                equipmentRequired = equipment,
                baseSets = 3,
                baseReps = "10",
                description = description,
                isCustom = true
            )
            repository.insertExercise(newEx)
            notifyMsg("Exercise '$name' added successfully to directory!", "achievement")
        }
    }

    fun updateExercise(name: String, category: String, equipment: String, description: String) {
        viewModelScope.launch {
            val existingList = repository.getAllExercisesOneShot()
            val existing = existingList.find { it.name.lowercase() == name.lowercase() }
            val updated = ExerciseEntity(
                name = name,
                category = category,
                equipmentRequired = equipment,
                baseSets = existing?.baseSets ?: 3,
                baseReps = existing?.baseReps ?: "10",
                description = description,
                isCustom = existing?.isCustom ?: true
            )
            repository.insertExercise(updated)
            notifyMsg("Exercise '$name' updated successfully!", "achievement")
        }
    }

    fun deleteExercise(name: String) {
        viewModelScope.launch {
            repository.deleteExercise(name)
            notifyMsg("Exercise '$name' deleted from directory.", "neutral")
        }
    }

    fun simulateFriendInvitation(friendName: String) {
        viewModelScope.launch {
            gainXp(100)
            notifyMsg("[SYSTEM REFRESH] Friend '$friendName' registered via your referral link! +100 EXP awarded.", "achievement")
        }
    }

    fun checkMissedQuestPenalties() {
        viewModelScope.launch {
            val today = getTodayDateString()
            val olderDates = repository.getOlderQuestDates(today)
            var penaltyApplied = false
            for (oldDate in olderDates) {
                val quests = repository.getDailyQuestsOneShot(oldDate)
                if (quests.isNotEmpty()) {
                    val allCompleted = quests.all { it.isCompleted }
                    if (!allCompleted) {
                        deductXp(100)
                        penaltyApplied = true
                    }
                    // Mark completed so we don't penalize again!
                    val updatedQuests = quests.map { it.copy(isCompleted = true) }
                    repository.insertDailyQuests(updatedQuests)
                }
            }
            if (penaltyApplied) {
                notifyMsg("[PENALTY] Daily quest completed checks failed for previous days: -100 EXP deducted.", "warning")
            }
        }
    }

    fun deductXp(amount: Int) {
        val current = userProfile.value ?: return
        viewModelScope.launch {
            var currentXp = current.xp - amount
            var newLevel = current.level
            
            while (currentXp < 0 && newLevel > 1) {
                newLevel -= 1
                currentXp += getXpNeededForLevel(newLevel)
            }
            if (currentXp < 0) currentXp = 0
            
            val updated = current.copy(
                xp = currentXp,
                level = newLevel
            )
            repository.insertOrUpdateProfile(updated)
            syncEverythingToCloud()
        }
    }

    // --- Dynamic Alert Sound Notifications ---
    fun notifyMsg(msg: String, type: String = "neutral") {
        alertMessage.value = msg
        alertType.value = type
        if (type == "achievement") {
            com.example.util.WorkoutSoundAlert.playSystemAchievementSound()
        } else if (type == "warning") {
            com.example.util.WorkoutSoundAlert.playSystemWarningAlertSound()
        }
    }

    // --- Cloud Sync Actions ---
    fun syncEverythingToCloud() {
        if (!isFirebaseEnabled.value || firebaseUser.value == null) return
        viewModelScope.launch {
            isCloudSyncing.value = true
            val success = com.example.util.FirebaseHelper.syncLocalToFirestore(getApplication(), repository)
            isCloudSyncing.value = false
            if (success) {
                cloudSyncResult.value = "Success: Local database backed up in Firestore!"
            } else {
                cloudSyncResult.value = "Failed: Cloud connection parameter sync aborted."
            }
        }
    }

    fun restoreEverythingFromCloud(onComplete: (Boolean) -> Unit = {}) {
        if (!isFirebaseEnabled.value || firebaseUser.value == null) {
            onComplete(false)
            return
        }
        viewModelScope.launch {
            isCloudSyncing.value = true
            val success = com.example.util.FirebaseHelper.restoreFromFirestore(getApplication(), repository)
            isCloudSyncing.value = false
            onComplete(success)
            if (success) {
                notifyMsg("SYSTEM DATA RESTORED: Profile, workouts and dimensions synced from Firestore!", "achievement")
            } else {
                notifyMsg("Cloud restore failed or was empty. Syncing local progress UP as base.", "warning")
            }
        }
    }

    // --- Registration & Login ---
    fun loginOrAwaken(name: String, email: String) {
        if (name.isBlank() || email.isBlank()) {
            notifyMsg("Hunters must input a valid name & email to awaken!", "warning")
            return
        }
        viewModelScope.launch {
            val existing = repository.getUserProfileOneShot()
            if (existing != null) {
                // Already awakened
                currentScreen.value = "DASHBOARD"
            } else {
                // Proceed to Onboarding Setup questions
                userProfile.value = UserProfileEntity(
                    name = name,
                    email = email,
                    age = 22,
                    experience = "Beginner",
                    intensity = "Medium",
                    equipment = "Bodyweight"
                )
                currentScreen.value = "CHALLENGE_SETUP"
            }
        }
    }

    fun loginOrAwakenWithFirebase(email: String, name: String, password: String, isCreateAccount: Boolean, onDone: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            notifyMsg("Auth Parameters Error: Email & password are required!", "warning")
            onDone()
            return
        }
        if (isCreateAccount && name.isBlank()) {
            notifyMsg("Auth Parameters Error: Name/Alias is required for brand new awakenings!", "warning")
            onDone()
            return
        }
        
        viewModelScope.launch {
            try {
                if (!isFirebaseEnabled.value) {
                    // Fallback sandboxed registration
                    notifyMsg("Firebase not configured. Proceeding in offline sandbox mode.", "warning")
                    loginOrAwaken(name.ifBlank { "Awakened Hunter" }, email)
                    onDone()
                    return@launch
                }
                
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                if (isCreateAccount) {
                    val uid = com.example.util.FirebaseHelper.signUpWithEmail(getApplication(), email, password)
                    if (uid != null) {
                        firebaseUser.value = auth.currentUser
                        // Create initial profile Entity
                        val profile = UserProfileEntity(
                            id = 1,
                            name = name,
                            email = email,
                            age = 24,
                            experience = "Beginner",
                            intensity = "Medium",
                            equipment = "Full Gym"
                        )
                        repository.insertOrUpdateProfile(profile)
                        syncEverythingToCloud()
                        notifyMsg("COVENANT SEALED: Firebase Auth registered and integrated!", "achievement")
                        currentScreen.value = "CHALLENGE_SETUP"
                    }
                } else {
                    val uid = com.example.util.FirebaseHelper.signInWithEmail(getApplication(), email, password)
                    if (uid != null) {
                        firebaseUser.value = auth.currentUser
                        // Pull database down from FireStore and restore local Room Database
                        restoreEverythingFromCloud { success ->
                            if (success) {
                                currentScreen.value = "DASHBOARD"
                            } else {
                                // create a basic offline flow if restore is empty
                                viewModelScope.launch {
                                    val existing = repository.getUserProfileOneShot()
                                    if (existing == null) {
                                        val profile = UserProfileEntity(
                                            id = 1,
                                            name = name.ifBlank { email.substringBefore("@") },
                                            email = email,
                                            age = 24,
                                            experience = "Beginner",
                                            intensity = "Medium",
                                            equipment = "Full Gym"
                                        )
                                        repository.insertOrUpdateProfile(profile)
                                        currentScreen.value = "CHALLENGE_SETUP"
                                    } else {
                                        currentScreen.value = "DASHBOARD"
                                    }
                                }
                            }
                        }
                        notifyMsg("WELCOME BACK HUNTER: Authentication validated!", "achievement")
                    }
                }
                onDone()
            } catch (e: Exception) {
                notifyMsg("Authentication Protocol Failed: ${e.localizedMessage}", "warning")
                onDone()
            }
        }
    }

    fun handleGoogleAuthSignIn(idToken: String, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                if (!isFirebaseEnabled.value) {
                    notifyMsg("Firebase not configured. Unable to connect Google Auth.", "warning")
                    onDone()
                    return@launch
                }
                val uid = com.example.util.FirebaseHelper.signInWithGoogleToken(getApplication(), idToken)
                if (uid != null) {
                    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                    firebaseUser.value = auth.currentUser
                    restoreEverythingFromCloud { success ->
                        if (success) {
                            currentScreen.value = "DASHBOARD"
                        } else {
                            viewModelScope.launch {
                                val existing = repository.getUserProfileOneShot()
                                if (existing == null) {
                                    val profile = UserProfileEntity(
                                        id = 1,
                                        name = auth.currentUser?.displayName ?: "Google Challenger",
                                        email = auth.currentUser?.email ?: "",
                                        age = 24,
                                        experience = "Beginner",
                                        intensity = "Medium",
                                        equipment = "Full Gym"
                                    )
                                    repository.insertOrUpdateProfile(profile)
                                    currentScreen.value = "CHALLENGE_SETUP"
                                } else {
                                    currentScreen.value = "DASHBOARD"
                                }
                            }
                        }
                    }
                    notifyMsg("GOOGLE LINK PROTOCOL ENERGETIZED!", "achievement")
                }
                onDone()
            } catch (e: Exception) {
                notifyMsg("Google Integration failed: ${e.localizedMessage}", "warning")
                onDone()
            }
        }
    }

    fun handleFirebaseSignOut() {
        if (isFirebaseEnabled.value) {
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                android.util.Log.e("SystemViewModel", "SignOut failed: ${e.message}")
            }
            firebaseUser.value = null
        }
        logout()
    }

    fun completeOnboarding(
        age: Int,
        experience: String,
        intensity: String,
        equipment: String,
        soundTime: String,
        benchPress: Float,
        squat: Float,
        deadlift: Float,
        overheadPress: Float
    ) {
        val current = userProfile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                age = age,
                experience = experience,
                intensity = intensity,
                equipment = equipment,
                soundAlertTime = soundTime,
                benchPress = benchPress,
                squat = squat,
                deadlift = deadlift,
                overheadPress = overheadPress,
                streak = 1,
                lastQuestDate = getTodayDateString()
            )
            repository.insertOrUpdateProfile(updated)
            
            // Populate today's quests immediately
            generateDailyQuestsForDate(getTodayDateString(), intensity)
            
            // Gain Level Up badge initially
            checkAndAwardBadges()
            
            currentScreen.value = "DASHBOARD"
        }
    }

    // --- Core Stats & System Progression ---
    fun increaseStat(statType: String) {
        val current = userProfile.value ?: return
        if (current.statPoints <= 0) {
            notifyMsg("Insufficient Stat Points! Complete daily quests to earn points.", "warning")
            return
        }
        viewModelScope.launch {
            val updated = when (statType.uppercase()) {
                "STR" -> current.copy(strength = current.strength + 1, statPoints = current.statPoints - 1)
                "AGI" -> current.copy(agility = current.agility + 1, statPoints = current.statPoints - 1)
                "VIT" -> current.copy(vitality = current.vitality + 1, statPoints = current.statPoints - 1)
                "INT" -> current.copy(intelligence = current.intelligence + 1, statPoints = current.statPoints - 1)
                "SEN" -> current.copy(sense = current.sense + 1, statPoints = current.statPoints - 1)
                else -> current
            }
            repository.insertOrUpdateProfile(updated)
            syncEverythingToCloud()
        }
    }

    fun getXpNeededForLevel(level: Int): Int {
        if (level <= 1) return 250
        return (250 * Math.pow(2.0, (level - 1).toDouble())).toInt()
    }

    fun gainXp(amount: Int) {
        val current = userProfile.value ?: return
        viewModelScope.launch {
            var currentXp = current.xp + amount
            var newLevel = current.level
            var pointsEarned = 0
            var leveledUp = false
            
            // Solo Leveling Curve formulas: 250, 500, 1000, 2000, 4000... (doubles every level)
            while (currentXp >= getXpNeededForLevel(newLevel)) {
                currentXp -= getXpNeededForLevel(newLevel)
                newLevel += 1
                pointsEarned += 5 // +5 stat points per Level Up!
                leveledUp = true
            }

            if (leveledUp) {
                showLevelUpAnimation.value = newLevel
                notifyMsg("[SYSTEM MSG] LEVEL UP! Hunter Class Rank Ascended to Level $newLevel", "achievement")
                try {
                    com.example.util.WorkoutSoundAlert.playLevelUpSound()
                } catch (e: Exception) {
                    android.util.Log.e("SystemViewModel", "Failed playing level up sound", e)
                }
            }

            val updated = current.copy(
                xp = currentXp,
                level = newLevel,
                statPoints = current.statPoints + pointsEarned
            )
            repository.insertOrUpdateProfile(updated)
            checkAndAwardBadges()
            syncEverythingToCloud()
        }
    }

    // --- Daily Focus Split Management ---
    private fun initializeDefaultFocusSplit() {
        val defaults = listOf(
            BodyFocusEntity(1, "Chest & Triceps"), // Mon
            BodyFocusEntity(2, "Back & Biceps"),   // Tue
            BodyFocusEntity(3, "Legs & Shoulders"),// Wed
            BodyFocusEntity(4, "Abs & Cardio"),    // Thu
            BodyFocusEntity(5, "Chest & Triceps"), // Fri
            BodyFocusEntity(6, "Full Body"),      // Sat
            BodyFocusEntity(7, "Rest Day")         // Sun
        )
        viewModelScope.launch {
            repository.insertBodyFocusList(defaults)
        }
    }

    fun updateFocusSplit(day: Int, newFocus: String) {
        viewModelScope.launch {
            repository.insertBodyFocus(BodyFocusEntity(day, newFocus))
        }
    }

    // --- Quests System ---
    private fun loadQuestsForToday() {
        val today = getTodayDateString()
        viewModelScope.launch {
            repository.getDailyQuests(today).collect { quests ->
                if (quests.isEmpty()) {
                    val profile = repository.getUserProfileOneShot()
                    generateDailyQuestsForDate(today, profile?.intensity ?: "Medium")
                } else {
                    dailyQuests.value = quests
                }
            }
        }
    }

    private suspend fun generateDailyQuestsForDate(date: String, intensity: String) {
        // Multipliers based on workout intensity
        val multiplier = when (intensity.lowercase()) {
            "high" -> 2.0
            "low" -> 0.5
            else -> 1.0
        }

        // Standard Solo Leveling Quest set
        val defaultQuests = listOf(
            DailyQuestEntity(date = date, name = "Push-ups", targetCount = (50 * multiplier).toInt(), currentCount = 0),
            DailyQuestEntity(date = date, name = "Sit-ups", targetCount = (50 * multiplier).toInt(), currentCount = 0),
            DailyQuestEntity(date = date, name = "Squats", targetCount = (50 * multiplier).toInt(), currentCount = 0),
            DailyQuestEntity(date = date, name = "Active Run duration (mins)", targetCount = (10 * multiplier).toInt(), currentCount = 0)
        )
        repository.insertDailyQuests(defaultQuests)
    }

    fun incrementQuestCount(quest: DailyQuestEntity) {
        if (quest.isCompleted) return
        viewModelScope.launch {
            val newCount = quest.currentCount + 5
            val isNowCompleted = newCount >= quest.targetCount
            val updated = quest.copy(
                currentCount = if (newCount >= quest.targetCount) quest.targetCount else newCount,
                isCompleted = isNowCompleted
            )
            repository.updateDailyQuest(updated)
            
            if (isNowCompleted) {
                // Award XP and Gold immediately for System leveling!
                gainXp(50)
                val profile = repository.getUserProfileOneShot()
                if (profile != null) {
                    repository.insertOrUpdateProfile(profile.copy(gold = profile.gold + 50))
                }
                notifyMsg("DAILY QUEST COMPLETE! +50 XP, +50 Gold", "achievement")
                checkAndAwardBadges()
            }
            syncEverythingToCloud()
        }
    }

    fun getMajorLiftKey(exerciseName: String): String? {
        val lower = exerciseName.lowercase()
        return when {
            lower.contains("bench press") -> "BENCH"
            lower.contains("squat") -> "SQUAT"
            lower.contains("deadlift") -> "DEADLIFT"
            lower.contains("overhead press") || lower.contains("shoulder press") -> "OHP"
            else -> null
        }
    }

    // --- Workouts & Custom Log Injection ---
    fun logWorkoutExercise(name: String, category: String, weight: Float, sets: Int, reps: Int, barWeight: Float = 20f, intensity: String = "Medium") {
        val date = getTodayDateString()
        viewModelScope.launch {
            val multiplier = when (intensity.lowercase()) {
                "low" -> 1.0f
                "high" -> 1.8f
                else -> 1.3f // "medium"
            }
            
            // Base volume calculation = sets * reps * (weight + barWeight)
            val volume = sets * reps * (weight + barWeight)
            
            val currentProfile = repository.getUserProfileOneShot() ?: return@launch
            val totalReps = sets * reps
            var majorXp = 0
            var isNewPb = false
            var pbIncreaseXp = 0
            var pbRepsXp = 0
            var updatedProfile = currentProfile

            val liftKey = getMajorLiftKey(name)
            if (liftKey != null) {
                val previousPb = when (liftKey) {
                    "BENCH" -> currentProfile.benchPress
                    "SQUAT" -> currentProfile.squat
                    "DEADLIFT" -> currentProfile.deadlift
                    "OHP" -> currentProfile.overheadPress
                    else -> 0f
                }
                
                majorXp = totalReps * 1 // 1 exp to every rep
                
                if (weight > previousPb) {
                    isNewPb = true
                    pbRepsXp = totalReps * 10 // 10exp in every rep
                    val kgIncrease = weight - previousPb
                    pbIncreaseXp = (kgIncrease * 10).toInt() // every kg increase 10exp
                    
                    // Update personal best in profile
                    updatedProfile = when (liftKey) {
                        "BENCH" -> updatedProfile.copy(benchPress = weight)
                        "SQUAT" -> updatedProfile.copy(squat = weight)
                        "DEADLIFT" -> updatedProfile.copy(deadlift = weight)
                        "OHP" -> updatedProfile.copy(overheadPress = weight)
                        else -> updatedProfile
                    }
                }
            }

            // Calculate XP rewarded:
            val xpReward = if (liftKey != null) {
                majorXp + pbRepsXp + pbIncreaseXp
            } else {
                // Dynamic XP rewarded based on effort, min 30 XP, max 250 XP
                ((volume / 50f) * multiplier).toInt().coerceIn(30, 250)
            }

            val newLog = WorkoutLogEntity(
                date = date,
                exerciseName = name,
                category = category,
                weight = weight,
                sets = sets,
                reps = reps,
                xpEarned = xpReward,
                barWeight = barWeight,
                intensity = intensity
            )
            repository.insertWorkoutLog(newLog)
            gainXp(xpReward)
            
            // Award gold for active workouts
            val goldReward = when (intensity.lowercase()) {
                "high" -> 50
                "low" -> 20
                else -> 30
            }
            repository.insertOrUpdateProfile(updatedProfile.copy(gold = updatedProfile.gold + goldReward))

            // Trigger show workout completion/achievement card
            showWorkoutCompletionDialog.value = true

            val pbAlertStr = if (isNewPb) " [NEW PERSONAL BEST! +$pbRepsXp rep XP, +$pbIncreaseXp weight XP]" else ""
            notifyMsg("Workout Logged! $name: +$xpReward XP$pbAlertStr.", "achievement")
            checkAndAwardBadges()
            syncEverythingToCloud()
        }
    }

    fun dismissWorkoutCompletion() {
        showWorkoutCompletionDialog.value = false
    }

    fun downloadHevyWorkoutCard(context: android.content.Context) {
        val profile = userProfile.value
        val name = profile?.name ?: "Hunter"
        val date = getTodayDateString()
        val todayLogs = workoutLogs.value.filter { it.date == date }
        if (todayLogs.isEmpty()) {
            notifyMsg("No exercises completed today yet! Log a workout first.", "neutral")
            return
        }

        try {
            val totalXp = todayLogs.sumOf { it.xpEarned }
            val totalGold = todayLogs.size * 30
            val bitmap = com.example.util.WorkoutImageExporter.generateWorkoutSummaryCard(
                username = name,
                date = date,
                logs = todayLogs,
                xpEarned = totalXp,
                goldEarned = totalGold
            )
            val fileSaved = com.example.util.WorkoutImageExporter.saveBitmapToGallery(context, bitmap, "Workout_Achievements")
            if (fileSaved != null || android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                notifyMsg("Hevy Share Card successfully saved to Gallery / Pictures directory!", "achievement")
            } else {
                notifyMsg("Error saving card image to storage.", "warning")
            }
        } catch (e: Exception) {
            notifyMsg("Card export failed: ${e.message}", "warning")
        }
    }

    fun downloadLevelUpCard(context: android.content.Context, level: Int) {
        try {
            val profile = userProfile.value
            val name = profile?.name ?: "Hunter"
            val statsText = "STR: ${profile?.strength ?: 10} | AGI: ${profile?.agility ?: 10} | INT: ${profile?.intelligence ?: 10}"
            val bitmap = com.example.util.WorkoutImageExporter.generateLevelUpCard(
                username = name,
                level = level,
                statsText = statsText
            )
            val fileSaved = com.example.util.WorkoutImageExporter.saveBitmapToGallery(context, bitmap, "Level_Up_Ascension")
            if (fileSaved != null || android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                notifyMsg("S-Rank Level-Up Card successfully saved to Gallery / Pictures directory!", "achievement")
            } else {
                notifyMsg("Error saving Level-Up card image to storage.", "warning")
            }
        } catch (e: Exception) {
            notifyMsg("Level Up Card generation failed: ${e.message}", "warning")
        }
    }

    fun shareLevelUpCard(context: android.content.Context, level: Int) {
        try {
            val profile = userProfile.value
            val statsText = "STR: ${profile?.strength ?: 10} | AGI: ${profile?.agility ?: 10} | INT: ${profile?.intelligence ?: 10}"
            val textShare = "⚡ SHADOW MONARCH RE-AWAKEN: ASCENDED TO LEVEL $level! ⚡\nMy current status: $statsText\nDownload the app to register your coordinates!"
            
            val sendIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_TEXT, textShare)
                type = "text/plain"
            }
            val shareIntent = android.content.Intent.createChooser(sendIntent, "SHARE ASCENSION DECREE")
            shareIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
            
            notifyMsg("Ascension share intent dispatched successfully!", "achievement")
        } catch (e: Exception) {
            notifyMsg("Ascension share failed: ${e.message}", "warning")
        }
    }

    suspend fun evaluateExerciseIntensityWithAi(
        exerciseName: String,
        sets: Int,
        reps: Int,
        weight: Float,
        barWeight: Float
    ): String {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "YOUR_GEMINI_API_KEY") {
            // Offline/Invalid key Fallback: Local deterministic AI simulator
            val index = sets * reps * (weight + barWeight)
            return when {
                index < 250f -> "Low"
                index > 1000f -> "High"
                else -> "Medium"
            }
        }
        
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(8, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(8, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                
                val prompt = """
                    Determine strength training exercise intensity strictly based on:
                    Exercise: $exerciseName
                    Sets: $sets
                    Reps: $reps
                    Weight Added: $weight kg
                    Barbell Weight: $barWeight kg
                    
                    Respond with EXACTLY one of these three uppercase words: LOW, MEDIUM, or HIGH.
                    Do not include any explanation or punctuation. Output only a single word.
                """.trimIndent()
                
                val requestJson = org.json.JSONObject().apply {
                    put("contents", org.json.JSONArray().apply {
                        put(org.json.JSONObject().apply {
                            put("parts", org.json.JSONArray().apply {
                                put(org.json.JSONObject().apply {
                                    put("text", prompt)
                                })
                            })
                        })
                    })
                }
                
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = requestJson.toString().toRequestBody(mediaType)
                
                // Using low-latency model gemini-1.5-flash
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
                
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
                    val body = response.body?.string() ?: throw Exception("Empty")
                    val jsonRoot = org.json.JSONObject(body)
                    val cand = jsonRoot.getJSONArray("candidates").getJSONObject(0)
                    val text = cand.getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text").trim()
                    
                    val cleanVal = text.uppercase()
                    when {
                        cleanVal.contains("LOW") -> "Low"
                        cleanVal.contains("HIGH") -> "High"
                        else -> "Medium"
                    }
                }
            } catch (e: Exception) {
                val index = sets * reps * (weight + barWeight)
                when {
                    index < 250f -> "Low"
                    index > 1000f -> "High"
                    else -> "Medium"
                }
            }
        }
    }

    fun initializeActiveWorkoutIfEmpty(suggested: List<PreloadedExercise>) {
        if (activeWorkoutExercises.value.isEmpty()) {
            activeWorkoutExercises.value = suggested.map { ex ->
                ActiveWorkoutExercise(
                    name = ex.name,
                    category = ex.category,
                    sets = ex.baseSets,
                    reps = ex.baseReps.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 10,
                    weight = 15f,
                    barWeight = 20f,
                    isDone = false,
                    setsList = List(ex.baseSets) { idx ->
                        ActiveWorkoutSet(id = idx + 1, reps = ex.baseReps.replace(Regex("[^0-9]"), ""), weight = "15")
                    }
                )
            }
        }
    }

    fun addExerciseToActiveWorkout(ex: PreloadedExercise, customSets: Int = 3, customReps: String = "10", customWeight: Float = 15f) {
        val repsInt = customReps.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 10
        val newExercise = ActiveWorkoutExercise(
            name = ex.name,
            category = ex.category,
            sets = customSets,
            reps = repsInt,
            weight = customWeight,
            barWeight = 20f,
            isDone = false,
            setsList = List(customSets) { idx ->
                ActiveWorkoutSet(id = idx + 1, reps = customReps, weight = customWeight.toString())
            }
        )
        activeWorkoutExercises.value = activeWorkoutExercises.value + newExercise
        notifyMsg("Added ${ex.name} to Live Workout Session queue!", "neutral")
    }

    fun updateActiveExerciseSetsList(exerciseId: String, newSetsList: List<ActiveWorkoutSet>) {
        activeWorkoutExercises.value = activeWorkoutExercises.value.map {
            if (it.id == exerciseId) {
                it.copy(setsList = newSetsList, sets = newSetsList.size)
            } else {
                it
            }
        }
    }

    fun updateActiveExerciseBarWeight(exerciseId: String, newBarWeight: Float) {
        activeWorkoutExercises.value = activeWorkoutExercises.value.map {
            if (it.id == exerciseId) {
                it.copy(barWeight = newBarWeight)
            } else {
                it
            }
        }
    }

    fun completeActiveExercise(exerciseId: String, context: android.content.Context) {
        val exercise = activeWorkoutExercises.value.find { it.id == exerciseId } ?: return
        
        viewModelScope.launch {
            notifyMsg("AI is calculating performance intensity...", "neutral")
            
            val decidedIntensity = evaluateExerciseIntensityWithAi(
                exerciseName = exercise.name,
                sets = exercise.sets,
                reps = exercise.reps,
                weight = exercise.setsList.firstOrNull()?.weight?.toFloatOrNull() ?: exercise.weight,
                barWeight = exercise.barWeight
            )
            
            val avgWeight = exercise.setsList.mapWithIndexMaybe { it.weight.toFloatOrNull() }.filterNotNull().average().toFloat().takeIf { !it.isNaN() } ?: exercise.weight
            val avgReps = exercise.setsList.mapWithIndexMaybe { it.reps.toIntOrNull() }.filterNotNull().average().toInt().takeIf { it > 0 } ?: exercise.reps
            
            logWorkoutExercise(
                name = exercise.name,
                category = exercise.category,
                weight = avgWeight,
                sets = exercise.sets,
                reps = avgReps,
                barWeight = exercise.barWeight,
                intensity = decidedIntensity
            )
            
            activeWorkoutExercises.value = activeWorkoutExercises.value.map {
                if (it.id == exerciseId) {
                    it.copy(isDone = true, aiIntensity = decidedIntensity)
                } else {
                    it
                }
            }
        }
    }

    fun resetActiveWorkout() {
        activeWorkoutExercises.value = emptyList()
        notifyMsg("Re-initialized active workout queue.", "neutral")
    }

    private fun <T, R> List<T>.mapWithIndexMaybe(transform: (T) -> R): List<R> {
        return this.map(transform)
    }

    fun deleteWorkoutLog(id: Int) {
        viewModelScope.launch {
            repository.deleteWorkoutLog(id)
            syncEverythingToCloud()
        }
    }

    // --- Body Analytics ---
    fun logBodyMeasurements(weight: Float, muscle: Float, fat: Float, chest: Float, arms: Float, waist: Float) {
        val date = getTodayDateString()
        viewModelScope.launch {
            val analytics = BodyMeasurementEntity(
                date = date,
                weight = weight,
                muscleMass = muscle,
                fatPercentage = fat,
                chest = chest,
                arms = arms,
                waist = waist
            )
            repository.insertBodyMeasurement(analytics)
            
            // Reward minor XP for tracking
            gainXp(20)
            
            notifyMsg("System Analytics updated! +20 XP System Synced.", "achievement")
            checkAndAwardBadges()
            syncEverythingToCloud()
        }
    }

    fun deleteMeasurement(id: Int) {
        viewModelScope.launch {
            repository.deleteBodyMeasurement(id)
            syncEverythingToCloud()
        }
    }

    // --- Trigger System Warning Alert Time preference ---
    fun testWarningSound() {
        val profile = userProfile.value
        val currentIntensity = profile?.intensity ?: "Medium"
        viewModelScope.launch {
            showFullScreenWarning.value = true
            notifyMsg("[SYSTEM ALARM] Workout time alert trigger!", "warning")
            com.example.util.WorkoutSoundAlert.playSystemWarningSound(currentIntensity)
        }
    }

    fun dismissFullScreenWarning() {
        showFullScreenWarning.value = false
    }

    // --- Badge Achievement Engine ---
    private fun checkAndAwardBadges() {
        viewModelScope.launch {
            val currentProfile = repository.getUserProfileOneShot() ?: return@launch
            val listBadges = repository.getEarnedBadgesOneShot()
            val existingIds = listBadges.map { it.badgeId }.toSet()
            val today = getTodayDateString()

            // 1. Level up badges
            if (currentProfile.level >= 2 && !existingIds.contains("LEVEL_UP_2")) {
                repository.insertBadge(BadgeEntity(
                    badgeId = "LEVEL_UP_2",
                    name = "Awakened Hunter",
                    description = "Reached System Level 2 or higher.",
                    earnedDate = today,
                    iconName = "military_tech"
                ))
                notifyMsg("NEW BADGE AWARDED: Awakened Hunter", "achievement")
                syncEverythingToCloud()
            }

            if (currentProfile.level >= 5 && !existingIds.contains("LEVEL_UP_5")) {
                repository.insertBadge(BadgeEntity(
                    badgeId = "LEVEL_UP_5",
                    name = "S-Rank Gatecrasher",
                    description = "Ascended to Level 5 or higher in growth stats.",
                    earnedDate = today,
                    iconName = "star"
                ))
                notifyMsg("NEW BADGE AWARDED: S-Rank Gatecrasher", "achievement")
                syncEverythingToCloud()
            }

            // 2. Continuous Streak
            if (currentProfile.streak >= 7 && !existingIds.contains("STREAK_7")) {
                repository.insertBadge(BadgeEntity(
                    badgeId = "STREAK_7",
                    name = "7-Day Soloing",
                    description = "Maintained extreme consistent logs for a full 7 days.",
                    earnedDate = today,
                    iconName = "local_fire_department"
                ))
                notifyMsg("NEW BADGE AWARDED: 7-Day Soloing", "achievement")
                syncEverythingToCloud()
            }

            // 3. Perfect Week (7 separate workout logs)
            val allLogs = workoutLogs.value
            if (allLogs.size >= 5 && !existingIds.contains("PERFECT_WEEK")) {
                repository.insertBadge(BadgeEntity(
                    badgeId = "PERFECT_WEEK",
                    name = "Perfect Week",
                    description = "Completed 5 or more custom suggested exercises.",
                    earnedDate = today,
                    iconName = "emoji_events"
                ))
                notifyMsg("NEW BADGE AWARDED: Perfect Week", "achievement")
                syncEverythingToCloud()
            }

            // 4. Initial pb custom workout
            if (allLogs.isNotEmpty() && !existingIds.contains("FIRST_WORKOUT")) {
                repository.insertBadge(BadgeEntity(
                    badgeId = "FIRST_WORKOUT",
                    name = "New Personal Best",
                    description = "Successfully finished a full specialized workout log under equipment filter.",
                    earnedDate = today,
                    iconName = "fitness_center"
                ))
                notifyMsg("NEW BADGE AWARDED: New Personal Best", "achievement")
                syncEverythingToCloud()
            }

            // 5. Body measurements check
            if (bodyMeasurements.value.isNotEmpty() && !existingIds.contains("ANALYTICS_LOGGED")) {
                repository.insertBadge(BadgeEntity(
                    badgeId = "ANALYTICS_LOGGED",
                    name = "Grand Analyst",
                    description = "First detailed body analytics log recorded.",
                    earnedDate = today,
                    iconName = "troubleshoot"
                ))
                notifyMsg("NEW BADGE AWARDED: Grand Analyst", "achievement")
                syncEverythingToCloud()
            }

            // 6. E-Rank Rookie Badge
            if ((currentProfile.streak >= 1 || allLogs.isNotEmpty()) && !existingIds.contains("ERANK_BADGE")) {
                repository.insertBadge(BadgeEntity(
                    badgeId = "ERANK_BADGE",
                    name = "E-Rank Rookie",
                    description = "Milestone: Awakened into the growth system with initial training consistency.",
                    earnedDate = today,
                    iconName = "shield"
                ))
                notifyMsg("NEW BADGE AWARDED: E-Rank Rookie", "achievement")
                syncEverythingToCloud()
            }

            // 7. S-Rank Overlord Badge
            val totalVolume = allLogs.sumOf { (it.sets * it.reps * (it.weight + it.barWeight)).toDouble() }.toFloat()
            if ((totalVolume >= 5000f || currentProfile.streak >= 7) && !existingIds.contains("SRANK_BADGE")) {
                repository.insertBadge(BadgeEntity(
                    badgeId = "SRANK_BADGE",
                    name = "S-Rank Overlord",
                    description = "Milestone: Lifted 5,000+ kg total volume or attained 7-day consistency!",
                    earnedDate = today,
                    iconName = "star"
                ))
                notifyMsg("NEW BADGE AWARDED: S-Rank Overlord", "achievement")
                syncEverythingToCloud()
            }
        }
    }

    // --- AI Shadow Trainer Guide States ---
    val aiTrainerExercises = MutableStateFlow<List<AiWorkoutExercise>>(emptyList())
    val aiTrainerMessage = MutableStateFlow<String>("The System Shadow Guide is awaiting your command. Trigger the evaluation to load your custom routine based on available equipment and past logs.")
    val aiTrainerLoading = MutableStateFlow<Boolean>(false)
    val aiTrainerError = MutableStateFlow<String?>(null)

    fun generateAiTrainerRecommendation() {
        val profile = userProfile.value ?: return
        aiTrainerLoading.value = true
        aiTrainerError.value = null

        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            var currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
            if (currentDayOfWeek == 0) currentDayOfWeek = 7
            val focusSplit = bodyFocusSplit.value
            val todayFocus = focusSplit.find { it.dayOfWeek == currentDayOfWeek }?.focusPart ?: "Full Body"

            val pastLogs = workoutLogs.value.take(8).joinToString("; ") {
                "${it.exerciseName} (${it.sets}s x ${it.reps}r)"
            }
            val historyText = if (pastLogs.isBlank()) "No recorded exercises yet." else pastLogs

            val promptBody = """
                Extract 3-4 exercises for physical category "$todayFocus" and equipment "${profile.equipment}". 
                Output a strict JSON matching this structure without any backticks, code blocks or other wrapper formatting:
                {
                  "message": "Intimidating systemic Solo Leveling style analysis for ${profile.name} (Lvl ${profile.level}, experience ${profile.experience}). Analyze previous logs: $historyText",
                  "exercises": [
                    {
                      "name": "Exercise Name",
                      "sets": 4,
                      "reps": "12",
                      "recommendedWeight": "15kg",
                      "description": "Short explanation",
                      "intensity": "Medium"
                    }
                  ]
                }
            """.trimIndent()

            val apiKey = com.example.BuildConfig.GEMINI_API_KEY
            val isUsingSimulated = apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY"

            if (isUsingSimulated) {
                val simulatedMsg = "Hunter ${profile.name}, the System AI transceiver is offline. Deploying stand-by recommendation from localized database memory. Focus part target: $todayFocus."
                val simulatedList = getLocalSimulatedWorkout(todayFocus, profile.equipment, profile.intensity)
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    aiTrainerMessage.value = simulatedMsg
                    aiTrainerExercises.value = simulatedList
                    aiTrainerLoading.value = false
                }, 800)
            } else {
                try {
                    val client = okhttp3.OkHttpClient.Builder()
                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .build()

                    val requestJson = org.json.JSONObject().apply {
                        put("contents", org.json.JSONArray().apply {
                            put(org.json.JSONObject().apply {
                                put("parts", org.json.JSONArray().apply {
                                    put(org.json.JSONObject().apply {
                                        put("text", promptBody)
                                    })
                                })
                            })
                        })
                    }

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val requestBody = requestJson.toString().toRequestBody(mediaType)
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

                    val request = okhttp3.Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build()

                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            client.newCall(request).execute().use { response ->
                                if (!response.isSuccessful) {
                                    throw Exception("HTTP Error: ${response.code}")
                                }
                                val bodyString = response.body?.string() ?: throw Exception("Empty response body")
                                val jsonResponse = org.json.JSONObject(bodyString)
                                val candidates = jsonResponse.optJSONArray("candidates")
                                if (candidates == null || candidates.length() == 0) {
                                    throw Exception("Empty candidates")
                                }
                                val text = candidates.getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .optString("text")

                                val cleanJsonText = text.trim()
                                    .removePrefix("```json")
                                    .removePrefix("```")
                                    .removeSuffix("```")
                                    .trim()

                                val parsedRoot = org.json.JSONObject(cleanJsonText)
                                val parsedMessage = parsedRoot.optString("message", "System selected workout sequence ready.")
                                val parsedArray = parsedRoot.optJSONArray("exercises")
                                val parsedList = mutableListOf<AiWorkoutExercise>()

                                if (parsedArray != null) {
                                    for (i in 0 until parsedArray.length()) {
                                        val obj = parsedArray.getJSONObject(i)
                                        parsedList.add(AiWorkoutExercise(
                                            name = obj.optString("name", "Custom AI Lift"),
                                            sets = obj.optInt("sets", 3),
                                            reps = obj.optString("reps", "10"),
                                            recommendedWeight = obj.optString("recommendedWeight", "Bodyweight"),
                                            description = obj.optString("description", "Perform with optimal form."),
                                            intensity = obj.optString("intensity", "Medium")
                                        ))
                                    }
                                }

                                aiTrainerMessage.value = parsedMessage
                                aiTrainerExercises.value = parsedList
                                aiTrainerLoading.value = false
                            }
                        } catch (e: Exception) {
                            val simulatedList = getLocalSimulatedWorkout(todayFocus, profile.equipment, profile.intensity)
                            aiTrainerMessage.value = "Hunter ${profile.name}, a synaptic connection anomaly occurred (${e.localizedMessage}). Utilizing secure local fallback sequence:"
                            aiTrainerExercises.value = simulatedList
                            aiTrainerLoading.value = false
                        }
                    }
                } catch (e: Exception) {
                    aiTrainerError.value = "Synapse route error: ${e.localizedMessage}"
                    aiTrainerLoading.value = false
                }
            }
        }
    }

    private fun getLocalSimulatedWorkout(category: String, equipment: String, intensity: String): List<AiWorkoutExercise> {
        val basicList = PreloadedWorkouts.workouts.filter {
            it.category == category &&
            (it.equipmentRequired == equipment || it.equipmentRequired == "Bodyweight")
        }.take(3)
        
        val exercises = if (basicList.isNotEmpty()) basicList else {
            listOf(
                PreloadedExercise(
                    name = "Isometric Centering Plank",
                    category = category,
                    equipmentRequired = "Bodyweight",
                    baseSets = 3,
                    baseReps = "60s hold",
                    description = "Brace absolute entire core grid. Great for general stabilization and cellular recovery."
                )
            )
        }

        return exercises.map {
            AiWorkoutExercise(
                name = "[AI Guide] ${it.name}",
                sets = it.baseSets,
                reps = it.baseReps,
                recommendedWeight = if (intensity.lowercase() == "high") "Progressive overload weight" else "Moderate starting weight",
                description = "Shadow tailor optimization: " + it.description,
                intensity = intensity
            )
        }
    }

    fun dismissAlert() {
        alertMessage.value = null
    }

    fun dismissFullScreenQuestAlert() {
        showFullScreenQuestAlert.value = false
    }

    fun insertOrUpdateProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.insertOrUpdateProfile(profile)
            syncEverythingToCloud()
        }
    }

    fun logout() {
        viewModelScope.launch {
            hasCheckedDailyQuestOnOpen = false
            showFullScreenQuestAlert.value = false
            // Simply return to login screen
            currentScreen.value = "LOGIN"
        }
    }

    fun saveCustomAppTrayName(newName: String) {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("solo_leveling_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("custom_app_tray_name", newName).apply()
        customAppTrayName.value = newName
        notifyMsg("App Tray label requested update: '$newName' saved inside system preferences!", "achievement")
    }

    fun saveMonthlyGoals(sessions: Int, volume: Int) {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("solo_leveling_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putInt("monthly_goal_sessions", sessions)
            .putInt("monthly_goal_volume", volume)
            .apply()
        monthlyGoalSessions.value = sessions
        monthlyGoalVolume.value = volume
        notifyMsg("Target Special Goals updated perfectly! New training schedule initialized.", "achievement")
    }

    val adminProcessing = MutableStateFlow(false)
    val adminResponseLog = MutableStateFlow<List<String>>(emptyList())
    val adminChatMessages = MutableStateFlow<List<AdminChatMessage>>(listOf(
        AdminChatMessage("system", "Welcome, Sovereign Satyam. I am the Solo Leveling System Architect Core. You may issue prompts, ask questions, or decree database modifications directly. I have full override privileges on user profiles, quests, and workout structures.")
    ))

    fun sendAdminChatMessage(promptText: String) {
        if (promptText.isBlank()) return
        val userMsg = AdminChatMessage("sovereign", promptText)
        adminChatMessages.value = adminChatMessages.value + userMsg
        adminProcessing.value = true

        viewModelScope.launch {
            val profile = repository.getUserProfileOneShot() ?: return@launch
            val apiKey = com.example.BuildConfig.GEMINI_API_KEY
            val isUsingSimulated = apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY"

            var responseText = ""
            if (isUsingSimulated) {
                val cleanPrompt = promptText.lowercase().trim()
                try {
                    if (cleanPrompt.contains("level up") || cleanPrompt.contains("levelup")) {
                        val numMatch = Regex("\\d+").find(cleanPrompt)
                        val levelsToAdd = numMatch?.value?.toIntOrNull() ?: 1
                        val nextProfile = profile.copy(level = profile.level + levelsToAdd)
                        repository.insertOrUpdateProfile(nextProfile)
                        responseText = "SUCCEEDED: Sovereign Satyam, adjusting the neural matrix. You have ascended to Level ${nextProfile.level}!"
                    } else if (cleanPrompt.contains("add stats") || cleanPrompt.contains("stat points") || cleanPrompt.contains("stat")) {
                        val numMatch = Regex("\\d+").find(cleanPrompt)
                        val ptsToAdd = numMatch?.value?.toIntOrNull() ?: 10
                        val nextProfile = profile.copy(statPoints = profile.statPoints + ptsToAdd)
                        repository.insertOrUpdateProfile(nextProfile)
                        responseText = "SUCCEEDED: Sovereign decree processed. +$ptsToAdd stat points have been injected into your current pool."
                    } else if (cleanPrompt.contains("gold") || cleanPrompt.contains("money")) {
                        val numMatch = Regex("\\d+").find(cleanPrompt)
                        val goldToAdd = numMatch?.value?.toIntOrNull() ?: 500
                        val nextProfile = profile.copy(gold = profile.gold + goldToAdd)
                        repository.insertOrUpdateProfile(nextProfile)
                        responseText = "SUCCEEDED: Injected +$goldToAdd golden mana crystals directly into your treasury!"
                    } else if (cleanPrompt.contains("quest")) {
                        val questName = if (cleanPrompt.contains("name")) {
                            cleanPrompt.substringAfter("name").trim()
                        } else "Sovereign Special Quest"
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                        val today = sdf.format(java.util.Date())
                        val quest = DailyQuestEntity(
                            date = today,
                            name = questName.uppercase(),
                            targetCount = 100,
                            currentCount = 0,
                            isCompleted = false
                        )
                        repository.insertDailyQuest(quest)
                        responseText = "SUCCEEDED: Created active quest: '$questName' (Target: 100) inside the global register."
                    } else {
                        val nextProfile = profile.copy(
                            strength = profile.strength + 2,
                            agility = profile.agility + 2,
                            vitality = profile.vitality + 2
                        )
                        repository.insertOrUpdateProfile(nextProfile)
                        responseText = "SYSTEM AUTOMATION: Instruction observed. Universal stats up by +2! Specify specific commands such as 'level up 10 times' or 'add 500 gold' to issue detailed overwrites."
                    }
                } catch (e: Exception) {
                    responseText = "FAILED execution logic error: ${e.message}"
                }
                adminChatMessages.value = adminChatMessages.value + AdminChatMessage("system", responseText)
                notifyMsg(responseText, "achievement")
                adminProcessing.value = false
            } else {
                try {
                    val client = okhttp3.OkHttpClient.Builder()
                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .build()

                    val historySerialized = adminChatMessages.value.takeLast(10).joinToString("\n") {
                        "${it.sender.uppercase()}: ${it.text}"
                    }

                    val instructions = """
                        You are the Solo Leveling System Architect Core, the omnipotent controller of user satyamyadav30042008@gmail.com's system database.
                        The administrator (SOVEREIGN) wants to make changes to the app by writing prompts in this chat.
                        Here is the previous conversation history:
                        $historySerialized

                        Latest prompt to execute: "$promptText"

                        Analyze this prompt and calculate adjustments.
                        You MUST reply with a JSON response that contains:
                        1. "explanation": A narrative confirming the changes and roleplaying as the System Architect (e.g. "By your sovereign authority, Satyam, I have adjusted the physical attributes matrix. Your strength is now 80 and level 12!").
                        2. "profileUpdates": Optional JSON object matching fields in UserProfileEntity: level:Int, xp:Int, strength:Int, agility:Int, vitality:Int, intelligence:Int, sense:Int, statPoints:Int, gold:Int, benchPress:Float, squat:Float, deadlift:Float, overheadPress:Float.
                        3. "newQuest": Optional JSON object for a new quest: name:String, targetCount:Int.
                        4. "newWorkoutLog": Optional JSON object for a workout log: exerciseName:String, category:String, weight:Float, sets:Int, reps:Int.
                        Format your output STRICTLY as a clean JSON object containing only these optional fields. No additional text wrappers.
                    """.trimIndent()

                    val requestJson = org.json.JSONObject().apply {
                        put("contents", org.json.JSONArray().apply {
                            put(org.json.JSONObject().apply {
                                put("parts", org.json.JSONArray().apply {
                                    put(org.json.JSONObject().apply {
                                        put("text", instructions)
                                    })
                                })
                            })
                        })
                    }

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val requestBody = requestJson.toString().toRequestBody(mediaType)
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

                    val request = okhttp3.Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build()

                    val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        client.newCall(request).execute().use { response ->
                            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
                            val body = response.body?.string() ?: throw Exception("Empty response body")
                            val jsonRoot = org.json.JSONObject(body)
                            val cand = jsonRoot.getJSONArray("candidates").getJSONObject(0)
                            cand.getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
                        }
                    }

                    val cleanJsonStr = result.trim()
                        .removePrefix("```json")
                        .removePrefix("```")
                        .removeSuffix("```")
                        .trim()

                    val jsonResult = org.json.JSONObject(cleanJsonStr)
                    val explanation = jsonResult.optString("explanation", "Sovereign request executed.")
                    
                    if (jsonResult.has("profileUpdates")) {
                        val pUp = jsonResult.getJSONObject("profileUpdates")
                        var updatedProfile = repository.getUserProfileOneShot() ?: profile
                        if (pUp.has("level")) updatedProfile = updatedProfile.copy(level = pUp.getInt("level"))
                        if (pUp.has("xp")) updatedProfile = updatedProfile.copy(xp = pUp.getInt("xp"))
                        if (pUp.has("strength")) updatedProfile = updatedProfile.copy(strength = pUp.getInt("strength"))
                        if (pUp.has("agility")) updatedProfile = updatedProfile.copy(agility = pUp.getInt("agility"))
                        if (pUp.has("vitality")) updatedProfile = updatedProfile.copy(vitality = pUp.getInt("vitality"))
                        if (pUp.has("intelligence")) updatedProfile = updatedProfile.copy(intelligence = pUp.getInt("intelligence"))
                        if (pUp.has("sense")) updatedProfile = updatedProfile.copy(sense = pUp.getInt("sense"))
                        if (pUp.has("statPoints")) updatedProfile = updatedProfile.copy(statPoints = pUp.getInt("statPoints"))
                        if (pUp.has("gold")) updatedProfile = updatedProfile.copy(gold = pUp.getInt("gold"))
                        if (pUp.has("benchPress")) updatedProfile = updatedProfile.copy(benchPress = pUp.getDouble("benchPress").toFloat())
                        if (pUp.has("squat")) updatedProfile = updatedProfile.copy(squat = pUp.getDouble("squat").toFloat())
                        if (pUp.has("deadlift")) updatedProfile = updatedProfile.copy(deadlift = pUp.getDouble("deadlift").toFloat())
                        if (pUp.has("overheadPress")) updatedProfile = updatedProfile.copy(overheadPress = pUp.getDouble("overheadPress").toFloat())
                        repository.insertOrUpdateProfile(updatedProfile)
                    }

                    if (jsonResult.has("newQuest")) {
                        val nQ = jsonResult.getJSONObject("newQuest")
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                        val today = sdf.format(java.util.Date())
                        val quest = DailyQuestEntity(
                            date = today,
                            name = nQ.optString("name", "Sovereign Mandate").uppercase(),
                            targetCount = nQ.optInt("targetCount", 100),
                            currentCount = 0,
                            isCompleted = false
                        )
                        repository.insertDailyQuest(quest)
                    }

                    if (jsonResult.has("newWorkoutLog")) {
                        val nW = jsonResult.getJSONObject("newWorkoutLog")
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                        val today = sdf.format(java.util.Date())
                        val log = WorkoutLogEntity(
                            date = today,
                            exerciseName = nW.optString("exerciseName", "Architect Authorized Lift"),
                            category = nW.optString("category", "Full Body"),
                            weight = nW.optDouble("weight", 60.0).toFloat(),
                            sets = nW.optInt("sets", 3),
                            reps = nW.optInt("reps", 10),
                            xpEarned = 25
                        )
                        repository.insertWorkoutLog(log)
                    }

                    adminChatMessages.value = adminChatMessages.value + AdminChatMessage("system", explanation)
                    notifyMsg(explanation, "achievement")
                } catch (e: Exception) {
                    val errMsg = "Error processing system edit: ${e.message}"
                    adminChatMessages.value = adminChatMessages.value + AdminChatMessage("system", errMsg)
                    notifyMsg(errMsg, "warning")
                }
                adminProcessing.value = false
            }
        }
    }

    fun executeAdminPrompt(promptText: String) {}

    fun unused_executeAdminPrompt(promptText: String) {
        if (promptText.isBlank()) return
        viewModelScope.launch {
            adminProcessing.value = true
            val profile = repository.getUserProfileOneShot() ?: return@launch
            val apiKey = com.example.BuildConfig.GEMINI_API_KEY
            val isUsingSimulated = apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY"

            var executionLogMessage = "Admin Command Executed: " + promptText
            
            if (isUsingSimulated) {
                // Perform robust Local Command Fallback Parser!
                val cleanPrompt = promptText.lowercase().trim()
                var responseText = "Executed via Offline System Core Rules."
                
                try {
                    if (cleanPrompt.contains("level up") || cleanPrompt.contains("levelup")) {
                        val numMatch = Regex("\\d+").find(cleanPrompt)
                        val levelsToAdd = numMatch?.value?.toIntOrNull() ?: 1
                        val nextProfile = profile.copy(level = profile.level + levelsToAdd)
                        repository.insertOrUpdateProfile(nextProfile)
                        responseText = "SUCCEEDED: Under decree, Admin leveled up +$levelsToAdd times. Level is now ${nextProfile.level}."
                    } else if (cleanPrompt.contains("add stats") || cleanPrompt.contains("stat points") || cleanPrompt.contains("stat")) {
                        val numMatch = Regex("\\d+").find(cleanPrompt)
                        val ptsToAdd = numMatch?.value?.toIntOrNull() ?: 10
                        val nextProfile = profile.copy(statPoints = profile.statPoints + ptsToAdd)
                        repository.insertOrUpdateProfile(nextProfile)
                        responseText = "SUCCEEDED: System matrix has distributed +$ptsToAdd stat points to your current pool."
                    } else if (cleanPrompt.contains("quest")) {
                        val questName = if (cleanPrompt.contains("name")) {
                            cleanPrompt.substringAfter("name").substringBefore("with").trim()
                        } else "Admin Custom Compulsory Quest"
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                        val today = sdf.format(java.util.Date())
                        val quest = DailyQuestEntity(
                            date = today,
                            name = questName.uppercase(),
                            targetCount = 50,
                            currentCount = 0,
                            isCompleted = false
                        )
                        repository.insertDailyQuest(quest)
                        responseText = "SUCCEEDED: Added compulsory quest: '$questName' (Target: 50) into active register."
                    } else {
                        val nextProfile = profile.copy(
                            strength = profile.strength + 5,
                            agility = profile.agility + 5,
                            vitality = profile.vitality + 5,
                            intelligence = profile.intelligence + 5,
                            sense = profile.sense + 5
                        )
                        repository.insertOrUpdateProfile(nextProfile)
                        responseText = "SUCCEEDED: Universal stats upgrade! System core has enhanced strength, agility, vitality, intelligence, and sense by +5."
                    }
                } catch (e: Exception) {
                    responseText = "FAILED offline parser error: ${e.message}"
                }
                
                adminResponseLog.value = adminResponseLog.value + "${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())} - $responseText"
                notifyMsg(responseText, "achievement")
                adminProcessing.value = false
            } else {
                try {
                    val client = okhttp3.OkHttpClient.Builder()
                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .build()

                    val instructions = """
                        You are the Solo Leveling System Architect Core. The administrator has issued a command prompt to update the application database entries or user metrics.
                        Analyze the command prompt: "$promptText"
                        Output a JSON response with:
                        1. "explanation": A message explaining the changes (e.g., "Leveled up the Admin to Level 50 and set bench press to 120kg!").
                        2. "profileUpdates": Optional JSON object matching fields in UserProfileEntity: level:Int, xp:Int, strength:Int, agility:Int, vitality:Int, intelligence:Int, sense:Int, statPoints:Int, benchPress:Float, squat:Float, deadlift:Float, overheadPress:Float.
                        3. "newQuest": Optional JSON object for a new quest: name:String, targetCount:Int.
                        4. "newWorkoutLog": Optional JSON object for a workout log: exerciseName:String, category:String, weight:Float, sets:Int, reps:Int.
                        Format your output strictly as a clean JSON object containing only these optional fields.
                    """.trimIndent()

                    val requestJson = org.json.JSONObject().apply {
                        put("contents", org.json.JSONArray().apply {
                            put(org.json.JSONObject().apply {
                                put("parts", org.json.JSONArray().apply {
                                    put(org.json.JSONObject().apply {
                                        put("text", instructions)
                                    })
                                })
                            })
                        })
                    }

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val requestBody = requestJson.toString().toRequestBody(mediaType)
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

                    val request = okhttp3.Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build()

                    val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        client.newCall(request).execute().use { response ->
                            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
                            val body = response.body?.string() ?: throw Exception("Empty response body")
                            val jsonRoot = org.json.JSONObject(body)
                            val cand = jsonRoot.getJSONArray("candidates").getJSONObject(0)
                            cand.getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
                        }
                    }

                    val cleanJsonStr = result.trim()
                        .removePrefix("```json")
                        .removePrefix("```")
                        .removeSuffix("```")
                        .trim()

                    val jsonResult = org.json.JSONObject(cleanJsonStr)
                    val explanation = jsonResult.optString("explanation", "Processed configuration update.")
                    
                    if (jsonResult.has("profileUpdates")) {
                        val pUp = jsonResult.getJSONObject("profileUpdates")
                        var updatedProfile = repository.getUserProfileOneShot() ?: profile
                        if (pUp.has("level")) updatedProfile = updatedProfile.copy(level = pUp.getInt("level"))
                        if (pUp.has("xp")) updatedProfile = updatedProfile.copy(xp = pUp.getInt("xp"))
                        if (pUp.has("strength")) updatedProfile = updatedProfile.copy(strength = pUp.getInt("strength"))
                        if (pUp.has("agility")) updatedProfile = updatedProfile.copy(agility = pUp.getInt("agility"))
                        if (pUp.has("vitality")) updatedProfile = updatedProfile.copy(vitality = pUp.getInt("vitality"))
                        if (pUp.has("intelligence")) updatedProfile = updatedProfile.copy(intelligence = pUp.getInt("intelligence"))
                        if (pUp.has("sense")) updatedProfile = updatedProfile.copy(sense = pUp.getInt("sense"))
                        if (pUp.has("statPoints")) updatedProfile = updatedProfile.copy(statPoints = pUp.getInt("statPoints"))
                        if (pUp.has("benchPress")) updatedProfile = updatedProfile.copy(benchPress = pUp.getDouble("benchPress").toFloat())
                        if (pUp.has("squat")) updatedProfile = updatedProfile.copy(squat = pUp.getDouble("squat").toFloat())
                        if (pUp.has("deadlift")) updatedProfile = updatedProfile.copy(deadlift = pUp.getDouble("deadlift").toFloat())
                        if (pUp.has("overheadPress")) updatedProfile = updatedProfile.copy(overheadPress = pUp.getDouble("overheadPress").toFloat())
                        repository.insertOrUpdateProfile(updatedProfile)
                    }

                    if (jsonResult.has("newQuest")) {
                        val nQ = jsonResult.getJSONObject("newQuest")
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                        val today = sdf.format(java.util.Date())
                        val quest = DailyQuestEntity(
                            date = today,
                            name = nQ.optString("name", "Custom Directive").uppercase(),
                            targetCount = nQ.optInt("targetCount", 100),
                            currentCount = 0,
                            isCompleted = false
                        )
                        repository.insertDailyQuest(quest)
                    }

                    if (jsonResult.has("newWorkoutLog")) {
                        val nW = jsonResult.getJSONObject("newWorkoutLog")
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                        val today = sdf.format(java.util.Date())
                        val log = WorkoutLogEntity(
                            date = today,
                            exerciseName = nW.optString("exerciseName", "Shadow Push Press"),
                            category = nW.optString("category", "Full Body"),
                            weight = nW.optDouble("weight", 60.0).toFloat(),
                            sets = nW.optInt("sets", 3),
                            reps = nW.optInt("reps", 10),
                            xpEarned = 25
                        )
                        repository.insertWorkoutLog(log)
                    }

                    adminResponseLog.value = adminResponseLog.value + "${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())} - SUCCEEDED: $explanation"
                    notifyMsg(explanation, "achievement")
                } catch (e: Exception) {
                    val errMsg = "Error processing administration decree: ${e.message}"
                    adminResponseLog.value = adminResponseLog.value + "${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())} - FAILED: $errMsg"
                    notifyMsg(errMsg, "warning")
                }
                adminProcessing.value = false
            }
        }
    }
}

data class AdminChatMessage(
    val sender: String, // "system" or "sovereign"
    val text: String,
    val timestamp: String = java.text.SimpleDateFormat("HH:mm", java.util.Locale.US).format(java.util.Date())
)

data class AiWorkoutExercise(
    val name: String,
    val sets: Int,
    val reps: String,
    val recommendedWeight: String,
    val description: String,
    val intensity: String
)

data class ActiveWorkoutSet(
    val id: Int,
    val reps: String,
    val weight: String
)

data class ActiveWorkoutExercise(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val sets: Int,
    val reps: Int,
    val weight: Float,
    val barWeight: Float,
    val isDone: Boolean = false,
    val setsList: List<ActiveWorkoutSet> = emptyList(),
    val aiIntensity: String = "Medium"
)
