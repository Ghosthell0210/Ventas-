package com.example.data.repository

import com.example.data.local.ProductDao
import com.example.data.local.TransactionDao
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

class SalesRepository(
    private val productDao: ProductDao,
    private val transactionDao: TransactionDao
) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val activeProducts: Flow<List<Product>> = productDao.getActiveProducts()
    val allTransactions: Flow<List<TransactionRecord>> = transactionDao.getAllTransactions()

    fun getTransactionsBetween(start: Long, end: Long): Flow<List<TransactionRecord>> =
        transactionDao.getTransactionsBetween(start, end)

    suspend fun recordSale(
        product: Product,
        quantity: Int = 1,
        paymentMethod: PaymentMethod = PaymentMethod.CASH,
        customPrice: Double? = null
    ) {
        val price = customPrice ?: product.price
        val total = price * quantity
        val record = TransactionRecord(
            timestamp = System.currentTimeMillis(),
            type = TransactionType.SALE,
            amount = total,
            paymentMethod = paymentMethod,
            description = "${quantity}x ${product.name}",
            productId = product.id,
            productName = product.name,
            quantity = quantity,
            unitPrice = price,
            estimatedUnitCost = product.costPrice
        )
        transactionDao.insertTransaction(record)
    }

    suspend fun recordFreeSale(
        amount: Double,
        description: String,
        paymentMethod: PaymentMethod = PaymentMethod.CASH
    ) {
        val desc = description.ifBlank { "Venta Libre al Paso" }
        val record = TransactionRecord(
            timestamp = System.currentTimeMillis(),
            type = TransactionType.SALE,
            amount = amount,
            paymentMethod = paymentMethod,
            description = desc,
            productName = desc,
            quantity = 1,
            unitPrice = amount
        )
        transactionDao.insertTransaction(record)
    }

    suspend fun recordMultiItemSale(
        items: List<Pair<Product, Int>>,
        paymentMethod: PaymentMethod = PaymentMethod.CASH
    ) {
        val timestamp = System.currentTimeMillis()
        val records = items.map { (product, qty) ->
            TransactionRecord(
                timestamp = timestamp,
                type = TransactionType.SALE,
                amount = product.price * qty,
                paymentMethod = paymentMethod,
                description = "${qty}x ${product.name}",
                productId = product.id,
                productName = product.name,
                quantity = qty,
                unitPrice = product.price,
                estimatedUnitCost = product.costPrice
            )
        }
        transactionDao.insertTransactions(records)
    }

    suspend fun recordExpense(
        amount: Double,
        category: ExpenseCategory,
        subcategory: String,
        description: String,
        paymentMethod: PaymentMethod = PaymentMethod.CASH
    ) {
        val record = TransactionRecord(
            timestamp = System.currentTimeMillis(),
            type = TransactionType.EXPENSE,
            expenseCategory = category,
            expenseSubcategory = subcategory,
            amount = amount,
            paymentMethod = paymentMethod,
            description = description.ifBlank { "$subcategory (${category.label})" }
        )
        transactionDao.insertTransaction(record)
    }

    suspend fun deleteTransaction(transaction: TransactionRecord) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun saveProduct(product: Product) {
        if (product.id == 0L) {
            productDao.insertProduct(product)
        } else {
            productDao.updateProduct(product)
        }
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }

    suspend fun resetAllTransactions() {
        transactionDao.deleteAllTransactions()
    }
}
