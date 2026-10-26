package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExpenseCategory
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
import com.example.ui.components.ExpenseDialog
import com.example.ui.components.MetricCard
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.BlueReinvestment
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.OrangeInvestment
import com.example.ui.theme.RedExpense
import com.example.ui.viewmodel.PeriodFilter
import com.example.ui.viewmodel.SalesViewModel
import com.example.util.ReportExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancesScreen(
    viewModel: SalesViewModel,
    modifier: Modifier = Modifier
) {
    val periodFilter by viewModel.periodFilter.collectAsStateWithLifecycle()
    val summary by viewModel.periodSummary.collectAsStateWithLifecycle()
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()

    var showExpenseDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<TransactionRecord?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    var txFilterType by remember { mutableStateOf<TransactionType?>(null) } // null = All, SALE = Ventas, EXPENSE = Egresos

    val displayedTransactions = remember(transactions, txFilterType) {
        if (txFilterType == null) transactions else transactions.filter { it.type == txFilterType }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Finanzas y Gastos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.testTag("reset_caja_button")
                    ) {
                        Icon(
                            Icons.Default.RestartAlt,
                            contentDescription = "Empezar de Cero",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showExpenseDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Registrar Egreso", fontWeight = FontWeight.Bold) },
                containerColor = AmberPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_expense_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 88.dp, start = 14.dp, end = 14.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Period filter chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PeriodFilter.values().forEach { filter ->
                        FilterChip(
                            selected = periodFilter == filter,
                            onClick = { viewModel.setPeriodFilter(filter) },
                            label = { Text(filter.label, fontSize = 12.sp) },
                            modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                        )
                    }
                }
            }

            // Hero Net Profit Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("net_profit_hero_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (summary.netProfit >= 0) {
                            GreenSuccess.copy(alpha = 0.12f)
                        } else {
                            RedExpense.copy(alpha = 0.12f)
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GANANCIA NETA REAL",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (summary.netProfit >= 0) GreenSuccess else RedExpense
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (summary.netProfit >= 0) GreenSuccess else RedExpense
                            ) {
                                Text(
                                    text = "Margen ${String.format(java.util.Locale.US, "%.1f", summary.profitMarginPercent)}%",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ReportExporter.formatMoney(summary.netProfit),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ventas Totales menos Reinversión en insumos y Gastos diarios",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 4 Core Financial Pillar Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Pilares Financieros (${summary.periodLabel})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Ventas Totales
                    MetricCard(
                        title = "1. Ventas Totales (Ingresos)",
                        value = ReportExporter.formatMoney(summary.totalSales),
                        subtitle = "${summary.salesCount} ventas registradas (${ReportExporter.formatMoney(summary.salesCash)} efectivo / ${ReportExporter.formatMoney(summary.salesTransfer)} QR)",
                        icon = Icons.Default.MonetizationOn,
                        color = GreenSuccess,
                        testTag = "metric_total_sales"
                    )

                    // Reinversión (Insumos)
                    MetricCard(
                        title = "2. Reinversión (Insumos y Stock)",
                        value = ReportExporter.formatMoney(summary.totalReinvestment),
                        subtitle = "Materia prima repuesta para seguir vendiendo (harina, manteca, mercadería)",
                        icon = Icons.Default.Loop,
                        color = BlueReinvestment,
                        testTag = "metric_reinvestment"
                    )

                    // Inversión (Capital)
                    MetricCard(
                        title = "3. Inversión (Capital y Herramientas)",
                        value = ReportExporter.formatMoney(summary.totalInvestment),
                        subtitle = "Equipos duraderos (balanza, carro, canasta, exhibidor)",
                        icon = Icons.Default.Build,
                        color = OrangeInvestment,
                        testTag = "metric_investment"
                    )

                    // Gastos Operativos
                    MetricCard(
                        title = "4. Gastos Operativos / Transporte",
                        value = ReportExporter.formatMoney(summary.totalOperatingExpense),
                        subtitle = "Flete, combustible, permiso municipal, gas y costos diarios",
                        icon = Icons.Default.LocalShipping,
                        color = RedExpense,
                        testTag = "metric_operating_expense"
                    )

                    // Gasto Total Acumulado
                    MetricCard(
                        title = "📉 GASTO TOTAL DEL PERÍODO",
                        value = ReportExporter.formatMoney(summary.totalExpense),
                        subtitle = "Suma de Reinversión + Inversión + Gastos Operativos",
                        icon = Icons.Default.AccountBalanceWallet,
                        color = AmberPrimary,
                        testTag = "metric_total_expense"
                    )
                }
            }

            // Subcategory breakdown
            if (summary.subcategoryBreakdown.isNotEmpty()) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PieChart, contentDescription = null, tint = AmberPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Desglose Detallado de Egresos",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            val maxSub = summary.subcategoryBreakdown.values.maxOrNull() ?: 1.0
                            summary.subcategoryBreakdown.forEach { (subName, amt) ->
                                val progress = (amt / maxSub).toFloat().coerceIn(0f, 1f)
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = subName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                        Text(text = ReportExporter.formatMoney(amt), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = AmberPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Interactive filter for the transactions feed
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Historial de Movimientos (${displayedTransactions.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = txFilterType == null,
                            onClick = { txFilterType = null },
                            label = { Text("Todos (${transactions.size})", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = txFilterType == TransactionType.SALE,
                            onClick = { txFilterType = TransactionType.SALE },
                            label = { Text("Ventas (${transactions.count { it.type == TransactionType.SALE }})", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = txFilterType == TransactionType.EXPENSE,
                            onClick = { txFilterType = TransactionType.EXPENSE },
                            label = { Text("Egresos (${transactions.count { it.type == TransactionType.EXPENSE }})", fontSize = 11.sp) }
                        )
                    }
                }
            }

            if (displayedTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Sin movimientos registrados en este filtro.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(displayedTransactions, key = { it.id }) { tx ->
                    TransactionItemRow(
                        transaction = tx,
                        onDelete = { transactionToDelete = tx }
                    )
                }
            }
        }
    }

    if (showExpenseDialog) {
        ExpenseDialog(
            onDismiss = { showExpenseDialog = false },
            onConfirm = { amount, category, subcategory, desc, paymentMethod ->
                viewModel.recordExpense(amount, category, subcategory, desc, paymentMethod)
                showExpenseDialog = false
            }
        )
    }

    transactionToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Eliminar Movimiento") },
            text = { Text("¿Deseas eliminar el registro de '${tx.description}' (${ReportExporter.formatMoney(tx.amount)})?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(tx)
                        transactionToDelete = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Empezar de Cero") },
            text = {
                Text(
                    "¿Deseas borrar todos los registros y movimientos de prueba para empezar de cero con tu caja en $0?\n\nLos productos del catálogo se mantendrán."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAllTransactions()
                        showResetDialog = false
                    }
                ) {
                    Text("Borrar y Empezar en $0", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TransactionItemRow(
    transaction: TransactionRecord,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tx_item_${transaction.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isSale = transaction.type == TransactionType.SALE
            val icon = if (isSale) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
            val color = if (isSale) {
                GreenSuccess
            } else {
                when (transaction.expenseCategory) {
                    ExpenseCategory.REINVESTMENT -> BlueReinvestment
                    ExpenseCategory.INVESTMENT -> OrangeInvestment
                    ExpenseCategory.OPERATING_EXPENSE -> RedExpense
                    null -> RedExpense
                }
            }

            val categoryLabel = if (isSale) {
                "VENTA"
            } else {
                transaction.expenseCategory?.label?.uppercase() ?: "EGRESO"
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = color.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = categoryLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = color,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                    if (!transaction.expenseSubcategory.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "• ${transaction.expenseSubcategory}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${ReportExporter.formatDate(transaction.timestamp)} • ${transaction.paymentMethod.label}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = (if (isSale) "+ " else "- ") + ReportExporter.formatMoney(transaction.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = color
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
