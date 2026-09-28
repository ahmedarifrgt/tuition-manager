package com.example.ui.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.TeacherProfile
import com.example.ui.Screen
import com.example.ui.TuitionViewModel
import com.example.ui.theme.RedError
import com.example.util.ActionHelper
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: TuitionViewModel,
    profile: TeacherProfile
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var tuitionName by remember(profile) { mutableStateOf(profile.tuitionName) }
    var teacherName by remember(profile) { mutableStateOf(profile.teacherName) }
    var phone by remember(profile) { mutableStateOf(profile.phone) }
    var email by remember(profile) { mutableStateOf(profile.email) }
    var address by remember(profile) { mutableStateOf(profile.address) }
    var currencySymbol by remember(profile) { mutableStateOf(profile.currencySymbol) }
    var defaultFeeStr by remember(profile) { mutableStateOf(profile.defaultMonthlyFee.toString()) }
    var notifyClasses by remember(profile) { mutableStateOf(profile.notifyClasses) }
    var notifyFees by remember(profile) { mutableStateOf(profile.notifyFees) }
    var notifyExams by remember(profile) { mutableStateOf(profile.notifyExams) }

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore Backup Data", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Warning: Restoring backup data will replace your existing students, classes, and payment records.",
                        color = RedError,
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        label = { Text("Paste Backup JSON") },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        maxLines = 8
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreJsonInput.isNotBlank()) {
                            viewModel.restoreFromJson(restoreJsonInput) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                if (success) showRestoreDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedError)
                ) {
                    Text("Confirm Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Teacher / Tuition Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Teacher & Tuition Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = tuitionName,
                    onValueChange = { tuitionName = it },
                    label = { Text("Tuition / Coaching Name") },
                    modifier = Modifier.fillMaxWidth().testTag("input_settings_tuition_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = teacherName,
                    onValueChange = { teacherName = it },
                    label = { Text("Teacher's Full Name") },
                    modifier = Modifier.fillMaxWidth().testTag("input_settings_teacher_name"),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Contact Phone") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Center / Tuition Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // Currency & Financial Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Currency & Fee Defaults",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = currencySymbol,
                        onValueChange = { currencySymbol = it },
                        label = { Text("Currency Symbol") },
                        placeholder = { Text("৳ / $ / € / ₹") },
                        modifier = Modifier.weight(1f).testTag("input_settings_currency"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = defaultFeeStr,
                        onValueChange = { defaultFeeStr = it },
                        label = { Text("Default Monthly Fee") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_settings_default_fee"),
                        singleLine = true
                    )
                }

                Text(
                    text = "Quick Currency Presets:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("৳" to "BDT", "$" to "USD", "₹" to "INR", "£" to "GBP", "€" to "EUR").forEach { (sym, code) ->
                        FilterChip(
                            selected = currencySymbol == sym,
                            onClick = { currencySymbol = sym },
                            label = { Text("$sym ($code)") }
                        )
                    }
                }
            }
        }

        // Notification Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Reminders & Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Upcoming Class Alerts", style = MaterialTheme.typography.bodyMedium)
                        Text("Alert when a new class is scheduled", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = notifyClasses, onCheckedChange = { notifyClasses = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Tuition Fee Due Reminders", style = MaterialTheme.typography.bodyMedium)
                        Text("Enable due alerts for unpaid balances", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = notifyFees, onCheckedChange = { notifyFees = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Exam Alerts", style = MaterialTheme.typography.bodyMedium)
                        Text("Notifications for upcoming tests", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = notifyExams, onCheckedChange = { notifyExams = it })
                }
            }
        }

        // Backup & Restore
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Local Data Backup & Restore",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Your data stays 100% private on your device. Export a backup to save or transfer records safely.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                val json = viewModel.createBackupJson()
                                ActionHelper.shareText(
                                    context = context,
                                    subject = "Tuition Manager Backup JSON",
                                    body = json
                                )
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("btn_export_backup")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Backup")
                    }

                    OutlinedButton(
                        onClick = {
                            restoreJsonInput = ""
                            showRestoreDialog = true
                        },
                        modifier = Modifier.weight(1f).testTag("btn_restore_backup"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedError)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore Data")
                    }
                }
            }
        }

        // About Author / Developer Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateTo(Screen.AboutAuthor) }
                .testTag("btn_about_author_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "About Author",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "MD ARIF • Aspiring Software Engineer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "View Profile",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Save Settings Button
        Button(
            onClick = {
                val updatedProfile = profile.copy(
                    tuitionName = tuitionName.trim(),
                    teacherName = teacherName.trim(),
                    phone = phone.trim(),
                    email = email.trim(),
                    address = address.trim(),
                    currencySymbol = currencySymbol.trim(),
                    defaultMonthlyFee = defaultFeeStr.toDoubleOrNull() ?: 2000.0,
                    notifyClasses = notifyClasses,
                    notifyFees = notifyFees,
                    notifyExams = notifyExams
                )
                viewModel.updateTeacherProfile(updatedProfile)
                Toast.makeText(context, "Settings saved successfully", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_save_settings"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save All Settings", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
