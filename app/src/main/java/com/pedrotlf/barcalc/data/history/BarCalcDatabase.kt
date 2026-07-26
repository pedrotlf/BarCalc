package com.pedrotlf.barcalc.data.history

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TabHistoryEntity::class], version = 1, exportSchema = true)
abstract class BarCalcDatabase : RoomDatabase() {

    abstract fun tabHistoryDao(): TabHistoryDao

    companion object {
        @Volatile
        private var instance: BarCalcDatabase? = null

        fun get(context: Context): BarCalcDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BarCalcDatabase::class.java,
                    "barcalc.db",
                ).build().also { instance = it }
            }
    }
}
