package com.example.ui.transaksi

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
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
import com.example.ui.theme.*

@Composable
fun AddTransactionDialog(
    initialIsIncome: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (
        type: String,
        amount: Long,
        category: String,
        date: Long,
        sourceOrMethod: String,
        note: String,
        attachmentUri: String?
    ) -> Unit
) {
    var isIncome by remember { mutableStateOf(initialIsIncome) }

    val incomeCategories = listOf("Gaji", "Bonus", "Bisnis", "Freelance", "Hadiah", "Lainnya")
    val expenseCategories = listOf(
        "Makanan", "Transportasi", "Belanja", "Kebutuhan pribadi",
        "Tagihan", "Pendidikan", "Hiburan", "Kesehatan", "Pernikahan", "Lainnya"
    )

    val paymentMethods = listOf("Transfer Bank", "Tunai", "Kartu Debit", "E-Wallet", "QRIS", "Lainnya")
    val incomeSources = listOf("Transfer Bank", "Tunai", "Gaji Perusahaan", "Klien", "Lainnya")

    var selectedCategory by remember {
        mutableStateOf(if (isIncome) incomeCategories.first() else expenseCategories.first())
    }
    var selectedSourceOrMethod by remember {
        mutableStateOf(if (isIncome) incomeSources.first() else paymentMethods.first())
    }

    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isIncome) "Catat Pemasukan" else "Catat Pengeluaran",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isIncome) SuccessGreen else DangerRed
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Type Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isIncome) SuccessGreen else Color.Transparent)
                            .clickable {
                                isIncome = true
                                selectedCategory = incomeCategories.first()
                                selectedSourceOrMethod = incomeSources.first()
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pemasukan (+)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isIncome) DangerRed else Color.Transparent)
                            .clickable {
                                isIncome = false
                                selectedCategory = expenseCategories.first()
                                selectedSourceOrMethod = paymentMethods.first()
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pengeluaran (-)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (!isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    label = { Text("Nominal (Rp)") },
                    placeholder = { Text("Contoh: 1500000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_tx_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category chips
                Text(
                    text = "Kategori:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                val categories = if (isIncome) incomeCategories else expenseCategories
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (isIncome) SuccessGreen else DangerRed,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Source / Payment Method chips
                Text(
                    text = if (isIncome) "Sumber Pemasukan:" else "Metode Pembayaran:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                val methods = if (isIncome) incomeSources else paymentMethods
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(methods) { method ->
                        FilterChip(
                            selected = selectedSourceOrMethod == method,
                            onClick = { selectedSourceOrMethod = method },
                            label = { Text(method, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan (Opsional)") },
                    placeholder = { Text("Contoh: Bonus proyek Q3") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Photo attachment (Android Photo Picker)
                Text(
                    text = "Lampiran Struk/Bukti (Opsional):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (photoUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Lampiran",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { photoUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(28.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Hapus", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pilih Foto dari Galeri")
                    }
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
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
                    onConfirm(
                        if (isIncome) "INCOME" else "EXPENSE",
                        amount,
                        selectedCategory,
                        System.currentTimeMillis(),
                        selectedSourceOrMethod,
                        note,
                        photoUri
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isIncome) SuccessGreen else DangerRed,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("add_tx_confirm_btn")
            ) {
                Text(if (isIncome) "Simpan Pemasukan" else "Simpan Pengeluaran")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
