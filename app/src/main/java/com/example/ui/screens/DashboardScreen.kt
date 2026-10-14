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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CurrencyFormatter
import com.example.domain.model.DailySummary
import com.example.domain.model.MonthlyAccountingSummary
import com.example.domain.model.RecentActivityItem
import com.example.domain.model.TransactionType
import com.example.ui.theme.MoneyAdvanceColor
import com.example.ui.theme.MoneyDueColor
import com.example.ui.theme.MoneyEarnedColor
import com.example.ui.theme.MoneyExpenseColor
import com.example.ui.theme.MoneyInHandColor
import com.example.ui.theme.MoneyReceivedColor

@Composable
fun DashboardScreen(
    currentMonthSummary: MonthlyAccountingSummary,
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
            // Header: "হাজিরা হিসাব" & "আজ (২ অক্টোবর ২০২৬)" - Clean & Not Duplicated
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

            // Clean "আজকের দিনের হিসাব" Card (Today's activity only)
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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

            // Current Month Financial Cards (Row 1: মোট প্রাপ্য আয় & মোট পাওয়া টাকা)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "মোট প্রাপ্য আয়",
                        value = CurrencyFormatter.formatTaka(currentMonthSummary.monthEarnedPoisha, useBengaliDigits),
                        subtitle = CurrencyFormatter.formatAttendance(currentMonthSummary.monthAttendanceUnits, useBengaliDigits),
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        iconColor = MoneyEarnedColor,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_earned",
                        onClick = onNavigateToWork
                    )
                    MetricCard(
                        title = "মোট পাওয়া টাকা",
                        value = CurrencyFormatter.formatTaka(currentMonthSummary.monthReceivedPoisha, useBengaliDigits),
                        subtitle = "প্রাপ্ত নগদ টাকা",
                        icon = Icons.Default.AttachMoney,
                        iconColor = MoneyReceivedColor,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_received",
                        onClick = onNavigateToPayments
                    )
                }
            }

            // Current Month Financial Cards (Row 2: মোট বাকি পাওনা & মোট খরচ)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val dueTitle = if (currentMonthSummary.isMonthAdvance) "মোট অগ্রিম নেওয়া" else "মোট বাকি পাওনা"
                    val dueVal = if (currentMonthSummary.isMonthAdvance)
                        CurrencyFormatter.formatTaka(currentMonthSummary.monthAdvancePoisha, useBengaliDigits)
                    else
                        CurrencyFormatter.formatTaka(currentMonthSummary.monthUnpaidDuesPoisha, useBengaliDigits)
                    val dueColor = if (currentMonthSummary.isMonthAdvance) MoneyAdvanceColor else MoneyDueColor
                    val dueSubtitle = if (currentMonthSummary.isMonthAdvance) "অতিরিক্ত প্রাপ্তি" else "বকেয়া পারিশ্রমিক"

                    MetricCard(
                        title = dueTitle,
                        value = dueVal,
                        subtitle = dueSubtitle,
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        iconColor = dueColor,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_due",
                        onClick = onNavigateToPayments
                    )
                    MetricCard(
                        title = "মোট খরচ",
                        value = CurrencyFormatter.formatTaka(currentMonthSummary.monthExpensePoisha, useBengaliDigits),
                        subtitle = "খাবার: ${CurrencyFormatter.formatTaka(currentMonthSummary.monthFoodExpensePoisha, useBengaliDigits)}",
                        icon = Icons.Default.ShoppingCart,
                        iconColor = MoneyExpenseColor,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_expense",
                        onClick = onNavigateToExpenses
                    )
                }
            }

            // Ratio Progress Card (Current Month Earnings vs Received vs Remaining Dues)
            if (currentMonthSummary.monthEarnedPoisha > 0L || currentMonthSummary.monthReceivedPoisha > 0L) {
                item {
                    val recPercent = if (currentMonthSummary.monthEarnedPoisha > 0L) {
                        (currentMonthSummary.monthPaymentsForCurrentMonthPoisha.toFloat() / currentMonthSummary.monthEarnedPoisha.toFloat())
                    } else 0f

                    val dueStr = if (currentMonthSummary.isMonthAdvance) {
                        CurrencyFormatter.formatTaka(currentMonthSummary.monthAdvancePoisha, useBengaliDigits)
                    } else {
                        CurrencyFormatter.formatTaka(currentMonthSummary.monthUnpaidDuesPoisha, useBengaliDigits)
                    }

                    ComparisonProgressCard(
                        earnedAmountStr = CurrencyFormatter.formatTaka(currentMonthSummary.monthEarnedPoisha, useBengaliDigits),
                        receivedAmountStr = CurrencyFormatter.formatTaka(currentMonthSummary.monthPaymentsForCurrentMonthPoisha, useBengaliDigits),
                        dueOrAdvanceStr = dueStr,
                        isAdvance = currentMonthSummary.isMonthAdvance,
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
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
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
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ComparisonProgressCard(
    earnedAmountStr: String,
    receivedAmountStr: String,
    dueOrAdvanceStr: String,
    isAdvance: Boolean,
    receivedPercent: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    text = "আয় ও আদায়ের অনুপাত",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val pct = (receivedPercent * 100).toInt().coerceIn(0, 999)
                Text(
                    text = "${CurrencyFormatter.formatNumber(pct.toLong(), true)}% আদায়",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MoneyReceivedColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { receivedPercent.coerceIn(0f, 1f) },
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
                        text = "মোট আয়",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = earnedAmountStr,
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
                        text = receivedAmountStr,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MoneyReceivedColor
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isAdvance) "অগ্রিম" else "বাকি পাওনা",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dueOrAdvanceStr,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isAdvance) MoneyAdvanceColor else MoneyDueColor
                    )
                }
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
fun EmptyStateView(
    title: String,
    description: String,
    icon: ImageVector,
    actionButtonText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onActionClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(actionButtonText)
            }
        }
    }
}
