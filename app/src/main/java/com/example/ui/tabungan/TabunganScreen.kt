package com.example.ui.tabungan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SavingsPocket
import com.example.data.model.UserAccount
import com.example.ui.common.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ZavaViewModel

@Composable
fun TabunganScreen(
    viewModel: ZavaViewModel,
    user: UserAccount,
    onOpenAddPocket: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pockets by viewModel.pockets.collectAsState()
    var selectedTab by remember { mutableStateOf("NIKAH") } // "NIKAH" or "UMUM"

    var activeDepositPocket by remember { mutableStateOf<SavingsPocket?>(null) }
    var activeWithdrawPocket by remember { mutableStateOf<SavingsPocket?>(null) }
    var activeEditPocket by remember { mutableStateOf<SavingsPocket?>(null) }
    var pocketToDelete by remember { mutableStateOf<SavingsPocket?>(null) }

    val weddingPockets = remember(pockets) { pockets.filter { it.category == "NIKAH" } }
    val generalPockets = remember(pockets) { pockets.filter { it.category == "UMUM" } }
    val displayedPockets = if (selectedTab == "NIKAH") weddingPockets else generalPockets

    val currencyFormat = user.currencyFormat

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddPocket,
                containerColor = GoldAccent,
                contentColor = NavyDeep,
                modifier = Modifier
                    .padding(bottom = 68.dp)
                    .testTag("add_savings_pocket_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Buat Tabungan Baru")
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
                text = "Kantong Tabungan",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Wujudkan impian pernikahan & masa depan finansialmu",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Segment: Tabungan Buat Nikah vs Tabungan Umum
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                // Tab Nikah
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == "NIKAH") Color(0xFFE5B80B) else Color.Transparent)
                        .clickable { selectedTab = "NIKAH" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = if (selectedTab == "NIKAH") NavyDeep else Color(0xFFF472B6),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tabungan Buat Nikah",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selectedTab == "NIKAH") NavyDeep else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tab Umum
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == "UMUM") MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedTab = "UMUM" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Savings,
                            contentDescription = null,
                            tint = if (selectedTab == "UMUM") MaterialTheme.colorScheme.onPrimary else InfoBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tabungan Umum",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selectedTab == "UMUM") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTab == "NIKAH") {
                    // Special Wedding Header Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    Color(0xFFE5B80B).copy(alpha = 0.5f),
                                    RoundedCornerShape(16.dp)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                            ) {
                                AsyncImage(
                                    model = com.example.R.drawable.img_wedding_savings,
                                    contentDescription = "Pernikahan Impian",
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
                                                    NavyDeep.copy(alpha = 0.88f)
                                                )
                                            )
                                        )
                                )
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        text = "Wujudkan Momen Sakral Pernikahan",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldLight
                                    )
                                    Text(
                                        text = "Kalkulator otomatis untuk target biaya, kekurangan dana, dan estimasi tabungan bulanan.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SilverLight
                                    )
                                }
                            }
                        }
                    }
                }

                if (displayedPockets.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = if (selectedTab == "NIKAH") Icons.Default.Favorite else Icons.Default.Savings,
                            title = if (selectedTab == "NIKAH") "Belum Ada Tabungan Nikah" else "Belum Ada Tabungan Umum",
                            subtitle = if (selectedTab == "NIKAH")
                                "Mulai rencanakan biaya pernikahanmu dengan membuat kantong tabungan baru."
                            else
                                "Buat kantong dana darurat, liburan, atau gadget impianmu."
                        )
                    }
                } else {
                    items(displayedPockets) { pocket ->
                        SavingsPocketCard(
                            pocket = pocket,
                            currencyFormat = currencyFormat,
                            onDeposit = { activeDepositPocket = pocket },
                            onWithdraw = { activeWithdrawPocket = pocket },
                            onEdit = { activeEditPocket = pocket },
                            onDelete = { pocketToDelete = pocket }
                        )
                    }
                }
            }
        }
    }

    // Deposit Dialog
    if (activeDepositPocket != null) {
        val pocket = activeDepositPocket!!
        MoneyActionDialog(
            title = "Setor Tabungan - ${pocket.name}",
            confirmButtonText = "Tambah Tabungan",
            onDismiss = { activeDepositPocket = null },
            onConfirm = { amount, note ->
                viewModel.depositSavings(pocket, amount, note)
                activeDepositPocket = null
            }
        )
    }

    // Withdraw Dialog
    if (activeWithdrawPocket != null) {
        val pocket = activeWithdrawPocket!!
        MoneyActionDialog(
            title = "Ambil Tabungan - ${pocket.name}",
            confirmButtonText = "Ambil Dana",
            maxAmount = pocket.currentBalance,
            onDismiss = { activeWithdrawPocket = null },
            onConfirm = { amount, note ->
                viewModel.withdrawSavings(pocket, amount, note)
                activeWithdrawPocket = null
            }
        )
    }

    // Edit Pocket Dialog
    if (activeEditPocket != null) {
        val pocket = activeEditPocket!!
        EditPocketDialog(
            pocket = pocket,
            onDismiss = { activeEditPocket = null },
            onConfirm = { updated ->
                viewModel.updatePocket(updated)
                activeEditPocket = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (pocketToDelete != null) {
        val pocket = pocketToDelete!!
        AlertDialog(
            onDismissRequest = { pocketToDelete = null },
            title = { Text("Hapus Tabungan?") },
            text = { Text("Apakah Anda yakin ingin menghapus kantong tabungan '${pocket.name}' beserta seluruh riwayatnya?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePocket(pocket)
                        pocketToDelete = null
                    }
                ) {
                    Text("Hapus", color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { pocketToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun SavingsPocketCard(
    pocket: SavingsPocket,
    currencyFormat: String,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isWedding = pocket.category == "NIKAH"
    val shortage = maxOf(0L, pocket.targetAmount - pocket.currentBalance)
    val progressFraction = if (pocket.targetAmount > 0)
        (pocket.currentBalance.toFloat() / pocket.targetAmount.toFloat()).coerceIn(0f, 1f)
    else 0f
    val monthlyRec = FormatUtil.calculateMonthlySavings(pocket.targetAmount, pocket.currentBalance, pocket.targetDate)
    val daysRemaining = FormatUtil.getDaysRemaining(pocket.targetDate)

    ZavaCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = if (isWedding) GoldAccent.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    ) {
        // Title & Actions row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isWedding) GoldAccent.copy(alpha = 0.2f) else InfoBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isWedding) Icons.Default.Favorite else Icons.Default.Savings,
                        contentDescription = null,
                        tint = if (isWedding) GoldAccent else InfoBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = pocket.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isWedding) "Target Pernikahan: ${FormatUtil.formatDate(pocket.targetDate)}" else "Target: ${FormatUtil.formatDate(pocket.targetDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Balance & Target Numbers
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
                    text = FormatUtil.formatRupiah(pocket.currentBalance, currencyFormat),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isWedding) GoldAccent else SuccessGreen
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Target Dana",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = FormatUtil.formatRupiah(pocket.targetAmount, currencyFormat),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Progress Bar
        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = if (isWedding) GoldAccent else InfoBlue,
            trackColor = Color.Gray.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Shortage & Percentage
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Kekurangan: ${FormatUtil.formatRupiah(shortage, currencyFormat)}",
                style = MaterialTheme.typography.labelSmall,
                color = if (shortage > 0) DangerRed else SuccessGreen
            )
            Text(
                text = "${(progressFraction * 100).toInt()}% Tercapai",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Monthly recommendation badge
        if (shortage > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isWedding) GoldAccent.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = if (isWedding) GoldAccent else InfoBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Perlu menabung: ${FormatUtil.formatRupiah(monthlyRec, currencyFormat)} / bulan ($daysRemaining hari tersisa)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isWedding) GoldAccent else InfoBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons: + Tambah (Setor) & - Ambil (Tarik)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ZavaButton(
                text = "+ Setor",
                onClick = onDeposit,
                modifier = Modifier.weight(1f),
                isGold = isWedding
            )
            ZavaOutlinedButton(
                text = "- Tarik",
                onClick = onWithdraw,
                modifier = Modifier.weight(1f),
                borderColor = if (isWedding) GoldAccent else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun MoneyActionDialog(
    title: String,
    confirmButtonText: String,
    maxAmount: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (amount: Long, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                if (maxAmount != null) {
                    Text(
                        text = "Saldo tersedia: Rp$maxAmount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    label = { Text("Nominal (Rp)") },
                    placeholder = { Text("Contoh: 500000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan (Opsional)") },
                    placeholder = { Text("Contoh: Gaji sampingan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = error!!, color = DangerRed, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (amount <= 0) {
                        error = "Nominal harus lebih dari 0"
                        return@Button
                    }
                    if (maxAmount != null && amount > maxAmount) {
                        error = "Nominal melebihi saldo tersedia"
                        return@Button
                    }
                    onConfirm(amount, note)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDeep)
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun EditPocketDialog(
    pocket: SavingsPocket,
    onDismiss: () -> Unit,
    onConfirm: (SavingsPocket) -> Unit
) {
    var name by remember { mutableStateOf(pocket.name) }
    var targetText by remember { mutableStateOf(pocket.targetAmount.toString()) }
    var note by remember { mutableStateOf(pocket.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Tabungan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Tabungan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it.filter { c -> c.isDigit() } },
                    label = { Text("Target Nominal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    val target = targetText.toLongOrNull() ?: pocket.targetAmount
                    onConfirm(pocket.copy(name = name, targetAmount = target, note = note))
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
