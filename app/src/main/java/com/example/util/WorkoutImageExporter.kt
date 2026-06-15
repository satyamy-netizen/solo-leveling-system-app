package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.model.WorkoutLogEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object WorkoutImageExporter {

    fun generateWorkoutSummaryCard(
        username: String,
        date: String,
        logs: List<WorkoutLogEntity>,
        xpEarned: Int,
        goldEarned: Int
    ): Bitmap {
        // Dimensions: 1080 x 1350 (standard high card ratio - 4:5, great for sharing)
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw obsidian gradient background
        val backgroundPaint = Paint().apply {
            isAntiAlias = true
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                Color.parseColor("#090d16"), // Dark deep blue slate
                Color.parseColor("#020306"), // Obsidian black
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // Draw decorative thin neon borders matching Solo Theme (Hevy inspired)
        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#7c3aed") // SoloPurpleAccent
            style = Paint.Style.STROKE
            strokeWidth = 14f
        }
        canvas.drawRoundRect(20f, 20f, width - 20f, height - 20f, 32f, 32f, borderPaint)

        // Inner glowing secondary border
        val innerBorderPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#00f0ff") // SoloBlueAccent
            style = Paint.Style.STROKE
            strokeWidth = 3f
            alpha = 130
        }
        canvas.drawRoundRect(35f, 35f, width - 35f, height - 35f, 24f, 24f, innerBorderPaint)

        // Paint for texts
        val textPaint = Paint().apply {
            isAntiAlias = true
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }

        // 1. Draw Title
        textPaint.color = Color.parseColor("#00f0ff")
        textPaint.textSize = 44f
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("[ DYNAMIC REGISTRY: WORKOUT RATIFICATION ]", width / 2f, 130f, textPaint)

        // Subtitle line
        textPaint.color = Color.WHITE
        textPaint.textSize = 26f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        canvas.drawText("CRITICAL PROGRESS REAWAKENING REPORT", width / 2f, 180f, textPaint)

        // Horizontal partition line
        val linePaint = Paint().apply {
            color = Color.parseColor("#121824")
            strokeWidth = 4f
        }
        canvas.drawLine(100f, 220f, width - 100f, 220f, linePaint)

        // 2. Draw Hunter Profile Info Card
        val cardPaint = Paint().apply {
            color = Color.parseColor("#121824") // SoloDarkGrey
            style = Paint.Style.FILL
        }
        val cardBorderPaint = Paint().apply {
            color = Color.parseColor("#313d4f")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        // Draw User / Info Box
        canvas.drawRoundRect(100f, 260f, width - 100f, 420f, 16f, 16f, cardPaint)
        canvas.drawRoundRect(100f, 260f, width - 100f, 420f, 16f, 16f, cardBorderPaint)

        // Hunter Info Text
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.parseColor("#9ca3af") // grey
        textPaint.textSize = 24f
        canvas.drawText("HUNTER DOCK NAME", 140f, 310f, textPaint)
        textPaint.color = Color.WHITE
        textPaint.textSize = 36f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(username.uppercase(), 140f, 365f, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.color = Color.parseColor("#9ca3af")
        textPaint.textSize = 24f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        canvas.drawText("COVENANT TIMESTAMP", width - 140f, 310f, textPaint)
        textPaint.color = Color.parseColor("#00f0ff")
        textPaint.textSize = 30f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(date, width - 140f, 365f, textPaint)

        // 3. STATS HIGHLIGHTS (Volumes & Lift Achievements)
        var totalVolumeKg = 0f
        var maxWeightSingle = 0f
        var totalSets = 0

        logs.forEach { log ->
            val setSumVolume = log.weight * log.sets * log.reps
            totalVolumeKg += setSumVolume
            totalSets += log.sets
            if (log.weight > maxWeightSingle) {
                maxWeightSingle = log.weight
            }
        }

        // Layout three statistics cards in a single row
        val cellWidth = (width - 240f) / 3f
        val startY = 470f
        val cellHeight = 180f

        // Stats Box 1: Total Volume
        val boxYEnd = startY + cellHeight
        drawStatBox(canvas, 100f, startY, 100f + cellWidth, boxYEnd, "TOTAL VOLUME", "${totalVolumeKg.toInt()} kg", "#00f0ff")
        // Stats Box 2: Max Single Lift
        drawStatBox(canvas, 100f + cellWidth + 20f, startY, 100f + 2 * cellWidth + 20f, boxYEnd, "MAX SINGLE LIFT", "${maxWeightSingle.toInt()} kg", "#7c3aed")
        // Stats Box 3: Total Sets
        drawStatBox(canvas, 100f + 2 * cellWidth + 40f, startY, width - 100f, boxYEnd, "TOTAL EXECUTION", "$totalSets Sets", "#10b981")

        // 4. Completed Exercises details checklist
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.parseColor("#9ca3af")
        textPaint.textSize = 24f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText("LOGGED COMPLETED PROTOCOLS:", 100f, 720f, textPaint)

        var currentLogY = 780f
        val itemPaint = Paint().apply {
            isAntiAlias = true
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            color = Color.WHITE
        }
        val categoryPaint = Paint().apply {
            isAntiAlias = true
            textSize = 20f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            color = Color.parseColor("#00f0ff")
        }
        val detailPaint = Paint().apply {
            isAntiAlias = true
            textSize = 24f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            color = Color.parseColor("#9ca3af")
        }

        // Draw up to 5 logged exercises
        val limitedLogs = logs.take(5)
        limitedLogs.forEach { log ->
            // Draw bullet circle
            val bulletPaint = Paint().apply {
                color = Color.parseColor("#7c3aed")
                style = Paint.Style.FILL
            }
            canvas.drawCircle(115f, currentLogY - 10f, 8f, bulletPaint)

            // Exercise Name
            canvas.drawText(log.exerciseName.uppercase(), 145f, currentLogY, itemPaint)
            
            // Category tag
            canvas.drawText(" [${log.category.uppercase()}]", 145f + itemPaint.measureText(log.exerciseName.uppercase() + " "), currentLogY, categoryPaint)

            // Log details right-aligned
            val detailStr = "${log.sets} sets x ${log.reps} reps @ ${log.weight.toInt()}kg"
            canvas.drawText(detailStr, width - 120f, currentLogY, detailPaint)

            // Thin divider lines
            val dividerPaint = Paint().apply {
                color = Color.parseColor("#1e293b")
                strokeWidth = 2f
            }
            canvas.drawLine(100f, currentLogY + 20f, width - 100f, currentLogY + 20f, dividerPaint)

            currentLogY += 75f
        }

        if (logs.size > 5) {
            textPaint.color = Color.parseColor("#9ca3af")
            textPaint.textSize = 22f
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("... and ${logs.size - 5} other workout sets finalized ...", width / 2f, currentLogY + 10f, textPaint)
        }

        // 5. XP and rewards display bar
        val rewardsY = 1130f
        canvas.drawRoundRect(100f, rewardsY, width - 100f, rewardsY + 100f, 12f, 12f, cardPaint)
        canvas.drawRoundRect(100f, rewardsY, width - 100f, rewardsY + 100f, 12f, 12f, cardBorderPaint)

        val rewardTextPaint = Paint().apply {
            isAntiAlias = true
            textSize = 24f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            color = Color.parseColor("#10b981")
            textAlign = Paint.Align.CENTER
        }
        val rewardMessage = "SYSTEM UPGRADE SUCCESS COMPLETED: +$xpEarned XP • HEVY PROTOCOL ON"
        canvas.drawText(rewardMessage, width / 2f, rewardsY + 58f, rewardTextPaint)

        // 6. Signature watermark at the bottom
        val footerPaint = Paint().apply {
            isAntiAlias = true
            textSize = 21f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            color = Color.parseColor("#4b5563")
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("RATIFIED SECURE BY S-RANK SYSTEM • HEVY ACTIVE EXPORT", width / 2f, height - 60f, footerPaint)

        return bitmap
    }

    private fun drawStatBox(canvas: Canvas, xStart: Float, yStart: Float, xEnd: Float, yEnd: Float, title: String, value: String, accentColorHex: String) {
        val fillPaint = Paint().apply {
            color = Color.parseColor("#0f172a") // Card interior
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#1e293b")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val accentIndicatorPaint = Paint().apply {
            color = Color.parseColor(accentColorHex)
            style = Paint.Style.FILL
        }

        canvas.drawRoundRect(xStart, yStart, xEnd, yEnd, 12f, 12f, fillPaint)
        canvas.drawRoundRect(xStart, yStart, xEnd, yEnd, 12f, 12f, borderPaint)
        
        // Horizontal left-marker bar
        canvas.drawRoundRect(xStart, yStart, xStart + 8f, yEnd, 4f, 4f, accentIndicatorPaint)

        val textPaint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // Draw title
        textPaint.color = Color.parseColor("#9ca3af")
        textPaint.textSize = 14f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        canvas.drawText(title, (xStart + xEnd) / 2f, yStart + 50f, textPaint)

        // Draw big value
        textPaint.color = Color.WHITE
        textPaint.textSize = 30f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(value, (xStart + xEnd) / 2f, yStart + 115f, textPaint)
    }

    // Fully scoped-storage safe image saver
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String): File? {
        val filename = "HEVY_SHARE_${title.replace(" ", "_")}_${System.currentTimeMillis()}.png"
        var outStream: OutputStream? = null
        var file: File? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SoloLevelingTrack")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val imageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (imageUri != null) {
                    outStream = context.contentResolver.openOutputStream(imageUri)
                    if (outStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                        values.clear()
                        values.put(MediaStore.Images.Media.IS_PENDING, 0)
                        context.contentResolver.update(imageUri, values, null, null)
                    }
                }
            } else {
                // Fallback for older APIs
                val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
                val imageFile = File(dir, filename)
                outStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                file = imageFile
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                outStream?.flush()
                outStream?.close()
            } catch (e: Exception) {
                // ignore
            }
        }
        return file
    }

    fun generateLevelUpCard(
        username: String,
        level: Int,
        statsText: String
    ): Bitmap {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw obsidian gradient background
        val backgroundPaint = Paint().apply {
            isAntiAlias = true
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                Color.parseColor("#110a24"), // Deep cyber violet
                Color.parseColor("#030209"), // Void black
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // Draw decorative thin neon borders matching Solo Theme (Cyberpunk inspired)
        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#fcee09") // Cyber yellow
            style = Paint.Style.STROKE
            strokeWidth = 14f
        }
        canvas.drawRoundRect(20f, 20f, width - 20f, height - 20f, 32f, 32f, borderPaint)

        // Inner glowing secondary border
        val innerBorderPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#00f0ff") // Cyber cyan
            style = Paint.Style.STROKE
            strokeWidth = 4f
            alpha = 180
        }
        canvas.drawRoundRect(35f, 35f, width - 35f, height - 35f, 24f, 24f, innerBorderPaint)

        // Paint for texts
        val textPaint = Paint().apply {
            isAntiAlias = true
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }

        // Title
        textPaint.color = Color.parseColor("#fcee09")
        textPaint.textSize = 50f
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("⚡ SYSTEM ASCENSION DECREE ⚡", width / 2f, 150f, textPaint)

        // Subtitle line
        textPaint.color = Color.WHITE
        textPaint.textSize = 28f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        canvas.drawText("S-RANK HUNTER SYSTEM EVOLUTION", width / 2f, 210f, textPaint)

        // Horizontal partition line
        val linePaint = Paint().apply {
            color = Color.parseColor("#3b3b11") // Dim yellow/brown
            strokeWidth = 4f
        }
        canvas.drawLine(100f, 250f, width - 100f, 250f, linePaint)

        // Big level badge
        val cardPaint = Paint().apply {
            color = Color.parseColor("#05070c") // Void black card interior
            style = Paint.Style.FILL
        }
        val cardBorderPaint = Paint().apply {
            color = Color.parseColor("#fcee09")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        canvas.drawRoundRect(width / 2f - 200f, 320f, width / 2f + 200f, 520f, 16f, 16f, cardPaint)
        canvas.drawRoundRect(width / 2f - 200f, 320f, width / 2f + 200f, 520f, 16f, 16f, cardBorderPaint)

        // Draw level text
        textPaint.color = Color.parseColor("#fcee09")
        textPaint.textSize = 80f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText("LEVEL $level", width / 2f, 440f, textPaint)

        // Congratulations box
        canvas.drawRoundRect(100f, 580f, width - 100f, 960f, 16f, 16f, cardPaint)
        val secondBorderPaint = Paint().apply {
            color = Color.parseColor("#00f0ff")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(100f, 580f, width - 100f, 960f, 16f, 16f, secondBorderPaint)

        // Details
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = Color.parseColor("#00f0ff")
        textPaint.textSize = 34f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText("HUNTER PROTOCOL CONFIRMED", width / 2f, 650f, textPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 28f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        canvas.drawText("RANK ASCENDED FOR REGISTRANT:", width / 2f, 720f, textPaint)

        textPaint.color = Color.parseColor("#fcee09")
        textPaint.textSize = 42f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText(username.uppercase(), width / 2f, 790f, textPaint)

        textPaint.color = Color.parseColor("#9ca3af")
        textPaint.textSize = 24f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        canvas.drawText("ACTIVE COORDINATES & ENERGY METRIC STATUS:", width / 2f, 850f, textPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 28f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText(statsText, width / 2f, 910f, textPaint)

        // Rewards bar
        val rewardsY = 1020f
        canvas.drawRoundRect(100f, rewardsY, width - 100f, rewardsY + 120f, 12f, 12f, cardPaint)
        canvas.drawRoundRect(100f, rewardsY, width - 100f, rewardsY + 120f, 12f, 12f, secondBorderPaint)

        textPaint.color = Color.parseColor("#10b981")
        textPaint.textSize = 24f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText("SOLO UPGRADE: +5 MANUAL STATUS STAT POINTS GRANTED", width / 2f, rewardsY + 70f, textPaint)

        // Footer signature
        textPaint.color = Color.parseColor("#4b5563")
        textPaint.textSize = 22f
        textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        canvas.drawText("RATIFIED SECURE BY S-RANK SYSTEM ARCHITECT CORE", width / 2f, height - 70f, textPaint)

        return bitmap
    }
}
