package com.example.markahanmobile.helper

import com.example.markahanmobile.data.AttendanceRecord
import com.example.markahanmobile.data.Calendar
import com.example.markahanmobile.data.Grade
import com.example.markahanmobile.data.Journal
import com.example.markahanmobile.data.LoginResponse
import com.example.markahanmobile.data.Student
import com.example.markahanmobile.data.User
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // User Endpoints
    @POST("api/user/signup")
    suspend fun signup(@Body user: User): User

    @POST("api/user/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @GET("api/user/getAllUser")
    suspend fun getAllUsers(): List<User>

    @POST("api/user/postUser")
    suspend fun postUser(@Body user: User): User

    @PUT("api/user/putUser/{id}")
    suspend fun putUser(@Path("id") id: Int, @Body user: User): User

    @DELETE("api/user/deleteUser/{id}")
    suspend fun deleteUser(@Path("id") id: Int): String

    @GET("api/user/me")
    suspend fun getCurrentUser(): Map<String, Any>

    // Student Endpoints
    @POST("api/student/add")
    suspend fun addStudent(@Body student: Student): Student

    @GET("api/student/all")
    suspend fun getAllStudents(): List<Student>

    @GET("api/student/getStudentsByUser")
    suspend fun getStudentsByUser(
        @Query("userId") userId: Int,
        @Query("includeArchived") includeArchived: Boolean = false
    ): List<Student>

    @PUT("api/student/update/{id}")
    suspend fun updateStudent(@Path("id") id: Int, @Body student: Student): Student

    @DELETE("api/student/delete/{id}")
    suspend fun deleteStudent(@Path("id") id: Int): String

    // Attendance Endpoints
    @POST("api/attendance/postAttendance")
    suspend fun postAttendance(@Body attendance: AttendanceRecord): AttendanceRecord

    @GET("api/attendance/getAllAttendance")
    suspend fun getAllAttendance(): List<AttendanceRecord>

    @GET("api/attendance/getAttendanceByUser")
    suspend fun getAttendanceByUser(@Query("userId") userId: Int): List<AttendanceRecord>

    @PUT("api/attendance/putAttendance/{id}")
    suspend fun putAttendance(@Path("id") id: Int, @Body attendance: AttendanceRecord): AttendanceRecord

    @DELETE("api/attendance/deleteAttendance/{id}")
    suspend fun deleteAttendance(@Path("id") id: Int): String

    // Grade Endpoints
    @POST("api/grade/postGrade")
    suspend fun postGrade(@Body grade: Grade): Grade

    @GET("api/grade/getAllGrades")
    suspend fun getAllGrades(): List<Grade>

    @GET("api/grade/getGradesByUser")
    suspend fun getGradesByUser(@Query("userId") userId: Int): List<Grade>

    @PUT("api/grade/putGrade/{id}")
    suspend fun putGrade(@Path("id") id: Int, @Body grade: Grade): Grade

    @DELETE("api/grade/deleteGrade/{id}")
    suspend fun deleteGrade(@Path("id") id: Int): String

    @GET("api/grade/getGradesByStudent")
    suspend fun getGradesByStudent(@Query("studentId") studentId: Int): Grade?

    // Journal Endpoints
    @POST("api/journal/post")
    suspend fun postJournal(@Body journal: Journal): Response<Journal>

    @GET("api/journal/getAll")
    suspend fun getAllJournals(): List<Journal>

    @GET("api/journal/getJournalsByUser")
    suspend fun getJournalsByUser(@Query("userId") userId: Int): List<Journal>

    @PUT("api/journal/update/{id}")
    suspend fun updateJournal(@Path("id") id: Int, @Body journal: Journal): Journal

    @DELETE("api/journal/delete/{id}")
    suspend fun deleteJournal(@Path("id") id: Int): Response<String>

    // Calendar Endpoints
    @POST("api/eventcalendar/addEventCal")
    suspend fun addEvent(@Body event: CalendarRequest): Calendar

    @GET("api/eventcalendar/getEventByUser")
    suspend fun getEventsByUser(@Query("userId") userId: Int): List<Calendar>

    @GET("api/eventcalendar/getAllEvent")
    suspend fun getAllEvents(): List<Calendar>

    @GET("api/eventcalendar/getEventsCalByDate")
    suspend fun getEventsByDate(@Query("date") date: String): List<Calendar>

    @GET("api/eventcalendar/getEventsByDateRange")
    suspend fun getEventsByDateRange(
        @Query("userId") userId: Int,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): List<Calendar>

    @PUT("api/eventcalendar/updateEventCal/{calendarId}")
    suspend fun updateEvent(@Path("calendarId") id: Int, @Body event: CalendarRequest): Response<Calendar>

    @DELETE("api/eventcalendar/deleteEventCal/{calendarId}")
    suspend fun deleteEvent(@Path("calendarId") id: Int): Response<ResponseBody>
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class CalendarRequest(
    val calendarId: Int,
    val user: User?,
    val eventDescription: String,
    val date: String
) {
    data class User(
        val userId: Int
    )
}