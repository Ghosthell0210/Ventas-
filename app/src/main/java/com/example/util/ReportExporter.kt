package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class FinancialSummary(
    val periodLabel: String,
    val totalSales: Double,
    val salesCash: Double,
    val salesTransfer: Double,
    val salesCount: Int,
    val totalReinvestment: Double,
    val totalInvestment: Double,
    val totalOperatingExpense: Double,
    val totalExpense: Double,
    val netProfit: Double,
    val netCashBalance: Double,
    val profitMarginPercent: Double,
    val expensesByCategory: Map<ExpenseCategory, Double>,
    val subcategoryBreakdown: Map<String, Double>,
    val topProducts: List<ProductSalesStat>
)

data class ProductSalesStat(
    val name: String,
    val quantity: Int,
    val totalRevenue: Double
)

object ReportExporter {

    private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR")).apply {
        maximumFractionDigits = 0
    }

    fun formatMoney(amount: Double): String {
        return try {
            currencyFormat.format(amount)
        } catch (_: Exception) {
            "$$amount"
        }
    }

    fun calculateSummary(
        transactions: List<TransactionRecord>,
        periodLabel: String
    ): FinancialSummary {
        var sales = 0.0
        var salesCash = 0.0
        var salesTransfer = 0.0
        var salesCount = 0

        var reinvestment = 0.0
        var investment = 0.0
        var operating = 0.0

        val categoryMap = mutableMapOf<ExpenseCategory, Double>()
        val subcategoryMap = mutableMapOf<String, Double>()
        val productMap = mutableMapOf<String, Pair<Int, Double>>()

        for (tx in transactions) {
            when (tx.type) {
                TransactionType.SALE -> {
                    sales += tx.amount
                    salesCount++
                    if (tx.paymentMethod == PaymentMethod.CASH) {
                        salesCash += tx.amount
                    } else {
                        salesTransfer += tx.amount
                    }

                    val pName = tx.productName ?: tx.description
                    val existing = productMap[pName] ?: Pair(0, 0.0)
                    productMap[pName] = Pair(existing.first + tx.quantity, existing.second + tx.amount)
                }
                TransactionType.EXPENSE -> {
                    val cat = tx.expenseCategory ?: ExpenseCategory.OPERATING_EXPENSE
                    val currentCatTotal = categoryMap[cat] ?: 0.0
                    categoryMap[cat] = currentCatTotal + tx.amount

                    when (cat) {
                        ExpenseCategory.REINVESTMENT -> reinvestment += tx.amount
                        ExpenseCategory.INVESTMENT -> investment += tx.amount
                        ExpenseCategory.OPERATING_EXPENSE -> operating += tx.amount
                    }

                    val sub = tx.expenseSubcategory ?: "Varios"
                    val currentSubTotal = subcategoryMap[sub] ?: 0.0
                    subcategoryMap[sub] = currentSubTotal + tx.amount
                }
            }
        }

        val totalExpense = reinvestment + investment + operating
        val netProfit = sales - (reinvestment + operating)
        val netCashBalance = sales - totalExpense
        val profitMargin = if (sales > 0) (netProfit / sales) * 100 else 0.0

        val topProducts = productMap.entries
            .map { ProductSalesStat(it.key, it.value.first, it.value.second) }
            .sortedByDescending { it.totalRevenue }

        return FinancialSummary(
            periodLabel = periodLabel,
            totalSales = sales,
            salesCash = salesCash,
            salesTransfer = salesTransfer,
            salesCount = salesCount,
            totalReinvestment = reinvestment,
            totalInvestment = investment,
            totalOperatingExpense = operating,
            totalExpense = totalExpense,
            netProfit = netProfit,
            netCashBalance = netCashBalance,
            profitMarginPercent = profitMargin,
            expensesByCategory = categoryMap,
            subcategoryBreakdown = subcategoryMap.toList().sortedByDescending { it.second }.toMap(),
            topProducts = topProducts
        )
    }

    fun generateFormattedReport(summary: FinancialSummary): String {
        val sb = StringBuilder()
        sb.append("📊 REPORTE DE FINANZAS Y VENTAS\n")
        sb.append("🗓️ Período: ${summary.periodLabel}\n")
        sb.append("🏪 Puesto / Comercio al Paso\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")

        sb.append("💰 TOTAL VENTAS: ${formatMoney(summary.totalSales)} (${summary.salesCount} ventas)\n")
        sb.append("   💵 Efectivo: ${formatMoney(summary.salesCash)}\n")
        sb.append("   📱 Transferencia / QR: ${formatMoney(summary.salesTransfer)}\n\n")

        sb.append("📉 GASTO TOTAL: ${formatMoney(summary.totalExpense)}\n")
        sb.append("   🔄 Reinversión (Insumos/Stock): ${formatMoney(summary.totalReinvestment)}\n")
        sb.append("   🛠️ Inversión (Capital/Equipos): ${formatMoney(summary.totalInvestment)}\n")
        sb.append("   📦 Gastos Operativos (Flete/Gas/Permisos): ${formatMoney(summary.totalOperatingExpense)}\n\n")

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("💵 GANANCIA NETA REAL: ${formatMoney(summary.netProfit)}\n")
        sb.append("   (Ingresos menos Insumos y Gastos diarios)\n")
        sb.append("🏦 Balance en Mano / Caja: ${formatMoney(summary.netCashBalance)}\n")
        sb.append("📈 Margen Operativo: ${String.format(Locale.US, "%.1f", summary.profitMarginPercent)}%\n\n")

        if (summary.subcategoryBreakdown.isNotEmpty()) {
            sb.append("📋 DESGLOSE DE GASTOS PRINCIPALES:\n")
            summary.subcategoryBreakdown.entries.take(5).forEach { (sub, amt) ->
                sb.append("   • $sub: ${formatMoney(amt)}\n")
            }
            sb.append("\n")
        }

        if (summary.topProducts.isNotEmpty()) {
            sb.append("⭐ PRODUCTOS MÁS VENDIDOS:\n")
            summary.topProducts.take(5).forEachIndexed { index, stat ->
                sb.append("   ${index + 1}. ${stat.name}: ${stat.quantity} u. (${formatMoney(stat.totalRevenue)})\n")
            }
            sb.append("\n")
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("Generado con VentaAlPaso")
        return sb.toString()
    }

    fun shareReport(context: Context, text: String, periodLabel: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "Reporte Financiero - $periodLabel")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Compartir Reporte vía...")
        context.startActivity(shareIntent)
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Reporte VentaAlPaso", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "¡Reporte copiado al portapapeles!", Toast.LENGTH_SHORT).show()
    }

    fun getMonthRange(year: Int, monthZeroIndexed: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthZeroIndexed)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return Pair(start, end)
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatShortDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale("es", "ES"))
        return sdf.format(Date(timestamp))
    }
}
