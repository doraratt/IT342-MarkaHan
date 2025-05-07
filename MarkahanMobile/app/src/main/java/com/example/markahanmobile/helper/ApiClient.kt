package com.example.markahanmobile.helper

import android.content.Context
import android.util.Log
import com.example.markahanmobile.BuildConfig
import com.example.markahanmobile.data.AttendanceRecord
import com.example.markahanmobile.data.Calendar
import com.example.markahanmobile.data.Grade
import com.example.markahanmobile.data.Student
import com.example.markahanmobile.data.User
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val TAG = "ApiClient"
    private lateinit var retrofit: Retrofit
    lateinit var apiService: ApiService
        private set

    fun init(context: Context) {
        val baseUrl = BuildConfig.BACKEND_URL
        val environment = when (baseUrl) {
            "http://10.0.2.2:8080/" -> "Local (Emulator)"
            "https://rendeer-ya43.onrender.com/" -> "Deployed"
            else -> "Unknown ($baseUrl)"
        }
        Log.d(TAG, "Initializing ApiClient with baseUrl: $baseUrl (Environment: $environment)")

        val logging = HttpLoggingInterceptor { message -> Log.d(TAG, "Network: $message") }.apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val formatter = DateTimeFormatter.ISO_LOCAL_DATE // "yyyy-MM-dd"
        val formatterFallback = DateTimeFormatter.ofPattern("MM/dd/yyyy")

        val localDateAdapter = object : TypeAdapter<LocalDate>() {
            override fun write(out: JsonWriter, value: LocalDate?) {
                val dateString = value?.format(formatter)
                Log.d(TAG, "Serializing LocalDate to: $dateString")
                out.value(dateString)
            }

            override fun read(`in`: JsonReader): LocalDate {
                val dateString = `in`.nextString()
                Log.d(TAG, "Attempting to deserialize date string: $dateString")
                return try {
                    LocalDate.parse(dateString, formatter)
                } catch (e: Exception) {
                    Log.w(TAG, "ISO_LOCAL_DATE failed, trying fallback: $dateString")
                    try {
                        LocalDate.parse(dateString, formatterFallback)
                    } catch (e2: Exception) {
                        Log.e(TAG, "All date parsing failed for: $dateString", e2)
                        throw e2
                    }
                }
            }
        }

        val calendarAdapter = object : TypeAdapter<Calendar>() {
            override fun write(out: JsonWriter, value: Calendar?) {
                if (value == null) {
                    out.nullValue()
                    return
                }
                out.beginObject()
                out.name("calendarId").value(value.calendarId)
                out.name("userId").value(value.userId)
                out.name("eventDescription").value(value.eventDescription)
                out.name("date").value(value.date)
                out.endObject()
            }

            override fun read(`in`: JsonReader): Calendar {
                var calendarId = 0
                var userId = 0
                var eventDescription = ""
                var date = ""

                `in`.beginObject()
                while (`in`.hasNext()) {
                    when (`in`.nextName()) {
                        "calendarId" -> calendarId = `in`.nextInt()
                        "userId" -> userId = `in`.nextInt()
                        "eventDescription" -> eventDescription = `in`.nextString()
                        "date" -> date = `in`.nextString()
                        "user" -> {
                            `in`.beginObject()
                            while (`in`.hasNext()) {
                                if (`in`.nextName() == "userId") {
                                    userId = `in`.nextInt()
                                    break
                                } else {
                                    `in`.skipValue()
                                }
                            }
                            `in`.endObject()
                        }
                        else -> `in`.skipValue()
                    }
                }
                `in`.endObject()
                return Calendar(calendarId, userId, eventDescription, date)
            }
        }

        val studentAdapter = object : TypeAdapter<Student>() {
            override fun write(out: JsonWriter, value: Student?) {
                if (value == null) {
                    out.nullValue()
                    return
                }
                out.beginObject()
                out.name("studentId").value(value.studentId)
                out.name("userId").value(value.userId)
                out.name("firstName").value(value.firstName)
                out.name("lastName").value(value.lastName)
                out.name("gender").value(value.gender)
                out.name("section").value(value.section)
                out.name("gradeLevel").value(value.gradeLevel)
                out.name("attendanceStatus").value(value.attendanceStatus)
                out.name("archived").value(value.isArchived)
                if (value.user != null) {
                    out.name("user")
                    out.beginObject()
                    out.name("userId").value(value.user.userId)
                    out.endObject()
                }
                out.endObject()
            }

            override fun read(`in`: JsonReader): Student {
                var studentId = 0
                var userId = 0
                var firstName = ""
                var lastName = ""
                var gender = ""
                var section = ""
                var gradeLevel = ""
                var attendanceStatus = ""
                var isArchived = false
                var user: User? = null

                `in`.beginObject()
                while (`in`.hasNext()) {
                    when (`in`.nextName()) {
                        "studentId" -> studentId = `in`.nextInt()
                        "userId" -> userId = `in`.nextInt()
                        "firstName" -> firstName = `in`.nextString() ?: ""
                        "lastName" -> lastName = `in`.nextString() ?: ""
                        "gender" -> gender = `in`.nextString() ?: ""
                        "section" -> section = `in`.nextString() ?: ""
                        "gradeLevel" -> gradeLevel = `in`.nextString() ?: ""
                        "attendanceStatus" -> attendanceStatus = `in`.nextString() ?: ""
                        "archived" -> isArchived = `in`.nextBoolean()
                        "user" -> {
                            `in`.beginObject()
                            var userIdFromUser = 0
                            var email: String? = null
                            var firstNameUser: String? = null
                            var lastNameUser: String? = null
                            var oauthId: String? = null
                            while (`in`.hasNext()) {
                                when (`in`.nextName()) {
                                    "userId" -> userIdFromUser = `in`.nextInt()
                                    "email" -> email = `in`.nextStringOrNull()
                                    "firstName" -> firstNameUser = `in`.nextStringOrNull()
                                    "lastName" -> lastNameUser = `in`.nextStringOrNull()
                                    "oauthId" -> oauthId = `in`.nextStringOrNull()
                                    else -> `in`.skipValue()
                                }
                            }
                            `in`.endObject()
                            user = User(
                                userId = userIdFromUser,
                                email = email,
                                firstName = firstNameUser,
                                lastName = lastNameUser,
                                oauthId = oauthId
                            )
                            userId = userIdFromUser
                        }
                        else -> `in`.skipValue()
                    }
                }
                `in`.endObject()
                return Student(
                    studentId = studentId,
                    userId = userId,
                    user = user,
                    firstName = firstName,
                    lastName = lastName,
                    gender = gender,
                    section = section,
                    gradeLevel = gradeLevel,
                    attendanceStatus = attendanceStatus,
                    isArchived = isArchived
                )
            }
        }

        val attendanceRecordAdapter = object : TypeAdapter<AttendanceRecord>() {
            override fun write(out: JsonWriter, value: AttendanceRecord?) {
                if (value == null) {
                    out.nullValue()
                    return
                }
                out.beginObject()
                out.name("attendanceId").value(value.attendanceId)
                out.name("studentId").value(value.studentId)
                out.name("userId").value(value.userId)
                out.name("date").value(value.date.format(formatter))
                out.name("status").value(value.status)
                out.name("section").value(value.section)
                // Always include student field with at least studentId
                out.name("student")
                out.beginObject()
                out.name("studentId").value(value.studentId)
                // Include additional student fields if available
                if (value.student != null) {
                    out.name("firstName").value(value.student.firstName)
                    out.name("lastName").value(value.student.lastName)
                    out.name("gender").value(value.student.gender)
                    out.name("section").value(value.student.section)
                    out.name("gradeLevel").value(value.student.gradeLevel)
                    out.name("attendanceStatus").value(value.student.attendanceStatus)
                    out.name("archived").value(value.student.isArchived)
                }
                out.endObject()
                if (value.user != null) {
                    out.name("user")
                    out.beginObject()
                    out.name("userId").value(value.user.userId)
                    out.endObject()
                }
                out.endObject()
            }

            override fun read(`in`: JsonReader): AttendanceRecord {
                var attendanceId = 0
                var studentId = 0
                var userId = 0
                var date: LocalDate? = null
                var status = ""
                var section = ""
                var user: User? = null
                var student: Student? = null

                `in`.beginObject()
                while (`in`.hasNext()) {
                    when (`in`.nextName()) {
                        "attendanceId" -> attendanceId = `in`.nextInt()
                        "student" -> {
                            if (`in`.peek() == JsonToken.NUMBER) {
                                studentId = `in`.nextInt()
                            } else {
                                `in`.beginObject()
                                var studentIdFromStudent = 0
                                var firstName: String? = null
                                var lastName: String? = null
                                var gender: String? = null
                                var sectionStudent: String? = null
                                var gradeLevel: String? = null
                                var attendanceStatus: String? = null
                                var isArchived = false
                                while (`in`.hasNext()) {
                                    when (`in`.nextName()) {
                                        "studentId" -> studentIdFromStudent = `in`.nextInt()
                                        "firstName" -> firstName = `in`.nextStringOrNull()
                                        "lastName" -> lastName = `in`.nextStringOrNull()
                                        "gender" -> gender = `in`.nextStringOrNull()
                                        "section" -> sectionStudent = `in`.nextStringOrNull()
                                        "gradeLevel" -> gradeLevel = `in`.nextStringOrNull()
                                        "attendanceStatus" -> attendanceStatus = `in`.nextStringOrNull()
                                        "archived" -> isArchived = `in`.nextBoolean()
                                        else -> `in`.skipValue()
                                    }
                                }
                                `in`.endObject()
                                student = Student(
                                    studentId = studentIdFromStudent,
                                    userId = 0,
                                    user = null,
                                    firstName = firstName ?: "",
                                    lastName = lastName ?: "",
                                    gender = gender ?: "",
                                    section = sectionStudent ?: "",
                                    gradeLevel = gradeLevel ?: "",
                                    attendanceStatus = attendanceStatus ?: "",
                                    isArchived = isArchived
                                )
                                studentId = studentIdFromStudent
                            }
                        }
                        "studentId" -> studentId = `in`.nextInt()
                        "userId" -> userId = `in`.nextInt()
                        "date" -> date = LocalDate.parse(`in`.nextString(), formatter)
                        "status" -> status = `in`.nextString() ?: ""
                        "section" -> section = `in`.nextString() ?: ""
                        "user" -> {
                            `in`.beginObject()
                            var userIdFromUser = 0
                            var email: String? = null
                            var firstNameUser: String? = null
                            var lastNameUser: String? = null
                            var oauthId: String? = null
                            while (`in`.hasNext()) {
                                when (`in`.nextName()) {
                                    "userId" -> userIdFromUser = `in`.nextInt()
                                    "email" -> email = `in`.nextStringOrNull()
                                    "firstName" -> firstNameUser = `in`.nextStringOrNull()
                                    "lastName" -> lastNameUser = `in`.nextStringOrNull()
                                    "oauthId" -> oauthId = `in`.nextStringOrNull()
                                    else -> `in`.skipValue()
                                }
                            }
                            `in`.endObject()
                            user = User(
                                userId = userIdFromUser,
                                email = email,
                                firstName = firstNameUser,
                                lastName = lastNameUser,
                                oauthId = oauthId
                            )
                            userId = userIdFromUser
                        }
                        else -> `in`.skipValue()
                    }
                }
                `in`.endObject()
                return AttendanceRecord(
                    attendanceId = attendanceId,
                    studentId = studentId,
                    userId = userId,
                    date = date ?: LocalDate.now(),
                    status = status,
                    section = section,
                    user = user,
                    student = student
                )
            }
        }

        val gradeAdapter = object : TypeAdapter<Grade>() {
            override fun write(out: JsonWriter, value: Grade?) {
                if (value == null) {
                    out.nullValue()
                    return
                }
                out.beginObject()
                out.name("gradeId").value(value.gradeId)
                out.name("studentId").value(value.studentId)
                out.name("userId").value(value.userId)
                out.name("filipino").value(value.filipino)
                out.name("english").value(value.english)
                out.name("mathematics").value(value.mathematics)
                out.name("science").value(value.science)
                out.name("ap").value(value.ap)
                out.name("esp").value(value.esp)
                out.name("mapeh").value(value.mapeh)
                out.name("computer").value(value.computer)
                out.name("finalGrade").value(value.finalGrade)
                out.name("remarks").value(value.remarks)
                // Include student field with at least studentId
                out.name("student")
                out.beginObject()
                out.name("studentId").value(value.studentId)
                if (value.student != null) {
                    out.name("firstName").value(value.student.firstName)
                    out.name("lastName").value(value.student.lastName)
                    out.name("gender").value(value.student.gender)
                    out.name("section").value(value.student.section)
                    out.name("gradeLevel").value(value.student.gradeLevel)
                    out.name("attendanceStatus").value(value.student.attendanceStatus)
                    out.name("archived").value(value.student.isArchived)
                }
                out.endObject()
                // Include user field with at least userId
                out.name("user")
                out.beginObject()
                out.name("userId").value(value.userId)
                if (value.user != null) {
                    out.name("email").value(value.user.email)
                    out.name("firstName").value(value.user.firstName)
                    out.name("lastName").value(value.user.lastName)
                    out.name("oauthId").value(value.user.oauthId)
                }
                out.endObject()
                out.endObject()
            }

            override fun read(`in`: JsonReader): Grade {
                var gradeId = 0
                var studentId = 0
                var userId = 0
                var filipino = 0.0
                var english = 0.0
                var mathematics = 0.0
                var science = 0.0
                var ap = 0.0
                var esp = 0.0
                var mapeh = 0.0
                var computer = 0.0
                var finalGrade = 0.0
                var remarks = ""
                var student: Student? = null
                var user: User? = null

                `in`.beginObject()
                while (`in`.hasNext()) {
                    when (`in`.nextName()) {
                        "gradeId" -> gradeId = `in`.nextInt()
                        "studentId" -> studentId = `in`.nextInt()
                        "userId" -> userId = `in`.nextInt()
                        "filipino" -> filipino = `in`.nextDouble()
                        "english" -> english = `in`.nextDouble()
                        "mathematics" -> mathematics = `in`.nextDouble()
                        "science" -> science = `in`.nextDouble()
                        "ap" -> ap = `in`.nextDouble()
                        "esp" -> esp = `in`.nextDouble()
                        "mapeh" -> mapeh = `in`.nextDouble()
                        "computer" -> computer = `in`.nextDouble()
                        "finalGrade" -> finalGrade = `in`.nextDouble()
                        "remarks" -> remarks = `in`.nextString() ?: ""
                        "student" -> {
                            `in`.beginObject()
                            var studentIdFromStudent = 0
                            var firstName: String? = null
                            var lastName: String? = null
                            var gender: String? = null
                            var section: String? = null
                            var gradeLevel: String? = null
                            var attendanceStatus: String? = null
                            var isArchived = false
                            while (`in`.hasNext()) {
                                when (`in`.nextName()) {
                                    "studentId" -> studentIdFromStudent = `in`.nextInt()
                                    "firstName" -> firstName = `in`.nextStringOrNull()
                                    "lastName" -> lastName = `in`.nextStringOrNull()
                                    "gender" -> gender = `in`.nextStringOrNull()
                                    "section" -> section = `in`.nextStringOrNull()
                                    "gradeLevel" -> gradeLevel = `in`.nextStringOrNull()
                                    "attendanceStatus" -> attendanceStatus = `in`.nextStringOrNull()
                                    "archived" -> isArchived = `in`.nextBoolean()
                                    else -> `in`.skipValue()
                                }
                            }
                            `in`.endObject()
                            student = Student(
                                studentId = studentIdFromStudent,
                                userId = 0,
                                user = null,
                                firstName = firstName ?: "",
                                lastName = lastName ?: "",
                                gender = gender ?: "",
                                section = section ?: "",
                                gradeLevel = gradeLevel ?: "",
                                attendanceStatus = attendanceStatus ?: "",
                                isArchived = isArchived
                            )
                            studentId = studentIdFromStudent
                        }
                        "user" -> {
                            `in`.beginObject()
                            var userIdFromUser = 0
                            var email: String? = null
                            var firstNameUser: String? = null
                            var lastNameUser: String? = null
                            var oauthId: String? = null
                            while (`in`.hasNext()) {
                                when (`in`.nextName()) {
                                    "userId" -> userIdFromUser = `in`.nextInt()
                                    "email" -> email = `in`.nextStringOrNull()
                                    "firstName" -> firstNameUser = `in`.nextStringOrNull()
                                    "lastName" -> lastNameUser = `in`.nextStringOrNull()
                                    "oauthId" -> oauthId = `in`.nextStringOrNull()
                                    else -> `in`.skipValue()
                                }
                            }
                            `in`.endObject()
                            user = User(
                                userId = userIdFromUser,
                                email = email,
                                firstName = firstNameUser,
                                lastName = lastNameUser,
                                oauthId = oauthId
                            )
                            userId = userIdFromUser
                        }
                        else -> `in`.skipValue()
                    }
                }
                `in`.endObject()
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
                    remarks = remarks
                )
            }
        }

        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, localDateAdapter)
            .registerTypeAdapter(Calendar::class.java, calendarAdapter)
            .registerTypeAdapter(Student::class.java, studentAdapter)
            .registerTypeAdapter(AttendanceRecord::class.java, attendanceRecordAdapter)
            .registerTypeAdapter(Grade::class.java, gradeAdapter)
            .create()

        Log.d(TAG, "Gson configured with LocalDate, Calendar, Student, AttendanceRecord, and Grade adapters")

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        apiService = retrofit.create(ApiService::class.java)
        Log.d(TAG, "ApiService initialized for $environment environment")
    }
}

private fun JsonReader.nextStringOrNull(): String? {
    return if (peek() == JsonToken.NULL) {
        nextNull()
        null
    } else {
        nextString()
    }
}