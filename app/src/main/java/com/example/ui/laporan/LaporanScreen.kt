package com.example.ui.laporan

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserAccount
import com.example.ui.common.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ZavaViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanScreen(
    viewModel: ZavaViewModel,
    user: UserAccount,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsState()
    val pockets by viewModel.pockets.collectAsState()

    var selectedPeriod by remember { mutableStateOf("BULANAN") } // HARIAN, MINGGUAN, BULANAN, TAHUNAN, SEMUA
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    val now = remember { System.currentTimeMillis() }
    val oneDay = 24L * 60 * 60 * 1000

    val filteredTransactions = remember(transactions, selectedPeriod, selectedCategoryFilter) {
        val calNow = Calendar.getInstance()
        transactions.filter { tx ->
            val matchPeriod = when (selectedPeriod) {
                "HARIAN" -> {
                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
                    txCal.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR) &&
                            txCal.get(Calendar.YEAR) == calNow.get(Calendar.YEAR)
                }
                "MINGGUAN" -> {
                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
                    txCal.get(Calendar.WEEK_OF_YEAR) == calNow.get(Calendar.WEEK_OF_YEAR) &&
                            txCal.get(Calendar.YEAR) == calNow.get(Calendar.YEAR)
                }
                "BULANAN" -> {
                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
                    txCal.get(Calendar.MONTH) == calNow.get(Calendar.MONTH) &&
                            txCal.get(Calendar.YEAR) == calNow.get(Calendar.YEAR)
                }
                "TAHUNAN" -> {
                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
                    txCal.get(Calendar.YEAR) == calNow.get(Calendar.YEAR)
                }
                else -> true
            }

            val matchCategory = selectedCategoryFilter == null || tx.category == selectedCategoryFilter
            matchPeriod && matchCategory
        }
    }

    val totalIncome = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalExpense = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val netCashflow = totalIncome - totalExpense
    val totalSavings = remember(pockets) { pockets.sumOf { it.currentBalance } }

    // Top Expense Categories calculation
    val topExpenseCategories = remember(filteredTransactions) {
        val expenseTx = filteredTransactions.filter { it.type == "EXPENSE" }
        expenseTx.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    val currencyFormat = user.currencyFormat

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan & Statistik", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.exportCsv { csvContent ->
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, csvContent)
                                    putExtra(Intent.EXTRA_TITLE, "laporan_keuangan_${user.username}.csv")
                                    type = "text/csv"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Ekspor Laporan CSV"))
                            }
                        }
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Ekspor CSV", tint = GoldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period Filter Chips
            item {
                val periods = listOf(
                    "HARIAN" to "Harian",
                    "MINGGUAN" to "Mingguan",
                    "BULANAN" to "Bulanan",
                    "TAHUNAN" to "Tahunan",
                    "SEMUA" to "Semua"
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(periods) { (key, label) ->
                        FilterChip(
                            selected = selectedPeriod == key,
                            onClick = { selectedPeriod = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = NavyDeep
                            )
                        )
                    }
                }
            }

            // Overview Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ZavaCard(
                        modifier = Modifier.weight(1f),
                        borderColor = SuccessGreen.copy(alpha = 0.4f)
                    ) {
                        Text("Pemasukan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FormatUtil.formatRupiah(totalIncome, currencyFormat),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }

                    ZavaCard(
                        modifier = Modifier.weight(1f),
                        borderColor = DangerRed.copy(alpha = 0.4f)
                    ) {
                        Text("Pengeluaran", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FormatUtil.formatRupiah(totalExpense, currencyFormat),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DangerRed
                        )
                    }
                }
            }

            // Cashflow & Savings Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ZavaCard(
                        modifier = Modifier.weight(1f),
                        borderColor = if (netCashflow >= 0) GoldAccent.copy(alpha = 0.4f) else DangerRed.copy(alpha = 0.4f)
                    ) {
                        Text("Selisih Bersih", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FormatUtil.formatRupiah(netCashflow, currencyFormat),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (netCashflow >= 0) GoldAccent else DangerRed
                        )
                    }

                    ZavaCard(
                        modifier = Modifier.weight(1f),
                        borderColor = InfoBlue.copy(alpha = 0.4f)
                    ) {
                        Text("Total Tabungan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FormatUtil.formatRupiah(totalSavings, currencyFormat),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InfoBlue
                        )
                    }
                }
            }

            // Visual Comparison Bar Chart
            item {
                SimpleBarChart(
                    income = totalIncome,
                    expense = totalExpense,
                    currencyFormat = currencyFormat
                )
            }

            // Top Expense Categories Section
            item {
                ZavaCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Kategori Pengeluaran Terbesar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (topExpenseCategories.isEmpty()) {
                        Text(
                            text = "Tidak ada pengeluaran pada periode ini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val maxExpense = topExpenseCategories.first().second
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            topExpenseCategories.take(6).forEach { (category, amount) ->
                                val fraction = if (maxExpense > 0) (amount.toFloat() / maxExpense.toFloat()).coerceIn(0.05f, 1f) else 0f
                                val percentOfTotal = if (totalExpense > 0) ((amount.toDouble() / totalExpense.toDouble()) * 100).toInt() else 0

                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = category,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${FormatUtil.formatRupiah(amount, currencyFormat)} ($percentOfTotal%)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = DangerRed
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.Gray.copy(alpha = 0.15f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(fraction)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(DangerRed)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick CSV Export Card
            item {
                ZavaCard(
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    borderColor = GoldAccent.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ekspor Semua Transaksi",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Format CSV standar yang dapat dibuka di Microsoft Excel atau Google Spreadsheet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.exportCsv { csvContent ->
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, csvContent)
                                        putExtra(Intent.EXTRA_TITLE, "transaksi_${user.username}.csv")
                                        type = "text/csv"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Ekspor Transaksi ke CSV"))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDeep),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Unduh CSV", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
