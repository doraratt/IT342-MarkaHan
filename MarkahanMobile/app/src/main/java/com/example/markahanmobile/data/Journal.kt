package com.example.markahanmobile.data

import java.time.LocalDate
import java.util.Date

data class Journal(
    val journalId: Int = 0,
    val user: User? = null,
    val entry: String = "",
    val date: LocalDate = LocalDate.now()
)