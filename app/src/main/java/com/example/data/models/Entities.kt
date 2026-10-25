package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * عمليـات شـراء الزيتون لحساب المعصرة
 */
@Entity(tableName = "olive_purchases")
data class OlivePurchase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val farmerName: String,
    val phoneNumber: String = "",
    val weightKg: Double,
    val grossWeightKg: Double = 0.0, // الوزن القائم (مع السيارة/الصناديق)
    val tareWeightKg: Double = 0.0,  // الوزن الفارغ
    val unit: String = "KG", // "KG" or "TON"
    val pricePerUnit: Double,
    val transportCost: Double = 0.0,
    val totalAmount: Double,
    val paidAmount: Double,
    val remainingAmount: Double,
    val oliveVariety: String = "بلدي / نبالي", // صنف الزيتون
    val notes: String = "",
    val dateMillis: Long = System.currentTimeMillis()
)

/**
 * عمليـات عصر الزيتون للزبائن مقابل أجرة (نقدية أو عينية)
 */
@Entity(tableName = "pressing_operations")
data class PressingOperation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptNumber: String,
    val customerName: String,
    val phoneNumber: String = "",
    val oliveWeightKg: Double,
    val grossWeightKg: Double = 0.0, // الوزن القائم
    val tareWeightKg: Double = 0.0,  // الوزن الفارغ
    val producedOilKg: Double,
    val extractionRate: Double, // نسبة الاستخلاص = (الزيت / الزيتون) * 100
    val feeType: String = "PER_KG", // PER_KG, PER_TON, PER_TIN, OIL_PERCENTAGE, OIL_FIXED_KG
    val feeRate: Double, // قيمة الأجرة (مثلاً 1000 ليرة للكيلو، أو 10% زيت)
    val retainedOilKg: Double = 0.0, // كمية الزيت التي تحتفظ بها المعصرة كأجرة عينية (الرد)
    val deliveredOilKg: Double, // كمية الزيت الصافية المسلمة للزبون
    val emptyTinsProvided: Int = 0, // عدد التنكات الفارغة المباعة للزبون من المعصرة
    val emptyTinPrice: Double = 0.0,
    val tinsFilledCount: Double = 0.0, // عدد التنكات المعبأة للزبون
    val pomaceWeightKg: Double = 0.0, // تفل الزيتون الناتج
    val pomaceRetainedByPress: Boolean = true, // هل احتفظت المعصرة بالتفل؟
    val totalFeeCash: Double, // إجمالي المبلغ النقدي المطلوب (أجرة نقدية + ثمن تنكات فارغة)
    val paidAmountCash: Double,
    val remainingAmountCash: Double,
    val notes: String = "",
    val dateMillis: Long = System.currentTimeMillis()
)

/**
 * عمليات بيع الزيت والمنتجات من مخزون المعصرة
 */
@Entity(tableName = "oil_sales")
data class OilSale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerName: String,
    val phoneNumber: String = "",
    val itemType: String, // "OIL_TIN" (تنكة زيت), "OIL_KG" (زيت بالكيلو), "OIL_LITER" (لتر زيت), "EMPTY_TIN" (تنكة فارغة), "POMACE" (تفل زيتون)
    val quantity: Double,
    val unitPrice: Double,
    val discount: Double = 0.0,
    val totalAmount: Double,
    val paidAmount: Double,
    val remainingAmount: Double,
    val paymentType: String = "CASH", // CASH, CREDIT, PARTIAL
    val notes: String = "",
    val dateMillis: Long = System.currentTimeMillis()
)

/**
 * عمليات عصر مخزون الزيتون الخاص بالمعصرة داخلياً
 */
@Entity(tableName = "internal_pressings")
data class InternalPressing(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchNumber: String,
    val oliveUsedKg: Double,
    val producedOilKg: Double,
    val pomaceProducedKg: Double = 0.0,
    val extractionRate: Double,
    val targetTankId: Long? = null,
    val notes: String = "",
    val dateMillis: Long = System.currentTimeMillis()
)

/**
 * خزانات الزيت المعدنية / الستانلس ستيل في المعصرة
 */
@Entity(tableName = "oil_tanks")
data class OilTank(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tankName: String,
    val capacityKg: Double,
    val currentLevelKg: Double,
    val oilQuality: String = "بكر ممتاز (Extra Virgin)",
    val notes: String = ""
)

/**
 * حركة الصندوق، المصاريف التشغيلية، سندات القبض والصرف
 */
@Entity(tableName = "cash_transactions")
data class CashTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val voucherNumber: String,
    val transactionType: String, // "EXPENSE" (مصروف), "RECEIPT" (سند قبض), "PAYMENT" (سند صرف), "CAPITAL" (رأس مال)
    val category: String, // محروقات وديزل، كهرباء، أجور عمال، صيانة، شراء مستلزمات، أخرى
    val amount: Double,
    val partyName: String = "",
    val relatedInvoice: String = "",
    val notes: String = "",
    val dateMillis: Long = System.currentTimeMillis()
)

/**
 * إعدادات المعصرة وثوابت التسعير والعملة
 */
@Entity(tableName = "press_settings")
data class PressSettings(
    @PrimaryKey val id: Int = 1,
    val pressName: String = "معصرة البركة لزيت الزيتون",
    val ownerName: String = "أبو أحمد",
    val phoneNumber: String = "0912345678",
    val address: String = "ريف دمشق - سهل الزبداني",
    val currencySymbol: String = "ل.س", // ليرة سورية، ليرة تركية، $، د.أ
    val tinCapacityKg: Double = 16.0, // سعة التنكة القياسية بالكيلوغرام
    val defaultPressingFeePerKg: Double = 800.0,
    val defaultPressingFeePerTin: Double = 15000.0,
    val defaultEmptyTinPrice: Double = 25000.0,
    val defaultRetentionPercent: Double = 10.0, // نسبة الرد العيني 10%
    val invoiceFooterText: String = "أهلاً وسهلاً بكم، زيت زيتون معصور على البارد بأعلى معايير الجودة"
)

/**
 * سجل التدقيق والرقابة (Audit Log)
 */
@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionType: String, // "إضافة شراء", "عصر زبون", "بيع زيت", "مصروف", "حذف"...
    val details: String,
    val operatorRole: String = "المدير العام",
    val timestamp: Long = System.currentTimeMillis()
)

