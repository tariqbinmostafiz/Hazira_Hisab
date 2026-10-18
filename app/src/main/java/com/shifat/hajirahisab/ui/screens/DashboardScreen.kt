package com.shifat.hajirahisab.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifat.hajirahisab.domain.model.CurrencyFormatter
import com.shifat.hajirahisab.ui.components.EmptyStateView
import com.shifat.hajirahisab.ui.components.MetricCard
import com.shifat.hajirahisab.ui.components.RecentActivityRow
import com.shifat.hajirahisab.ui.dialogs.AddEditExpenseDialog
import com.shifat.hajirahisab.ui.dialogs.AddEditPaymentDialog
import com.shifat.hajirahisab.ui.dialogs.AddEditWorkDialog
import com.shifat.hajirahisab.ui.theme.MoneyDueColor
import com.shifat.hajirahisab.ui.theme.MoneyEarnedColor
import com.shifat.hajirahisab.ui.theme.MoneyExpenseColor
import com.shifat.hajirahisab.ui.theme.MoneyReceivedColor
import com.shifat.hajirahisab.ui.viewmodel.LedgerViewModel
import java.time.LocalDate

@Composable
fun DashboardScreen(
    viewModel: LedgerViewModel,
    onNavigateToWork: () -> Unit,
    onNavigateToPayment: () -> Unit,
    onNavigateToExpense: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todaySummary by viewModel.todaySummary.collectAsState()
    val currentMonthSummary by viewModel.currentMonthSummary.collectAsState()
    val currentMonthActivities by viewModel.currentMonthActivities.collectAsState()
    val useBengaliDigits by viewModel.useBengaliDigits.collectAsState()
    val defaultRate by viewModel.defaultRatePoisha.collectAsState()

    var showAddWorkDialog by remember { mutableStateOf(false) }
    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val todayBengaliDateStr = "আজ (${CurrencyFormatter.formatFullBengaliDate(today, useBengaliDigits)})"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // A. Header: App title and dynamically generated Bengali today date
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_header_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "হাজিরা হিসাব",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = todayBengaliDateStr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "হাজিরা খাতা",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // B. Today's Activity Card: আজকের দিনের হিসাব (Only Today's Records)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("today_summary_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = "আজকের দিনের হিসাব",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TodayStatItem(
                        label = "আজকের কাজ",
                        value = CurrencyFormatter.formatAttendance(todaySummary.attendanceUnits, useBengaliDigits),
                        color = MoneyEarnedColor,
                        modifier = Modifier.weight(1f)
                    )
                    TodayStatItem(
                        label = "আজকের আয়",
                        value = CurrencyFormatter.formatTaka(todaySummary.earnedPoisha, useBengaliDigits),
                        color = MoneyEarnedColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TodayStatItem(
                        label = "আজকের পাওয়া",
                        value = CurrencyFormatter.formatTaka(todaySummary.receivedPoisha, useBengaliDigits),
                        color = MoneyReceivedColor,
                        modifier = Modifier.weight(1f)
                    )
                    TodayStatItem(
                        label = "আজকের খরচ",
                        value = CurrencyFormatter.formatTaka(todaySummary.expensePoisha, useBengaliDigits),
                        color = MoneyExpenseColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // C. Current-month summary cards: 2x2 Grid (Strictly current calendar month totals)
        // Cash-in-hand card completely removed as requested
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "মোট প্রাপ্য আয়",
                    value = CurrencyFormatter.formatTaka(currentMonthSummary.monthEarnedPoisha, useBengaliDigits),
                    subtitle = CurrencyFormatter.formatAttendance(currentMonthSummary.monthAttendanceUnits, useBengaliDigits),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    iconColor = MoneyEarnedColor,
                    modifier = Modifier.weight(1f),
                    testTag = "home_total_earned_card",
                    onClick = onNavigateToWork
                )

                MetricCard(
                    title = "মোট পাওয়া টাকা",
                    value = CurrencyFormatter.formatTaka(currentMonthSummary.monthReceivedPoisha, useBengaliDigits),
                    subtitle = "জমা হয়েছে",
                    icon = Icons.Default.AttachMoney,
                    iconColor = MoneyReceivedColor,
                    modifier = Modifier.weight(1f),
                    testTag = "home_total_received_card",
                    onClick = onNavigateToPayment
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "মোট বাকি পাওনা",
                    value = CurrencyFormatter.formatTaka(currentMonthSummary.monthUnpaidDuesPoisha, useBengaliDigits),
                    subtitle = if (currentMonthSummary.isMonthAdvance) "অগ্রিম পরিশোধ" else "চলতি মাসের পাওনা",
                    icon = Icons.Default.PendingActions,
                    iconColor = MoneyDueColor,
                    modifier = Modifier.weight(1f),
                    testTag = "home_total_due_card",
                    onClick = onNavigateToPayment
                )

                MetricCard(
                    title = "মোট খরচ",
                    value = CurrencyFormatter.formatTaka(currentMonthSummary.monthExpensePoisha, useBengaliDigits),
                    subtitle = "চলতি মাসে ব্যয়",
                    icon = Icons.Default.ShoppingCart,
                    iconColor = MoneyExpenseColor,
                    modifier = Modifier.weight(1f),
                    testTag = "home_total_expense_card",
                    onClick = onNavigateToExpense
                )
            }
        }

        // D. Current-Month Income/Payment Ratio Card
        val earnedPoisha = currentMonthSummary.monthEarnedPoisha
        val receivedForCurrentMonth = currentMonthSummary.monthPaymentsForCurrentMonthPoisha
        val ratioProgress = if (earnedPoisha > 0L) {
            (receivedForCurrentMonth.toFloat() / earnedPoisha.toFloat()).coerceIn(0f, 1f)
        } else if (currentMonthSummary.monthReceivedPoisha > 0L) {
            1f
        } else {
            0f
        }
        val ratioPercentage = (ratioProgress * 100).toInt()

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_ratio_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "চলতি মাসের আয় বনাম আদায়",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${CurrencyFormatter.formatNumber(ratioPercentage.toLong(), useBengaliDigits)}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { ratioProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "আদায়: ${CurrencyFormatter.formatTaka(receivedForCurrentMonth, useBengaliDigits)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MoneyReceivedColor
                    )
                    Text(
                        text = "প্রাপ্য: ${CurrencyFormatter.formatTaka(earnedPoisha, useBengaliDigits)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MoneyEarnedColor
                    )
                }
            }
        }

        // Quick Entry Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showAddWorkDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .testTag("home_add_work_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MoneyEarnedColor)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("হাজিরা", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Button(
                onClick = { showAddPaymentDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .testTag("home_add_payment_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MoneyReceivedColor)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("পাওয়া", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Button(
                onClick = { showAddExpenseDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .testTag("home_add_expense_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MoneyExpenseColor)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("খরচ", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        // Recent Activity Section (Current Month)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "চলতি মাসের সাম্প্রতিক লেনদেন",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (currentMonthActivities.isEmpty()) {
                EmptyStateView(
                    title = "কোনো লেনদেন নেই",
                    description = "চলতি মাসে এখনও কোনো কাজের হাজিরা, টাকা পাওয়া বা খরচের হিসাব লিপিবদ্ধ করা হয়নি।",
                    icon = Icons.Default.ReceiptLong,
                    actionButtonText = "+ হাজিরা যোগ করুন",
                    onActionClick = { showAddWorkDialog = true }
                )
            } else {
                currentMonthActivities.take(10).forEach { item ->
                    RecentActivityRow(
                        item = item,
                        useBengaliDigits = useBengaliDigits
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Dialogs
    if (showAddWorkDialog) {
        AddEditWorkDialog(
            initialEntry = null,
            defaultRatePoisha = defaultRate,
            useBengaliDigits = useBengaliDigits,
            onDismiss = { showAddWorkDialog = false },
            onSave = { date, units, rate, workType, note ->
                viewModel.saveWorkEntry(
                    date = date,
                    attendanceUnits = units,
                    rateInPoisha = rate,
                    workType = workType,
                    note = note
                )
                showAddWorkDialog = false
            }
        )
    }

    if (showAddPaymentDialog) {
        AddEditPaymentDialog(
            initialPayment = null,
            useBengaliDigits = useBengaliDigits,
            onDismiss = { showAddPaymentDialog = false },
            onSave = { date, amount, payer, note, paymentTarget ->
                viewModel.savePayment(
                    date = date,
                    amountInPoisha = amount,
                    payer = payer,
                    note = note,
                    paymentTarget = paymentTarget
                )
                showAddPaymentDialog = false
            }
        )
    }

    if (showAddExpenseDialog) {
        AddEditExpenseDialog(
            initialExpense = null,
            useBengaliDigits = useBengaliDigits,
            onDismiss = { showAddExpenseDialog = false },
            onSave = { date, amount, category, note, source ->
                viewModel.saveExpense(
                    date = date,
                    amountInPoisha = amount,
                    category = category,
                    note = note,
                    paymentSource = source
                )
                showAddExpenseDialog = false
            }
        )
    }
}

@Composable
private fun TodayStatItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
