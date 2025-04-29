package com.example.markahanmobile.data

import java.time.LocalDate

data class Calendar(
    val calendarId: Int = 0,
    val userId: Int = 0,
    val eventDescription: String = "",
    val date: LocalDate = LocalDate.now()
)
