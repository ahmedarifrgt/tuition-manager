package com.example.ui.fees

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.data.model.TeacherProfile
import com.example.ui.Screen
import com.example.ui.TuitionViewModel
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyState
import com.example.ui.components.StatusChip
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedError
import com.example.ui.theme.TealSecondary
import com.example.util.Calculations
import com.example.util.DateUtils

@Composable
fun FeesScreen(
    viewModel: TuitionViewModel,
    payments: List<FeePayment>,
    students: List<Student>,
    profile: TeacherProfile
) {
    var showRecordDialog by remember { mutableStateOf(false) }
    var selectedPaymentForReceipt by remember { mutableStateOf<FeePayment?>(null) }
    var paymentToDelete by remember { mutableStateOf<FeePayment?>(null) }
    var selectedMonthFilter by remember { mutableStateOf("All") }

    val currentMonth = DateUtils.getCurrentMonthYear()

    // Calculations
    val activeStudents = students.filter { it.isActive }
    val monthlyExpected = activeStudents.sumOf { it.monthlyFee }
    val currentMonthCollected = payments.filter { it.billingMonth == currentMonth }.sumOf { it.amountPaid }
    val totalCollectedAllTime = payments.sumOf { it.amountPaid }
    val totalDueCurrentMonth = maxOf(0.0, monthlyExpected - currentMonthCollected)

    val filteredPayments = remember(payments, selectedMonthFilter) {
        if (selectedMonthFilter == "All") payments
        else payments.filter { it.billingMonth == selectedMonthFilter }
    }

    if (showRecordDialog) {
        RecordPaymentDialog(
            students = students,
            profile = profile,
            onDismiss = { showRecordDialog = false },
            onSave = { payment ->
                viewModel.savePayment(payment) {
                    showRecordDialog = false
                }
            }
        )
    }

    if (selectedPaymentForReceipt != null) {
        val student = students.find { it.id == selectedPaymentForReceipt?.studentId }
        ReceiptDialog(
            payment = selectedPaymentForReceipt!!,
            student = student,
            profile = profile,
            onDismiss = { selectedPaymentForReceipt = null }
        )
    }

    if (paymentToDelete != null) {
        ConfirmDeleteDialog(
            title = "Delete Payment Record?",
            message = "Are you sure you want to delete this payment of ${Calculations.formatCurrency(paymentToDelete!!.amountPaid, profile.currencySymbol)}?",
            onConfirm = {
                paymentToDelete?.let { viewModel.deletePayment(it) }
                paymentToDelete = null
            },
            onDismiss = { paymentToDelete = null }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showRecordDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_record_payment")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Record Payment")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Financial Highlights Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fees Overview ($currentMonth)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(
                            onClick = { viewModel.navigateTo(Screen.DueReminders) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_due_reminders")
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp), tint = RedError)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Due Reminders", style = MaterialTheme.typography.labelMedium, color = RedError)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Expected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(Calculations.formatCurrency(monthlyExpected, profile.currencySymbol), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Collected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(Calculations.formatCurrency(currentMonthCollected, profile.currencySymbol), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GreenSuccess)
                        }
                        Column {
                            Text("Total Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(Calculations.formatCurrency(totalDueCurrentMonth, profile.currencySymbol), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (totalDueCurrentMonth > 0) RedError else GreenSuccess)
                        }
                    }
                }
            }

            // Month Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", currentMonth).forEach { month ->
                    FilterChip(
                        selected = selectedMonthFilter == month,
                        onClick = { selectedMonthFilter = month },
                        label = { Text(month) }
                    )
                }
            }

            if (payments.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Payments,
                    title = "No payment records",
                    description = "Payment records will appear here after you record a payment.",
                    actionText = "Record Payment",
                    onActionClick = { showRecordDialog = true }
                )
            } else if (filteredPayments.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.ReceiptLong,
                    title = "No payments for $selectedMonthFilter",
                    description = "Switch filter to 'All' to see all past records."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPayments, key = { it.id }) { payment ->
                        val student = students.find { it.id == payment.studentId }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("payment_card_${payment.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = student?.fullName ?: "Student #${payment.studentId}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${payment.billingMonth} • ${payment.paymentMethod}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Date: ${DateUtils.formatToDisplayDate(payment.paymentDate)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = Calculations.formatCurrency(payment.amountPaid, profile.currencySymbol),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = GreenSuccess
                                        )
                                        StatusChip(status = payment.paymentStatus)
                                    }
                                }

                                if (payment.remainingBalance > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Remaining Due: ${Calculations.formatCurrency(payment.remainingBalance, profile.currencySymbol)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RedError,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { selectedPaymentForReceipt = payment }) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Receipt / Invoice")
                                    }
                                    IconButton(onClick = { paymentToDelete = payment }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Payment", tint = RedError, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
