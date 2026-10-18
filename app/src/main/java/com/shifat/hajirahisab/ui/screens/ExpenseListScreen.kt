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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.shifat.hajirahisab.data.local.entity.ExpenseEntity
import com.shifat.hajirahisab.domain.model.CurrencyFormatter
import com.shifat.hajirahisab.ui.components.ConfirmDeleteDialog
import com.shifat.hajirahisab.ui.components.EmptyStateView
import com.shifat.hajirahisab.ui.dialogs.AddEditExpenseDialog
import com.shifat.hajirahisab.ui.theme.MoneyDueColor
import com.shifat.hajirahisab.ui.theme.MoneyExpenseColor
import com.shifat.hajirahisab.ui.viewmodel.LedgerViewModel

@Composable
fun ExpenseListScreen(
    viewModel: LedgerViewModel,
    modifier: Modifier = Modifier
) {
    val expenses by viewModel.expenses.collectAsState()
    val useBengaliDigits by viewModel.useBengaliDigits.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var deletingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    val totalExpense = expenses.sumOf { it.amountInPoisha }
    val foodExpense = expenses.filter { it.category == "খাবার" }.sumOf { it.amountInPoisha }
    val deductedFromDues = expenses.filter { it.isDeductedFromDues }.sumOf { it.amountInPoisha }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MoneyExpenseColor,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_expense_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "খরচ যোগ করুন")
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
                    .testTag("expense_list_header"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MoneyExpenseColor.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মোট খরচের হিসাব",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.formatTaka(totalExpense, useBengaliDigits),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MoneyExpenseColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "খাবার খরচ: ${CurrencyFormatter.formatTaka(foodExpense, useBengaliDigits)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (deductedFromDues > 0L) {
                            Text(
                                text = "বকেয়া কর্তন: ${CurrencyFormatter.formatTaka(deductedFromDues, useBengaliDigits)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MoneyDueColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            if (expenses.isEmpty()) {
                EmptyStateView(
                    title = "কোনো খরচের হিসাব নেই",
                    description = "দৈনন্দিন খাবার বা হাতখরচের হিসাব লিখে রাখতে নিচের + বাটনে ট্যাপ করুন।",
                    icon = Icons.Default.ShoppingCart,
                    actionButtonText = "+ খরচ যোগ করুন",
                    onActionClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("expense_entries_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(expenses, key = { it.id }) { expense ->
                        ExpenseEntryCard(
                            expense = expense,
                            useBengaliDigits = useBengaliDigits,
                            onEdit = { editingExpense = expense },
                            onDelete = { deletingExpense = expense }
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
        AddEditExpenseDialog(
            initialExpense = null,
            useBengaliDigits = useBengaliDigits,
            onDismiss = { showAddDialog = false },
            onSave = { date, amount, category, note, source ->
                viewModel.saveExpense(
                    date = date,
                    amountInPoisha = amount,
                    category = category,
                    note = note,
                    paymentSource = source
                )
                showAddDialog = false
            }
        )
    }

    if (editingExpense != null) {
        AddEditExpenseDialog(
            initialExpense = editingExpense,
            useBengaliDigits = useBengaliDigits,
            onDismiss = { editingExpense = null },
            onSave = { date, amount, category, note, source ->
                viewModel.saveExpense(
                    id = editingExpense!!.id,
                    date = date,
                    amountInPoisha = amount,
                    category = category,
                    note = note,
                    paymentSource = source
                )
                editingExpense = null
            }
        )
    }

    if (deletingExpense != null) {
        ConfirmDeleteDialog(
            title = "খরচের হিসাব মুছে ফেলুন",
            message = "আপনি কি নিশ্চিত যে ${CurrencyFormatter.formatTaka(deletingExpense!!.amountInPoisha, useBengaliDigits)} টাকার খরচের হিসাবটি মুছে ফেলতে চান?",
            onConfirm = {
                viewModel.deleteExpense(deletingExpense!!)
                deletingExpense = null
            },
            onDismiss = { deletingExpense = null }
        )
    }
}

@Composable
private fun ExpenseEntryCard(
    expense: ExpenseEntity,
    useBengaliDigits: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("expense_item_${expense.id}")
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
                        text = CurrencyFormatter.formatDateBengali(expense.date, useBengaliDigits),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = expense.category,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (expense.isDeductedFromDues) {
                        Surface(
                            color = MoneyDueColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "বকেয়া কর্তন",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MoneyDueColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (expense.note.isNotBlank()) expense.note else "দৈনন্দিন সাধারণ খরচ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "-${CurrencyFormatter.formatTaka(expense.amountInPoisha, useBengaliDigits)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MoneyExpenseColor
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
