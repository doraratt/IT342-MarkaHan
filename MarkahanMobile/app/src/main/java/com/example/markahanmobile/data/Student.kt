package com.example.markahanmobile.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class Student(
    @SerializedName("studentId") val studentId: Int = 0,
    @SerializedName("userId") val userId: Int = 0,
    @SerializedName("user") val user: User? = null,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("section") val section: String,
    @SerializedName("gradeLevel") val gradeLevel: String,
    @SerializedName("grade") val grade: Grade? = null,
    @SerializedName("attendanceStatus") val attendanceStatus: String = "",
    @SerializedName("archived") val isArchived: Boolean = false
) : Parcelable, Serializable

@Parcelize
data class Grade(
    @SerializedName("gradeId") val gradeId: Int = 0,
    @SerializedName("studentId") val studentId: Int = 0,
    @SerializedName("userId") val userId: Int = 0,
    @SerializedName("student") val student: Student? = null,
    @SerializedName("user") val user: User? = null,
    @SerializedName("filipino") val filipino: Double = 0.0,
    @SerializedName("english") val english: Double = 0.0,
    @SerializedName("mathematics") val mathematics: Double = 0.0,
    @SerializedName("science") val science: Double = 0.0,
    @SerializedName("ap") val ap: Double = 0.0,
    @SerializedName("esp") val esp: Double = 0.0,
    @SerializedName("mapeh") val mapeh: Double = 0.0,
    @SerializedName("computer") val computer: Double = 0.0,
    @SerializedName("finalGrade") val finalGrade: Double = 0.0,
    @SerializedName("remarks") val remarks: String = "",
    @SerializedName("subjectGrades") val subjectGrades: Map<String, SubjectGrade> = mapOf(
        "Filipino" to SubjectGrade("Filipino", filipino, if (filipino >= 75) "PASSED" else if (filipino > 0) "FAILED" else ""),
        "English" to SubjectGrade("English", english, if (english >= 75) "PASSED" else if (english > 0) "FAILED" else ""),
        "Math" to SubjectGrade("Math", mathematics, if (mathematics >= 75) "PASSED" else if (mathematics > 0) "FAILED" else ""),
        "Science" to SubjectGrade("Science", science, if (science >= 75) "PASSED" else if (science > 0) "FAILED" else ""),
        "AP" to SubjectGrade("AP", ap, if (ap >= 75) "PASSED" else if (ap > 0) "FAILED" else ""),
        "ESP" to SubjectGrade("ESP", esp, if (esp >= 75) "PASSED" else if (esp > 0) "FAILED" else ""),
        "MAPEH" to SubjectGrade("MAPEH", mapeh, if (mapeh >= 75) "PASSED" else if (mapeh > 0) "FAILED" else ""),
        "Computer" to SubjectGrade("Computer", computer, if (computer >= 75) "PASSED" else if (computer > 0) "FAILED" else "")
    )
) : Parcelable, Serializable {
    val average: Double
        get() = finalGrade
}

@Parcelize
data class SubjectGrade(
    @SerializedName("subject") val subject: String,
    @SerializedName("grade") val grade: Double,
    @SerializedName("remarks") val remarks: String
) : Parcelable, Serializable {
    constructor(subject: String, grade: Double) : this(
        subject,
        grade,
        if (grade >= 75) "PASSED" else if (grade > 0) "FAILED" else ""
    )
}

@Parcelize
data class User(
    @SerializedName("userId") val userId: Int = 0,
    @SerializedName("email") val email: String? = null,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("oauthId") val oauthId: String? = null
) : Parcelable, Serializable