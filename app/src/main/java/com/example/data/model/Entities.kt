package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val email: String,
    val age: Int,
    val experience: String, // "Beginner", "Intermediate", "Advanced"
    val intensity: String,  // "Low", "Medium", "High"
    val equipment: String,  // "Full Gym", "Dumbbells Only", "Bodyweight"
    val level: Int = 1,
    val xp: Int = 0,
    val strength: Int = 10,
    val agility: Int = 10,
    val vitality: Int = 10,
    val intelligence: Int = 10,
    val sense: Int = 10,
    val statPoints: Int = 0,
    val gold: Int = 100, // custom mana or gold coins representing Solo Leveling gold reward
    val streak: Int = 0,
    val lastQuestDate: String = "", // YYYY-MM-DD
    val soundAlertTime: String = "08:00", // Time of day for workout alert preference
    val benchPress: Float = 0f,
    val squat: Float = 0f,
    val deadlift: Float = 0f,
    val overheadPress: Float = 0f
)

@Entity(tableName = "body_focus")
data class BodyFocusEntity(
    @PrimaryKey val dayOfWeek: Int, // 1 = Mon, 2 = Tue, 3 = Wed, 4 = Thu, 5 = Fri, 6 = Sat, 7 = Sun
    val focusPart: String // e.g., "Chest & Triceps", "Back & Biceps", "Legs & Shoulders", "Abs & Cardio", "Full Body", "Rest Day"
)

@Entity(tableName = "daily_quest")
data class DailyQuestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val name: String, // E.g., "Push-ups", "Sit-ups", "Squats", "Run (meters)"
    val targetCount: Int,
    val currentCount: Int,
    val isCompleted: Boolean = false
)

@Entity(tableName = "workout_log")
data class WorkoutLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val exerciseName: String,
    val category: String, // "Chest", "Back", etc.
    val weight: Float, // in kg
    val sets: Int,
    val reps: Int,
    val xpEarned: Int,
    val barWeight: Float = 20f, // Default standard barbell weight is 20kg
    val intensity: String = "Medium" // "Low", "Medium", "High"
)

@Entity(tableName = "body_measurement")
data class BodyMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val weight: Float, // kg
    val muscleMass: Float, // kg
    val fatPercentage: Float, // %
    val chest: Float, // cm
    val arms: Float, // cm
    val waist: Float // cm
)

@Entity(tableName = "badge")
data class BadgeEntity(
    @PrimaryKey val badgeId: String, // e.g. "STREAK_7", "PERFECT_WEEK", "LVL_UP", "QUEST_1"
    val name: String,
    val description: String,
    val earnedDate: String, // YYYY-MM-DD
    val iconName: String // string descriptor for icon representation
)

@Entity(tableName = "exercise")
data class ExerciseEntity(
    @PrimaryKey val name: String,
    val category: String, // "Chest", "Biceps", "Calves", "Triceps", "Back", "Shoulders", "Legs", "Abdominals", "Full Body"
    val equipmentRequired: String, // "None", "Barbell", "Dumbbell", "Kettlebell", "Machine", "Plate", "Resistance Band"
    val baseSets: Int = 3,
    val baseReps: String = "10",
    val description: String = "",
    val isCustom: Boolean = false
)

@Entity(tableName = "afterlife_post")
data class AfterlifePostEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val authorName: String,
    val authorRank: String,
    val content: String,
    val guildName: String,
    val dateStr: String,
    val likes: Int = 0,
    val isLiked: Boolean = false,
    val exerciseTag: String = ""
)

