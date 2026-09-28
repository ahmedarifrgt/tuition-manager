package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.Screen
import com.example.ui.TuitionViewModel
import com.example.ui.about.AboutAuthorScreen
import com.example.ui.attendance.AttendanceScreen
import com.example.ui.attendance.MarkAttendanceScreen
import com.example.ui.calendar.CalendarScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.exams.ExamsScreen
import com.example.ui.exams.RecordMarksScreen
import com.example.ui.fees.DueRemindersScreen
import com.example.ui.fees.FeesScreen
import com.example.ui.reports.ReportsScreen
import com.example.ui.schedule.AddEditClassDialog
import com.example.ui.schedule.ScheduleScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.students.AddEditStudentScreen
import com.example.ui.students.StudentDetailScreen
import com.example.ui.students.StudentListScreen
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TealSecondary
import kotlinx.coroutines.launch

data class NavItem(
    val title: String,
    val icon: ImageVector,
    val screen: Screen,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(viewModel: TuitionViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val students by viewModel.allStudents.collectAsStateWithLifecycle()
    val classes by viewModel.allClasses.collectAsStateWithLifecycle()
    val attendance by viewModel.allAttendance.collectAsStateWithLifecycle()
    val payments by viewModel.allPayments.collectAsStateWithLifecycle()
    val exams by viewModel.allExams.collectAsStateWithLifecycle()
    val examResults by viewModel.allExamResults.collectAsStateWithLifecycle()
    val profile by viewModel.teacherProfile.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var showMoreSheet by remember { mutableStateOf(false) }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // Handle system back
    BackHandler(enabled = currentScreen !is Screen.Dashboard) {
        if (!viewModel.navigateBack()) {
            viewModel.navigateToRoot(Screen.Dashboard)
        }
    }

    val primaryNavItems = listOf(
        NavItem("Dashboard", Icons.Default.Dashboard, Screen.Dashboard, "nav_dashboard"),
        NavItem("Students", Icons.Default.People, Screen.Students, "nav_students"),
        NavItem("Schedule", Icons.Default.CalendarMonth, Screen.Schedule, "nav_schedule"),
        NavItem("Fees", Icons.Default.Payments, Screen.Fees, "nav_fees"),
        NavItem("More", Icons.Default.MoreHoriz, Screen.Dashboard, "nav_more")
    )

    val allNavItems = listOf(
        NavItem("Dashboard", Icons.Default.Dashboard, Screen.Dashboard, "drawer_dashboard"),
        NavItem("Students", Icons.Default.People, Screen.Students, "drawer_students"),
        NavItem("Schedule", Icons.Default.CalendarMonth, Screen.Schedule, "drawer_schedule"),
        NavItem("Attendance", Icons.Default.FactCheck, Screen.Attendance, "drawer_attendance"),
        NavItem("Fees & Billing", Icons.Default.Payments, Screen.Fees, "drawer_fees"),
        NavItem("Due Reminders", Icons.Default.NotificationsActive, Screen.DueReminders, "drawer_dues"),
        NavItem("Exams & Marks", Icons.Default.Grade, Screen.Exams, "drawer_exams"),
        NavItem("Reports & Invoices", Icons.Default.Assessment, Screen.Reports, "drawer_reports"),
        NavItem("Calendar", Icons.Default.Event, Screen.Calendar, "drawer_calendar"),
        NavItem("Settings", Icons.Default.Settings, Screen.Settings, "drawer_settings"),
        NavItem("About Author", Icons.Default.Person, Screen.AboutAuthor, "drawer_about_author")
    )

    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Tuition Management Tools",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                val moreGridItems = listOf(
                    Triple("Attendance", Icons.Default.FactCheck, Screen.Attendance),
                    Triple("Exams & Marks", Icons.Default.Grade, Screen.Exams),
                    Triple("Reports", Icons.Default.Assessment, Screen.Reports),
                    Triple("Due Reminders", Icons.Default.NotificationsActive, Screen.DueReminders),
                    Triple("Calendar", Icons.Default.Event, Screen.Calendar),
                    Triple("Settings", Icons.Default.Settings, Screen.Settings),
                    Triple("About Author", Icons.Default.Person, Screen.AboutAuthor)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    moreGridItems.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { (label, icon, target) ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            showMoreSheet = false
                                            viewModel.navigateTo(target)
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Tuition Header in Drawer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(24.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = profile.tuitionName.ifBlank { "Tuition Manager" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = profile.teacherName.ifBlank { "Private Tutor" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    allNavItems.forEach { item ->
                        val isSelected = currentScreen::class == item.screen::class
                        NavigationDrawerItem(
                            label = { Text(item.title) },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            selected = isSelected,
                            onClick = {
                                scope.launch { drawerState.close() }
                                viewModel.navigateTo(item.screen)
                            },
                            modifier = Modifier
                                .padding(NavigationDrawerItemDefaults.ItemPadding)
                                .testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) {
        val topTitle = when (currentScreen) {
            is Screen.Dashboard -> profile.tuitionName.ifBlank { "Tuition Manager" }
            is Screen.Students -> "Students"
            is Screen.StudentDetail -> "Student Profile"
            is Screen.AddEditStudent -> "Student Registration"
            is Screen.Schedule -> "Class Schedule"
            is Screen.AddEditClass -> "Schedule Class"
            is Screen.Attendance -> "Attendance"
            is Screen.MarkAttendance -> "Mark Attendance"
            is Screen.Fees -> "Monthly Fees & Payments"
            is Screen.DueReminders -> "Due Payment Reminders"
            is Screen.Exams -> "Exams & Tests"
            is Screen.ExamDetails -> "Exam Details"
            is Screen.RecordMarks -> "Exam Marks"
            is Screen.Reports -> "Reports & Invoices"
            is Screen.Calendar -> "Calendar"
            is Screen.Settings -> "Settings"
            is Screen.AboutAuthor -> "About Author"
        }

        val isRootScreen = currentScreen is Screen.Dashboard ||
                currentScreen is Screen.Students ||
                currentScreen is Screen.Schedule ||
                currentScreen is Screen.Fees ||
                currentScreen is Screen.Attendance ||
                currentScreen is Screen.Exams ||
                currentScreen is Screen.Reports ||
                currentScreen is Screen.Calendar ||
                currentScreen is Screen.Settings

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                // Top App Bar
                TopAppBar(
                    title = {
                        Text(
                            text = topTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        if (!isRootScreen) {
                            IconButton(onClick = { viewModel.navigateBack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    },
                    actions = {
                        if (currentScreen !is Screen.Settings) {
                            IconButton(onClick = { viewModel.navigateTo(Screen.Settings) }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    primaryNavItems.forEach { item ->
                        val isSelected = when (item.title) {
                            "Dashboard" -> currentScreen is Screen.Dashboard
                            "Students" -> currentScreen is Screen.Students || currentScreen is Screen.StudentDetail || currentScreen is Screen.AddEditStudent
                            "Schedule" -> currentScreen is Screen.Schedule || currentScreen is Screen.AddEditClass
                            "Fees" -> currentScreen is Screen.Fees || currentScreen is Screen.DueReminders
                            "More" -> false
                            else -> false
                        }

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (item.title == "More") {
                                    showMoreSheet = true
                                } else {
                                    viewModel.navigateToRoot(item.screen)
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (val screen = currentScreen) {
                    is Screen.Dashboard -> DashboardScreen(
                        viewModel = viewModel,
                        students = students,
                        classes = classes,
                        attendance = attendance,
                        payments = payments,
                        exams = exams,
                        profile = profile
                    )
                    is Screen.Students -> StudentListScreen(
                        viewModel = viewModel,
                        students = students,
                        profile = profile
                    )
                    is Screen.StudentDetail -> StudentDetailScreen(
                        viewModel = viewModel,
                        studentId = screen.studentId,
                        students = students,
                        classes = classes,
                        attendance = attendance,
                        payments = payments,
                        exams = exams,
                        examResults = examResults,
                        profile = profile
                    )
                    is Screen.AddEditStudent -> AddEditStudentScreen(
                        viewModel = viewModel,
                        studentId = screen.studentId,
                        students = students,
                        profile = profile
                    )
                    is Screen.Schedule -> ScheduleScreen(
                        viewModel = viewModel,
                        classes = classes,
                        students = students
                    )
                    is Screen.AddEditClass -> {
                        AddEditClassDialog(
                            existingClass = if (screen.classId != null) classes.find { it.id == screen.classId } else null,
                            students = students,
                            onDismiss = { viewModel.navigateBack() },
                            onSave = { session ->
                                viewModel.saveClass(session) {
                                    viewModel.navigateBack()
                                }
                            }
                        )
                    }
                    is Screen.Attendance -> AttendanceScreen(
                        viewModel = viewModel,
                        attendance = attendance,
                        students = students,
                        classes = classes
                    )
                    is Screen.MarkAttendance -> MarkAttendanceScreen(
                        viewModel = viewModel,
                        classId = screen.classId,
                        defaultDate = screen.date,
                        students = students,
                        classes = classes
                    )
                    is Screen.Fees -> FeesScreen(
                        viewModel = viewModel,
                        payments = payments,
                        students = students,
                        profile = profile
                    )
                    is Screen.DueReminders -> DueRemindersScreen(
                        viewModel = viewModel,
                        students = students,
                        payments = payments,
                        profile = profile
                    )
                    is Screen.Exams -> ExamsScreen(
                        viewModel = viewModel,
                        exams = exams,
                        examResults = examResults
                    )
                    is Screen.ExamDetails -> {
                        RecordMarksScreen(
                            viewModel = viewModel,
                            examId = screen.examId,
                            exams = exams,
                            students = students,
                            examResults = examResults
                        )
                    }
                    is Screen.RecordMarks -> RecordMarksScreen(
                        viewModel = viewModel,
                        examId = screen.examId,
                        exams = exams,
                        students = students,
                        examResults = examResults
                    )
                    is Screen.Reports -> ReportsScreen(
                        viewModel = viewModel,
                        students = students,
                        classes = classes,
                        attendance = attendance,
                        payments = payments,
                        exams = exams,
                        examResults = examResults,
                        profile = profile
                    )
                    is Screen.Calendar -> CalendarScreen(
                        viewModel = viewModel,
                        classes = classes,
                        exams = exams
                    )
                    is Screen.Settings -> SettingsScreen(
                        viewModel = viewModel,
                        profile = profile
                    )
                    is Screen.AboutAuthor -> AboutAuthorScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
