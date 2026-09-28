package com.example.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.TuitionViewModel
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedError
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.theme.TealSecondary
import com.example.util.ActionHelper
import com.example.util.Calculations
import com.example.util.DateUtils

@Composable
fun ReportsScreen(
    viewModel: TuitionViewModel,
    students: List<Student>,
    classes: List<ClassSession>,
    attendance: List<AttendanceRecord>,
    payments: List<FeePayment>,
    exams: List<Exam>,
    examResults: List<ExamResult>,
    profile: TeacherProfile
) {
    val context = LocalContext.current
    var selectedReportType by remember { mutableIntStateOf(0) } // 0: Financial, 1: Attendance, 2: Students, 3: Exams
    val currentMonth = DateUtils.getCurrentMonthYear()

    // Calculations for Financial Report
    val activeStudents = students.filter { it.isActive }
    val expectedIncome = activeStudents.sumOf { it.monthlyFee }
    val currentMonthPaid = payments.filter { it.billingMonth == currentMonth }.sumOf { it.amountPaid }
    val allTimePaid = payments.sumOf { it.amountPaid }
    val currentMonthDue = maxOf(0.0, expectedIncome - currentMonthPaid)

    // Attendance calculations
    val totalAttendanceRecords = attendance.size
    val totalPresents = attendance.count { it.status.equals("Present", ignoreCase = true) }
    val overallAttendanceRate = Calculations.calculateAttendancePercentage(totalPresents, totalAttendanceRecords)

    val reportTitles = listOf("Financial", "Attendance", "Students", "Exams")

    fun generateReportText(): String {
        return buildString {
            appendLine("=========================================")
            appendLine("       ${profile.tuitionName.uppercase()}")
            appendLine("         ACADEMIC & FINANCIAL REPORT")
            appendLine(" Teacher: ${profile.teacherName} | Phone: ${profile.phone}")
            appendLine(" Generated: ${DateUtils.formatToDisplayDate(DateUtils.getTodayIso())}")
            appendLine("=========================================")

            when (selectedReportType) {
                0 -> {
                    appendLine("FINANCIAL SUMMARY ($currentMonth)")
                    appendLine("-----------------------------------------")
                    appendLine("Active Students: ${activeStudents.size}")
                    appendLine("Monthly Expected Fee: ${Calculations.formatCurrency(expectedIncome, profile.currencySymbol)}")
                    appendLine("Current Month Collected: ${Calculations.formatCurrency(currentMonthPaid, profile.currencySymbol)}")
                    appendLine("Current Month Pending Due: ${Calculations.formatCurrency(currentMonthDue, profile.currencySymbol)}")
                    appendLine("All-Time Total Collections: ${Calculations.formatCurrency(allTimePaid, profile.currencySymbol)}")
                    appendLine("-----------------------------------------")
                    appendLine("RECENT PAYMENT LOG:")
                    payments.take(15).forEach { p ->
                        val s = students.find { it.id == p.studentId }
                        appendLine(" • ${s?.fullName ?: "ID:${p.studentId}"} | ${p.billingMonth} | ${Calculations.formatCurrency(p.amountPaid, profile.currencySymbol)} | ${p.paymentStatus}")
                    }
                }
                1 -> {
                    appendLine("ATTENDANCE SUMMARY")
                    appendLine("-----------------------------------------")
                    appendLine("Total Attendance Sessions: $totalAttendanceRecords")
                    appendLine("Total Presents: $totalPresents")
                    appendLine("Overall Attendance Percentage: $overallAttendanceRate%")
                    appendLine("-----------------------------------------")
                    appendLine("STUDENT ATTENDANCE BREAKDOWN:")
                    students.forEach { s ->
                        val sAtt = attendance.filter { it.studentId == s.id }
                        val sPres = sAtt.count { it.status.equals("Present", ignoreCase = true) }
                        val sPct = Calculations.calculateAttendancePercentage(sPres, sAtt.size)
                        appendLine(" • ${s.fullName}: ${sAtt.size} classes, $sPres present ($sPct%)")
                    }
                }
                2 -> {
                    appendLine("STUDENT ENROLLMENT DIRECTORY")
                    appendLine("-----------------------------------------")
                    appendLine("Total Students: ${students.size}")
                    appendLine("Active Students: ${activeStudents.size}")
                    appendLine("Inactive Students: ${students.size - activeStudents.size}")
                    appendLine("-----------------------------------------")
                    students.forEach { s ->
                        appendLine(" • ${s.fullName} (${if (s.isActive) "Active" else "Inactive"})")
                        appendLine("   ID: ${s.studentCustomId.ifBlank { "N/A" }} | Class: ${s.gradeClass} | Roll: ${s.rollNumber}")
                        appendLine("   Parent: ${s.parentName} (${s.parentPhone})")
                        appendLine("   Monthly Fee: ${Calculations.formatCurrency(s.monthlyFee, profile.currencySymbol)}")
                    }
                }
                3 -> {
                    appendLine("EXAMINATIONS & MARKS SUMMARY")
                    appendLine("-----------------------------------------")
                    appendLine("Total Exams Created: ${exams.size}")
                    appendLine("Total Marks Recorded: ${examResults.size}")
                    appendLine("-----------------------------------------")
                    exams.forEach { e ->
                        appendLine("EXAM: ${e.title} (${e.subject}, Total: ${e.totalMarks.toInt()} Marks)")
                        val results = examResults.filter { it.examId == e.id }
                        if (results.isEmpty()) {
                            appendLine("  (No marks recorded yet)")
                        } else {
                            results.forEach { r ->
                                val s = students.find { it.id == r.studentId }
                                appendLine("  - ${s?.fullName ?: "Student"}: ${r.obtainedMarks.toInt()}/${r.totalMarks.toInt()} (${r.percentage}%, Grade: ${r.grade})")
                            }
                        }
                    }
                }
            }
            appendLine("=========================================")
            appendLine("End of Report")
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val reportText = generateReportText()
                    ActionHelper.shareText(
                        context = context,
                        subject = "${profile.tuitionName} - ${reportTitles[selectedReportType]} Report",
                        body = reportText
                    )
                },
                icon = { Icon(Icons.Default.Share, contentDescription = null) },
                text = { Text("Export & Share Report") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_export_report")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Report Category Tabs
            TabRow(
                selectedTabIndex = selectedReportType,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                reportTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedReportType == index,
                        onClick = { selectedReportType = index },
                        text = { Text(title, fontWeight = if (selectedReportType == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    // Summary highlight card based on category
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${reportTitles[selectedReportType]} Performance Overview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            when (selectedReportType) {
                                0 -> {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("Expected", style = MaterialTheme.typography.labelSmall)
                                            Text(Calculations.formatCurrency(expectedIncome, profile.currencySymbol), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        }
                                        Column {
                                            Text("Collected", style = MaterialTheme.typography.labelSmall)
                                            Text(Calculations.formatCurrency(currentMonthPaid, profile.currencySymbol), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GreenSuccess)
                                        }
                                        Column {
                                            Text("Pending Due", style = MaterialTheme.typography.labelSmall)
                                            Text(Calculations.formatCurrency(currentMonthDue, profile.currencySymbol), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (currentMonthDue > 0) RedError else GreenSuccess)
                                        }
                                    }
                                }
                                1 -> {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("Total Entries", style = MaterialTheme.typography.labelSmall)
                                            Text("$totalAttendanceRecords", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        }
                                        Column {
                                            Text("Presents", style = MaterialTheme.typography.labelSmall)
                                            Text("$totalPresents", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GreenSuccess)
                                        }
                                        Column {
                                            Text("Average Rate", style = MaterialTheme.typography.labelSmall)
                                            Text("$overallAttendanceRate%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TealSecondary)
                                        }
                                    }
                                }
                                2 -> {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("Total", style = MaterialTheme.typography.labelSmall)
                                            Text("${students.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        }
                                        Column {
                                            Text("Active", style = MaterialTheme.typography.labelSmall)
                                            Text("${activeStudents.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GreenSuccess)
                                        }
                                        Column {
                                            Text("Inactive", style = MaterialTheme.typography.labelSmall)
                                            Text("${students.size - activeStudents.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SlateTextSecondary)
                                        }
                                    }
                                }
                                3 -> {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("Total Exams", style = MaterialTheme.typography.labelSmall)
                                            Text("${exams.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        }
                                        Column {
                                            Text("Results Recorded", style = MaterialTheme.typography.labelSmall)
                                            Text("${examResults.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GreenSuccess)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Printable Preview Card
                item {
                    Text(
                        text = "Document Preview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = generateReportText(),
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
