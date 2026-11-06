package com.example.ui.auth

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserAccount
import com.example.ui.common.ZavaButton
import com.example.ui.common.ZavaCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.ZavaViewModel

@Composable
fun AuthScreen(
    viewModel: ZavaViewModel,
    modifier: Modifier = Modifier
) {
    var isRegisterTab by remember { mutableStateOf(false) }
    val allUsers by viewModel.allUsers.collectAsState()

    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedPhotoUri by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri.toString()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(horizontal = 20.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            // App Branding Header with Official Logo
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(
                            2.dp,
                            Brush.linearGradient(listOf(TurquoiseAccent, GoldAccent)),
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.zava_official_logo),
                        contentDescription = "Logo Resmi ZAVA BANK REC",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "ZAVA BANK REC",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = SilverWhite
                )
                Text(
                    text = "Atur Keuanganmu, Wujudkan Impianmu",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TurquoiseAccent
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Tab selector (Masuk / Buat Akun)
            item {
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
                            .background(if (!isRegisterTab) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable {
                                isRegisterTab = false
                                errorMessage = null
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Masuk Akun",
                            fontWeight = FontWeight.Bold,
                            color = if (!isRegisterTab) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isRegisterTab) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable {
                                isRegisterTab = true
                                errorMessage = null
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Daftar Akun Baru",
                            fontWeight = FontWeight.Bold,
                            color = if (isRegisterTab) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Error display
            if (errorMessage != null) {
                item {
                    ZavaCard(
                        backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                        borderColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Form Content
            item {
                ZavaCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isRegisterTab) {
                        // Profile photo picker in Registration
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(2.dp, GoldAccent, CircleShape)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedPhotoUri != null) {
                                    AsyncImage(
                                        model = selectedPhotoUri,
                                        contentDescription = "Foto Profil",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = "Pilih Foto",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Foto Profil (Opsional)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Full name input
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Nama Lengkap") },
                            placeholder = { Text("Contoh: Rizky Pratama") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_fullname_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Username input
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it.filter { char -> char.isLetterOrDigit() || char == '_' } },
                        label = { Text("Username") },
                        placeholder = { Text("huruf kecil / angka") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_username_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Password / PIN input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(if (isRegisterTab) "Password / PIN (min 4 digit)" else "Password / PIN") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Lihat Password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )

                    // Confirm Password (in Register tab)
                    if (isRegisterTab) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Konfirmasi Password / PIN") },
                            leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_confirm_password_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Button
                    ZavaButton(
                        text = if (isRegisterTab) "Daftar Akun Baru" else "Masuk ke ZAVA BANK",
                        onClick = {
                            if (isRegisterTab) {
                                if (fullName.isBlank()) {
                                    errorMessage = "Nama lengkap wajib diisi"
                                    return@ZavaButton
                                }
                                if (username.isBlank()) {
                                    errorMessage = "Username tidak boleh kosong"
                                    return@ZavaButton
                                }
                                if (password != confirmPassword) {
                                    errorMessage = "Konfirmasi password tidak cocok"
                                    return@ZavaButton
                                }
                                viewModel.register(username, fullName, password, selectedPhotoUri) { success, err ->
                                    if (!success) errorMessage = err
                                }
                            } else {
                                if (username.isBlank() || password.isBlank()) {
                                    errorMessage = "Username dan password wajib diisi"
                                    return@ZavaButton
                                }
                                viewModel.login(username, password) { success, err ->
                                    if (!success) errorMessage = err
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isGold = true,
                        icon = if (isRegisterTab) Icons.Default.PersonAdd else Icons.Default.Login,
                        testTag = "auth_submit_button"
                    )
                }
            }

            // Existing accounts quick switch on device
            if (!isRegisterTab && allUsers.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Akun Tersimpan di Perangkat Ini",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        allUsers.forEach { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        username = user.username
                                        errorMessage = null
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(GoldAccent.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (user.profilePhotoUri != null) {
                                        AsyncImage(
                                            model = user.profilePhotoUri,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = user.fullName.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = GoldAccent
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.fullName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "@${user.username}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.Default.ArrowForwardIos,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Security Disclaimer Banner (Mandatory requirement from brief!)
            item {
                Spacer(modifier = Modifier.height(28.dp))
                ZavaCard(
                    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Keamanan Lokal Mandiri",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Autentikasi dan data keuangan tersimpan aman secara offline pada perangkat ini menggunakan enkripsi hash lokal tanpa server/backend pihak ketiga. Gunakan fitur Backup di menu Profil agar data tetap aman saat ganti perangkat.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
