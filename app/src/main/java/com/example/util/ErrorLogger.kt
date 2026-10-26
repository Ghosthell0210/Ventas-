package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel {
    INFO,
    WARNING,
    ERROR
}

data class LogEntry(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String,
    val stackTrace: String? = null
)

object ErrorLogger {

    private val _logs = MutableStateFlow<List<LogEntry>>(
        listOf(
            LogEntry(
                timestamp = System.currentTimeMillis() - 60000,
                level = LogLevel.INFO,
                tag = "Sistema",
                message = "VentaAlPaso iniciado correctamente. Base de datos SQLite inicializada."
            )
        )
    )
    val logs = _logs.asStateFlow()

    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        val stack = throwable?.let {
            val sw = StringWriter()
            it.printStackTrace(PrintWriter(sw))
            sw.toString()
        }
        val entry = LogEntry(
            level = level,
            tag = tag,
            message = message,
            stackTrace = stack
        )
        val current = _logs.value.toMutableList()
        current.add(0, entry)
        if (current.size > 100) {
            _logs.value = current.take(100)
        } else {
            _logs.value = current
        }
    }

    fun info(tag: String, message: String) = log(LogLevel.INFO, tag, message)
    fun warn(tag: String, message: String, throwable: Throwable? = null) = log(LogLevel.WARNING, tag, message, throwable)
    fun error(tag: String, message: String, throwable: Throwable? = null) = log(LogLevel.ERROR, tag, message, throwable)

    fun clearLogs() {
        _logs.value = listOf(
            LogEntry(
                level = LogLevel.INFO,
                tag = "Sistema",
                message = "Registro de logs limpiado por el usuario."
            )
        )
    }

    fun buildDiagnosticReport(): String {
        val sb = StringBuilder()
        sb.append("📋 REPORTE DE DIAGNÓSTICO Y ERRORES\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📱 Dispositivo: ${Build.MANUFACTURER} ${Build.MODEL}\n")
        sb.append("🤖 Android: Versión ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})\n")
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        sb.append("⏱️ Fecha y Hora: ${sdf.format(Date())}\n")
        sb.append("📊 Total eventos registrados: ${_logs.value.size}\n\n")

        sb.append("📑 ÚLTIMOS REGISTROS:\n")
        _logs.value.take(25).forEach { entry ->
            val icon = when (entry.level) {
                LogLevel.INFO -> "ℹ️"
                LogLevel.WARNING -> "⚠️"
                LogLevel.ERROR -> "❌"
            }
            sb.append("$icon [${entry.level}] ${sdf.format(Date(entry.timestamp))} - ${entry.tag}\n")
            sb.append("   ${entry.message}\n")
            if (!entry.stackTrace.isNullOrBlank()) {
                sb.append("   Detalle técnico:\n${entry.stackTrace.take(300)}...\n")
            }
            sb.append("\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("Generado desde VentaAlPaso")
        return sb.toString()
    }

    fun shareDiagnosticReport(context: Context) {
        val report = buildDiagnosticReport()
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Reporte de Error - VentaAlPaso")
            putExtra(Intent.EXTRA_TEXT, report)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(intent, "Enviar reporte de error vía..."))
    }

    fun copyDiagnosticToClipboard(context: Context) {
        ReportExporter.copyToClipboard(context, buildDiagnosticReport())
        Toast.makeText(context, "Diagnóstico copiado al portapapeles", Toast.LENGTH_SHORT).show()
    }
}
