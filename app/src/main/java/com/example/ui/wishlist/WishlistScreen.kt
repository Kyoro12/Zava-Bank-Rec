package com.example.ui.wishlist

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import com.example.data.model.FinancialTarget
import com.example.data.model.WishlistItem
import com.example.data.model.UserAccount
import com.example.ui.common.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ZavaViewModel

@Composable
fun WishlistScreen(
    viewModel: ZavaViewModel,
    user: UserAccount,
    initialSubTab: String = "WISHLIST", // "WISHLIST" or "TARGET"
    modifier: Modifier = Modifier
) {
    val wishlists by viewModel.wishlists.collectAsState()
    val targets by viewModel.targets.collectAsState()

    var activeSubTab by remember { mutableStateOf(initialSubTab) }
    var showAddWishlistDialog by remember { mutableStateOf(false) }
    var showAddTargetDialog by remember { mutableStateOf(false) }

    var addFundsWishlistItem by remember { mutableStateOf<WishlistItem?>(null) }
    var addFundsTargetItem by remember { mutableStateOf<FinancialTarget?>(null) }
    var itemToDelete by remember { mutableStateOf<Any?>(null) }

    val currencyFormat = user.currencyFormat

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (activeSubTab == "WISHLIST") showAddWishlistDialog = true
                    else showAddTargetDialog = true
                },
                containerColor = GoldAccent,
                contentColor = NavyDeep,
                modifier = Modifier
                    .padding(bottom = 68.dp)
                    .testTag("add_wishlist_or_target_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
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
                text = "Impian & Target Finansial",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Wujudkan rencana belanja impian dan tujuan finansial",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-Tab Switcher: Wishlist vs Target
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeSubTab == "WISHLIST") MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeSubTab = "WISHLIST" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = if (activeSubTab == "WISHLIST") MaterialTheme.colorScheme.onPrimary else GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Wishlist Impian",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (activeSubTab == "WISHLIST") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeSubTab == "TARGET") MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeSubTab = "TARGET" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = null,
                            tint = if (activeSubTab == "TARGET") MaterialTheme.colorScheme.onPrimary else InfoBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Target Keuangan",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (activeSubTab == "TARGET") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (activeSubTab == "WISHLIST") {
                    if (wishlists.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.CardGiftcard,
                                title = "Belum Ada Wishlist",
                                subtitle = "Catat barang atau impian yang ingin kamu beli beserta target menabungnya."
                            )
                        }
                    } else {
                        items(wishlists) { item ->
                            WishlistItemCard(
                                item = item,
                                currencyFormat = currencyFormat,
                                onAddFunds = { addFundsWishlistItem = item },
                                onMarkPurchased = {
                                    viewModel.updateWishlist(item.copy(status = "SUDAH_TERBELI", savedAmount = item.estimatedPrice))
                                },
                                onDelete = { itemToDelete = item }
                            )
                        }
                    }
                } else {
                    if (targets.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.Flag,
                                title = "Belum Ada Target Finansial",
                                subtitle = "Tentukan target menikah, beli kendaraan, rumah, atau dana darurat dengan rekomendasi menabung bulanan."
                            )
                        }
                    } else {
                        items(targets) { target ->
                            FinancialTargetCard(
                                target = target,
                                currencyFormat = currencyFormat,
                                onAddFunds = { addFundsTargetItem = target },
                                onDelete = { itemToDelete = target }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Wishlist Dialog
    if (showAddWishlistDialog) {
        AddWishlistDialog(
            onDismiss = { showAddWishlistDialog = false },
            onConfirm = { name, price, priority, category, note, photoUri ->
                viewModel.createWishlist(
                    name = name,
                    estimatedPrice = price,
                    priority = priority,
                    category = category,
                    note = note,
                    targetDate = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000),
                    photoUri = photoUri
                )
                showAddWishlistDialog = false
            }
        )
    }

    // Add Target Dialog
    if (showAddTargetDialog) {
        AddTargetDialog(
            onDismiss = { showAddTargetDialog = false },
            onConfirm = { title, category, targetAmount, collectedAmount, months, note ->
                val now = System.currentTimeMillis()
                val deadline = now + (months.toLong() * 30L * 24 * 60 * 60 * 1000)
                viewModel.createTarget(
                    title = title,
                    category = category,
                    targetAmount = targetAmount,
                    collectedAmount = collectedAmount,
                    startDate = now,
                    deadlineDate = deadline,
                    note = note
                )
                showAddTargetDialog = false
            }
        )
    }

    // Add Funds to Wishlist Dialog
    if (addFundsWishlistItem != null) {
        val item = addFundsWishlistItem!!
        AddFundsDialog(
            title = "Nabung untuk ${item.name}",
            onDismiss = { addFundsWishlistItem = null },
            onConfirm = { amount ->
                viewModel.addWishlistFunds(item, amount)
                addFundsWishlistItem = null
            }
        )
    }

    // Add Funds to Target Dialog
    if (addFundsTargetItem != null) {
        val target = addFundsTargetItem!!
        AddFundsDialog(
            title = "Nabung untuk ${target.title}",
            onDismiss = { addFundsTargetItem = null },
            onConfirm = { amount ->
                viewModel.addTargetFunds(target, amount)
                addFundsTargetItem = null
            }
        )
    }

    // Delete Confirmation
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Hapus Item?") },
            text = { Text("Apakah Anda yakin ingin menghapus data ini?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val item = itemToDelete
                        if (item is WishlistItem) viewModel.deleteWishlist(item)
                        if (item is FinancialTarget) viewModel.deleteTarget(item)
                        itemToDelete = null
                    }
                ) {
                    Text("Hapus", color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun WishlistItemCard(
    item: WishlistItem,
    currencyFormat: String,
    onAddFunds: () -> Unit,
    onMarkPurchased: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (item.estimatedPrice > 0)
        (item.savedAmount.toFloat() / item.estimatedPrice.toFloat()).coerceIn(0f, 1f)
    else 0f
    val isPurchased = item.status == "SUDAH_TERBELI"

    ZavaCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = if (isPurchased) SuccessGreen.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo if available or default gift icon
            if (item.photoUri != null) {
                AsyncImage(
                    model = item.photoUri,
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    // Priority Badge
                    val priorityColor = when (item.priority) {
                        "TINGGI" -> DangerRed
                        "SEDANG" -> WarningOrange
                        else -> InfoBlue
                    }
                    Text(
                        text = item.priority,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = priorityColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(priorityColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.category} • Estimasi: ${FormatUtil.formatRupiah(item.estimatedPrice, currencyFormat)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.note.isNotBlank()) {
                    Text(
                        text = item.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (isPurchased) SuccessGreen else GoldAccent,
            trackColor = Color.Gray.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Terkumpul: ${FormatUtil.formatRupiah(item.savedAmount, currencyFormat)}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isPurchased) SuccessGreen else GoldAccent
            )
            Text(
                text = if (isPurchased) "Sudah Terbeli" else "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isPurchased) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isPurchased) {
                OutlinedButton(
                    onClick = onAddFunds,
                    modifier = Modifier.padding(end = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Dana", style = MaterialTheme.typography.labelSmall)
                }
                Button(
                    onClick = onMarkPurchased,
                    modifier = Modifier.padding(end = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Beli", style = MaterialTheme.typography.labelSmall)
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun FinancialTargetCard(
    target: FinancialTarget,
    currencyFormat: String,
    onAddFunds: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shortage = maxOf(0L, target.targetAmount - target.collectedAmount)
    val progress = if (target.targetAmount > 0)
        (target.collectedAmount.toFloat() / target.targetAmount.toFloat()).coerceIn(0f, 1f)
    else 0f
    val monthlyRec = FormatUtil.calculateMonthlySavings(target.targetAmount, target.collectedAmount, target.deadlineDate)
    val daysRemaining = FormatUtil.getDaysRemaining(target.deadlineDate)

    ZavaCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(InfoBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = InfoBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = target.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Deadline: ${FormatUtil.formatDate(target.deadlineDate)} ($daysRemaining hari lagi)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Terkumpul", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = FormatUtil.formatRupiah(target.collectedAmount, currencyFormat),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = InfoBlue
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Target Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = FormatUtil.formatRupiah(target.targetAmount, currencyFormat),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = InfoBlue,
            trackColor = Color.Gray.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Sisa: ${FormatUtil.formatRupiah(shortage, currencyFormat)}",
                style = MaterialTheme.typography.labelSmall,
                color = if (shortage > 0) DangerRed else SuccessGreen
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (shortage > 0) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(InfoBlue.copy(alpha = 0.1f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = InfoBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Rekomendasi menabung: ${FormatUtil.formatRupiah(monthlyRec, currencyFormat)} / bulan",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = InfoBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = onAddFunds,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tambah Dana", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun AddWishlistDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Long, priority: String, category: String, note: String, photoUri: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("SEDANG") }
    var category by remember { mutableStateOf("Elektronik") }
    var note by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) photoUri = uri.toString() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Wishlist", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Barang / Impian") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                    label = { Text("Estimasi Harga (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Priority
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("TINGGI", "SEDANG", "RENDAH").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (photoUri != null) "Foto Terpilih ✓" else "Pilih Foto Barang")
                }
                if (error != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(error!!, color = DangerRed, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "Nama barang tidak boleh kosong"
                        return@Button
                    }
                    val price = priceText.toLongOrNull() ?: 0L
                    if (price <= 0) {
                        error = "Estimasi harga harus lebih dari 0"
                        return@Button
                    }
                    onConfirm(name, price, priority, category, note, photoUri)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDeep)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun AddTargetDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, targetAmount: Long, collectedAmount: Long, months: Int, note: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var collectedText by remember { mutableStateOf("") }
    var months by remember { mutableStateOf(12) }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Target Finansial", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Target") },
                    placeholder = { Text("Contoh: DP Rumah, Beli Laptop") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it.filter { c -> c.isDigit() } },
                    label = { Text("Nominal Target (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = collectedText,
                    onValueChange = { collectedText = it.filter { c -> c.isDigit() } },
                    label = { Text("Dana Terkumpul Saat Ini (Opsional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Jangka Waktu: $months Bulan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = months.toFloat(),
                    onValueChange = { months = it.toInt() },
                    valueRange = 1f..60f,
                    steps = 59
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(error!!, color = DangerRed, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        error = "Nama target tidak boleh kosong"
                        return@Button
                    }
                    val target = targetText.toLongOrNull() ?: 0L
                    if (target <= 0) {
                        error = "Nominal target harus lebih dari 0"
                        return@Button
                    }
                    val collected = collectedText.toLongOrNull() ?: 0L
                    onConfirm(title, "General", target, collected, months, note)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDeep)
            ) {
                Text("Simpan Target")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun AddFundsDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (amount: Long) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    label = { Text("Nominal Tambahan (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(error!!, color = DangerRed, style = MaterialTheme.typography.labelSmall)
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
                    onConfirm(amount)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDeep)
            ) {
                Text("Tambah Dana")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
