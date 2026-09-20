package com.example.ui.dialogs

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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.PaymentEntity
import com.example.domain.model.CurrencyFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddEditPaymentDialog(
    initialPayment: PaymentEntity? = null,
    useBengaliDigits: Boolean,
    onDismiss: () -> Unit,
    onSave: (date: String, amountInPoisha: Long, payer: String, note: String) -> Unit
) {
    val context = LocalContext.current
    val today = LocalDate.now()

    var selectedDate by remember {
        mutableStateOf(initialPayment?.date ?: today.format(DateTimeFormatter.ISO_LOCAL_DATE))
    }
    var amountText by remember {
        mutableStateOf(
            if (initialPayment != null) CurrencyFormatter.poishaToTakaInput(initialPayment.amountInPoisha) else ""
        )
    }
    var payerText by remember {
        mutableStateOf(initialPayment?.payer ?: "")
    }
    var noteText by remember {
        mutableStateOf(initialPayment?.note ?: "")
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
                .testTag("add_edit_payment_dialog"),
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
                                .background(Color(0xFFE3F2FD)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = Color(0xFF1565C0)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (initialPayment == null) "টাকা পাওয়ার হিসাব" else "টাকা পাওয়ার তথ্য পরিবর্তন",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_payment_dialog_btn")
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
                        .testTag("payment_date_selector"),
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
                                text = "টাকা প্রাপ্তির তারিখ",
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
                        .testTag("payment_amount_input"),
                    placeholder = { Text("১০০০ বা ২০০০") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Text(
                            text = "৳",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0),
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
                    listOf("280", "500", "1000", "5000").forEach { quickAmount ->
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
                            modifier = Modifier.testTag("quick_payment_$quickAmount")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payer / Employer Name
                Text(
                    text = "কার কাছ থেকে পেয়েছেন? (ঐচ্ছিক)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = payerText,
                    onValueChange = { payerText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_payer_input"),
                    placeholder = { Text("মালিক বা ঠিকাদারের নাম") },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Optional Note
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
                        .testTag("payment_note_input"),
                    placeholder = { Text("যেমন: সাপ্তাহিক কিস্তির নগদ টাকা") },
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
                            errorMessage = "দয়া করে টাকার সঠিক পরিমাণ লিখুন (০ এর বেশি হতে হবে)"
                            return@Button
                        }
                        onSave(selectedDate, poisha, payerText, noteText)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_payment_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (initialPayment == null) "টাকা পাওয়ার হিসাব সংরক্ষণ" else "পরিবর্তন সংরক্ষণ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
