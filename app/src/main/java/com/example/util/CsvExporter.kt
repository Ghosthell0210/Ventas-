package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    fun generateMonthlyCsv(summary: FinancialSummary, transactions: List<TransactionRecord>): String {
        val sb = StringBuilder()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        // Executive summary lines
        sb.append("REPORTE FINANCIERO MENSUAL - VENTAALPASO\n")
        sb.append("Período,${summary.periodLabel}\n")
        sb.append("Total Ventas (Ingresos),${summary.totalSales}\n")
        sb.append("Ventas en Efectivo,${summary.salesCash}\n")
        sb.append("Ventas en Transferencia / QR,${summary.salesTransfer}\n")
        sb.append("Cantidad de Ventas,${summary.salesCount}\n")
        sb.append("Total Reinversión (Insumos / Stock),${summary.totalReinvestment}\n")
        sb.append("Total Inversión (Capital / Equipos),${summary.totalInvestment}\n")
        sb.append("Total Gastos Operativos (Flete / Gas / Puesto),${summary.totalOperatingExpense}\n")
        sb.append("GASTO TOTAL ACUMULADO,${summary.totalExpense}\n")
        sb.append("GANANCIA NETA REAL (Ventas - Reinversión - Gastos Operativos),${summary.netProfit}\n")
        sb.append("Balance Final de Caja (Ventas - Gasto Total),${summary.netCashBalance}\n")
        sb.append("Margen Operativo Porcentaje,${String.format(Locale.US, "%.2f", summary.profitMarginPercent)}%\n")
        sb.append("\n")

        // Detailed Transactions table
        sb.append("DETALLE DE MOVIMIENTOS\n")
        sb.append("Fecha,Tipo,Categoría,Subcategoría,Descripción,Monto,Método Pago,Producto,Cantidad,Precio Unitario,Costo Estimado\n")

        transactions.forEach { tx ->
            val dateStr = sdf.format(Date(tx.timestamp))
            val tipo = if (tx.type == TransactionType.SALE) "VENTA" else "EGRESO"
            val cat = tx.expenseCategory?.label ?: ""
            val sub = tx.expenseSubcategory ?: ""
            val desc = cleanCsvCell(tx.description)
            val monto = tx.amount
            val pago = tx.paymentMethod.label
            val prod = cleanCsvCell(tx.productName ?: "")
            val cant = tx.quantity
            val unitPrice = tx.unitPrice
            val cost = tx.estimatedUnitCost

            sb.append("\"$dateStr\",\"$tipo\",\"$cat\",\"$sub\",\"$desc\",$monto,\"$pago\",\"$prod\",$cant,$unitPrice,$cost\n")
        }

        return sb.toString()
    }

    private fun cleanCsvCell(value: String): String {
        return value.replace("\"", "\"\"").replace("\n", " ").trim()
    }

    fun shareCsv(context: Context, filename: String, csvContent: String) {
        try {
            val cachePath = File(context.cacheDir, "exports")
            cachePath.mkdirs()
            val file = File(cachePath, filename)
            FileOutputStream(file).use { out ->
                // UTF-8 BOM for Microsoft Excel proper accented characters rendering
                out.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                out.write(csvContent.toByteArray(Charsets.UTF_8))
            }

            val contentUri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (_: Exception) {
                null
            }

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                if (contentUri != null) {
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    type = "text/csv"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    putExtra(Intent.EXTRA_TEXT, csvContent)
                    type = "text/plain"
                }
                putExtra(Intent.EXTRA_SUBJECT, "Exportación Reporte CSV - $filename")
            }
            context.startActivity(Intent.createChooser(shareIntent, "Exportar Reporte CSV..."))
            ErrorLogger.info("Exportador", "Reporte CSV exportado exitosamente: $filename")
        } catch (e: Exception) {
            ErrorLogger.error("Exportador", "Error al exportar archivo CSV: ${e.message}", e)
            // Fallback to text sharing
            val textIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, csvContent)
                putExtra(Intent.EXTRA_SUBJECT, filename)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(textIntent, "Compartir datos CSV vía..."))
        }
    }
}
