package com.example.ui.exams

import androidx.activity.compose.BackHandler
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
import com.example.data.model.Student
import com.example.ui.TuitionViewModel
import com.example.ui.components.EmptyState
import com.example.ui.components.TuitionTopBar
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.GreenSuccessContainer
import com.example.ui.theme.RedError
import com.example.ui.theme.RedErrorContainer
import com.example.util.Calculations

@Composable
fun RecordMarksScreen(
    viewModel: TuitionViewModel,
    examId: Long,
    exams: List<Exam>,
    students: List<Student>,
    examResults: List<ExamResult>
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val exam = remember(examId, exams) { exams.find { it.id == examId } }

    if (exam == null) {
        Scaffold(
            topBar = { TuitionTopBar(title = "Record Marks", onBack = { viewModel.navigateBack() }) }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Exam not found")
            }
        }
        return
    }

    val activeStudents = remember(students) { students.filter { it.isActive } }

    // Map studentId -> obtainedMarks string
    val marksMap = remember(examResults, activeStudents) {
        val existingForExam = examResults.filter { it.examId == examId }
        val map = mutableStateMapOf<Long, String>()
        activeStudents.forEach { s ->
            val prev = existingForExam.find { it.studentId == s.id }
            if (prev != null) {
                map[s.id] = if (prev.obtainedMarks % 1.0 == 0.0) prev.obtainedMarks.toInt().toString() else prev.obtainedMarks.toString()
            } else {
                map[s.id] = ""
            }
        }
        map
    }

    // Map studentId -> remarks string
    val remarksMap = remember(examResults, activeStudents) {
        val existingForExam = examResults.filter { it.examId == examId }
        val map = mutableStateMapOf<Long, String>()
        activeStudents.forEach { s ->
            val prev = existingForExam.find { it.studentId == s.id }
            map[s.id] = prev?.remarks ?: ""
        }
        map
    }

    fun saveAllMarks() {
        val resultsToSave = activeStudents.mapNotNull { student ->
            val markStr = marksMap[student.id] ?: ""
            val obtained = markStr.toDoubleOrNull() ?: return@mapNotNull null
            val percentage = Calculations.calculatePercentage(obtained, exam.totalMarks)
            val grade = Calculations.calculateGrade(percentage)
            val remarks = remarksMap[student.id] ?: ""

            ExamResult(
                examId = exam.id,
                studentId = student.id,
                obtainedMarks = obtained,
                totalMarks = exam.totalMarks,
                percentage = percentage,
                grade = grade,
                remarks = remarks
            )
        }

        viewModel.saveBatchExamResults(resultsToSave) {
            viewModel.navigateBack()
        }
    }

    Scaffold(
        topBar = {
            TuitionTopBar(
                title = "Marks: ${exam.title}",
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
                            onClick = { saveAllMarks() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_save_marks"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save All Exam Marks", fontWeight = FontWeight.Bold)
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
            // Header Exam Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(exam.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${exam.subject} • Class: ${exam.gradeClass.ifBlank { "All" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total: ${exam.totalMarks.toInt()} Marks", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Pass: ${exam.passingMarks.toInt()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (activeStudents.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.School,
                    title = "No active students",
                    description = "Register students before entering exam marks."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(activeStudents, key = { it.id }) { student ->
                        val markStr = marksMap[student.id] ?: ""
                        val markVal = markStr.toDoubleOrNull()
                        val percentage = if (markVal != null) Calculations.calculatePercentage(markVal, exam.totalMarks) else null
                        val grade = if (percentage != null) Calculations.calculateGrade(percentage) else null

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(student.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("Roll: ${student.rollNumber.ifBlank { "N/A" }} • ${student.gradeClass.ifBlank { "Class" }}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    if (grade != null && percentage != null) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (grade == "F") RedErrorContainer else GreenSuccessContainer
                                        ) {
                                            Text(
                                                text = "Grade $grade ($percentage%)",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (grade == "F") RedError else GreenSuccess,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = markStr,
                                        onValueChange = { marksMap[student.id] = it },
                                        label = { Text("Obtained Marks") },
                                        placeholder = { Text("e.g. 85") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f).testTag("input_marks_${student.id}"),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = remarksMap[student.id] ?: "",
                                        onValueChange = { remarksMap[student.id] = it },
                                        label = { Text("Remarks") },
                                        placeholder = { Text("Good / Needs practice") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
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
