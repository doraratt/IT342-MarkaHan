package com.example.markahanmobile.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.time.LocalDate

@Parcelize
data class AttendanceRecord(
    @SerializedName("attendanceId") val attendanceId: Int = 0,
    @SerializedName("studentId") val studentId: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("date") val date: LocalDate,
    @SerializedName("status") val status: String, // "Present", "Absent", "Late"
    @SerializedName("section") val section: String = "",
    @SerializedName("user") val user: User? = null,
    @SerializedName("student") val student: Student? = null
) : Parcelable, Serializable