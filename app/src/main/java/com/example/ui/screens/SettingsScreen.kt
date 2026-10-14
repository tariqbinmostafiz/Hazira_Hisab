package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.core.content.FileProvider
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.model.CurrencyFormatter
import com.example.ui.components.ConfirmDeleteDialog
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    defaultRatePoisha: Long,
    themeMode: String,
    useBengaliDigits: Boolean,
    onUpdateDefaultRate: (Long) -> Unit,
    onUpdateThemeMode: (String) -> Unit,
    onUpdateBengaliDigits: (Boolean) -> Unit,
    onClearAllData: () -> Unit,
    onExportBackup: suspend () -> String,
    onRestoreBackup: suspend (String) -> Unit,
    onLoadDemoData: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showRateDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showBackupOptionsDialog by remember { mutableStateOf(false) }
    var pendingBackupJson by remember { mutableStateOf<String?>(null) }

    fun shareBackupAsFile(json: String) {
        try {
            val dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val fileName = "hazira_hisab_backup_$dateStr.json"
            val backupDir = File(context.cacheDir, "backups").apply { mkdirs() }
            val backupFile = File(backupDir, fileName)
            backupFile.writeText(json, Charsets.UTF_8)

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "হিসাবের ব্যাকআপ ফাইল শেয়ার করুন").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "ফাইল শেয়ার করতে সমস্যা হয়েছে: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // SAF Document Creator for saving JSON file to device/Downloads
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = pendingBackupJson ?: onExportBackup()
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(json.toByteArray(Charsets.UTF_8))
                        output.flush()
                    }
                    Toast.makeText(context, "হিসাবের ব্যাকআপ ফাইলটি সফলভাবে ডিভাইসে সংরক্ষিত হয়েছে", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "ফাইল সংরক্ষণ করতে ত্রুটি হয়েছে: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                } finally {
                    pendingBackupJson = null
                }
            }
        } else {
            pendingBackupJson = null
            Toast.makeText(context, "ফাইল সংরক্ষণ বাতিল করা হয়েছে", Toast.LENGTH_SHORT).show()
        }
    }

    // File picker for JSON restore
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val json = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                    if (json.isNotBlank()) {
                        onRestoreBackup(json)
                    }
                } catch (_: Exception) {
                    // Handled inside ViewModel
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "সেটিংস ও ব্যাকআপ",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "হাজিরার দর, রূপরেখা এবং ব্যক্তিগত তথ্যের সুরক্ষা",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section: Work Rate
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "কাজের মজুরি বা হাজিরার রেট",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsRowItem(
                        icon = Icons.Default.AttachMoney,
                        title = "১ হাজিরার ডিফল্ট মূল্য",
                        subtitle = "বর্তমান নির্ধারিত রেট: ${CurrencyFormatter.formatTaka(defaultRatePoisha, useBengaliDigits)}",
                        onClick = { showRateDialog = true },
                        testTag = "settings_rate_item"
                    )
                }
            }
        }

        // Section: Appearance & Localization
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "প্রদর্শন ও ভাষা",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val themeLabel = when (themeMode) {
                        "LIGHT" -> "লাইট মোড"
                        "DARK" -> "ডার্ক মোড"
                        else -> "সিস্টেম অনুযায়ী"
                    }
                    SettingsRowItem(
                        icon = Icons.Default.Brightness4,
                        title = "থিম ও রূপরেখা",
                        subtitle = themeLabel,
                        onClick = { showThemeDialog = true },
                        testTag = "settings_theme_item"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Numbers,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "বাংলা সংখ্যা ব্যবহার (০-৯)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (useBengaliDigits) "বাংলা সংখ্যা প্রদর্শিত হচ্ছে" else "ইংরেজি সংখ্যা (0-9) প্রদর্শিত হচ্ছে",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = useBengaliDigits,
                            onCheckedChange = { onUpdateBengaliDigits(it) },
                            modifier = Modifier.testTag("switch_bengali_digits")
                        )
                    }
                }
            }
        }

        // Section: Data Backup & Management
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ডাটা ব্যবস্থাপনা ও ব্যাকআপ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsRowItem(
                        icon = Icons.Default.Backup,
                        title = "হিসাবের ব্যাকআপ ফাইল তৈরি ও শেয়ার",
                        subtitle = "সমস্ত হিসাব সুরক্ষিত JSON ফাইল (.json) হিসেবে ডিভাইসে সংরক্ষণ বা হোয়াটসঅ্যাপে শেয়ার করুন",
                        onClick = { showBackupOptionsDialog = true },
                        testTag = "settings_backup_export"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsRowItem(
                        icon = Icons.Default.Restore,
                        title = "ব্যাকআপ থেকে ডাটা ফিরিয়ে আনুন",
                        subtitle = "পূর্বে সংরক্ষিত ব্যাকআপ ফাইল সিলেক্ট করে সমস্ত হিসাব পুনরুদ্ধার করুন",
                        onClick = { filePickerLauncher.launch("*/*") },
                        testTag = "settings_backup_import"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsRowItem(
                        icon = Icons.Default.DeleteForever,
                        title = "সমস্ত হিসাব মুছে ফেলুন (ডাটা রিসেট)",
                        subtitle = "ডাটাবেজের সকল হাজিরা, টাকা পাওয়া ও খরচের তথ্য সম্পূর্ণ ডিলিট হবে",
                        iconTint = MaterialTheme.colorScheme.error,
                        onClick = { showResetDialog = true },
                        testTag = "settings_reset_data"
                    )
                }
            }
        }

        // Section: About & Privacy
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "হাজিরা হিসাব (ভার্সন ১.১)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "এটি ১০০% অফলাইন ব্যক্তিগত খাতা। আপনার কোনো আর্থিক তথ্য বা কাজের হিসাব কোনো সার্ভারে পাঠানো হয় না। সকল হিসাব কেবলমাত্র আপনার এই মোবাইলেই সুরক্ষিত থাকে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Rate update dialog
    if (showRateDialog) {
        var inputRate by remember {
            mutableStateOf(CurrencyFormatter.poishaToTakaInput(defaultRatePoisha))
        }
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            title = {
                Text(
                    text = "১ হাজিরার ডিফল্ট মূল্য নির্ধারণ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "নতুন কাজ যোগ করার সময় এই রেটটি স্বয়ংক্রিয়ভাবে বসবে। তবে পুরনো কাজের রেট অপরিবর্তিত থাকবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputRate,
                        onValueChange = { inputRate = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_default_rate_input"),
                        leadingIcon = { Text("৳ ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val poisha = CurrencyFormatter.takaToPoisha(inputRate)
                        if (poisha > 0) {
                            onUpdateDefaultRate(poisha)
                        }
                        showRateDialog = false
                    },
                    modifier = Modifier.testTag("dialog_save_rate_btn")
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRateDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Theme selector dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Text(
                    text = "থিম নির্বাচন করুন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    listOf(
                        "SYSTEM" to "সিস্টেম অনুযায়ী (স্বয়ংক্রিয়)",
                        "LIGHT" to "লাইট মোড (সাদা)",
                        "DARK" to "ডার্ক মোড (কালো)"
                    ).forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdateThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    onUpdateThemeMode(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }

    // Reset confirmation dialog
    if (showResetDialog) {
        ConfirmDeleteDialog(
            title = "সকল হিসাব সম্পূর্ণ মুছে ফেলতে চান?",
            message = "সতর্কতা: এই কাজটি করলে আপনার সমস্ত কাজের হাজিরা, টাকা পাওয়া ও খরচের এন্ট্রি স্থায়ীভাবে মুছে যাবে।",
            confirmText = "সব মুছে ফেলুন",
            onConfirm = {
                onClearAllData()
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }

    // Backup Options Dialog (Save As File vs Share File)
    if (showBackupOptionsDialog) {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val defaultFileName = "hazira_hisab_backup_$todayStr.json"

        AlertDialog(
            onDismissRequest = { showBackupOptionsDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Backup,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "হিসাবের ব্যাকআপ ফাইল তৈরি",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "আপনার সকল কাজের হাজিরা, টাকা প্রাপ্তি ও খরচের সম্পূর্ণ হিসাব দিয়ে একটি প্রকৃত '.json' ব্যাকআপ ফাইল ($defaultFileName) তৈরি হবে।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "আপনি ফাইলটি নিজের মোবাইলে বা Downloads-এ সেভ করতে পারেন অথবা সরাসরি হোয়াটসঅ্যাপ, ড্রাইভ ইত্যাদিতে শেয়ার করতে পারেন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBackupOptionsDialog = false
                        scope.launch {
                            val json = onExportBackup()
                            shareBackupAsFile(json)
                        }
                    },
                    modifier = Modifier.testTag("confirm_share_backup_file_btn")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ফাইল শেয়ার")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { showBackupOptionsDialog = false }) {
                        Text("বাতিল")
                    }
                    OutlinedButton(
                        onClick = {
                            showBackupOptionsDialog = false
                            scope.launch {
                                val json = onExportBackup()
                                pendingBackupJson = json
                                createDocumentLauncher.launch(defaultFileName)
                            }
                        },
                        modifier = Modifier.testTag("confirm_save_backup_file_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ডিভাইসে সেভ")
                    }
                }
            }
        )
    }
}

@Composable
fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    testTag: String = "settings_row"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
