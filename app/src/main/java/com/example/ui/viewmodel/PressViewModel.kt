package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.models.CashTransaction
import com.example.data.models.InternalPressing
import com.example.data.models.OilSale
import com.example.data.models.OilTank
import com.example.data.models.OlivePurchase
import com.example.data.models.PressSettings
import com.example.data.models.PressingOperation
import com.example.data.repository.PressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ScreenTab(val title: String) {
    DASHBOARD("الرئيسية"),
    PURCHASES("شراء الزيتون"),
    PRESSING("عصر الزبائن"),
    SALES("بيع الزيت"),
    INVENTORY("المخزون والخزانات"),
    TREASURY("الصندوق والمصاريف"),
    REPORTS("التقارير والأرباح"),
    SETTINGS("الإعدادات")
}

sealed class InvoicePreviewData {
    data class PurchaseInvoice(val purchase: OlivePurchase) : InvoicePreviewData()
    data class PressingReceipt(val pressing: PressingOperation) : InvoicePreviewData()
    data class SaleInvoice(val sale: OilSale) : InvoicePreviewData()
    data class CashVoucher(val transaction: CashTransaction) : InvoicePreviewData()
}

data class StockSummary(
    val olivesStockKg: Double,
    val oilStockKg: Double,
    val oilStockTins: Double,
    val emptyTinsCount: Int,
    val pomaceStockKg: Double,
    val tanksTotalStoredKg: Double,
    val tanksTotalCapacityKg: Double
)

data class FinancialMetrics(
    val totalRevenues: Double,
    val totalPurchasesCost: Double,
    val totalOperationalExpenses: Double,
    val netProfit: Double,
    val customerDebtsTotal: Double,
    val supplierDebtsTotal: Double,
    val treasuryCashBalance: Double,
    val totalOlivesPressedAllKg: Double,
    val totalOilProducedAllKg: Double,
    val averageExtractionRate: Double
)

class PressViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PressRepository

    private val coroutineExceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
        android.util.Log.e("PressViewModel", "Coroutine error caught safely", throwable)
    }

    private fun safeLaunch(block: suspend kotlinx.coroutines.CoroutineScope.() -> Unit) {
        viewModelScope.launch(coroutineExceptionHandler) {
            try {
                block()
            } catch (e: Exception) {
                android.util.Log.e("PressViewModel", "safeLaunch error", e)
            }
        }
    }

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PressRepository(db.pressDao())
        
        // Initial setup check: if no settings or data, seed demo data for seamless out-of-the-box experience
        safeLaunch {
            val count = db.pressDao().getPurchasesCount()
            if (count == 0) {
                repository.loadSeasonalDemoData()
            }
        }
    }

    // === Navigation State ===
    private val _currentTab = MutableStateFlow(ScreenTab.DASHBOARD)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    fun setTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    // === Invoice Preview / Print Modal State ===
    private val _invoicePreview = MutableStateFlow<InvoicePreviewData?>(null)
    val invoicePreview: StateFlow<InvoicePreviewData?> = _invoicePreview.asStateFlow()

    fun showInvoicePreview(preview: InvoicePreviewData) {
        _invoicePreview.value = preview
    }

    fun dismissInvoicePreview() {
        _invoicePreview.value = null
    }

    // === Flows from Database ===
    val settings: StateFlow<PressSettings> = repository.settings
        .map { it ?: PressSettings() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PressSettings()
        )

    val purchases: StateFlow<List<OlivePurchase>> = repository.purchases
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pressings: StateFlow<List<PressingOperation>> = repository.pressings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val sales: StateFlow<List<OilSale>> = repository.sales
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val internalPressings: StateFlow<List<InternalPressing>> = repository.internalPressings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val tanks: StateFlow<List<OilTank>> = repository.tanks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val cashTransactions: StateFlow<List<CashTransaction>> = repository.cashTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val auditLogs: StateFlow<List<com.example.data.models.AuditLog>> = repository.auditLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // === Stock Calculations ===
    val stockSummary: StateFlow<StockSummary> = combine(
        purchases,
        pressings,
        sales,
        internalPressings,
        tanks
    ) { pList, prList, sList, ipList, tList ->
        val set = settings.value ?: PressSettings()

        // الزيتون الخاص بالمعصرة
        val totalPurchasedOlives = pList.sumOf { it.weightKg }
        val totalInternalUsedOlives = ipList.sumOf { it.oliveUsedKg }
        val olivesStock = (totalPurchasedOlives - totalInternalUsedOlives).coerceAtLeast(0.0)

        // زيت الزيتون: ناتج العصر الداخلي + الرد العيني من الزبائن - مبيعات الزيت
        val internalOil = ipList.sumOf { it.producedOilKg }
        val retainedCustomerOil = prList.sumOf { it.retainedOilKg }
        
        val tinCap = if (set.tinCapacityKg > 0) set.tinCapacityKg else 16.0
        val oilSoldKg = sList.sumOf { sale ->
            when (sale.itemType) {
                "OIL_TIN" -> sale.quantity * tinCap
                "OIL_KG" -> sale.quantity
                "OIL_LITER" -> sale.quantity * 0.916 // تقريب كثافة الزيت 0.916 كغ لكل لتر
                else -> 0.0
            }
        }
        val currentOilKg = (internalOil + retainedCustomerOil - oilSoldKg).coerceAtLeast(0.0)
        val currentOilTins = currentOilKg / tinCap

        // مخزون التنكات الفارغة (قاعدة أولية 250 تنكة + مشتراة - مسلمة للزبائن - مباعة فارغة)
        val emptyTinsProvidedToCustomers = prList.sumOf { it.emptyTinsProvided }
        val emptyTinsSold = sList.filter { it.itemType == "EMPTY_TIN" }.sumOf { it.quantity.toInt() }
        val baseTins = 200
        val currentEmptyTins = (baseTins - emptyTinsProvidedToCustomers - emptyTinsSold).coerceAtLeast(0)

        // تفل الزيتون (البيرين/الجفت)
        val internalPomace = ipList.sumOf { it.pomaceProducedKg }
        val retainedCustomerPomace = prList.filter { it.pomaceRetainedByPress }.sumOf { it.pomaceWeightKg }
        val pomaceSold = sList.filter { it.itemType == "POMACE" }.sumOf { it.quantity }
        val currentPomaceKg = (internalPomace + retainedCustomerPomace - pomaceSold).coerceAtLeast(0.0)

        // إجمالي سعة وتخزين الخزانات
        val tanksStored = tList.sumOf { it.currentLevelKg }
        val tanksCapacity = tList.sumOf { it.capacityKg }

        StockSummary(
            olivesStockKg = olivesStock,
            oilStockKg = currentOilKg,
            oilStockTins = currentOilTins,
            emptyTinsCount = currentEmptyTins,
            pomaceStockKg = currentPomaceKg,
            tanksTotalStoredKg = tanksStored,
            tanksTotalCapacityKg = tanksCapacity
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StockSummary(0.0, 0.0, 0.0, 0, 0.0, 0.0, 0.0)
    )

    // === Financial & Operational Metrics ===
    val financialMetrics: StateFlow<FinancialMetrics> = combine(
        purchases,
        pressings,
        sales,
        cashTransactions,
        internalPressings
    ) { pList, prList, sList, ctList, ipList ->
        val totalPurchasesCost = pList.sumOf { it.totalAmount }
        val salesRevenue = sList.sumOf { it.totalAmount }
        val pressingFeeRevenue = prList.sumOf { it.totalFeeCash }
        val totalRevenues = salesRevenue + pressingFeeRevenue

        val operatingExpenses = ctList.filter { it.transactionType == "EXPENSE" }.sumOf { it.amount }
        val netProfit = totalRevenues - totalPurchasesCost - operatingExpenses

        val customerDebts = prList.sumOf { it.remainingAmountCash } + sList.sumOf { it.remainingAmount }
        val supplierDebts = pList.sumOf { it.remainingAmount }

        // رصيد الصندوق الفعلي = المقبوضات النقدية - المصروفات والمدفوعات النقدية
        val totalCashIn = ctList.filter { it.transactionType == "RECEIPT" || it.transactionType == "CAPITAL" }.sumOf { it.amount }
        val totalCashOut = ctList.filter { it.transactionType == "EXPENSE" || it.transactionType == "PAYMENT" }.sumOf { it.amount }
        val cashBalance = totalCashIn - totalCashOut

        // إحصائيات المعصرة الإجمالية
        val customerOlives = prList.sumOf { it.oliveWeightKg }
        val internalOlives = ipList.sumOf { it.oliveUsedKg }
        val totalOlivesPressed = customerOlives + internalOlives

        val customerOil = prList.sumOf { it.producedOilKg }
        val internalOil = ipList.sumOf { it.producedOilKg }
        val totalOilProduced = customerOil + internalOil

        val avgExtractionRate = if (totalOlivesPressed > 0) (totalOilProduced / totalOlivesPressed) * 100 else 0.0

        FinancialMetrics(
            totalRevenues = totalRevenues,
            totalPurchasesCost = totalPurchasesCost,
            totalOperationalExpenses = operatingExpenses,
            netProfit = netProfit,
            customerDebtsTotal = customerDebts,
            supplierDebtsTotal = supplierDebts,
            treasuryCashBalance = cashBalance,
            totalOlivesPressedAllKg = totalOlivesPressed,
            totalOilProducedAllKg = totalOilProduced,
            averageExtractionRate = avgExtractionRate
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinancialMetrics(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    )

    // === Actions: Purchases ===
    fun addPurchase(
        farmerName: String,
        phone: String,
        weightKg: Double,
        pricePerKg: Double,
        transportCost: Double,
        paidAmount: Double,
        oliveVariety: String,
        notes: String,
        grossWeightKg: Double = 0.0,
        tareWeightKg: Double = 0.0
    ) {
        safeLaunch {
            val invoiceNo = repository.getNextPurchaseInvoiceNumber()
            val total = (weightKg * pricePerKg) + transportCost
            val remaining = (total - paidAmount).coerceAtLeast(0.0)
            val purchase = OlivePurchase(
                invoiceNumber = invoiceNo,
                farmerName = farmerName.trim(),
                phoneNumber = phone.trim(),
                weightKg = weightKg,
                grossWeightKg = grossWeightKg,
                tareWeightKg = tareWeightKg,
                unit = "KG",
                pricePerUnit = pricePerKg,
                transportCost = transportCost,
                totalAmount = total,
                paidAmount = paidAmount,
                remainingAmount = remaining,
                oliveVariety = oliveVariety.trim().ifEmpty { "بلدي / نبالي" },
                notes = notes.trim(),
                dateMillis = System.currentTimeMillis()
            )
            repository.savePurchase(purchase)
            showInvoicePreview(InvoicePreviewData.PurchaseInvoice(purchase))
        }
    }

    fun deletePurchase(purchase: OlivePurchase) {
        safeLaunch { repository.deletePurchase(purchase) }
    }

    // === Actions: Pressing for Customers ===
    fun addPressing(
        customerName: String,
        phone: String,
        oliveWeightKg: Double,
        producedOilKg: Double,
        feeType: String,
        feeRate: Double,
        emptyTinsProvided: Int,
        emptyTinPrice: Double,
        tinsFilledCount: Double,
        pomaceWeightKg: Double,
        pomaceRetainedByPress: Boolean,
        paidAmountCash: Double,
        notes: String,
        grossWeightKg: Double = 0.0,
        tareWeightKg: Double = 0.0
    ) {
        safeLaunch {
            val receiptNo = repository.getNextPressingReceiptNumber()
            val extractionRate = if (oliveWeightKg > 0) (producedOilKg / oliveWeightKg) * 100 else 0.0

            var retainedOil = 0.0
            var cashFee = 0.0

            when (feeType) {
                "PER_KG" -> cashFee = oliveWeightKg * feeRate
                "PER_TON" -> cashFee = (oliveWeightKg / 1000.0) * feeRate
                "PER_TIN" -> cashFee = tinsFilledCount * feeRate
                "OIL_PERCENTAGE" -> {
                    retainedOil = producedOilKg * (feeRate / 100.0)
                    cashFee = 0.0
                }
                "OIL_FIXED_KG" -> {
                    retainedOil = feeRate
                    cashFee = 0.0
                }
            }

            val deliveredOil = (producedOilKg - retainedOil).coerceAtLeast(0.0)
            val totalCashDue = cashFee + (emptyTinsProvided * emptyTinPrice)
            val remaining = (totalCashDue - paidAmountCash).coerceAtLeast(0.0)

            val op = PressingOperation(
                receiptNumber = receiptNo,
                customerName = customerName.trim(),
                phoneNumber = phone.trim(),
                oliveWeightKg = oliveWeightKg,
                grossWeightKg = grossWeightKg,
                tareWeightKg = tareWeightKg,
                producedOilKg = producedOilKg,
                extractionRate = extractionRate,
                feeType = feeType,
                feeRate = feeRate,
                retainedOilKg = retainedOil,
                deliveredOilKg = deliveredOil,
                emptyTinsProvided = emptyTinsProvided,
                emptyTinPrice = emptyTinPrice,
                tinsFilledCount = tinsFilledCount,
                pomaceWeightKg = pomaceWeightKg,
                pomaceRetainedByPress = pomaceRetainedByPress,
                totalFeeCash = totalCashDue,
                paidAmountCash = paidAmountCash,
                remainingAmountCash = remaining,
                notes = notes.trim(),
                dateMillis = System.currentTimeMillis()
            )

            repository.savePressing(op)
            showInvoicePreview(InvoicePreviewData.PressingReceipt(op))
        }
    }

    fun deletePressing(op: PressingOperation) {
        safeLaunch { repository.deletePressing(op) }
    }

    // === Actions: Sales ===
    fun addSale(
        customerName: String,
        phone: String,
        itemType: String,
        quantity: Double,
        unitPrice: Double,
        discount: Double,
        paidAmount: Double,
        paymentType: String,
        notes: String
    ) {
        safeLaunch {
            val invoiceNo = repository.getNextSaleInvoiceNumber()
            val total = ((quantity * unitPrice) - discount).coerceAtLeast(0.0)
            val remaining = (total - paidAmount).coerceAtLeast(0.0)

            val sale = OilSale(
                invoiceNumber = invoiceNo,
                customerName = customerName.trim(),
                phoneNumber = phone.trim(),
                itemType = itemType,
                quantity = quantity,
                unitPrice = unitPrice,
                discount = discount,
                totalAmount = total,
                paidAmount = paidAmount,
                remainingAmount = remaining,
                paymentType = paymentType,
                notes = notes.trim(),
                dateMillis = System.currentTimeMillis()
            )

            repository.saveSale(sale)
            showInvoicePreview(InvoicePreviewData.SaleInvoice(sale))
        }
    }

    fun deleteSale(sale: OilSale) {
        safeLaunch { repository.deleteSale(sale) }
    }

    // === Actions: Internal Pressing (Own Olive to Oil) ===
    fun addInternalPressing(
        oliveUsedKg: Double,
        producedOilKg: Double,
        pomaceKg: Double,
        targetTankId: Long?,
        notes: String
    ) {
        safeLaunch {
            val batchNo = String.format(java.util.Locale.US, "INT-%04d", (System.currentTimeMillis() % 10000).toInt())
            val rate = if (oliveUsedKg > 0) (producedOilKg / oliveUsedKg) * 100 else 0.0

            val pressing = InternalPressing(
                batchNumber = batchNo,
                oliveUsedKg = oliveUsedKg,
                producedOilKg = producedOilKg,
                pomaceProducedKg = pomaceKg,
                extractionRate = rate,
                targetTankId = targetTankId,
                notes = notes.trim(),
                dateMillis = System.currentTimeMillis()
            )

            repository.saveInternalPressing(pressing)

            // Update tank if selected
            targetTankId?.let { tankId ->
                val currentTanks = tanks.value
                val tank = currentTanks.find { it.id == tankId }
                tank?.let {
                    val updated = it.copy(currentLevelKg = (it.currentLevelKg + producedOilKg).coerceAtMost(it.capacityKg))
                    repository.saveTank(updated)
                }
            }
        }
    }

    // === Actions: Tanks ===
    fun addOrUpdateTank(
        id: Long = 0,
        name: String,
        capacityKg: Double,
        currentLevelKg: Double,
        oilQuality: String,
        notes: String
    ) {
        safeLaunch {
            val tank = OilTank(
                id = id,
                tankName = name.trim(),
                capacityKg = capacityKg,
                currentLevelKg = currentLevelKg,
                oilQuality = oilQuality.trim(),
                notes = notes.trim()
            )
            repository.saveTank(tank)
        }
    }

    fun deleteTank(tank: OilTank) {
        safeLaunch { repository.deleteTank(tank) }
    }

    // === Actions: Cash Transactions & Expenses ===
    fun addCashTransaction(
        type: String, // EXPENSE, RECEIPT, PAYMENT
        category: String,
        amount: Double,
        partyName: String,
        notes: String
    ) {
        safeLaunch {
            val voucherNo = repository.getNextVoucherNumber()
            val txn = CashTransaction(
                voucherNumber = voucherNo,
                transactionType = type,
                category = category.trim(),
                amount = amount,
                partyName = partyName.trim(),
                relatedInvoice = "",
                notes = notes.trim(),
                dateMillis = System.currentTimeMillis()
            )
            repository.saveCashTransaction(txn)
            showInvoicePreview(InvoicePreviewData.CashVoucher(txn))
        }
    }

    fun deleteCashTransaction(txn: CashTransaction) {
        safeLaunch { repository.deleteCashTransaction(txn) }
    }

    // === Actions: Settings ===
    fun updateSettings(newSettings: PressSettings) {
        safeLaunch {
            repository.saveSettings(newSettings)
        }
    }

    fun resetAndLoadDemoData() {
        safeLaunch {
            repository.loadSeasonalDemoData()
        }
    }

    fun resetAllDataEmpty() {
        safeLaunch {
            repository.resetAllData()
            repository.saveSettings(PressSettings())
        }
    }

    fun settleCustomerDebt(customerName: String, amount: Double, notes: String) {
        safeLaunch {
            val voucherNo = repository.getNextVoucherNumber()
            val txn = CashTransaction(
                voucherNumber = voucherNo,
                transactionType = "RECEIPT",
                category = "تحصيل دفعة وسداد دين",
                amount = amount,
                partyName = customerName,
                notes = "سداد دفعة من الحساب المستحق - $notes",
                dateMillis = System.currentTimeMillis()
            )
            repository.saveCashTransaction(txn)
            repository.logAudit("تحصيل دين زبون", "تحصيل $amount من الزبون $customerName بسند $voucherNo")
            showInvoicePreview(InvoicePreviewData.CashVoucher(txn))
        }
    }

    fun settleSupplierDebt(farmerName: String, amount: Double, notes: String) {
        safeLaunch {
            val voucherNo = repository.getNextVoucherNumber()
            val txn = CashTransaction(
                voucherNumber = voucherNo,
                transactionType = "PAYMENT",
                category = "سداد مستحقات مزارع / مورد",
                amount = amount,
                partyName = farmerName,
                notes = "سداد دفعة للمزارع من ثمن الزيتون - $notes",
                dateMillis = System.currentTimeMillis()
            )
            repository.saveCashTransaction(txn)
            repository.logAudit("سداد دين مورد", "سداد $amount للمزارع $farmerName بسند $voucherNo")
            showInvoicePreview(InvoicePreviewData.CashVoucher(txn))
        }
    }

    fun generatePartyStatement(name: String, isCustomer: Boolean): String {
        val s = settings.value
        val sb = StringBuilder()
        sb.appendLine("📑 كشف حساب - ${s.pressName}")
        sb.appendLine("الطرف: $name | التاريخ: ${formatDateShort(System.currentTimeMillis())}")
        sb.appendLine("----------------------------------------")

        if (isCustomer) {
            val custPressings = pressings.value.filter { it.customerName == name }
            val custSales = sales.value.filter { it.customerName == name }
            val custPayments = cashTransactions.value.filter { it.partyName == name && it.transactionType == "RECEIPT" }

            var totalDebits = 0.0
            var totalCredits = 0.0

            if (custPressings.isNotEmpty()) {
                sb.appendLine("عمليات العصر:")
                custPressings.forEach {
                    sb.appendLine("• ${formatDateShort(it.dateMillis)} [${it.receiptNumber}] زيتون: ${formatNumber(it.oliveWeightKg)} كغ، زيت: ${formatNumber(it.producedOilKg)} كغ. الأجرة: ${formatCurrency(it.totalFeeCash)}")
                    totalDebits += it.totalFeeCash
                    totalCredits += it.paidAmountCash
                }
            }

            if (custSales.isNotEmpty()) {
                sb.appendLine("مشتريات الزيت والمنتجات:")
                custSales.forEach {
                    sb.appendLine("• ${formatDateShort(it.dateMillis)} [${it.invoiceNumber}] ${it.itemType} كمية ${it.quantity}: ${formatCurrency(it.totalAmount)}")
                    totalDebits += it.totalAmount
                    totalCredits += it.paidAmount
                }
            }

            if (custPayments.isNotEmpty()) {
                sb.appendLine("سندات القبض المسددة:")
                custPayments.forEach {
                    sb.appendLine("• ${formatDateShort(it.dateMillis)} [${it.voucherNumber}] سداد: ${formatCurrency(it.amount)}")
                }
            }

            val balanceDue = (totalDebits - totalCredits).coerceAtLeast(0.0)
            sb.appendLine("----------------------------------------")
            sb.appendLine("إجمالي المطلوب: ${formatCurrency(totalDebits)}")
            sb.appendLine("إجمالي المسدد: ${formatCurrency(totalCredits)}")
            sb.appendLine("الرصيد المتبقي المستحق بذمته: ${formatCurrency(balanceDue)}")
        } else {
            val suppPurchases = purchases.value.filter { it.farmerName == name }
            var totalOwed = 0.0
            var totalPaid = 0.0

            sb.appendLine("توريدات الزيتون:")
            suppPurchases.forEach {
                sb.appendLine("• ${formatDateShort(it.dateMillis)} [${it.invoiceNumber}] وزن: ${formatNumber(it.weightKg)} كغ صنف ${it.oliveVariety}. القيمة: ${formatCurrency(it.totalAmount)}")
                totalOwed += it.totalAmount
                totalPaid += it.paidAmount
            }
            val balanceDue = (totalOwed - totalPaid).coerceAtLeast(0.0)
            sb.appendLine("----------------------------------------")
            sb.appendLine("إجمالي قيمة الزيتون المورد: ${formatCurrency(totalOwed)}")
            sb.appendLine("إجمالي المسدد له: ${formatCurrency(totalPaid)}")
            sb.appendLine("الرصيد المتبقي له بذمة المعصرة: ${formatCurrency(balanceDue)}")
        }

        sb.appendLine("----------------------------------------")
        sb.appendLine(s.invoiceFooterText)
        return sb.toString()
    }

    // === Helper Formatters ===
    fun formatCurrency(amount: Double, symbol: String = settings.value.currencySymbol): String {
        return try {
            val safeAmount = if (amount.isNaN() || amount.isInfinite()) 0.0 else amount
            val df = DecimalFormat("#,##0")
            "${df.format(safeAmount)} $symbol"
        } catch (e: Exception) {
            "0 $symbol"
        }
    }

    fun formatNumber(number: Double, decimals: Int = 1): String {
        return try {
            val safeNumber = if (number.isNaN() || number.isInfinite()) 0.0 else number
            if (safeNumber % 1.0 == 0.0) {
                safeNumber.toLong().toString()
            } else {
                val pattern = if (decimals == 2) "#,##0.00" else "#,##0.0"
                DecimalFormat(pattern).format(safeNumber)
            }
        } catch (e: Exception) {
            "0"
        }
    }

    fun formatDate(millis: Long): String {
        return try {
            val sdf = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))
            sdf.format(Date(millis))
        } catch (e: Exception) {
            ""
        }
    }

    fun formatDateShort(millis: Long): String {
        return try {
            val sdf = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))
            sdf.format(Date(millis))
        } catch (e: Exception) {
            ""
        }
    }
}
