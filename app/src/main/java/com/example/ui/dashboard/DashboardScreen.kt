package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SavingsPocket
import com.example.data.model.TransactionEntity
import com.example.data.model.UserAccount
import com.example.ui.common.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ZavaViewModel
import java.util.Calendar

@Composable
fun DashboardScreen(
    viewModel: ZavaViewModel,
    user: UserAccount,
    onNavigateToTabungan: () -> Unit,
    onNavigateToTransaksi: (initialType: String) -> Unit,
    onNavigateToWishlist: () -> Unit,
    onNavigateToTarget: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenAddTransaction: (isIncome: Boolean) -> Unit,
    onOpenAddPocket: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()
    val pockets by viewModel.pockets.collectAsState()
    val wishlists by viewModel.wishlists.collectAsState()
    val targets by viewModel.targets.collectAsState()

    var isPrivacy by remember { mutableStateOf(user.isPrivacyEnabled) }

    // Calculate this month's stats
    val calendar = remember { Calendar.getInstance() }
    val currentMonth = remember { calendar.get(Calendar.MONTH) }
    val currentYear = remember { calendar.get(Calendar.YEAR) }

    val thisMonthTransactions = remember(transactions) {
        transactions.filter { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
            txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
        }
    }

    val thisMonthIncome = remember(thisMonthTransactions) {
        thisMonthTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val thisMonthExpense = remember(thisMonthTransactions) {
        thisMonthTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val remainingBalance = remember(thisMonthIncome, thisMonthExpense) {
        thisMonthIncome - thisMonthExpense
    }

    val totalSavings = remember(pockets) {
        pockets.sumOf { it.currentBalance }
    }

    val allTimeIncome = remember(transactions) {
        transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val allTimeExpense = remember(transactions) {
        transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val totalBalance = remember(allTimeIncome, allTimeExpense, totalSavings) {
        (allTimeIncome - allTimeExpense) + totalSavings
    }

    val weddingPocket = remember(pockets) {
        pockets.firstOrNull { it.category == "NIKAH" }
    }

    val currencyFormat = user.currencyFormat

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Custom gallery wallpaper background if set by user
        if (user.wallpaperUri != null) {
            AsyncImage(
                model = user.wallpaperUri,
                contentDescription = "Wallpaper Dashboard",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Premium dark scrim overlay for razor-sharp readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                NavyDeep.copy(alpha = 0.85f),
                                NavyDeep.copy(alpha = 0.92f),
                                NavyDeep.copy(alpha = 0.98f)
                            )
                        )
                    )
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            // Header: Greeting & Profile
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small Official Logo and Greeting
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.5.dp,
                                    Brush.linearGradient(listOf(TurquoiseAccent, GoldAccent)),
                                    RoundedCornerShape(12.dp)
                                )
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.zava_official_logo),
                                contentDescription = "Logo ZAVA",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Selamat datang,",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = TurquoiseAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = user.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1
                            )
                        }
                    }

                    // Avatar button (distinct and separate from app logo)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(GoldAccent, Color(0xFFC59419)))
                            )
                            .border(2.dp, GoldAccent, CircleShape)
                            .clickable { onOpenProfile() }
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.profilePhotoUri != null) {
                            AsyncImage(
                                model = user.profilePhotoUri,
                                contentDescription = "Foto Profil Pengguna",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = user.fullName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = NavyDeep,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Master Balance Card (Banking Card Layout)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.5.dp,
                            Brush.linearGradient(
                                listOf(
                                    TurquoiseAccent.copy(alpha = 0.8f),
                                    GoldAccent.copy(alpha = 0.7f),
                                    NavyBorder
                                )
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .testTag("dashboard_balance_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = NavySurface
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        NavySurface,
                                        NavyCardElevated.copy(alpha = 0.95f)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            // Top row: Brand with logo badge & Privacy Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                    ) {
                                        androidx.compose.foundation.Image(
                                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.zava_official_logo),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ZAVA BANK REC",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TurquoiseAccent,
                                        letterSpacing = 1.sp
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        isPrivacy = !isPrivacy
                                        viewModel.togglePrivacy(isPrivacy)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPrivacy) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Privasi Saldo",
                                        tint = SilverLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Main Total Balance
                            Text(
                                text = "Total Seluruh Saldo",
                                style = MaterialTheme.typography.labelSmall,
                                color = SilverMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = FormatUtil.formatRupiah(totalBalance, currencyFormat, isPrivacy),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = SilverWhite
                            )

                            Spacer(modifier = Modifier.height(18.dp))
                            Divider(color = NavyBorder.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(14.dp))

                            // 3-Column Stats: Pemasukan, Pengeluaran, Saldo Tersisa Bulan Ini
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Masuk Bln Ini",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SilverMedium
                                    )
                                    Text(
                                        text = FormatUtil.formatRupiah(thisMonthIncome, currencyFormat, isPrivacy),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Keluar Bln Ini",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SilverMedium
                                    )
                                    Text(
                                        text = FormatUtil.formatRupiah(thisMonthExpense, currencyFormat, isPrivacy),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = DangerRed
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Saldo Tersisa",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SilverMedium
                                    )
                                    Text(
                                        text = FormatUtil.formatRupiah(remainingBalance, currencyFormat, isPrivacy),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (remainingBalance >= 0) GoldAccent else DangerRed
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Quick Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DashboardActionButton(
                        icon = Icons.Default.AddCircle,
                        label = "Pemasukan",
                        color = SuccessGreen,
                        onClick = { onOpenAddTransaction(true) }
                    )
                    DashboardActionButton(
                        icon = Icons.Default.RemoveCircle,
                        label = "Pengeluaran",
                        color = DangerRed,
                        onClick = { onOpenAddTransaction(false) }
                    )
                    DashboardActionButton(
                        icon = Icons.Default.Savings,
                        label = "Tabungan",
                        color = GoldAccent,
                        onClick = { onOpenAddPocket() }
                    )
                    DashboardActionButton(
                        icon = Icons.Default.Star,
                        label = "Wishlist",
                        color = PurpleLuxuryAccent,
                        onClick = { onNavigateToWishlist() }
                    )
                    DashboardActionButton(
                        icon = Icons.Default.Flag,
                        label = "Target",
                        color = InfoBlue,
                        onClick = { onNavigateToTarget() }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Tabungan Buat Nikah Highlight (Khusus Pernikahan)
            item {
                if (weddingPocket != null) {
                    val targetAmt = weddingPocket.targetAmount
                    val currentAmt = weddingPocket.currentBalance
                    val shortage = maxOf(0L, targetAmt - currentAmt)
                    val progressFraction = if (targetAmt > 0) (currentAmt.toFloat() / targetAmt.toFloat()).coerceIn(0f, 1f) else 0f
                    val monthlyRec = FormatUtil.calculateMonthlySavings(targetAmt, currentAmt, weddingPocket.targetDate)
                    val daysRemaining = FormatUtil.getDaysRemaining(weddingPocket.targetDate)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.5.dp,
                                Brush.linearGradient(listOf(GoldAccent, Color(0xFFE879F9))),
                                RoundedCornerShape(18.dp)
                            )
                            .clickable { onNavigateToTabungan() },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column {
                            // Romantic Banner Header with generated wedding rings asset
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            ) {
                                AsyncImage(
                                    model = com.example.R.drawable.img_wedding_savings,
                                    contentDescription = "Tabungan Pernikahan",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color.Transparent,
                                                    NavyDeep.copy(alpha = 0.9f)
                                                )
                                            )
                                        )
                                )
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Favorite,
                                            contentDescription = null,
                                            tint = Color(0xFFF472B6),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "TABUNGAN BUAT NIKAH",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = GoldLight,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                    Text(
                                        text = weddingPocket.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SilverWhite
                                    )
                                }
                            }

                            // Info and calculation
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Terkumpul",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = FormatUtil.formatRupiah(currentAmt, currencyFormat, isPrivacy),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldAccent
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Target Total",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = FormatUtil.formatRupiah(targetAmt, currencyFormat, isPrivacy),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp)),
                                    color = GoldAccent,
                                    trackColor = Color.Gray.copy(alpha = 0.2f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Kekurangan: ${FormatUtil.formatRupiah(shortage, currencyFormat, isPrivacy)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DangerRed
                                    )
                                    Text(
                                        text = "${(progressFraction * 100).toInt()}% Tercapai",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(GoldAccent.copy(alpha = 0.12f))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Calculate,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Rekomendasi Tabungan:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${FormatUtil.formatRupiah(monthlyRec, currencyFormat, isPrivacy)} / bulan ($daysRemaining hari tersisa)",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Monthly Flow Chart
            item {
                SimpleBarChart(
                    income = thisMonthIncome,
                    expense = thisMonthExpense,
                    currencyFormat = currencyFormat
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Recent Transactions Section
            item {
                SectionHeader(
                    title = "Transaksi Terakhir",
                    actionText = "Lihat Semua",
                    onActionClick = { onNavigateToTransaksi("ALL") }
                )
            }

            if (transactions.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.ReceiptLong,
                        title = "Belum Ada Transaksi",
                        subtitle = "Catat pemasukan dan pengeluaranmu sekarang untuk mengelola keuangan dengan rapi."
                    )
                }
            } else {
                items(transactions.take(5)) { tx ->
                    TransactionItemRow(
                        transaction = tx,
                        currencyFormat = currencyFormat,
                        isPrivacy = isPrivacy,
                        onClick = { onNavigateToTransaksi(tx.type) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun DashboardActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.15f))
                .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun TransactionItemRow(
    transaction: TransactionEntity,
    currencyFormat: String,
    isPrivacy: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == "INCOME"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryIcon(
            category = transaction.category,
            isIncome = isIncome
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.category,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${FormatUtil.formatDate(transaction.date)} • ${transaction.sourceOrMethod}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            if (transaction.note.isNotBlank()) {
                Text(
                    text = transaction.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        val amountPrefix = if (isIncome) "+" else "-"
        val amountColor = if (isIncome) SuccessGreen else DangerRed
        Text(
            text = if (isPrivacy) "Rp ••••••••" else "$amountPrefix${FormatUtil.formatRupiah(transaction.amount, currencyFormat)}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}
