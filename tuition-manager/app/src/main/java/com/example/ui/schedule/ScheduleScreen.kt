package com.example.ui.schedule

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
import com.example.data.model.ClassSession
import com.example.data.model.Student
import com.example.ui.Screen
import com.example.ui.TuitionViewModel
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyState
import com.example.ui.components.StatusChip
import com.example.ui.theme.*
import com.example.util.DateUtils

@Composable
fun ScheduleScreen(
    viewModel: TuitionViewModel,
    classes: List<ClassSession>,
    students: List<Student>
) {
    var selectedFilter by remember { mutableStateOf("All") } // "Today", "Upcoming", "All", "Completed"
    var classToDelete by remember { mutableStateOf<ClassSession?>(null) }
    var classToEdit by remember { mutableStateOf<ClassSession?>(null) }

    val today = DateUtils.getTodayIso()

    val filteredClasses = remember(classes, selectedFilter) {
        when (selectedFilter) {
            "Today" -> classes.filter { it.date == today }
            "Upcoming" -> classes.filter { it.date >= today && it.status == "Scheduled" }
            "Completed" -> classes.filter { it.status == "Completed" }
            else -> classes
        }.sortedWith(compareBy({ it.date }, { it.startTime }))
    }

    if (classToDelete != null) {
        ConfirmDeleteDialog(
            title = "Delete Class?",
            message = "Are you sure you want to remove this scheduled class for ${classToDelete?.subject}?",
            onConfirm = {
                classToDelete?.let { viewModel.deleteClass(it) }
                classToDelete = null
            },
            onDismiss = { classToDelete = null }
        )
    }

    if (classToEdit != null) {
        AddEditClassDialog(
            existingClass = classToEdit,
            students = students,
            onDismiss = { classToEdit = null },
            onSave = { updated ->
                viewModel.saveClass(updated) {
                    classToEdit = null
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(Screen.AddEditClass()) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_class")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Class")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Today", "Upcoming", "Completed").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }

            if (classes.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.CalendarToday,
                    title = "No classes scheduled",
                    description = "Create a class schedule to see it here.",
                    actionText = "Schedule Class",
                    onActionClick = { viewModel.navigateTo(Screen.AddEditClass()) }
                )
            } else if (filteredClasses.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.EventBusy,
                    title = "No $selectedFilter classes found",
                    description = "Change your filter to view other scheduled classes."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredClasses, key = { it.id }) { session ->
                        val isToday = session.date == today
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("class_card_${session.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = session.subject.ifBlank { "Tuition Class" },
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (session.isOnline) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = TealContainer
                                                ) {
                                                    Text(
                                                        text = "Online",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = TealSecondary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        if (session.studentGroupName.isNotBlank()) {
                                            Text(
                                                text = "Batch/Student: ${session.studentGroupName}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = DateUtils.formatToDisplayDate(session.date),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${DateUtils.formatTo12HourTime(session.startTime)} - ${DateUtils.formatTo12HourTime(session.endTime)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (session.location.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = session.location,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    StatusChip(status = session.status)
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = { classToEdit = session }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Class", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                        }
                                        IconButton(onClick = { classToDelete = session }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Class", tint = RedError, modifier = Modifier.size(20.dp))
                                        }
                                        if (session.status != "Completed") {
                                            IconButton(onClick = { viewModel.updateClassStatus(session, "Completed") }) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = "Mark Completed", tint = GreenSuccess, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.navigateTo(Screen.MarkAttendance(session.id, session.date))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TealSecondary),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Attendance")
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

@Composable
fun AddEditClassDialog(
    existingClass: ClassSession?,
    students: List<Student>,
    onDismiss: () -> Unit,
    onSave: (ClassSession) -> Unit
) {
    var subject by remember { mutableStateOf(existingClass?.subject ?: "") }
    var studentGroupName by remember { mutableStateOf(existingClass?.studentGroupName ?: "") }
    var date by remember { mutableStateOf(existingClass?.date ?: DateUtils.getTodayIso()) }
    var startTime by remember { mutableStateOf(existingClass?.startTime ?: "10:00") }
    var endTime by remember { mutableStateOf(existingClass?.endTime ?: "11:30") }
    var location by remember { mutableStateOf(existingClass?.location ?: "") }
    var isOnline by remember { mutableStateOf(existingClass?.isOnline ?: false) }
    var notes by remember { mutableStateOf(existingClass?.notes ?: "") }
    var status by remember { mutableStateOf(existingClass?.status ?: "Scheduled") }

    var subjectError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingClass != null) "Edit Class Schedule" else "Schedule New Class", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = {
                        subject = it
                        if (subjectError) subjectError = false
                    },
                    label = { Text("Subject *") },
                    placeholder = { Text("e.g. Mathematics, Physics") },
                    isError = subjectError,
                    supportingText = if (subjectError) { { Text("Subject is required") } } else null,
                    modifier = Modifier.fillMaxWidth().testTag("input_class_subject"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = studentGroupName,
                    onValueChange = { studentGroupName = it },
                    label = { Text("Batch / Student Group") },
                    placeholder = { Text("e.g. Class 10 Batch A") },
                    modifier = Modifier.fillMaxWidth().testTag("input_class_group"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_class_date"),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        placeholder = { Text("10:00") },
                        modifier = Modifier.weight(1f).testTag("input_start_time"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        placeholder = { Text("11:30") },
                        modifier = Modifier.weight(1f).testTag("input_end_time"),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location / Room") },
                    placeholder = { Text("Home / Coaching Center") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Online Class", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isOnline, onCheckedChange = { isOnline = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subject.trim().isEmpty()) {
                        subjectError = true
                        return@Button
                    }
                    val session = ClassSession(
                        id = existingClass?.id ?: 0L,
                        subject = subject.trim(),
                        studentGroupName = studentGroupName.trim(),
                        date = date.trim(),
                        startTime = startTime.trim(),
                        endTime = endTime.trim(),
                        location = location.trim(),
                        isOnline = isOnline,
                        notes = notes.trim(),
                        status = status
                    )
                    onSave(session)
                },
                modifier = Modifier.testTag("btn_save_class_dialog")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
