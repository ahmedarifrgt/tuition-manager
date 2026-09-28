package com.example.ui.students

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.data.model.TeacherProfile
import com.example.ui.Screen
import com.example.ui.TuitionViewModel
import com.example.ui.components.EmptyState
import com.example.ui.components.StatusChip
import com.example.ui.theme.NavyPrimary
import com.example.util.Calculations

@Composable
fun StudentListScreen(
    viewModel: TuitionViewModel,
    students: List<Student>,
    profile: TeacherProfile
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // All, Active, Inactive
    var sortBy by remember { mutableStateOf("Name") } // Name, Roll, Admission Date, Fee

    // Filter and search
    val filteredStudents = remember(students, searchQuery, selectedFilter, sortBy) {
        var list = students.filter { student ->
            val matchesFilter = when (selectedFilter) {
                "Active" -> student.isActive
                "Inactive" -> !student.isActive
                else -> true
            }
            val q = searchQuery.trim().lowercase()
            val matchesSearch = q.isEmpty() ||
                    student.fullName.lowercase().contains(q) ||
                    student.studentCustomId.lowercase().contains(q) ||
                    student.parentName.lowercase().contains(q) ||
                    student.parentPhone.contains(q) ||
                    student.schoolOrCollege.lowercase().contains(q) ||
                    student.gradeClass.lowercase().contains(q)
            matchesFilter && matchesSearch
        }

        list = when (sortBy) {
            "Roll" -> list.sortedBy { it.rollNumber }
            "Fee" -> list.sortedByDescending { it.monthlyFee }
            "Admission Date" -> list.sortedByDescending { it.admissionDate }
            else -> list.sortedBy { it.fullName.lowercase() }
        }
        list
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(Screen.AddEditStudent()) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_student")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Student")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("student_search_input"),
                placeholder = { Text("Search by name, ID, phone, class...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Filter and Sort Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter chips
                listOf("All", "Active", "Inactive").forEach { filter ->
                    item {
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) }
                        )
                    }
                }
                item {
                    VerticalDivider(modifier = Modifier.height(24.dp))
                }
                // Sort options
                listOf("Name", "Roll", "Fee").forEach { sort ->
                    item {
                        FilterChip(
                            selected = sortBy == sort,
                            onClick = { sortBy = sort },
                            label = { Text("Sort: $sort") },
                            leadingIcon = if (sortBy == sort) {
                                { Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }

            if (students.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.School,
                    title = "No students added yet",
                    description = "Create your first student to start managing your tuition.",
                    actionText = "Add Student",
                    onActionClick = { viewModel.navigateTo(Screen.AddEditStudent()) }
                )
            } else if (filteredStudents.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.SearchOff,
                    title = "No students found",
                    description = "No students match your search or filter criteria."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        StudentCardItem(
                            student = student,
                            currencySymbol = profile.currencySymbol,
                            onClick = { viewModel.navigateTo(Screen.StudentDetail(student.id)) },
                            onToggleActive = { viewModel.toggleStudentActive(student) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentCardItem(
    student: Student,
    currencySymbol: String,
    onClick: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("student_card_${student.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Student Initials Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (student.isActive) NavyPrimary else MaterialTheme.colorScheme.outlineVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.fullName.take(2).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    StatusChip(status = if (student.isActive) "Active" else "Inactive")
                }
                Spacer(modifier = Modifier.height(2.dp))
                val gradeInfo = buildString {
                    if (student.gradeClass.isNotBlank()) append(student.gradeClass)
                    if (student.section.isNotBlank()) append(" (${student.section})")
                    if (student.rollNumber.isNotBlank()) append(" • Roll: ${student.rollNumber}")
                }
                if (gradeInfo.isNotBlank()) {
                    Text(
                        text = gradeInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (student.parentPhone.isNotBlank()) {
                    Text(
                        text = "Parent: ${student.parentPhone}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Calculations.formatCurrency(student.monthlyFee, currencySymbol),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "/month",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
