package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.models.PressSettings
import com.example.ui.theme.OilGold
import com.example.ui.theme.OliveDebtRed
import com.example.ui.theme.OlivePrimary
import com.example.ui.theme.OliveSuccess
import com.example.ui.viewmodel.InvoicePreviewData
import com.example.ui.viewmodel.PressViewModel

@Composable
fun InvoiceDialog(
    data: InvoicePreviewData,
    viewModel: PressViewModel,
    settings: PressSettings,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // إعداد نص الفاتورة للمشاركة أو الطباعة
    val invoiceText = buildShareableText(data, viewModel, settings)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .testTag("invoice_preview_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_invoice_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                    Text(
                        text = "معاينة الفاتورة والإيصال",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OlivePrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Printable Invoice Paper
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Press Title
                        Text(
                            text = settings.pressName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = OlivePrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${settings.address} | هاتف: ${settings.phoneNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Invoice Specific Body
                        when (data) {
                            is InvoicePreviewData.PurchaseInvoice -> {
                                PurchaseInvoiceBody(data.purchase, viewModel, settings)
                            }
                            is InvoicePreviewData.PressingReceipt -> {
                                PressingReceiptBody(data.pressing, viewModel, settings)
                            }
                            is InvoicePreviewData.SaleInvoice -> {
                                SaleInvoiceBody(data.sale, viewModel, settings)
                            }
                            is InvoicePreviewData.CashVoucher -> {
                                CashVoucherBody(data.transaction, viewModel, settings)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Accounting Signatures & Stamp
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "توقيع المحاسب / أمين الصندوق",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = ".......................",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "توقيع العميل / المستلم",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = ".......................",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = settings.invoiceFooterText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons: Share & Print
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { shareInvoice(context, invoiceText) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_invoice_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = OlivePrimary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مشاركة الفاتورة", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { shareInvoice(context, invoiceText) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("print_invoice_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("طباعة / تصدير", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PurchaseInvoiceBody(
    p: com.example.data.models.OlivePurchase,
    vm: PressViewModel,
    s: PressSettings
) {
    Text(
        text = "فاتورة شراء زيتون لحساب المعصرة",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = OlivePrimary
    )
    Text(
        text = "رقم الفاتورة: ${p.invoiceNumber}",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
    )
    Text(
        text = "التاريخ: ${vm.formatDate(p.dateMillis)}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(12.dp))

    InvoiceDetailRow(label = "اسم المزارع / المورد", value = p.farmerName)
    if (p.phoneNumber.isNotEmpty()) {
        InvoiceDetailRow(label = "رقم الهاتف", value = p.phoneNumber)
    }
    InvoiceDetailRow(label = "صنف الزيتون", value = p.oliveVariety)

    if (p.grossWeightKg > 0.0 && p.tareWeightKg > 0.0) {
        InvoiceDetailRow(label = "وزن القبان القائم", value = "${vm.formatNumber(p.grossWeightKg)} كغ")
        InvoiceDetailRow(label = "وزن القبان الفارغ", value = "${vm.formatNumber(p.tareWeightKg)} كغ")
        InvoiceDetailRow(label = "الوزن الصافي المعتمد", value = "${vm.formatNumber(p.weightKg)} كغ", isBold = true)
    } else {
        InvoiceDetailRow(label = "الوزن الصافي", value = "${vm.formatNumber(p.weightKg)} كغ", isBold = true)
    }

    InvoiceDetailRow(label = "سعر الكيلو", value = vm.formatCurrency(p.pricePerUnit, s.currencySymbol))
    if (p.transportCost > 0) {
        InvoiceDetailRow(label = "أجور النقل والمصاريف", value = vm.formatCurrency(p.transportCost, s.currencySymbol))
    }

    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(modifier = Modifier.height(8.dp))

    InvoiceDetailRow(label = "المبلغ الإجمالي", value = vm.formatCurrency(p.totalAmount, s.currencySymbol), isBold = true)
    InvoiceDetailRow(label = "المدفوع نقداً للمزارع", value = vm.formatCurrency(p.paidAmount, s.currencySymbol), valueColor = OliveSuccess)
    InvoiceDetailRow(
        label = "المتبقي للمزارع (دين)",
        value = vm.formatCurrency(p.remainingAmount, s.currencySymbol),
        valueColor = if (p.remainingAmount > 0) OliveDebtRed else OliveSuccess,
        isBold = true
    )

    if (p.notes.isNotEmpty()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "ملاحظات: ${p.notes}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun PressingReceiptBody(
    pr: com.example.data.models.PressingOperation,
    vm: PressViewModel,
    s: PressSettings
) {
    Text(
        text = "إيصال تسليم وعصر زيتون للزبون",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = OlivePrimary
    )
    Text(
        text = "رقم الإيصال: ${pr.receiptNumber}",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
    )
    Text(
        text = "التاريخ: ${vm.formatDate(pr.dateMillis)}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(12.dp))

    InvoiceDetailRow(label = "اسم الزبون", value = pr.customerName)
    if (pr.phoneNumber.isNotEmpty()) {
        InvoiceDetailRow(label = "رقم الهاتف", value = pr.phoneNumber)
    }

    if (pr.grossWeightKg > 0.0 && pr.tareWeightKg > 0.0) {
        InvoiceDetailRow(label = "وزن القبان القائم", value = "${vm.formatNumber(pr.grossWeightKg)} كغ")
        InvoiceDetailRow(label = "وزن القبان الفارغ", value = "${vm.formatNumber(pr.tareWeightKg)} كغ")
        InvoiceDetailRow(label = "وزن الزيتون الصافي", value = "${vm.formatNumber(pr.oliveWeightKg)} كغ", isBold = true)
    } else {
        InvoiceDetailRow(label = "وزن الزيتون المستلم", value = "${vm.formatNumber(pr.oliveWeightKg)} كغ", isBold = true)
    }

    InvoiceDetailRow(label = "كمية الزيت الناتجة", value = "${vm.formatNumber(pr.producedOilKg)} كغ", isBold = true)
    
    // نسبة الاستخلاص
    InvoiceDetailRow(
        label = "نسبة الاستخلاص (الرد)",
        value = "${vm.formatNumber(pr.extractionRate, 1)} %",
        valueColor = OilGold,
        isBold = true
    )

    if (pr.retainedOilKg > 0) {
        InvoiceDetailRow(label = "الزيت المحتجز كأجرة عينية", value = "${vm.formatNumber(pr.retainedOilKg)} كغ", valueColor = OlivePrimary)
    }
    InvoiceDetailRow(label = "الزيت المسلم للزبون", value = "${vm.formatNumber(pr.deliveredOilKg)} كغ", valueColor = OliveSuccess, isBold = true)
    InvoiceDetailRow(label = "عدد التنكات المعبأة", value = "${vm.formatNumber(pr.tinsFilledCount, 1)} تنكة")

    if (pr.emptyTinsProvided > 0) {
        InvoiceDetailRow(label = "تنكات فارغة مشتراة (${pr.emptyTinsProvided} تنكة)", value = vm.formatCurrency(pr.emptyTinsProvided * pr.emptyTinPrice, s.currencySymbol))
    }

    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(modifier = Modifier.height(8.dp))

    InvoiceDetailRow(label = "إجمالي الأجرة النقدية المطلوبة", value = vm.formatCurrency(pr.totalFeeCash, s.currencySymbol), isBold = true)
    InvoiceDetailRow(label = "المدفوع من الزبون", value = vm.formatCurrency(pr.paidAmountCash, s.currencySymbol), valueColor = OliveSuccess)
    InvoiceDetailRow(
        label = "المتبقي بذمة الزبون",
        value = vm.formatCurrency(pr.remainingAmountCash, s.currencySymbol),
        valueColor = if (pr.remainingAmountCash > 0) OliveDebtRed else OliveSuccess,
        isBold = true
    )
}

@Composable
private fun SaleInvoiceBody(
    sale: com.example.data.models.OilSale,
    vm: PressViewModel,
    s: PressSettings
) {
    val itemName = when (sale.itemType) {
        "OIL_TIN" -> "تنكة زيت زيتون (سعة ${s.tinCapacityKg} كغ)"
        "OIL_KG" -> "زيت زيتون بالكيلوغرام"
        "OIL_LITER" -> "زيت زيتون باللتر"
        "EMPTY_TIN" -> "تنكة فارغة جديدة"
        "POMACE" -> "تفل زيتون / بيرين"
        else -> sale.itemType
    }

    Text(
        text = "فاتورة بيع زيت ومنتجات",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = OlivePrimary
    )
    Text(
        text = "رقم الفاتورة: ${sale.invoiceNumber}",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
    )
    Text(
        text = "التاريخ: ${vm.formatDate(sale.dateMillis)}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(12.dp))

    InvoiceDetailRow(label = "اسم المشتري / الزبون", value = sale.customerName)
    if (sale.phoneNumber.isNotEmpty()) {
        InvoiceDetailRow(label = "رقم الهاتف", value = sale.phoneNumber)
    }
    InvoiceDetailRow(label = "المادة المباعة", value = itemName)
    InvoiceDetailRow(label = "الكمية", value = vm.formatNumber(sale.quantity))
    InvoiceDetailRow(label = "سعر الوحدة", value = vm.formatCurrency(sale.unitPrice, s.currencySymbol))
    if (sale.discount > 0) {
        InvoiceDetailRow(label = "الخصم الممنوح", value = vm.formatCurrency(sale.discount, s.currencySymbol), valueColor = OliveDebtRed)
    }

    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(modifier = Modifier.height(8.dp))

    InvoiceDetailRow(label = "الإجمالي الصافي", value = vm.formatCurrency(sale.totalAmount, s.currencySymbol), isBold = true)
    InvoiceDetailRow(label = "المسدد نقداً", value = vm.formatCurrency(sale.paidAmount, s.currencySymbol), valueColor = OliveSuccess)
    InvoiceDetailRow(
        label = "المتبقي بالدين",
        value = vm.formatCurrency(sale.remainingAmount, s.currencySymbol),
        valueColor = if (sale.remainingAmount > 0) OliveDebtRed else OliveSuccess,
        isBold = true
    )
}

@Composable
private fun CashVoucherBody(
    txn: com.example.data.models.CashTransaction,
    vm: PressViewModel,
    s: PressSettings
) {
    val title = when (txn.transactionType) {
        "EXPENSE" -> "سند صرف مصروفات تشغيلية"
        "RECEIPT" -> "سند قبض نقدي"
        "PAYMENT" -> "سند صرف وسداد"
        else -> "سند حركة صندوق"
    }

    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = OlivePrimary
    )
    Text(
        text = "رقم السند: ${txn.voucherNumber}",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
    )
    Text(
        text = "التاريخ: ${vm.formatDate(txn.dateMillis)}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(12.dp))

    InvoiceDetailRow(label = "البند / التصنيف", value = txn.category)
    if (txn.partyName.isNotEmpty()) {
        InvoiceDetailRow(label = "الطرف / المستلم", value = txn.partyName)
    }
    InvoiceDetailRow(label = "المبلغ", value = vm.formatCurrency(txn.amount, s.currencySymbol), isBold = true, valueColor = OlivePrimary)
    if (txn.notes.isNotEmpty()) {
        InvoiceDetailRow(label = "البيان والتفاصيل", value = txn.notes)
    }
}

@Composable
private fun InvoiceDetailRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (valueColor != Color.Unspecified) valueColor else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun buildShareableText(
    data: InvoicePreviewData,
    vm: PressViewModel,
    s: PressSettings
): String {
    val sb = StringBuilder()
    sb.appendLine("🌿 ${s.pressName} 🌿")
    sb.appendLine("${s.address} | هاتف: ${s.phoneNumber}")
    sb.appendLine("----------------------------------------")

    when (data) {
        is InvoicePreviewData.PurchaseInvoice -> {
            val p = data.purchase
            sb.appendLine("📄 فاتورة شراء زيتون [${p.invoiceNumber}]")
            sb.appendLine("التاريخ: ${vm.formatDate(p.dateMillis)}")
            sb.appendLine("المزارع: ${p.farmerName} ${p.phoneNumber}")
            sb.appendLine("الصنف: ${p.oliveVariety}")
            if (p.grossWeightKg > 0.0 && p.tareWeightKg > 0.0) {
                sb.appendLine("وزن القبان: قائم ${vm.formatNumber(p.grossWeightKg)} كغ | فارغ ${vm.formatNumber(p.tareWeightKg)} كغ")
                sb.appendLine("الوزن الصافي المعتمد: ${vm.formatNumber(p.weightKg)} كغ")
            } else {
                sb.appendLine("الوزن الصافي: ${vm.formatNumber(p.weightKg)} كغ")
            }
            sb.appendLine("السعر: ${vm.formatCurrency(p.pricePerUnit, s.currencySymbol)}")
            sb.appendLine("الإجمالي: ${vm.formatCurrency(p.totalAmount, s.currencySymbol)}")
            sb.appendLine("المدفوع: ${vm.formatCurrency(p.paidAmount, s.currencySymbol)}")
            sb.appendLine("المتبقي: ${vm.formatCurrency(p.remainingAmount, s.currencySymbol)}")
        }
        is InvoicePreviewData.PressingReceipt -> {
            val pr = data.pressing
            sb.appendLine("📄 إيصال عصر وتسليم [${pr.receiptNumber}]")
            sb.appendLine("التاريخ: ${vm.formatDate(pr.dateMillis)}")
            sb.appendLine("الزبون: ${pr.customerName} ${pr.phoneNumber}")
            if (pr.grossWeightKg > 0.0 && pr.tareWeightKg > 0.0) {
                sb.appendLine("وزن القبان: قائم ${vm.formatNumber(pr.grossWeightKg)} كغ | فارغ ${vm.formatNumber(pr.tareWeightKg)} كغ")
                sb.appendLine("وزن الزيتون الصافي: ${vm.formatNumber(pr.oliveWeightKg)} كغ")
            } else {
                sb.appendLine("وزن الزيتون: ${vm.formatNumber(pr.oliveWeightKg)} كغ")
            }
            sb.appendLine("الزيت المستخرج: ${vm.formatNumber(pr.producedOilKg)} كغ")
            sb.appendLine("نسبة الاستخلاص: ${vm.formatNumber(pr.extractionRate, 1)} %")
            if (pr.retainedOilKg > 0) {
                sb.appendLine("زيت محتجز للمعصرة: ${vm.formatNumber(pr.retainedOilKg)} كغ")
            }
            sb.appendLine("الزيت المسلم للزبون: ${vm.formatNumber(pr.deliveredOilKg)} كغ")
            sb.appendLine("التنكات المعبأة: ${vm.formatNumber(pr.tinsFilledCount, 1)} تنكة")
            sb.appendLine("الأجرة النقدية المطلوبة: ${vm.formatCurrency(pr.totalFeeCash, s.currencySymbol)}")
            sb.appendLine("المدفوع نقداً: ${vm.formatCurrency(pr.paidAmountCash, s.currencySymbol)}")
            sb.appendLine("المتبقي بذمة الزبون: ${vm.formatCurrency(pr.remainingAmountCash, s.currencySymbol)}")
        }
        is InvoicePreviewData.SaleInvoice -> {
            val sale = data.sale
            sb.appendLine("📄 فاتورة بيع زيت ومنتجات [${sale.invoiceNumber}]")
            sb.appendLine("التاريخ: ${vm.formatDate(sale.dateMillis)}")
            sb.appendLine("الزبون: ${sale.customerName} ${sale.phoneNumber}")
            sb.appendLine("المادة: ${sale.itemType} | الكمية: ${vm.formatNumber(sale.quantity)}")
            sb.appendLine("الإجمالي: ${vm.formatCurrency(sale.totalAmount, s.currencySymbol)}")
            sb.appendLine("المدفوع: ${vm.formatCurrency(sale.paidAmount, s.currencySymbol)}")
            sb.appendLine("المتبقي: ${vm.formatCurrency(sale.remainingAmount, s.currencySymbol)}")
        }
        is InvoicePreviewData.CashVoucher -> {
            val txn = data.transaction
            sb.appendLine("📄 سند مالي [${txn.voucherNumber}]")
            sb.appendLine("التاريخ: ${vm.formatDate(txn.dateMillis)}")
            sb.appendLine("النوع: ${txn.category}")
            sb.appendLine("الطرف: ${txn.partyName}")
            sb.appendLine("المبلغ: ${vm.formatCurrency(txn.amount, s.currencySymbol)}")
            sb.appendLine("البيان: ${txn.notes}")
        }
    }

    sb.appendLine("----------------------------------------")
    sb.appendLine(s.invoiceFooterText)
    return sb.toString()
}

private fun shareInvoice(context: Context, text: String) {
    try {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة الفاتورة").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        android.util.Log.e("InvoiceDialog", "Failed to share invoice", e)
    }
}
