package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentCustomId: String = "",
    val fullName: String,
    val photoUri: String? = null,
    val dateOfBirth: String = "",
    val gender: String = "Male",
    val schoolOrCollege: String = "",
    val gradeClass: String = "",
    val section: String = "",
    val rollNumber: String = "",
    val subjects: String = "",
    val address: String = "",
    val parentName: String = "",
    val parentRelationship: String = "Father",
    val parentPhone: String = "",
    val altPhone: String = "",
    val email: String = "",
    val admissionDate: String = "",
    val monthlyFee: Double = 0.0,
    val notes: String = "",
    val isActive: Boolean = true
)

@Entity(tableName = "class_sessions")
data class ClassSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long? = null,
    val studentGroupName: String = "",
    val subject: String = "",
    val date: String = "", // YYYY-MM-DD
    val startTime: String = "", // HH:mm
    val endTime: String = "", // HH:mm
    val location: String = "",
    val isOnline: Boolean = false,
    val notes: String = "",
    val status: String = "Scheduled" // "Scheduled", "Completed", "Cancelled"
)

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val classId: Long? = null,
    val studentId: Long,
    val date: String, // YYYY-MM-DD
    val subject: String = "",
    val status: String = "Present", // "Present", "Absent", "Late", "Excused"
    val notes: String = ""
)

@Entity(tableName = "fee_payments")
data class FeePayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val billingMonth: String, // e.g. "September 2026"
    val monthlyFee: Double,
    val amountPaid: Double,
    val remainingBalance: Double,
    val paymentDate: String, // YYYY-MM-DD
    val paymentMethod: String = "Cash", // Cash, bKash, Nagad, Bank, Other
    val paymentStatus: String = "Paid", // Paid, Partially Paid, Due, Overdue
    val receiptNumber: String = "",
    val notes: String = ""
)

@Entity(tableName = "exams")
data class Exam(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val gradeClass: String = "",
    val subject: String = "",
    val date: String = "", // YYYY-MM-DD
    val totalMarks: Double = 100.0,
    val passingMarks: Double = 33.0,
    val notes: String = ""
)

@Entity(tableName = "exam_results")
data class ExamResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val examId: Long,
    val studentId: Long,
    val obtainedMarks: Double,
    val totalMarks: Double,
    val percentage: Double,
    val grade: String,
    val remarks: String = ""
)

@Entity(tableName = "teacher_profile")
data class TeacherProfile(
    @PrimaryKey
    val id: Int = 1,
    val tuitionName: String = "Tuition Center",
    val teacherName: String = "Teacher",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val currencySymbol: String = "৳",
    val defaultMonthlyFee: Double = 2000.0,
    val notifyClasses: Boolean = true,
    val notifyFees: Boolean = true,
    val notifyExams: Boolean = true
)
