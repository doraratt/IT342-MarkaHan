package com.example.markahanmobile.data

import java.util.Date
import java.util.UUID

data class Journal(
    val journalID: String = UUID.randomUUID().toString(),
    val journalEntry: String,
    val date: Date,
)