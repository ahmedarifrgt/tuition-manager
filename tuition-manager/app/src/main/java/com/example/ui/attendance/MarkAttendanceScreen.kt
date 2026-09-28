package com.example.ui.attendance

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.ClassSession
import com.example.data.model.Student
import com.example.ui.TuitionViewModel
import com.example.ui.components.EmptyState
import com.example.ui.components.TuitionTopBar
import com.example.ui.theme.*
import com.example.util.DateUtils

@Composable
fun MarkAttendanceScreen(
    viewModel: TuitionViewModel,
    classId: Long?,
    defaultDate: String,
    students: List<Student>,
    classes: List<ClassSession>
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val targetClass = remember(classId, classes) {
        if (classId != null) classes.find { it.id == classId } else null
    }

    var selectedDate by remember { mutableStateOf(targetClass?.date ?: defaultDate) }
    var subject by remember { mutableStateOf(targetClass?.subject ?: "") }

    val activeStudents = remember(students) {
        students.filter { it.isActive }
    }

    // Map studentId -> status (Present, Absent, Late, Excused)
    val statusMap = remember {
        mutableStateMapOf<Long, String>().apply {
            activeStudents.forEach { put(it.id, "Present") }
        }
    }

    // Map studentId -> note
    val noteMap = remember { mutableStateMapOf<Long, String>() }

    fun markAllPresent() {
        activeStudents.forEach { statusMap[it.id] = "Present" }
    }

    fun saveAttendance() {
        val records = activeStudents.map { student ->
            AttendanceRecord(
                classId = classId,
                studentId = student.id,
                date = selectedDate.trim(),
                subject = subject.trim(),
                status = statusMap[student.id] ?: "Present",
                notes = noteMap[student.id] ?: ""
            )
        }
        viewModel.saveBatchAttendance(records) {
            viewModel.navigateBack()
        }
    }

    Scaffold(
        topBar = {
            TuitionTopBar(
                title = "Mark Attendance",
                onBack = { viewModel.navigateBack() }
            )
        },
        bottomBar = {
            if (activeStudents.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = { saveAttendance() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_save_attendance"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Attendance (${activeStudents.size} Students)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Configuration Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedDate,
                            onValueChange = { selectedDate = it },
                            label = { Text("Date (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f).testTag("attendance_date_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("Subject / Topic") },
                            placeholder = { Text("e.g. Math") },
                            modifier = Modifier.weight(1f).testTag("attendance_subject_input"),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${activeStudents.size} Enrolled Active Students",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedButton(
                            onClick = { markAllPresent() },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark All Present")
                        }
                    }
                }
            }

            if (activeStudents.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.People,
                    title = "No active students",
                    description = "Add or activate students in the Students tab before taking attendance."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(activeStudents, key = { it.id }) { student ->
                        val currentStatus = statusMap[student.id] ?: "Present"
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(NavyPrimary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = student.fullName.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = NavyPrimary,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = student.fullName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${student.gradeClass.ifBlank { "Class" }} • Roll: ${student.rollNumber.ifBlank { "N/A" }}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Attendance Status Chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Present", "Absent", "Late", "Excused").forEach { status ->
                                        val isSelected = currentStatus == status
                                        val selectedColor = when (status) {
                                            "Present" -> GreenSuccess
                                            "Absent" -> RedError
                                            "Late" -> OrangeWarning
                                            else -> TealSecondary
                                        }

                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { statusMap[student.id] = status },
                                            label = { Text(status, fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = selectedColor.copy(alpha = 0.2f),
                                                selectedLabelColor = selectedColor
                                            ),
                                            border = if (isSelected) {
                                                FilterChipDefaults.filterChipBorder(
                                                    enabled = true,
                                                    selected = true,
                                                    borderColor = selectedColor,
                                                    selectedBorderColor = selectedColor
                                                )
                                            } else null
                                        )
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
