package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.ZavaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ZavaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ZavaRepository.getInstance(application)

    val currentAccount: StateFlow<UserAccount?> = repository.currentAccount
    val allUsers: StateFlow<List<UserAccount>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Auto-login to first user if exists and no current account
    init {
        viewModelScope.launch {
            repository.allUsers.collect { users ->
                if (repository.currentAccount.value == null && users.isNotEmpty()) {
                    repository.switchAccount(users.first())
                }
            }
        }
    }

    private val accountIdFlow = currentAccount.map { it?.id ?: -1L }

    val transactions: StateFlow<List<TransactionEntity>> = accountIdFlow.flatMapLatest { id ->
        if (id == -1L) flowOf(emptyList()) else repository.getTransactions(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pockets: StateFlow<List<SavingsPocket>> = accountIdFlow.flatMapLatest { id ->
        if (id == -1L) flowOf(emptyList()) else repository.getPockets(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlists: StateFlow<List<WishlistItem>> = accountIdFlow.flatMapLatest { id ->
        if (id == -1L) flowOf(emptyList()) else repository.getWishlists(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val targets: StateFlow<List<FinancialTarget>> = accountIdFlow.flatMapLatest { id ->
        if (id == -1L) flowOf(emptyList()) else repository.getTargets(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Feedback state
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun showMessage(msg: String) {
        _uiMessage.value = msg
    }

    // Auth actions
    fun register(username: String, fullName: String, pass: String, photoUri: String? = null, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = repository.registerUser(username, fullName, pass, photoUri)
            res.fold(
                onSuccess = {
                    showMessage("Selamat datang, ${it.fullName}! Akun berhasil dibuat.")
                    onResult(true, null)
                },
                onFailure = {
                    onResult(false, it.message)
                }
            )
        }
    }

    fun login(username: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = repository.loginUser(username, pass)
            res.fold(
                onSuccess = {
                    showMessage("Login berhasil. Selamat datang kembali, ${it.fullName}!")
                    onResult(true, null)
                },
                onFailure = {
                    onResult(false, it.message)
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            showMessage("Anda telah logout")
        }
    }

    fun switchAccount(account: UserAccount) {
        viewModelScope.launch {
            repository.switchAccount(account)
            showMessage("Beralih ke akun ${account.fullName}")
        }
    }

    fun updateProfile(fullName: String, photoUri: String?) {
        val user = currentAccount.value ?: return
        viewModelScope.launch {
            val updated = user.copy(
                fullName = fullName.trim().ifBlank { user.fullName },
                profilePhotoUri = photoUri ?: user.profilePhotoUri
            )
            repository.updateUser(updated)
            showMessage("Profil berhasil diperbarui")
        }
    }

    fun changePassword(oldPass: String, newPass: String, onResult: (Boolean, String?) -> Unit) {
        val user = currentAccount.value ?: return
        viewModelScope.launch {
            val res = repository.changePassword(user, oldPass, newPass)
            res.fold(
                onSuccess = {
                    showMessage("PIN/Password berhasil diubah")
                    onResult(true, null)
                },
                onFailure = {
                    onResult(false, it.message)
                }
            )
        }
    }

    fun deleteCurrentAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteCurrentAccount()
            showMessage("Akun berhasil dihapus")
            onComplete()
        }
    }

    // Personalisasi
    fun setWallpaper(uri: String?) {
        val user = currentAccount.value ?: return
        viewModelScope.launch {
            val updated = user.copy(wallpaperUri = uri)
            repository.updateUser(updated)
            showMessage(if (uri != null) "Wallpaper berhasil dipasang" else "Wallpaper dihapus")
        }
    }

    fun setTheme(themeName: String) {
        val user = currentAccount.value ?: return
        viewModelScope.launch {
            val updated = user.copy(activeTheme = themeName)
            repository.updateUser(updated)
        }
    }

    fun setCustomAccent(colorHex: String) {
        val user = currentAccount.value ?: return
        viewModelScope.launch {
            val updated = user.copy(customAccentHex = colorHex, activeTheme = "CUSTOM")
            repository.updateUser(updated)
        }
    }

    fun togglePrivacy(enabled: Boolean) {
        val user = currentAccount.value ?: return
        viewModelScope.launch {
            val updated = user.copy(isPrivacyEnabled = enabled)
            repository.updateUser(updated)
        }
    }

    fun setFontSize(isLarge: Boolean) {
        val user = currentAccount.value ?: return
        viewModelScope.launch {
            val updated = user.copy(isLargeFont = isLarge)
            repository.updateUser(updated)
        }
    }

    fun setCurrencyFormat(format: String) {
        val user = currentAccount.value ?: return
        viewModelScope.launch {
            val updated = user.copy(currencyFormat = format)
            repository.updateUser(updated)
        }
    }

    // Transactions
    fun addTransaction(
        type: String,
        amount: Long,
        category: String,
        date: Long,
        sourceOrMethod: String,
        note: String,
        attachmentUri: String?
    ) {
        val accountId = currentAccount.value?.id ?: return
        viewModelScope.launch {
            val tx = TransactionEntity(
                accountId = accountId,
                type = type,
                amount = amount,
                category = category,
                date = date,
                sourceOrMethod = sourceOrMethod,
                note = note,
                attachmentUri = attachmentUri
            )
            repository.addTransaction(tx)
            showMessage("Transaksi berhasil disimpan")
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
            showMessage("Transaksi berhasil diperbarui")
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            showMessage("Transaksi berhasil dihapus")
        }
    }

    // Savings Pockets
    fun createPocket(
        name: String,
        category: String,
        targetAmount: Long,
        initialBalance: Long,
        startDate: Long,
        targetDate: Long,
        note: String,
        colorHex: String
    ) {
        val accountId = currentAccount.value?.id ?: return
        viewModelScope.launch {
            val pocket = SavingsPocket(
                accountId = accountId,
                name = name,
                category = category,
                targetAmount = targetAmount,
                currentBalance = initialBalance,
                startDate = startDate,
                targetDate = targetDate,
                note = note,
                colorHex = colorHex
            )
            val pocketId = repository.addPocket(pocket)
            if (initialBalance > 0) {
                repository.depositSavings(pocket.copy(id = pocketId), initialBalance, "Setoran awal")
            }
            showMessage("Kantong tabungan '$name' berhasil dibuat")
        }
    }

    fun updatePocket(pocket: SavingsPocket) {
        viewModelScope.launch {
            repository.updatePocket(pocket)
            showMessage("Tabungan berhasil diperbarui")
        }
    }

    fun deletePocket(pocket: SavingsPocket) {
        viewModelScope.launch {
            repository.deletePocket(pocket)
            showMessage("Tabungan '${pocket.name}' berhasil dihapus")
        }
    }

    fun depositSavings(pocket: SavingsPocket, amount: Long, note: String) {
        viewModelScope.launch {
            val res = repository.depositSavings(pocket, amount, note)
            res.fold(
                onSuccess = { showMessage("Berhasil menabung Rp$amount ke ${pocket.name}") },
                onFailure = { showMessage(it.message ?: "Gagal menabung") }
            )
        }
    }

    fun withdrawSavings(pocket: SavingsPocket, amount: Long, note: String) {
        viewModelScope.launch {
            val res = repository.withdrawSavings(pocket, amount, note)
            res.fold(
                onSuccess = { showMessage("Berhasil menarik Rp$amount dari ${pocket.name}") },
                onFailure = { showMessage(it.message ?: "Gagal menarik dana") }
            )
        }
    }

    // Wishlist
    fun createWishlist(
        name: String,
        estimatedPrice: Long,
        priority: String,
        category: String,
        note: String,
        targetDate: Long,
        photoUri: String?
    ) {
        val accountId = currentAccount.value?.id ?: return
        viewModelScope.launch {
            val item = WishlistItem(
                accountId = accountId,
                name = name,
                estimatedPrice = estimatedPrice,
                priority = priority,
                category = category,
                note = note,
                targetDate = targetDate,
                photoUri = photoUri,
                status = "BELUM_DIBELI"
            )
            repository.addWishlist(item)
            showMessage("Wishlist '$name' berhasil ditambahkan")
        }
    }

    fun updateWishlist(item: WishlistItem) {
        viewModelScope.launch {
            repository.updateWishlist(item)
            showMessage("Wishlist berhasil diperbarui")
        }
    }

    fun deleteWishlist(item: WishlistItem) {
        viewModelScope.launch {
            repository.deleteWishlist(item)
            showMessage("Wishlist dihapus")
        }
    }

    fun addWishlistFunds(item: WishlistItem, amount: Long) {
        viewModelScope.launch {
            val res = repository.addWishlistFunds(item, amount)
            res.fold(
                onSuccess = { showMessage("Berhasil menambah tabungan wishlist") },
                onFailure = { showMessage(it.message ?: "Gagal menambah dana") }
            )
        }
    }

    // Targets
    fun createTarget(
        title: String,
        category: String,
        targetAmount: Long,
        collectedAmount: Long,
        startDate: Long,
        deadlineDate: Long,
        note: String
    ) {
        val accountId = currentAccount.value?.id ?: return
        viewModelScope.launch {
            val target = FinancialTarget(
                accountId = accountId,
                title = title,
                category = category,
                targetAmount = targetAmount,
                collectedAmount = collectedAmount,
                startDate = startDate,
                deadlineDate = deadlineDate,
                note = note
            )
            repository.addTarget(target)
            showMessage("Target keuangan '$title' berhasil dibuat")
        }
    }

    fun updateTarget(target: FinancialTarget) {
        viewModelScope.launch {
            repository.updateTarget(target)
            showMessage("Target berhasil diperbarui")
        }
    }

    fun deleteTarget(target: FinancialTarget) {
        viewModelScope.launch {
            repository.deleteTarget(target)
            showMessage("Target dihapus")
        }
    }

    fun addTargetFunds(target: FinancialTarget, amount: Long) {
        viewModelScope.launch {
            val res = repository.addTargetFunds(target, amount)
            res.fold(
                onSuccess = { showMessage("Target '${target.title}' berhasil ditambah dana") },
                onFailure = { showMessage(it.message ?: "Gagal menambah dana") }
            )
        }
    }

    // Backup & Restore
    fun exportBackup(onResult: (String?) -> Unit) {
        val accountId = currentAccount.value?.id ?: return
        viewModelScope.launch {
            val res = repository.exportBackupJson(accountId)
            res.fold(
                onSuccess = { onResult(it) },
                onFailure = {
                    showMessage("Gagal mengekspor: ${it.message}")
                    onResult(null)
                }
            )
        }
    }

    fun restoreBackup(jsonString: String, onResult: (Boolean, String) -> Unit) {
        val accountId = currentAccount.value?.id ?: return
        viewModelScope.launch {
            val res = repository.restoreBackupJson(accountId, jsonString)
            res.fold(
                onSuccess = {
                    showMessage("Data berhasil dipulihkan!")
                    onResult(true, it)
                },
                onFailure = {
                    showMessage("Gagal memulihkan: ${it.message}")
                    onResult(false, it.message ?: "Error tidak diketahui")
                }
            )
        }
    }

    fun exportCsv(onResult: (String) -> Unit) {
        val accountId = currentAccount.value?.id ?: return
        viewModelScope.launch {
            val csv = repository.generateCsvString(accountId)
            onResult(csv)
        }
    }
}
