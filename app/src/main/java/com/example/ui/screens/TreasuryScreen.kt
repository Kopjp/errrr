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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import com.example.data.models.CashTransaction
import com.example.data.models.PressSettings
import com.example.ui.theme.OilGold
import com.example.ui.theme.OliveDebtRed
import com.example.ui.theme.OlivePrimary
import com.example.ui.theme.OliveSuccess
import com.example.ui.viewmodel.InvoicePreviewData
import com.example.ui.viewmodel.PressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreasuryScreen(viewModel: PressViewModel) {
    val transactions by viewModel.cashTransactions.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val metrics by viewModel.financialMetrics.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, EXPENSE, RECEIPT, PAYMENT
    var showAddSheet by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<CashTransaction?>(null) }

    val filteredList = remember(transactions, selectedFilter) {
        when (selectedFilter) {
            "EXPENSE" -> transactions.filter { it.transactionType == "EXPENSE" }
            "RECEIPT" -> transactions.filter { it.transactionType == "RECEIPT" || it.transactionType == "CAPITAL" }
            "PAYMENT" -> transactions.filter { it.transactionType == "PAYMENT" }
            else -> transactions
        }
    }

    val totalIn = transactions.filter { it.transactionType == "RECEIPT" || it.transactionType == "CAPITAL" }.sumOf { it.amount }
    val totalOut = transactions.filter { it.transactionType == "EXPENSE" || it.transactionType == "PAYMENT" }.sumOf { it.amount }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = OlivePrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_treasury_txn_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "تسجيل حركة مالية")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("treasury_screen")
        ) {
            // Cash Balance Big Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = OlivePrimary)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "رصيد الصندوق الفعلي (النقدية المتاحة)",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = viewModel.formatCurrency(metrics.treasuryCashBalance, settings.currencySymbol),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "إجمالي المقبوضات (+)", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                            Text(text = viewModel.formatCurrency(totalIn, settings.currencySymbol), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "إجمالي المدفوعات والمصاريف (-)", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                            Text(text = viewModel.formatCurrency(totalOut, settings.currencySymbol), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "ALL" to "كافة الحركات",
                    "EXPENSE" to "مصاريف تشغيلية",
                    "RECEIPT" to "مقبوضات",
                    "PAYMENT" to "مدفوعات"
                ).forEach { (fKey, fLabel) ->
                    val isSelected = selectedFilter == fKey
                    Surface(
                        onClick = { selectedFilter = fKey },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) OlivePrimary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = fLabel,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Transactions list
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد حركات مالية مسجلة في هذا البند",
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
                    items(filteredList, key = { it.id }) { txn ->
                        TransactionItemCard(
                            txn = txn,
                            viewModel = viewModel,
                            settings = settings,
                            onClick = { viewModel.showInvoicePreview(InvoicePreviewData.CashVoucher(txn)) },
                            onDelete = { itemToDelete = txn }
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddTransactionBottomSheet(
            viewModel = viewModel,
            currency = settings.currencySymbol,
            onDismiss = { showAddSheet = false }
        )
    }

    itemToDelete?.let { txn ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("تأكيد حذف السند") },
            text = { Text("هل أنت متأكد من حذف ${txn.category} بمبلغ ${viewModel.formatCurrency(txn.amount, settings.currencySymbol)}؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCashTransaction(txn)
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
private fun TransactionItemCard(
    txn: CashTransaction,
    viewModel: PressViewModel,
    settings: PressSettings,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncoming = txn.transactionType == "RECEIPT" || txn.transactionType == "CAPITAL"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("txn_card_${txn.voucherNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(if (isIncoming) OliveSuccess.copy(alpha = 0.15f) else OliveDebtRed.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncoming) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isIncoming) OliveSuccess else OliveDebtRed,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(text = txn.category, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    if (txn.partyName.isNotEmpty()) {
                        Text(text = txn.partyName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = "${txn.voucherNumber} | ${viewModel.formatDateShort(txn.dateMillis)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isIncoming) "+" else "-") + viewModel.formatCurrency(txn.amount, settings.currencySymbol),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isIncoming) OliveSuccess else OliveDebtRed,
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionBottomSheet(
    viewModel: PressViewModel,
    currency: String,
    onDismiss: () -> Unit
) {
    var txnType by remember { mutableStateOf("EXPENSE") } // EXPENSE, RECEIPT, PAYMENT
    var category by remember { mutableStateOf("محروقات وديزل للمعصرة") }
    var amountText by remember { mutableStateOf("") }
    var partyName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
                .testTag("add_treasury_txn_modal")
        ) {
            Text(
                text = "تسجيل حركة مالية في الصندوق",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OlivePrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Transaction Type Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "EXPENSE" to "مصروف تشغيلي",
                    "RECEIPT" to "سند قبض",
                    "PAYMENT" to "سند صرف"
                ).forEach { (tKey, tLabel) ->
                    val isSelected = txnType == tKey
                    Surface(
                        onClick = {
                            txnType = tKey
                            category = when (tKey) {
                                "EXPENSE" -> "محروقات وديزل للمعصرة"
                                "RECEIPT" -> "تحصيل دفعة من زبون"
                                else -> "سداد دفعة لمورد"
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) OlivePrimary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = tLabel,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Category suggestions
            Text(text = "البند / التصنيف:", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(6.dp))

            val suggestions = when (txnType) {
                "EXPENSE" -> listOf("محروقات وديزل للمعصرة", "أجور وعمالة", "صيانة وقطع غيار", "كهرباء ومياه", "شراء مواد ومستلزمات", "ضيافة ومصاريف إدارة")
                "RECEIPT" -> listOf("تحصيل دفعة من زبون", "إيداع رأس مال", "إيرادات أخرى")
                else -> listOf("سداد دفعة لمورد", "سحب أرباح", "مدفوعات أخرى")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                suggestions.take(3).forEach { cat ->
                    val isSelected = category == cat
                    Surface(
                        onClick = { category = cat },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) OlivePrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("بيان البند أو التصنيف *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("المبلغ ($currency) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("txn_amount_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = partyName,
                onValueChange = { partyName = it },
                label = { Text(if (txnType == "RECEIPT") "اسم الدافع (الزبون)" else "اسم المستلم / الجهة") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("تفاصيل وبيان إضافي") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (amount > 0 && category.isNotBlank()) {
                        viewModel.addCashTransaction(
                            type = txnType,
                            category = category,
                            amount = amount,
                            partyName = partyName,
                            notes = notes
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_txn_button"),
                enabled = amount > 0 && category.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = OlivePrimary)
            ) {
                Text("حفظ وتسجيل السند المالي", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
