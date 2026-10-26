package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Double,
    val costPrice: Double = 0.0,
    val category: String = "Panes y Masas",
    val colorHex: Long = 0xFFD84315,
    val isActive: Boolean = true,
    val sortOrder: Int = 0
) {
    val profitPerUnit: Double
        get() = (price - costPrice).coerceAtLeast(0.0)

    val profitMarginPercent: Double
        get() = if (price > 0) ((price - costPrice) / price * 100).coerceAtLeast(0.0) else 0.0
}
