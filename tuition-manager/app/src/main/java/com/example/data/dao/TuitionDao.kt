package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecord
import com.example.data.model.ClassSession
import com.example.data.model.Exam
import com.example.data.model.ExamResult
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.data.model.TeacherProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface TuitionDao {

    // --- Students ---
    @Query("SELECT * FROM students ORDER BY fullName ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE isActive = 1 ORDER BY fullName ASC")
    fun getActiveStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    fun getStudentById(id: Long): Flow<Student?>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentByIdDirect(id: Long): Student?

    @Query("SELECT * FROM students")
    suspend fun getAllStudentsDirect(): List<Student>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    // --- Class Sessions ---
    @Query("SELECT * FROM class_sessions ORDER BY date ASC, startTime ASC")
    fun getAllClasses(): Flow<List<ClassSession>>

    @Query("SELECT * FROM class_sessions WHERE date = :date ORDER BY startTime ASC")
    fun getClassesForDate(date: String): Flow<List<ClassSession>>

    @Query("SELECT * FROM class_sessions WHERE id = :id")
    fun getClassById(id: Long): Flow<ClassSession?>

    @Query("SELECT * FROM class_sessions")
    suspend fun getAllClassesDirect(): List<ClassSession>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(session: ClassSession): Long

    @Update
    suspend fun updateClass(session: ClassSession)

    @Delete
    suspend fun deleteClass(session: ClassSession)

    // --- Attendance Records ---
    @Query("SELECT * FROM attendance_records ORDER BY date DESC")
    fun getAllAttendance(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE classId = :classId")
    fun getAttendanceForClass(classId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records")
    suspend fun getAllAttendanceDirect(): List<AttendanceRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(records: List<AttendanceRecord>)

    @Update
    suspend fun updateAttendance(record: AttendanceRecord)

    @Delete
    suspend fun deleteAttendance(record: AttendanceRecord)

    @Query("DELETE FROM attendance_records WHERE classId = :classId")
    suspend fun deleteAttendanceForClass(classId: Long)

    // --- Fee Payments ---
    @Query("SELECT * FROM fee_payments ORDER BY paymentDate DESC, id DESC")
    fun getAllPayments(): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments WHERE studentId = :studentId ORDER BY paymentDate DESC")
    fun getPaymentsForStudent(studentId: Long): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments")
    suspend fun getAllPaymentsDirect(): List<FeePayment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: FeePayment): Long

    @Update
    suspend fun updatePayment(payment: FeePayment)

    @Delete
    suspend fun deletePayment(payment: FeePayment)

    // --- Exams ---
    @Query("SELECT * FROM exams ORDER BY date DESC")
    fun getAllExams(): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE id = :id")
    fun getExamById(id: Long): Flow<Exam?>

    @Query("SELECT * FROM exams")
    suspend fun getAllExamsDirect(): List<Exam>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam): Long

    @Update
    suspend fun updateExam(exam: Exam)

    @Delete
    suspend fun deleteExam(exam: Exam)

    // --- Exam Results ---
    @Query("SELECT * FROM exam_results")
    fun getAllExamResults(): Flow<List<ExamResult>>

    @Query("SELECT * FROM exam_results WHERE examId = :examId")
    fun getResultsForExam(examId: Long): Flow<List<ExamResult>>

    @Query("SELECT * FROM exam_results WHERE studentId = :studentId")
    fun getResultsForStudent(studentId: Long): Flow<List<ExamResult>>

    @Query("SELECT * FROM exam_results")
    suspend fun getAllExamResultsDirect(): List<ExamResult>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: ExamResult): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResultsList(results: List<ExamResult>)

    @Update
    suspend fun updateResult(result: ExamResult)

    @Delete
    suspend fun deleteResult(result: ExamResult)

    @Query("DELETE FROM exam_results WHERE examId = :examId")
    suspend fun deleteResultsForExam(examId: Long)

    // --- Teacher Profile ---
    @Query("SELECT * FROM teacher_profile WHERE id = 1")
    fun getProfile(): Flow<TeacherProfile?>

    @Query("SELECT * FROM teacher_profile WHERE id = 1")
    suspend fun getProfileDirect(): TeacherProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: TeacherProfile)

    // Clear all for restore
    @Query("DELETE FROM students")
    suspend fun clearStudents()

    @Query("DELETE FROM class_sessions")
    suspend fun clearClassSessions()

    @Query("DELETE FROM attendance_records")
    suspend fun clearAttendance()

    @Query("DELETE FROM fee_payments")
    suspend fun clearFeePayments()

    @Query("DELETE FROM exams")
    suspend fun clearExams()

    @Query("DELETE FROM exam_results")
    suspend fun clearExamResults()
}
