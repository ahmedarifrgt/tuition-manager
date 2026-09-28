package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.TuitionDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.ClassSession
import com.example.data.model.Exam
import com.example.data.model.ExamResult
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.data.model.TeacherProfile

@Database(
    entities = [
        Student::class,
        ClassSession::class,
        AttendanceRecord::class,
        FeePayment::class,
        Exam::class,
        ExamResult::class,
        TeacherProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tuitionDao(): TuitionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tuition_manager_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
