package com.shifat.hajirahisab.ui.dialogs

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shifat.hajirahisab.data.local.entity.ExpenseEntity
import com.shifat.hajirahisab.domain.model.CurrencyFormatter
import com.shifat.hajirahisab.ui.theme.MoneyExpenseColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddEditExpenseDialog(
    initialExpense: ExpenseEntity? = null,
    useBengaliDigits: Boolean,
    onDismiss: () -> Unit,
    onSave: (date: String, amountInPoisha: Long, category: String, note: String, paymentSource: String) -> Unit
) {
    val context = LocalContext.current
    val today = LocalDate.now()

    var selectedDate by remember {
        mutableStateOf(initialExpense?.date ?: today.format(DateTimeFormatter.ISO_LOCAL_DATE))
    }
    var amountText by remember {
        mutableStateOf(
            if (initialExpense != null) CurrencyFormatter.poishaToTakaInput(initialExpense.amountInPoisha) else ""
        )
    }
    var category by remember {
        mutableStateOf(initialExpense?.category ?: "খাবার")
    }
    var paymentSource by remember {
        mutableStateOf(initialExpense?.paymentSource ?: ExpenseEntity.SOURCE_CASH)
    }
    var noteText by remember {
        mutableStateOf(initialExpense?.note ?: "")
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val datePickerDialog = remember {
        val parsedDate = try {
            LocalDate.parse(selectedDate)
        } catch (_: Exception) {
            today
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newDate = LocalDate.of(year, month + 1, dayOfMonth)
                selectedDate = newDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            },
            parsedDate.year,
            parsedDate.monthValue - 1,
            parsedDate.dayOfMonth
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_edit_expense_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MoneyExpenseColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = MoneyExpenseColor
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (initialExpense == null) "খরচের হিসাব" else "খরচ পরিবর্তন",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_expense_dialog_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Picker
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                        .testTag("expense_date_selector"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "খরচের তারিখ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.formatDateBengali(selectedDate, useBengaliDigits),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "তারিখ বাছাই",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount Input
                Text(
                    text = "টাকার পরিমাণ",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input"),
                    placeholder = { Text("১০০ বা ২৫০") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Text(
                            text = "৳",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MoneyExpenseColor,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                )

                // Quick amount chips
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("50", "100", "200", "500").forEach { quickAmount ->
                        val chipText = CurrencyFormatter.formatTaka(
                            quickAmount.toLong() * CurrencyFormatter.POISHA_PER_TAKA,
                            useBengaliDigits
                        )
                        FilterChip(
                            selected = amountText == quickAmount,
                            onClick = {
                                amountText = quickAmount
                                errorMessage = null
                            },
                            label = { Text(chipText) },
                            modifier = Modifier.testTag("quick_expense_$quickAmount")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selection
                Text(
                    text = "খরচের খাত / ক্যাটাগরি",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("খাবার", "যাতায়াত", "চিকিৎসা", "ব্যক্তিগত", "অন্যান্য").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            modifier = Modifier.testTag("chip_category_$cat")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Source (Normal cash expense vs Deducted from dues)
                Text(
                    text = "খরচের মাধ্যম",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = paymentSource == ExpenseEntity.SOURCE_CASH,
                        onClick = { paymentSource = ExpenseEntity.SOURCE_CASH },
                        label = { Text("ব্যক্তিগত খরচ") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chip_source_cash")
                    )
                    FilterChip(
                        selected = paymentSource == ExpenseEntity.SOURCE_FROM_DUES,
                        onClick = { paymentSource = ExpenseEntity.SOURCE_FROM_DUES },
                        label = { Text("পাওনা থেকে কাটা") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chip_source_dues")
                    )
                }
                Text(
                    text = if (paymentSource == ExpenseEntity.SOURCE_FROM_DUES)
                        "মালিক কাজের পাওনা টাকা থেকে এই খরচটি কেটে নিয়েছেন (বকেয়া কমে যাবে এবং মোট খরচে যুক্ত হবে)।"
                    else
                        "সাধারণ খরচ হিসেবে গণ্য হবে এবং মোট খরচে যুক্ত হবে।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Note
                Text(
                    text = "নোট বা বিবরণ (ঐচ্ছিক)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_note_input"),
                    placeholder = { Text("যেমন: সকালের নাস্তা বা বাস ভাড়া") },
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save button
                Button(
                    onClick = {
                        val poisha = CurrencyFormatter.takaToPoisha(amountText)
                        if (poisha <= 0) {
                            errorMessage = "দয়া করে খরচের সঠিক পরিমাণ লিখুন (০ এর বেশি হতে হবে)"
                            return@Button
                        }
                        onSave(selectedDate, poisha, category, noteText, paymentSource)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_expense_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (initialExpense == null) "খরচের হিসাব সংরক্ষণ" else "পরিবর্তন সংরক্ষণ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
