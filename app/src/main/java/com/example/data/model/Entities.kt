package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_accounts",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val fullName: String,
    val passwordHash: String,
    val salt: String,
    val profilePhotoUri: String? = null,
    val wallpaperUri: String? = null,
    val activeTheme: String = "DARK_PREMIUM", // DARK_PREMIUM, LIGHT_MINIMALIST, PURPLE_LUXURY, NAVY_GOLD, CUSTOM
    val customAccentHex: String = "#F59E0B",
    val currencyFormat: String = "RP_NOSPACE", // RP_NOSPACE (Rp1.500.000), RP_SPACE (Rp 1.500.000), IDR (IDR 1.500.000)
    val isPrivacyEnabled: Boolean = false,
    val isLargeFont: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["accountId"]), Index(value = ["date"])]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val type: String, // INCOME or EXPENSE
    val amount: Long,
    val category: String,
    val date: Long,
    val sourceOrMethod: String,
    val note: String = "",
    val attachmentUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "savings_pockets",
    indices = [Index(value = ["accountId"])]
)
data class SavingsPocket(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val name: String,
    val category: String, // UMUM or NIKAH
    val targetAmount: Long,
    val currentBalance: Long = 0L,
    val startDate: Long = System.currentTimeMillis(),
    val targetDate: Long = System.currentTimeMillis() + (180L * 24 * 60 * 60 * 1000), // Default 6 months
    val note: String = "",
    val iconName: String = "savings",
    val colorHex: String = "#F59E0B",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "savings_logs",
    indices = [Index(value = ["pocketId"]), Index(value = ["accountId"])]
)
data class SavingsLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pocketId: Long,
    val accountId: Long,
    val type: String, // DEPOSIT or WITHDRAW
    val amount: Long,
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "wishlist_items",
    indices = [Index(value = ["accountId"])]
)
data class WishlistItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val name: String,
    val photoUri: String? = null,
    val estimatedPrice: Long,
    val priority: String = "SEDANG", // TINGGI, SEDANG, RENDAH
    val category: String = "Lainnya",
    val note: String = "",
    val targetDate: Long = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000),
    val status: String = "BELUM_DIBELI", // BELUM_DIBELI, SEDANG_DITABUNG, SUDAH_TERBELI
    val savedAmount: Long = 0L,
    val linkedPocketId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "financial_targets",
    indices = [Index(value = ["accountId"])]
)
data class FinancialTarget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val title: String,
    val category: String = "General",
    val iconName: String = "flag",
    val photoUri: String? = null,
    val targetAmount: Long,
    val collectedAmount: Long = 0L,
    val startDate: Long = System.currentTimeMillis(),
    val deadlineDate: Long = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000),
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
