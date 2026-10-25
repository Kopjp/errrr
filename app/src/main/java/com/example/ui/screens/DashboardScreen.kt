package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.StockSummaryCards
import com.example.ui.theme.OilGold
import com.example.ui.theme.OliveDebtRed
import com.example.ui.theme.OlivePrimary
import com.example.ui.theme.OliveSuccess
import com.example.ui.viewmodel.FinancialMetrics
import com.example.ui.viewmodel.InvoicePreviewData
import com.example.ui.viewmodel.PressViewModel
import com.example.ui.viewmodel.ScreenTab

@Composable
fun DashboardScreen(
    viewModel: PressViewModel,
    onNavigateToTab: (ScreenTab) -> Unit,
    onQuickAction: (String) -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val metrics by viewModel.financialMetrics.collectAsStateWithLifecycle()
    val stock by viewModel.stockSummary.collectAsStateWithLifecycle()
    val pressings by viewModel.pressings.collectAsStateWithLifecycle()
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Enterprise Executive Header
        item {
            EnterpriseExecutiveHeader(
                pressName = settings.pressName,
                address = settings.address,
                ownerName = settings.ownerName,
                metrics = metrics,
                viewModel = viewModel,
                currency = settings.currencySymbol
            )
        }

        // 2. Financial Metrics Grid
        item {
            FinancialAccountingGrid(metrics = metrics, viewModel = viewModel, currency = settings.currencySymbol)
        }

        // 3. Operational Quick Actions
        item {
            EnterpriseQuickActions(
                onPurchaseClick = { onNavigateToTab(ScreenTab.PURCHASES) },
                onPressingClick = { onNavigateToTab(ScreenTab.PRESSING) },
                onSaleClick = { onNavigateToTab(ScreenTab.SALES) },
                onExpenseClick = { onNavigateToTab(ScreenTab.TREASURY) }
            )
        }

        // 4. Inventory & Asset Valuation Card
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = OlivePrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "المخزون الفعلي والأصول السلعية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OlivePrimary
                        )
                    }
                    Text(
                        text = "إدارة المخازن والخزانات ←",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = OlivePrimary,
                        modifier = Modifier.clickable { onNavigateToTab(ScreenTab.INVENTORY) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                StockSummaryCards(stock = stock, viewModel = viewModel)
            }
        }

        // 5. Recent Pressing Vouchers
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "دفتر عمليات العصر الحديثة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OlivePrimary
                    )
                    Text(
                        text = "عرض كافة العمليات (${pressings.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = OlivePrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToTab(ScreenTab.PRESSING) }
                    )
                }
            }
        }

        if (pressings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "لا توجد عمليات عصر مسجلة في الموسم الحالي حتى الآن",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(pressings.take(4)) { op ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .clickable { viewModel.showInvoicePreview(InvoicePreviewData.PressingReceipt(op)) }
                        .testTag("pressing_item_${op.receiptNumber}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = OlivePrimary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = op.receiptNumber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OlivePrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = op.customerName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "زيتون: ${viewModel.formatNumber(op.oliveWeightKg)} كغ | زيت: ${viewModel.formatNumber(op.producedOilKg)} كغ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = OilGold.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OilGold.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "رد ${viewModel.formatNumber(op.extractionRate, 1)}%",
                                    color = OilGold,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (op.remainingAmountCash > 0) "متبقي: ${viewModel.formatCurrency(op.remainingAmountCash, settings.currencySymbol)}" else "مسدد بالكامل",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (op.remainingAmountCash > 0) OliveDebtRed else OliveSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnterpriseExecutiveHeader(
    pressName: String,
    address: String,
    ownerName: String,
    metrics: FinancialMetrics,
    viewModel: PressViewModel,
    currency: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3012))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Enterprise Sub-header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "نظام إدارة ومحاسبة المعاصر • الموسم المالي 2026 / 2027",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "● المعصرة نشطة",
                    color = Color(0xFF81C784),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Title
            Text(
                text = pressName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "$address | الإدارة: $ownerName",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.White.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(10.dp))

            // Real-time Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "النقدية بالصندوق", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text(
                        text = viewModel.formatCurrency(metrics.treasuryCashBalance, currency),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "صافي الأرباح", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text(
                        text = viewModel.formatCurrency(metrics.netProfit, currency),
                        color = if (metrics.netProfit >= 0) Color(0xFF81C784) else Color(0xFFFF8A80),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "متوسط نسبة الرد", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text(
                        text = "${viewModel.formatNumber(metrics.averageExtractionRate, 1)} %",
                        color = Color(0xFFFFD54F),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FinancialAccountingGrid(
    metrics: FinancialMetrics,
    viewModel: PressViewModel,
    currency: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccountingMetricTile(
                title = "صافي الأرباح التشغيلية",
                amount = viewModel.formatCurrency(metrics.netProfit, currency),
                sub = "الإيرادات - التكاليف والمصاريف",
                accentColor = if (metrics.netProfit >= 0) OliveSuccess else OliveDebtRed,
                icon = if (metrics.netProfit >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                modifier = Modifier.weight(1f)
            )

            AccountingMetricTile(
                title = "النقدية الجاهزة بالصندوق",
                amount = viewModel.formatCurrency(metrics.treasuryCashBalance, currency),
                sub = "المقبوضات - المدفوعات",
                accentColor = OlivePrimary,
                icon = Icons.Default.AccountBalanceWallet,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccountingMetricTile(
                title = "الذمم المدينة (ديون الزبائن)",
                amount = viewModel.formatCurrency(metrics.customerDebtsTotal, currency),
                sub = "مبالغ مستحقة للمعصرة",
                accentColor = if (metrics.customerDebtsTotal > 0) OliveDebtRed else OliveSuccess,
                icon = Icons.Default.ReceiptLong,
                modifier = Modifier.weight(1f)
            )

            AccountingMetricTile(
                title = "الذمم الدائنة (مستحقات المزارعين)",
                amount = viewModel.formatCurrency(metrics.supplierDebtsTotal, currency),
                sub = "مبالغ بذمة المعصرة للموردين",
                accentColor = if (metrics.supplierDebtsTotal > 0) OliveDebtRed else OliveSuccess,
                icon = Icons.Default.Scale,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AccountingMetricTile(
    title: String,
    amount: String,
    sub: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun EnterpriseQuickActions(
    onPurchaseClick: () -> Unit,
    onPressingClick: () -> Unit,
    onSaleClick: () -> Unit,
    onExpenseClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "العمليات المحاسبية المباشرة",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OlivePrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EnterpriseActionButton(
                label = "شراء زيتون",
                subLabel = "توريد وقبان",
                icon = Icons.Default.Agriculture,
                onClick = onPurchaseClick,
                modifier = Modifier.weight(1f)
            )
            EnterpriseActionButton(
                label = "عصر لزبون",
                subLabel = "احتساب الرد",
                icon = Icons.Default.WaterDrop,
                onClick = onPressingClick,
                modifier = Modifier.weight(1f)
            )
            EnterpriseActionButton(
                label = "بيع زيت",
                subLabel = "تنكات ومخزون",
                icon = Icons.Default.PointOfSale,
                onClick = onSaleClick,
                modifier = Modifier.weight(1f)
            )
            EnterpriseActionButton(
                label = "سند مصروف",
                subLabel = "محروقات وأجور",
                icon = Icons.Default.AttachMoney,
                onClick = onExpenseClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EnterpriseActionButton(
    label: String,
    subLabel: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag("action_${label}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = OlivePrimary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 11.sp
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp
            )
        }
    }
}
