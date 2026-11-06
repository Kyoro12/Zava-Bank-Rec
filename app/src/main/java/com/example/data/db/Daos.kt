package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_accounts ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserAccount>>

    @Query("SELECT * FROM user_accounts WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserAccount?

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserAccount?

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: Long): Flow<UserAccount?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserAccount): Long

    @Update
    suspend fun updateUser(user: UserAccount)

    @Delete
    suspend fun deleteUser(user: UserAccount)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC, id DESC")
    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId AND type = :type ORDER BY date DESC")
    fun getTransactionsByType(accountId: Long, type: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE accountId = :accountId")
    suspend fun deleteAllByAccount(accountId: Long)

    @Query("SELECT * FROM transactions WHERE accountId = :accountId")
    suspend fun getAllListByAccount(accountId: Long): List<TransactionEntity>
}

@Dao
interface SavingsDao {
    @Query("SELECT * FROM savings_pockets WHERE accountId = :accountId ORDER BY createdAt DESC")
    fun getPocketsByAccount(accountId: Long): Flow<List<SavingsPocket>>

    @Query("SELECT * FROM savings_pockets WHERE id = :id LIMIT 1")
    suspend fun getPocketById(id: Long): SavingsPocket?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPocket(pocket: SavingsPocket): Long

    @Update
    suspend fun updatePocket(pocket: SavingsPocket)

    @Delete
    suspend fun deletePocket(pocket: SavingsPocket)

    @Query("SELECT * FROM savings_logs WHERE pocketId = :pocketId ORDER BY date DESC")
    fun getLogsByPocket(pocketId: Long): Flow<List<SavingsLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SavingsLog): Long

    @Query("DELETE FROM savings_logs WHERE pocketId = :pocketId")
    suspend fun deleteLogsByPocket(pocketId: Long)

    @Query("DELETE FROM savings_pockets WHERE accountId = :accountId")
    suspend fun deleteAllPocketsByAccount(accountId: Long)

    @Query("DELETE FROM savings_logs WHERE accountId = :accountId")
    suspend fun deleteAllLogsByAccount(accountId: Long)

    @Query("SELECT * FROM savings_pockets WHERE accountId = :accountId")
    suspend fun getAllPocketsListByAccount(accountId: Long): List<SavingsPocket>

    @Query("SELECT * FROM savings_logs WHERE accountId = :accountId")
    suspend fun getAllLogsListByAccount(accountId: Long): List<SavingsLog>
}

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist_items WHERE accountId = :accountId ORDER BY priority ASC, createdAt DESC")
    fun getWishlistsByAccount(accountId: Long): Flow<List<WishlistItem>>

    @Query("SELECT * FROM wishlist_items WHERE id = :id LIMIT 1")
    suspend fun getWishlistById(id: Long): WishlistItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlist(item: WishlistItem): Long

    @Update
    suspend fun updateWishlist(item: WishlistItem)

    @Delete
    suspend fun deleteWishlist(item: WishlistItem)

    @Query("DELETE FROM wishlist_items WHERE accountId = :accountId")
    suspend fun deleteAllByAccount(accountId: Long)

    @Query("SELECT * FROM wishlist_items WHERE accountId = :accountId")
    suspend fun getAllListByAccount(accountId: Long): List<WishlistItem>
}

@Dao
interface TargetDao {
    @Query("SELECT * FROM financial_targets WHERE accountId = :accountId ORDER BY deadlineDate ASC")
    fun getTargetsByAccount(accountId: Long): Flow<List<FinancialTarget>>

    @Query("SELECT * FROM financial_targets WHERE id = :id LIMIT 1")
    suspend fun getTargetById(id: Long): FinancialTarget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarget(target: FinancialTarget): Long

    @Update
    suspend fun updateTarget(target: FinancialTarget)

    @Delete
    suspend fun deleteTarget(target: FinancialTarget)

    @Query("DELETE FROM financial_targets WHERE accountId = :accountId")
    suspend fun deleteAllByAccount(accountId: Long)

    @Query("SELECT * FROM financial_targets WHERE accountId = :accountId")
    suspend fun getAllListByAccount(accountId: Long): List<FinancialTarget>
}
