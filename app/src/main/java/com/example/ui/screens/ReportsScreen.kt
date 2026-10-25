package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.OilGold
import com.example.ui.theme.OliveDebtRed
import com.example.ui.theme.OlivePrimary
import com.example.ui.theme.OlivePrimaryContainer
import com.example.ui.theme.OliveSuccess
import com.example.ui.viewmodel.PressViewModel

@Composable
fun ReportsScreen(viewModel: PressViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val metrics by viewModel.financialMetrics.collectAsStateWithLifecycle()
    val stock by viewModel.stockSummary.collectAsStateWithLifecycle()
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val pressings by viewModel.pressings.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val cashTxns by viewModel.cashTransactions.collectAsStateWithLifecycle()
    val internalPressings by viewModel.internalPressings.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: الأرباح والخسائر, 1: كشف الذمم والديون, 2: كفاءة العصر, 3: حركة المواد
    var partyToSettle by remember { mutableStateOf<Triple<String, Double, Boolean>?>(null) } // Name, Amount, isCustomer

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen")
    ) {
        // Tab Header
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = OlivePrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("الأرباح والخسائر", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("الذمم والديون", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("الإنتاج والردود", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("حركة المواد", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // === تقرير الأرباح والخسائر ===
                    item {
                        ProfitAndLossCard(
                            metrics = metrics,
                            salesTotal = sales.sumOf { it.totalAmount },
                            pressingFeeTotal = pressings.sumOf { it.totalFeeCash },
                            purchasesCost = purchases.sumOf { it.totalAmount },
                            expensesCost = cashTxns.filter { it.transactionType == "EXPENSE" }.sumOf { it.amount },
                            currency = settings.currencySymbol,
                            viewModel = viewModel,
                            onShare = {
                                val reportText = buildPnLText(settings.pressName, metrics, sales.sumOf { it.totalAmount }, pressings.sumOf { it.totalFeeCash }, purchases.sumOf { it.totalAmount }, cashTxns.filter { it.transactionType == "EXPENSE" }.sumOf { it.amount }, settings.currencySymbol, viewModel)
                                shareReport(context, reportText)
                            }
                        )
                    }

                    item {
                        ExpenseBreakdownCard(
                            cashTxns = cashTxns.filter { it.transactionType == "EXPENSE" },
                            currency = settings.currencySymbol,
                            viewModel = viewModel
                        )
                    }
                }
                1 -> {
                    // === كشف الذمم والديون ===
                    item {
                        DebtsOverviewCard(
                            customerDebts = metrics.customerDebtsTotal,
                            supplierDebts = metrics.supplierDebtsTotal,
                            currency = settings.currencySymbol,
                            viewModel = viewModel
                        )
                    }

                    item {
                        Text(
                            text = "ذمم الزبائن المستحقة (لنا بذمتهم):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = OlivePrimary
                        )
                    }

                    val customersWithDebts = (pressings.filter { it.remainingAmountCash > 0 }.map { it.customerName to it.remainingAmountCash } +
                            sales.filter { it.remainingAmount > 0 }.map { it.customerName to it.remainingAmount })
                        .groupBy { it.first }
                        .mapValues { entry -> entry.value.sumOf { it.second } }

                    if (customersWithDebts.isEmpty()) {
                        item {
                            Text("لا توجد ديون مستحقة على الزبائن (كافة الحسابات مسددة)", style = MaterialTheme.typography.bodySmall, color = OliveSuccess)
                        }
                    } else {
                        items(customersWithDebts.toList()) { (name, amount) ->
                            PartyDebtRow(
                                name = name,
                                amount = amount,
                                currency = settings.currencySymbol,
                                isReceivable = true,
                                viewModel = viewModel,
                                onSettleClick = { partyToSettle = Triple(name, amount, true) },
                                onShareStatement = {
                                    val statement = viewModel.generatePartyStatement(name, isCustomer = true)
                                    shareReport(context, statement)
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "مستحقات الموردين (بذمتنا لهم):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = OlivePrimary
                        )
                    }

                    val suppliersWithDebts = purchases.filter { it.remainingAmount > 0 }
                        .groupBy { it.farmerName }
                        .mapValues { entry -> entry.value.sumOf { it.remainingAmount } }

                    if (suppliersWithDebts.isEmpty()) {
                        item {
                            Text("كافة مستحقات المزارعين والموردين مسددة بالكامل", style = MaterialTheme.typography.bodySmall, color = OliveSuccess)
                        }
                    } else {
                        items(suppliersWithDebts.toList()) { (name, amount) ->
                            PartyDebtRow(
                                name = name,
                                amount = amount,
                                currency = settings.currencySymbol,
                                isReceivable = false,
                                viewModel = viewModel,
                                onSettleClick = { partyToSettle = Triple(name, amount, false) },
                                onShareStatement = {
                                    val statement = viewModel.generatePartyStatement(name, isCustomer = false)
                                    shareReport(context, statement)
                                }
                            )
                        }
                    }
                }
                2 -> {
                    // === كفاءة العصر ونسب الاستخلاص ===
                    item {
                        YieldEfficiencyCard(
                            metrics = metrics,
                            viewModel = viewModel
                        )
                    }

                    item {
                        Text(
                            text = "سجل نسب استخلاص الدفعات الأخيرة:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = OlivePrimary
                        )
                    }

                    items(pressings.take(8)) { op ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = op.customerName, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "زيتون: ${viewModel.formatNumber(op.oliveWeightKg)} كغ | زيت: ${viewModel.formatNumber(op.producedOilKg)} كغ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = OilGold.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${viewModel.formatNumber(op.extractionRate, 1)}%",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = OilGold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // === سجل حركة المواد (كاردكس المخزون) ===
                    item {
                        MaterialLedgerCard(
                            purchasedOlivesKg = purchases.sumOf { it.weightKg },
                            usedOlivesKg = internalPressings.sumOf { it.oliveUsedKg },
                            olivesStockKg = stock.olivesStockKg,
                            producedOilKg = internalPressings.sumOf { it.producedOilKg },
                            retainedOilKg = pressings.sumOf { it.retainedOilKg },
                            soldOilKg = stock.oilStockKg,
                            emptyTinsCount = stock.emptyTinsCount,
                            pomaceStockKg = stock.pomaceStockKg,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }

    // Debt Settlement Dialog
    partyToSettle?.let { (partyName, currentDebt, isCustomer) ->
        var settleAmountText by remember { mutableStateOf(currentDebt.toInt().toString()) }
        var settleNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { partyToSettle = null },
            title = { Text(if (isCustomer) "تحصيل دفعة من الزبون $partyName" else "سداد دفعة للمزارع $partyName") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "الرصيد المتبقي الحالي: ${viewModel.formatCurrency(currentDebt, settings.currencySymbol)}", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = settleAmountText,
                        onValueChange = { settleAmountText = it },
                        label = { Text("المبلغ المدفوع (${settings.currencySymbol}) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = settleNotes,
                        onValueChange = { settleNotes = it },
                        label = { Text("ملاحظات") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = settleAmountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            if (isCustomer) {
                                viewModel.settleCustomerDebt(partyName, amount, settleNotes)
                            } else {
                                viewModel.settleSupplierDebt(partyName, amount, settleNotes)
                            }
                            partyToSettle = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OlivePrimary)
                ) {
                    Text("حفظ السند والترحيل للصندوق")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { partyToSettle = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun ProfitAndLossCard(
    metrics: com.example.ui.viewmodel.FinancialMetrics,
    salesTotal: Double,
    pressingFeeTotal: Double,
    purchasesCost: Double,
    expensesCost: Double,
    currency: String,
    viewModel: PressViewModel,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "قائمة الدخل والأرباح والخسائر (P&L)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OlivePrimary
                )
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "مشاركة التقرير", tint = OlivePrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Revenues
            Text(text = "الإيرادات التشغيلية (+):", fontWeight = FontWeight.Bold, color = OliveSuccess)
            ReportLine(label = "مبيعات الزيت والمنتجات والتنكات", value = viewModel.formatCurrency(salesTotal, currency))
            ReportLine(label = "أجور عصر زيتون الزبائن النقدية", value = viewModel.formatCurrency(pressingFeeTotal, currency))
            ReportLine(label = "إجمالي الإيرادات", value = viewModel.formatCurrency(metrics.totalRevenues, currency), isBold = true, valueColor = OliveSuccess)

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // Expenses & Costs
            Text(text = "التكاليف والمصروفات (-):", fontWeight = FontWeight.Bold, color = OliveDebtRed)
            ReportLine(label = "تكلفة مشتريات الزيتون لحساب المعصرة", value = viewModel.formatCurrency(purchasesCost, currency))
            ReportLine(label = "المصاريف التشغيلية (محروقات، أجور، صيانة...)", value = viewModel.formatCurrency(expensesCost, currency))
            val totalCosts = purchasesCost + expensesCost
            ReportLine(label = "إجمالي التكاليف والمصاريف", value = viewModel.formatCurrency(totalCosts, currency), isBold = true, valueColor = OliveDebtRed)

            Spacer(modifier = Modifier.height(14.dp))

            // Net Profit Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (metrics.netProfit >= 0) OlivePrimaryContainer.copy(alpha = 0.6f) else OliveDebtRed.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (metrics.netProfit >= 0) "صافي الربح التشغيلي للموسم" else "صافي العجز التشغيلي",
                            fontWeight = FontWeight.Bold,
                            color = if (metrics.netProfit >= 0) OlivePrimary else OliveDebtRed
                        )
                        Text(
                            text = "الإيرادات مطروحاً منها كافة التكاليف",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = viewModel.formatCurrency(metrics.netProfit, currency),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (metrics.netProfit >= 0) OlivePrimary else OliveDebtRed
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseBreakdownCard(
    cashTxns: List<com.example.data.models.CashTransaction>,
    currency: String,
    viewModel: PressViewModel
) {
    val grouped = cashTxns.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "توزيع بنود المصاريف التشغيلية",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = OlivePrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (grouped.isEmpty()) {
                Text(text = "لا توجد مصاريف تشغيلية مسجلة بعد", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                grouped.forEach { (category, total) ->
                    ReportLine(label = category, value = viewModel.formatCurrency(total, currency))
                }
            }
        }
    }
}

@Composable
private fun DebtsOverviewCard(
    customerDebts: Double,
    supplierDebts: Double,
    currency: String,
    viewModel: PressViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "ملخص الذمم والديون المتبقية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OlivePrimary)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "إجمالي ديون الزبائن (لنا)", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = viewModel.formatCurrency(customerDebts, currency),
                        fontWeight = FontWeight.ExtraBold,
                        color = if (customerDebts > 0) OliveDebtRed else OliveSuccess
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "إجمالي مستحقات الموردين (علينا)", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = viewModel.formatCurrency(supplierDebts, currency),
                        fontWeight = FontWeight.ExtraBold,
                        color = if (supplierDebts > 0) OliveDebtRed else OliveSuccess
                    )
                }
            }
        }
    }
}

@Composable
private fun PartyDebtRow(
    name: String,
    amount: Double,
    currency: String,
    isReceivable: Boolean,
    viewModel: PressViewModel,
    onSettleClick: () -> Unit,
    onShareStatement: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = viewModel.formatCurrency(amount, currency),
                    fontWeight = FontWeight.ExtraBold,
                    color = OliveDebtRed,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShareStatement,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("كشف حساب", fontSize = 11.sp)
                }

                Button(
                    onClick = onSettleClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isReceivable) OliveSuccess else OlivePrimary)
                ) {
                    Text(if (isReceivable) "قبض دفعة" else "صرف دفعة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MaterialLedgerCard(
    purchasedOlivesKg: Double,
    usedOlivesKg: Double,
    olivesStockKg: Double,
    producedOilKg: Double,
    retainedOilKg: Double,
    soldOilKg: Double,
    emptyTinsCount: Int,
    pomaceStockKg: Double,
    viewModel: PressViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "سجل حركة المواد ومخزون المعصرة (Kardex)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OlivePrimary
            )
            Text(
                text = "تتبع دقيق لمصادر كل مادة والكميات الواردة والمنصرفة والأرصدة الفعلية",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 1. الزيتون
            MaterialKardexItem(
                title = "1. الزيتون (Olive Stock)",
                inflow = "وارد (مشتريات من المزارعين): ${viewModel.formatNumber(purchasedOlivesKg)} كغ",
                outflow = "منصرف (عصر داخلي للمعصرة): ${viewModel.formatNumber(usedOlivesKg)} كغ",
                balance = "${viewModel.formatNumber(olivesStockKg)} كغ متبقي بالمخزن"
            )

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // 2. زيت الزيتون
            val totalInflowOil = producedOilKg + retainedOilKg
            MaterialKardexItem(
                title = "2. زيت الزيتون (Olive Oil)",
                inflow = "وارد (عصر داخلي: ${viewModel.formatNumber(producedOilKg)} كغ + ردود عينية: ${viewModel.formatNumber(retainedOilKg)} كغ)",
                outflow = "منصرف مبيعات للزبائن",
                balance = "${viewModel.formatNumber(soldOilKg)} كغ متاح بالخزانات"
            )

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // 3. التنكات الفارغة
            MaterialKardexItem(
                title = "3. التنكات الفارغة (Empty Tins)",
                inflow = "مخزون أساسي ومشتريات",
                outflow = "منصرف معبأ للزبائن ومباع",
                balance = "$emptyTinsCount تنكة فارغة متاحة"
            )

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // 4. تفل الزيتون (الجفت / البيرين)
            MaterialKardexItem(
                title = "4. تفل الزيتون / الجفت (Olive Cake / Pomace)",
                inflow = "ناتج عصر الزيتون للزبائن والمعصرة",
                outflow = "مبيعات وقود تدفئة ومصانع فحم",
                balance = "${viewModel.formatNumber(pomaceStockKg)} كغ متاح للبيع"
            )
        }
    }
}

@Composable
private fun MaterialKardexItem(
    title: String,
    inflow: String,
    outflow: String,
    balance: String
) {
    Column {
        Text(text = title, fontWeight = FontWeight.Bold, color = OlivePrimary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = "• $inflow", style = MaterialTheme.typography.bodySmall, color = OliveSuccess)
        Text(text = "• $outflow", style = MaterialTheme.typography.bodySmall, color = OliveDebtRed)
        Text(text = "• الرصيد الدفتري الحالي: $balance", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun YieldEfficiencyCard(
    metrics: com.example.ui.viewmodel.FinancialMetrics,
    viewModel: PressViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = OilGold.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "كفاءة الإنتاج ومعدلات استخلاص زيت الزيتون",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OilGold
            )
            Spacer(modifier = Modifier.height(10.dp))
            ReportLine(label = "إجمالي الزيتون المعصور في المعصرة", value = "${viewModel.formatNumber(metrics.totalOlivesPressedAllKg)} كغ")
            ReportLine(label = "إجمالي الزيت المستخرج الإجمالي", value = "${viewModel.formatNumber(metrics.totalOilProducedAllKg)} كغ", isBold = true)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "متوسط نسبة الاستخلاص العامة:", fontWeight = FontWeight.Bold)
                    Text(
                        text = "${viewModel.formatNumber(metrics.averageExtractionRate, 2)} %",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = OilGold
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportLine(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (valueColor != Color.Unspecified) valueColor else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun buildPnLText(
    pressName: String,
    metrics: com.example.ui.viewmodel.FinancialMetrics,
    sales: Double,
    pressing: Double,
    purchases: Double,
    expenses: Double,
    currency: String,
    vm: PressViewModel
): String {
    return """
        📊 تقرير الأرباح والخسائر - $pressName
        ----------------------------------------
        الإيرادات:
        - مبيعات الزيت والمنتجات: ${vm.formatCurrency(sales, currency)}
        - أجور عصر زيتون الزبائن: ${vm.formatCurrency(pressing, currency)}
        * إجمالي الإيرادات: ${vm.formatCurrency(metrics.totalRevenues, currency)}

        التكاليف والمصروفات:
        - تكلفة شراء الزيتون: ${vm.formatCurrency(purchases, currency)}
        - مصاريف التشغيل والصيانة: ${vm.formatCurrency(expenses, currency)}
        * إجمالي التكاليف: ${vm.formatCurrency(purchases + expenses, currency)}
        ----------------------------------------
        صافي الأرباح: ${vm.formatCurrency(metrics.netProfit, currency)}
        رصيد الصندوق: ${vm.formatCurrency(metrics.treasuryCashBalance, currency)}
        متوسط نسبة الاستخلاص: ${vm.formatNumber(metrics.averageExtractionRate, 1)}%
    """.trimIndent()
}

private fun shareReport(context: Context, text: String) {
    try {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(sendIntent, "مشاركة التقرير المالي").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    } catch (e: Exception) {
        android.util.Log.e("ReportsScreen", "Failed to share report", e)
    }
}
