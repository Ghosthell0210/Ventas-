package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpensePresets
import com.example.data.model.PaymentMethod
import com.example.ui.theme.BlueReinvestment
import com.example.ui.theme.OrangeInvestment
import com.example.ui.theme.RedExpense

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, category: ExpenseCategory, subcategory: String, description: String, paymentMethod: PaymentMethod) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.REINVESTMENT) }
    var selectedSubcategory by remember { mutableStateOf(ExpensePresets.REINVESTMENT_SUBCATEGORIES.first()) }
    var descriptionText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var isError by remember { mutableStateOf(false) }

    val currentSubcategories = remember(selectedCategory) {
        ExpensePresets.forCategory(selectedCategory)
    }

    val categoryColor = when (selectedCategory) {
        ExpenseCategory.REINVESTMENT -> BlueReinvestment
        ExpenseCategory.INVESTMENT -> OrangeInvestment
        ExpenseCategory.OPERATING_EXPENSE -> RedExpense
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Registrar Egreso / Inversión",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "1. Tipo de Egreso:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                // 3 Category choice buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val categories = listOf(
                        Triple(ExpenseCategory.REINVESTMENT, "Reinversión", Icons.Default.Loop),
                        Triple(ExpenseCategory.INVESTMENT, "Inversión", Icons.Default.Build),
                        Triple(ExpenseCategory.OPERATING_EXPENSE, "Gasto", Icons.Default.LocalShipping)
                    )

                    categories.forEach { (cat, label, icon) ->
                        val isSelected = selectedCategory == cat
                        val btnColor = when (cat) {
                            ExpenseCategory.REINVESTMENT -> BlueReinvestment
                            ExpenseCategory.INVESTMENT -> OrangeInvestment
                            ExpenseCategory.OPERATING_EXPENSE -> RedExpense
                        }

                        Button(
                            onClick = {
                                selectedCategory = cat
                                selectedSubcategory = ExpensePresets.forCategory(cat).first()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cat_button_${cat.name.lowercase()}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) btnColor else btnColor.copy(alpha = 0.12f),
                                contentColor = if (isSelected) Color.White else btnColor
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
                        ) {
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                Icon(icon, contentDescription = label, modifier = Modifier.height(18.dp))
                                Text(label, fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = selectedCategory.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "2. Subcategoría:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    currentSubcategories.forEach { sub ->
                        FilterChip(
                            selected = selectedSubcategory == sub,
                            onClick = { selectedSubcategory = sub },
                            label = { Text(sub, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = categoryColor.copy(alpha = 0.2f),
                                selectedLabelColor = categoryColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "3. Monto ($):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { char -> char.isDigit() || char == '.' }
                        isError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input"),
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = "Monto") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isError,
                    supportingText = if (isError) { { Text("Ingresa un monto válido mayor a 0") } } else null,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))
                // Quick add chips
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(500, 1000, 2000, 5000, 10000, 20000).forEach { addVal ->
                        SuggestionChip(
                            onClick = {
                                val current = amountText.toDoubleOrNull() ?: 0.0
                                amountText = (current + addVal).toInt().toString()
                                isError = false
                            },
                            label = { Text("+$addVal") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "4. Medio de Pago:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    PaymentMethod.values().forEachIndexed { index, method ->
                        SegmentedButton(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = PaymentMethod.values().size)
                        ) {
                            Text(method.label, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Nota o proveedor (opcional)") },
                    placeholder = { Text("Ej: Compra en Molino Harinero") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_desc_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        isError = true
                    } else {
                        onConfirm(amount, selectedCategory, selectedSubcategory, descriptionText, paymentMethod)
                    }
                },
                modifier = Modifier.testTag("expense_save_button"),
                colors = ButtonDefaults.buttonColors(containerColor = categoryColor)
            ) {
                Text("Guardar Egreso")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
