package com.example.ui

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class LogSeverity {
    INFO, WARNING, ERROR, CRITICAL
}

data class LogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val timeLabel: String = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date(timestamp)),
    val tag: String,
    val message: String,
    val exceptionMessage: String? = null,
    val stackTrace: String? = null,
    val severity: LogSeverity
)

object SoloErrorLogger {
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private const val MAX_LOGS = 100

    fun log(severity: LogSeverity, tag: String, message: String, throwable: Throwable? = null) {
        val stackTraceString = throwable?.stackTraceToString()
        val exceptionMessage = throwable?.localizedMessage ?: throwable?.message

        // Log to standard Android logcat first
        val formattedMessage = if (exceptionMessage != null) "$message | Exception: $exceptionMessage" else message
        when (severity) {
            LogSeverity.INFO -> Log.i(tag, formattedMessage)
            LogSeverity.WARNING -> Log.w(tag, formattedMessage, throwable)
            LogSeverity.ERROR -> Log.e(tag, formattedMessage, throwable)
            LogSeverity.CRITICAL -> Log.e(tag, "CRITICAL: $formattedMessage", throwable)
        }

        val entry = LogEntry(
            tag = tag,
            message = message,
            exceptionMessage = exceptionMessage,
            stackTrace = stackTraceString,
            severity = severity
        )

        synchronized(this) {
            val currentList = _logs.value.toMutableList()
            if (currentList.size >= MAX_LOGS) {
                currentList.removeAt(0)
            }
            currentList.add(entry)
            _logs.value = currentList
        }
    }

    fun info(tag: String, message: String) {
        log(LogSeverity.INFO, tag, message)
    }

    fun warning(tag: String, message: String, throwable: Throwable? = null) {
        log(LogSeverity.WARNING, tag, message, throwable)
    }

    fun error(tag: String, message: String, throwable: Throwable? = null) {
        log(LogSeverity.ERROR, tag, message, throwable)
    }

    fun critical(tag: String, message: String, throwable: Throwable? = null) {
        log(LogSeverity.CRITICAL, tag, message, throwable)
    }

    fun clearLogs() {
        synchronized(this) {
            _logs.value = emptyList()
        }
    }
}
