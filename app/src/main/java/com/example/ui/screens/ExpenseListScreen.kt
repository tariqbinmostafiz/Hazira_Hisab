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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.local.entity.ExpenseEntity
import com.example.domain.model.CurrencyFormatter
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.MoneyExpenseColor

@Composable
fun ExpenseListScreen(
    expenses: List<ExpenseEntity>,
    useBengaliDigits: Boolean,
    onAddExpenseClick: () -> Unit,
    onEditExpenseClick: (ExpenseEntity) -> Unit,
    onDeleteExpenseClick: (ExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("সব") }
    var itemToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    val filteredList = remember(expenses, searchQuery, selectedCategoryFilter) {
        expenses.filter { exp ->
            val matchesSearch = searchQuery.isBlank() ||
                    exp.date.contains(searchQuery, ignoreCase = true) ||
                    exp.category.contains(searchQuery, ignoreCase = true) ||
                    exp.note.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategoryFilter == "সব" || exp.category == selectedCategoryFilter

            matchesSearch && matchesCategory
        }
    }

    val totalExpensePoisha = filteredList.sumOf { it.amountInPoisha }
    val foodExpensePoisha = filteredList.filter { it.category == "খাবার" }.sumOf { it.amountInPoisha }
    val otherExpensePoisha = totalExpensePoisha - foodExpensePoisha

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("expense_list_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Screen Header
            item {
                Column {
                    Text(
                        text = "খরচের হিসাব",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "খাবার ও অন্যান্য ব্যক্তিগত ব্যয়ের হিসাব খাতা",
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
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "মোট খরচ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MoneyExpenseColor
                            )
                            Text(
                                text = CurrencyFormatter.formatTaka(totalExpensePoisha, useBengaliDigits),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MoneyExpenseColor
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "খাবার খরচ: ${CurrencyFormatter.formatTaka(foodExpensePoisha, useBengaliDigits)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "অন্যান্য: ${CurrencyFormatter.formatTaka(otherExpensePoisha, useBengaliDigits)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val categories = listOf("সব", "খাবার", "অন্যান্য", "যাতায়াত", "চিকিৎসা")
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat) },
                            modifier = Modifier.testTag("filter_cat_$cat")
                        )
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
                        .testTag("expense_search_input"),
                    placeholder = { Text("খরচের নাম, তারিখ বা বিবরণ দিয়ে খুঁজুন...") },
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
                        title = if (searchQuery.isBlank()) "এখনও কোনো খরচ যোগ করা হয়নি" else "কোনো খরচ পাওয়া যায়নি",
                        description = if (searchQuery.isBlank()) "খাবার বা অন্যান্য খরচ থাকলে নিচের বাটনে চাপ দিয়ে সংরক্ষণ করুন।" else "অন্য কোনো শব্দ দিয়ে খুঁজুন।",
                        icon = Icons.Default.ShoppingCart,
                        actionButtonText = if (searchQuery.isBlank()) "খরচ যোগ করুন" else null,
                        onActionClick = onAddExpenseClick
                    )
                }
            } else {
                items(filteredList, key = { it.id }) { expense ->
                    ExpenseEntryCard(
                        expense = expense,
                        useBengaliDigits = useBengaliDigits,
                        onEdit = { onEditExpenseClick(expense) },
                        onDelete = { itemToDelete = expense }
                    )
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = onAddExpenseClick,
            containerColor = MoneyExpenseColor,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp)
                .testTag("fab_add_expense_screen")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "খরচ যোগ করুন")
        }
    }

    // Delete dialog
    if (itemToDelete != null) {
        ConfirmDeleteDialog(
            title = "এই খরচের হিসাবটি মুছে ফেলতে চান?",
            message = "${CurrencyFormatter.formatDateBengali(itemToDelete!!.date, useBengaliDigits)} তারিখের ${itemToDelete!!.category} বাবদ ${CurrencyFormatter.formatTaka(itemToDelete!!.amountInPoisha, useBengaliDigits)} মুছে যাবে।",
            onConfirm = {
                onDeleteExpenseClick(itemToDelete!!)
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }
}

@Composable
fun ExpenseEntryCard(
    expense: ExpenseEntity,
    useBengaliDigits: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("expense_card_${expense.id}"),
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
                    val icon = when (expense.category) {
                        "খাবার" -> Icons.Default.Restaurant
                        "যাতায়াত" -> Icons.Default.DirectionsBus
                        "চিকিৎসা" -> Icons.Default.LocalHospital
                        else -> Icons.Default.ShoppingCart
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MoneyExpenseColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = CurrencyFormatter.formatDateBengali(expense.date, useBengaliDigits),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = expense.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_expense_btn_${expense.id}")
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
                        modifier = Modifier.testTag("delete_expense_btn_${expense.id}")
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
                    .background(Color(0xFFFFF3E0))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "খরচের পরিমাণ",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFE65100)
                )
                Text(
                    text = CurrencyFormatter.formatTaka(expense.amountInPoisha, useBengaliDigits),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MoneyExpenseColor
                )
            }

            if (expense.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "বিবরণ: ${expense.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
