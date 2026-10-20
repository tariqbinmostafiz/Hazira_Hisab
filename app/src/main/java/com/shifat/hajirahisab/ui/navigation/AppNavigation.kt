package com.shifat.hajirahisab.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifat.hajirahisab.ui.screens.DashboardScreen
import com.shifat.hajirahisab.ui.screens.ExpenseListScreen
import com.shifat.hajirahisab.ui.screens.PaymentListScreen
import com.shifat.hajirahisab.ui.screens.ReportsScreen
import com.shifat.hajirahisab.ui.screens.SettingsScreen
import com.shifat.hajirahisab.ui.screens.WorkListScreen
import com.shifat.hajirahisab.ui.viewmodel.LedgerViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class Screen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("হোম", Icons.Filled.Home, Icons.Outlined.Home),
    WORK("হাজিরা", Icons.Filled.Work, Icons.Outlined.WorkOutline),
    PAYMENT("পাওয়া", Icons.Filled.AttachMoney, Icons.Outlined.AttachMoney),
    EXPENSE("খরচ", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    REPORT("রিপোর্ট", Icons.Filled.Assessment, Icons.Outlined.Assessment),
    SETTINGS("সেটিংস", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    viewModel: LedgerViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var previousScreen by remember { mutableStateOf(Screen.HOME) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { message ->
            val isDeleteMessage = message.contains("মুছে ফেলা")
            val actionLabel = if (isDeleteMessage) "পুনরুদ্ধার" else null
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = if (isDeleteMessage) SnackbarDuration.Short else SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoLastDelete()
            }
        }
    }

    val isTopLevelDestination = currentScreen in listOf(Screen.HOME, Screen.WORK, Screen.PAYMENT, Screen.EXPENSE)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (currentScreen) {
                                Screen.HOME -> "হাজিরা হিসাব"
                                Screen.WORK -> "কাজের হাজিরা খাতা"
                                Screen.PAYMENT -> "টাকা পাওয়ার খাতা"
                                Screen.EXPENSE -> "খরচের খাতা"
                                Screen.REPORT -> "হিসাবের রিপোর্ট"
                                Screen.SETTINGS -> "সেটিংস"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentScreen == Screen.REPORT) {
                            Text(
                                text = "মাসভিত্তিক আয়, খরচ ও বকেয়ার বিস্তারিত হিসাব",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (!isTopLevelDestination) {
                        IconButton(
                            onClick = { currentScreen = previousScreen },
                            modifier = Modifier.testTag("top_bar_back_btn")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "ফিরে যান"
                            )
                        }
                    }
                },
                actions = {
                    // Report and Settings icons positioned at the top right app bar
                    IconButton(
                        onClick = {
                            if (currentScreen != Screen.REPORT) {
                                previousScreen = if (isTopLevelDestination) currentScreen else Screen.HOME
                                currentScreen = Screen.REPORT
                            }
                        },
                        modifier = Modifier.testTag("top_bar_report_btn")
                    ) {
                        Icon(
                            imageVector = if (currentScreen == Screen.REPORT) Icons.Filled.Assessment else Icons.Outlined.Assessment,
                            contentDescription = "রিপোর্ট",
                            tint = if (currentScreen == Screen.REPORT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = {
                            if (currentScreen != Screen.SETTINGS) {
                                previousScreen = if (isTopLevelDestination) currentScreen else Screen.HOME
                                currentScreen = Screen.SETTINGS
                            }
                        },
                        modifier = Modifier.testTag("top_bar_settings_btn")
                    ) {
                        Icon(
                            imageVector = if (currentScreen == Screen.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "সেটিংস",
                            tint = if (currentScreen == Screen.SETTINGS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            // Exactly 4 options in the bottom navigation: Home, Hazira, Pawa, Khoroch
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                val bottomNavItems = listOf(
                    Screen.HOME,
                    Screen.WORK,
                    Screen.PAYMENT,
                    Screen.EXPENSE
                )

                bottomNavItems.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Crossfade(targetState = currentScreen, label = "screen_transition") { targetScreen ->
                when (targetScreen) {
                    Screen.HOME -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToWork = { currentScreen = Screen.WORK },
                        onNavigateToPayment = { currentScreen = Screen.PAYMENT },
                        onNavigateToExpense = { currentScreen = Screen.EXPENSE }
                    )
                    Screen.WORK -> WorkListScreen(
                        viewModel = viewModel
                    )
                    Screen.PAYMENT -> PaymentListScreen(
                        viewModel = viewModel
                    )
                    Screen.EXPENSE -> ExpenseListScreen(
                        viewModel = viewModel
                    )
                    Screen.REPORT -> ReportsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = previousScreen }
                    )
                    Screen.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = previousScreen }
                    )
                }
            }
        }
    }
}
