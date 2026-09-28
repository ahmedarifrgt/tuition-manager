package com.example.ui.students

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.Student
import com.example.data.model.TeacherProfile
import com.example.ui.TuitionViewModel
import com.example.ui.components.TuitionTopBar
import com.example.util.DateUtils

@Composable
fun AddEditStudentScreen(
    viewModel: TuitionViewModel,
    studentId: Long?,
    students: List<Student>,
    profile: TeacherProfile
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val existingStudent = remember(studentId, students) {
        if (studentId != null) students.find { it.id == studentId } else null
    }

    var fullName by remember { mutableStateOf(existingStudent?.fullName ?: "") }
    var studentCustomId by remember { mutableStateOf(existingStudent?.studentCustomId ?: "") }
    var gradeClass by remember { mutableStateOf(existingStudent?.gradeClass ?: "") }
    var section by remember { mutableStateOf(existingStudent?.section ?: "") }
    var rollNumber by remember { mutableStateOf(existingStudent?.rollNumber ?: "") }
    var schoolOrCollege by remember { mutableStateOf(existingStudent?.schoolOrCollege ?: "") }
    var subjects by remember { mutableStateOf(existingStudent?.subjects ?: "") }
    var dateOfBirth by remember { mutableStateOf(existingStudent?.dateOfBirth ?: "") }
    var gender by remember { mutableStateOf(existingStudent?.gender ?: "Male") }
    var parentName by remember { mutableStateOf(existingStudent?.parentName ?: "") }
    var parentRelationship by remember { mutableStateOf(existingStudent?.parentRelationship ?: "Father") }
    var parentPhone by remember { mutableStateOf(existingStudent?.parentPhone ?: "") }
    var altPhone by remember { mutableStateOf(existingStudent?.altPhone ?: "") }
    var email by remember { mutableStateOf(existingStudent?.email ?: "") }
    var address by remember { mutableStateOf(existingStudent?.address ?: "") }
    var admissionDate by remember { mutableStateOf(existingStudent?.admissionDate ?: DateUtils.getTodayIso()) }
    var monthlyFeeStr by remember {
        mutableStateOf(existingStudent?.monthlyFee?.toString() ?: profile.defaultMonthlyFee.toString())
    }
    var notes by remember { mutableStateOf(existingStudent?.notes ?: "") }
    var isActive by remember { mutableStateOf(existingStudent?.isActive ?: true) }

    // Validation errors
    var nameError by remember { mutableStateOf<String?>(null) }
    var parentPhoneError by remember { mutableStateOf<String?>(null) }
    var feeError by remember { mutableStateOf<String?>(null) }

    fun validateAndSave() {
        var isValid = true

        if (fullName.trim().isEmpty()) {
            nameError = "Student name is required"
            isValid = false
        } else {
            nameError = null
        }

        if (parentPhone.trim().isEmpty()) {
            parentPhoneError = "Parent phone number is required"
            isValid = false
        } else {
            parentPhoneError = null
        }

        val feeValue = monthlyFeeStr.toDoubleOrNull()
        if (feeValue == null || feeValue < 0.0) {
            feeError = "Enter a valid positive fee"
            isValid = false
        } else {
            feeError = null
        }

        if (isValid) {
            val studentToSave = Student(
                id = existingStudent?.id ?: 0L,
                studentCustomId = studentCustomId.trim(),
                fullName = fullName.trim(),
                dateOfBirth = dateOfBirth.trim(),
                gender = gender,
                schoolOrCollege = schoolOrCollege.trim(),
                gradeClass = gradeClass.trim(),
                section = section.trim(),
                rollNumber = rollNumber.trim(),
                subjects = subjects.trim(),
                address = address.trim(),
                parentName = parentName.trim(),
                parentRelationship = parentRelationship,
                parentPhone = parentPhone.trim(),
                altPhone = altPhone.trim(),
                email = email.trim(),
                admissionDate = admissionDate.trim(),
                monthlyFee = feeValue ?: 0.0,
                notes = notes.trim(),
                isActive = isActive
            )

            viewModel.saveStudent(studentToSave) {
                viewModel.navigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TuitionTopBar(
                title = if (existingStudent != null) "Edit Student" else "New Student Registration",
                onBack = { viewModel.navigateBack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Student Information Section Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Student Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            if (nameError != null) nameError = null
                        },
                        label = { Text("Full Name *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_student_name"),
                        isError = nameError != null,
                        supportingText = nameError?.let { { Text(it) } },
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = studentCustomId,
                            onValueChange = { studentCustomId = it },
                            label = { Text("Student ID (e.g. STU-1)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_student_id"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = rollNumber,
                            onValueChange = { rollNumber = it },
                            label = { Text("Roll No.") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_roll_number"),
                            singleLine = true
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = gradeClass,
                            onValueChange = { gradeClass = it },
                            label = { Text("Class / Grade") },
                            placeholder = { Text("e.g. Class 10") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = section,
                            onValueChange = { section = it },
                            label = { Text("Section") },
                            placeholder = { Text("A / Science") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = schoolOrCollege,
                        onValueChange = { schoolOrCollege = it },
                        label = { Text("School / College") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = subjects,
                        onValueChange = { subjects = it },
                        label = { Text("Subjects") },
                        placeholder = { Text("e.g. Mathematics, Higher Math, Physics") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Gender Selector
                    Column {
                        Text(
                            text = "Gender",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Male", "Female", "Other").forEach { g ->
                                FilterChip(
                                    selected = gender == g,
                                    onClick = { gender = g },
                                    label = { Text(g) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = dateOfBirth,
                        onValueChange = { dateOfBirth = it },
                        label = { Text("Date of Birth") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Parent & Contact Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Parent / Guardian Contact",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = parentName,
                        onValueChange = { parentName = it },
                        label = { Text("Parent / Guardian Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Relationship
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Father", "Mother", "Guardian").forEach { rel ->
                            FilterChip(
                                selected = parentRelationship == rel,
                                onClick = { parentRelationship = rel },
                                label = { Text(rel) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = parentPhone,
                        onValueChange = {
                            parentPhone = it
                            if (parentPhoneError != null) parentPhoneError = null
                        },
                        label = { Text("Parent Phone Number *") },
                        placeholder = { Text("e.g. 01712345678") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_parent_phone"),
                        isError = parentPhoneError != null,
                        supportingText = parentPhoneError?.let { { Text(it) } },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = altPhone,
                        onValueChange = { altPhone = it },
                        label = { Text("Alternative Phone") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Home Address") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }

            // Tuition Fees & Admission Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tuition & Admission",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = monthlyFeeStr,
                        onValueChange = {
                            monthlyFeeStr = it
                            if (feeError != null) feeError = null
                        },
                        label = { Text("Monthly Tuition Fee (${profile.currencySymbol}) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_monthly_fee"),
                        isError = feeError != null,
                        supportingText = feeError?.let { { Text(it) } },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = admissionDate,
                        onValueChange = { admissionDate = it },
                        label = { Text("Admission Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Remarks") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active Student Status", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            modifier = Modifier.testTag("switch_student_active")
                        )
                    }
                }
            }

            // Save Button
            Button(
                onClick = { validateAndSave() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_student"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (existingStudent != null) "Update Student" else "Save Student Record",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
