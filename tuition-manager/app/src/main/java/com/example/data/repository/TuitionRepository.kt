package com.example.data.repository

import com.example.data.dao.TuitionDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.ClassSession
import com.example.data.model.Exam
import com.example.data.model.ExamResult
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.data.model.TeacherProfile
import kotlinx.coroutines.flow.Flow

class TuitionRepository(private val dao: TuitionDao) {

    // Students
    val allStudents: Flow<List<Student>> = dao.getAllStudents()
    val activeStudents: Flow<List<Student>> = dao.getActiveStudents()

    fun getStudentById(id: Long): Flow<Student?> = dao.getStudentById(id)
    suspend fun getStudentByIdDirect(id: Long): Student? = dao.getStudentByIdDirect(id)
    suspend fun getAllStudentsDirect(): List<Student> = dao.getAllStudentsDirect()
    suspend fun insertStudent(student: Student): Long = dao.insertStudent(student)
    suspend fun updateStudent(student: Student) = dao.updateStudent(student)
    suspend fun deleteStudent(student: Student) = dao.deleteStudent(student)

    // Classes
    val allClasses: Flow<List<ClassSession>> = dao.getAllClasses()
    fun getClassesForDate(date: String): Flow<List<ClassSession>> = dao.getClassesForDate(date)
    fun getClassById(id: Long): Flow<ClassSession?> = dao.getClassById(id)
    suspend fun getAllClassesDirect(): List<ClassSession> = dao.getAllClassesDirect()
    suspend fun insertClass(session: ClassSession): Long = dao.insertClass(session)
    suspend fun updateClass(session: ClassSession) = dao.updateClass(session)
    suspend fun deleteClass(session: ClassSession) = dao.deleteClass(session)

    // Attendance
    val allAttendance: Flow<List<AttendanceRecord>> = dao.getAllAttendance()
    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecord>> = dao.getAttendanceForDate(date)
    fun getAttendanceForClass(classId: Long): Flow<List<AttendanceRecord>> = dao.getAttendanceForClass(classId)
    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceRecord>> = dao.getAttendanceForStudent(studentId)
    suspend fun getAllAttendanceDirect(): List<AttendanceRecord> = dao.getAllAttendanceDirect()
    suspend fun insertAttendance(record: AttendanceRecord): Long = dao.insertAttendance(record)
    suspend fun insertAttendanceList(records: List<AttendanceRecord>) = dao.insertAttendanceList(records)
    suspend fun updateAttendance(record: AttendanceRecord) = dao.updateAttendance(record)
    suspend fun deleteAttendance(record: AttendanceRecord) = dao.deleteAttendance(record)
    suspend fun deleteAttendanceForClass(classId: Long) = dao.deleteAttendanceForClass(classId)

    // Fee Payments
    val allPayments: Flow<List<FeePayment>> = dao.getAllPayments()
    fun getPaymentsForStudent(studentId: Long): Flow<List<FeePayment>> = dao.getPaymentsForStudent(studentId)
    suspend fun getAllPaymentsDirect(): List<FeePayment> = dao.getAllPaymentsDirect()
    suspend fun insertPayment(payment: FeePayment): Long = dao.insertPayment(payment)
    suspend fun updatePayment(payment: FeePayment) = dao.updatePayment(payment)
    suspend fun deletePayment(payment: FeePayment) = dao.deletePayment(payment)

    // Exams
    val allExams: Flow<List<Exam>> = dao.getAllExams()
    fun getExamById(id: Long): Flow<Exam?> = dao.getExamById(id)
    suspend fun getAllExamsDirect(): List<Exam> = dao.getAllExamsDirect()
    suspend fun insertExam(exam: Exam): Long = dao.insertExam(exam)
    suspend fun updateExam(exam: Exam) = dao.updateExam(exam)
    suspend fun deleteExam(exam: Exam) = dao.deleteExam(exam)

    // Exam Results
    val allExamResults: Flow<List<ExamResult>> = dao.getAllExamResults()
    fun getResultsForExam(examId: Long): Flow<List<ExamResult>> = dao.getResultsForExam(examId)
    fun getResultsForStudent(studentId: Long): Flow<List<ExamResult>> = dao.getResultsForStudent(studentId)
    suspend fun getAllExamResultsDirect(): List<ExamResult> = dao.getAllExamResultsDirect()
    suspend fun insertResult(result: ExamResult): Long = dao.insertResult(result)
    suspend fun insertResultsList(results: List<ExamResult>) = dao.insertResultsList(results)
    suspend fun updateResult(result: ExamResult) = dao.updateResult(result)
    suspend fun deleteResult(result: ExamResult) = dao.deleteResult(result)
    suspend fun deleteResultsForExam(examId: Long) = dao.deleteResultsForExam(examId)

    // Profile & Settings
    val profile: Flow<TeacherProfile?> = dao.getProfile()
    suspend fun getProfileDirect(): TeacherProfile? = dao.getProfileDirect()
    suspend fun saveProfile(profile: TeacherProfile) = dao.insertOrUpdateProfile(profile)

    // Clear all for restore
    suspend fun clearAllData() {
        dao.clearAttendance()
        dao.clearClassSessions()
        dao.clearExamResults()
        dao.clearExams()
        dao.clearFeePayments()
        dao.clearStudents()
    }
}
