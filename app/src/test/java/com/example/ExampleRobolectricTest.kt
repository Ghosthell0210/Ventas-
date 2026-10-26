package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
import com.example.util.ReportExporter
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("VentaAlPaso", appName)
    }

    @Test
    fun `verify financial summary calculations`() {
        val transactions = listOf(
            TransactionRecord(
                type = TransactionType.SALE,
                amount = 10000.0,
                paymentMethod = PaymentMethod.CASH,
                description = "Venta pan"
            ),
            TransactionRecord(
                type = TransactionType.EXPENSE,
                expenseCategory = ExpenseCategory.REINVESTMENT,
                expenseSubcategory = "Harina y Granos",
                amount = 4000.0,
                description = "Harina"
            ),
            TransactionRecord(
                type = TransactionType.EXPENSE,
                expenseCategory = ExpenseCategory.OPERATING_EXPENSE,
                expenseSubcategory = "Transporte",
                amount = 1500.0,
                description = "Flete"
            ),
            TransactionRecord(
                type = TransactionType.EXPENSE,
                expenseCategory = ExpenseCategory.INVESTMENT,
                expenseSubcategory = "Balanza",
                amount = 2000.0,
                description = "Balanza"
            )
        )

        val summary = ReportExporter.calculateSummary(transactions, "Octubre 2026")

        assertEquals(10000.0, summary.totalSales, 0.01)
        assertEquals(4000.0, summary.totalReinvestment, 0.01)
        assertEquals(2000.0, summary.totalInvestment, 0.01)
        assertEquals(1500.0, summary.totalOperatingExpense, 0.01)
        assertEquals(7500.0, summary.totalExpense, 0.01)
        // Net profit = Sales - (Reinvestment + Operating Expense) = 10000 - 5500 = 4500
        assertEquals(4500.0, summary.netProfit, 0.01)
        // Net cash balance = Sales - Total Expense = 10000 - 7500 = 2500
        assertEquals(2500.0, summary.netCashBalance, 0.01)
    }
}
