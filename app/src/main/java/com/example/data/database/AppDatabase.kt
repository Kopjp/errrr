package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.PressDao
import com.example.data.models.CashTransaction
import com.example.data.models.InternalPressing
import com.example.data.models.OilSale
import com.example.data.models.OilTank
import com.example.data.models.OlivePurchase
import com.example.data.models.PressSettings
import com.example.data.models.PressingOperation

@Database(
    entities = [
        OlivePurchase::class,
        PressingOperation::class,
        OilSale::class,
        InternalPressing::class,
        OilTank::class,
        CashTransaction::class,
        PressSettings::class,
        com.example.data.models.AuditLog::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pressDao(): PressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "olive_press_accounting.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
