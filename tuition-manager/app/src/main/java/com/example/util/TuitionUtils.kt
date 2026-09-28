package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.AttendanceRecord
import com.example.data.model.ClassSession
import com.example.data.model.Exam
import com.example.data.model.ExamResult
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.data.model.TeacherProfile
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val time24Format = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun getTodayIso(): String = isoDateFormat.format(Date())

    fun getCurrentMonthYear(): String = monthYearFormat.format(Date())

    fun formatToDisplayDate(isoDate: String): String {
        return try {
            val date = isoDateFormat.parse(isoDate)
            if (date != null) displayDateFormat.format(date) else isoDate
        } catch (e: Exception) {
            isoDate
        }
    }

    fun formatTo12HourTime(time24: String): String {
        return try {
            val date = time24Format.parse(time24)
            if (date != null) timeFormat.format(date) else time24
        } catch (e: Exception) {
            time24
        }
    }

    fun getMonthList(): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        for (i in -3..6) {
            val c = Calendar.getInstance()
            c.add(Calendar.MONTH, i)
            list.add(monthYearFormat.format(c.time))
        }
        return list
    }
}

object Calculations {
    fun calculateGrade(percentage: Double): String {
        return when {
            percentage >= 80.0 -> "A+"
            percentage >= 70.0 -> "A"
            percentage >= 60.0 -> "A-"
            percentage >= 50.0 -> "B"
            percentage >= 40.0 -> "C"
            percentage >= 33.0 -> "D"
            else -> "F"
        }
    }

    fun calculatePercentage(obtained: Double, total: Double): Double {
        if (total <= 0.0) return 0.0
        val p = (obtained / total) * 100.0
        return (p * 10).toLong() / 10.0 // 1 decimal place
    }

    fun calculateAttendancePercentage(presentCount: Int, totalClasses: Int): Double {
        if (totalClasses <= 0) return 0.0
        val p = (presentCount.toDouble() / totalClasses.toDouble()) * 100.0
        return (p * 10).toLong() / 10.0
    }

    fun formatCurrency(amount: Double, symbol: String = "৳"): String {
        return if (amount % 1.0 == 0.0) {
            "$symbol${amount.toLong()}"
        } else {
            String.format(Locale.getDefault(), "%s%.2f", symbol, amount)
        }
    }
}

object ActionHelper {
    fun callNumber(context: Context, phoneNumber: String) {
        if (phoneNumber.isBlank()) return
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${phoneNumber.trim()}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun sendSms(context: Context, phoneNumber: String, message: String) {
        if (phoneNumber.isBlank()) return
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${phoneNumber.trim()}")
            putExtra("sms_body", message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun shareText(context: Context, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }
}

object NotificationHelper {
    private const val CHANNEL_ID = "tuition_alerts_channel"
    private const val CHANNEL_NAME = "Tuition Reminders & Alerts"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for classes, exams, and fee collections"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, title: String, message: String, notificationId: Int = 1001) {
        createNotificationChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())
    }
}

object BackupRestoreManager {
    fun exportBackupJson(
        profile: TeacherProfile,
        students: List<Student>,
        classes: List<ClassSession>,
        attendance: List<AttendanceRecord>,
        payments: List<FeePayment>,
        exams: List<Exam>,
        examResults: List<ExamResult>
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportDate", DateUtils.getTodayIso())

        // Profile
        val profObj = JSONObject().apply {
            put("tuitionName", profile.tuitionName)
            put("teacherName", profile.teacherName)
            put("phone", profile.phone)
            put("email", profile.email)
            put("address", profile.address)
            put("currencySymbol", profile.currencySymbol)
            put("defaultMonthlyFee", profile.defaultMonthlyFee)
        }
        root.put("profile", profObj)

        // Students
        val stuArr = JSONArray()
        students.forEach { s ->
            val obj = JSONObject().apply {
                put("id", s.id)
                put("studentCustomId", s.studentCustomId)
                put("fullName", s.fullName)
                put("photoUri", s.photoUri ?: "")
                put("dateOfBirth", s.dateOfBirth)
                put("gender", s.gender)
                put("schoolOrCollege", s.schoolOrCollege)
                put("gradeClass", s.gradeClass)
                put("section", s.section)
                put("rollNumber", s.rollNumber)
                put("subjects", s.subjects)
                put("address", s.address)
                put("parentName", s.parentName)
                put("parentRelationship", s.parentRelationship)
                put("parentPhone", s.parentPhone)
                put("altPhone", s.altPhone)
                put("email", s.email)
                put("admissionDate", s.admissionDate)
                put("monthlyFee", s.monthlyFee)
                put("notes", s.notes)
                put("isActive", s.isActive)
            }
            stuArr.put(obj)
        }
        root.put("students", stuArr)

        // Classes
        val clsArr = JSONArray()
        classes.forEach { c ->
            val obj = JSONObject().apply {
                put("id", c.id)
                put("studentId", c.studentId ?: -1L)
                put("studentGroupName", c.studentGroupName)
                put("subject", c.subject)
                put("date", c.date)
                put("startTime", c.startTime)
                put("endTime", c.endTime)
                put("location", c.location)
                put("isOnline", c.isOnline)
                put("notes", c.notes)
                put("status", c.status)
            }
            clsArr.put(obj)
        }
        root.put("classes", clsArr)

        // Attendance
        val attArr = JSONArray()
        attendance.forEach { a ->
            val obj = JSONObject().apply {
                put("id", a.id)
                put("classId", a.classId ?: -1L)
                put("studentId", a.studentId)
                put("date", a.date)
                put("subject", a.subject)
                put("status", a.status)
                put("notes", a.notes)
            }
            attArr.put(obj)
        }
        root.put("attendance", attArr)

        // Payments
        val payArr = JSONArray()
        payments.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("studentId", p.studentId)
                put("billingMonth", p.billingMonth)
                put("monthlyFee", p.monthlyFee)
                put("amountPaid", p.amountPaid)
                put("remainingBalance", p.remainingBalance)
                put("paymentDate", p.paymentDate)
                put("paymentMethod", p.paymentMethod)
                put("paymentStatus", p.paymentStatus)
                put("receiptNumber", p.receiptNumber)
                put("notes", p.notes)
            }
            payArr.put(obj)
        }
        root.put("payments", payArr)

        // Exams
        val exmArr = JSONArray()
        exams.forEach { e ->
            val obj = JSONObject().apply {
                put("id", e.id)
                put("title", e.title)
                put("gradeClass", e.gradeClass)
                put("subject", e.subject)
                put("date", e.date)
                put("totalMarks", e.totalMarks)
                put("passingMarks", e.passingMarks)
                put("notes", e.notes)
            }
            exmArr.put(obj)
        }
        root.put("exams", exmArr)

        // Results
        val resArr = JSONArray()
        examResults.forEach { r ->
            val obj = JSONObject().apply {
                put("id", r.id)
                put("examId", r.examId)
                put("studentId", r.studentId)
                put("obtainedMarks", r.obtainedMarks)
                put("totalMarks", r.totalMarks)
                put("percentage", r.percentage)
                put("grade", r.grade)
                put("remarks", r.remarks)
            }
            resArr.put(obj)
        }
        root.put("examResults", resArr)

        return root.toString(2)
    }

    data class BackupData(
        val profile: TeacherProfile?,
        val students: List<Student>,
        val classes: List<ClassSession>,
        val attendance: List<AttendanceRecord>,
        val payments: List<FeePayment>,
        val exams: List<Exam>,
        val examResults: List<ExamResult>
    )

    fun parseBackupJson(jsonString: String): BackupData {
        val root = JSONObject(jsonString)

        val profile = if (root.has("profile")) {
            val p = root.getJSONObject("profile")
            TeacherProfile(
                tuitionName = p.optString("tuitionName", "Tuition Center"),
                teacherName = p.optString("teacherName", "Teacher"),
                phone = p.optString("phone", ""),
                email = p.optString("email", ""),
                address = p.optString("address", ""),
                currencySymbol = p.optString("currencySymbol", "৳"),
                defaultMonthlyFee = p.optDouble("defaultMonthlyFee", 2000.0)
            )
        } else null

        val students = mutableListOf<Student>()
        if (root.has("students")) {
            val arr = root.getJSONArray("students")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                students.add(
                    Student(
                        studentCustomId = o.optString("studentCustomId", ""),
                        fullName = o.optString("fullName", ""),
                        photoUri = o.optString("photoUri").takeIf { it.isNotBlank() },
                        dateOfBirth = o.optString("dateOfBirth", ""),
                        gender = o.optString("gender", "Male"),
                        schoolOrCollege = o.optString("schoolOrCollege", ""),
                        gradeClass = o.optString("gradeClass", ""),
                        section = o.optString("section", ""),
                        rollNumber = o.optString("rollNumber", ""),
                        subjects = o.optString("subjects", ""),
                        address = o.optString("address", ""),
                        parentName = o.optString("parentName", ""),
                        parentRelationship = o.optString("parentRelationship", "Father"),
                        parentPhone = o.optString("parentPhone", ""),
                        altPhone = o.optString("altPhone", ""),
                        email = o.optString("email", ""),
                        admissionDate = o.optString("admissionDate", ""),
                        monthlyFee = o.optDouble("monthlyFee", 0.0),
                        notes = o.optString("notes", ""),
                        isActive = o.optBoolean("isActive", true)
                    )
                )
            }
        }

        val classes = mutableListOf<ClassSession>()
        if (root.has("classes")) {
            val arr = root.getJSONArray("classes")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val sId = o.optLong("studentId", -1L)
                classes.add(
                    ClassSession(
                        studentId = if (sId > 0) sId else null,
                        studentGroupName = o.optString("studentGroupName", ""),
                        subject = o.optString("subject", ""),
                        date = o.optString("date", ""),
                        startTime = o.optString("startTime", ""),
                        endTime = o.optString("endTime", ""),
                        location = o.optString("location", ""),
                        isOnline = o.optBoolean("isOnline", false),
                        notes = o.optString("notes", ""),
                        status = o.optString("status", "Scheduled")
                    )
                )
            }
        }

        val attendance = mutableListOf<AttendanceRecord>()
        if (root.has("attendance")) {
            val arr = root.getJSONArray("attendance")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val cId = o.optLong("classId", -1L)
                attendance.add(
                    AttendanceRecord(
                        classId = if (cId > 0) cId else null,
                        studentId = o.optLong("studentId", 0L),
                        date = o.optString("date", ""),
                        subject = o.optString("subject", ""),
                        status = o.optString("status", "Present"),
                        notes = o.optString("notes", "")
                    )
                )
            }
        }

        val payments = mutableListOf<FeePayment>()
        if (root.has("payments")) {
            val arr = root.getJSONArray("payments")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                payments.add(
                    FeePayment(
                        studentId = o.optLong("studentId", 0L),
                        billingMonth = o.optString("billingMonth", ""),
                        monthlyFee = o.optDouble("monthlyFee", 0.0),
                        amountPaid = o.optDouble("amountPaid", 0.0),
                        remainingBalance = o.optDouble("remainingBalance", 0.0),
                        paymentDate = o.optString("paymentDate", ""),
                        paymentMethod = o.optString("paymentMethod", "Cash"),
                        paymentStatus = o.optString("paymentStatus", "Paid"),
                        receiptNumber = o.optString("receiptNumber", ""),
                        notes = o.optString("notes", "")
                    )
                )
            }
        }

        val exams = mutableListOf<Exam>()
        if (root.has("exams")) {
            val arr = root.getJSONArray("exams")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                exams.add(
                    Exam(
                        title = o.optString("title", ""),
                        gradeClass = o.optString("gradeClass", ""),
                        subject = o.optString("subject", ""),
                        date = o.optString("date", ""),
                        totalMarks = o.optDouble("totalMarks", 100.0),
                        passingMarks = o.optDouble("passingMarks", 33.0),
                        notes = o.optString("notes", "")
                    )
                )
            }
        }

        val examResults = mutableListOf<ExamResult>()
        if (root.has("examResults")) {
            val arr = root.getJSONArray("examResults")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                examResults.add(
                    ExamResult(
                        examId = o.optLong("examId", 0L),
                        studentId = o.optLong("studentId", 0L),
                        obtainedMarks = o.optDouble("obtainedMarks", 0.0),
                        totalMarks = o.optDouble("totalMarks", 100.0),
                        percentage = o.optDouble("percentage", 0.0),
                        grade = o.optString("grade", "F"),
                        remarks = o.optString("remarks", "")
                    )
                )
            }
        }

        return BackupData(profile, students, classes, attendance, payments, exams, examResults)
    }
}
