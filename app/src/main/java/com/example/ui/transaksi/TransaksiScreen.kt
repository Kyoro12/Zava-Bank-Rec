package com.example.ui.transaksi

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.TransactionEntity
import com.example.data.model.UserAccount
import com.example.ui.common.*
import com.example.ui.dashboard.TransactionItemRow
import com.example.ui.theme.*
import com.example.ui.viewmodel.ZavaViewModel

@Composable
fun TransaksiScreen(
    viewModel: ZavaViewModel,
    user: UserAccount,
    initialTab: String = "ALL", // "ALL", "INCOME", "EXPENSE"
    onOpenAdd: (isIncome: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()
    var selectedTab by remember { mutableStateOf(initialTab) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    val filteredTransactions = remember(transactions, selectedTab, searchQuery, selectedCategoryFilter) {
        transactions.filter { tx ->
            val matchType = when (selectedTab) {
                "INCOME" -> tx.type == "INCOME"
                "EXPENSE" -> tx.type == "EXPENSE"
                else -> true
            }
            val matchSearch = searchQuery.isBlank() ||
                    tx.category.contains(searchQuery, ignoreCase = true) ||
                    tx.note.contains(searchQuery, ignoreCase = true) ||
                    tx.sourceOrMethod.contains(searchQuery, ignoreCase = true)
            val matchCat = selectedCategoryFilter == null || tx.category == selectedCategoryFilter

            matchType && matchSearch && matchCat
        }
    }

    val totalIncome = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalExpense = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }

    val currencyFormat = user.currencyFormat

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onOpenAdd(selectedTab != "EXPENSE") },
                containerColor = if (selectedTab == "EXPENSE") DangerRed else GoldAccent,
                contentColor = if (selectedTab == "EXPENSE") Color.White else NavyDeep,
                modifier = Modifier
                    .padding(bottom = 68.dp)
                    .testTag("add_transaction_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Transaksi")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Catatan Transaksi",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tab bar: Semua, Pemasukan, Pengeluaran
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                TabItem(
                    title = "Semua",
                    isSelected = selectedTab == "ALL",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedTab = "ALL"
                        selectedCategoryFilter = null
                    }
                )
                TabItem(
                    title = "Pemasukan",
                    isSelected = selectedTab == "INCOME",
                    selectedColor = SuccessGreen,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedTab = "INCOME"
                        selectedCategoryFilter = null
                    }
                )
                TabItem(
                    title = "Pengeluaran",
                    isSelected = selectedTab == "EXPENSE",
                    selectedColor = DangerRed,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedTab = "EXPENSE"
                        selectedCategoryFilter = null
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari catatan, kategori, metode...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Summary banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                if (selectedTab != "EXPENSE") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Total Masuk", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = FormatUtil.formatRupiah(totalIncome, currencyFormat),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }
                if (selectedTab != "INCOME") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Total Keluar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = FormatUtil.formatRupiah(totalExpense, currencyFormat),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = DangerRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List of Transactions
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredTransactions.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Default.ReceiptLong,
                            title = "Tidak Ada Transaksi",
                            subtitle = "Belum ada transaksi pada kategori atau filter pencarian ini."
                        )
                    }
                } else {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        TransactionDetailCard(
                            transaction = tx,
                            currencyFormat = currencyFormat,
                            onEdit = { transactionToEdit = tx },
                            onDelete = { transactionToDelete = tx }
                        )
                    }
                }
            }
        }
    }

    // Edit Transaction Dialog
    if (transactionToEdit != null) {
        val tx = transactionToEdit!!
        EditTransactionDialog(
            transaction = tx,
            onDismiss = { transactionToEdit = null },
            onConfirm = { updated ->
                viewModel.updateTransaction(updated)
                transactionToEdit = null
            }
        )
    }

    // Delete Confirmation
    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Hapus Transaksi?") },
            text = { Text("Apakah Anda yakin ingin menghapus transaksi ${tx.category} senilai ${FormatUtil.formatRupiah(tx.amount, currencyFormat)}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(tx)
                        transactionToDelete = null
                    }
                ) {
                    Text("Hapus", color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun TabItem(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) selectedColor else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TransactionDetailCard(
    transaction: TransactionEntity,
    currencyFormat: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == "INCOME"

    ZavaCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(
                category = transaction.category,
                isIncome = isIncome
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = (if (isIncome) "+" else "-") + FormatUtil.formatRupiah(transaction.amount, currencyFormat),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isIncome) SuccessGreen else DangerRed
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${FormatUtil.formatDate(transaction.date)} • ${transaction.sourceOrMethod}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (transaction.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Receipt photo if attached
        if (transaction.attachmentUri != null) {
            Spacer(modifier = Modifier.height(8.dp))
            AsyncImage(
                model = transaction.attachmentUri,
                contentDescription = "Lampiran Struk",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit")
            }
            TextButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = DangerRed)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Hapus", color = DangerRed)
            }
        }
    }
}

@Composable
fun EditTransactionDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onConfirm: (TransactionEntity) -> Unit
) {
    var amountText by remember { mutableStateOf(transaction.amount.toString()) }
    var note by remember { mutableStateOf(transaction.note) }
    var sourceOrMethod by remember { mutableStateOf(transaction.sourceOrMethod) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Transaksi", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    label = { Text("Nominal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = sourceOrMethod,
                    onValueChange = { sourceOrMethod = it },
                    label = { Text(if (transaction.type == "INCOME") "Sumber Pemasukan" else "Metode Pembayaran") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: transaction.amount
                    onConfirm(transaction.copy(amount = amount, note = note, sourceOrMethod = sourceOrMethod))
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDeep)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
