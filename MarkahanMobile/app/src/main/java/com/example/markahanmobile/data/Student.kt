package com.example.markahanmobile.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class Student(
    val studentId: Int = 0,
    val userId: Int = 0,
    val user: User? = null,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val section: String,
    val gradeLevel: String,
    val grade: Grade? = null,
    val attendanceStatus: String = "",
    val isArchived: Boolean = false
) : Parcelable, Serializable

@Parcelize
data class Grade(
    val gradeId: Int = 0,
    val studentId: Int = 0, // Used for API serialization
    val userId: Int = 0,    // Used for API serialization
    val student: Student? = null, // Optional for deserialization, but often null to avoid circular reference
    val user: User? = null,       // Optional for deserialization
    val filipino: Double = 0.0,
    val english: Double = 0.0,
    val mathematics: Double = 0.0,
    val science: Double = 0.0,
    val ap: Double = 0.0,
    val esp: Double = 0.0,
    val mapeh: Double = 0.0,
    val computer: Double = 0.0,
    val finalGrade: Double = 0.0,
    val remarks: String = "",
    val subjectGrades: Map<String, SubjectGrade> = mapOf(
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
    val subject: String,
    val grade: Double,
    val remarks: String
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