package com.example.markahanmobile.helper

import android.content.Context
import android.util.Log
import com.example.markahanmobile.BuildConfig
import com.example.markahanmobile.data.AttendanceRecord
import com.example.markahanmobile.data.Calendar
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

        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
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
                // Avoid serializing the grade field to prevent circular reference
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
                        "grade" -> `in`.skipValue() // Skip grade to avoid circular reference
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
                Log.d(TAG, "Serializing AttendanceRecord: attendanceId=${value.attendanceId}")
                out.name("attendanceId").value(value.attendanceId)
                Log.d(TAG, "Serializing AttendanceRecord: studentId=${value.studentId}")
                out.name("studentId").value(value.studentId)
                Log.d(TAG, "Serializing AttendanceRecord: userId=${value.userId}")
                out.name("userId").value(value.userId)
                val dateString = value.date.format(formatter)
                Log.d(TAG, "Serializing AttendanceRecord: date=$dateString")
                out.name("date").value(dateString)
                Log.d(TAG, "Serializing AttendanceRecord: status=${value.status}")
                out.name("status").value(value.status)
                Log.d(TAG, "Serializing AttendanceRecord: section=${value.section}")
                out.name("section").value(value.section)
                if (value.user != null) {
                    Log.d(TAG, "Serializing AttendanceRecord: user.userId=${value.user.userId}")
                    out.name("user")
                    out.beginObject()
                    out.name("userId").value(value.user.userId)
                    out.endObject()
                }
                if (value.student != null) {
                    Log.d(TAG, "Serializing AttendanceRecord: student.studentId=${value.student.studentId}")
                    out.name("student")
                    out.beginObject()
                    out.name("studentId").value(value.student.studentId)
                    out.name("userId").value(value.student.userId)
                    out.name("firstName").value(value.student.firstName)
                    out.name("lastName").value(value.student.lastName)
                    out.name("gender").value(value.student.gender)
                    out.name("section").value(value.student.section)
                    out.name("gradeLevel").value(value.student.gradeLevel)
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
                        "studentId" -> studentId = `in`.nextInt()
                        "userId" -> userId = `in`.nextInt()
                        "date" -> date = try {
                            LocalDate.parse(`in`.nextString(), formatter)
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to parse date with ISO_LOCAL_DATE, trying fallback")
                            LocalDate.parse(`in`.nextString(), formatterFallback)
                        }
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
                        "student" -> {
                            `in`.beginObject()
                            var studentIdFromStudent = 0
                            var userIdFromStudent = 0
                            var firstName = ""
                            var lastName = ""
                            var gender = ""
                            var sectionFromStudent = ""
                            var gradeLevel = ""
                            var isArchived = false
                            while (`in`.hasNext()) {
                                when (`in`.nextName()) {
                                    "studentId" -> studentIdFromStudent = `in`.nextInt()
                                    "userId" -> userIdFromStudent = `in`.nextInt()
                                    "firstName" -> firstName = `in`.nextString() ?: ""
                                    "lastName" -> lastName = `in`.nextString() ?: ""
                                    "gender" -> gender = `in`.nextString() ?: ""
                                    "section" -> sectionFromStudent = `in`.nextString() ?: ""
                                    "gradeLevel" -> gradeLevel = `in`.nextString() ?: ""
                                    "archived" -> isArchived = `in`.nextBoolean()
                                    "grade" -> `in`.skipValue() // Skip grade to avoid circular reference
                                    else -> `in`.skipValue()
                                }
                            }
                            `in`.endObject()
                            student = Student(
                                studentId = studentIdFromStudent,
                                userId = userIdFromStudent,
                                firstName = firstName,
                                lastName = lastName,
                                gender = gender,
                                section = sectionFromStudent,
                                gradeLevel = gradeLevel,
                                isArchived = isArchived
                            )
                            studentId = studentIdFromStudent
                            section = sectionFromStudent
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

        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, localDateAdapter)
            .registerTypeAdapter(Calendar::class.java, calendarAdapter)
            .registerTypeAdapter(Student::class.java, studentAdapter)
            .registerTypeAdapter(AttendanceRecord::class.java, attendanceRecordAdapter)
            .create()

        Log.d(TAG, "Gson configured with LocalDate, Calendar, Student, and AttendanceRecord adapters")

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