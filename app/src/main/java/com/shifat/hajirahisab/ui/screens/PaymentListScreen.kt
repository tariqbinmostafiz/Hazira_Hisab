package com.shifat.hajirahisab.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifat.hajirahisab.data.local.entity.PaymentEntity
import com.shifat.hajirahisab.domain.model.CurrencyFormatter
import com.shifat.hajirahisab.ui.components.ConfirmDeleteDialog
import com.shifat.hajirahisab.ui.components.EmptyStateView
import com.shifat.hajirahisab.ui.dialogs.AddEditPaymentDialog
import com.shifat.hajirahisab.ui.theme.MoneyAdvanceColor
import com.shifat.hajirahisab.ui.theme.MoneyReceivedColor
import com.shifat.hajirahisab.ui.viewmodel.LedgerViewModel

@Composable
fun PaymentListScreen(
    viewModel: LedgerViewModel,
    modifier: Modifier = Modifier
) {
    val payments by viewModel.payments.collectAsState()
    val useBengaliDigits by viewModel.useBengaliDigits.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingPayment by remember { mutableStateOf<PaymentEntity?>(null) }
    var deletingPayment by remember { mutableStateOf<PaymentEntity?>(null) }

    val totalReceived = payments.sumOf { it.amountInPoisha }
    val totalForPreviousDues = payments.filter { it.isForPreviousDues }.sumOf { it.amountInPoisha }
    val totalForCurrentMonth = payments.filter { !it.isForPreviousDues }.sumOf { it.amountInPoisha }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MoneyReceivedColor,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_payment_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "টাকা পাওয়া যোগ করুন")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Header stats
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("payment_list_header"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MoneyReceivedColor.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মোট পাওয়া টাকা",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.formatTaka(totalReceived, useBengaliDigits),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MoneyReceivedColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "কাজের মজুরি: ${CurrencyFormatter.formatTaka(totalForCurrentMonth, useBengaliDigits)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "বকেয়া আদায়: ${CurrencyFormatter.formatTaka(totalForPreviousDues, useBengaliDigits)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MoneyAdvanceColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (payments.isEmpty()) {
                EmptyStateView(
                    title = "কোনো টাকা পাওয়ার তথ্য নেই",
                    description = "মালিক বা ঠিকাদার থেকে পাওয়া টাকা লিপিবদ্ধ করতে নিচের + বাটনে ট্যাপ করুন।",
                    icon = Icons.Default.AttachMoney,
                    actionButtonText = "+ টাকা পাওয়া যোগ করুন",
                    onActionClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("payment_entries_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(payments, key = { it.id }) { payment ->
                        PaymentEntryCard(
                            payment = payment,
                            useBengaliDigits = useBengaliDigits,
                            onEdit = { editingPayment = payment },
                            onDelete = { deletingPayment = payment }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditPaymentDialog(
            initialPayment = null,
            useBengaliDigits = useBengaliDigits,
            onDismiss = { showAddDialog = false },
            onSave = { date, amount, payer, note, paymentTarget ->
                viewModel.savePayment(
                    date = date,
                    amountInPoisha = amount,
                    payer = payer,
                    note = note,
                    paymentTarget = paymentTarget
                )
                showAddDialog = false
            }
        )
    }

    if (editingPayment != null) {
        AddEditPaymentDialog(
            initialPayment = editingPayment,
            useBengaliDigits = useBengaliDigits,
            onDismiss = { editingPayment = null },
            onSave = { date, amount, payer, note, paymentTarget ->
                viewModel.savePayment(
                    id = editingPayment!!.id,
                    date = date,
                    amountInPoisha = amount,
                    payer = payer,
                    note = note,
                    paymentTarget = paymentTarget
                )
                editingPayment = null
            }
        )
    }

    if (deletingPayment != null) {
        ConfirmDeleteDialog(
            title = "টাকা পাওয়ার হিসাব মুছে ফেলুন",
            message = "আপনি কি নিশ্চিত যে ${CurrencyFormatter.formatTaka(deletingPayment!!.amountInPoisha, useBengaliDigits)} টাকার হিসাবটি মুছে ফেলতে চান?",
            onConfirm = {
                viewModel.deletePayment(deletingPayment!!)
                deletingPayment = null
            },
            onDismiss = { deletingPayment = null }
        )
    }
}

@Composable
private fun PaymentEntryCard(
    payment: PaymentEntity,
    useBengaliDigits: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPreviousDue = payment.isForPreviousDues
    val badgeColor = if (isPreviousDue) MoneyAdvanceColor else MoneyReceivedColor
    val badgeText = if (isPreviousDue) "পূর্বের বকেয়া আদায়" else "চলতি কাজের মজুরি"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("payment_item_${payment.id}")
            .clickable { onEdit() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = CurrencyFormatter.formatDateBengali(payment.date, useBengaliDigits),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = badgeColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = badgeText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                val details = buildString {
                    if (payment.payer.isNotBlank()) append("প্রদানকারী: ${payment.payer}")
                    if (payment.note.isNotBlank()) {
                        if (isNotEmpty()) append(" • ")
                        append(payment.note)
                    }
                    if (isEmpty()) append("নগদ জমা")
                }

                Text(
                    text = details,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${CurrencyFormatter.formatTaka(payment.amountInPoisha, useBengaliDigits)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MoneyReceivedColor
                )

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "সম্পাদনা",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "মুছুন",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
