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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifat.hajirahisab.data.local.entity.WorkEntryEntity
import com.shifat.hajirahisab.domain.model.CurrencyFormatter
import com.shifat.hajirahisab.ui.components.ConfirmDeleteDialog
import com.shifat.hajirahisab.ui.components.EmptyStateView
import com.shifat.hajirahisab.ui.dialogs.AddEditWorkDialog
import com.shifat.hajirahisab.ui.theme.MoneyEarnedColor
import com.shifat.hajirahisab.ui.viewmodel.LedgerViewModel

@Composable
fun WorkListScreen(
    viewModel: LedgerViewModel,
    modifier: Modifier = Modifier
) {
    val workEntries by viewModel.workEntries.collectAsState()
    val useBengaliDigits by viewModel.useBengaliDigits.collectAsState()
    val defaultRate by viewModel.defaultRatePoisha.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingWork by remember { mutableStateOf<WorkEntryEntity?>(null) }
    var deletingWork by remember { mutableStateOf<WorkEntryEntity?>(null) }

    val totalAttendance = workEntries.sumOf { it.attendanceUnits }
    val totalEarned = workEntries.sumOf { it.earnedInPoisha }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MoneyEarnedColor,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_work_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "হাজিরা যোগ করুন")
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
                    .testTag("work_list_header"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MoneyEarnedColor.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "মোট হাজিরা",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.formatAttendance(totalAttendance, useBengaliDigits),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MoneyEarnedColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "মোট কাজের আয়",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.formatTaka(totalEarned, useBengaliDigits),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MoneyEarnedColor
                        )
                    }
                }
            }

            if (workEntries.isEmpty()) {
                EmptyStateView(
                    title = "কোনো হাজিরার তথ্য নেই",
                    description = "নতুন কাজের হাজিরা যোগ করতে নিচের + বাটনে ট্যাপ করুন।",
                    icon = Icons.Default.Work,
                    actionButtonText = "+ হাজিরা যোগ করুন",
                    onActionClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("work_entries_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(workEntries, key = { it.id }) { work ->
                        WorkEntryCard(
                            work = work,
                            useBengaliDigits = useBengaliDigits,
                            onEdit = { editingWork = work },
                            onDelete = { deletingWork = work }
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
        AddEditWorkDialog(
            initialEntry = null,
            defaultRatePoisha = defaultRate,
            useBengaliDigits = useBengaliDigits,
            onDismiss = { showAddDialog = false },
            onSave = { date, units, rate, workType, note ->
                viewModel.saveWorkEntry(
                    date = date,
                    attendanceUnits = units,
                    rateInPoisha = rate,
                    workType = workType,
                    note = note
                )
                showAddDialog = false
            }
        )
    }

    if (editingWork != null) {
        AddEditWorkDialog(
            initialEntry = editingWork,
            defaultRatePoisha = defaultRate,
            useBengaliDigits = useBengaliDigits,
            onDismiss = { editingWork = null },
            onSave = { date, units, rate, workType, note ->
                viewModel.saveWorkEntry(
                    id = editingWork!!.id,
                    date = date,
                    attendanceUnits = units,
                    rateInPoisha = rate,
                    workType = workType,
                    note = note
                )
                editingWork = null
            }
        )
    }

    if (deletingWork != null) {
        ConfirmDeleteDialog(
            title = "হাজিরা মুছে ফেলুন",
            message = "আপনি কি নিশ্চিত যে ${CurrencyFormatter.formatShortDateBengali(deletingWork!!.date, useBengaliDigits)} তারিখের হাজিরা মুছে ফেলতে চান?",
            onConfirm = {
                viewModel.deleteWorkEntry(deletingWork!!)
                deletingWork = null
            },
            onDismiss = { deletingWork = null }
        )
    }
}

@Composable
private fun WorkEntryCard(
    work: WorkEntryEntity,
    useBengaliDigits: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("work_item_${work.id}")
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
                        text = CurrencyFormatter.formatDateBengali(work.date, useBengaliDigits),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = MoneyEarnedColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = work.workType,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MoneyEarnedColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${CurrencyFormatter.formatAttendance(work.attendanceUnits, useBengaliDigits)} • রেট: ${CurrencyFormatter.formatTaka(work.rateInPoisha, useBengaliDigits)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (work.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = work.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.formatTaka(work.earnedInPoisha, useBengaliDigits),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MoneyEarnedColor
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
