package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CurrencyFormatter
import com.example.domain.model.FinancialSummary
import com.example.domain.model.MonthlyAccountingSummary
import com.example.domain.model.RecentActivityItem
import com.example.ui.theme.MoneyAdvanceColor
import com.example.ui.theme.MoneyDueColor
import com.example.ui.theme.MoneyEarnedColor
import com.example.ui.theme.MoneyExpenseColor
import com.example.ui.theme.MoneyInHandColor
import com.example.ui.theme.MoneyReceivedColor
import com.example.ui.viewmodel.ReportPeriod
import java.time.YearMonth

@Composable
fun ReportsScreen(
    monthlySummary: MonthlyAccountingSummary,
    selectedMonth: YearMonth,
    availableMonths: List<YearMonth>,
    monthActivities: List<RecentActivityItem>,
    periodSummary: FinancialSummary,
    selectedPeriod: ReportPeriod,
    useBengaliDigits: Boolean,
    aiAnalysis: String?,
    isAiLoading: Boolean,
    onSelectMonth: (YearMonth) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onResetToCurrentMonth: () -> Unit,
    onPeriodSelected: (ReportPeriod) -> Unit,
    onRunAiAnalysis: () -> Unit,
    modifier: Modifier = Modifier
) {
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "আর্থিক রিপোর্ট ও বিশ্লেষণ",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "মাসভিত্তিক আয়, ব্যয় ও বকেয়া পাওনার পূর্ণাঙ্গ হিসাব",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Dedicated Month Selector Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reports_month_selector_card"),
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

        // Section: Selected Month Summary (৪টি মূল মেট্রিক কার্ড)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "${CurrencyFormatter.formatYearMonthBengali(selectedMonth, useBengaliDigits)}-এর হিসাব",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // এই মাসের মোট কাজ
                        MonthlyMetricItem(
                            title = "মোট কাজ",
                            value = CurrencyFormatter.formatAttendance(monthlySummary.monthAttendanceUnits, useBengaliDigits),
                            valueColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        // এই মাসের মোট আয়
                        MonthlyMetricItem(
                            title = "এই মাসের মোট আয়",
                            value = CurrencyFormatter.formatTaka(monthlySummary.monthEarnedPoisha, useBengaliDigits),
                            valueColor = MoneyEarnedColor,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // এই মাসে মোট পাওয়া
                        MonthlyMetricItem(
                            title = "এই মাসে মোট পাওয়া",
                            value = CurrencyFormatter.formatTaka(monthlySummary.monthReceivedPoisha, useBengaliDigits),
                            valueColor = MoneyReceivedColor,
                            modifier = Modifier.weight(1f)
                        )
                        // এই মাসের মোট খরচ
                        MonthlyMetricItem(
                            title = "এই মাসের মোট খরচ",
                            value = CurrencyFormatter.formatTaka(monthlySummary.monthExpensePoisha, useBengaliDigits),
                            valueColor = MoneyExpenseColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Cash Position Highlight for Selected Month
        item {
            val cashInHand = monthlySummary.totalCashInHandPoisha
            val cashSubtitle = if (monthlySummary.previousCashInHandPoisha != 0L) {
                "পূর্বের নগদ (${CurrencyFormatter.formatTaka(monthlySummary.previousCashInHandPoisha, useBengaliDigits)}) + এই মাসে প্রাপ্তি - নগদ খরচ বাদে"
            } else {
                "এই মাসে প্রাপ্ত টাকা থেকে নগদ খরচ বাদে"
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (cashInHand >= 0)
                        Color(0xFFE8F5E9)
                    else
                        Color(0xFFFFEBEE)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (cashInHand >= 0) MoneyInHandColor else MoneyExpenseColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "হাতে থাকা নগদ স্থিতি",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (cashInHand >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cashSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            text = CurrencyFormatter.formatTaka(cashInHand, useBengaliDigits),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (cashInHand >= 0) MoneyInHandColor else MoneyExpenseColor
                        )
                    }
                }
            }
        }

        // Opening Balance / Previous Month Balance Carry-forward Card
        if (monthlySummary.hasPreviousData || monthlySummary.previousDuesPoisha != 0L || monthlySummary.previousCashInHandPoisha != 0L) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                    text = "পূর্বের জের ও বকেয়া হিসাব",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "এই মাস শুরু হওয়ার পূর্বের অমীমাংসিত হিসাবের জের",
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
                            // Previous Dues
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

                            // Previous Cash
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
                                        text = "আগের মাসের উদ্বৃত্ত নগদ",
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

        // Detailed Financial Ledger Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "${CurrencyFormatter.formatYearMonthBengali(selectedMonth, useBengaliDigits)} - পূর্ণাঙ্গ বিবরণী",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Row 1: Work Attendance
                    ReportDetailRow(
                        title = "মোট কাজ / হাজিরা",
                        value = CurrencyFormatter.formatAttendance(monthlySummary.monthAttendanceUnits, useBengaliDigits),
                        valueColor = MaterialTheme.colorScheme.onSurface
                    )

                    // Row 2: Total Earned
                    ReportDetailRow(
                        title = "এই মাসের প্রাপ্য নতুন আয়",
                        value = CurrencyFormatter.formatTaka(monthlySummary.monthEarnedPoisha, useBengaliDigits),
                        valueColor = MoneyEarnedColor,
                        isBold = true
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Row 3: Total Received
                    ReportDetailRow(
                        title = "এই মাসে পাওয়া মোট নগদ টাকা",
                        value = CurrencyFormatter.formatTaka(monthlySummary.monthReceivedPoisha, useBengaliDigits),
                        valueColor = MoneyReceivedColor
                    )

                    if (monthlySummary.monthPaymentsForPreviousDuesPoisha > 0L) {
                        ReportDetailRow(
                            title = "  └ পূর্বের বকেয়া বাবদ আদায়",
                            value = CurrencyFormatter.formatTaka(monthlySummary.monthPaymentsForPreviousDuesPoisha, useBengaliDigits),
                            valueColor = MoneyReceivedColor
                        )
                        ReportDetailRow(
                            title = "  └ চলতি মাসের কাজের জন্য আদায়",
                            value = CurrencyFormatter.formatTaka(monthlySummary.monthPaymentsForCurrentMonthPoisha, useBengaliDigits),
                            valueColor = MoneyReceivedColor
                        )
                    }

                    // If dues deduction exists
                    if (monthlySummary.monthDeductedFromDuesPoisha > 0L) {
                        ReportDetailRow(
                            title = "পাওনা টাকা থেকে কাটা খরচ",
                            value = CurrencyFormatter.formatTaka(monthlySummary.monthDeductedFromDuesPoisha, useBengaliDigits),
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Row 4: Net due change this month
                    val monthDueTitle = if (monthlySummary.isMonthAdvance) "চলতি মাসের অগ্রিম" else "চলতি মাসের বাকি পাওনা"
                    val monthDueAmount = if (monthlySummary.isMonthAdvance) monthlySummary.monthAdvancePoisha else monthlySummary.monthUnpaidDuesPoisha
                    ReportDetailRow(
                        title = monthDueTitle,
                        value = CurrencyFormatter.formatTaka(monthDueAmount, useBengaliDigits),
                        valueColor = if (monthlySummary.isMonthAdvance) MoneyAdvanceColor else MoneyDueColor
                    )

                    // Previous dues carry forward
                    if (monthlySummary.previousDuesPoisha != 0L) {
                        val prevTitle = if (monthlySummary.isPreviousAdvance) "পূর্বের অগ্রিম জের" else "পূর্বের বকেয়া জের"
                        ReportDetailRow(
                            title = prevTitle,
                            value = CurrencyFormatter.formatTaka(monthlySummary.previousDuesPoisha, useBengaliDigits),
                            valueColor = if (monthlySummary.isPreviousAdvance) MoneyAdvanceColor else MoneyDueColor
                        )
                    }

                    // Row 5: Total Outstanding Closing Across All Time
                    val totalDueTitle = if (monthlySummary.isTotalAdvance) "সর্বমোট অগ্রিম নেওয়া টাকা" else "সর্বমোট সমাপনী বকেয়া পাওনা"
                    val totalDueAmount = if (monthlySummary.isTotalAdvance) monthlySummary.totalAdvancePoisha else monthlySummary.totalUnpaidDuesPoisha
                    val totalDueColor = if (monthlySummary.isTotalAdvance) MoneyAdvanceColor else MoneyDueColor
                    ReportDetailRow(
                        title = totalDueTitle,
                        value = CurrencyFormatter.formatTaka(totalDueAmount, useBengaliDigits),
                        valueColor = totalDueColor,
                        isBold = true
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Row 6: Food Expense
                    ReportDetailRow(
                        title = "খাবার বাবদ খরচ",
                        value = CurrencyFormatter.formatTaka(monthlySummary.monthFoodExpensePoisha, useBengaliDigits),
                        valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Row 7: Other Expense
                    ReportDetailRow(
                        title = "অন্যান্য খরচ",
                        value = CurrencyFormatter.formatTaka(monthlySummary.monthOtherExpensePoisha, useBengaliDigits),
                        valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Row 8: Total Expense
                    ReportDetailRow(
                        title = "এই মাসের মোট খরচ",
                        value = CurrencyFormatter.formatTaka(monthlySummary.monthExpensePoisha, useBengaliDigits),
                        valueColor = MoneyExpenseColor,
                        isBold = true
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    if (monthlySummary.previousCashInHandPoisha != 0L) {
                        ReportDetailRow(
                            title = "পূর্বের নগদ স্থিতি",
                            value = CurrencyFormatter.formatTaka(monthlySummary.previousCashInHandPoisha, useBengaliDigits),
                            valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    ReportDetailRow(
                        title = "হাতে থাকা মোট নগদ টাকা",
                        value = CurrencyFormatter.formatTaka(monthlySummary.totalCashInHandPoisha, useBengaliDigits),
                        valueColor = if (monthlySummary.totalCashInHandPoisha >= 0) MoneyInHandColor else MoneyExpenseColor,
                        isBold = true
                    )
                }
            }
        }

        // Monthly Comparison / Ratio Progress Card
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

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "এই মাসের আয় ও আদায়ের অনুপাত",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val pct = (recPercent * 100).toInt().coerceIn(0, 999)
                            Text(
                                text = "${CurrencyFormatter.formatNumber(pct.toLong(), true)}% আদায়",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MoneyReceivedColor
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { recPercent.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = MoneyReceivedColor,
                            trackColor = MoneyReceivedColor.copy(alpha = 0.15f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "এই মাসের আয়",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyFormatter.formatTaka(monthlySummary.monthEarnedPoisha, useBengaliDigits),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MoneyEarnedColor
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "আদায় হয়েছে",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyFormatter.formatTaka(monthlySummary.monthReceivedPoisha, useBengaliDigits),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MoneyReceivedColor
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (monthlySummary.isTotalAdvance) "সর্বমোট অগ্রিম" else "সর্বমোট বাকি",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = dueStr,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (monthlySummary.isTotalAdvance) MoneyAdvanceColor else MoneyDueColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Transactions In Selected Month
        item {
            Text(
                text = "${CurrencyFormatter.formatYearMonthBengali(selectedMonth, useBengaliDigits)} এর কাজের ও টাকার হিসাব",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (monthActivities.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "এই মাসে কোনো হিসাব লিপিবদ্ধ করা হয়নি",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(monthActivities, key = { "rep_${it.type}_${it.id}" }) { item ->
                RecentActivityRow(item = item, useBengaliDigits = useBengaliDigits)
            }
        }

        // Smart AI Financial Advisor Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "স্মার্ট এআই হিসাব সহকারী",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "উচ্চ বুদ্ধিমত্তার পরামর্শ ও আর্থিক বিশ্লেষণ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isAiLoading) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "হিসাব বিশ্লেষণ ও গভীর চিন্তা করা হচ্ছে...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    } else if (aiAnalysis != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Text(
                                text = aiAnalysis,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onRunAiAnalysis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reanalyze_ai_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("পুনরায় বিশ্লেষণ করুন")
                        }
                    } else {
                        Button(
                            onClick = onRunAiAnalysis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("run_ai_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("হিসাবের এআই বিশ্লেষণ দেখুন")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyMetricItem(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun ReportDetailRow(
    title: String,
    value: String,
    valueColor: Color,
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
            text = title,
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
