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
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.material3.Switch
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
import com.example.data.models.PressSettings
import com.example.data.models.PressingOperation
import com.example.ui.theme.OilGold
import com.example.ui.theme.OliveDebtRed
import com.example.ui.theme.OlivePrimary
import com.example.ui.theme.OliveSuccess
import com.example.ui.viewmodel.InvoicePreviewData
import com.example.ui.viewmodel.PressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PressingScreen(viewModel: PressViewModel) {
    val pressings by viewModel.pressings.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showAddSheet by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<PressingOperation?>(null) }

    val filteredList = remember(pressings, searchQuery) {
        if (searchQuery.isBlank()) pressings
        else pressings.filter {
            it.customerName.contains(searchQuery, ignoreCase = true) ||
            it.receiptNumber.contains(searchQuery, ignoreCase = true) ||
            it.phoneNumber.contains(searchQuery)
        }
    }

    val totalOliveKg = filteredList.sumOf { it.oliveWeightKg }
    val totalOilKg = filteredList.sumOf { it.producedOilKg }
    val avgYield = if (totalOliveKg > 0) (totalOilKg / totalOliveKg) * 100 else 0.0
    val totalRemainingDebts = filteredList.sumOf { it.remainingAmountCash }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = OlivePrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_pressing_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "تسجيل عصر لزبون")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("pressing_screen")
        ) {
            // Header Stats
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "عصر الزيتون للزبائن وإدارة الأجور",
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
                            Text(text = "إجمالي الزيتون", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${viewModel.formatNumber(totalOliveKg)} كغ",
                                fontWeight = FontWeight.Bold,
                                color = OlivePrimary
                            )
                        }
                        Column {
                            Text(text = "الزيت المستخرج", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${viewModel.formatNumber(totalOilKg)} كغ",
                                fontWeight = FontWeight.Bold,
                                color = OilGold
                            )
                        }
                        Column {
                            Text(text = "متوسط الرد", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${viewModel.formatNumber(avgYield, 1)} %",
                                fontWeight = FontWeight.ExtraBold,
                                color = OliveSuccess
                            )
                        }
                        Column {
                            Text(text = "ديون الزبائن", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = viewModel.formatCurrency(totalRemainingDebts, settings.currencySymbol),
                                fontWeight = FontWeight.Bold,
                                color = if (totalRemainingDebts > 0) OliveDebtRed else OliveSuccess
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
                    .testTag("pressing_search_input"),
                placeholder = { Text("بحث باسم الزبون أو رقم الإيصال أو الهاتف...") },
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

            // List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد عمليات عصر مطابقة",
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
                    items(filteredList, key = { it.id }) { op ->
                        PressingItemCard(
                            op = op,
                            viewModel = viewModel,
                            settings = settings,
                            onClick = { viewModel.showInvoicePreview(InvoicePreviewData.PressingReceipt(op)) },
                            onDelete = { itemToDelete = op }
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddPressingBottomSheet(
            viewModel = viewModel,
            settings = settings,
            onDismiss = { showAddSheet = false }
        )
    }

    itemToDelete?.let { op ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("تأكيد حذف العملية") },
            text = { Text("هل أنت متأكد من حذف إيصال العصر رقم ${op.receiptNumber} للزبون ${op.customerName}؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePressing(op)
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
private fun PressingItemCard(
    op: PressingOperation,
    viewModel: PressViewModel,
    settings: PressSettings,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("pressing_card_${op.receiptNumber}"),
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
                            .background(OilGold.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = OilGold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = op.customerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${op.receiptNumber} | ${viewModel.formatDateShort(op.dateMillis)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = OilGold.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "رد ${viewModel.formatNumber(op.extractionRate, 1)}%",
                        color = OilGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
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
                    Text(text = "زيتون مستلم", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${viewModel.formatNumber(op.oliveWeightKg)} كغ", fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "زيت منتج كلي", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${viewModel.formatNumber(op.producedOilKg)} كغ", fontWeight = FontWeight.Bold, color = OilGold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "المسلم للزبون", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${viewModel.formatNumber(op.deliveredOilKg)} كغ", fontWeight = FontWeight.Bold, color = OliveSuccess)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fees breakdown
            val feeLabel = when (op.feeType) {
                "PER_KG" -> "أجرة بالكغ"
                "PER_TON" -> "أجرة بالطن"
                "PER_TIN" -> "أجرة بالتنكة"
                "OIL_PERCENTAGE" -> "رد عيني ${op.feeRate}% زيت"
                else -> "أجرة عينية"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "طريقة الأجرة: $feeLabel" + if (op.emptyTinsProvided > 0) " + (${op.emptyTinsProvided} تنكة فارغة)" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "المطلوب: ${viewModel.formatCurrency(op.totalFeeCash, settings.currencySymbol)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (op.remainingAmountCash > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "المدفوع نقداً: ${viewModel.formatCurrency(op.paidAmountCash, settings.currencySymbol)}", style = MaterialTheme.typography.bodySmall, color = OliveSuccess)
                    Text(text = "المتبقي دين: ${viewModel.formatCurrency(op.remainingAmountCash, settings.currencySymbol)}", style = MaterialTheme.typography.bodySmall, color = OliveDebtRed, fontWeight = FontWeight.Bold)
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
private fun AddPressingBottomSheet(
    viewModel: PressViewModel,
    settings: PressSettings,
    onDismiss: () -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var useWeighbridge by remember { mutableStateOf(false) } // وزن القبان
    var grossWeightText by remember { mutableStateOf("") }
    var tareWeightText by remember { mutableStateOf("") }
    var oliveWeightText by remember { mutableStateOf("") }
    var producedOilText by remember { mutableStateOf("") }

    val grossKg = grossWeightText.toDoubleOrNull() ?: 0.0
    val tareKg = tareWeightText.toDoubleOrNull() ?: 0.0
    val weighbridgeNetKg = (grossKg - tareKg).coerceAtLeast(0.0)

    val directOliveKg = oliveWeightText.toDoubleOrNull() ?: 0.0
    val oliveWeight = if (useWeighbridge) weighbridgeNetKg else directOliveKg
    val producedOil = producedOilText.toDoubleOrNull() ?: 0.0
    val extractionRate = if (oliveWeight > 0) (producedOil / oliveWeight) * 100 else 0.0

    // Fee Types: PER_KG, PER_TON, PER_TIN, OIL_PERCENTAGE
    var selectedFeeType by remember { mutableStateOf("PER_KG") }
    var feeRateText by remember {
        mutableStateOf(
            when (selectedFeeType) {
                "PER_KG" -> settings.defaultPressingFeePerKg.toInt().toString()
                "PER_TIN" -> settings.defaultPressingFeePerTin.toInt().toString()
                "OIL_PERCENTAGE" -> settings.defaultRetentionPercent.toInt().toString()
                else -> "0"
            }
        )
    }

    var emptyTinsCountText by remember { mutableStateOf("0") }
    var emptyTinPriceText by remember { mutableStateOf(settings.defaultEmptyTinPrice.toInt().toString()) }

    var pomaceWeightText by remember { mutableStateOf("") }
    var pomaceRetainedByPress by remember { mutableStateOf(true) }

    var paidAmountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val tinCap = if (settings.tinCapacityKg > 0) settings.tinCapacityKg else 16.0
    val estimatedTins = if (producedOil > 0) producedOil / tinCap else 0.0

    val feeRate = feeRateText.toDoubleOrNull() ?: 0.0
    val emptyTinsCount = emptyTinsCountText.toIntOrNull() ?: 0
    val emptyTinPrice = emptyTinPriceText.toDoubleOrNull() ?: 0.0

    // Calculations
    var cashPressingFee = 0.0
    var retainedOilKg = 0.0

    when (selectedFeeType) {
        "PER_KG" -> cashPressingFee = oliveWeight * feeRate
        "PER_TON" -> cashPressingFee = (oliveWeight / 1000.0) * feeRate
        "PER_TIN" -> cashPressingFee = estimatedTins * feeRate
        "OIL_PERCENTAGE" -> {
            retainedOilKg = producedOil * (feeRate / 100.0)
            cashPressingFee = 0.0
        }
    }

    val deliveredOil = (producedOil - retainedOilKg).coerceAtLeast(0.0)
    val emptyTinsTotal = emptyTinsCount * emptyTinPrice
    val totalCashRequired = cashPressingFee + emptyTinsTotal

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
                .testTag("add_pressing_modal")
        ) {
            Text(
                text = "تسجيل عملية عصر زيتون للزبون",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OlivePrimary
            )
            Text(
                text = "حساب فوري لنسبة الاستخلاص والأجرة نقداً أو عيناً (رد زيت)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("اسم الزبون *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pressing_customer_name_input"),
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

            // Weighing Method Selector
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
                        text = "وزن مباشر (صافي)",
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
                    text = "وزن الزيتون الصافي = ${viewModel.formatNumber(weighbridgeNetKg)} كغ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = OlivePrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = producedOilText,
                    onValueChange = { producedOilText = it },
                    label = { Text("الزيت الناتج (كغ) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pressing_oil_weight_input"),
                    singleLine = true
                )
            } else {
                // Weights row: Olive weight & Oil produced
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = oliveWeightText,
                        onValueChange = { oliveWeightText = it },
                        label = { Text("وزن الزيتون (كغ) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pressing_olive_weight_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = producedOilText,
                        onValueChange = { producedOilText = it },
                        label = { Text("الزيت الناتج (كغ) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pressing_oil_weight_input"),
                        singleLine = true
                    )
                }
            }

            // Live Extraction Rate Preview
            if (oliveWeight > 0 && producedOil > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = OilGold.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نسبة الاستخلاص (الرد): ${viewModel.formatNumber(extractionRate, 2)} %",
                            fontWeight = FontWeight.Bold,
                            color = OilGold
                        )
                        Text(
                            text = "≈ ${viewModel.formatNumber(estimatedTins, 1)} تنكة (سعة $tinCap كغ)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fee Type Options
            Text(text = "طريقة احتساب أجرة العصر:", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "PER_KG" to "بالكيلو",
                    "PER_TON" to "بالطن",
                    "PER_TIN" to "بالتنكة",
                    "OIL_PERCENTAGE" to "رد عيني (% زيت)"
                ).forEach { (typeKey, typeLabel) ->
                    val isSelected = selectedFeeType == typeKey
                    Surface(
                        onClick = {
                            selectedFeeType = typeKey
                            feeRateText = when (typeKey) {
                                "PER_KG" -> settings.defaultPressingFeePerKg.toInt().toString()
                                "PER_TIN" -> settings.defaultPressingFeePerTin.toInt().toString()
                                "PER_TON" -> (settings.defaultPressingFeePerKg * 1000).toInt().toString()
                                "OIL_PERCENTAGE" -> settings.defaultRetentionPercent.toInt().toString()
                                else -> "0"
                            }
                        },
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

            Spacer(modifier = Modifier.height(10.dp))

            // Fee Rate Input
            val rateLabel = when (selectedFeeType) {
                "PER_KG" -> "أجرة العصر لكل كغ زيتون (${settings.currencySymbol})"
                "PER_TON" -> "أجرة العصر لكل طن زيتون (${settings.currencySymbol})"
                "PER_TIN" -> "أجرة العصر لكل تنكة زيت ناتجة (${settings.currencySymbol})"
                "OIL_PERCENTAGE" -> "نسبة الزيت المحتجز للمعصرة كأجرة عينية (%)"
                else -> "الأجرة"
            }

            OutlinedTextField(
                value = feeRateText,
                onValueChange = { feeRateText = it },
                label = { Text(rateLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pressing_fee_rate_input"),
                singleLine = true
            )

            if (selectedFeeType == "OIL_PERCENTAGE" && retainedOilKg > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "كمية الزيت المحتجزة للمعصرة: ${viewModel.formatNumber(retainedOilKg)} كغ | المسلم للزبون: ${viewModel.formatNumber(deliveredOil)} كغ",
                    style = MaterialTheme.typography.bodySmall,
                    color = OlivePrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Empty Tins Purchased from Press
            Text(text = "شراء تنكات فارغة من المعصرة:", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = emptyTinsCountText,
                    onValueChange = { emptyTinsCountText = it },
                    label = { Text("عدد التنكات الفارغة") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = emptyTinPriceText,
                    onValueChange = { emptyTinPriceText = it },
                    label = { Text("سعر التنكة (${settings.currencySymbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pomace / Jift
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "احتفاظ المعصرة بتفل الزيتون (الجفت):", style = MaterialTheme.typography.bodySmall)
                Switch(
                    checked = pomaceRetainedByPress,
                    onCheckedChange = { pomaceRetainedByPress = it }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calculated Cash Due Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("إجمالي الأجرة النقدية المطلوبة:", fontWeight = FontWeight.Bold)
                        Text(
                            text = viewModel.formatCurrency(totalCashRequired, settings.currencySymbol),
                            fontWeight = FontWeight.ExtraBold,
                            color = OlivePrimary,
                            fontSize = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Paid Amount Cash
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = paidAmountText,
                    onValueChange = { paidAmountText = it },
                    label = { Text("المبلغ المسدد نقداً (${settings.currencySymbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pressing_paid_input"),
                    singleLine = true
                )

                OutlinedButton(
                    onClick = { paidAmountText = totalCashRequired.toInt().toString() },
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text("سداد كامل")
                }
            }

            val paidAmountCash = paidAmountText.toDoubleOrNull() ?: 0.0
            val remainingCash = (totalCashRequired - paidAmountCash).coerceAtLeast(0.0)

            if (totalCashRequired > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "المتبقي بذمة الزبون (دين): ${viewModel.formatCurrency(remainingCash, settings.currencySymbol)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (remainingCash > 0) OliveDebtRed else OliveSuccess,
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
                    if (customerName.isNotBlank() && oliveWeight > 0 && producedOil > 0) {
                        val pomaceKg = pomaceWeightText.toDoubleOrNull() ?: (oliveWeight * 0.45) // تقريباً 45% تفل
                        viewModel.addPressing(
                            customerName = customerName,
                            phone = phone,
                            oliveWeightKg = oliveWeight,
                            producedOilKg = producedOil,
                            feeType = selectedFeeType,
                            feeRate = feeRate,
                            emptyTinsProvided = emptyTinsCount,
                            emptyTinPrice = emptyTinPrice,
                            tinsFilledCount = estimatedTins,
                            pomaceWeightKg = pomaceKg,
                            pomaceRetainedByPress = pomaceRetainedByPress,
                            paidAmountCash = paidAmountCash,
                            notes = notes,
                            grossWeightKg = if (useWeighbridge) grossKg else 0.0,
                            tareWeightKg = if (useWeighbridge) tareKg else 0.0
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_pressing_button"),
                enabled = customerName.isNotBlank() && oliveWeight > 0 && producedOil > 0,
                colors = ButtonDefaults.buttonColors(containerColor = OlivePrimary)
            ) {
                Text("حفظ العملية وإصدار إيصال التسليم", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
