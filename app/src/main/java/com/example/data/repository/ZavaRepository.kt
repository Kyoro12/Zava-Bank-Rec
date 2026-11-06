package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.security.SecurityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ZavaRepository(private val db: AppDatabase, private val context: Context) {

    private val userDao = db.userDao()
    private val transactionDao = db.transactionDao()
    private val savingsDao = db.savingsDao()
    private val wishlistDao = db.wishlistDao()
    private val targetDao = db.targetDao()

    val allUsers: Flow<List<UserAccount>> = userDao.getAllUsers()

    private val _currentAccount = MutableStateFlow<UserAccount?>(null)
    val currentAccount: StateFlow<UserAccount?> = _currentAccount.asStateFlow()

    suspend fun switchAccount(account: UserAccount) {
        _currentAccount.value = account
    }

    suspend fun logout() {
        _currentAccount.value = null
    }

    suspend fun registerUser(
        username: String,
        fullName: String,
        password: String,
        photoUri: String? = null
    ): Result<UserAccount> = withContext(Dispatchers.IO) {
        try {
            val cleanUsername = username.trim().lowercase(Locale.ROOT)
            if (cleanUsername.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Username tidak boleh kosong"))
            }
            if (password.length < 4) {
                return@withContext Result.failure(IllegalArgumentException("Password/PIN minimal 4 karakter"))
            }
            val existing = userDao.getUserByUsername(cleanUsername)
            if (existing != null) {
                return@withContext Result.failure(IllegalArgumentException("Username '$cleanUsername' sudah digunakan di perangkat ini"))
            }

            val salt = SecurityUtil.generateSalt()
            val hash = SecurityUtil.hashPassword(password, salt)
            val newUser = UserAccount(
                username = cleanUsername,
                fullName = fullName.trim().ifBlank { cleanUsername },
                passwordHash = hash,
                salt = salt,
                profilePhotoUri = photoUri,
                createdAt = System.currentTimeMillis()
            )
            val newId = userDao.insertUser(newUser)
            val savedUser = newUser.copy(id = newId)

            // Seed initial starter welcome data for smooth onboarding
            seedWelcomeData(newId)

            _currentAccount.value = savedUser
            Result.success(savedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(username: String, password: String): Result<UserAccount> = withContext(Dispatchers.IO) {
        try {
            val cleanUsername = username.trim().lowercase(Locale.ROOT)
            val user = userDao.getUserByUsername(cleanUsername)
                ?: return@withContext Result.failure(IllegalArgumentException("Username tidak ditemukan"))

            val isValid = SecurityUtil.verifyPassword(password, user.salt, user.passwordHash)
            if (!isValid) {
                return@withContext Result.failure(IllegalArgumentException("Password atau PIN salah"))
            }

            _currentAccount.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: UserAccount) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
        if (_currentAccount.value?.id == user.id) {
            _currentAccount.value = user
        }
    }

    suspend fun changePassword(user: UserAccount, oldPass: String, newPass: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!SecurityUtil.verifyPassword(oldPass, user.salt, user.passwordHash)) {
            return@withContext Result.failure(IllegalArgumentException("Password/PIN lama tidak cocok"))
        }
        if (newPass.length < 4) {
            return@withContext Result.failure(IllegalArgumentException("Password/PIN baru minimal 4 karakter"))
        }
        val newSalt = SecurityUtil.generateSalt()
        val newHash = SecurityUtil.hashPassword(newPass, newSalt)
        val updated = user.copy(passwordHash = newHash, salt = newSalt)
        userDao.updateUser(updated)
        _currentAccount.value = updated
        Result.success(Unit)
    }

    suspend fun deleteCurrentAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        val current = _currentAccount.value ?: return@withContext Result.failure(IllegalStateException("Tidak ada akun aktif"))
        try {
            transactionDao.deleteAllByAccount(current.id)
            savingsDao.deleteAllPocketsByAccount(current.id)
            savingsDao.deleteAllLogsByAccount(current.id)
            wishlistDao.deleteAllByAccount(current.id)
            targetDao.deleteAllByAccount(current.id)
            userDao.deleteUser(current)
            _currentAccount.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Transactions
    fun getTransactions(accountId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByAccount(accountId)

    suspend fun addTransaction(transaction: TransactionEntity): Long = withContext(Dispatchers.IO) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(transaction)
    }

    // Savings Pockets
    fun getPockets(accountId: Long): Flow<List<SavingsPocket>> =
        savingsDao.getPocketsByAccount(accountId)

    fun getPocketLogs(pocketId: Long): Flow<List<SavingsLog>> =
        savingsDao.getLogsByPocket(pocketId)

    suspend fun addPocket(pocket: SavingsPocket): Long = withContext(Dispatchers.IO) {
        savingsDao.insertPocket(pocket)
    }

    suspend fun updatePocket(pocket: SavingsPocket) = withContext(Dispatchers.IO) {
        savingsDao.updatePocket(pocket)
    }

    suspend fun deletePocket(pocket: SavingsPocket) = withContext(Dispatchers.IO) {
        savingsDao.deleteLogsByPocket(pocket.id)
        savingsDao.deletePocket(pocket)
    }

    suspend fun depositSavings(pocket: SavingsPocket, amount: Long, note: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext Result.failure(IllegalArgumentException("Nominal harus lebih dari 0"))
        val updated = pocket.copy(currentBalance = pocket.currentBalance + amount)
        savingsDao.updatePocket(updated)
        savingsDao.insertLog(
            SavingsLog(
                pocketId = pocket.id,
                accountId = pocket.accountId,
                type = "DEPOSIT",
                amount = amount,
                note = note.ifBlank { "Setoran tabungan" }
            )
        )
        Result.success(Unit)
    }

    suspend fun withdrawSavings(pocket: SavingsPocket, amount: Long, note: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext Result.failure(IllegalArgumentException("Nominal harus lebih dari 0"))
        if (amount > pocket.currentBalance) {
            return@withContext Result.failure(IllegalArgumentException("Saldo tabungan tidak mencukupi (Tersedia: Rp${pocket.currentBalance})"))
        }
        val updated = pocket.copy(currentBalance = pocket.currentBalance - amount)
        savingsDao.updatePocket(updated)
        savingsDao.insertLog(
            SavingsLog(
                pocketId = pocket.id,
                accountId = pocket.accountId,
                type = "WITHDRAW",
                amount = amount,
                note = note.ifBlank { "Penarikan tabungan" }
            )
        )
        Result.success(Unit)
    }

    // Wishlist
    fun getWishlists(accountId: Long): Flow<List<WishlistItem>> =
        wishlistDao.getWishlistsByAccount(accountId)

    suspend fun addWishlist(item: WishlistItem): Long = withContext(Dispatchers.IO) {
        wishlistDao.insertWishlist(item)
    }

    suspend fun updateWishlist(item: WishlistItem) = withContext(Dispatchers.IO) {
        wishlistDao.updateWishlist(item)
    }

    suspend fun deleteWishlist(item: WishlistItem) = withContext(Dispatchers.IO) {
        wishlistDao.deleteWishlist(item)
    }

    suspend fun addWishlistFunds(item: WishlistItem, amount: Long): Result<Unit> = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext Result.failure(IllegalArgumentException("Nominal harus lebih dari 0"))
        val newSaved = item.savedAmount + amount
        val newStatus = if (newSaved >= item.estimatedPrice) "SUDAH_TERBELI" else "SEDANG_DITABUNG"
        val updated = item.copy(savedAmount = newSaved, status = newStatus)
        wishlistDao.updateWishlist(updated)
        Result.success(Unit)
    }

    // Targets
    fun getTargets(accountId: Long): Flow<List<FinancialTarget>> =
        targetDao.getTargetsByAccount(accountId)

    suspend fun addTarget(target: FinancialTarget): Long = withContext(Dispatchers.IO) {
        targetDao.insertTarget(target)
    }

    suspend fun updateTarget(target: FinancialTarget) = withContext(Dispatchers.IO) {
        targetDao.updateTarget(target)
    }

    suspend fun deleteTarget(target: FinancialTarget) = withContext(Dispatchers.IO) {
        targetDao.deleteTarget(target)
    }

    suspend fun addTargetFunds(target: FinancialTarget, amount: Long): Result<Unit> = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext Result.failure(IllegalArgumentException("Nominal harus lebih dari 0"))
        val newCollected = target.collectedAmount + amount
        val updated = target.copy(collectedAmount = newCollected)
        targetDao.updateTarget(updated)
        Result.success(Unit)
    }

    // Backup & Restore
    suspend fun exportBackupJson(accountId: Long): Result<String> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.getUserById(accountId)
                ?: return@withContext Result.failure(IllegalStateException("User tidak ditemukan"))
            val transactions = transactionDao.getAllListByAccount(accountId)
            val pockets = savingsDao.getAllPocketsListByAccount(accountId)
            val logs = savingsDao.getAllLogsListByAccount(accountId)
            val wishlists = wishlistDao.getAllListByAccount(accountId)
            val targets = targetDao.getAllListByAccount(accountId)

            val rootJson = JSONObject().apply {
                put("app", "ZAVA_BANK_REC")
                put("version", "1.0.0")
                put("exportedAt", System.currentTimeMillis())

                val userObj = JSONObject().apply {
                    put("username", user.username)
                    put("fullName", user.fullName)
                    put("activeTheme", user.activeTheme)
                    put("customAccentHex", user.customAccentHex)
                    put("currencyFormat", user.currencyFormat)
                    put("isPrivacyEnabled", user.isPrivacyEnabled)
                    put("isLargeFont", user.isLargeFont)
                }
                put("userProfile", userObj)

                val txArray = JSONArray()
                transactions.forEach { tx ->
                    txArray.put(JSONObject().apply {
                        put("type", tx.type)
                        put("amount", tx.amount)
                        put("category", tx.category)
                        put("date", tx.date)
                        put("sourceOrMethod", tx.sourceOrMethod)
                        put("note", tx.note)
                    })
                }
                put("transactions", txArray)

                val pocketsArray = JSONArray()
                pockets.forEach { p ->
                    pocketsArray.put(JSONObject().apply {
                        put("name", p.name)
                        put("category", p.category)
                        put("targetAmount", p.targetAmount)
                        put("currentBalance", p.currentBalance)
                        put("startDate", p.startDate)
                        put("targetDate", p.targetDate)
                        put("note", p.note)
                        put("iconName", p.iconName)
                        put("colorHex", p.colorHex)
                    })
                }
                put("savingsPockets", pocketsArray)

                val wishlistArray = JSONArray()
                wishlists.forEach { w ->
                    wishlistArray.put(JSONObject().apply {
                        put("name", w.name)
                        put("estimatedPrice", w.estimatedPrice)
                        put("priority", w.priority)
                        put("category", w.category)
                        put("note", w.note)
                        put("targetDate", w.targetDate)
                        put("status", w.status)
                        put("savedAmount", w.savedAmount)
                    })
                }
                put("wishlist", wishlistArray)

                val targetsArray = JSONArray()
                targets.forEach { t ->
                    targetsArray.put(JSONObject().apply {
                        put("title", t.title)
                        put("category", t.category)
                        put("iconName", t.iconName)
                        put("targetAmount", t.targetAmount)
                        put("collectedAmount", t.collectedAmount)
                        put("startDate", t.startDate)
                        put("deadlineDate", t.deadlineDate)
                        put("note", t.note)
                    })
                }
                put("targets", targetsArray)
            }

            Result.success(rootJson.toString(2))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreBackupJson(accountId: Long, jsonString: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("app") || root.getString("app") != "ZAVA_BANK_REC") {
                return@withContext Result.failure(IllegalArgumentException("Format file backup tidak valid untuk ZAVA BANK REC"))
            }

            // Clean old data for this account
            transactionDao.deleteAllByAccount(accountId)
            savingsDao.deleteAllPocketsByAccount(accountId)
            savingsDao.deleteAllLogsByAccount(accountId)
            wishlistDao.deleteAllByAccount(accountId)
            targetDao.deleteAllByAccount(accountId)

            // Restore user settings if present
            if (root.has("userProfile")) {
                val u = root.getJSONObject("userProfile")
                val currentUser = userDao.getUserById(accountId)
                if (currentUser != null) {
                    val updated = currentUser.copy(
                        fullName = u.optString("fullName", currentUser.fullName),
                        activeTheme = u.optString("activeTheme", currentUser.activeTheme),
                        customAccentHex = u.optString("customAccentHex", currentUser.customAccentHex),
                        currencyFormat = u.optString("currencyFormat", currentUser.currencyFormat),
                        isPrivacyEnabled = u.optBoolean("isPrivacyEnabled", currentUser.isPrivacyEnabled),
                        isLargeFont = u.optBoolean("isLargeFont", currentUser.isLargeFont)
                    )
                    userDao.updateUser(updated)
                    _currentAccount.value = updated
                }
            }

            // Restore transactions
            var txCount = 0
            if (root.has("transactions")) {
                val txArray = root.getJSONArray("transactions")
                for (i in 0 until txArray.length()) {
                    val obj = txArray.getJSONObject(i)
                    transactionDao.insertTransaction(
                        TransactionEntity(
                            accountId = accountId,
                            type = obj.getString("type"),
                            amount = obj.getLong("amount"),
                            category = obj.getString("category"),
                            date = obj.optLong("date", System.currentTimeMillis()),
                            sourceOrMethod = obj.optString("sourceOrMethod", "-"),
                            note = obj.optString("note", "")
                        )
                    )
                    txCount++
                }
            }

            // Restore savings pockets
            var pocketCount = 0
            if (root.has("savingsPockets")) {
                val pArray = root.getJSONArray("savingsPockets")
                for (i in 0 until pArray.length()) {
                    val obj = pArray.getJSONObject(i)
                    savingsDao.insertPocket(
                        SavingsPocket(
                            accountId = accountId,
                            name = obj.getString("name"),
                            category = obj.optString("category", "UMUM"),
                            targetAmount = obj.getLong("targetAmount"),
                            currentBalance = obj.optLong("currentBalance", 0L),
                            startDate = obj.optLong("startDate", System.currentTimeMillis()),
                            targetDate = obj.optLong("targetDate", System.currentTimeMillis() + 180L * 24 * 60 * 60 * 1000),
                            note = obj.optString("note", ""),
                            iconName = obj.optString("iconName", "savings"),
                            colorHex = obj.optString("colorHex", "#F59E0B")
                        )
                    )
                    pocketCount++
                }
            }

            // Restore wishlists
            var wishlistCount = 0
            if (root.has("wishlist")) {
                val wArray = root.getJSONArray("wishlist")
                for (i in 0 until wArray.length()) {
                    val obj = wArray.getJSONObject(i)
                    wishlistDao.insertWishlist(
                        WishlistItem(
                            accountId = accountId,
                            name = obj.getString("name"),
                            estimatedPrice = obj.getLong("estimatedPrice"),
                            priority = obj.optString("priority", "SEDANG"),
                            category = obj.optString("category", "Lainnya"),
                            note = obj.optString("note", ""),
                            targetDate = obj.optLong("targetDate", System.currentTimeMillis() + 90L * 24 * 60 * 60 * 1000),
                            status = obj.optString("status", "BELUM_DIBELI"),
                            savedAmount = obj.optLong("savedAmount", 0L)
                        )
                    )
                    wishlistCount++
                }
            }

            // Restore targets
            var targetCount = 0
            if (root.has("targets")) {
                val tArray = root.getJSONArray("targets")
                for (i in 0 until tArray.length()) {
                    val obj = tArray.getJSONObject(i)
                    targetDao.insertTarget(
                        FinancialTarget(
                            accountId = accountId,
                            title = obj.getString("title"),
                            category = obj.optString("category", "General"),
                            iconName = obj.optString("iconName", "flag"),
                            targetAmount = obj.getLong("targetAmount"),
                            collectedAmount = obj.optLong("collectedAmount", 0L),
                            startDate = obj.optLong("startDate", System.currentTimeMillis()),
                            deadlineDate = obj.optLong("deadlineDate", System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000),
                            note = obj.optString("note", "")
                        )
                    )
                    targetCount++
                }
            }

            Result.success("Berhasil memulihkan: $txCount transaksi, $pocketCount tabungan, $wishlistCount wishlist, $targetCount target.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateCsvString(accountId: Long): String = withContext(Dispatchers.IO) {
        val transactions = transactionDao.getAllListByAccount(accountId)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("ID,Tipe,Tanggal,Kategori,Nominal,Metode_Sumber,Catatan\n")
        transactions.forEach { tx ->
            val dateStr = sdf.format(Date(tx.date))
            val cleanNote = tx.note.replace("\"", "\"\"")
            sb.append("${tx.id},\"${tx.type}\",\"$dateStr\",\"${tx.category}\",${tx.amount},\"${tx.sourceOrMethod}\",\"$cleanNote\"\n")
        }
        sb.toString()
    }

    private suspend fun seedWelcomeData(accountId: Long) {
        // Tabungan Buat Nikah (Special highlight requested in brief!)
        val weddingDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000) // 1 year
        savingsDao.insertPocket(
            SavingsPocket(
                accountId = accountId,
                name = "Tabungan Pernikahan Impian",
                category = "NIKAH",
                targetAmount = 50_000_000L,
                currentBalance = 15_000_000L,
                startDate = System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000),
                targetDate = weddingDate,
                note = "Persiapan resepsi, katering & cincin nikah",
                colorHex = "#E5B80B"
            )
        )

        // Tabungan Umum (Dana Darurat)
        savingsDao.insertPocket(
            SavingsPocket(
                accountId = accountId,
                name = "Dana Darurat (Emergency Fund)",
                category = "UMUM",
                targetAmount = 20_000_000L,
                currentBalance = 5_000_000L,
                note = "Simpanan darurat 6 bulan biaya hidup",
                colorHex = "#3B82F6"
            )
        )

        // Sample initial income & expenses
        val now = System.currentTimeMillis()
        val oneDay = 24L * 60 * 60 * 1000

        transactionDao.insertTransaction(
            TransactionEntity(
                accountId = accountId,
                type = "INCOME",
                amount = 8_500_000L,
                category = "Gaji",
                date = now - (3 * oneDay),
                sourceOrMethod = "Transfer Bank",
                note = "Gaji Bulanan Utama"
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                accountId = accountId,
                type = "INCOME",
                amount = 1_500_000L,
                category = "Freelance",
                date = now - (1 * oneDay),
                sourceOrMethod = "Transfer Bank",
                note = "Proyek Desain Web"
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                accountId = accountId,
                type = "EXPENSE",
                amount = 650_000L,
                category = "Belanja",
                date = now - (2 * oneDay),
                sourceOrMethod = "QRIS",
                note = "Belanja Mingguan Supermarket"
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                accountId = accountId,
                type = "EXPENSE",
                amount = 350_000L,
                category = "Tagihan",
                date = now - (1 * oneDay),
                sourceOrMethod = "E-Wallet",
                note = "Tagihan Listrik & Internet"
            )
        )

        // Sample Wishlist
        wishlistDao.insertWishlist(
            WishlistItem(
                accountId = accountId,
                name = "Smartphone Flagship Baru",
                estimatedPrice = 12_000_000L,
                priority = "TINGGI",
                category = "Kebutuhan pribadi",
                note = "Untuk kerja dan fotografi",
                savedAmount = 4_000_000L,
                status = "SEDANG_DITABUNG"
            )
        )

        // Sample Financial Target
        targetDao.insertTarget(
            FinancialTarget(
                accountId = accountId,
                title = "DP Rumah Pertama",
                category = "Investasi",
                targetAmount = 100_000_000L,
                collectedAmount = 25_000_000L,
                deadlineDate = now + (730L * oneDay),
                note = "Persiapan masa depan keluarga"
            )
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: ZavaRepository? = null

        fun getInstance(context: Context): ZavaRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getInstance(context)
                val instance = ZavaRepository(db, context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
