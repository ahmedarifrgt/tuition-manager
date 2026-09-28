package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.Screen
import com.example.ui.TuitionViewModel
import com.example.ui.components.EmptyState
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.components.StatusChip
import com.example.ui.theme.*
import com.example.util.Calculations
import com.example.util.DateUtils

@Composable
fun DashboardScreen(
    viewModel: TuitionViewModel,
    students: List<Student>,
    classes: List<ClassSession>,
    attendance: List<AttendanceRecord>,
    payments: List<FeePayment>,
    exams: List<Exam>,
    profile: TeacherProfile
) {
    val today = DateUtils.getTodayIso()
    val currentMonth = DateUtils.getCurrentMonthYear()

    // Calculations
    val totalStudentsCount = students.size
    val activeStudentsCount = students.count { it.isActive }

    val todayClasses = classes.filter { it.date == today }
    val todayAttendance = attendance.filter { it.date == today }

    // Fees calculation
    // Monthly expected fee is the sum of monthly fee for all active students
    val monthlyExpectedFees = students.filter { it.isActive }.sumOf { it.monthlyFee }
    // Monthly collected fee is sum of payments recorded for current billing month
    val monthlyCollectedFees = payments.filter { it.billingMonth == currentMonth }.sumOf { it.amountPaid }
    val totalDueFees = maxOf(0.0, monthlyExpectedFees - monthlyCollectedFees)

    val upcomingExams = exams.filter { it.date >= today }.sortedBy { it.date }
    val recentPayments = payments.take(5)
    val recentAttendance = attendance.take(5)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Welcome Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.tuitionName.ifBlank { "Tuition Manager" },
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Welcome, ${profile.teacherName.ifBlank { "Teacher" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Today: ${DateUtils.formatToDisplayDate(today)}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        // Quick Actions
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    QuickActionButton(
                        icon = Icons.Default.PersonAdd,
                        label = "Add Student",
                        color = NavyPrimary,
                        testTag = "quick_action_add_student"
                    ) {
                        viewModel.navigateTo(Screen.AddEditStudent())
                    }
                }
                item {
                    QuickActionButton(
                        icon = Icons.Default.FactCheck,
                        label = "Take Attendance",
                        color = TealSecondary,
                        testTag = "quick_action_take_attendance"
                    ) {
                        viewModel.navigateTo(Screen.Attendance)
                    }
                }
                item {
                    QuickActionButton(
                        icon = Icons.Default.CalendarMonth,
                        label = "Add Class",
                        color = AmberTertiary,
                        testTag = "quick_action_add_class"
                    ) {
                        viewModel.navigateTo(Screen.AddEditClass())
                    }
                }
                item {
                    QuickActionButton(
                        icon = Icons.Default.Payments,
                        label = "Record Payment",
                        color = GreenSuccess,
                        testTag = "quick_action_record_payment"
                    ) {
                        viewModel.navigateTo(Screen.Fees)
                    }
                }
                item {
                    QuickActionButton(
                        icon = Icons.Default.Grade,
                        label = "Add Exam",
                        color = PurpleTag,
                        testTag = "quick_action_add_exam"
                    ) {
                        viewModel.navigateTo(Screen.Exams)
                    }
                }
                item {
                    QuickActionButton(
                        icon = Icons.Default.Assessment,
                        label = "Generate Report",
                        color = NavyLight,
                        testTag = "quick_action_generate_report"
                    ) {
                        viewModel.navigateTo(Screen.Reports)
                    }
                }
            }
        }

        // Summary Overview Section
        item {
            Text(
                text = "Overview & Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Total Students",
                    value = totalStudentsCount.toString(),
                    subtitle = "$activeStudentsCount Active",
                    icon = Icons.Default.Groups,
                    accentColor = NavyPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.Students) }
                )
                StatCard(
                    title = "Today's Classes",
                    value = todayClasses.size.toString(),
                    subtitle = "${todayAttendance.size} Marked Today",
                    icon = Icons.Default.Schedule,
                    accentColor = TealSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.Schedule) }
                )
            }
        }

        // Financial Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = TealSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Monthly Fees: $currentMonth",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        TextButton(onClick = { viewModel.navigateTo(Screen.DueReminders) }) {
                            Text("Due Reminders")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Expected",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = Calculations.formatCurrency(monthlyExpectedFees, profile.currencySymbol),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                text = "Collected",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = Calculations.formatCurrency(monthlyCollectedFees, profile.currencySymbol),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GreenSuccess
                            )
                        }
                        Column {
                            Text(
                                text = "Total Due",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = Calculations.formatCurrency(totalDueFees, profile.currencySymbol),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (totalDueFees > 0) RedError else GreenSuccess
                            )
                        }
                    }
                    if (monthlyExpectedFees > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val progress = (monthlyCollectedFees / monthlyExpectedFees).toFloat().coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = GreenSuccess,
                            trackColor = SlateSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Today's Classes List
        item {
            SectionHeader(
                title = "Today's Schedule",
                actionText = "View All",
                onActionClick = { viewModel.navigateTo(Screen.Schedule) }
            )
            if (todayClasses.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.EventAvailable,
                    title = "No classes scheduled for today",
                    description = "Add a class to schedule your teaching session",
                    actionText = "Schedule Class",
                    onActionClick = { viewModel.navigateTo(Screen.AddEditClass()) }
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    todayClasses.forEach { session ->
                        ClassItemCard(
                            session = session,
                            onTakeAttendance = {
                                viewModel.navigateTo(Screen.MarkAttendance(session.id, session.date))
                            }
                        )
                    }
                }
            }
        }

        // Upcoming Exams Section
        item {
            SectionHeader(
                title = "Upcoming Exams",
                actionText = "Exams",
                onActionClick = { viewModel.navigateTo(Screen.Exams) }
            )
            if (upcomingExams.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Assignment,
                    title = "No upcoming exams",
                    description = "Create tests and exams to assess your students' learning",
                    actionText = "Create Exam",
                    onActionClick = { viewModel.navigateTo(Screen.Exams) }
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    upcomingExams.take(3).forEach { exam ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.navigateTo(Screen.ExamDetails(exam.id)) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = exam.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${exam.subject} • ${exam.gradeClass.ifBlank { "All" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Date: ${DateUtils.formatToDisplayDate(exam.date)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PurpleTagContainer
                                ) {
                                    Text(
                                        text = "${exam.totalMarks.toInt()} Marks",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PurpleTag,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Payments
        item {
            SectionHeader(
                title = "Recent Payments",
                actionText = "All Payments",
                onActionClick = { viewModel.navigateTo(Screen.Fees) }
            )
            if (recentPayments.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.ReceiptLong,
                    title = "No payment records",
                    description = "Payment records will appear here after you record a payment."
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentPayments.forEach { payment ->
                        val student = students.find { it.id == payment.studentId }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = student?.fullName ?: "Student #${payment.studentId}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${payment.billingMonth} • ${payment.paymentMethod}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Paid on ${DateUtils.formatToDisplayDate(payment.paymentDate)}",
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
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(105.dp)
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
        }
    }
}

@Composable
fun ClassItemCard(
    session: ClassSession,
    onTakeAttendance: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.subject.ifBlank { "Tuition Class" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (session.studentGroupName.isNotBlank()) {
                    Text(
                        text = session.studentGroupName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "${DateUtils.formatTo12HourTime(session.startTime)} - ${DateUtils.formatTo12HourTime(session.endTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (session.location.isNotBlank()) {
                    Text(
                        text = if (session.isOnline) "Online • ${session.location}" else session.location,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Button(
                onClick = onTakeAttendance,
                colors = ButtonDefaults.buttonColors(containerColor = TealSecondary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Attendance", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
