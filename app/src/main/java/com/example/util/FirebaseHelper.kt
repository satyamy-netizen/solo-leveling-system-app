package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.model.*
import com.example.data.repository.SystemRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object FirebaseHelper {
    private const val TAG = "FirebaseHelper"

    // Safe checker if Firebase is initialized and ready
    fun isAvailable(context: Context): Boolean {
        return try {
            val app = FirebaseApp.getInstance()
            if (app == null) {
                false
            } else {
                FirebaseAuth.getInstance()
                FirebaseFirestore.getInstance()
                true
            }
        } catch (e: Exception) {
            try {
                val app = FirebaseApp.initializeApp(context)
                if (app == null) {
                    false
                } else {
                    FirebaseAuth.getInstance()
                    FirebaseFirestore.getInstance()
                    true
                }
            } catch (ex: Exception) {
                Log.w(TAG, "Firebase SDK not fully configured or missing google-services.json: ${ex.message}")
                false
            }
        }
    }

    // Auth: Email & Password Sign-Up (Create Account)
    suspend fun signUpWithEmail(context: Context, email: String, password: String): String? = withContext(Dispatchers.IO) {
        if (!isAvailable(context)) {
            Log.w(TAG, "Running in Local Offline Sandboxed mode (Firebase not available)")
            return@withContext null
        }
        try {
            val auth = FirebaseAuth.getInstance()
            val task = auth.createUserWithEmailAndPassword(email, password)
            val result = Tasks.await(task, 15, TimeUnit.SECONDS)
            result.user?.uid
        } catch (e: Exception) {
            Log.e(TAG, "Firebase SignUp Error: ${e.message}")
            throw e
        }
    }

    // Auth: Email & Password Sign-In
    suspend fun signInWithEmail(context: Context, email: String, password: String): String? = withContext(Dispatchers.IO) {
        if (!isAvailable(context)) {
            Log.w(TAG, "Running in Local Offline Sandboxed mode (Firebase not available)")
            return@withContext null
        }
        try {
            val auth = FirebaseAuth.getInstance()
            val task = auth.signInWithEmailAndPassword(email, password)
            val result = Tasks.await(task, 15, TimeUnit.SECONDS)
            result.user?.uid
        } catch (e: Exception) {
            Log.e(TAG, "Firebase SignIn Error: ${e.message}")
            throw e
        }
    }

    // Auth: Google Sign-In with Id Token
    suspend fun signInWithGoogleToken(context: Context, idToken: String): String? = withContext(Dispatchers.IO) {
        if (!isAvailable(context)) {
            Log.w(TAG, "Running in Local Offline Sandboxed mode (Firebase not available)")
            return@withContext null
        }
        try {
            val auth = FirebaseAuth.getInstance()
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val task = auth.signInWithCredential(credential)
            val result = Tasks.await(task, 15, TimeUnit.SECONDS)
            result.user?.uid
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Google Auth Error: ${e.message}")
            throw e
        }
    }

    // Firestore: Sync all local Room databases up to cloud Firestore
    suspend fun syncLocalToFirestore(context: Context, repository: SystemRepository): Boolean = withContext(Dispatchers.IO) {
        if (!isAvailable(context)) return@withContext false
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser ?: return@withContext false
        val uid = currentUser.uid
        val db = FirebaseFirestore.getInstance()

        try {
            // 1. Sync User Profile details
            val profile = repository.getUserProfileOneShot()
            if (profile != null) {
                val profileMap = hashMapOf(
                    "name" to profile.name,
                    "email" to profile.email,
                    "age" to profile.age,
                    "experience" to profile.experience,
                    "intensity" to profile.intensity,
                    "equipment" to profile.equipment,
                    "level" to profile.level,
                    "xp" to profile.xp,
                    "strength" to profile.strength,
                    "agility" to profile.agility,
                    "vitality" to profile.vitality,
                    "intelligence" to profile.intelligence,
                    "sense" to profile.sense,
                    "statPoints" to profile.statPoints,
                    "gold" to profile.gold,
                    "streak" to profile.streak,
                    "lastQuestDate" to profile.lastQuestDate,
                    "soundAlertTime" to profile.soundAlertTime,
                    "benchPress" to profile.benchPress,
                    "squat" to profile.squat,
                    "deadlift" to profile.deadlift,
                    "overheadPress" to profile.overheadPress
                )
                val setTask = db.collection("users").document(uid).collection("profile").document("info")
                    .set(profileMap, SetOptions.merge())
                Tasks.await(setTask, 10, TimeUnit.SECONDS)
            }

            // 2. Sync Workout Logs
            val workoutLogs = repository.allWorkoutLogs.firstOrNull() ?: emptyList()
            for (log in workoutLogs) {
                val logMap = hashMapOf(
                    "id" to log.id,
                    "date" to log.date,
                    "exerciseName" to log.exerciseName,
                    "category" to log.category,
                    "weight" to log.weight,
                    "sets" to log.sets,
                    "reps" to log.reps,
                    "xpEarned" to log.xpEarned
                )
                val task = db.collection("users").document(uid).collection("workoutLogs")
                    .document(log.id.toString()).set(logMap, SetOptions.merge())
                Tasks.await(task, 10, TimeUnit.SECONDS)
            }

            // 3. Sync Body Measurements
            val bodyMeasurements = repository.allBodyMeasurements.firstOrNull() ?: emptyList()
            for (m in bodyMeasurements) {
                val mMap = hashMapOf(
                    "id" to m.id,
                    "date" to m.date,
                    "weight" to m.weight,
                    "muscleMass" to m.muscleMass,
                    "fatPercentage" to m.fatPercentage,
                    "chest" to m.chest,
                    "arms" to m.arms,
                    "waist" to m.waist
                )
                val task = db.collection("users").document(uid).collection("bodyMeasurements")
                    .document(m.id.toString()).set(mMap, SetOptions.merge())
                Tasks.await(task, 10, TimeUnit.SECONDS)
            }

            // 4. Sync Earned Badges
            val badges = repository.getEarnedBadgesOneShot()
            for (b in badges) {
                val bMap = hashMapOf(
                    "badgeId" to b.badgeId,
                    "name" to b.name,
                    "description" to b.description,
                    "earnedDate" to b.earnedDate,
                    "iconName" to b.iconName
                )
                val task = db.collection("users").document(uid).collection("badges")
                    .document(b.badgeId).set(bMap, SetOptions.merge())
                Tasks.await(task, 10, TimeUnit.SECONDS)
            }

            Log.i(TAG, "Full Firestore Backup Sync successful for: $uid")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Firestore sync failed: ${e.message}")
            false
        }
    }

    // Firestore: Pull cloud data from Firestore and populate the Local Room Database
    suspend fun restoreFromFirestore(context: Context, repository: SystemRepository): Boolean = withContext(Dispatchers.IO) {
        if (!isAvailable(context)) return@withContext false
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser ?: return@withContext false
        val uid = currentUser.uid
        val db = FirebaseFirestore.getInstance()

        try {
            // Restore User Profile info
            val profileDocTask = db.collection("users").document(uid).collection("profile").document("info").get()
            val profileDoc = Tasks.await(profileDocTask, 10, TimeUnit.SECONDS)
            if (profileDoc.exists()) {
                val profile = UserProfileEntity(
                    id = 1,
                    name = profileDoc.getString("name") ?: "Awakened Hunter",
                    email = profileDoc.getString("email") ?: currentUser.email ?: "",
                    age = profileDoc.getLong("age")?.toInt() ?: 24,
                    experience = profileDoc.getString("experience") ?: "Beginner",
                    intensity = profileDoc.getString("intensity") ?: "Medium",
                    equipment = profileDoc.getString("equipment") ?: "Full Gym",
                    level = profileDoc.getLong("level")?.toInt() ?: 1,
                    xp = profileDoc.getLong("xp")?.toInt() ?: 0,
                    strength = profileDoc.getLong("strength")?.toInt() ?: 10,
                    agility = profileDoc.getLong("agility")?.toInt() ?: 10,
                    vitality = profileDoc.getLong("vitality")?.toInt() ?: 10,
                    intelligence = profileDoc.getLong("intelligence")?.toInt() ?: 10,
                    sense = profileDoc.getLong("sense")?.toInt() ?: 10,
                    statPoints = profileDoc.getLong("statPoints")?.toInt() ?: 0,
                    gold = profileDoc.getLong("gold")?.toInt() ?: 100,
                    streak = profileDoc.getLong("streak")?.toInt() ?: 0,
                    lastQuestDate = profileDoc.getString("lastQuestDate") ?: "",
                    soundAlertTime = profileDoc.getString("soundAlertTime") ?: "08:00 AM",
                    benchPress = profileDoc.getDouble("benchPress")?.toFloat() ?: 0f,
                    squat = profileDoc.getDouble("squat")?.toFloat() ?: 0f,
                    deadlift = profileDoc.getDouble("deadlift")?.toFloat() ?: 0f,
                    overheadPress = profileDoc.getDouble("overheadPress")?.toFloat() ?: 0f
                )
                repository.insertOrUpdateProfile(profile)
            }

            // Restore Workout Logs
            val workoutDocsTask = db.collection("users").document(uid).collection("workoutLogs").get()
            val workoutDocs = Tasks.await(workoutDocsTask, 10, TimeUnit.SECONDS)
            for (doc in workoutDocs.documents) {
                val log = WorkoutLogEntity(
                    id = doc.getLong("id")?.toInt() ?: 0,
                    date = doc.getString("date") ?: "",
                    exerciseName = doc.getString("exerciseName") ?: "",
                    category = doc.getString("category") ?: "",
                    weight = doc.getDouble("weight")?.toFloat() ?: 0f,
                    sets = doc.getLong("sets")?.toInt() ?: 0,
                    reps = doc.getLong("reps")?.toInt() ?: 0,
                    xpEarned = doc.getLong("xpEarned")?.toInt() ?: 0
                )
                repository.insertWorkoutLog(log)
            }

            // Restore Measurements
            val measurementDocsTask = db.collection("users").document(uid).collection("bodyMeasurements").get()
            val measurementDocs = Tasks.await(measurementDocsTask, 10, TimeUnit.SECONDS)
            for (doc in measurementDocs.documents) {
                val m = BodyMeasurementEntity(
                    id = doc.getLong("id")?.toInt() ?: 0,
                    date = doc.getString("date") ?: "",
                    weight = doc.getDouble("weight")?.toFloat() ?: 0f,
                    muscleMass = doc.getDouble("muscleMass")?.toFloat() ?: 0f,
                    fatPercentage = doc.getDouble("fatPercentage")?.toFloat() ?: 0f,
                    chest = doc.getDouble("chest")?.toFloat() ?: 0f,
                    arms = doc.getDouble("arms")?.toFloat() ?: 0f,
                    waist = doc.getDouble("waist")?.toFloat() ?: 0f
                )
                repository.insertBodyMeasurement(m)
            }

            // Restore Badges
            val badgeDocsTask = db.collection("users").document(uid).collection("badges").get()
            val badgeDocs = Tasks.await(badgeDocsTask, 10, TimeUnit.SECONDS)
            for (doc in badgeDocs.documents) {
                val b = BadgeEntity(
                    badgeId = doc.getString("badgeId") ?: "",
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description") ?: "",
                    earnedDate = doc.getString("earnedDate") ?: "",
                    iconName = doc.getString("iconName") ?: ""
                )
                repository.insertBadge(b)
            }

            Log.i(TAG, "Full Firestore Sync Restore successful for: $uid")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Firestore restore failed: ${e.message}")
            false
        }
    }
}
