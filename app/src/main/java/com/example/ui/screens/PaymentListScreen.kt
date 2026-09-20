package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.local.entity.PaymentEntity
import com.example.domain.model.CurrencyFormatter
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.MoneyAdvanceColor
import com.example.ui.theme.MoneyDueColor
import com.example.ui.theme.MoneyReceivedColor

@Composable
fun PaymentListScreen(
    payments: List<PaymentEntity>,
    totalEarnedPoisha: Long,
    deductedFromDuesPoisha: Long = 0L,
    useBengaliDigits: Boolean,
    onAddPaymentClick: () -> Unit,
    onEditPaymentClick: (PaymentEntity) -> Unit,
    onDeletePaymentClick: (PaymentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var itemToDelete by remember { mutableStateOf<PaymentEntity?>(null) }

    val filteredList = remember(payments, searchQuery) {
        if (searchQuery.isBlank()) {
            payments
        } else {
            payments.filter {
                it.date.contains(searchQuery, ignoreCase = true) ||
                        it.payer.contains(searchQuery, ignoreCase = true) ||
                        it.note.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val totalReceivedPoisha = filteredList.sumOf { it.amountInPoisha }
    val outstandingPoisha = totalEarnedPoisha - totalReceivedPoisha - deductedFromDuesPoisha
    val isAdvance = (totalReceivedPoisha + deductedFromDuesPoisha) > totalEarnedPoisha

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("payment_list_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Screen Header
            item {
                Column {
                    Text(
                        text = "টাকা পাওয়ার হিসাব",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "মালিক বা ঠিকাদারের নিকট হতে প্রাপ্ত নগদ টাকার লেজার",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Summary Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "মোট প্রাপ্ত টাকা",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF1565C0)
                            )
                            Text(
                                text = CurrencyFormatter.formatTaka(totalReceivedPoisha, useBengaliDigits),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val dueTitle = if (isAdvance) "অগ্রিম নেওয়া" else "বাকি পাওনা"
                            val dueAmount = if (isAdvance) (totalReceivedPoisha - totalEarnedPoisha) else outstandingPoisha
                            val dueColor = if (isAdvance) MoneyAdvanceColor else MoneyDueColor

                            Text(
                                text = dueTitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = dueColor
                            )
                            Text(
                                text = CurrencyFormatter.formatTaka(dueAmount, useBengaliDigits),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = dueColor
                            )
                        }
                    }
                }
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_search_input"),
                    placeholder = { Text("মালিকের নাম, তারিখ বা নোট দিয়ে খুঁজুন...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "অনুসন্ধান")
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }

            // List or empty state
            if (filteredList.isEmpty()) {
                item {
                    EmptyStateView(
                        title = if (searchQuery.isBlank()) "এখনও কোনো টাকা পাওয়ার হিসাব নেই" else "কোনো তথ্য পাওয়া যায়নি",
                        description = if (searchQuery.isBlank()) "মালিক বা কারো কাছ থেকে টাকা পেয়ে থাকলে নিচের বাটনে চাপ দিয়ে লিখে রাখুন।" else "অন্য কোনো শব্দ দিয়ে খুঁজুন।",
                        icon = Icons.Default.AttachMoney,
                        actionButtonText = if (searchQuery.isBlank()) "টাকা পাওয়া যোগ করুন" else null,
                        onActionClick = onAddPaymentClick
                    )
                }
            } else {
                items(filteredList, key = { it.id }) { payment ->
                    PaymentEntryCard(
                        payment = payment,
                        useBengaliDigits = useBengaliDigits,
                        onEdit = { onEditPaymentClick(payment) },
                        onDelete = { itemToDelete = payment }
                    )
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = onAddPaymentClick,
            containerColor = MoneyReceivedColor,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp)
                .testTag("fab_add_payment_screen")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "টাকা পাওয়া যোগ করুন")
        }
    }

    // Delete dialog
    if (itemToDelete != null) {
        ConfirmDeleteDialog(
            title = "টাকা পাওয়ার এই হিসাবটি মুছে ফেলতে চান?",
            message = "${CurrencyFormatter.formatDateBengali(itemToDelete!!.date, useBengaliDigits)} তারিখের ${CurrencyFormatter.formatTaka(itemToDelete!!.amountInPoisha, useBengaliDigits)} টাকা পাওয়ার এন্ট্রিটি মুছে ফেলা হবে।",
            onConfirm = {
                onDeletePaymentClick(itemToDelete!!)
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }
}

@Composable
fun PaymentEntryCard(
    payment: PaymentEntity,
    useBengaliDigits: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("payment_card_${payment.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3F2FD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = CurrencyFormatter.formatDateBengali(payment.date, useBengaliDigits),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (payment.payer.isNotBlank()) {
                            Text(
                                text = "মালিক: ${payment.payer}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_payment_btn_${payment.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "পরিবর্তন",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_payment_btn_${payment.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "মুছে ফেলুন",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F8E9))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "প্রাপ্ত পরিমাণ",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = CurrencyFormatter.formatTaka(payment.amountInPoisha, useBengaliDigits),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
            }

            if (payment.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "নোট: ${payment.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
