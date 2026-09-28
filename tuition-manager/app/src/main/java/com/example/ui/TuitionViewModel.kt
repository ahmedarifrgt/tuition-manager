package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.ClassSession
import com.example.data.model.Exam
import com.example.data.model.ExamResult
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.data.model.TeacherProfile
import com.example.data.repository.TuitionRepository
import com.example.util.BackupRestoreManager
import com.example.util.DateUtils
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Dashboard : Screen()
    object Students : Screen()
    data class StudentDetail(val studentId: Long) : Screen()
    data class AddEditStudent(val studentId: Long? = null) : Screen()
    object Schedule : Screen()
    data class AddEditClass(val classId: Long? = null) : Screen()
    object Attendance : Screen()
    data class MarkAttendance(val classId: Long? = null, val date: String = DateUtils.getTodayIso()) : Screen()
    object Fees : Screen()
    object DueReminders : Screen()
    object Exams : Screen()
    data class ExamDetails(val examId: Long) : Screen()
    data class RecordMarks(val examId: Long) : Screen()
    object Reports : Screen()
    object Calendar : Screen()
    object Settings : Screen()
    object AboutAuthor : Screen()
}

class TuitionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TuitionRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TuitionRepository(db.tuitionDao())
    }

    // Navigation BackStack
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Dashboard))
    val currentScreen: StateFlow<Screen> = MutableStateFlow<Screen>(Screen.Dashboard).also { flow ->
        viewModelScope.launch {
            _screenStack.collect { stack ->
                flow.value = stack.lastOrNull() ?: Screen.Dashboard
            }
        }
    }

    fun navigateTo(screen: Screen) {
        val current = _screenStack.value.toMutableList()
        // Prevent duplicate top
        if (current.lastOrNull() != screen) {
            current.add(screen)
            _screenStack.value = current
        }
    }

    fun navigateBack(): Boolean {
        val current = _screenStack.value.toMutableList()
        return if (current.size > 1) {
            current.removeAt(current.size - 1)
            _screenStack.value = current
            true
        } else {
            false
        }
    }

    fun navigateToRoot(screen: Screen = Screen.Dashboard) {
        _screenStack.value = listOf(screen)
    }

    // Data Flows
    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeStudents: StateFlow<List<Student>> = repository.activeStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClasses: StateFlow<List<ClassSession>> = repository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<AttendanceRecord>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<FeePayment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExams: StateFlow<List<Exam>> = repository.allExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExamResults: StateFlow<List<ExamResult>> = repository.allExamResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teacherProfile: StateFlow<TeacherProfile> = MutableStateFlow(
        TeacherProfile(
            tuitionName = "Tuition Center",
            teacherName = "Teacher",
            currencySymbol = "৳",
            defaultMonthlyFee = 2000.0
        )
    ).also { flow ->
        viewModelScope.launch {
            repository.profile.collect { p ->
                if (p != null) {
                    flow.value = p
                } else {
                    // Seed initial default profile if not set
                    val initial = TeacherProfile()
                    repository.saveProfile(initial)
                    flow.value = initial
                }
            }
        }
    }

    // Toast / Message snackbar
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    // Student CRUD
    fun saveStudent(student: Student, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (student.id == 0L) {
                repository.insertStudent(student)
                _userMessage.value = "Student '${student.fullName}' added"
            } else {
                repository.updateStudent(student)
                _userMessage.value = "Student updated"
            }
            onComplete()
        }
    }

    fun toggleStudentActive(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student.copy(isActive = !student.isActive))
            _userMessage.value = if (student.isActive) "Student marked inactive" else "Student marked active"
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            _userMessage.value = "Student deleted"
        }
    }

    // Class Session CRUD
    fun saveClass(session: ClassSession, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (session.id == 0L) {
                repository.insertClass(session)
                _userMessage.value = "Class scheduled"
                // Local notification trigger if enabled
                if (teacherProfile.value.notifyClasses) {
                    NotificationHelper.showNotification(
                        getApplication(),
                        "New Class Scheduled",
                        "${session.subject} on ${session.date} at ${session.startTime}",
                        session.id.toInt().takeIf { it != 0 } ?: 2001
                    )
                }
            } else {
                repository.updateClass(session)
                _userMessage.value = "Class updated"
            }
            onComplete()
        }
    }

    fun updateClassStatus(session: ClassSession, newStatus: String) {
        viewModelScope.launch {
            repository.updateClass(session.copy(status = newStatus))
            _userMessage.value = "Class marked $newStatus"
        }
    }

    fun deleteClass(session: ClassSession) {
        viewModelScope.launch {
            repository.deleteClass(session)
            _userMessage.value = "Class deleted"
        }
    }

    // Attendance
    fun saveAttendance(record: AttendanceRecord) {
        viewModelScope.launch {
            repository.insertAttendance(record)
        }
    }

    fun saveBatchAttendance(records: List<AttendanceRecord>, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertAttendanceList(records)
            _userMessage.value = "Attendance recorded for ${records.size} students"
            onComplete()
        }
    }

    fun deleteAttendance(record: AttendanceRecord) {
        viewModelScope.launch {
            repository.deleteAttendance(record)
            _userMessage.value = "Attendance record removed"
        }
    }

    // Fee Payments
    fun savePayment(payment: FeePayment, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (payment.id == 0L) {
                repository.insertPayment(payment)
                _userMessage.value = "Payment recorded"
            } else {
                repository.updatePayment(payment)
                _userMessage.value = "Payment updated"
            }
            onComplete()
        }
    }

    fun deletePayment(payment: FeePayment) {
        viewModelScope.launch {
            repository.deletePayment(payment)
            _userMessage.value = "Payment record deleted"
        }
    }

    // Exams
    fun saveExam(exam: Exam, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (exam.id == 0L) {
                repository.insertExam(exam)
                _userMessage.value = "Exam created"
            } else {
                repository.updateExam(exam)
                _userMessage.value = "Exam updated"
            }
            onComplete()
        }
    }

    fun deleteExam(exam: Exam) {
        viewModelScope.launch {
            repository.deleteResultsForExam(exam.id)
            repository.deleteExam(exam)
            _userMessage.value = "Exam and results deleted"
        }
    }

    // Exam Results
    fun saveExamResult(result: ExamResult) {
        viewModelScope.launch {
            repository.insertResult(result)
        }
    }

    fun saveBatchExamResults(results: List<ExamResult>, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertResultsList(results)
            _userMessage.value = "Marks saved for ${results.size} students"
            onComplete()
        }
    }

    fun deleteExamResult(result: ExamResult) {
        viewModelScope.launch {
            repository.deleteResult(result)
        }
    }

    // Teacher Profile & Settings
    fun updateTeacherProfile(profile: TeacherProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
            _userMessage.value = "Settings saved"
        }
    }

    // Backup & Restore
    suspend fun createBackupJson(): String {
        val prof = repository.getProfileDirect() ?: teacherProfile.value
        val stu = repository.getAllStudentsDirect()
        val cls = repository.getAllClassesDirect()
        val att = repository.getAllAttendanceDirect()
        val pay = repository.getAllPaymentsDirect()
        val exm = repository.getAllExamsDirect()
        val res = repository.getAllExamResultsDirect()
        return BackupRestoreManager.exportBackupJson(prof, stu, cls, att, pay, exm, res)
    }

    fun restoreFromJson(jsonString: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val data = BackupRestoreManager.parseBackupJson(jsonString)
                repository.clearAllData()

                data.profile?.let { repository.saveProfile(it) }
                data.students.forEach { repository.insertStudent(it) }
                data.classes.forEach { repository.insertClass(it) }
                data.attendance.forEach { repository.insertAttendance(it) }
                data.payments.forEach { repository.insertPayment(it) }
                data.exams.forEach { repository.insertExam(it) }
                data.examResults.forEach { repository.insertResult(it) }

                _userMessage.value = "Data restored successfully"
                onComplete(true, "Successfully restored ${data.students.size} students and all records.")
            } catch (e: Exception) {
                onComplete(false, "Failed to restore: ${e.message}")
            }
        }
    }
}
