package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Product
import com.example.data.model.TransactionRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Product::class, TransactionRecord::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ventaalpaso.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        prepopulate(database.productDao())
                    }
                }
            }
        }

        // Only prepopulate initial product buttons for fast cashier catalog.
        // Transactions start strictly from 0 ($0 ventas, $0 egresos) so the real vendor starts clean.
        private suspend fun prepopulate(productDao: ProductDao) {
            val initialProducts = listOf(
                Product(name = "Pan Casero / Baguette", price = 1500.0, costPrice = 600.0, category = "Panes", colorHex = 0xFFD84315, sortOrder = 1),
                Product(name = "Pan Francés (kg)", price = 2200.0, costPrice = 900.0, category = "Panes", colorHex = 0xFFBF360C, sortOrder = 2),
                Product(name = "Criollos / Tortas Fritas", price = 600.0, costPrice = 200.0, category = "Especialidades", colorHex = 0xFFEF6C00, sortOrder = 3),
                Product(name = "Medialuna / Factura", price = 700.0, costPrice = 250.0, category = "Dulces", colorHex = 0xFFF57C00, sortOrder = 4),
                Product(name = "Docena de Facturas", price = 7200.0, costPrice = 2600.0, category = "Dulces", colorHex = 0xFFE65100, sortOrder = 5),
                Product(name = "Empanada al Paso", price = 2400.0, costPrice = 1100.0, category = "Comidas", colorHex = 0xFFC2185B, sortOrder = 6),
                Product(name = "Café Caliente / Té", price = 1400.0, costPrice = 400.0, category = "Bebidas", colorHex = 0xFF5D4037, sortOrder = 7),
                Product(name = "Agua / Gaseosa", price = 1800.0, costPrice = 1000.0, category = "Bebidas", colorHex = 0xFF1976D2, sortOrder = 8)
            )
            productDao.insertProducts(initialProducts)
        }
    }
}
