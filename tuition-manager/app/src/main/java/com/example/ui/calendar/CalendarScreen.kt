package com.example.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Class
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClassSession
import com.example.data.model.Exam
import com.example.ui.Screen
import com.example.ui.TuitionViewModel
import com.example.ui.components.EmptyState
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PurpleTag
import com.example.ui.theme.PurpleTagContainer
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealSecondary
import com.example.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: TuitionViewModel,
    classes: List<ClassSession>,
    exams: List<Exam>
) {
    val today = DateUtils.getTodayIso()
    var selectedDate by remember { mutableStateOf(today) }

    // Generate list of 30 days (previous 7 days to next 23 days)
    val dayList = remember {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -7)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (i in 0..30) {
            list.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val classesOnDay = remember(classes, selectedDate) {
        classes.filter { it.date == selectedDate }
    }

    val examsOnDay = remember(exams, selectedDate) {
        exams.filter { it.date == selectedDate }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Date selector strip
        Text(
            text = "Select Date",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dayNumFormat = SimpleDateFormat("d", Locale.getDefault())
        val parseFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(dayList) { dateStr ->
                val isSelected = dateStr == selectedDate
                val isCurrentToday = dateStr == today

                val parsedDate = try { parseFormat.parse(dateStr) } catch (e: Exception) { null }
                val dayOfWeek = if (parsedDate != null) dayFormat.format(parsedDate) else ""
                val dayNum = if (parsedDate != null) dayNumFormat.format(parsedDate) else ""

                // Indicators
                val hasClasses = classes.any { it.date == dateStr }
                val hasExams = exams.any { it.date == dateStr }

                Card(
                    modifier = Modifier
                        .width(58.dp)
                        .clickable { selectedDate = dateStr }
                        .testTag("calendar_day_$dateStr"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dayOfWeek.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dayNum,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Event dots
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            if (hasClasses) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else TealSecondary)
                                )
                            }
                            if (hasExams) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else PurpleTag)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Date Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = DateUtils.formatToDisplayDate(selectedDate),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (selectedDate == today) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Today",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (classesOnDay.isEmpty() && examsOnDay.isEmpty()) {
            EmptyState(
                icon = Icons.Default.CalendarMonth,
                title = "No events on this date",
                description = "No classes or exams scheduled for ${DateUtils.formatToDisplayDate(selectedDate)}.",
                actionText = "Schedule Class",
                onActionClick = { viewModel.navigateTo(Screen.AddEditClass()) }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (classesOnDay.isNotEmpty()) {
                    item {
                        Text("Classes (${classesOnDay.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TealSecondary)
                    }
                    items(classesOnDay) { session ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.navigateTo(Screen.MarkAttendance(session.id, session.date))
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(CircleShape).background(TealContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Class, contentDescription = null, tint = TealSecondary, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(session.subject, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(session.studentGroupName.ifBlank { "Batch" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            "${DateUtils.formatTo12HourTime(session.startTime)} - ${DateUtils.formatTo12HourTime(session.endTime)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Button(
                                    onClick = { viewModel.navigateTo(Screen.MarkAttendance(session.id, session.date)) },
                                    colors = ButtonDefaults.buttonColors(containerColor = TealSecondary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Attendance", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                if (examsOnDay.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Exams (${examsOnDay.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = PurpleTag)
                    }
                    items(examsOnDay) { exam ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.navigateTo(Screen.RecordMarks(exam.id)) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(CircleShape).background(PurpleTagContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Assignment, contentDescription = null, tint = PurpleTag, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(exam.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("${exam.subject} • ${exam.totalMarks.toInt()} Marks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Button(
                                    onClick = { viewModel.navigateTo(Screen.RecordMarks(exam.id)) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PurpleTag),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Marks", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
