package com.example.ui.exams

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Exam
import com.example.data.model.ExamResult
import com.example.ui.Screen
import com.example.ui.TuitionViewModel
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyState
import com.example.ui.theme.*
import com.example.util.DateUtils

@Composable
fun ExamsScreen(
    viewModel: TuitionViewModel,
    exams: List<Exam>,
    examResults: List<ExamResult>
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var examToDelete by remember { mutableStateOf<Exam?>(null) }
    var examToEdit by remember { mutableStateOf<Exam?>(null) }

    if (showAddDialog || examToEdit != null) {
        AddEditExamDialog(
            existingExam = examToEdit,
            onDismiss = {
                showAddDialog = false
                examToEdit = null
            },
            onSave = { exam ->
                viewModel.saveExam(exam) {
                    showAddDialog = false
                    examToEdit = null
                }
            }
        )
    }

    if (examToDelete != null) {
        ConfirmDeleteDialog(
            title = "Delete Exam?",
            message = "Are you sure you want to delete '${examToDelete?.title}'? All entered student marks for this exam will also be deleted.",
            onConfirm = {
                examToDelete?.let { viewModel.deleteExam(it) }
                examToDelete = null
            },
            onDismiss = { examToDelete = null }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_exam")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Exam")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (exams.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Assignment,
                    title = "No exams created yet",
                    description = "Create tests and examinations to evaluate student progress and track grades.",
                    actionText = "Create Exam",
                    onActionClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(exams, key = { it.id }) { exam ->
                        val resultsForExam = examResults.filter { it.examId == exam.id }
                        val hasResults = resultsForExam.isNotEmpty()

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.navigateTo(Screen.RecordMarks(exam.id)) }
                                .testTag("exam_card_${exam.id}"),
                            shape = RoundedCornerShape(16.dp),
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
                                            text = exam.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${exam.subject} • ${exam.gradeClass.ifBlank { "All Classes" }}",
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
                                            text = "Total: ${exam.totalMarks.toInt()} Marks",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PurpleTag,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (hasResults) "${resultsForExam.size} Students Evaluated" else "Marks Not Recorded",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (hasResults) GreenSuccess else OrangeWarning,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Passing: ${exam.passingMarks.toInt()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = { examToEdit = exam }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Exam", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { examToDelete = exam }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Exam", tint = RedError, modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    Button(
                                        onClick = { viewModel.navigateTo(Screen.RecordMarks(exam.id)) },
                                        colors = ButtonDefaults.buttonColors(containerColor = PurpleTag),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Grade, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (hasResults) "Edit Marks" else "Record Marks")
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
fun AddEditExamDialog(
    existingExam: Exam?,
    onDismiss: () -> Unit,
    onSave: (Exam) -> Unit
) {
    var title by remember { mutableStateOf(existingExam?.title ?: "") }
    var gradeClass by remember { mutableStateOf(existingExam?.gradeClass ?: "") }
    var subject by remember { mutableStateOf(existingExam?.subject ?: "") }
    var date by remember { mutableStateOf(existingExam?.date ?: DateUtils.getTodayIso()) }
    var totalMarksStr by remember { mutableStateOf(existingExam?.totalMarks?.toInt()?.toString() ?: "100") }
    var passingMarksStr by remember { mutableStateOf(existingExam?.passingMarks?.toInt()?.toString() ?: "33") }
    var notes by remember { mutableStateOf(existingExam?.notes ?: "") }

    var titleError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingExam != null) "Edit Exam" else "Create New Exam", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (titleError) titleError = false
                    },
                    label = { Text("Exam Title *") },
                    placeholder = { Text("e.g. Monthly Math Test 1") },
                    isError = titleError,
                    supportingText = if (titleError) { { Text("Title is required") } } else null,
                    modifier = Modifier.fillMaxWidth().testTag("input_exam_title"),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        placeholder = { Text("Math") },
                        modifier = Modifier.weight(1f).testTag("input_exam_subject"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = gradeClass,
                        onValueChange = { gradeClass = it },
                        label = { Text("Class") },
                        placeholder = { Text("Class 10") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_exam_date"),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalMarksStr,
                        onValueChange = { totalMarksStr = it },
                        label = { Text("Total Marks") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_total_marks"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = passingMarksStr,
                        onValueChange = { passingMarksStr = it },
                        label = { Text("Passing Marks") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_passing_marks"),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Remarks / Syllabus") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.trim().isEmpty()) {
                        titleError = true
                        return@Button
                    }
                    val exam = Exam(
                        id = existingExam?.id ?: 0L,
                        title = title.trim(),
                        gradeClass = gradeClass.trim(),
                        subject = subject.trim(),
                        date = date.trim(),
                        totalMarks = totalMarksStr.toDoubleOrNull() ?: 100.0,
                        passingMarks = passingMarksStr.toDoubleOrNull() ?: 33.0,
                        notes = notes.trim()
                    )
                    onSave(exam)
                },
                modifier = Modifier.testTag("btn_save_exam_dialog")
            ) {
                Text("Save Exam")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
