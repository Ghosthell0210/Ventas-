package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Product

@Composable
fun ProductEditDialog(
    initialProduct: Product? = null,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var priceText by remember { mutableStateOf(initialProduct?.price?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var costPriceText by remember { mutableStateOf(initialProduct?.costPrice?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "Panes y Masas") }
    var isActive by remember { mutableStateOf(initialProduct?.isActive ?: true) }
    var isNameError by remember { mutableStateOf(false) }
    var isPriceError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialProduct == null) "Nuevo Producto" else "Editar Producto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isNameError = false
                    },
                    label = { Text("Nombre del Producto") },
                    placeholder = { Text("Ej: Baguette Casera") },
                    leadingIcon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Nombre") },
                    isError = isNameError,
                    supportingText = if (isNameError) { { Text("El nombre es requerido") } } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it.filter { c -> c.isDigit() || c == '.' }
                        isPriceError = false
                    },
                    label = { Text("Precio de Venta al Público ($)") },
                    placeholder = { Text("Ej: 1500") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = "Precio") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isPriceError,
                    supportingText = if (isPriceError) { { Text("Ingresa un precio válido") } } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_price_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = costPriceText,
                    onValueChange = {
                        costPriceText = it.filter { c -> c.isDigit() || c == '.' }
                    },
                    label = { Text("Costo estimado de insumos por unidad ($)") },
                    placeholder = { Text("Ej: 600 (harina, manteca, bolsa)") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = "Costo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text("Sirve para estimar margen de ganancia") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_cost_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría") },
                    placeholder = { Text("Panes, Facturas, Bebidas, etc.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_category_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Visible en caja rápida:", modifier = Modifier.weight(1f))
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull()
                    if (name.isBlank()) {
                        isNameError = true
                        return@Button
                    }
                    if (price == null || price <= 0) {
                        isPriceError = true
                        return@Button
                    }
                    val cost = costPriceText.toDoubleOrNull() ?: 0.0
                    val product = Product(
                        id = initialProduct?.id ?: 0L,
                        name = name.trim(),
                        price = price,
                        costPrice = cost,
                        category = category.ifBlank { "General" }.trim(),
                        isActive = isActive,
                        sortOrder = initialProduct?.sortOrder ?: 0
                    )
                    onSave(product)
                },
                modifier = Modifier.testTag("save_product_button")
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
