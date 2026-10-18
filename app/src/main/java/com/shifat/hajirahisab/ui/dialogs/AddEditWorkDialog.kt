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
import androidx.compose.material.icons.filled.Work
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
import com.shifat.hajirahisab.data.local.entity.WorkEntryEntity
import com.shifat.hajirahisab.domain.model.CurrencyFormatter
import com.shifat.hajirahisab.ui.theme.MoneyEarnedColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddEditWorkDialog(
    initialEntry: WorkEntryEntity? = null,
    defaultRatePoisha: Long,
    useBengaliDigits: Boolean,
    onDismiss: () -> Unit,
    onSave: (date: String, attendanceUnits: Int, rateInPoisha: Long, workType: String, note: String) -> Unit
) {
    val context = LocalContext.current
    val today = LocalDate.now()

    var selectedDate by remember {
        mutableStateOf(initialEntry?.date ?: today.format(DateTimeFormatter.ISO_LOCAL_DATE))
    }
    var selectedUnits by remember {
        mutableStateOf(initialEntry?.attendanceUnits ?: 1000)
    }
    var rateText by remember {
        mutableStateOf(
            if (initialEntry != null) CurrencyFormatter.poishaToTakaInput(initialEntry.rateInPoisha)
            else CurrencyFormatter.poishaToTakaInput(defaultRatePoisha)
        )
    }
    var workType by remember {
        mutableStateOf(initialEntry?.workType ?: "পূর্ণ দিনের কাজ")
    }
    var noteText by remember {
        mutableStateOf(initialEntry?.note ?: "")
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

    val currentRatePoisha = CurrencyFormatter.takaToPoisha(rateText)
    val calculatedEarnedPoisha = CurrencyFormatter.calculateEarnedPoisha(selectedUnits, currentRatePoisha)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_edit_work_dialog"),
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
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Work,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (initialEntry == null) "কাজের হাজিরা হিসাব" else "হাজিরা পরিবর্তন",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_work_dialog_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Picker Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                        .testTag("work_date_selector"),
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
                                text = "কাজের তারিখ",
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

                // Attendance Units Selection (হাজিরা পরিমাণ)
                Text(
                    text = "কাজের পরিমাণ (হাজিরা)",
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
                    val unitOptions = listOf(
                        Triple(1000, "১ হাজিরা", "পূর্ণ দিনের কাজ"),
                        Triple(500, "হাফ (০.৫)", "হাফ দিনের কাজ"),
                        Triple(1500, "দেড় (১.৫)", "দেড় দিনের কাজ"),
                        Triple(2000, "২ হাজিরা", "ডাবল হাজিরা")
                    )
                    unitOptions.forEach { (units, label, typeName) ->
                        FilterChip(
                            selected = selectedUnits == units,
                            onClick = {
                                selectedUnits = units
                                workType = typeName
                            },
                            label = { Text(label) },
                            modifier = Modifier.testTag("chip_units_$units")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Rate input
                Text(
                    text = "১ হাজিরার রেট বা মজুরি (টাকা)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = rateText,
                    onValueChange = {
                        rateText = it
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("work_rate_input"),
                    placeholder = { Text("১০০০") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Text(
                            text = "৳",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                )

                // Earned summary preview
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MoneyEarnedColor.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "আজকের মোট প্রাপ্য আয়:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = CurrencyFormatter.formatTaka(calculatedEarnedPoisha, useBengaliDigits),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MoneyEarnedColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Note
                Text(
                    text = "বিবরণ বা কাজের নোট (ঐচ্ছিক)",
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
                        .testTag("work_note_input"),
                    placeholder = { Text("যেমন: ঢালাইয়ের কাজ, সাইট-১") },
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
                        val ratePoisha = CurrencyFormatter.takaToPoisha(rateText)
                        if (ratePoisha <= 0) {
                            errorMessage = "দয়া করে সঠিক রেট লিখুন (০ এর বেশি হতে হবে)"
                            return@Button
                        }
                        onSave(selectedDate, selectedUnits, ratePoisha, workType, noteText)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_work_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (initialEntry == null) "কাজের হাজিরা সংরক্ষণ" else "পরিবর্তন সংরক্ষণ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
