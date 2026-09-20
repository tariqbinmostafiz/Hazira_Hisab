package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.MoneyAdvanceColor
import com.example.ui.theme.MoneyDueColor
import com.example.ui.theme.MoneyEarnedColor
import com.example.ui.theme.MoneyExpenseColor
import com.example.ui.theme.MoneyInHandColor
import com.example.ui.theme.MoneyReceivedColor
import com.example.ui.viewmodel.ReportPeriod

@Composable
fun ReportsScreen(
    periodSummary: FinancialSummary,
    selectedPeriod: ReportPeriod,
    useBengaliDigits: Boolean,
    aiAnalysis: String?,
    isAiLoading: Boolean,
    onPeriodSelected: (ReportPeriod) -> Unit,
    onRunAiAnalysis: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    text = "কাজের সময়কাল অনুযায়ী আয়, ব্যয় ও বকেয়ার সামগ্রিক হিসাব",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Period Selection Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ReportPeriod.values()) { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { onPeriodSelected(period) },
                        label = { Text(period.label) },
                        modifier = Modifier.testTag("report_period_${period.name}")
                    )
                }
            }
        }

        // Cash Position Highlight
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (periodSummary.cashInHandPoisha >= 0)
                        Color(0xFFE8F5E9)
                    else
                        Color(0xFFFFEBEE)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "এই সময়ের নগদ স্থিতি (হাতে অবশিষ্ট)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (periodSummary.cashInHandPoisha >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                            )
                            Text(
                                text = "মোট প্রাপ্ত টাকা থেকে মোট খরচ বাদে",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            text = CurrencyFormatter.formatTaka(periodSummary.cashInHandPoisha, useBengaliDigits),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (periodSummary.cashInHandPoisha >= 0) MoneyInHandColor else MoneyExpenseColor
                        )
                    }
                }
            }
        }

        // Detailed Financial Ledger Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "আর্থিক বিবরণী (${selectedPeriod.label})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Row 1: Work Attendance
                    ReportDetailRow(
                        title = "মোট কাজের পরিমাণ",
                        value = CurrencyFormatter.formatAttendance(periodSummary.totalAttendanceUnits, useBengaliDigits),
                        valueColor = MaterialTheme.colorScheme.onSurface
                    )

                    // Row 2: Total Earned
                    ReportDetailRow(
                        title = "মোট প্রাপ্য আয়",
                        value = CurrencyFormatter.formatTaka(periodSummary.totalEarnedPoisha, useBengaliDigits),
                        valueColor = MoneyEarnedColor
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Row 3: Total Received
                    ReportDetailRow(
                        title = "মোট পাওয়া নগদ টাকা",
                        value = CurrencyFormatter.formatTaka(periodSummary.totalReceivedPoisha, useBengaliDigits),
                        valueColor = MoneyReceivedColor
                    )

                    // Row 4: Outstanding Dues or Advance
                    val dueTitle = if (periodSummary.isAdvance) "অগ্রিম নেওয়া টাকা" else "বকেয়া বাকি টাকা"
                    val dueAmount = if (periodSummary.isAdvance) periodSummary.advancePoisha else periodSummary.unpaidDuesPoisha
                    val dueColor = if (periodSummary.isAdvance) MoneyAdvanceColor else MoneyDueColor
                    ReportDetailRow(
                        title = dueTitle,
                        value = CurrencyFormatter.formatTaka(dueAmount, useBengaliDigits),
                        valueColor = dueColor
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Row 5: Food Expense
                    ReportDetailRow(
                        title = "খাবার বাবদ খরচ",
                        value = CurrencyFormatter.formatTaka(periodSummary.foodExpensePoisha, useBengaliDigits),
                        valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Row 6: Other Expense
                    ReportDetailRow(
                        title = "অন্যান্য খরচ",
                        value = CurrencyFormatter.formatTaka(periodSummary.otherExpensePoisha, useBengaliDigits),
                        valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Row 7: Total Expense
                    ReportDetailRow(
                        title = "মোট ব্যক্তিগত খরচ",
                        value = CurrencyFormatter.formatTaka(periodSummary.totalExpensePoisha, useBengaliDigits),
                        valueColor = MoneyExpenseColor,
                        isBold = true
                    )
                }
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
                        Text(
                            text = "আপনার কাজের হাজিরা, আয়, বকেয়া পাওনা এবং খরচের অনুপাত বিশ্লেষণ করে এআই বাস্তবসম্মত পরামর্শ দিবে।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onRunAiAnalysis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("run_ai_analysis_btn"),
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
fun ReportDetailRow(
    title: String,
    value: String,
    valueColor: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = if (isBold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor
        )
    }
}
