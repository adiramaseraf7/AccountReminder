package com.ptnw.accountreminder.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Account::class], version = 1, exportSchema = false)
abstract class AccountDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile private var INSTANCE: AccountDatabase? = null

        fun getDatabase(context: Context): AccountDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AccountDatabase::class.java, "account_db")
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
