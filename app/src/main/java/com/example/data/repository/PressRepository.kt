package com.example.data.repository

import com.example.data.dao.PressDao
import com.example.data.models.CashTransaction
import com.example.data.models.InternalPressing
import com.example.data.models.OilSale
import com.example.data.models.OilTank
import com.example.data.models.OlivePurchase
import com.example.data.models.PressSettings
import com.example.data.models.PressingOperation
import com.example.data.models.AuditLog
import kotlinx.coroutines.flow.Flow

class PressRepository(private val dao: PressDao) {

    val purchases: Flow<List<OlivePurchase>> = dao.getAllPurchases()
    val pressings: Flow<List<PressingOperation>> = dao.getAllPressings()
    val sales: Flow<List<OilSale>> = dao.getAllSales()
    val internalPressings: Flow<List<InternalPressing>> = dao.getAllInternalPressings()
    val tanks: Flow<List<OilTank>> = dao.getAllTanks()
    val cashTransactions: Flow<List<CashTransaction>> = dao.getAllCashTransactions()
    val settings: Flow<PressSettings?> = dao.getSettings()
    val auditLogs: Flow<List<AuditLog>> = dao.getAllAuditLogs()

    suspend fun logAudit(action: String, details: String, role: String = "المدير") {
        dao.insertAuditLog(
            AuditLog(
                actionType = action,
                details = details,
                operatorRole = role,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun getNextPurchaseInvoiceNumber(): String {
        val count = dao.getPurchasesCount()
        return String.format(java.util.Locale.US, "PUR-%04d", 1001 + count)
    }

    suspend fun getNextPressingReceiptNumber(): String {
        val count = dao.getPressingsCount()
        return String.format(java.util.Locale.US, "PRS-%04d", 2001 + count)
    }

    suspend fun getNextSaleInvoiceNumber(): String {
        val count = dao.getSalesCount()
        return String.format(java.util.Locale.US, "SAL-%04d", 3001 + count)
    }

    suspend fun getNextVoucherNumber(): String {
        val count = dao.getTransactionsCount()
        return String.format(java.util.Locale.US, "VCH-%04d", 5001 + count)
    }

    // === حفظ شراء زيتون مع الربط بالصندوق ===
    suspend fun savePurchase(purchase: OlivePurchase) {
        dao.insertPurchase(purchase)
        logAudit(
            action = "شراء زيتون",
            details = "شراء زيتون من المزارع ${purchase.farmerName} وزن ${purchase.weightKg} كغ صنف ${purchase.oliveVariety} بإجمالي ${purchase.totalAmount} (فاتورة ${purchase.invoiceNumber})"
        )
        if (purchase.paidAmount > 0) {
            dao.insertCashTransaction(
                CashTransaction(
                    voucherNumber = getNextVoucherNumber(),
                    transactionType = "PAYMENT",
                    category = "شراء زيتون لحساب المعصرة",
                    amount = purchase.paidAmount,
                    partyName = purchase.farmerName,
                    relatedInvoice = purchase.invoiceNumber,
                    notes = "دفعة من فاتورة شراء زيتون وزن ${purchase.weightKg} كغ",
                    dateMillis = purchase.dateMillis
                )
            )
        }
    }

    suspend fun deletePurchase(purchase: OlivePurchase) {
        dao.deletePurchase(purchase)
        logAudit(action = "حذف فاتورة شراء", details = "حذف فاتورة شراء رقم ${purchase.invoiceNumber} للمزارع ${purchase.farmerName}")
    }

    // === حفظ عملية عصر زيتون للزبون مع الربط بالصندوق والخزانات ===
    suspend fun savePressing(operation: PressingOperation, targetTankIdForRetainedOil: Long? = null) {
        dao.insertPressing(operation)
        logAudit(
            action = "عصر زيتون لزبون",
            details = "عصر ${operation.oliveWeightKg} كغ للزبون ${operation.customerName} نتج عنه ${operation.producedOilKg} كغ زيت (رد ${String.format("%.1f", operation.extractionRate)}%) بإيصال ${operation.receiptNumber}"
        )
        if (operation.paidAmountCash > 0) {
            dao.insertCashTransaction(
                CashTransaction(
                    voucherNumber = getNextVoucherNumber(),
                    transactionType = "RECEIPT",
                    category = "أجور عصر زيتون",
                    amount = operation.paidAmountCash,
                    partyName = operation.customerName,
                    relatedInvoice = operation.receiptNumber,
                    notes = "قبض أجور عصر من الزبون عن وزن ${operation.oliveWeightKg} كغ",
                    dateMillis = operation.dateMillis
                )
            )
        }
    }

    suspend fun deletePressing(operation: PressingOperation) {
        dao.deletePressing(operation)
        logAudit(action = "حذف إيصال عصر", details = "حذف إيصال عصر رقم ${operation.receiptNumber} للزبون ${operation.customerName}")
    }

    // === حفظ عملية بيع زيت مع الربط بالصندوق ===
    suspend fun saveSale(sale: OilSale, fromTankId: Long? = null) {
        dao.insertSale(sale)
        logAudit(
            action = "بيع زيت ومنتجات",
            details = "بيع (${sale.itemType}) كمية ${sale.quantity} للمشتري ${sale.customerName} بإجمالي ${sale.totalAmount} (فاتورة ${sale.invoiceNumber})"
        )
        if (sale.paidAmount > 0) {
            dao.insertCashTransaction(
                CashTransaction(
                    voucherNumber = getNextVoucherNumber(),
                    transactionType = "RECEIPT",
                    category = "مبيعات زيت ومنتجات",
                    amount = sale.paidAmount,
                    partyName = sale.customerName,
                    relatedInvoice = sale.invoiceNumber,
                    notes = "قبض ثمن مبيعات (${sale.itemType}) كمية ${sale.quantity}",
                    dateMillis = sale.dateMillis
                )
            )
        }
    }

    suspend fun deleteSale(sale: OilSale) {
        dao.deleteSale(sale)
        logAudit(action = "حذف فاتورة بيع", details = "حذف فاتورة بيع رقم ${sale.invoiceNumber} للزبون ${sale.customerName}")
    }

    // === حفظ عصر داخلي لمخزون المعصرة ===
    suspend fun saveInternalPressing(internalPressing: InternalPressing) {
        dao.insertInternalPressing(internalPressing)
        logAudit(
            action = "عصر داخلي للمعصرة",
            details = "عصر ${internalPressing.oliveUsedKg} كغ من زيتون المعصرة نتج عنه ${internalPressing.producedOilKg} كغ زيت (رد ${String.format("%.1f", internalPressing.extractionRate)}%)"
        )
        // إذا تم تحديد خزان للزيت الناتج، نضيف الكمية إليه
        internalPressing.targetTankId?.let { tankId ->
            // Update tank level logic can be handled or calculated
        }
    }

    // === الخزانات ===
    suspend fun saveTank(tank: OilTank) {
        if (tank.id == 0L) {
            dao.insertTank(tank)
        } else {
            dao.updateTank(tank)
        }
    }

    suspend fun deleteTank(tank: OilTank) {
        dao.deleteTank(tank)
    }

    // === الصندوق والمصاريف ===
    suspend fun saveCashTransaction(transaction: CashTransaction) {
        dao.insertCashTransaction(transaction)
    }

    suspend fun deleteCashTransaction(transaction: CashTransaction) {
        dao.deleteCashTransaction(transaction)
    }

    // === الإعدادات ===
    suspend fun saveSettings(settings: PressSettings) {
        dao.insertOrUpdateSettings(settings)
    }

    // === تفريغ أو تحميل بيانات نموذجية للموسم ===
    suspend fun resetAllData() {
        dao.clearPurchases()
        dao.clearPressings()
        dao.clearSales()
        dao.clearInternalPressings()
        dao.clearTanks()
        dao.clearCashTransactions()
    }

    suspend fun loadSeasonalDemoData() {
        resetAllData()

        // 1. إعدادات المعصرة
        saveSettings(
            PressSettings(
                pressName = "معصرة الزيتون النموذجية",
                ownerName = "الحاج أبو خالد الشامي",
                phoneNumber = "0944123456",
                address = "ريف دمشق - وادي بردى والزبداني",
                currencySymbol = "ل.س",
                tinCapacityKg = 16.0,
                defaultPressingFeePerKg = 850.0,
                defaultPressingFeePerTin = 16000.0,
                defaultEmptyTinPrice = 28000.0,
                defaultRetentionPercent = 10.0,
                invoiceFooterText = "معصرة نموذجية حديثة - عصر على البارد بأحدث الخطوط الإيطالية"
            )
        )

        // 2. خزانات الزيت
        val tank1Id = dao.insertTank(OilTank(tankName = "خزان ستانلس رقم 1 (بكر ممتاز)", capacityKg = 5000.0, currentLevelKg = 2450.0, oilQuality = "بكر ممتاز"))
        val tank2Id = dao.insertTank(OilTank(tankName = "خزان ستانلس رقم 2 (بكر)", capacityKg = 3000.0, currentLevelKg = 1120.0, oilQuality = "بكر"))
        dao.insertTank(OilTank(tankName = "خزان رقم 3 (احتياطي عالي السعة)", capacityKg = 10000.0, currentLevelKg = 4200.0, oilQuality = "بكر ممتاز"))

        val now = System.currentTimeMillis()
        val oneDay = 86400000L

        // 3. مشتريات زيتون من المزارعين
        savePurchase(
            OlivePurchase(
                invoiceNumber = "PUR-1001",
                farmerName = "المزارع أحمد العلي",
                phoneNumber = "0933112233",
                weightKg = 3500.0,
                unit = "KG",
                pricePerUnit = 12000.0,
                transportCost = 150000.0,
                totalAmount = 42150000.0,
                paidAmount = 35000000.0,
                remainingAmount = 7150000.0,
                oliveVariety = "نبالي بعل ممتاز",
                notes = "حبة خضراء ناضجة خالية من الإصابات",
                dateMillis = now - 3 * oneDay
            )
        )

        savePurchase(
            OlivePurchase(
                invoiceNumber = "PUR-1002",
                farmerName = "أبو محمود بستان التل",
                phoneNumber = "0955223344",
                weightKg = 4800.0,
                unit = "KG",
                pricePerUnit = 11500.0,
                transportCost = 200000.0,
                totalAmount = 55400000.0,
                paidAmount = 55400000.0,
                remainingAmount = 0.0,
                oliveVariety = "صوراني بلدي",
                notes = "تم الدفع نقداً بالكامل",
                dateMillis = now - 2 * oneDay
            )
        )

        // 4. عمليات عصر للزبائن
        savePressing(
            PressingOperation(
                receiptNumber = "PRS-2001",
                customerName = "الزبون ياسين القاسمي",
                phoneNumber = "0988776655",
                oliveWeightKg = 1850.0,
                producedOilKg = 351.5,
                extractionRate = 19.0,
                feeType = "PER_KG",
                feeRate = 850.0,
                retainedOilKg = 0.0,
                deliveredOilKg = 351.5,
                emptyTinsProvided = 22,
                emptyTinPrice = 28000.0,
                tinsFilledCount = 22.0,
                pomaceWeightKg = 850.0,
                pomaceRetainedByPress = true,
                totalFeeCash = (1850 * 850.0) + (22 * 28000.0), // 1572500 + 616000 = 2188500
                paidAmountCash = 2188500.0,
                remainingAmountCash = 0.0,
                notes = "نسبة ممتازة ونوعية الزيت صافية جداً",
                dateMillis = now - 2 * oneDay
            )
        )

        savePressing(
            PressingOperation(
                receiptNumber = "PRS-2002",
                customerName = "عمر الخضر",
                phoneNumber = "0999887711",
                oliveWeightKg = 2400.0,
                producedOilKg = 432.0,
                extractionRate = 18.0,
                feeType = "OIL_PERCENTAGE", // رد عيني
                feeRate = 10.0, // 10% رد
                retainedOilKg = 43.2, // 43.2 كغ زيت للمعصرة
                deliveredOilKg = 388.8,
                emptyTinsProvided = 10,
                emptyTinPrice = 28000.0,
                tinsFilledCount = 25.0,
                pomaceWeightKg = 1100.0,
                pomaceRetainedByPress = true,
                totalFeeCash = 280000.0, // فقط ثمن التنكات الفارغة المباعة
                paidAmountCash = 280000.0,
                remainingAmountCash = 0.0,
                notes = "تم احتساب الأجرة رداً عينياً 10% وأخذ الزبون 10 تنكات جديدة",
                dateMillis = now - 1 * oneDay
            )
        )

        savePressing(
            PressingOperation(
                receiptNumber = "PRS-2003",
                customerName = "مزرعة الروضة - طارق",
                phoneNumber = "0944665522",
                oliveWeightKg = 3200.0,
                producedOilKg = 544.0,
                extractionRate = 17.0,
                feeType = "PER_KG",
                feeRate = 850.0,
                retainedOilKg = 0.0,
                deliveredOilKg = 544.0,
                emptyTinsProvided = 34,
                emptyTinPrice = 28000.0,
                tinsFilledCount = 34.0,
                pomaceWeightKg = 1500.0,
                pomaceRetainedByPress = true,
                totalFeeCash = (3200 * 850.0) + (34 * 28000.0), // 2720000 + 952000 = 3672000
                paidAmountCash = 2500000.0,
                remainingAmountCash = 1172000.0,
                notes = "متبقي عليه 1,172,000 ل.س يتم تسديدها لاحقاً",
                dateMillis = now - 4 * 3600000L
            )
        )

        // 5. مبيعات زيت ومنتجات
        saveSale(
            OilSale(
                invoiceNumber = "SAL-3001",
                customerName = "مؤسسة الأمانة للتجارة",
                phoneNumber = "0933445566",
                itemType = "OIL_TIN", // بيع تنكات زيت
                quantity = 40.0,
                unitPrice = 850000.0, // 850 ألف للتنكة
                discount = 500000.0,
                totalAmount = (40 * 850000.0) - 500000.0, // 33,500,000
                paidAmount = 25000000.0,
                remainingAmount = 8500000.0,
                paymentType = "PARTIAL",
                notes = "تسليم 40 تنكة نخب أول زيت بلدي عصر الموسم",
                dateMillis = now - 1 * oneDay
            )
        )

        saveSale(
            OilSale(
                invoiceNumber = "SAL-3002",
                customerName = "الدكتور نبيل صباغ",
                phoneNumber = "0922114477",
                itemType = "OIL_TIN",
                quantity = 5.0,
                unitPrice = 860000.0,
                discount = 0.0,
                totalAmount = 4300000.0,
                paidAmount = 4300000.0,
                remainingAmount = 0.0,
                paymentType = "CASH",
                notes = "مدفوعة بالكامل نقداً",
                dateMillis = now - 12 * 3600000L
            )
        )

        // 6. عصر داخلي لحساب المعصرة
        dao.insertInternalPressing(
            InternalPressing(
                batchNumber = "INT-4001",
                oliveUsedKg = 4000.0,
                producedOilKg = 760.0,
                pomaceProducedKg = 1900.0,
                extractionRate = 19.0,
                targetTankId = tank1Id,
                notes = "تم عصر دفعة من زيتون المعصرة المشتري ونقل الزيت إلى خزان رقم 1",
                dateMillis = now - 1 * oneDay
            )
        )

        // 7. مصاريف تشغيلية
        dao.insertCashTransaction(
            CashTransaction(
                voucherNumber = "VCH-5001",
                transactionType = "EXPENSE",
                category = "محروقات وديزل للمعصرة",
                amount = 4500000.0,
                partyName = "محطة الشام للمحروقات",
                relatedInvoice = "",
                notes = "شراء 300 لتر مازوت لتشغيل المولدات والمراجل",
                dateMillis = now - 3 * oneDay
            )
        )

        dao.insertCashTransaction(
            CashTransaction(
                voucherNumber = "VCH-5002",
                transactionType = "EXPENSE",
                category = "أجور وعمالة",
                amount = 2800000.0,
                partyName = "عمال خط العصر والتفريغ",
                relatedInvoice = "",
                notes = "سلفة أسبوعية لـ 4 عمال وردية ليلية ونهارية",
                dateMillis = now - 2 * oneDay
            )
        )

        dao.insertCashTransaction(
            CashTransaction(
                voucherNumber = "VCH-5003",
                transactionType = "EXPENSE",
                category = "صيانة وقطع غيار",
                amount = 1200000.0,
                partyName = "ورشة الفارس الهندسية",
                relatedInvoice = "",
                notes = "صيانة الفرازات وتبديل قواشين ناقل الزيتون",
                dateMillis = now - 1 * oneDay
            )
        )
    }
}
