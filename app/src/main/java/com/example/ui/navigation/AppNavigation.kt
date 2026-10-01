package com.example.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.WorkEntryEntity
import com.example.ui.dialogs.AddEditExpenseDialog
import com.example.ui.dialogs.AddEditPaymentDialog
import com.example.ui.dialogs.AddEditWorkDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpenseListScreen
import com.example.ui.screens.PaymentListScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WorkListScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LedgerViewModel
import kotlinx.coroutines.flow.collectLatest

enum class NavDestination(val route: String, val title: String, val icon: ImageVector) {
    HOME("home", "হোম", Icons.Default.Home),
    WORK("work", "হাজিরা", Icons.Default.Work),
    PAYMENTS("payments", "পাওয়া", Icons.Default.AttachMoney),
    EXPENSES("expenses", "খরচ", Icons.Default.ShoppingCart),
    REPORTS("reports", "রিপোর্ট", Icons.Default.Assessment),
    SETTINGS("settings", "সেটিংস", Icons.Default.Settings)
}

@Composable
fun MainAppNavigation(viewModel: LedgerViewModel) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDark = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    MyApplicationTheme(darkTheme = isDark) {
        val snackbarHostState = remember { SnackbarHostState() }
        var currentDestination by remember { mutableStateOf(NavDestination.HOME) }

        // State flows from ViewModel
        val overallSummary by viewModel.overallSummary.collectAsState()
        val todaySummary by viewModel.todaySummary.collectAsState()
        val workEntries by viewModel.workEntries.collectAsState()
        val payments by viewModel.payments.collectAsState()
        val expenses by viewModel.expenses.collectAsState()
        val recentActivities by viewModel.recentActivities.collectAsState()
        val defaultRatePoisha by viewModel.defaultRatePoisha.collectAsState()
        val useBengaliDigits by viewModel.useBengaliDigits.collectAsState()
        val selectedPeriod by viewModel.selectedPeriod.collectAsState()
        val periodSummary by viewModel.periodSummary.collectAsState()
        val selectedMonth by viewModel.selectedMonth.collectAsState()
        val availableMonths by viewModel.availableMonths.collectAsState()
        val monthlySummary by viewModel.monthlySummary.collectAsState()
        val monthActivities by viewModel.monthActivities.collectAsState()
        val aiAnalysis by viewModel.aiAnalysis.collectAsState()
        val isAiLoading by viewModel.isAiLoading.collectAsState()

        // Dialog states
        var showWorkDialog by remember { mutableStateOf(false) }
        var workToEdit by remember { mutableStateOf<WorkEntryEntity?>(null) }

        var showPaymentDialog by remember { mutableStateOf(false) }
        var paymentToEdit by remember { mutableStateOf<PaymentEntity?>(null) }

        var showExpenseDialog by remember { mutableStateOf(false) }
        var expenseToEdit by remember { mutableStateOf<ExpenseEntity?>(null) }

        // Listen for snackbar events
        LaunchedEffect(Unit) {
            viewModel.snackbarMessage.collectLatest { msg ->
                val result = snackbarHostState.showSnackbar(
                    message = msg,
                    actionLabel = if (msg.contains("মুছে")) "ফিরিয়ে আনুন" else null,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undoLastDelete()
                }
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                NavigationBar(modifier = Modifier.testTag("main_bottom_nav")) {
                    listOf(
                        NavDestination.HOME,
                        NavDestination.WORK,
                        NavDestination.PAYMENTS,
                        NavDestination.EXPENSES,
                        NavDestination.REPORTS,
                        NavDestination.SETTINGS
                    ).forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination == destination,
                            onClick = { currentDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.title
                                )
                            },
                            label = { Text(destination.title) },
                            modifier = Modifier.testTag("nav_item_${destination.route}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentDestination,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_nav_transition"
                ) { dest ->
                    when (dest) {
                        NavDestination.HOME -> DashboardScreen(
                            monthlySummary = monthlySummary,
                            selectedMonth = selectedMonth,
                            availableMonths = availableMonths,
                            onSelectMonth = { viewModel.selectMonth(it) },
                            onPreviousMonth = { viewModel.goToPreviousMonth() },
                            onNextMonth = { viewModel.goToNextMonth() },
                            onResetToCurrentMonth = { viewModel.resetToCurrentMonth() },
                            todaySummary = todaySummary,
                            recentActivities = if (monthActivities.isNotEmpty()) monthActivities else recentActivities,
                            useBengaliDigits = useBengaliDigits,
                            onAddWorkClick = {
                                workToEdit = null
                                showWorkDialog = true
                            },
                            onAddPaymentClick = {
                                paymentToEdit = null
                                showPaymentDialog = true
                            },
                            onAddExpenseClick = {
                                expenseToEdit = null
                                showExpenseDialog = true
                            },
                            onNavigateToWork = { currentDestination = NavDestination.WORK },
                            onNavigateToPayments = { currentDestination = NavDestination.PAYMENTS },
                            onNavigateToExpenses = { currentDestination = NavDestination.EXPENSES }
                        )

                        NavDestination.WORK -> WorkListScreen(
                            workEntries = workEntries,
                            useBengaliDigits = useBengaliDigits,
                            onAddWorkClick = {
                                workToEdit = null
                                showWorkDialog = true
                            },
                            onEditWorkClick = { entry ->
                                workToEdit = entry
                                showWorkDialog = true
                            },
                            onDeleteWorkClick = { entry ->
                                viewModel.deleteWorkEntry(entry)
                            }
                        )

                        NavDestination.PAYMENTS -> PaymentListScreen(
                            payments = payments,
                            totalEarnedPoisha = overallSummary.totalEarnedPoisha,
                            deductedFromDuesPoisha = overallSummary.deductedFromDuesPoisha,
                            useBengaliDigits = useBengaliDigits,
                            onAddPaymentClick = {
                                paymentToEdit = null
                                showPaymentDialog = true
                            },
                            onEditPaymentClick = { payment ->
                                paymentToEdit = payment
                                showPaymentDialog = true
                            },
                            onDeletePaymentClick = { payment ->
                                viewModel.deletePayment(payment)
                            }
                        )

                        NavDestination.EXPENSES -> ExpenseListScreen(
                            expenses = expenses,
                            useBengaliDigits = useBengaliDigits,
                            onAddExpenseClick = {
                                expenseToEdit = null
                                showExpenseDialog = true
                            },
                            onEditExpenseClick = { expense ->
                                expenseToEdit = expense
                                showExpenseDialog = true
                            },
                            onDeleteExpenseClick = { expense ->
                                viewModel.deleteExpense(expense)
                            }
                        )

                        NavDestination.REPORTS -> ReportsScreen(
                            periodSummary = periodSummary,
                            selectedPeriod = selectedPeriod,
                            useBengaliDigits = useBengaliDigits,
                            aiAnalysis = aiAnalysis,
                            isAiLoading = isAiLoading,
                            onPeriodSelected = { viewModel.selectPeriod(it) },
                            onRunAiAnalysis = { viewModel.runAiAnalysis() },
                            monthlySummary = monthlySummary,
                            selectedMonth = selectedMonth
                        )

                        NavDestination.SETTINGS -> SettingsScreen(
                            defaultRatePoisha = defaultRatePoisha,
                            themeMode = themeMode,
                            useBengaliDigits = useBengaliDigits,
                            onUpdateDefaultRate = { viewModel.updateDefaultRate(it) },
                            onUpdateThemeMode = { viewModel.updateThemeMode(it) },
                            onUpdateBengaliDigits = { viewModel.updateBengaliDigits(it) },
                            onLoadDemoData = { viewModel.loadDemoData() },
                            onClearAllData = { viewModel.clearAllData() },
                            onExportBackup = { viewModel.getBackupJson() },
                            onRestoreBackup = { json -> viewModel.restoreBackup(json) }
                        )
                    }
                }

                // Add/Edit Work Dialog
                if (showWorkDialog) {
                    AddEditWorkDialog(
                        initialEntry = workToEdit,
                        defaultRatePoisha = defaultRatePoisha,
                        useBengaliDigits = useBengaliDigits,
                        onDismiss = {
                            showWorkDialog = false
                            workToEdit = null
                        },
                        onSave = { date, attUnits, rate, workType, note ->
                            viewModel.saveWorkEntry(
                                id = workToEdit?.id ?: 0L,
                                date = date,
                                attendanceUnits = attUnits,
                                rateInPoisha = rate,
                                workType = workType,
                                note = note
                            )
                            showWorkDialog = false
                            workToEdit = null
                        }
                    )
                }

                // Add/Edit Payment Dialog
                if (showPaymentDialog) {
                    AddEditPaymentDialog(
                        initialPayment = paymentToEdit,
                        useBengaliDigits = useBengaliDigits,
                        onDismiss = {
                            showPaymentDialog = false
                            paymentToEdit = null
                        },
                        onSave = { date, amount, payer, note ->
                            viewModel.savePayment(
                                id = paymentToEdit?.id ?: 0L,
                                date = date,
                                amountInPoisha = amount,
                                payer = payer,
                                note = note
                            )
                            showPaymentDialog = false
                            paymentToEdit = null
                        }
                    )
                }

                // Add/Edit Expense Dialog
                if (showExpenseDialog) {
                    AddEditExpenseDialog(
                        initialExpense = expenseToEdit,
                        useBengaliDigits = useBengaliDigits,
                        onDismiss = {
                            showExpenseDialog = false
                            expenseToEdit = null
                        },
                        onSave = { date, amount, category, note, paymentSource ->
                            viewModel.saveExpense(
                                id = expenseToEdit?.id ?: 0L,
                                date = date,
                                amountInPoisha = amount,
                                category = category,
                                note = note,
                                paymentSource = paymentSource
                            )
                            showExpenseDialog = false
                            expenseToEdit = null
                        }
                    )
                }
            }
        }
    }
}
