package com.example.data.repository

import com.example.data.database.SystemDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class SystemRepository(private val systemDao: SystemDao) {

    val userProfile: Flow<UserProfileEntity?> = systemDao.getUserProfile()
    val bodyFocusSplit: Flow<List<BodyFocusEntity>> = systemDao.getBodyFocusSplit()
    val allWorkoutLogs: Flow<List<WorkoutLogEntity>> = systemDao.getAllWorkoutLogs()
    val allBodyMeasurements: Flow<List<BodyMeasurementEntity>> = systemDao.getAllBodyMeasurements()
    val earnedBadges: Flow<List<BadgeEntity>> = systemDao.getEarnedBadges()
    val allDailyQuestsHistory: Flow<List<DailyQuestEntity>> = systemDao.getAllDailyQuests()

    suspend fun getUserProfileOneShot(): UserProfileEntity? = systemDao.getUserProfileOneShot()

    suspend fun insertOrUpdateProfile(profile: UserProfileEntity) {
        systemDao.insertOrUpdateProfile(profile)
    }

    suspend fun insertBodyFocus(focus: BodyFocusEntity) {
        systemDao.insertBodyFocus(focus)
    }

    suspend fun insertBodyFocusList(list: List<BodyFocusEntity>) {
        systemDao.insertBodyFocusList(list)
    }

    fun getDailyQuests(date: String): Flow<List<DailyQuestEntity>> {
        return systemDao.getDailyQuests(date)
    }

    suspend fun getDailyQuestsOneShot(date: String): List<DailyQuestEntity> {
        return systemDao.getDailyQuestsOneShot(date)
    }

    suspend fun insertDailyQuest(quest: DailyQuestEntity) {
        systemDao.insertDailyQuest(quest)
    }

    suspend fun insertDailyQuests(quests: List<DailyQuestEntity>) {
        systemDao.insertDailyQuests(quests)
    }

    suspend fun updateDailyQuest(quest: DailyQuestEntity) {
        systemDao.updateDailyQuest(quest)
    }

    suspend fun insertWorkoutLog(log: WorkoutLogEntity) {
        systemDao.insertWorkoutLog(log)
    }

    suspend fun deleteWorkoutLog(id: Int) {
        systemDao.deleteWorkoutLog(id)
    }

    suspend fun insertBodyMeasurement(measurement: BodyMeasurementEntity) {
        systemDao.insertBodyMeasurement(measurement)
    }

    suspend fun deleteBodyMeasurement(id: Int) {
        systemDao.deleteBodyMeasurement(id)
    }

    suspend fun getEarnedBadgesOneShot(): List<BadgeEntity> {
        return systemDao.getEarnedBadgesOneShot()
    }

    suspend fun insertBadge(badge: BadgeEntity) {
        systemDao.insertBadge(badge)
    }

    // --- Dynamic Quest Miss Penalties ---
    suspend fun getOlderQuestDates(today: String): List<String> {
        return systemDao.getOlderQuestDates(today)
    }

    // --- Exercise Dictionary (Hevy-style) ---
    val allExercises: Flow<List<ExerciseEntity>> = systemDao.getAllExercises()

    suspend fun getAllExercisesOneShot(): List<ExerciseEntity> {
        return systemDao.getAllExercisesOneShot()
    }

    suspend fun insertExercises(exercises: List<ExerciseEntity>) {
        systemDao.insertExercises(exercises)
    }

    suspend fun insertExercise(exercise: ExerciseEntity) {
        systemDao.insertExercise(exercise)
    }

    suspend fun deleteExercise(name: String) {
        systemDao.deleteExercise(name)
    }
}
