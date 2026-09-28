package com.example.ui.attendance

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
import com.example.data.model.AttendanceRecord
import com.example.data.model.ClassSession
import com.example.data.model.Student
import com.example.ui.Screen
import com.example.ui.TuitionViewModel
import com.example.ui.components.EmptyState
import com.example.ui.components.StatCard
import com.example.ui.components.StatusChip
import com.example.ui.theme.*
import com.example.util.Calculations
import com.example.util.DateUtils

@Composable
fun AttendanceScreen(
    viewModel: TuitionViewModel,
    attendance: List<AttendanceRecord>,
    students: List<Student>,
    classes: List<ClassSession>
) {
    val today = DateUtils.getTodayIso()
    var selectedDateFilter by remember { mutableStateOf(today) }

    // Summary calculations
    val totalRecords = attendance.size
    val presentCount = attendance.count { it.status.equals("Present", ignoreCase = true) }
    val absentCount = attendance.count { it.status.equals("Absent", ignoreCase = true) }
    val lateCount = attendance.count { it.status.equals("Late", ignoreCase = true) }
    val overallPercentage = Calculations.calculateAttendancePercentage(presentCount, totalRecords)

    val dateFilteredAttendance = remember(attendance, selectedDateFilter) {
        if (selectedDateFilter.isBlank()) attendance
        else attendance.filter { it.date == selectedDateFilter }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(Screen.MarkAttendance()) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_mark_attendance")
            ) {
                Icon(Icons.Default.FactCheck, contentDescription = "Take Attendance")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Attendance %",
                    value = "$overallPercentage%",
                    icon = Icons.Default.Percent,
                    accentColor = GreenSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total Marked",
                    value = totalRecords.toString(),
                    icon = Icons.Default.Checklist,
                    accentColor = TealSecondary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick summary pill card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Present", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$presentCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GreenSuccess)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Absent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$absentCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RedError)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Late", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$lateCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OrangeWarning)
                    }
                }
            }

            // Date selector filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Date: ${DateUtils.formatToDisplayDate(selectedDateFilter)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { selectedDateFilter = today }) {
                        Text("Today")
                    }
                    TextButton(onClick = { selectedDateFilter = "" }) {
                        Text("Show All")
                    }
                }
            }

            if (attendance.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.FactCheck,
                    title = "No attendance recorded yet",
                    description = "Take attendance for your tuition classes to track student presence and calculate rates.",
                    actionText = "Take Attendance",
                    onActionClick = { viewModel.navigateTo(Screen.MarkAttendance()) }
                )
            } else if (dateFilteredAttendance.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.EventBusy,
                    title = "No attendance on ${DateUtils.formatToDisplayDate(selectedDateFilter)}",
                    description = "Tap below to take attendance for this day.",
                    actionText = "Take Attendance",
                    onActionClick = { viewModel.navigateTo(Screen.MarkAttendance(date = selectedDateFilter)) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dateFilteredAttendance, key = { it.id }) { record ->
                        val student = students.find { it.id == record.studentId }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = student?.fullName ?: "Student #${record.studentId}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    val subtitle = buildString {
                                        if (record.subject.isNotBlank()) append(record.subject)
                                        if (selectedDateFilter.isBlank()) {
                                            if (isNotEmpty()) append(" • ")
                                            append(DateUtils.formatToDisplayDate(record.date))
                                        }
                                    }
                                    if (subtitle.isNotBlank()) {
                                        Text(
                                            text = subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (record.notes.isNotBlank()) {
                                        Text(
                                            text = "Note: ${record.notes}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                StatusChip(status = record.status)
                            }
                        }
                    }
                }
            }
        }
    }
}
