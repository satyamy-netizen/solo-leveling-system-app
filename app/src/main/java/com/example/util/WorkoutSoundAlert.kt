package com.example.util

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

object WorkoutSoundAlert {
    fun playSystemWarningSound(intensity: String) {
        try {
            // Pick sound tone frequency and duration based on Solo Leveling system intensity
            // High Intensity: Triple high-pitch danger tone
            // Medium Intensity: Double classic system alert tone
            // Low Intensity: Single simple notice tone
            val toneType = when (intensity.lowercase()) {
                "high" -> ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK
                "medium" -> ToneGenerator.TONE_CDMA_CONFIRM
                else -> ToneGenerator.TONE_PROP_BEEP
            }
            
            val duration = when (intensity.lowercase()) {
                "high" -> 800
                "medium" -> 500
                else -> 300
            }

            val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneGenerator.startTone(toneType, duration)
            
            // For extreme systems (High), play a secondary tone after a short delay
            if (intensity.lowercase() == "high") {
                Thread {
                    try {
                        Thread.sleep(600)
                        val extraTone = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                        extraTone.startTone(ToneGenerator.TONE_SUP_ERROR, 400)
                    } catch (e: Exception) {
                        Log.e("SoundAlert", "Secondary sound failed: ${e.message}")
                    }
                }.start()
            }
        } catch (e: Exception) {
            Log.e("SoundAlert", "Sound playback failed: ${e.message}")
        }
    }

    // New specific achievement triumphant synthetic sweep! (ascending pitch)
    fun playSystemAchievementSound() {
        Thread {
            try {
                val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                Thread.sleep(180)
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 150)
                Thread.sleep(180)
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 250)
            } catch (e: Exception) {
                Log.e("SoundAlert", "Achievement sound failed: ${e.message}")
            }
        }.start()
    }

    // New warning/penalty alert synth beep sequence! (descending alarm tones)
    fun playSystemWarningAlertSound() {
        Thread {
            try {
                val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                toneGenerator.startTone(ToneGenerator.TONE_SUP_ERROR, 350)
                Thread.sleep(400)
                toneGenerator.startTone(ToneGenerator.TONE_SUP_ERROR, 350)
            } catch (e: Exception) {
                Log.e("SoundAlert", "Warning alert sound failed: ${e.message}")
            }
        }.start()
    }

    // High-tech ascending game-system Level Up melodic sequence
    fun playLevelUpSound() {
        Thread {
            try {
                val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                Thread.sleep(120)
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 150)
                Thread.sleep(170)
                toneGenerator.startTone(ToneGenerator.TONE_SUP_PIP, 200)
                Thread.sleep(220)
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 350)
            } catch (e: Exception) {
                Log.e("SoundAlert", "Level up sound failed: ${e.message}")
            }
        }.start()
    }
}
