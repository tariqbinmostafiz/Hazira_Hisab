package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CurrencyFormatter
import com.example.domain.model.DailySummary
import com.example.domain.model.FinancialSummary
import com.example.domain.model.MonthlyAccountingSummary
import com.example.domain.model.RecentActivityItem
import com.example.domain.model.TransactionType
import com.example.ui.components.ComparisonProgressCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MetricCard
import com.example.ui.theme.MoneyAdvanceColor
import com.example.ui.theme.MoneyDueColor
import com.example.ui.theme.MoneyEarnedColor
import com.example.ui.theme.MoneyExpenseColor
import com.example.ui.theme.MoneyInHandColor
import com.example.ui.theme.MoneyReceivedColor
import java.time.YearMonth

@Composable
fun DashboardScreen(
    monthlySummary: MonthlyAccountingSummary,
    selectedMonth: YearMonth,
    availableMonths: List<YearMonth>,
    onSelectMonth: (YearMonth) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onResetToCurrentMonth: () -> Unit,
    todaySummary: DailySummary,
    recentActivities: List<RecentActivityItem>,
    useBengaliDigits: Boolean,
    onAddWorkClick: () -> Unit,
    onAddPaymentClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onNavigateToWork: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFabExpanded by remember { mutableStateOf(false) }
    var showMonthDialog by remember { mutableStateOf(false) }
    val isCurrentMonth = selectedMonth == YearMonth.now()

    // Month Selection Dialog
    if (showMonthDialog) {
        AlertDialog(
            onDismissRequest = { showMonthDialog = false },
            title = {
                Text(
                    text = "মাস নির্বাচন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableMonths) { month ->
                        val isSelected = month == selectedMonth
                        val isNow = month == YearMonth.now()
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectMonth(month)
                                    showMonthDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = CurrencyFormatter.formatYearMonthBengali(month, useBengaliDigits),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isNow) {
                                        Spacer(Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = "চলতি মাস",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "নির্বাচিত",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMonthDialog = false }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("dashboard_scroll_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "হাজিরা হিসাব",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = CurrencyFormatter.formatDateBengali(CurrencyFormatter.todayIso(), useBengaliDigits),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Month Selector Bar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("month_selector_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onPreviousMonth,
                            modifier = Modifier.testTag("btn_prev_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "পূর্ববর্তী মাস"
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .clickable { showMonthDialog = true }
                                .testTag("btn_month_picker")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = CurrencyFormatter.formatYearMonthBengali(selectedMonth, useBengaliDigits),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                if (isCurrentMonth) {
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "চলতি মাস",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        IconButton(
                            onClick = onNextMonth,
                            modifier = Modifier.testTag("btn_next_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "পরবর্তী মাস"
                            )
                        }
                    }
                }
            }

            // Return to Current Month Banner (if viewing previous month)
            if (!isCurrentMonth) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "আপনি ${CurrencyFormatter.formatYearMonthBengali(selectedMonth, useBengaliDigits)} এর হিসাব দেখছেন",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            TextButton(
                                onClick = onResetToCurrentMonth,
                                modifier = Modifier.testTag("btn_back_to_current_month")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(text = "চলতি মাস")
                            }
                        }
                    }
                }
            }

            // Today's Activity Banner (shown if viewing current month)
            if (isCurrentMonth) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("today_summary_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EventAvailable,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "আজকের দিনের হিসাব",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "আজকের কাজ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = CurrencyFormatter.formatAttendance(todaySummary.attendanceUnits, useBengaliDigits),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Column {
                                    Text(
                                        text = "আজকের আয়",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = CurrencyFormatter.formatTaka(todaySummary.earnedPoisha, useBengaliDigits),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MoneyEarnedColor
                                    )
                                }

                                Column {
                                    Text(
                                        text = "আজকের পাওয়া",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = CurrencyFormatter.formatTaka(todaySummary.receivedPoisha, useBengaliDigits),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MoneyReceivedColor
                                    )
                                }

                                Column {
                                    Text(
                                        text = "আজকের খরচ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = CurrencyFormatter.formatTaka(todaySummary.expensePoisha, useBengaliDigits),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MoneyExpenseColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Opening Balance Card (পূর্বের বকেয়া ও নগদ স্থিতি carried forward)
            if (monthlySummary.hasPreviousData || monthlySummary.previousDuesPoisha != 0L || monthlySummary.previousCashInHandPoisha != 0L) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("opening_balance_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "পূর্বের বকেয়া ও নগদ স্থিতি",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "এই মাস শুরু হওয়ার আগের অমীমাংসিত হিসাবের জের",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Previous Outstanding Dues
                                val prevDueTitle = if (monthlySummary.isPreviousAdvance) "পূর্বের অগ্রিম" else "পূর্বের বকেয়া"
                                val prevDueVal = if (monthlySummary.isPreviousAdvance)
                                    CurrencyFormatter.formatTaka(monthlySummary.previousAdvancePoisha, useBengaliDigits)
                                else
                                    CurrencyFormatter.formatTaka(monthlySummary.previousUnpaidDuesPoisha, useBengaliDigits)
                                val prevDueColor = if (monthlySummary.isPreviousAdvance) MoneyAdvanceColor else MoneyDueColor

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = prevDueTitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = prevDueVal,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = prevDueColor
                                        )
                                        Text(
                                            text = if (monthlySummary.isPreviousAdvance) "অতিরিক্ত নেওয়া" else "আগের বাকি পাওনা",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                // Previous Cash in Hand
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "পূর্বের নগদ স্থিতি",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = CurrencyFormatter.formatTaka(monthlySummary.previousCashInHandPoisha, useBengaliDigits),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (monthlySummary.previousCashInHandPoisha >= 0) MoneyInHandColor else MoneyExpenseColor
                                        )
                                        Text(
                                            text = "আগের মাসের অবশিষ্ট নগদ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Cash in Hand Hero Card (Hate ache)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cash_in_hand_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (monthlySummary.totalCashInHandPoisha >= 0)
                            Color(0xFFE8F5E9)
                        else
                            Color(0xFFFFEBEE)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (monthlySummary.totalCashInHandPoisha >= 0) MoneyInHandColor else MoneyExpenseColor
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "হাতে থাকা নগদ টাকা",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (monthlySummary.totalCashInHandPoisha >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                                    )
                                    val cashSubtitle = if (monthlySummary.previousCashInHandPoisha != 0L) {
                                        "পূর্বের নগদ (${CurrencyFormatter.formatTaka(monthlySummary.previousCashInHandPoisha, useBengaliDigits)}) + এই মাসে প্রাপ্তি - নগদ খরচ"
                                    } else {
                                        "এই মাসে পাওয়া টাকা থেকে নগদ খরচ বাদে"
                                    }
                                    Text(
                                        text = cashSubtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Black.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        Text(
                            text = CurrencyFormatter.formatTaka(monthlySummary.totalCashInHandPoisha, useBengaliDigits),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = if (monthlySummary.totalCashInHandPoisha >= 0) MoneyInHandColor else MoneyExpenseColor
                        )
                    }
                }
            }

            // Metric Cards Grid (This Month's Numbers)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "এই মাসের প্রাপ্য আয়",
                        value = CurrencyFormatter.formatTaka(monthlySummary.monthEarnedPoisha, useBengaliDigits),
                        subtitle = "মোট ${CurrencyFormatter.formatAttendance(monthlySummary.monthAttendanceUnits, useBengaliDigits)}",
                        icon = Icons.Default.TrendingUp,
                        iconColor = MoneyEarnedColor,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_earned",
                        onClick = onNavigateToWork
                    )
                    MetricCard(
                        title = "এই মাসে পাওয়া টাকা",
                        value = CurrencyFormatter.formatTaka(monthlySummary.monthReceivedPoisha, useBengaliDigits),
                        subtitle = "এই মাসে নগদ আদায়",
                        icon = Icons.Default.AttachMoney,
                        iconColor = MoneyReceivedColor,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_received",
                        onClick = onNavigateToPayments
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val dueTitle = if (monthlySummary.isTotalAdvance) "মোট অগ্রিম নেওয়া" else "মোট বাকি পাওনা"
                    val dueVal = if (monthlySummary.isTotalAdvance)
                        CurrencyFormatter.formatTaka(monthlySummary.totalAdvancePoisha, useBengaliDigits)
                    else
                        CurrencyFormatter.formatTaka(monthlySummary.totalUnpaidDuesPoisha, useBengaliDigits)
                    val dueColor = if (monthlySummary.isTotalAdvance) MoneyAdvanceColor else MoneyDueColor

                    val dueSub = if (monthlySummary.previousDuesPoisha != 0L) {
                        "পূর্বের: ${CurrencyFormatter.formatTaka(monthlySummary.previousDuesPoisha, useBengaliDigits)} + নতুন: ${CurrencyFormatter.formatTaka(monthlySummary.monthNetDueChangePoisha, useBengaliDigits)}"
                    } else {
                        "মালিকের নিকট বর্তমান পাওনা"
                    }

                    MetricCard(
                        title = dueTitle,
                        value = dueVal,
                        subtitle = dueSub,
                        icon = Icons.Default.ReceiptLong,
                        iconColor = dueColor,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_due",
                        onClick = onNavigateToPayments
                    )
                    MetricCard(
                        title = "এই মাসের মোট খরচ",
                        value = CurrencyFormatter.formatTaka(monthlySummary.monthExpensePoisha, useBengaliDigits),
                        subtitle = "খাবার: ${CurrencyFormatter.formatTaka(monthlySummary.monthFoodExpensePoisha, useBengaliDigits)}",
                        icon = Icons.Default.ShoppingCart,
                        iconColor = MoneyExpenseColor,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_expense",
                        onClick = onNavigateToExpenses
                    )
                }
            }

            // Dedicated Monthly Summary Card (মাসিক হিসাবের সারসংক্ষেপ)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("monthly_detailed_summary_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${CurrencyFormatter.formatYearMonthBengali(selectedMonth, useBengaliDigits)} - মাসিক সারসংক্ষেপ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        MonthlySummaryRow(
                            label = "মোট কাজ / হাজিরা",
                            value = CurrencyFormatter.formatAttendance(monthlySummary.monthAttendanceUnits, useBengaliDigits)
                        )
                        MonthlySummaryRow(
                            label = "এই মাসের নতুন আয়",
                            value = CurrencyFormatter.formatTaka(monthlySummary.monthEarnedPoisha, useBengaliDigits),
                            valueColor = MoneyEarnedColor,
                            isBold = true
                        )
                        MonthlySummaryRow(
                            label = "এই মাসে পাওয়া টাকা",
                            value = CurrencyFormatter.formatTaka(monthlySummary.monthReceivedPoisha, useBengaliDigits),
                            valueColor = MoneyReceivedColor
                        )
                        if (monthlySummary.monthDeductedFromDuesPoisha > 0L) {
                            MonthlySummaryRow(
                                label = "পাওনা টাকা থেকে কাটা খরচ",
                                value = CurrencyFormatter.formatTaka(monthlySummary.monthDeductedFromDuesPoisha, useBengaliDigits),
                                valueColor = MaterialTheme.colorScheme.primary
                            )
                        }
                        MonthlySummaryRow(
                            label = "এই মাসের বকেয়া পরিবর্তন",
                            value = CurrencyFormatter.formatTaka(monthlySummary.monthNetDueChangePoisha, useBengaliDigits),
                            valueColor = if (monthlySummary.monthNetDueChangePoisha >= 0) MoneyDueColor else MoneyAdvanceColor
                        )
                        if (monthlySummary.previousDuesPoisha != 0L) {
                            MonthlySummaryRow(
                                label = "পূর্বের বকেয়া (আগের জের)",
                                value = CurrencyFormatter.formatTaka(monthlySummary.previousDuesPoisha, useBengaliDigits)
                            )
                        }
                        MonthlySummaryRow(
                            label = if (monthlySummary.isTotalAdvance) "সর্বমোট অগ্রিম" else "সর্বমোট বর্তমান বকেয়া",
                            value = if (monthlySummary.isTotalAdvance)
                                CurrencyFormatter.formatTaka(monthlySummary.totalAdvancePoisha, useBengaliDigits)
                            else
                                CurrencyFormatter.formatTaka(monthlySummary.totalUnpaidDuesPoisha, useBengaliDigits),
                            valueColor = if (monthlySummary.isTotalAdvance) MoneyAdvanceColor else MoneyDueColor,
                            isBold = true
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        MonthlySummaryRow(
                            label = "খাবার খরচ",
                            value = CurrencyFormatter.formatTaka(monthlySummary.monthFoodExpensePoisha, useBengaliDigits)
                        )
                        MonthlySummaryRow(
                            label = "অন্যান্য খরচ",
                            value = CurrencyFormatter.formatTaka(monthlySummary.monthOtherExpensePoisha, useBengaliDigits)
                        )
                        MonthlySummaryRow(
                            label = "মোট খরচ",
                            value = CurrencyFormatter.formatTaka(monthlySummary.monthExpensePoisha, useBengaliDigits),
                            valueColor = MoneyExpenseColor,
                            isBold = true
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        if (monthlySummary.previousCashInHandPoisha != 0L) {
                            MonthlySummaryRow(
                                label = "পূর্বের নগদ স্থিতি",
                                value = CurrencyFormatter.formatTaka(monthlySummary.previousCashInHandPoisha, useBengaliDigits)
                            )
                        }
                        MonthlySummaryRow(
                            label = "হাতে থাকা নগদ টাকা",
                            value = CurrencyFormatter.formatTaka(monthlySummary.totalCashInHandPoisha, useBengaliDigits),
                            valueColor = if (monthlySummary.totalCashInHandPoisha >= 0) MoneyInHandColor else MoneyExpenseColor,
                            isBold = true
                        )
                    }
                }
            }

            // Ratio Progress Card
            if (monthlySummary.monthEarnedPoisha > 0L || monthlySummary.monthReceivedPoisha > 0L) {
                item {
                    val recPercent = if (monthlySummary.monthEarnedPoisha > 0L) {
                        (monthlySummary.monthReceivedPoisha.toFloat() / monthlySummary.monthEarnedPoisha.toFloat())
                    } else 0f

                    val dueStr = if (monthlySummary.isTotalAdvance) {
                        CurrencyFormatter.formatTaka(monthlySummary.totalAdvancePoisha, useBengaliDigits)
                    } else {
                        CurrencyFormatter.formatTaka(monthlySummary.totalUnpaidDuesPoisha, useBengaliDigits)
                    }

                    ComparisonProgressCard(
                        earnedAmountStr = CurrencyFormatter.formatTaka(monthlySummary.monthEarnedPoisha, useBengaliDigits),
                        receivedAmountStr = CurrencyFormatter.formatTaka(monthlySummary.monthReceivedPoisha, useBengaliDigits),
                        dueOrAdvanceStr = dueStr,
                        isAdvance = monthlySummary.isTotalAdvance,
                        receivedPercent = recPercent
                    )
                }
            }

            // Recent Transactions Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${CurrencyFormatter.formatYearMonthBengali(selectedMonth, useBengaliDigits)} এর হিসাব",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Recent activities list or empty state
            if (recentActivities.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "এই মাসে এখনও কোনো হিসাব যোগ করা হয়নি",
                        description = "নিচের প্লাস (+) বাটনে চেপে আজ কত হাজিরা কাজ করেছেন বা কত টাকা পেয়েছেন লিখে রাখুন।",
                        icon = Icons.Default.ReceiptLong,
                        actionButtonText = "প্রথম হিসাব যোগ করুন",
                        onActionClick = onAddWorkClick
                    )
                }
            } else {
                items(recentActivities, key = { "${it.type}_${it.id}" }) { item ->
                    RecentActivityRow(item = item, useBengaliDigits = useBengaliDigits)
                }
            }
        }

        // Floating Action Button with Speed Dial
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(
                visible = isFabExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Option 1: Work
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            isFabExpanded = false
                            onAddWorkClick()
                        }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 3.dp,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "হাজিরা যোগ করুন",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        SmallFloatingActionButton(
                            onClick = {
                                isFabExpanded = false
                                onAddWorkClick()
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.testTag("fab_add_work")
                        ) {
                            Icon(imageVector = Icons.Default.Work, contentDescription = "হাজিরা যোগ")
                        }
                    }

                    // Option 2: Payment
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            isFabExpanded = false
                            onAddPaymentClick()
                        }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 3.dp,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "টাকা পাওয়া যোগ করুন",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        SmallFloatingActionButton(
                            onClick = {
                                isFabExpanded = false
                                onAddPaymentClick()
                            },
                            containerColor = MoneyReceivedColor,
                            contentColor = Color.White,
                            modifier = Modifier.testTag("fab_add_payment")
                        ) {
                            Icon(imageVector = Icons.Default.AttachMoney, contentDescription = "টাকা পাওয়া")
                        }
                    }

                    // Option 3: Expense
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            isFabExpanded = false
                            onAddExpenseClick()
                        }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 3.dp,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "খরচ যোগ করুন",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        SmallFloatingActionButton(
                            onClick = {
                                isFabExpanded = false
                                onAddExpenseClick()
                            },
                            containerColor = MoneyExpenseColor,
                            contentColor = Color.White,
                            modifier = Modifier.testTag("fab_add_expense")
                        ) {
                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "খরচ যোগ")
                        }
                    }
                }
            }

            // Primary FAB
            FloatingActionButton(
                onClick = { isFabExpanded = !isFabExpanded },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.testTag("main_fab_speed_dial")
            ) {
                Icon(
                    imageVector = if (isFabExpanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (isFabExpanded) "বন্ধ করুন" else "নতুন হিসাব যোগ করুন"
                )
            }
        }
    }
}

@Composable
fun RecentActivityRow(
    item: RecentActivityItem,
    useBengaliDigits: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                val (icon, iconBg, iconTint) = when (item.type) {
                    TransactionType.WORK -> Triple(
                        Icons.Default.Work,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.primary
                    )
                    TransactionType.PAYMENT -> Triple(
                        Icons.Default.ArrowDownward,
                        Color(0xFFE3F2FD),
                        MoneyReceivedColor
                    )
                    TransactionType.EXPENSE -> Triple(
                        Icons.Default.ArrowUpward,
                        Color(0xFFFFEBEE),
                        MoneyExpenseColor
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${CurrencyFormatter.formatShortDateBengali(item.date, useBengaliDigits)} • ${item.subtitle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            val amountPrefix = when (item.type) {
                TransactionType.WORK -> "+"
                TransactionType.PAYMENT -> "+"
                TransactionType.EXPENSE -> "-"
            }
            val amountColor = when (item.type) {
                TransactionType.WORK -> MoneyEarnedColor
                TransactionType.PAYMENT -> MoneyReceivedColor
                TransactionType.EXPENSE -> MoneyExpenseColor
            }

            Text(
                text = "$amountPrefix${CurrencyFormatter.formatTaka(item.amountPoisha, useBengaliDigits)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

@Composable
private fun MonthlySummaryRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor
        )
    }
}
