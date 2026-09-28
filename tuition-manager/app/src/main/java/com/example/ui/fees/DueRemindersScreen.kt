package com.example.ui.fees

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.data.model.TeacherProfile
import com.example.ui.TuitionViewModel
import com.example.ui.components.EmptyState
import com.example.ui.components.TuitionTopBar
import com.example.ui.theme.*
import com.example.util.ActionHelper
import com.example.util.Calculations
import com.example.util.DateUtils

data class DueStudentItem(
    val student: Student,
    val amountDue: Double,
    val billingMonth: String,
    val status: String // "Unpaid", "Partial"
)

@Composable
fun DueRemindersScreen(
    viewModel: TuitionViewModel,
    students: List<Student>,
    payments: List<FeePayment>,
    profile: TeacherProfile
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentMonth = DateUtils.getCurrentMonthYear()

    // Calculate due items for active students
    val dueStudents = remember(students, payments, currentMonth) {
        val list = mutableListOf<DueStudentItem>()
        students.filter { it.isActive }.forEach { student ->
            // Check current month payments
            val monthPayments = payments.filter { it.studentId == student.id && it.billingMonth == currentMonth }
            val paidThisMonth = monthPayments.sumOf { it.amountPaid }
            val due = student.monthlyFee - paidThisMonth
            if (due > 0.0) {
                list.add(
                    DueStudentItem(
                        student = student,
                        amountDue = due,
                        billingMonth = currentMonth,
                        status = if (paidThisMonth > 0.0) "Partially Paid" else "Unpaid"
                    )
                )
            }
        }
        list
    }

    val totalOverdueAmount = dueStudents.sumOf { it.amountDue }

    Scaffold(
        topBar = {
            TuitionTopBar(
                title = "Due Fee Reminders",
                onBack = { viewModel.navigateBack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header summary card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
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
                            text = "Pending Dues: $currentMonth",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${dueStudents.size} students with pending balance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = Calculations.formatCurrency(totalOverdueAmount, profile.currencySymbol),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (totalOverdueAmount > 0) RedError else GreenSuccess
                    )
                }
            }

            if (dueStudents.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.CheckCircle,
                    title = "All tuition fees are clear!",
                    description = "No pending or overdue fees found for active students for $currentMonth."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(dueStudents, key = { it.student.id }) { item ->
                        val reminderMessage = remember(item, profile) {
                            "Dear ${item.student.parentName.ifBlank { "Parent" }}, this is a friendly reminder from ${profile.tuitionName} that the tuition fee of ${Calculations.formatCurrency(item.amountDue, profile.currencySymbol)} for ${item.student.fullName} (${item.billingMonth}) is currently due. Please make the payment at your convenience. Thank you."
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("due_student_card_${item.student.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.student.fullName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Parent: ${item.student.parentName.ifBlank { "N/A" }} (${item.student.parentPhone.ifBlank { "No phone" }})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Class: ${item.student.gradeClass.ifBlank { "N/A" }} • Status: ${item.status}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = RedError,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Due",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = Calculations.formatCurrency(item.amountDue, profile.currencySymbol),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = RedError
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = reminderMessage,
                                        modifier = Modifier.padding(10.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Action buttons: Call, SMS, Share, Copy
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (item.student.parentPhone.isNotBlank()) {
                                            IconButton(
                                                onClick = { ActionHelper.callNumber(context, item.student.parentPhone) },
                                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = GreenSuccessContainer),
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = "Call Parent", tint = GreenSuccess, modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(
                                                onClick = { ActionHelper.sendSms(context, item.student.parentPhone, reminderMessage) },
                                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = TealContainer),
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Default.Message, contentDescription = "Send SMS Reminder", tint = TealSecondary, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                ActionHelper.shareText(context, "Tuition Fee Due Reminder", reminderMessage)
                                            },
                                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = "Share Reminder", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(reminderMessage))
                                                Toast.makeText(context, "Reminder copied", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = SlateSurfaceVariant),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Reminder", tint = SlateTextSecondary, modifier = Modifier.size(18.dp))
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
}
