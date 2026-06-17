package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemDao {

    // --- User Profile ---
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOneShot(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    // --- Body Focus Split ---
    @Query("SELECT * FROM body_focus ORDER BY dayOfWeek ASC")
    fun getBodyFocusSplit(): Flow<List<BodyFocusEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyFocus(focus: BodyFocusEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyFocusList(list: List<BodyFocusEntity>)

    // --- Daily Quests ---
    @Query("SELECT * FROM daily_quest WHERE date = :date")
    fun getDailyQuests(date: String): Flow<List<DailyQuestEntity>>

    @Query("SELECT * FROM daily_quest")
    fun getAllDailyQuests(): Flow<List<DailyQuestEntity>>

    @Query("SELECT * FROM daily_quest WHERE date = :date")
    suspend fun getDailyQuestsOneShot(date: String): List<DailyQuestEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyQuest(quest: DailyQuestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyQuests(quests: List<DailyQuestEntity>)

    @Update
    suspend fun updateDailyQuest(quest: DailyQuestEntity)

    // --- Workout Logs ---
    @Query("SELECT * FROM workout_log ORDER BY date DESC, id DESC")
    fun getAllWorkoutLogs(): Flow<List<WorkoutLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(log: WorkoutLogEntity)

    @Query("DELETE FROM workout_log WHERE id = :id")
    suspend fun deleteWorkoutLog(id: Int)

    // --- Body Measurements ---
    @Query("SELECT * FROM body_measurement ORDER BY date DESC, id DESC")
    fun getAllBodyMeasurements(): Flow<List<BodyMeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyMeasurement(measurement: BodyMeasurementEntity)

    @Query("DELETE FROM body_measurement WHERE id = :id")
    suspend fun deleteBodyMeasurement(id: Int)

    // --- Badges ---
    @Query("SELECT * FROM badge")
    fun getEarnedBadges(): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM badge")
    suspend fun getEarnedBadgesOneShot(): List<BadgeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: BadgeEntity)

    // --- Dynamic Quest Miss Penalties ---
    @Query("SELECT DISTINCT date FROM daily_quest WHERE date < :today")
    suspend fun getOlderQuestDates(today: String): List<String>

    // --- Exercise Dictionary (Hevy-style) ---
    @Query("SELECT * FROM exercise ORDER BY name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercise ORDER BY name ASC")
    suspend fun getAllExercisesOneShot(): List<ExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Query("DELETE FROM exercise WHERE name = :name")
    suspend fun deleteExercise(name: String)

    // --- Afterlife Social Feed ---
    @Query("SELECT * FROM afterlife_post ORDER BY id DESC")
    fun getAllAfterlifePosts(): Flow<List<AfterlifePostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAfterlifePost(post: AfterlifePostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAfterlifePosts(posts: List<AfterlifePostEntity>)

    @Update
    suspend fun updateAfterlifePost(post: AfterlifePostEntity)

    @Query("DELETE FROM afterlife_post WHERE id = :id")
    suspend fun deleteAfterlifePost(id: Int)
}
