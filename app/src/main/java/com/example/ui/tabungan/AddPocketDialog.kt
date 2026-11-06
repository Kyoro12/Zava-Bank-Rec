package com.example.ui.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDeep

@Composable
fun AddPocketDialog(
    initialCategory: String = "NIKAH",
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        category: String,
        targetAmount: Long,
        initialBalance: Long,
        startDate: Long,
        targetDate: Long,
        note: String,
        colorHex: String
    ) -> Unit
) {
    var category by remember { mutableStateOf(initialCategory) }
    var name by remember {
        mutableStateOf(if (initialCategory == "NIKAH") "Pernikahan Impian" else "")
    }
    var targetText by remember { mutableStateOf("") }
    var initialText by remember { mutableStateOf("") }
    var targetMonths by remember { mutableStateOf(12) } // default 12 months
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Buat Kantong Tabungan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // Category Picker
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
                            .background(if (category == "NIKAH") GoldAccent else Color.Transparent)
                            .clickable {
                                category = "NIKAH"
                                if (name.isBlank() || name == "Tabungan Baru") name = "Pernikahan Impian"
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = if (category == "NIKAH") NavyDeep else Color(0xFFF472B6),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Buat Nikah",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (category == "NIKAH") NavyDeep else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (category == "UMUM") MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable {
                                category = "UMUM"
                                if (name == "Pernikahan Impian") name = "Dana Darurat"
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Savings,
                                contentDescription = null,
                                tint = if (category == "UMUM") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Umum",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (category == "UMUM") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Tabungan") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_pocket_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it.filter { c -> c.isDigit() } },
                    label = { Text("Target Nominal (Rp)") },
                    placeholder = { Text(if (category == "NIKAH") "Contoh: 50000000" else "Contoh: 15000000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_pocket_target_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = initialText,
                    onValueChange = { initialText = it.filter { c -> c.isDigit() } },
                    label = { Text("Saldo Awal (Opsional)") },
                    placeholder = { Text("Contoh: 1000000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Target duration in months
                Text(
                    text = "Target Selesai: $targetMonths Bulan ke depan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = targetMonths.toFloat(),
                    onValueChange = { targetMonths = it.toInt() },
                    valueRange = 1f..60f,
                    steps = 59,
                    colors = SliderDefaults.colors(
                        thumbColor = if (category == "NIKAH") GoldAccent else MaterialTheme.colorScheme.primary,
                        activeTrackColor = if (category == "NIKAH") GoldAccent else MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan (Opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error!!,
                        color = DangerRed,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "Nama tabungan tidak boleh kosong"
                        return@Button
                    }
                    val target = targetText.toLongOrNull() ?: 0L
                    if (target <= 0) {
                        error = "Target nominal harus lebih dari 0"
                        return@Button
                    }
                    val initial = initialText.toLongOrNull() ?: 0L
                    val now = System.currentTimeMillis()
                    val targetDate = now + (targetMonths.toLong() * 30L * 24 * 60 * 60 * 1000)
                    val color = if (category == "NIKAH") "#E5B80B" else "#3B82F6"

                    onConfirm(name, category, target, initial, now, targetDate, note, color)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (category == "NIKAH") GoldAccent else MaterialTheme.colorScheme.primary,
                    contentColor = if (category == "NIKAH") NavyDeep else MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("add_pocket_confirm_btn")
            ) {
                Text("Simpan Tabungan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
