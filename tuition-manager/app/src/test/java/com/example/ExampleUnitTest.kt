package com.example

import com.example.data.model.*
import com.example.util.BackupRestoreManager
import com.example.util.Calculations
import com.example.util.DateUtils
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testGradeCalculations() {
        assertEquals("A+", Calculations.calculateGrade(85.0))
        assertEquals("A+", Calculations.calculateGrade(80.0))
        assertEquals("A", Calculations.calculateGrade(75.0))
        assertEquals("A", Calculations.calculateGrade(70.0))
        assertEquals("A-", Calculations.calculateGrade(65.0))
        assertEquals("B", Calculations.calculateGrade(55.0))
        assertEquals("C", Calculations.calculateGrade(45.0))
        assertEquals("D", Calculations.calculateGrade(35.0))
        assertEquals("F", Calculations.calculateGrade(30.0))
        assertEquals("F", Calculations.calculateGrade(0.0))
    }

    @Test
    fun testPercentageCalculations() {
        assertEquals(85.0, Calculations.calculatePercentage(85.0, 100.0), 0.01)
        assertEquals(50.0, Calculations.calculatePercentage(25.0, 50.0), 0.01)
        assertEquals(0.0, Calculations.calculatePercentage(0.0, 100.0), 0.01)
        assertEquals(0.0, Calculations.calculatePercentage(50.0, 0.0), 0.01)
    }

    @Test
    fun testAttendancePercentageCalculations() {
        assertEquals(100.0, Calculations.calculateAttendancePercentage(10, 10), 0.01)
        assertEquals(80.0, Calculations.calculateAttendancePercentage(8, 10), 0.01)
        assertEquals(0.0, Calculations.calculateAttendancePercentage(0, 10), 0.01)
        assertEquals(0.0, Calculations.calculateAttendancePercentage(5, 0), 0.01)
    }

    @Test
    fun testCurrencyFormatting() {
        assertEquals("৳2000", Calculations.formatCurrency(2000.0, "৳"))
        assertEquals("$1500", Calculations.formatCurrency(1500.0, "$"))
        assertEquals("৳2000.50", Calculations.formatCurrency(2000.50, "৳"))
    }

    @Test
    fun testDateUtils() {
        val today = DateUtils.getTodayIso()
        assertNotNull(today)
        assertTrue(today.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))

        val currentMonth = DateUtils.getCurrentMonthYear()
        assertNotNull(currentMonth)
        assertTrue(currentMonth.isNotBlank())
    }

    @Test
    fun testBackupRestoreJson() {
        val profile = TeacherProfile(tuitionName = "Bright Mind Academy", teacherName = "John Doe")
        val students = listOf(
            Student(id = 1L, fullName = "Alice Smith", monthlyFee = 2500.0, parentPhone = "01700000000")
        )
        val classes = listOf(
            ClassSession(id = 1L, subject = "Math", date = "2026-09-30", startTime = "10:00", endTime = "11:30")
        )
        val attendance = listOf(
            AttendanceRecord(id = 1L, studentId = 1L, date = "2026-09-30", status = "Present")
        )
        val payments = listOf(
            FeePayment(id = 1L, studentId = 1L, billingMonth = "September 2026", monthlyFee = 2500.0, amountPaid = 2500.0, remainingBalance = 0.0, paymentDate = "2026-09-25")
        )
        val exams = listOf(
            Exam(id = 1L, title = "Quarterly Math Test", subject = "Math", date = "2026-10-05")
        )
        val examResults = listOf(
            ExamResult(id = 1L, examId = 1L, studentId = 1L, obtainedMarks = 92.0, totalMarks = 100.0, percentage = 92.0, grade = "A+")
        )

        val json = BackupRestoreManager.exportBackupJson(profile, students, classes, attendance, payments, exams, examResults)
        assertNotNull(json)
        assertTrue(json.contains("Bright Mind Academy"))
        assertTrue(json.contains("Alice Smith"))

        val parsed = BackupRestoreManager.parseBackupJson(json)
        assertNotNull(parsed.profile)
        assertEquals("Bright Mind Academy", parsed.profile?.tuitionName)
        assertEquals(1, parsed.students.size)
        assertEquals("Alice Smith", parsed.students[0].fullName)
        assertEquals(1, parsed.classes.size)
        assertEquals("Math", parsed.classes[0].subject)
        assertEquals(1, parsed.attendance.size)
        assertEquals("Present", parsed.attendance[0].status)
        assertEquals(1, parsed.payments.size)
        assertEquals(2500.0, parsed.payments[0].amountPaid, 0.01)
        assertEquals(1, parsed.exams.size)
        assertEquals(1, parsed.examResults.size)
        assertEquals("A+", parsed.examResults[0].grade)
    }
}
