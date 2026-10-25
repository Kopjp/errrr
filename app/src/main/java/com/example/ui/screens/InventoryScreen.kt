package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.InternalPressing
import com.example.data.models.OilTank
import com.example.ui.components.StockSummaryCards
import com.example.ui.theme.OilGold
import com.example.ui.theme.OliveDebtRed
import com.example.ui.theme.OlivePrimary
import com.example.ui.theme.OliveSuccess
import com.example.ui.viewmodel.PressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: PressViewModel) {
    val stock by viewModel.stockSummary.collectAsStateWithLifecycle()
    val tanks by viewModel.tanks.collectAsStateWithLifecycle()
    val internalPressings by viewModel.internalPressings.collectAsStateWithLifecycle()

    var showInternalPressingSheet by remember { mutableStateOf(false) }
    var showAddTankDialog by remember { mutableStateOf(false) }
    var tankToEdit by remember { mutableStateOf<OilTank?>(null) }
    var tankToDelete by remember { mutableStateOf<OilTank?>(null) }

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Button to run internal pressing batch
                Button(
                    onClick = { showInternalPressingSheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = OilGold),
                    modifier = Modifier.testTag("run_internal_milling_button")
                ) {
                    Icon(Icons.Default.PrecisionManufacturing, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("عصر دفعة من زيتون المعصرة", fontWeight = FontWeight.Bold)
                }

                FloatingActionButton(
                    onClick = { showAddTankDialog = true },
                    containerColor = OlivePrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_tank_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة خزان جديد")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("inventory_screen"),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Stock Cards
            item {
                Text(
                    text = "نظرة عامة على المخزون والمستودعات",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OlivePrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                StockSummaryCards(stock = stock, viewModel = viewModel)
            }

            // Tanks Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "خزانات الزيت المعدنية (ستانلس ستيل)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OlivePrimary
                    )
                    OutlinedButton(
                        onClick = { showAddTankDialog = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("خزان جديد", fontSize = 12.sp)
                    }
                }
            }

            if (tanks.isEmpty()) {
                item {
                    Text(
                        text = "لم يتم تعريف أي خزانات زيت بعد",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(tanks, key = { it.id }) { tank ->
                    OilTankCard(
                        tank = tank,
                        viewModel = viewModel,
                        onEdit = { tankToEdit = tank },
                        onDelete = { tankToDelete = tank }
                    )
                }
            }

            // Internal Pressing Batches History
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "سجل العصر الداخلي لمخزون المعصرة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OlivePrimary
                )
            }

            if (internalPressings.isEmpty()) {
                item {
                    Text(
                        text = "لا توجد دفعات عصر داخلي سابقة",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(internalPressings, key = { it.id }) { batch ->
                    InternalPressingCard(batch = batch, viewModel = viewModel)
                }
            }
        }
    }

    // Modal: Internal Pressing Batch
    if (showInternalPressingSheet) {
        InternalPressingBottomSheet(
            viewModel = viewModel,
            availableOlivesKg = stock.olivesStockKg,
            tanks = tanks,
            onDismiss = { showInternalPressingSheet = false }
        )
    }

    // Dialog: Add / Edit Tank
    if (showAddTankDialog || tankToEdit != null) {
        AddEditTankDialog(
            existingTank = tankToEdit,
            onDismiss = {
                showAddTankDialog = false
                tankToEdit = null
            },
            onSave = { name, cap, level, quality, notes ->
                viewModel.addOrUpdateTank(
                    id = tankToEdit?.id ?: 0L,
                    name = name,
                    capacityKg = cap,
                    currentLevelKg = level,
                    oilQuality = quality,
                    notes = notes
                )
                showAddTankDialog = false
                tankToEdit = null
            }
        )
    }

    // Delete Tank Confirmation
    tankToDelete?.let { tank ->
        AlertDialog(
            onDismissRequest = { tankToDelete = null },
            title = { Text("تأكيد حذف الخزان") },
            text = { Text("هل أنت متأكد من حذف ${tank.tankName}؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTank(tank)
                        tankToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OliveDebtRed)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { tankToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun OilTankCard(
    tank: OilTank,
    viewModel: PressViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val percentage = if (tank.capacityKg > 0) (tank.currentLevelKg / tank.capacityKg * 100).coerceIn(0.0, 100.0) else 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tank_card_${tank.id}"),
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
                        Icon(Icons.Default.Storage, contentDescription = null, tint = OilGold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = tank.tankName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text(text = "النوعية: ${tank.oilQuality}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = OlivePrimary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Level Progress
            val ratio = if (tank.capacityKg > 0) (tank.currentLevelKg / tank.capacityKg).toFloat().coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = OilGold,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "المستوى الحالي: ${viewModel.formatNumber(tank.currentLevelKg)} كغ",
                    fontWeight = FontWeight.Bold,
                    color = OilGold,
                    fontSize = 13.sp
                )
                Text(
                    text = "السعة: ${viewModel.formatNumber(tank.capacityKg)} كغ (${viewModel.formatNumber(percentage, 0)}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InternalPressingCard(
    batch: InternalPressing,
    viewModel: PressViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "دفعة: ${batch.batchNumber}", fontWeight = FontWeight.Bold)
                Text(text = viewModel.formatDateShort(batch.dateMillis), style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "زيتون معصور: ${viewModel.formatNumber(batch.oliveUsedKg)} كغ", style = MaterialTheme.typography.bodySmall)
                Text(text = "زيت ناتج: ${viewModel.formatNumber(batch.producedOilKg)} كغ", fontWeight = FontWeight.Bold, color = OilGold)
                Text(text = "الرد: ${viewModel.formatNumber(batch.extractionRate, 1)}%", fontWeight = FontWeight.Bold, color = OliveSuccess)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InternalPressingBottomSheet(
    viewModel: PressViewModel,
    availableOlivesKg: Double,
    tanks: List<OilTank>,
    onDismiss: () -> Unit
) {
    var oliveUsedText by remember { mutableStateOf("") }
    var producedOilText by remember { mutableStateOf("") }
    var pomaceText by remember { mutableStateOf("") }
    var selectedTankId by remember { mutableStateOf<Long?>(tanks.firstOrNull()?.id) }
    var notes by remember { mutableStateOf("") }

    val oliveUsed = oliveUsedText.toDoubleOrNull() ?: 0.0
    val producedOil = producedOilText.toDoubleOrNull() ?: 0.0
    val extractionRate = if (oliveUsed > 0) (producedOil / oliveUsed) * 100 else 0.0

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
                .testTag("internal_milling_modal")
        ) {
            Text(
                text = "عصر دفعة من مخزون زيتون المعصرة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OlivePrimary
            )
            Text(
                text = "يخصم وزن الزيتون من المخزون ويضاف الزيت المستخرج إلى الخزان المختار",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                color = OlivePrimary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "مخزون الزيتون المتاح حالياً: ${viewModel.formatNumber(availableOlivesKg)} كغ",
                    color = OlivePrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = oliveUsedText,
                onValueChange = { oliveUsedText = it },
                label = { Text("كمية الزيتون المراد عصرها (كغ) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = producedOilText,
                onValueChange = { producedOilText = it },
                label = { Text("كمية الزيت الناتجة (كغ) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (oliveUsed > 0 && producedOil > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "نسبة الاستخلاص للدفعة: ${viewModel.formatNumber(extractionRate, 2)} %",
                    fontWeight = FontWeight.Bold,
                    color = OilGold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = pomaceText,
                onValueChange = { pomaceText = it },
                label = { Text("كمية تفل الزيتون الناتج (كغ)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Select Tank
            Text(text = "إيداع الزيت الناتج في الخزان:", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tanks.forEach { tank ->
                    val isSelected = selectedTankId == tank.id
                    Surface(
                        onClick = { selectedTankId = tank.id },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) OilGold else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = tank.tankName,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                    if (oliveUsed > 0 && producedOil > 0) {
                        val pomace = pomaceText.toDoubleOrNull() ?: (oliveUsed * 0.45)
                        viewModel.addInternalPressing(
                            oliveUsedKg = oliveUsed,
                            producedOilKg = producedOil,
                            pomaceKg = pomace,
                            targetTankId = selectedTankId,
                            notes = notes
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_internal_milling_button"),
                enabled = oliveUsed > 0 && producedOil > 0,
                colors = ButtonDefaults.buttonColors(containerColor = OlivePrimary)
            ) {
                Text("تنفيذ عملية العصر وتحديث المخزون", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AddEditTankDialog(
    existingTank: OilTank?,
    onDismiss: () -> Unit,
    onSave: (name: String, capacity: Double, level: Double, quality: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(existingTank?.tankName ?: "") }
    var capacityText by remember { mutableStateOf(existingTank?.capacityKg?.toInt()?.toString() ?: "5000") }
    var currentLevelText by remember { mutableStateOf(existingTank?.currentLevelKg?.toInt()?.toString() ?: "0") }
    var quality by remember { mutableStateOf(existingTank?.oilQuality ?: "بكر ممتاز (Extra Virgin)") }
    var notes by remember { mutableStateOf(existingTank?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingTank == null) "إضافة خزان زيت جديد" else "تعديل بيانات الخزان") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الخزان *") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = capacityText,
                    onValueChange = { capacityText = it },
                    label = { Text("السعة القصوى (كغ) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                OutlinedTextField(
                    value = currentLevelText,
                    onValueChange = { currentLevelText = it },
                    label = { Text("المستوى الحالي (كغ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                OutlinedTextField(
                    value = quality,
                    onValueChange = { quality = it },
                    label = { Text("صنف أو جودة الزيت") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cap = capacityText.toDoubleOrNull() ?: 5000.0
                    val level = currentLevelText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        onSave(name, cap, level, quality, notes)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = OlivePrimary)
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
