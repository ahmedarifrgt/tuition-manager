package com.example.ui.students

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.Screen
import com.example.ui.TuitionViewModel
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyState
import com.example.ui.components.StatusChip
import com.example.ui.components.TuitionTopBar
import com.example.ui.theme.*
import com.example.util.ActionHelper
import com.example.util.Calculations
import com.example.util.DateUtils

@Composable
fun StudentDetailScreen(
    viewModel: TuitionViewModel,
    studentId: Long,
    students: List<Student>,
    classes: List<ClassSession>,
    attendance: List<AttendanceRecord>,
    payments: List<FeePayment>,
    exams: List<Exam>,
    examResults: List<ExamResult>,
    profile: TeacherProfile
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val student = remember(studentId, students) {
        students.find { it.id == studentId }
    }

    if (student == null) {
        Scaffold(
            topBar = { TuitionTopBar(title = "Student Details", onBack = { viewModel.navigateBack() }) }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Student not found")
            }
        }
        return
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Student specific records
    val studentAttendance = remember(attendance, studentId) {
        attendance.filter { it.studentId == studentId }
    }
    val studentPayments = remember(payments, studentId) {
        payments.filter { it.studentId == studentId }
    }
    val studentResults = remember(examResults, studentId) {
        examResults.filter { it.studentId == studentId }
    }

    // Calculations
    val totalAttendanceRecords = studentAttendance.size
    val presentCount = studentAttendance.count { it.status.equals("Present", ignoreCase = true) }
    val absentCount = studentAttendance.count { it.status.equals("Absent", ignoreCase = true) }
    val lateCount = studentAttendance.count { it.status.equals("Late", ignoreCase = true) }
    val attendancePct = Calculations.calculateAttendancePercentage(presentCount, totalAttendanceRecords)

    val totalPaid = studentPayments.sumOf { it.amountPaid }

    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            title = "Delete Student?",
            message = "Are you sure you want to delete ${student.fullName}? This will remove all their records.",
            onConfirm = {
                showDeleteConfirm = false
                viewModel.deleteStudent(student)
                viewModel.navigateBack()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    Scaffold(
        topBar = {
            TuitionTopBar(
                title = student.fullName,
                onBack = { viewModel.navigateBack() },
                actions = {
                    IconButton(onClick = { viewModel.toggleStudentActive(student) }) {
                        Icon(
                            imageVector = if (student.isActive) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                            contentDescription = "Toggle Active Status",
                            tint = if (student.isActive) GreenSuccess else SlateTextSecondary
                        )
                    }
                    IconButton(onClick = { viewModel.navigateTo(Screen.AddEditStudent(student.id)) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Student")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Student", tint = RedError)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Profile Header Card
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(if (student.isActive) NavyPrimary else MaterialTheme.colorScheme.outlineVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = student.fullName.take(2).uppercase(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = student.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (student.studentCustomId.isNotBlank()) {
                                Text(
                                    text = "ID: ${student.studentCustomId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "${student.gradeClass.ifBlank { "N/A" }} • ${student.schoolOrCollege.ifBlank { "Tuition Batch" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            StatusChip(status = if (student.isActive) "Active Student" else "Inactive")
                        }
                    }

                    // Parent Direct Contact Actions
                    if (student.parentPhone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${student.parentName.ifBlank { "Parent" }} (${student.parentRelationship})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = student.parentPhone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { ActionHelper.callNumber(context, student.parentPhone) },
                                    modifier = Modifier.size(36.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = GreenSuccessContainer)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call Parent", tint = GreenSuccess, modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = {
                                        val defaultMsg = "Dear ${student.parentName.ifBlank { "Parent" }}, updating you regarding ${student.fullName} at ${profile.tuitionName}."
                                        ActionHelper.sendSms(context, student.parentPhone, defaultMsg)
                                    },
                                    modifier = Modifier.size(36.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = TealContainer)
                                ) {
                                    Icon(Icons.Default.Message, contentDescription = "Send SMS", tint = TealSecondary, modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(student.parentPhone))
                                        Toast.makeText(context, "Phone copied", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(36.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = SlateSurfaceVariant)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Phone", tint = SlateTextSecondary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Tabs
            val tabs = listOf("Overview", "Attendance", "Fees", "Exams")
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> StudentOverviewTab(student, profile)
                1 -> StudentAttendanceTab(
                    studentAttendance = studentAttendance,
                    totalCount = totalAttendanceRecords,
                    presentCount = presentCount,
                    absentCount = absentCount,
                    lateCount = lateCount,
                    percentage = attendancePct
                )
                2 -> StudentFeesTab(
                    student = student,
                    payments = studentPayments,
                    totalPaid = totalPaid,
                    profile = profile,
                    onRecordPayment = { viewModel.navigateTo(Screen.Fees) }
                )
                3 -> StudentExamsTab(
                    studentResults = studentResults,
                    exams = exams
                )
            }
        }
    }
}

@Composable
fun StudentOverviewTab(student: Student, profile: TeacherProfile) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Personal Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    InfoRow("Full Name", student.fullName)
                    InfoRow("Student ID", student.studentCustomId.ifBlank { "N/A" })
                    InfoRow("Class / Grade", student.gradeClass.ifBlank { "N/A" })
                    InfoRow("Section", student.section.ifBlank { "N/A" })
                    InfoRow("Roll Number", student.rollNumber.ifBlank { "N/A" })
                    InfoRow("Gender", student.gender)
                    InfoRow("Date of Birth", student.dateOfBirth.ifBlank { "N/A" })
                    InfoRow("School / College", student.schoolOrCollege.ifBlank { "N/A" })
                    InfoRow("Subjects", student.subjects.ifBlank { "All standard subjects" })
                    InfoRow("Admission Date", student.admissionDate.ifBlank { "N/A" })
                    InfoRow("Monthly Fee", Calculations.formatCurrency(student.monthlyFee, profile.currencySymbol))
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Parent & Contact Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    InfoRow("Parent / Guardian", student.parentName.ifBlank { "N/A" })
                    InfoRow("Relationship", student.parentRelationship)
                    InfoRow("Primary Phone", student.parentPhone.ifBlank { "N/A" })
                    InfoRow("Alternative Phone", student.altPhone.ifBlank { "N/A" })
                    InfoRow("Email", student.email.ifBlank { "N/A" })
                    InfoRow("Home Address", student.address.ifBlank { "N/A" })
                }
            }
        }

        if (student.notes.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Teacher's Notes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(student.notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StudentAttendanceTab(
    studentAttendance: List<AttendanceRecord>,
    totalCount: Int,
    presentCount: Int,
    absentCount: Int,
    lateCount: Int,
    percentage: Double
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Attendance Stat Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Attendance Rate", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("$percentage%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (percentage >= 75) GreenSuccess else OrangeWarning)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = if (percentage >= 75) GreenSuccess else OrangeWarning,
                        trackColor = SlateSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total: $totalCount", style = MaterialTheme.typography.bodySmall)
                        Text("Present: $presentCount", style = MaterialTheme.typography.bodySmall, color = GreenSuccess)
                        Text("Absent: $absentCount", style = MaterialTheme.typography.bodySmall, color = RedError)
                        Text("Late: $lateCount", style = MaterialTheme.typography.bodySmall, color = OrangeWarning)
                    }
                }
            }
        }

        if (studentAttendance.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.EventBusy,
                    title = "No attendance records",
                    description = "Attendance records will show up when you take attendance for scheduled classes."
                )
            }
        } else {
            items(studentAttendance) { record ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(DateUtils.formatToDisplayDate(record.date), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            if (record.subject.isNotBlank()) {
                                Text(record.subject, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        StatusChip(status = record.status)
                    }
                }
            }
        }
    }
}

@Composable
fun StudentFeesTab(
    student: Student,
    payments: List<FeePayment>,
    totalPaid: Double,
    profile: TeacherProfile,
    onRecordPayment: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Monthly Tuition", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(Calculations.formatCurrency(student.monthlyFee, profile.currencySymbol), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Paid to Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(Calculations.formatCurrency(totalPaid, profile.currencySymbol), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GreenSuccess)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onRecordPayment,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Record Payment")
                    }
                }
            }
        }

        if (payments.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.ReceiptLong,
                    title = "No fee payments recorded",
                    description = "Recorded payments for this student will appear here."
                )
            }
        } else {
            items(payments) { payment ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(payment.billingMonth, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Method: ${payment.paymentMethod} • Date: ${DateUtils.formatToDisplayDate(payment.paymentDate)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (payment.remainingBalance > 0) {
                                Text("Due: ${Calculations.formatCurrency(payment.remainingBalance, profile.currencySymbol)}", style = MaterialTheme.typography.labelSmall, color = RedError)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(Calculations.formatCurrency(payment.amountPaid, profile.currencySymbol), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GreenSuccess)
                            StatusChip(status = payment.paymentStatus)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentExamsTab(
    studentResults: List<ExamResult>,
    exams: List<Exam>
) {
    if (studentResults.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Grade,
            title = "No exam marks recorded",
            description = "Marks for exams will appear here once entered."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(studentResults) { result ->
                val exam = exams.find { it.id == result.examId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(exam?.title ?: "Exam #${result.examId}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${exam?.subject ?: ""} • Date: ${exam?.let { DateUtils.formatToDisplayDate(it.date) } ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (result.remarks.isNotBlank()) {
                                Text("Remarks: ${result.remarks}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${result.obtainedMarks.toInt()} / ${result.totalMarks.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (result.grade == "F") RedErrorContainer else GreenSuccessContainer
                            ) {
                                Text(
                                    text = "Grade ${result.grade} (${result.percentage}%)",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (result.grade == "F") RedError else GreenSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
