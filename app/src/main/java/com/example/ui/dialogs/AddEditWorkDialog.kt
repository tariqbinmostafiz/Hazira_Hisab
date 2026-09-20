package com.example.ui.dialogs

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Calculate
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.WorkEntryEntity
import com.example.domain.model.CurrencyFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
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
    var attendanceText by remember {
        mutableStateOf(
            if (initialEntry != null) CurrencyFormatter.unitsToAttendanceString(initialEntry.attendanceUnits) else "1"
        )
    }
    var rateText by remember {
        mutableStateOf(
            CurrencyFormatter.poishaToTakaInput(initialEntry?.rateInPoisha ?: defaultRatePoisha)
        )
    }
    var selectedWorkType by remember {
        mutableStateOf(initialEntry?.workType ?: "পূর্ণ দিনের কাজ")
    }
    var noteText by remember {
        mutableStateOf(initialEntry?.note ?: "")
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Live Calculation
    val currentUnits = CurrencyFormatter.attendanceToUnits(attendanceText)
    val currentRatePoisha = CurrencyFormatter.takaToPoisha(rateText)
    val calculatedEarnedPoisha = CurrencyFormatter.calculateEarnedPoisha(currentUnits, currentRatePoisha)

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
                            text = if (initialEntry == null) "নতুন কাজের হাজিরা" else "হাজিরার হিসাব পরিবর্তন",
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

                // Date Picker field
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

                // Attendance quantity
                Text(
                    text = "হাজিরা সংখ্যা (কত দিন/বেলা কাজ করেছেন?)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = attendanceText,
                    onValueChange = {
                        attendanceText = it
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("work_attendance_input"),
                    placeholder = { Text("১ বা ০.৫ বা ১.৫") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Text(
                            text = "হাজিরা",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                )

                // Quick Attendance Chips
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("0.5", "1", "1.5", "2").forEach { quickVal ->
                        val isSelected = attendanceText == quickVal
                        val chipLabel = when (quickVal) {
                            "0.5" -> "০.৫ (হাফ)"
                            "1" -> "১ (পূর্ণ)"
                            "1.5" -> "১.৫ দিন"
                            "2" -> "২ দিন"
                            else -> quickVal
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { attendanceText = quickVal },
                            label = { Text(chipLabel) },
                            modifier = Modifier.testTag("quick_chip_$quickVal")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Rate Input
                Text(
                    text = "১ হাজিরার রেট (টাকা)",
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

                Spacer(modifier = Modifier.height(14.dp))

                // Live Calculation Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "স্বয়ংক্রিয় হিসাব:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            val attFmt = CurrencyFormatter.formatAttendance(currentUnits, useBengaliDigits)
                            val rateFmt = CurrencyFormatter.formatTaka(currentRatePoisha, useBengaliDigits)
                            val earnedFmt = CurrencyFormatter.formatTaka(calculatedEarnedPoisha, useBengaliDigits)
                            Text(
                                text = "$attFmt × $rateFmt = $earnedFmt",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Work Type
                Text(
                    text = "কাজের ধরণ",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("পূর্ণ দিনের কাজ", "অর্ধ দিনের কাজ", "চুক্তির কাজ", "অন্যান্য").forEach { type ->
                        FilterChip(
                            selected = selectedWorkType == type,
                            onClick = { selectedWorkType = type },
                            label = { Text(type) },
                            modifier = Modifier.testTag("work_type_chip_$type")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Optional Note
                Text(
                    text = "বিবরণ বা নোট (ঐচ্ছিক)",
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
                    placeholder = { Text("যেমন: সকাল থেকে বিকাল অতিরিক্ত কাজ সহ") },
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
                        if (currentUnits <= 0) {
                            errorMessage = "দয়া করে হাজিরার সঠিক সংখ্যা লিখুন (০ এর বেশি হতে হবে)"
                            return@Button
                        }
                        if (currentRatePoisha < 0) {
                            errorMessage = "হাজিরার রেট সঠিক নয়"
                            return@Button
                        }
                        onSave(selectedDate, currentUnits, currentRatePoisha, selectedWorkType, noteText)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_work_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (initialEntry == null) "হিসাব সংরক্ষণ করুন" else "পরিবর্তন সংরক্ষণ করুন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
