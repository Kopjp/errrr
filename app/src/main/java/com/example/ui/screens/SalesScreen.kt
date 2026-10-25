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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
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
import com.example.data.models.OilSale
import com.example.data.models.PressSettings
import com.example.ui.theme.OilGold
import com.example.ui.theme.OliveDebtRed
import com.example.ui.theme.OlivePrimary
import com.example.ui.theme.OliveSuccess
import com.example.ui.viewmodel.InvoicePreviewData
import com.example.ui.viewmodel.PressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(viewModel: PressViewModel) {
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val stock by viewModel.stockSummary.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showAddSheet by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<OilSale?>(null) }

    val filteredList = remember(sales, searchQuery) {
        if (searchQuery.isBlank()) sales
        else sales.filter {
            it.customerName.contains(searchQuery, ignoreCase = true) ||
            it.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
            it.phoneNumber.contains(searchQuery)
        }
    }

    val totalSalesAmount = filteredList.sumOf { it.totalAmount }
    val totalCollected = filteredList.sumOf { it.paidAmount }
    val totalRemainingCredit = filteredList.sumOf { it.remainingAmount }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = OlivePrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_sale_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "تسجيل بيع جديد")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("sales_screen")
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
                        text = "مبيعات الزيت والمنتجات من المخزون",
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
                            Text(text = "إجمالي المبيعات", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = viewModel.formatCurrency(totalSalesAmount, settings.currencySymbol),
                                fontWeight = FontWeight.Bold,
                                color = OlivePrimary
                            )
                        }
                        Column {
                            Text(text = "المحصل نقداً", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = viewModel.formatCurrency(totalCollected, settings.currencySymbol),
                                fontWeight = FontWeight.Bold,
                                color = OliveSuccess
                            )
                        }
                        Column {
                            Text(text = "المتبقي بالدين", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = viewModel.formatCurrency(totalRemainingCredit, settings.currencySymbol),
                                fontWeight = FontWeight.Bold,
                                color = if (totalRemainingCredit > 0) OliveDebtRed else OliveSuccess
                            )
                        }
                    }
                }
            }

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("sales_search_input"),
                placeholder = { Text("بحث باسم المشتري أو رقم الفاتورة...") },
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

            // List of Sales
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد فواتير مبيعات مسجلة",
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
                    items(filteredList, key = { it.id }) { sale ->
                        SaleItemCard(
                            sale = sale,
                            viewModel = viewModel,
                            settings = settings,
                            onClick = { viewModel.showInvoicePreview(InvoicePreviewData.SaleInvoice(sale)) },
                            onDelete = { itemToDelete = sale }
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddSaleBottomSheet(
            viewModel = viewModel,
            settings = settings,
            stockOilKg = stock.oilStockKg,
            stockOilTins = stock.oilStockTins,
            stockEmptyTins = stock.emptyTinsCount,
            onDismiss = { showAddSheet = false }
        )
    }

    itemToDelete?.let { sale ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("تأكيد حذف الفاتورة") },
            text = { Text("هل أنت متأكد من حذف فاتورة البيع رقم ${sale.invoiceNumber} للزبون ${sale.customerName}؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSale(sale)
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
private fun SaleItemCard(
    sale: OilSale,
    viewModel: PressViewModel,
    settings: PressSettings,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val itemLabel = when (sale.itemType) {
        "OIL_TIN" -> "تنكة زيت"
        "OIL_KG" -> "كغ زيت"
        "OIL_LITER" -> "لتر زيت"
        "EMPTY_TIN" -> "تنكة فارغة"
        "POMACE" -> "تفل / بيرين"
        else -> sale.itemType
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("sale_card_${sale.invoiceNumber}"),
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
                        Icon(Icons.Default.PointOfSale, contentDescription = null, tint = OlivePrimary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = sale.customerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${sale.invoiceNumber} | ${viewModel.formatDateShort(sale.dateMillis)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (sale.remainingAmount > 0) OliveDebtRed.copy(alpha = 0.12f) else OliveSuccess.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (sale.remainingAmount > 0) "آجل / متبقي" else "مسدد نقداً",
                        color = if (sale.remainingAmount > 0) OliveDebtRed else OliveSuccess,
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
                    Text(text = "المادة والكمية", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${viewModel.formatNumber(sale.quantity)} $itemLabel", fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "الإجمالي الصافي", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = viewModel.formatCurrency(sale.totalAmount, settings.currencySymbol), fontWeight = FontWeight.Bold, color = OlivePrimary)
                }
            }

            if (sale.remainingAmount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "المسدد: ${viewModel.formatCurrency(sale.paidAmount, settings.currencySymbol)}", style = MaterialTheme.typography.bodySmall, color = OliveSuccess)
                    Text(text = "المتبقي: ${viewModel.formatCurrency(sale.remainingAmount, settings.currencySymbol)}", style = MaterialTheme.typography.bodySmall, color = OliveDebtRed, fontWeight = FontWeight.Bold)
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
private fun AddSaleBottomSheet(
    viewModel: PressViewModel,
    settings: PressSettings,
    stockOilKg: Double,
    stockOilTins: Double,
    stockEmptyTins: Int,
    onDismiss: () -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedItemType by remember { mutableStateOf("OIL_TIN") }
    var quantityText by remember { mutableStateOf("") }
    var unitPriceText by remember { mutableStateOf("") }
    var discountText by remember { mutableStateOf("0") }
    var paidAmountText by remember { mutableStateOf("") }
    var paymentType by remember { mutableStateOf("CASH") }
    var notes by remember { mutableStateOf("") }

    val quantity = quantityText.toDoubleOrNull() ?: 0.0
    val unitPrice = unitPriceText.toDoubleOrNull() ?: 0.0
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val totalAmount = ((quantity * unitPrice) - discount).coerceAtLeast(0.0)

    // Stock check
    val isStockInsufficient = when (selectedItemType) {
        "OIL_TIN" -> quantity > stockOilTins
        "OIL_KG" -> quantity > stockOilKg
        "OIL_LITER" -> (quantity * 0.916) > stockOilKg
        "EMPTY_TIN" -> quantity > stockEmptyTins
        else -> false
    }

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
                .testTag("add_sale_modal")
        ) {
            Text(
                text = "تسجيل عملية بيع زيت أو منتجات",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OlivePrimary
            )
            Text(
                text = "تخصم الكمية من المخزون تلقائياً ويثبت الإيراد في الصندوق",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("اسم الزبون / المشتري *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sale_customer_name_input"),
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

            Spacer(modifier = Modifier.height(14.dp))

            // Item Type Selection
            Text(text = "المادة المباعة:", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "OIL_TIN" to "تنكة زيت",
                    "OIL_KG" to "زيت بالكغ",
                    "OIL_LITER" to "لتر زيت",
                    "EMPTY_TIN" to "تنكة فارغة",
                    "POMACE" to "تفل زيتون"
                ).forEach { (typeKey, typeLabel) ->
                    val isSelected = selectedItemType == typeKey
                    Surface(
                        onClick = { selectedItemType = typeKey },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) OlivePrimary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = typeLabel,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Available stock indicator
            Spacer(modifier = Modifier.height(8.dp))
            val currentAvailable = when (selectedItemType) {
                "OIL_TIN" -> "المتاح بالمخزون: ${viewModel.formatNumber(stockOilTins, 1)} تنكة (≈ ${viewModel.formatNumber(stockOilKg)} كغ)"
                "OIL_KG" -> "المتاح بالمخزون: ${viewModel.formatNumber(stockOilKg)} كغ"
                "OIL_LITER" -> "المتاح بالمخزون: ≈ ${viewModel.formatNumber(stockOilKg / 0.916, 1)} لتر"
                "EMPTY_TIN" -> "المتاح بالمخزون: $stockEmptyTins تنكة فارغة"
                else -> ""
            }
            if (currentAvailable.isNotEmpty()) {
                Text(text = currentAvailable, style = MaterialTheme.typography.labelSmall, color = OilGold, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quantity & Unit Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("الكمية المباعة *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sale_quantity_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = unitPriceText,
                    onValueChange = { unitPriceText = it },
                    label = { Text("سعر الوحدة (${settings.currencySymbol}) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sale_unit_price_input"),
                    singleLine = true
                )
            }

            if (isStockInsufficient && quantity > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = OliveDebtRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "تنبيه: الكمية المطلوبة تتجاوز المخزون الفعلي الحالي!",
                        style = MaterialTheme.typography.labelSmall,
                        color = OliveDebtRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = discountText,
                onValueChange = { discountText = it },
                label = { Text("الخصم الممنوح (${settings.currencySymbol})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Total Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("صافي قيمة الفاتورة:", fontWeight = FontWeight.Bold)
                        Text(
                            text = viewModel.formatCurrency(totalAmount, settings.currencySymbol),
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
                    label = { Text("المبلغ المقبوض نقداً (${settings.currencySymbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sale_paid_input"),
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
                    text = "المتبقي بذمة الزبون (دين): ${viewModel.formatCurrency(remaining, settings.currencySymbol)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (remaining > 0) OliveDebtRed else OliveSuccess,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("ملاحظات") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (customerName.isNotBlank() && quantity > 0 && unitPrice > 0) {
                        viewModel.addSale(
                            customerName = customerName,
                            phone = phone,
                            itemType = selectedItemType,
                            quantity = quantity,
                            unitPrice = unitPrice,
                            discount = discount,
                            paidAmount = paidAmount,
                            paymentType = if (remaining == 0.0) "CASH" else if (paidAmount == 0.0) "CREDIT" else "PARTIAL",
                            notes = notes
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_sale_button"),
                enabled = customerName.isNotBlank() && quantity > 0 && unitPrice > 0,
                colors = ButtonDefaults.buttonColors(containerColor = OlivePrimary)
            ) {
                Text("حفظ الفاتورة وخصم المخزون", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
