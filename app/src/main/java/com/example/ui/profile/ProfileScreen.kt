package com.example.ui.profile

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserAccount
import com.example.ui.common.ZavaButton
import com.example.ui.common.ZavaCard
import com.example.ui.common.ZavaOutlinedButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.ZavaViewModel
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun ProfileScreen(
    viewModel: ZavaViewModel,
    user: UserAccount,
    onLogout: () -> Unit,
    onNavigateToLaporan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allUsers by viewModel.allUsers.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showSwitchAccountDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf<String?>(null) }
    var restoreFeedbackMessage by remember { mutableStateOf<String?>(null) }

    // Android Photo Picker for Profile Photo
    val profilePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateProfile(user.fullName, uri.toString())
        }
    }

    // Android Photo Picker for Wallpaper
    val wallpaperPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setWallpaper(uri.toString())
        }
    }

    // File picker for restoring JSON backup
    val restoreFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = reader.readText()
                reader.close()
                showRestoreConfirmDialog = content
            } catch (e: Exception) {
                viewModel.showMessage("Gagal membaca file: ${e.message}")
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header Card
        item {
            ZavaCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = GoldAccent.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(GoldAccent, Color(0xFFC59419)))
                            )
                            .border(2.dp, GoldAccent, CircleShape)
                            .clickable {
                                profilePhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.profilePhotoUri != null) {
                            AsyncImage(
                                model = user.profilePhotoUri,
                                contentDescription = "Foto Profil",
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
                                fontSize = 28.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.fullName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "@${user.username} • Rekening Lokal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ketuk foto untuk ganti dari Galeri",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = { showEditProfileDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Nama", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Laporan Keuangan Shortcut
        item {
            ZavaCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToLaporan() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SuccessGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = SuccessGreen)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Laporan & Statistik Keuangan",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Grafik analitik & ekspor laporan ke CSV",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Personalisasi Tampilan & Tema
        item {
            ZavaCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Personalisasi Tampilan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Wallpaper settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Wallpaper Dashboard",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (user.wallpaperUri != null) "Wallpaper galeri aktif" else "Menggunakan background default",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row {
                        OutlinedButton(
                            onClick = {
                                wallpaperPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pilih Galeri", style = MaterialTheme.typography.labelSmall)
                        }
                        if (user.wallpaperUri != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(onClick = { viewModel.setWallpaper(null) }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus Wallpaper", tint = DangerRed)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(14.dp))

                // Pilihan Tema (5 tema)
                Text(
                    text = "Pilihan Tema:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                val themes = listOf(
                    "DARK_PREMIUM" to "Dark Premium",
                    "LIGHT_MINIMALIST" to "Light Minimalist",
                    "PURPLE_LUXURY" to "Purple Luxury",
                    "NAVY_GOLD" to "Navy Gold",
                    "CUSTOM" to "Custom Accent"
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(themes) { (id, label) ->
                        FilterChip(
                            selected = user.activeTheme == id,
                            onClick = { viewModel.setTheme(id) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = NavyDeep
                            )
                        )
                    }
                }

                // If custom theme selected, show color picker swatches
                if (user.activeTheme == "CUSTOM") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Pilih Aksen Kustom:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val colorSwatches = listOf("#F59E0B", "#10B981", "#3B82F6", "#8B5CF6", "#EC4899", "#06B6D4")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        colorSwatches.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (user.customAccentHex == hex) 3.dp else 1.dp,
                                        color = if (user.customAccentHex == hex) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.setCustomAccent(hex) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(14.dp))

                // Ukuran Teks (Normal vs Besar)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Ukuran Teks Besar", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(text = "Meningkatkan ukuran font untuk kenyamanan membaca", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = user.isLargeFont,
                        onCheckedChange = { viewModel.setFontSize(it) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(14.dp))

                // Format Mata Uang
                Text(
                    text = "Format Tampilan Mata Uang:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "RP_NOSPACE" to "Rp1.500.000",
                        "RP_SPACE" to "Rp 1.500.000",
                        "IDR" to "IDR 1.500.000"
                    ).forEach { (formatKey, label) ->
                        FilterChip(
                            selected = user.currencyFormat == formatKey,
                            onClick = { viewModel.setCurrencyFormat(formatKey) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }

        // Keamanan & Pengaturan Akun
        item {
            ZavaCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Keamanan & Akun",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Privacy Saldo Default
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Sembunyikan Saldo", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(text = "Sembunyikan nominal saldo secara default", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = user.isPrivacyEnabled,
                        onCheckedChange = { viewModel.togglePrivacy(it) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                // Ganti PIN / Password
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showChangePinDialog = true }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Ganti PIN / Password", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                // Switch Akun
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSwitchAccountDialog = true }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SwitchAccount, contentDescription = null, tint = InfoBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Beralih / Tambah Akun Lain", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Text(text = "${allUsers.size} Akun", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Backup & Restore Section (Crucial requirement from prompt)
        item {
            ZavaCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Cadangan Data (Backup & Restore)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Karena seluruh data tersimpan secara lokal dan offline pada HP ini, gunakan pencadangan berkala agar data tidak hilang saat membersihkan memori atau ganti perangkat.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Backup JSON button
                    Button(
                        onClick = {
                            viewModel.exportBackup { jsonStr ->
                                if (jsonStr != null) {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, jsonStr)
                                        putExtra(Intent.EXTRA_TITLE, "zava_bank_backup_${user.username}.json")
                                        type = "application/json"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Simpan / Bagikan Backup JSON")
                                    context.startActivity(shareIntent)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDeep),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Backup JSON", style = MaterialTheme.typography.labelSmall)
                    }

                    // Restore JSON button
                    OutlinedButton(
                        onClick = { restoreFilePicker.launch("application/json") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore JSON", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Export CSV button
                OutlinedButton(
                    onClick = {
                        viewModel.exportCsv { csvStr ->
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, csvStr)
                                putExtra(Intent.EXTRA_TITLE, "transaksi_${user.username}.csv")
                                type = "text/csv"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Ekspor Transaksi ke CSV")
                            context.startActivity(shareIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ekspor Transaksi ke File CSV", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Logout and Delete Account
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Keluar (Logout)")
                }

                TextButton(
                    onClick = { showDeleteAccountDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hapus Akun Ini dari Perangkat", color = DangerRed)
                }
            }
        }

        // Official ZAVA Brand Identity Card
        item {
            ZavaCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = TurquoiseAccent.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .border(
                                2.dp,
                                Brush.linearGradient(listOf(TurquoiseAccent, GoldAccent)),
                                RoundedCornerShape(18.dp)
                            )
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.zava_official_logo),
                            contentDescription = "Logo Resmi ZAVA BANK REC",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ZAVA BANK REC",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = SilverWhite
                    )
                    Text(
                        text = "Atur Keuanganmu, Wujudkan Impianmu",
                        style = MaterialTheme.typography.bodySmall,
                        color = TurquoiseAccent
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Developed by ZAVA STUDIO",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Versi aplikasi 1.0.0 (Offline-First)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SilverMedium
                    )
                }
            }
        }
    }

    // Edit Name Dialog
    if (showEditProfileDialog) {
        var newFullName by remember { mutableStateOf(user.fullName) }
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Nama Lengkap", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newFullName,
                    onValueChange = { newFullName = it },
                    label = { Text("Nama Lengkap") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFullName.isNotBlank()) {
                            viewModel.updateProfile(newFullName, user.profilePhotoUri)
                        }
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDeep)
                ) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) { Text("Batal") }
            }
        )
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
        var oldPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var confirmNewPass by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Ganti PIN / Password", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it },
                        label = { Text("PIN / Password Lama") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("PIN / Password Baru (min 4 digit)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmNewPass,
                        onValueChange = { confirmNewPass = it },
                        label = { Text("Konfirmasi Password Baru") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(error!!, color = DangerRed, style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPass != confirmNewPass) {
                            error = "Konfirmasi password baru tidak cocok"
                            return@Button
                        }
                        viewModel.changePassword(oldPass, newPass) { success, err ->
                            if (success) {
                                showChangePinDialog = false
                            } else {
                                error = err
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDeep)
                ) { Text("Ganti PIN") }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) { Text("Batal") }
            }
        )
    }

    // Switch Account Dialog
    if (showSwitchAccountDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchAccountDialog = false },
            title = { Text("Pilih Akun", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allUsers.forEach { account ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (account.id == user.id) GoldAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    viewModel.switchAccount(account)
                                    showSwitchAccountDialog = false
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = if (account.id == user.id) GoldAccent else MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(account.fullName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("@${account.username}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showSwitchAccountDialog = false
                    onLogout()
                }) {
                    Text("+ Buat Akun Baru")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSwitchAccountDialog = false }) { Text("Tutup") }
            }
        )
    }

    // Restore Confirmation Dialog
    if (showRestoreConfirmDialog != null) {
        val json = showRestoreConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = null },
            title = { Text("Pulihkan Cadangan Data?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Memulihkan data akan menggantikan data transaksi, tabungan, wishlist, dan target pada akun saat ini dengan data dari file backup JSON. Lanjutkan?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.restoreBackup(json) { success, msg ->
                            showRestoreConfirmDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDeep)
                ) { Text("Pulihkan Sekarang") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = null }) { Text("Batal") }
            }
        )
    }

    // Delete Account Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Hapus Akun '${user.fullName}'?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Tindakan ini permanen. Seluruh data keuangan, riwayat transaksi, dan tabungan pada akun ini akan dihapus dari perangkat.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCurrentAccount {
                            showDeleteAccountDialog = false
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) { Text("Hapus Permanen") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Batal") }
            }
        )
    }
}
