package com.example.markahanmobile.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.time.LocalDate

@Parcelize
data class AttendanceRecord(
    val attendanceId: Int = 0,
    val studentId: Int,
    val userId: Int,
    val date: LocalDate,
    val status: String, // "Present", "Absent", "Late"
    val section: String = "",
    val student: Student? = null,
    val user: User? = null
) :Parcelable, Serializable