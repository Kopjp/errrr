package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.PressSettings
import com.example.ui.theme.OliveDebtRed
import com.example.ui.theme.OlivePrimary
import com.example.ui.viewmodel.PressViewModel

@Composable
fun SettingsScreen(viewModel: PressViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var pressName by remember(settings) { mutableStateOf(settings.pressName) }
    var ownerName by remember(settings) { mutableStateOf(settings.ownerName) }
    var phone by remember(settings) { mutableStateOf(settings.phoneNumber) }
    var address by remember(settings) { mutableStateOf(settings.address) }
    var currencySymbol by remember(settings) { mutableStateOf(settings.currencySymbol) }
    var tinCapacityText by remember(settings) { mutableStateOf(settings.tinCapacityKg.toString()) }
    var feePerKgText by remember(settings) { mutableStateOf(settings.defaultPressingFeePerKg.toInt().toString()) }
    var feePerTinText by remember(settings) { mutableStateOf(settings.defaultPressingFeePerTin.toInt().toString()) }
    var emptyTinPriceText by remember(settings) { mutableStateOf(settings.defaultEmptyTinPrice.toInt().toString()) }
    var retentionPercentText by remember(settings) { mutableStateOf(settings.defaultRetentionPercent.toInt().toString()) }
    var footerText by remember(settings) { mutableStateOf(settings.invoiceFooterText) }

    var showResetDialog by remember { mutableStateOf(false) }
    var showDemoLoadDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "إعدادات المعصرة والتسعير والعملة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OlivePrimary
            )
            Text(
                text = "يتم استخدام هذه البيانات في ترويسة الفواتير والإيصالات والاحتسابات الافتراضية",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "بيانات المعصرة والترويسة:", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = pressName,
                        onValueChange = { pressName = it },
                        label = { Text("اسم المعصرة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("اسم المدير / المالك") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم هاتف التواصل") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("العنوان / المنطقة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        // Currency Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "العملة المستخدمة في الحسابات:", fontWeight = FontWeight.Bold)

                    // Quick Currency chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("ل.س", "TL", "$", "د.أ", "ريال").forEach { curr ->
                            val isSelected = currencySymbol == curr
                            Surface(
                                onClick = { currencySymbol = curr },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) OlivePrimary else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = curr,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = currencySymbol,
                        onValueChange = { currencySymbol = it },
                        label = { Text("رمز أو اسم العملة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        // Default Prices & Capacity Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "الثوابت والتسعيرة الافتراضية:", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = tinCapacityText,
                        onValueChange = { tinCapacityText = it },
                        label = { Text("سعة التنكة القياسية (كغ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = feePerKgText,
                        onValueChange = { feePerKgText = it },
                        label = { Text("أجرة عصر كغ الزيتون الافتراضية ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = feePerTinText,
                        onValueChange = { feePerTinText = it },
                        label = { Text("أجرة عصر التنكة الافتراضية ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = emptyTinPriceText,
                        onValueChange = { emptyTinPriceText = it },
                        label = { Text("سعر بيع التنكة الفارغة الافتراضي ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = retentionPercentText,
                        onValueChange = { retentionPercentText = it },
                        label = { Text("النسبة المئوية الافتراضية للرد العيني من الزيت (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = footerText,
                        onValueChange = { footerText = it },
                        label = { Text("تذييل الفاتورة والإيصال") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    val tinCap = tinCapacityText.toDoubleOrNull() ?: 16.0
                    val feeKg = feePerKgText.toDoubleOrNull() ?: 800.0
                    val feeTin = feePerTinText.toDoubleOrNull() ?: 15000.0
                    val emptyPrice = emptyTinPriceText.toDoubleOrNull() ?: 25000.0
                    val retention = retentionPercentText.toDoubleOrNull() ?: 10.0

                    val updated = settings.copy(
                        pressName = pressName.trim(),
                        ownerName = ownerName.trim(),
                        phoneNumber = phone.trim(),
                        address = address.trim(),
                        currencySymbol = currencySymbol.trim(),
                        tinCapacityKg = tinCap,
                        defaultPressingFeePerKg = feeKg,
                        defaultPressingFeePerTin = feeTin,
                        defaultEmptyTinPrice = emptyPrice,
                        defaultRetentionPercent = retention,
                        invoiceFooterText = footerText.trim()
                    )
                    viewModel.updateSettings(updated)
                    Toast.makeText(context, "تم حفظ الإعدادات بنجاح", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_settings_button"),
                colors = ButtonDefaults.buttonColors(containerColor = OlivePrimary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("حفظ التعديلات", fontWeight = FontWeight.Bold)
            }
        }

        // Demo Data & Reset Actions
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "إدارة البيانات وقاعدة البيانات:", fontWeight = FontWeight.Bold)

                    OutlinedButton(
                        onClick = { showDemoLoadDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إعادة تحميل البيانات النموذجية للموسم")
                    }

                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OliveDebtRed)
                    ) {
                        Text("تصفير وبدء موسم جديد فارغ تماماً")
                    }
                }
            }
        }

        // Audit Trail Section (سجل الرقابة والتدقيق)
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "سجل التدقيق والرقابة (Audit Log):",
                        fontWeight = FontWeight.Bold,
                        color = OlivePrimary
                    )
                    Text(
                        text = "يوضح العمليات والإدخالات والتعديلات التي تمت في النظام",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val logs by viewModel.auditLogs.collectAsStateWithLifecycle()
                    if (logs.isEmpty()) {
                        Text(
                            text = "لا توجد حركات مسجلة في سجل التدقيق حالياً",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        logs.take(10).forEach { log ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = log.actionType, fontWeight = FontWeight.Bold, color = OlivePrimary, fontSize = 12.sp)
                                        Text(text = viewModel.formatDateShort(log.timestamp), style = MaterialTheme.typography.labelSmall)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = log.details, style = MaterialTheme.typography.bodySmall)
                                    Text(text = "المسؤول: ${log.operatorRole}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDemoLoadDialog) {
        AlertDialog(
            onDismissRequest = { showDemoLoadDialog = false },
            title = { Text("تحميل بيانات تجريبية للموسم") },
            text = { Text("سيتم استبدال البيانات الحالية بنموذج واقعي متكامل لموسم عصر الزيتون للتجربة.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAndLoadDemoData()
                        showDemoLoadDialog = false
                        Toast.makeText(context, "تم تحميل البيانات النموذجية", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OlivePrimary)
                ) {
                    Text("تأكيد التحميل")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDemoLoadDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("تصفير كافة البيانات") },
            text = { Text("تحذير: سيتم حذف كافة الفواتير والإيصالات والخزانات لبدء قاعدة بيانات فارغة للموسم الجديد. هل ترغب بالمتابعة؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllDataEmpty()
                        showResetDialog = false
                        Toast.makeText(context, "تم تصفير البيانات بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OliveDebtRed)
                ) {
                    Text("تصفير الآن")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
