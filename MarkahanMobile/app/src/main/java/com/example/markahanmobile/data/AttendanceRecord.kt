package com.example.markahanmobile.data

import java.util.*

data class AttendanceRecord(
    val studentId: String,
    val date: Date,
    val status: String, // "P", "A", or "L"
    val section: String = ""
)