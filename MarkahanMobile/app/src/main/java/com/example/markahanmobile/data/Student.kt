package com.example.markahanmobile.data

import android.os.Parcelable
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.lang.reflect.Type

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

@JsonAdapter(GradeDeserializer::class)
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

class GradeDeserializer : JsonDeserializer<Grade> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Grade {
        val jsonObject = json.asJsonObject

        val gradeId = jsonObject.get("gradeId")?.asInt ?: 0
        val filipino = jsonObject.get("filipino")?.asDouble ?: 0.0
        val english = jsonObject.get("english")?.asDouble ?: 0.0
        val mathematics = jsonObject.get("mathematics")?.asDouble ?: 0.0
        val science = jsonObject.get("science")?.asDouble ?: 0.0
        val ap = jsonObject.get("ap")?.asDouble ?: 0.0
        val esp = jsonObject.get("esp")?.asDouble ?: 0.0
        val mapeh = jsonObject.get("mapeh")?.asDouble ?: 0.0
        val computer = jsonObject.get("computer")?.asDouble ?: 0.0
        val finalGrade = jsonObject.get("finalGrade")?.asDouble ?: 0.0
        val remarks = jsonObject.get("remarks")?.asString ?: ""

        // Handle subjectGrades map
        val subjectGradesObj = jsonObject.get("subjectGrades")?.asJsonObject
        val subjectGrades = mutableMapOf<String, SubjectGrade>()
        subjectGradesObj?.entrySet()?.forEach { entry ->
            val subjectGrade = context.deserialize<SubjectGrade>(entry.value, SubjectGrade::class.java)
            subjectGrades[entry.key] = subjectGrade
        }

        // Handle the student field manually
        val studentElement = jsonObject.get("student")
        val student: Student?
        val studentId: Int
        if (studentElement != null && studentElement.isJsonObject) {
            val studentObj = studentElement.asJsonObject
            val studentIdFromStudent = studentObj.get("studentId")?.asInt ?: 0
            val userIdFromStudent = studentObj.get("userId")?.asInt ?: 0
            val firstName = studentObj.get("firstName")?.asString ?: ""
            val lastName = studentObj.get("lastName")?.asString ?: ""
            val gender = studentObj.get("gender")?.asString ?: ""
            val section = studentObj.get("section")?.asString ?: ""
            val gradeLevel = studentObj.get("gradeLevel")?.asString ?: ""
            val attendanceStatus = studentObj.get("attendanceStatus")?.asString ?: ""
            val isArchived = studentObj.get("archived")?.asBoolean ?: false

            // Handle nested user field in student manually
            val userElementInStudent = studentObj.get("user")
            val userInStudent = if (userElementInStudent != null && userElementInStudent.isJsonObject) {
                val userObj = userElementInStudent.asJsonObject
                User(
                    userId = userObj.get("userId")?.asInt ?: 0,
                    email = userObj.get("email")?.takeIf { !it.isJsonNull }?.asString,
                    firstName = userObj.get("firstName")?.takeIf { !it.isJsonNull }?.asString,
                    lastName = userObj.get("lastName")?.takeIf { !it.isJsonNull }?.asString,
                    oauthId = userObj.get("oauthId")?.takeIf { !it.isJsonNull }?.asString,
                    password = userObj.get("password")?.takeIf { !it.isJsonNull }?.asString
                )
            } else {
                null
            }

            student = Student(
                studentId = studentIdFromStudent,
                userId = userIdFromStudent,
                user = userInStudent,
                firstName = firstName,
                lastName = lastName,
                gender = gender,
                section = section,
                gradeLevel = gradeLevel,
                attendanceStatus = attendanceStatus,
                isArchived = isArchived
            )
            studentId = studentIdFromStudent
        } else if (studentElement != null && studentElement.isJsonPrimitive && studentElement.asJsonPrimitive.isNumber) {
            student = null
            studentId = studentElement.asInt
        } else {
            student = null
            studentId = jsonObject.get("studentId")?.asInt ?: 0
        }

        // Handle the user field manually
        val userElement = jsonObject.get("user")
        val user: User?
        val userId: Int
        if (userElement != null && userElement.isJsonObject) {
            val userObj = userElement.asJsonObject
            userId = userObj.get("userId")?.asInt ?: 0
            user = User(
                userId = userId,
                email = userObj.get("email")?.takeIf { !it.isJsonNull }?.asString,
                firstName = userObj.get("firstName")?.takeIf { !it.isJsonNull }?.asString,
                lastName = userObj.get("lastName")?.takeIf { !it.isJsonNull }?.asString,
                oauthId = userObj.get("oauthId")?.takeIf { !it.isJsonNull }?.asString,
                password = userObj.get("password")?.takeIf { !it.isJsonNull }?.asString
            )
        } else {
            user = null
            userId = jsonObject.get("userId")?.asInt ?: 0
        }

        return Grade(
            gradeId = gradeId,
            studentId = studentId,
            userId = userId,
            student = student,
            user = user,
            filipino = filipino,
            english = english,
            mathematics = mathematics,
            science = science,
            ap = ap,
            esp = esp,
            mapeh = mapeh,
            computer = computer,
            finalGrade = finalGrade,
            remarks = remarks,
            subjectGrades = if (subjectGrades.isEmpty()) mapOf(
                "Filipino" to SubjectGrade("Filipino", filipino, if (filipino >= 75) "PASSED" else if (filipino > 0) "FAILED" else ""),
                "English" to SubjectGrade("English", english, if (english >= 75) "PASSED" else if (english > 0) "FAILED" else ""),
                "Math" to SubjectGrade("Math", mathematics, if (mathematics >= 75) "PASSED" else if (mathematics > 0) "FAILED" else ""),
                "Science" to SubjectGrade("Science", science, if (science >= 75) "PASSED" else if (science > 0) "FAILED" else ""),
                "AP" to SubjectGrade("AP", ap, if (ap >= 75) "PASSED" else if (ap > 0) "FAILED" else ""),
                "ESP" to SubjectGrade("ESP", esp, if (esp >= 75) "PASSED" else if (esp > 0) "FAILED" else ""),
                "MAPEH" to SubjectGrade("MAPEH", mapeh, if (mapeh >= 75) "PASSED" else if (mapeh > 0) "FAILED" else ""),
                "Computer" to SubjectGrade("Computer", computer, if (computer >= 75) "PASSED" else if (computer > 0) "FAILED" else "")
            ) else subjectGrades
        )
    }
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
    @SerializedName("oauthId") val oauthId: String? = null,
    @SerializedName("password") val password: String? = null
) : Parcelable, Serializable