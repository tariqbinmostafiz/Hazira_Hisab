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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

@Composable
fun DashboardScreen(
    overallSummary: FinancialSummary,
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

            // Today's Activity Banner
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

            // Cash in Hand Hero Card (Hate ache)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cash_in_hand_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (overallSummary.cashInHandPoisha >= 0)
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
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (overallSummary.cashInHandPoisha >= 0) MoneyInHandColor else MoneyExpenseColor
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
                                        color = if (overallSummary.cashInHandPoisha >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                                    )
                                    Text(
                                        text = "মোট পাওয়া টাকা থেকে খরচ বাদে",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Black.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        Text(
                            text = CurrencyFormatter.formatTaka(overallSummary.cashInHandPoisha, useBengaliDigits),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = if (overallSummary.cashInHandPoisha >= 0) MoneyInHandColor else MoneyExpenseColor
                        )
                    }
                }
            }

            // Metric Cards Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "মোট প্রাপ্য আয়",
                        value = CurrencyFormatter.formatTaka(overallSummary.totalEarnedPoisha, useBengaliDigits),
                        subtitle = "মোট ${CurrencyFormatter.formatAttendance(overallSummary.totalAttendanceUnits, useBengaliDigits)}",
                        icon = Icons.Default.TrendingUp,
                        iconColor = MoneyEarnedColor,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_earned",
                        onClick = onNavigateToWork
                    )
                    MetricCard(
                        title = "মোট পাওয়া টাকা",
                        value = CurrencyFormatter.formatTaka(overallSummary.totalReceivedPoisha, useBengaliDigits),
                        subtitle = "নগদ আদায়",
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
                    val dueTitle = if (overallSummary.isAdvance) "অগ্রিম নেওয়া" else "মোট বাকি পাওনা"
                    val dueVal = if (overallSummary.isAdvance)
                        CurrencyFormatter.formatTaka(overallSummary.advancePoisha, useBengaliDigits)
                    else
                        CurrencyFormatter.formatTaka(overallSummary.unpaidDuesPoisha, useBengaliDigits)
                    val dueColor = if (overallSummary.isAdvance) MoneyAdvanceColor else MoneyDueColor

                    MetricCard(
                        title = dueTitle,
                        value = dueVal,
                        subtitle = if (overallSummary.isAdvance) "কাজের অতিরিক্ত" else "মালিকের নিকট পাওনা",
                        icon = Icons.Default.ReceiptLong,
                        iconColor = dueColor,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_due",
                        onClick = onNavigateToPayments
                    )
                    MetricCard(
                        title = "মোট খরচ",
                        value = CurrencyFormatter.formatTaka(overallSummary.totalExpensePoisha, useBengaliDigits),
                        subtitle = "খাবার: ${CurrencyFormatter.formatTaka(overallSummary.foodExpensePoisha, useBengaliDigits)}",
                        icon = Icons.Default.ShoppingCart,
                        iconColor = MoneyExpenseColor,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_expense",
                        onClick = onNavigateToExpenses
                    )
                }
            }

            // Ratio Progress Card
            if (overallSummary.totalEarnedPoisha > 0L || overallSummary.totalReceivedPoisha > 0L) {
                item {
                    val recPercent = if (overallSummary.totalEarnedPoisha > 0L) {
                        (overallSummary.totalReceivedPoisha.toFloat() / overallSummary.totalEarnedPoisha.toFloat())
                    } else 0f

                    val dueStr = if (overallSummary.isAdvance) {
                        CurrencyFormatter.formatTaka(overallSummary.advancePoisha, useBengaliDigits)
                    } else {
                        CurrencyFormatter.formatTaka(overallSummary.unpaidDuesPoisha, useBengaliDigits)
                    }

                    ComparisonProgressCard(
                        earnedAmountStr = CurrencyFormatter.formatTaka(overallSummary.totalEarnedPoisha, useBengaliDigits),
                        receivedAmountStr = CurrencyFormatter.formatTaka(overallSummary.totalReceivedPoisha, useBengaliDigits),
                        dueOrAdvanceStr = dueStr,
                        isAdvance = overallSummary.isAdvance,
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
                        text = "সাম্প্রতিক হিসাব",
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
                        title = "এখনও কোনো হিসাব যোগ করা হয়নি",
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
