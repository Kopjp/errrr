package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.models.CashTransaction
import com.example.data.models.InternalPressing
import com.example.data.models.OilSale
import com.example.data.models.OilTank
import com.example.data.models.OlivePurchase
import com.example.data.models.PressSettings
import com.example.data.models.PressingOperation
import kotlinx.coroutines.flow.Flow

@Dao
interface PressDao {

    // === مشتريات الزيتون ===
    @Query("SELECT * FROM olive_purchases ORDER BY dateMillis DESC")
    fun getAllPurchases(): Flow<List<OlivePurchase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: OlivePurchase): Long

    @Delete
    suspend fun deletePurchase(purchase: OlivePurchase)

    @Query("SELECT COUNT(*) FROM olive_purchases")
    suspend fun getPurchasesCount(): Int

    // === عمليات العصر للزبائن ===
    @Query("SELECT * FROM pressing_operations ORDER BY dateMillis DESC")
    fun getAllPressings(): Flow<List<PressingOperation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPressing(operation: PressingOperation): Long

    @Delete
    suspend fun deletePressing(operation: PressingOperation)

    @Query("SELECT COUNT(*) FROM pressing_operations")
    suspend fun getPressingsCount(): Int

    // === مبيعات الزيت والمنتجات ===
    @Query("SELECT * FROM oil_sales ORDER BY dateMillis DESC")
    fun getAllSales(): Flow<List<OilSale>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: OilSale): Long

    @Delete
    suspend fun deleteSale(sale: OilSale)

    @Query("SELECT COUNT(*) FROM oil_sales")
    suspend fun getSalesCount(): Int

    // === العصر الداخلي لحساب المعصرة ===
    @Query("SELECT * FROM internal_pressings ORDER BY dateMillis DESC")
    fun getAllInternalPressings(): Flow<List<InternalPressing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInternalPressing(pressing: InternalPressing): Long

    @Delete
    suspend fun deleteInternalPressing(pressing: InternalPressing)

    // === خزانات الزيت ===
    @Query("SELECT * FROM oil_tanks ORDER BY id ASC")
    fun getAllTanks(): Flow<List<OilTank>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTank(tank: OilTank): Long

    @Update
    suspend fun updateTank(tank: OilTank)

    @Delete
    suspend fun deleteTank(tank: OilTank)

    // === الصندوق والمصاريف والحركة النقدية ===
    @Query("SELECT * FROM cash_transactions ORDER BY dateMillis DESC")
    fun getAllCashTransactions(): Flow<List<CashTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashTransaction(transaction: CashTransaction): Long

    @Delete
    suspend fun deleteCashTransaction(transaction: CashTransaction)

    @Query("SELECT COUNT(*) FROM cash_transactions")
    suspend fun getTransactionsCount(): Int

    // === الإعدادات ===
    @Query("SELECT * FROM press_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<PressSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: PressSettings)

    // === سجل التدقيق والرقابة ===
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllAuditLogs(): Flow<List<com.example.data.models.AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: com.example.data.models.AuditLog): Long

    @Query("DELETE FROM audit_logs")
    suspend fun clearAuditLogs()

    // === تفريغ البيانات للبدء من جديد ===
    @Query("DELETE FROM olive_purchases")
    suspend fun clearPurchases()

    @Query("DELETE FROM pressing_operations")
    suspend fun clearPressings()

    @Query("DELETE FROM oil_sales")
    suspend fun clearSales()

    @Query("DELETE FROM internal_pressings")
    suspend fun clearInternalPressings()

    @Query("DELETE FROM oil_tanks")
    suspend fun clearTanks()

    @Query("DELETE FROM cash_transactions")
    suspend fun clearCashTransactions()
}
