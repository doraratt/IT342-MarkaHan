package com.example.markahanmobile.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Collections.emptyMap

@Parcelize
data class Student(
    val studentID: String = "",
    val firstName: String,
    val lastName: String,
    val section: String,
    val gradeLevel: String,
    var attendanceStatus: String = "",
    val grades: Map<String, SubjectGrade> = emptyMap()
) : Parcelable {
    val average: Double
        get() = if (grades.isNotEmpty()) grades.values.map { it.grade }.average() else 0.0

    val remarks: String
        get() = when {
            grades.isEmpty() -> "NO GRADES"
            average >= 75 -> "PASSED"
            else -> "FAILED"
        }
}

@Parcelize
data class SubjectGrade(
    val subjectName: String,
    val grade: Double,
    val remarks: String = if (grade >= 75) "PASSED" else "FAILED"
) : Parcelable