package com.ptnw.accountreminder.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY expiredDate ASC")
    fun getAll(): LiveData<List<Account>>

    @Query("SELECT * FROM accounts ORDER BY expiredDate ASC")
    suspend fun getAllSync(): List<Account>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account)

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)
}
