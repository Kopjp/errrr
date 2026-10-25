package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.OlivePurchase
import com.example.ui.theme.OliveDebtRed
import com.example.ui.theme.OlivePrimary
import com.example.ui.theme.OliveSuccess
import com.example.ui.viewmodel.InvoicePreviewData
import com.example.ui.viewmodel.PressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(viewModel: PressViewModel) {
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showAddSheet by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<OlivePurchase?>(null) }

    val filteredList = remember(purchases, searchQuery) {
        if (searchQuery.isBlank()) purchases
        else purchases.filter {
            it.farmerName.contains(searchQuery, ignoreCase = true) ||
            it.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
            it.phoneNumber.contains(searchQuery)
        }
    }

    val totalWeightKg = filteredList.sumOf { it.weightKg }
    val totalCost = filteredList.sumOf { it.totalAmount }
    val totalRemaining = filteredList.sumOf { it.remainingAmount }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = OlivePrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_purchase_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "شراء زيتون جديد")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("purchases_screen")
        ) {
            // Summary Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "مشتريات الزيتون لحساب المعصرة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OlivePrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "إجمالي الوزن المشتَرى", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${viewModel.formatNumber(totalWeightKg)} كغ",
                                fontWeight = FontWeight.ExtraBold,
                                color = OlivePrimary
                            )
                        }
                        Column {
                            Text(text = "إجمالي التكلفة", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = viewModel.formatCurrency(totalCost, settings.currencySymbol),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(text = "متبقي للموردين", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = viewModel.formatCurrency(totalRemaining, settings.currencySymbol),
                                fontWeight = FontWeight.Bold,
                                color = if (totalRemaining > 0) OliveDebtRed else OliveSuccess
                            )
                        }
                    }
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("purchase_search_input"),
                placeholder = { Text("بحث باسم المزارع أو رقم الفاتورة أو الهاتف...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // List of Purchases
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد فواتير شراء زيتون مطابقة",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { purchase ->
                        PurchaseItemCard(
                            purchase = purchase,
                            viewModel = viewModel,
                            currency = settings.currencySymbol,
                            onClick = { viewModel.showInvoicePreview(InvoicePreviewData.PurchaseInvoice(purchase)) },
                            onDelete = { itemToDelete = purchase }
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet: Add New Purchase
    if (showAddSheet) {
        AddPurchaseBottomSheet(
            viewModel = viewModel,
            currency = settings.currencySymbol,
            onDismiss = { showAddSheet = false }
        )
    }

    // Delete Confirmation Dialog
    itemToDelete?.let { purchase ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("تأكيد حذف الفاتورة") },
            text = { Text("هل أنت متأكد من حذف فاتورة الشراء رقم ${purchase.invoiceNumber} للمزارع ${purchase.farmerName}؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePurchase(purchase)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OliveDebtRed)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { itemToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun PurchaseItemCard(
    purchase: OlivePurchase,
    viewModel: PressViewModel,
    currency: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("purchase_card_${purchase.invoiceNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(OlivePrimary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Agriculture, contentDescription = null, tint = OlivePrimary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = purchase.farmerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${purchase.invoiceNumber} | ${viewModel.formatDateShort(purchase.dateMillis)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (purchase.remainingAmount > 0) OliveDebtRed.copy(alpha = 0.12f) else OliveSuccess.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (purchase.remainingAmount > 0) "متبقي دين" else "مسدد كامل",
                        color = if (purchase.remainingAmount > 0) OliveDebtRed else OliveSuccess,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "الوزن والصنف", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${viewModel.formatNumber(purchase.weightKg)} كغ (${purchase.oliveVariety})", fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "المبلغ الإجمالي", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = viewModel.formatCurrency(purchase.totalAmount, currency), fontWeight = FontWeight.Bold, color = OlivePrimary)
                }
            }

            if (purchase.remainingAmount > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "المدفوع: ${viewModel.formatCurrency(purchase.paidAmount, currency)}", style = MaterialTheme.typography.bodySmall, color = OliveSuccess)
                    Text(text = "المتبقي: ${viewModel.formatCurrency(purchase.remainingAmount, currency)}", style = MaterialTheme.typography.bodySmall, color = OliveDebtRed, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPurchaseBottomSheet(
    viewModel: PressViewModel,
    currency: String,
    onDismiss: () -> Unit
) {
    var farmerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var useWeighbridge by remember { mutableStateOf(false) } // وزن القبان (قائم وفارغ)
    var grossWeightText by remember { mutableStateOf("") }
    var tareWeightText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var isUnitTon by remember { mutableStateOf(false) } // toggle kg vs ton
    var pricePerKgText by remember { mutableStateOf("") }
    var transportCostText by remember { mutableStateOf("") }
    var paidAmountText by remember { mutableStateOf("") }
    var oliveVariety by remember { mutableStateOf("بلدي / نبالي") }
    var notes by remember { mutableStateOf("") }

    val grossKg = grossWeightText.toDoubleOrNull() ?: 0.0
    val tareKg = tareWeightText.toDoubleOrNull() ?: 0.0
    val weighbridgeNetKg = (grossKg - tareKg).coerceAtLeast(0.0)

    val rawWeight = weightText.toDoubleOrNull() ?: 0.0
    val directWeightKg = if (isUnitTon) rawWeight * 1000.0 else rawWeight

    val weightKg = if (useWeighbridge) weighbridgeNetKg else directWeightKg
    val pricePerKg = pricePerKgText.toDoubleOrNull() ?: 0.0
    val transportCost = transportCostText.toDoubleOrNull() ?: 0.0
    val totalAmount = (weightKg * pricePerKg) + transportCost

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
                .testTag("add_purchase_modal")
        ) {
            Text(
                text = "تسجيل عملية شراء زيتون جديدة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OlivePrimary
            )
            Text(
                text = "تضاف الكمية تلقائياً لمخزون الزيتون ويثبت المبلغ في الصندوق وحساب المورد",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = farmerName,
                onValueChange = { farmerName = it },
                label = { Text("اسم المزارع / المورد *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("purchase_farmer_name_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("رقم الهاتف") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Weighing Method Selector (وزن مباشر vs قبان الشاحنة)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = { useWeighbridge = false },
                    shape = RoundedCornerShape(8.dp),
                    color = if (!useWeighbridge) OlivePrimary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "وزن مباشر (يدوي)",
                        color = if (!useWeighbridge) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (!useWeighbridge) FontWeight.Bold else FontWeight.Normal,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp),
                        fontSize = 12.sp
                    )
                }

                Surface(
                    onClick = { useWeighbridge = true },
                    shape = RoundedCornerShape(8.dp),
                    color = if (useWeighbridge) OlivePrimary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "وزن قبان (شاحنة/سيارة)",
                        color = if (useWeighbridge) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (useWeighbridge) FontWeight.Bold else FontWeight.Normal,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (useWeighbridge) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = grossWeightText,
                        onValueChange = { grossWeightText = it },
                        label = { Text("الوزن القائم (كغ) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tareWeightText,
                        onValueChange = { tareWeightText = it },
                        label = { Text("الوزن الفارغ (كغ) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "الوزن الصافي الناتج = ${viewModel.formatNumber(weighbridgeNetKg)} كغ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = OlivePrimary
                )
            } else {
                // Unit & Weight Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text(if (isUnitTon) "الوزن بالطن *" else "الوزن بالكيلوغرام *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("purchase_weight_input"),
                        singleLine = true
                    )

                    // Toggle Unit
                    Surface(
                        onClick = { isUnitTon = !isUnitTon },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isUnitTon) OlivePrimary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text(
                            text = if (isUnitTon) "طن" else "كغ",
                            color = if (isUnitTon) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
                        )
                    }
                }

                if (isUnitTon && rawWeight > 0) {
                    Text(
                        text = "يساوي: ${viewModel.formatNumber(weightKg)} كغ",
                        style = MaterialTheme.typography.labelSmall,
                        color = OlivePrimary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = pricePerKgText,
                onValueChange = { pricePerKgText = it },
                label = { Text("سعر الكيلوغرام ($currency) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("purchase_price_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = transportCostText,
                onValueChange = { transportCostText = it },
                label = { Text("أجور نقل ومصاريف إضافية ($currency)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Variety Options
            Text(text = "صنف الزيتون:", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("بلدي / نبالي", "صوراني", "قيسي", "شامي", "بعل").forEach { variety ->
                    val isSelected = oliveVariety == variety
                    Surface(
                        onClick = { oliveVariety = variety },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) OlivePrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = variety,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Total Calculated Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("إجمالي قيمة الفاتورة:", fontWeight = FontWeight.Bold)
                        Text(
                            text = viewModel.formatCurrency(totalAmount, currency),
                            fontWeight = FontWeight.ExtraBold,
                            color = OlivePrimary,
                            fontSize = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Paid Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = paidAmountText,
                    onValueChange = { paidAmountText = it },
                    label = { Text("المبلغ المدفوع نقداً ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("purchase_paid_input"),
                    singleLine = true
                )

                OutlinedButton(
                    onClick = { paidAmountText = totalAmount.toInt().toString() },
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text("سداد كامل")
                }
            }

            val paidAmount = paidAmountText.toDoubleOrNull() ?: 0.0
            val remaining = (totalAmount - paidAmount).coerceAtLeast(0.0)
            if (totalAmount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "المتبقي بذمة المعصرة للمزارع (دين): ${viewModel.formatCurrency(remaining, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (remaining > 0) OliveDebtRed else OliveSuccess,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("ملاحظات إضافية") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (farmerName.isNotBlank() && weightKg > 0 && pricePerKg > 0) {
                        viewModel.addPurchase(
                            farmerName = farmerName,
                            phone = phone,
                            weightKg = weightKg,
                            pricePerKg = pricePerKg,
                            transportCost = transportCost,
                            paidAmount = paidAmount,
                            oliveVariety = oliveVariety,
                            notes = notes,
                            grossWeightKg = if (useWeighbridge) grossKg else 0.0,
                            tareWeightKg = if (useWeighbridge) tareKg else 0.0
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_purchase_button"),
                enabled = farmerName.isNotBlank() && weightKg > 0 && pricePerKg > 0,
                colors = ButtonDefaults.buttonColors(containerColor = OlivePrimary)
            ) {
                Text("حفظ الفاتورة وإصدار الإيصال", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
