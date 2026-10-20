package com.shifat.hajirahisab.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.core.content.FileProvider
import com.shifat.hajirahisab.domain.model.CurrencyFormatter
import com.shifat.hajirahisab.ui.components.ConfirmDeleteDialog
import com.shifat.hajirahisab.ui.theme.MoneyDueColor
import com.shifat.hajirahisab.ui.viewmodel.LedgerViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    viewModel: LedgerViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val useBengaliDigits by viewModel.useBengaliDigits.collectAsState()
    val defaultRate by viewModel.defaultRatePoisha.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    var showRateDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showBackupModal by remember { mutableStateOf(false) }

    var pendingBackupJson by remember { mutableStateOf<String?>(null) }

    // SAF Document Creator for saving JSON file to user-chosen location
    val saveDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null && pendingBackupJson != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(pendingBackupJson!!.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "ব্যাকআপ ফাইল সফলভাবে সেভ হয়েছে", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "ফাইল সেভ করতে সমস্যা হয়েছে: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // SAF Document Picker for Restoring backup from JSON file
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader(Charsets.UTF_8).readText()
                }
                if (!jsonString.isNullOrBlank()) {
                    coroutineScope.launch {
                        viewModel.restoreBackup(jsonString)
                    }
                } else {
                    Toast.makeText(context, "নির্বাচিত ফাইলটি খালি", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "ফাইল পড়া সম্ভব হয়নি: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Preferences Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "সাধারণ সেটিংস",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsItemRow(
                    icon = Icons.Default.AttachMoney,
                    title = "ডিফল্ট দৈনিক হাজিরার রেট",
                    subtitle = "বর্তমানে: ${CurrencyFormatter.formatTaka(defaultRate, useBengaliDigits)}",
                    onClick = { showRateDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "বাংলা সংখ্যা ব্যবহার",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (useBengaliDigits) "বাংলা (১, ২, ৩)" else "ইংরেজি (1, 2, 3)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = useBengaliDigits,
                        onCheckedChange = { viewModel.updateBengaliDigits(it) },
                        modifier = Modifier.testTag("bengali_digits_switch")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                SettingsItemRow(
                    icon = Icons.Default.Palette,
                    title = "থিম পছন্দ (Theme)",
                    subtitle = when (themeMode) {
                        "DARK" -> "ডার্ক থিম (Dark)"
                        "LIGHT" -> "লাইট থিম (Light)"
                        else -> "সিস্টেম অনুযায়ী (System Default)"
                    },
                    onClick = { showThemeDialog = true }
                )
            }
        }

        // Backup and Data Management Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "তথ্য ব্যাকআপ ও পুনরুদ্ধার",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsItemRow(
                    icon = Icons.Default.CloudUpload,
                    title = "হিসাবের ব্যাকআপ ফাইল তৈরি ও শেয়ার",
                    subtitle = "JSON ফাইল ডাউনলোড বা ফাইল হিসেবে শেয়ার করুন",
                    onClick = {
                        coroutineScope.launch {
                            val json = viewModel.getBackupJson()
                            pendingBackupJson = json
                            showBackupModal = true
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                SettingsItemRow(
                    icon = Icons.Default.CloudDownload,
                    title = "ব্যাকআপ ফাইল থেকে তথ্য পুনরুদ্ধার (Restore)",
                    subtitle = "পূর্বের সেভ করা .json ফাইল থেকে হিসাব আনুন",
                    onClick = {
                        openDocumentLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                SettingsItemRow(
                    icon = Icons.Default.DeleteForever,
                    title = "সকল হিসাবের তথ্য মুছে ফেলুন",
                    subtitle = "সমস্ত হাজিরা, আয় ও খরচের খাতা রিসেট করুন",
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = { showResetDialog = true }
                )
            }
        }
    }

    // Backup Options Dialog (Save File vs Share File)
    if (showBackupModal && pendingBackupJson != null) {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val defaultFileName = "hazira_hisab_backup_$todayStr.json"

        AlertDialog(
            onDismissRequest = { showBackupModal = false },
            title = {
                Text("হিসাবের ব্যাকআপ ফাইল", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "আপনার সমস্ত কাজের হাজিরা, টাকা পাওয়া এবং খরচের হিসাব নিয়ে একটি সম্পূর্ণ '.json' ব্যাকআপ ফাইল প্রস্তুত করা হয়েছে।",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "ফাইলের নাম: $defaultFileName",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBackupModal = false
                        saveDocumentLauncher.launch(defaultFileName)
                    },
                    modifier = Modifier.testTag("backup_save_btn")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ফাইল সেভ করুন")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showBackupModal = false
                        shareBackupFile(context, defaultFileName, pendingBackupJson!!)
                    },
                    modifier = Modifier.testTag("backup_share_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ফাইল শেয়ার করুন")
                }
            }
        )
    }

    // Default Rate Dialog
    if (showRateDialog) {
        var rateInput by remember {
            mutableStateOf((defaultRate / CurrencyFormatter.POISHA_PER_TAKA).toString())
        }

        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            title = { Text("দৈনিক হাজিরার রেট পরিবর্তন") },
            text = {
                Column {
                    Text("নতুন কাজের হাজিরা যোগ করার সময় এই রেটটি স্বয়ংক্রিয়ভাবে বসবে।")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rateInput,
                        onValueChange = { rateInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("দৈনিক রেট (টাকা)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val taka = rateInput.toLongOrNull() ?: 0L
                        if (taka > 0) {
                            viewModel.updateDefaultRate(taka * CurrencyFormatter.POISHA_PER_TAKA)
                        }
                        showRateDialog = false
                    }
                ) {
                    Text("সংরক্ষণ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRateDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Theme Picker Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("অ্যাপ থিম নির্বাচন করুন") },
            text = {
                Column {
                    listOf(
                        "SYSTEM" to "সিস্টেম অনুযায়ী (System Default)",
                        "DARK" to "ডার্ক মোড (Dark Theme)",
                        "LIGHT" to "লাইট মোড (Light Theme)"
                    ).forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    viewModel.updateThemeMode(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
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

    // Clear All Dialog
    if (showResetDialog) {
        ConfirmDeleteDialog(
            title = "সকল হিসাব মুছে ফেলবেন?",
            message = "সতর্কতা: আপনার সকল কাজের হাজিরা, টাকা পাওয়া এবং খরচের খাতা চিরতরে মুছে যাবে। আপনি কি নিশ্চিত?",
            confirmText = "হ্যাঁ, সব মুছুন",
            onConfirm = {
                viewModel.clearAllData()
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }
}

private fun shareBackupFile(context: Context, fileName: String, jsonContent: String) {
    try {
        val backupsDir = File(context.cacheDir, "backups")
        if (!backupsDir.exists()) {
            backupsDir.mkdirs()
        }
        val file = File(backupsDir, fileName)
        file.writeText(jsonContent, Charsets.UTF_8)

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "হাজিরা হিসাব ব্যাকআপ ফাইল")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(shareIntent, "ব্যাকআপ ফাইল শেয়ার করুন")
        )
    } catch (e: Exception) {
        Toast.makeText(context, "ফাইল শেয়ার করতে সমস্যা হয়েছে: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (titleColor == MaterialTheme.colorScheme.error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
