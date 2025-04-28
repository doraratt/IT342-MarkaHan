package com.example.markahanmobile.data

import android.content.ContentValues.TAG
import android.util.Log
import com.example.markahanmobile.helper.SectionAdapter
import java.util.*

object DataStore {
    private val students = mutableListOf<Student>()
    private val attendanceRecords = mutableListOf<AttendanceRecord>()

    fun addStudent(student: Student) {
        // Prevent duplicate student IDs
        if (students.any { it.studentID == student.studentID && student.studentID.isNotEmpty() }) {
            throw IllegalArgumentException("Student with ID ${student.studentID} already exists")
        }
        students.add(student)
        Log.d(TAG, "Added student: ${student.studentID}, ${student.firstName} ${student.lastName}, Section=${student.section}, GradeLevel=${student.gradeLevel}")
    }

    fun updateStudent(updatedStudent: Student) {
        val index = students.indexOfFirst { it.studentID == updatedStudent.studentID }
        if (index != -1) {
            students[index] = updatedStudent
            Log.d(TAG, "Updated student: ${updatedStudent.studentID}, Grades: ${updatedStudent.grades}, Remarks: ${updatedStudent.remarks}, Average: ${updatedStudent.average}")
        } else {
            throw IllegalArgumentException("Student with ID ${updatedStudent.studentID} not found")
        }
    }

    fun archiveStudent(studentID: String) {
        val index = students.indexOfFirst { it.studentID == studentID }
        if (index != -1) {
            students[index] = students[index].copy(isArchived = true)
            Log.d(TAG, "Archived student: $studentID")
        } else {
            throw IllegalArgumentException("Student with ID $studentID not found")
        }
    }

    fun getStudents(section: String? = null, includeArchived: Boolean = false): List<Student> {
        val filteredStudents = if (includeArchived) students else students.filter { !it.isArchived }
        val result = if (section != null && section != SectionAdapter.ALL_SECTIONS) {
            filteredStudents.filter { it.section.equals(section, ignoreCase = true) }
        } else {
            filteredStudents.toList()
        }
        Log.d(TAG, "Fetched students: ${result.size} for section: $section, includeArchived: $includeArchived, Students: ${result.map { "${it.firstName} ${it.lastName}, Section=${it.section}, GradeLevel=${it.gradeLevel}" }}")
        return result
    }

    fun getSections(): List<String> {
        val sections = students.filter { !it.isArchived }
            .map { it.section }
            .distinct()
            .toMutableList()
            .apply {
            add(0, SectionAdapter.ALL_SECTIONS)
        }
        Log.d(TAG, "Fetched sections: $sections")
        return sections
    }

    fun addAttendanceRecords(records: List<AttendanceRecord>) {
        records.forEach { newRecord ->
            attendanceRecords.removeAll {
                it.studentId == newRecord.studentId && it.section == newRecord.section && isSameDay(it.date, newRecord.date)
            }
            attendanceRecords.add(newRecord)
        }
        Log.d(TAG, "Added ${records.size} attendance records")
    }

    fun getAttendanceRecords(section: String, startDate: Date, endDate: Date): List<AttendanceRecord> {
        val records = attendanceRecords.filter {
            it.section == section && it.date in startDate..endDate
        }
        Log.d(TAG, "Fetched ${records.size} attendance records for section: $section")
        return records
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }
}