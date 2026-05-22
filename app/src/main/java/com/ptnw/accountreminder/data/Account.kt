package com.ptnw.accountreminder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val expiredDate: Long,  // timestamp millis
    val reminderDays: Int = 14
)
