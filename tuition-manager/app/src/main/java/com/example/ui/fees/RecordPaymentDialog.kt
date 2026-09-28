package com.example.ui.fees

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.data.model.TeacherProfile
import com.example.ui.components.StatusChip
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedError
import com.example.util.ActionHelper
import com.example.util.Calculations
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentDialog(
    students: List<Student>,
    profile: TeacherProfile,
    onDismiss: () -> Unit,
    onSave: (FeePayment) -> Unit
) {
    if (students.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("No Students Available") },
            text = { Text("Please register students first before recording fee payments.") },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("OK") }
            }
        )
        return
    }

    var selectedStudentId by remember { mutableLongStateOf(students.first().id) }
    var expandedDropdown by remember { mutableStateOf(false) }

    val currentSelectedStudent = students.find { it.id == selectedStudentId } ?: students.first()

    var billingMonth by remember { mutableStateOf(DateUtils.getCurrentMonthYear()) }
    var monthlyFeeStr by remember(currentSelectedStudent) {
        mutableStateOf(currentSelectedStudent.monthlyFee.toString())
    }
    var amountPaidStr by remember(currentSelectedStudent) {
        mutableStateOf(currentSelectedStudent.monthlyFee.toString())
    }
    var paymentDate by remember { mutableStateOf(DateUtils.getTodayIso()) }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var receiptNumber by remember {
        mutableStateOf("RCP-${System.currentTimeMillis().toString().takeLast(6)}")
    }
    var notes by remember { mutableStateOf("") }

    val monthlyFeeVal = monthlyFeeStr.toDoubleOrNull() ?: 0.0
    val amountPaidVal = amountPaidStr.toDoubleOrNull() ?: 0.0
    val remainingBalance = maxOf(0.0, monthlyFeeVal - amountPaidVal)

    val paymentStatus = when {
        remainingBalance <= 0.0 && amountPaidVal > 0.0 -> "Paid"
        amountPaidVal > 0.0 && remainingBalance > 0.0 -> "Partially Paid"
        else -> "Due"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Record Fee Payment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Student Dropdown Picker
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = currentSelectedStudent.fullName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Student *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .testTag("select_student_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        students.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.fullName} (${s.gradeClass.ifBlank { "Class" }})") },
                                onClick = {
                                    selectedStudentId = s.id
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                // Billing Month
                OutlinedTextField(
                    value = billingMonth,
                    onValueChange = { billingMonth = it },
                    label = { Text("Billing Month") },
                    placeholder = { Text("e.g. September 2026") },
                    modifier = Modifier.fillMaxWidth().testTag("input_billing_month"),
                    singleLine = true
                )

                // Fee & Amount Paid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = monthlyFeeStr,
                        onValueChange = { monthlyFeeStr = it },
                        label = { Text("Fee (${profile.currencySymbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_payment_fee"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = amountPaidStr,
                        onValueChange = { amountPaidStr = it },
                        label = { Text("Paid (${profile.currencySymbol}) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_payment_paid"),
                        singleLine = true
                    )
                }

                // Live Balance & Status Preview
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Remaining Due:", style = MaterialTheme.typography.labelSmall)
                            Text(
                                Calculations.formatCurrency(remainingBalance, profile.currencySymbol),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (remainingBalance > 0) RedError else GreenSuccess
                            )
                        }
                        StatusChip(status = paymentStatus)
                    }
                }

                // Payment Date
                OutlinedTextField(
                    value = paymentDate,
                    onValueChange = { paymentDate = it },
                    label = { Text("Payment Date") },
                    modifier = Modifier.fillMaxWidth().testTag("input_payment_date"),
                    singleLine = true
                )

                // Payment Method
                Column {
                    Text("Payment Method", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Cash", "bKash", "Nagad", "Bank", "Other").forEach { method ->
                            FilterChip(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method },
                                label = { Text(method, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Receipt Number
                OutlinedTextField(
                    value = receiptNumber,
                    onValueChange = { receiptNumber = it },
                    label = { Text("Receipt / Ref. Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val payment = FeePayment(
                                studentId = selectedStudentId,
                                billingMonth = billingMonth.trim(),
                                monthlyFee = monthlyFeeVal,
                                amountPaid = amountPaidVal,
                                remainingBalance = remainingBalance,
                                paymentDate = paymentDate.trim(),
                                paymentMethod = paymentMethod,
                                paymentStatus = paymentStatus,
                                receiptNumber = receiptNumber.trim(),
                                notes = notes.trim()
                            )
                            onSave(payment)
                        },
                        modifier = Modifier.testTag("btn_confirm_payment")
                    ) {
                        Text("Save Payment")
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptDialog(
    payment: FeePayment,
    student: Student?,
    profile: TeacherProfile,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val receiptText = remember(payment, student, profile) {
        buildString {
            appendLine("===============================")
            appendLine("       ${profile.tuitionName.ifBlank { "TUITION CENTER" }}")
            appendLine("  Teacher: ${profile.teacherName}")
            if (profile.phone.isNotBlank()) appendLine("  Contact: ${profile.phone}")
            appendLine("===============================")
            appendLine("PAYMENT RECEIPT")
            appendLine("Receipt No: ${payment.receiptNumber.ifBlank { "RCP-${payment.id}" }}")
            appendLine("Date: ${DateUtils.formatToDisplayDate(payment.paymentDate)}")
            appendLine("-------------------------------")
            appendLine("Student: ${student?.fullName ?: "N/A"}")
            appendLine("Student ID: ${student?.studentCustomId ?: "N/A"}")
            appendLine("Class/Grade: ${student?.gradeClass ?: "N/A"}")
            appendLine("Parent: ${student?.parentName ?: "N/A"}")
            appendLine("-------------------------------")
            appendLine("Billing Month: ${payment.billingMonth}")
            appendLine("Monthly Fee: ${Calculations.formatCurrency(payment.monthlyFee, profile.currencySymbol)}")
            appendLine("Amount Paid: ${Calculations.formatCurrency(payment.amountPaid, profile.currencySymbol)}")
            appendLine("Remaining Due: ${Calculations.formatCurrency(payment.remainingBalance, profile.currencySymbol)}")
            appendLine("Payment Method: ${payment.paymentMethod}")
            appendLine("Status: ${payment.paymentStatus.uppercase()}")
            if (payment.notes.isNotBlank()) appendLine("Notes: ${payment.notes}")
            appendLine("===============================")
            appendLine("Thank you for your payment!")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Payment Receipt", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = receiptText,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 11.sp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                    Button(
                        onClick = {
                            ActionHelper.shareText(
                                context = context,
                                subject = "Receipt - ${payment.billingMonth} - ${student?.fullName}",
                                body = receiptText
                            )
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Receipt")
                    }
                }
            }
        }
    }
}
