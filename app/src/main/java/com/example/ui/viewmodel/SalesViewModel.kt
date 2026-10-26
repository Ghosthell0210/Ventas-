package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
import com.example.data.repository.SalesRepository
import com.example.util.FinancialSummary
import com.example.util.ReportExporter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class PeriodFilter(val label: String) {
    TODAY("Hoy"),
    THIS_WEEK("Esta Semana"),
    THIS_MONTH("Este Mes"),
    ALL_TIME("Histórico")
}

class SalesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SalesRepository
    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = SalesRepository(db.productDao(), db.transactionDao())
    }

    val activeProducts: StateFlow<List<Product>> = repository.activeProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionRecord>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart for multiple product rapid checkout
    private val _cart = MutableStateFlow<Map<Product, Int>>(emptyMap())
    val cart = _cart.asStateFlow()

    val cartTotal: StateFlow<Double> = _cart.combine(_cart) { map, _ ->
        map.entries.sumOf { (product, qty) -> product.price * qty }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartItemCount: StateFlow<Int> = _cart.combine(_cart) { map, _ ->
        map.values.sum()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Current active financial filter (for Finances screen)
    private val _periodFilter = MutableStateFlow(PeriodFilter.TODAY)
    val periodFilter = _periodFilter.asStateFlow()

    // Filtered transactions for the Finances screen based on periodFilter
    val filteredTransactions: StateFlow<List<TransactionRecord>> = combine(
        allTransactions,
        _periodFilter
    ) { txs, filter ->
        filterTransactionsByPeriod(txs, filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val periodSummary: StateFlow<FinancialSummary> = combine(
        filteredTransactions,
        _periodFilter
    ) { txs, filter ->
        ReportExporter.calculateSummary(txs, filter.label)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ReportExporter.calculateSummary(emptyList(), "Hoy")
    )

    // Today's specific total for POS top header
    val todaySalesTotal: StateFlow<Double> = allTransactions.combine(allTransactions) { txs, _ ->
        val todayTxs = filterTransactionsByPeriod(txs, PeriodFilter.TODAY)
        todayTxs.filter { it.type == TransactionType.SALE }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Monthly Report Screen state
    private val currentCalendar = Calendar.getInstance()
    private val _reportYear = MutableStateFlow(currentCalendar.get(Calendar.YEAR))
    val reportYear = _reportYear.asStateFlow()

    private val _reportMonth = MutableStateFlow(currentCalendar.get(Calendar.MONTH)) // 0-indexed
    val reportMonth = _reportMonth.asStateFlow()

    val monthlyReportSummary: StateFlow<FinancialSummary> = combine(
        allTransactions,
        _reportYear,
        _reportMonth
    ) { txs, year, month ->
        val (start, end) = ReportExporter.getMonthRange(year, month)
        val monthTxs = txs.filter { it.timestamp in start..end }
        val monthNames = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        val label = "${monthNames[month]} $year"
        ReportExporter.calculateSummary(monthTxs, label)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ReportExporter.calculateSummary(emptyList(), "Mes")
    )

    // Toast/Snackbar notifications
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // ---------------- Actions ----------------

    fun addToCart(product: Product) {
        val current = _cart.value.toMutableMap()
        current[product] = (current[product] ?: 0) + 1
        _cart.value = current
    }

    fun decrementCartItem(product: Product) {
        val current = _cart.value.toMutableMap()
        val count = current[product] ?: 0
        if (count > 1) {
            current[product] = count - 1
        } else {
            current.remove(product)
        }
        _cart.value = current
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    fun checkoutCart(paymentMethod: PaymentMethod) {
        val items = _cart.value
        if (items.isEmpty()) return
        viewModelScope.launch {
            val itemList = items.map { (prod, qty) -> Pair(prod, qty) }
            repository.recordMultiItemSale(itemList, paymentMethod)
            clearCart()
            _userMessage.emit("¡Venta de ${ReportExporter.formatMoney(items.entries.sumOf { it.key.price * it.value })} registrada con éxito!")
        }
    }

    fun quickSingleSale(product: Product, quantity: Int = 1, paymentMethod: PaymentMethod = PaymentMethod.CASH) {
        viewModelScope.launch {
            repository.recordSale(product, quantity, paymentMethod)
            _userMessage.emit("Cobrado: ${ReportExporter.formatMoney(product.price * quantity)} (${product.name})")
        }
    }

    fun recordFreeSale(amount: Double, description: String, paymentMethod: PaymentMethod) {
        if (amount <= 0) return
        viewModelScope.launch {
            repository.recordFreeSale(amount, description, paymentMethod)
            _userMessage.emit("Venta libre de ${ReportExporter.formatMoney(amount)} registrada")
        }
    }

    fun recordExpense(
        amount: Double,
        category: ExpenseCategory,
        subcategory: String,
        description: String,
        paymentMethod: PaymentMethod
    ) {
        if (amount <= 0) return
        viewModelScope.launch {
            repository.recordExpense(amount, category, subcategory, description, paymentMethod)
            _userMessage.emit("${category.label} registrada: ${ReportExporter.formatMoney(amount)}")
        }
    }

    fun deleteTransaction(transaction: TransactionRecord) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            _userMessage.emit("Movimiento eliminado")
        }
    }

    fun saveProduct(product: Product) {
        viewModelScope.launch {
            repository.saveProduct(product)
            _userMessage.emit("Producto '${product.name}' guardado")
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _userMessage.emit("Producto eliminado")
        }
    }

    fun resetAllTransactions() {
        viewModelScope.launch {
            repository.resetAllTransactions()
            _userMessage.emit("Caja y registros reiniciados a $0")
        }
    }

    fun setPeriodFilter(filter: PeriodFilter) {
        _periodFilter.value = filter
    }

    fun setReportMonth(year: Int, month: Int) {
        _reportYear.value = year
        _reportMonth.value = month
    }

    fun prevReportMonth() {
        var m = _reportMonth.value - 1
        var y = _reportYear.value
        if (m < 0) {
            m = 11
            y -= 1
        }
        _reportMonth.value = m
        _reportYear.value = y
    }

    fun nextReportMonth() {
        var m = _reportMonth.value + 1
        var y = _reportYear.value
        if (m > 11) {
            m = 0
            y += 1
        }
        _reportMonth.value = m
        _reportYear.value = y
    }

    private fun filterTransactionsByPeriod(
        transactions: List<TransactionRecord>,
        period: PeriodFilter
    ): List<TransactionRecord> {
        if (period == PeriodFilter.ALL_TIME) return transactions

        val now = Calendar.getInstance()
        val startCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        when (period) {
            PeriodFilter.TODAY -> {
                // start of today is already set
            }
            PeriodFilter.THIS_WEEK -> {
                startCal.set(Calendar.DAY_OF_WEEK, startCal.firstDayOfWeek)
            }
            PeriodFilter.THIS_MONTH -> {
                startCal.set(Calendar.DAY_OF_MONTH, 1)
            }
            PeriodFilter.ALL_TIME -> {}
        }

        val startTime = startCal.timeInMillis
        return transactions.filter { it.timestamp >= startTime }
    }
}
