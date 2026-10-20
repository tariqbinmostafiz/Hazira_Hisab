package com.shifat.hajirahisab.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifat.hajirahisab.domain.model.CurrencyFormatter
import com.shifat.hajirahisab.ui.components.RecentActivityRow
import com.shifat.hajirahisab.ui.theme.MoneyAdvanceColor
import com.shifat.hajirahisab.ui.theme.MoneyDueColor
import com.shifat.hajirahisab.ui.theme.MoneyEarnedColor
import com.shifat.hajirahisab.ui.theme.MoneyExpenseColor
import com.shifat.hajirahisab.ui.theme.MoneyNetIncomeColor
import com.shifat.hajirahisab.ui.theme.MoneyReceivedColor
import com.shifat.hajirahisab.ui.viewmodel.LedgerViewModel
import java.time.YearMonth

@Composable
fun ReportsScreen(
    viewModel: LedgerViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val monthlySummary by viewModel.monthlySummary.collectAsState()
    val monthActivities by viewModel.monthActivities.collectAsState()
    val useBengaliDigits by viewModel.useBengaliDigits.collectAsState()
    val availableMonths by viewModel.availableMonths.collectAsState()
    val works by viewModel.workEntries.collectAsState()
    val pays by viewModel.payments.collectAsState()
    val exps by viewModel.expenses.collectAsState()
    val overallSummary by viewModel.overallSummary.collectAsState()

    val isCurrentCalendarMonth = selectedMonth == YearMonth.now()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Selector Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reports_month_selector"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.goToPreviousMonth() },
                    modifier = Modifier.testTag("reports_prev_month_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "পূর্ববর্তী মাস")
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = CurrencyFormatter.formatYearMonthBengali(selectedMonth, useBengaliDigits),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (isCurrentCalendarMonth) {
                        Text(
                            text = "(চলতি মাস)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.goToNextMonth() },
                    modifier = Modifier.testTag("reports_next_month_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "পরবর্তী মাস")
                }
            }
        }

        // Section 1: পূর্বের জের / Opening Dues Balance
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reports_opening_balance_card"),
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
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "১. পূর্বের মাসের জের (Opening Balance)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                ReportRowItem(
                    label = "পূর্ববর্তী মোট অর্জিত আয়:",
                    value = CurrencyFormatter.formatTaka(monthlySummary.previousEarnedPoisha, useBengaliDigits)
                )
                ReportRowItem(
                    label = "পূর্ববর্তী মোট পাওয়া টাকা:",
                    value = CurrencyFormatter.formatTaka(monthlySummary.previousReceivedPoisha, useBengaliDigits)
                )
                if (monthlySummary.previousDeductedFromDuesPoisha > 0L) {
                    ReportRowItem(
                        label = "পূর্ববর্তী বকেয়া থেকে কর্তন:",
                        value = CurrencyFormatter.formatTaka(monthlySummary.previousDeductedFromDuesPoisha, useBengaliDigits)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                val duesLabel = if (monthlySummary.isPreviousAdvance) "পূর্বের অগ্রিম স্থিতি:" else "মাসের শুরুতে পূর্বের বকেয়া:"
                val duesColor = if (monthlySummary.isPreviousAdvance) MoneyAdvanceColor else MoneyDueColor
                val duesAmount = if (monthlySummary.isPreviousAdvance) monthlySummary.previousAdvancePoisha else monthlySummary.previousUnpaidDuesPoisha

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = duesLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = CurrencyFormatter.formatTaka(duesAmount, useBengaliDigits),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = duesColor
                    )
                }
            }
        }

        // Section 2: চলতি মাসের হিসাব বিবরণী / Activity in this month
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reports_month_activity_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "২. এই মাসের কাজের আয় ও প্রাপ্তি",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                ReportRowItem(
                    label = "মোট হাজিরা:",
                    value = CurrencyFormatter.formatAttendance(monthlySummary.monthAttendanceUnits, useBengaliDigits),
                    valueColor = MoneyEarnedColor
                )
                ReportRowItem(
                    label = "মোট প্রাপ্য আয়:",
                    value = CurrencyFormatter.formatTaka(monthlySummary.monthEarnedPoisha, useBengaliDigits),
                    valueColor = MoneyEarnedColor
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                ReportRowItem(
                    label = "এই মাসের কাজের বিপরীতে প্রাপ্তি:",
                    value = CurrencyFormatter.formatTaka(monthlySummary.monthPaymentsForCurrentMonthPoisha, useBengaliDigits),
                    valueColor = MoneyReceivedColor
                )
                if (monthlySummary.monthPaymentsForPreviousDuesPoisha > 0L) {
                    ReportRowItem(
                        label = "পূর্বের বকেয়া বাবদ প্রাপ্তি:",
                        value = CurrencyFormatter.formatTaka(monthlySummary.monthPaymentsForPreviousDuesPoisha, useBengaliDigits),
                        valueColor = MoneyAdvanceColor
                    )
                }
                ReportRowItem(
                    label = "এই মাসে সর্বমোট প্রাপ্তি:",
                    value = CurrencyFormatter.formatTaka(monthlySummary.monthReceivedPoisha, useBengaliDigits),
                    valueColor = MoneyReceivedColor,
                    isBold = true
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                ReportRowItem(
                    label = "মোট খরচ (খাবার ও অন্যান্য):",
                    value = CurrencyFormatter.formatTaka(monthlySummary.monthExpensePoisha, useBengaliDigits),
                    valueColor = MoneyExpenseColor
                )
                if (monthlySummary.monthDeductedFromDuesPoisha > 0L) {
                    ReportRowItem(
                        label = "হাজিরা বা বকেয়া থেকে কর্তিত খরচ:",
                        value = CurrencyFormatter.formatTaka(monthlySummary.monthDeductedFromDuesPoisha, useBengaliDigits),
                        valueColor = MoneyDueColor
                    )
                }
            }
        }

        // Section 3: চুড়ান্ত হিসাব ও মোট বকেয়া / Closing Balance
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reports_final_balance_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "৩. চূড়ান্ত আর্থিক স্থিতি (Closing Summary)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                ReportRowItem(
                    label = "চলতি মাসের অবশিষ্ট বকেয়া পাওনা:",
                    value = CurrencyFormatter.formatTaka(monthlySummary.monthUnpaidDuesPoisha, useBengaliDigits),
                    valueColor = if (monthlySummary.isMonthAdvance) MoneyAdvanceColor else MoneyDueColor,
                    isBold = true
                )

                if (monthlySummary.remainingPreviousDuesPoisha > 0L) {
                    ReportRowItem(
                        label = "পূর্ববর্তী অবশিষ্ট বকেয়া:",
                        value = CurrencyFormatter.formatTaka(monthlySummary.remainingPreviousDuesPoisha, useBengaliDigits),
                        valueColor = MoneyDueColor
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "সর্বমোট বকেয়া পাওনা",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "আজ পর্যন্ত মালিকের কাছে পাওনা",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = CurrencyFormatter.formatTaka(monthlySummary.totalUnpaidDuesPoisha, useBengaliDigits),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MoneyDueColor
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "এই মাসের নিট সঞ্চয়/আয়:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = CurrencyFormatter.formatTaka(monthlySummary.monthNetIncomePoisha, useBengaliDigits),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MoneyNetIncomeColor
                    )
                }
            }
        }

        // Section 4: Activities in selected month
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "${CurrencyFormatter.formatYearMonthBengali(selectedMonth, useBengaliDigits)} এর সকল লেনদেন (${CurrencyFormatter.formatNumber(monthActivities.size.toLong(), useBengaliDigits)}টি)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (monthActivities.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = "এই মাসে কোনো লেনদেনের রেকর্ড নেই।",
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                monthActivities.forEach { item ->
                    RecentActivityRow(item = item, useBengaliDigits = useBengaliDigits)
                }
            }
        }

        // Section 5: মাসভিত্তিক পূর্ববর্তী ইতিহাস (Section 8.F - Monthly History, newest first)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "৫. সকল মাসের ইতিহাস ও তুলনা (${CurrencyFormatter.formatNumber(availableMonths.size.toLong(), useBengaliDigits)}টি মাস)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            availableMonths.forEach { m ->
                val itemSummary = remember(m, works, pays, exps) {
                    viewModel.calculateMonthlySummary(m, works, pays, exps)
                }
                val isSelected = m == selectedMonth

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("month_history_${m}")
                        .clickable { viewModel.selectMonth(m) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = CurrencyFormatter.formatYearMonthBengali(m, useBengaliDigits),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "নির্বাচিত",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "নিট: ${CurrencyFormatter.formatTaka(itemSummary.monthNetIncomePoisha, useBengaliDigits)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MoneyNetIncomeColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "হাজিরা: ${CurrencyFormatter.formatAttendance(itemSummary.monthAttendanceUnits, useBengaliDigits)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "আয়: ${CurrencyFormatter.formatTaka(itemSummary.monthEarnedPoisha, useBengaliDigits)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MoneyEarnedColor
                            )
                            Text(
                                text = "প্রাপ্তি: ${CurrencyFormatter.formatTaka(itemSummary.monthReceivedPoisha, useBengaliDigits)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MoneyReceivedColor
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "খরচ: ${CurrencyFormatter.formatTaka(itemSummary.monthExpensePoisha, useBengaliDigits)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MoneyExpenseColor
                            )
                            Text(
                                text = "মাসের বকেয়া: ${CurrencyFormatter.formatTaka(itemSummary.totalUnpaidDuesPoisha, useBengaliDigits)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MoneyDueColor
                            )
                        }
                    }
                }
            }
        }

        // Section 6: সর্বমোট সার্বিক হিসাব (Section 8.G - Overall Summary across all time)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reports_all_time_summary_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "৬. সর্বমোট সার্বিক হিসাব (আজ পর্যন্ত মোট)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                ReportRowItem(
                    label = "সর্বমোট হাজিরা:",
                    value = CurrencyFormatter.formatAttendance(overallSummary.totalAttendanceUnits, useBengaliDigits),
                    valueColor = MoneyEarnedColor
                )
                ReportRowItem(
                    label = "সর্বমোট অর্জিত আয়:",
                    value = CurrencyFormatter.formatTaka(overallSummary.totalEarnedPoisha, useBengaliDigits),
                    valueColor = MoneyEarnedColor
                )
                ReportRowItem(
                    label = "সর্বমোট পাওয়া টাকা:",
                    value = CurrencyFormatter.formatTaka(overallSummary.totalReceivedPoisha, useBengaliDigits),
                    valueColor = MoneyReceivedColor
                )
                ReportRowItem(
                    label = "সর্বমোট খরচ:",
                    value = CurrencyFormatter.formatTaka(overallSummary.totalExpensePoisha, useBengaliDigits),
                    valueColor = MoneyExpenseColor
                )
                ReportRowItem(
                    label = "সর্বমোট নিট আয়:",
                    value = CurrencyFormatter.formatTaka(overallSummary.netIncomePoisha, useBengaliDigits),
                    valueColor = MoneyNetIncomeColor,
                    isBold = true
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                ReportRowItem(
                    label = "সর্বমোট পাওনা বকেয়া:",
                    value = CurrencyFormatter.formatTaka(overallSummary.unpaidDuesPoisha, useBengaliDigits),
                    valueColor = MoneyDueColor,
                    isBold = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ReportRowItem(
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
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor
        )
    }
}
