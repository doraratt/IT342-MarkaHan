package com.example.markahanmobile.data

data class LoginResponse(
    val userId: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val oauthId: String?
)
