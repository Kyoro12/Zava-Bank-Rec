package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.auth.AuthScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.laporan.LaporanScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.tabungan.AddPocketDialog
import com.example.ui.tabungan.TabunganScreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.ZavaTheme
import com.example.ui.transaksi.AddTransactionDialog
import com.example.ui.transaksi.TransaksiScreen
import com.example.ui.viewmodel.ZavaViewModel
import com.example.ui.wishlist.WishlistScreen

import com.example.ui.splash.SplashScreen

enum class MainNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    TABUNGAN("Tabungan", Icons.Filled.Savings, Icons.Outlined.Savings),
    TRANSAKSI("Transaksi", Icons.Filled.Receipt, Icons.Outlined.Receipt),
    WISHLIST("Wishlist", Icons.Filled.Star, Icons.Outlined.StarOutline),
    PROFIL("Profil", Icons.Filled.Person, Icons.Outlined.Person)
}

class MainActivity : ComponentActivity() {

    private val viewModel: ZavaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var isSplashVisible by remember { mutableStateOf(true) }
            val currentAccount by viewModel.currentAccount.collectAsState()
            val uiMessage by viewModel.uiMessage.collectAsState()

            val activeTheme = currentAccount?.activeTheme ?: "DARK_PREMIUM"
            val customAccentHex = currentAccount?.customAccentHex ?: "#14F1D9"
            val isLargeFont = currentAccount?.isLargeFont ?: false

            ZavaTheme(
                themeName = activeTheme,
                customAccentHex = customAccentHex,
                isLargeFont = isLargeFont
            ) {
                if (isSplashVisible) {
                    SplashScreen(
                        onSplashFinished = { isSplashVisible = false }
                    )
                } else {
                    val snackbarHostState = remember { SnackbarHostState() }

                    LaunchedEffect(uiMessage) {
                        uiMessage?.let {
                            snackbarHostState.showSnackbar(it)
                            viewModel.clearUiMessage()
                        }
                    }

                    if (currentAccount == null) {
                        Scaffold(
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            modifier = Modifier.fillMaxSize()
                        ) { innerPadding ->
                            AuthScreen(
                                viewModel = viewModel,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }
                    } else {
                    val user = currentAccount!!
                    var currentTab by remember { mutableStateOf(MainNavTab.HOME) }
                    var initialTransactionType by remember { mutableStateOf("ALL") }
                    var isShowingLaporan by remember { mutableStateOf(false) }

                    var showAddTxDialog by remember { mutableStateOf(false) }
                    var addTxIsIncome by remember { mutableStateOf(true) }
                    var showAddPocketDialog by remember { mutableStateOf(false) }

                    // Hardware back button navigation handling
                    BackHandler(enabled = isShowingLaporan || currentTab != MainNavTab.HOME) {
                        if (isShowingLaporan) {
                            isShowingLaporan = false
                        } else if (currentTab != MainNavTab.HOME) {
                            currentTab = MainNavTab.HOME
                        }
                    }

                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            if (!isShowingLaporan) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 8.dp,
                                    modifier = Modifier
                                        .windowInsetsPadding(WindowInsets.navigationBars)
                                        .testTag("main_bottom_nav")
                                ) {
                                    MainNavTab.values().forEach { tab ->
                                        val isSelected = currentTab == tab
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { currentTab = tab },
                                            icon = {
                                                Icon(
                                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                    contentDescription = tab.title
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = tab.title,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = NavyDeep,
                                                selectedTextColor = GoldAccent,
                                                indicatorColor = GoldAccent,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.statusBars)
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (isShowingLaporan) {
                                LaporanScreen(
                                    viewModel = viewModel,
                                    user = user,
                                    onBack = { isShowingLaporan = false }
                                )
                            } else {
                                when (currentTab) {
                                    MainNavTab.HOME -> {
                                        DashboardScreen(
                                            viewModel = viewModel,
                                            user = user,
                                            onNavigateToTabungan = { currentTab = MainNavTab.TABUNGAN },
                                            onNavigateToTransaksi = { type ->
                                                initialTransactionType = type
                                                currentTab = MainNavTab.TRANSAKSI
                                            },
                                            onNavigateToWishlist = { currentTab = MainNavTab.WISHLIST },
                                            onNavigateToTarget = { currentTab = MainNavTab.WISHLIST },
                                            onOpenProfile = { currentTab = MainNavTab.PROFIL },
                                            onOpenAddTransaction = { isIncome ->
                                                addTxIsIncome = isIncome
                                                showAddTxDialog = true
                                            },
                                            onOpenAddPocket = { showAddPocketDialog = true }
                                        )
                                    }
                                    MainNavTab.TABUNGAN -> {
                                        TabunganScreen(
                                            viewModel = viewModel,
                                            user = user,
                                            onOpenAddPocket = { showAddPocketDialog = true }
                                        )
                                    }
                                    MainNavTab.TRANSAKSI -> {
                                        TransaksiScreen(
                                            viewModel = viewModel,
                                            user = user,
                                            initialTab = initialTransactionType,
                                            onOpenAdd = { isIncome ->
                                                addTxIsIncome = isIncome
                                                showAddTxDialog = true
                                            }
                                        )
                                    }
                                    MainNavTab.WISHLIST -> {
                                        WishlistScreen(
                                            viewModel = viewModel,
                                            user = user
                                        )
                                    }
                                    MainNavTab.PROFIL -> {
                                        ProfileScreen(
                                            viewModel = viewModel,
                                            user = user,
                                            onLogout = { viewModel.logout() },
                                            onNavigateToLaporan = { isShowingLaporan = true }
                                        )
                                    }
                                }
                            }

                            // Global Add Transaction Dialog
                            if (showAddTxDialog) {
                                AddTransactionDialog(
                                    initialIsIncome = addTxIsIncome,
                                    onDismiss = { showAddTxDialog = false },
                                    onConfirm = { type, amount, category, date, sourceOrMethod, note, attachmentUri ->
                                        viewModel.addTransaction(
                                            type = type,
                                            amount = amount,
                                            category = category,
                                            date = date,
                                            sourceOrMethod = sourceOrMethod,
                                            note = note,
                                            attachmentUri = attachmentUri
                                        )
                                        showAddTxDialog = false
                                    }
                                )
                            }

                            // Global Add Savings Pocket Dialog
                            if (showAddPocketDialog) {
                                AddPocketDialog(
                                    initialCategory = "NIKAH",
                                    onDismiss = { showAddPocketDialog = false },
                                    onConfirm = { name, category, targetAmount, initialBalance, startDate, targetDate, note, colorHex ->
                                        viewModel.createPocket(
                                            name = name,
                                            category = category,
                                            targetAmount = targetAmount,
                                            initialBalance = initialBalance,
                                            startDate = startDate,
                                            targetDate = targetDate,
                                            note = note,
                                            colorHex = colorHex
                                        )
                                        showAddPocketDialog = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}
