package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    SALE,
    EXPENSE
}

enum class ExpenseCategory(val label: String, val description: String) {
    REINVESTMENT(
        label = "Reinversión",
        description = "Materia prima e insumos para reponer stock y seguir produciendo (harina, manteca, mercadería, bolsas)"
    ),
    INVESTMENT(
        label = "Inversión Capital",
        description = "Bienes de uso y herramientas duraderas (balanza, carro, canasta, exhibidor, cartel)"
    ),
    OPERATING_EXPENSE(
        label = "Gasto Operativo",
        description = "Costos del día a día (transporte, combustible, permiso municipal, gas, almuerzo)"
    )
}

enum class PaymentMethod(val label: String) {
    CASH("Efectivo"),
    TRANSFER("Transferencia / QR")
}

object ExpensePresets {
    val REINVESTMENT_SUBCATEGORIES = listOf(
        "Harina y Granos",
        "Grasa / Manteca / Aceite",
        "Levadura y Mejoradores",
        "Azúcar / Dulces / Rellenos",
        "Bolsas y Empaque",
        "Mercadería lista (Reventa)",
        "Insumos Varios"
    )

    val INVESTMENT_SUBCATEGORIES = listOf(
        "Balanza / Pesa",
        "Canasta / Exhibidor",
        "Carro / Bici / Caja térmica",
        "Sombrilla / Puesto / Lona",
        "Cartelería y Pizarra",
        "Herramientas y Utensilios",
        "Otra Inversión"
    )

    val OPERATING_SUBCATEGORIES = listOf(
        "Transporte / Flete / Boletos",
        "Combustible / Nafta",
        "Gas / Leña / Electricidad",
        "Permiso municipal / Canon",
        "Almuerzo / Comida de turno",
        "Limpieza e Higiene",
        "Otro Gasto Diario"
    )

    fun forCategory(category: ExpenseCategory): List<String> = when (category) {
        ExpenseCategory.REINVESTMENT -> REINVESTMENT_SUBCATEGORIES
        ExpenseCategory.INVESTMENT -> INVESTMENT_SUBCATEGORIES
        ExpenseCategory.OPERATING_EXPENSE -> OPERATING_SUBCATEGORIES
    }
}

@Entity(tableName = "transactions")
data class TransactionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: TransactionType,
    val expenseCategory: ExpenseCategory? = null,
    val expenseSubcategory: String? = null,
    val amount: Double,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val description: String = "",
    val productId: Long? = null,
    val productName: String? = null,
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val estimatedUnitCost: Double = 0.0
)
