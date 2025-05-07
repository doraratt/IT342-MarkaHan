package com.example.markahanmobile.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.markahanmobile.helper.ApiClient
import com.example.markahanmobile.helper.ApiService
import com.example.markahanmobile.helper.CalendarRequest
import com.example.markahanmobile.helper.LoginRequest
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.EOFException
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DataStore {
    private const val TAG = "DataStore"

    private val students = mutableListOf<Student>()
    private val attendanceRecords = mutableListOf<AttendanceRecord>()
    private val grades = mutableListOf<Grade>()
    private val journals = mutableListOf<Journal>()
    private val events = mutableListOf<Calendar>()
    private var currentUser: User? = null

    private lateinit var context: Context
    private val apiService: ApiService by lazy { ApiClient.apiService }
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, object : JsonSerializer<LocalDate> {
            override fun serialize(src: LocalDate, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
                return JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE))
            }
        })
        .create()

    fun init(context: Context) {
        this.context = context
        ApiClient.init(context)
    }

    fun signup(user: User, onComplete: (User?, Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val createdUser = apiService.signup(user)
                currentUser = createdUser
                saveUserData(createdUser)
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "User signed up: ${createdUser.userId}, ${createdUser.email}")
                    onComplete(createdUser, true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to sign up: ${e.message}", e)
                    onComplete(null, false)
                }
            }
        }
    }

    fun login(email: String, password: String, onComplete: (User?, Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val loginRequest = LoginRequest(email, password)
                val response: Response<LoginResponse> = apiService.login(loginRequest)
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse == null) {
                        withContext(Dispatchers.Main) {
                            Log.e(TAG, "Login failed: Response body is null")
                            onComplete(null, false)
                        }
                        return@launch
                    }
                    val user = User(
                        userId = loginResponse.userId,
                        firstName = loginResponse.firstName,
                        lastName = loginResponse.lastName,
                        email = loginResponse.email,
                        oauthId = loginResponse.oauthId
                    )
                    currentUser = user
                    saveUserData(user)
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "User logged in: ${user.userId}, ${user.email}")
                        onComplete(user, true)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "Login failed: HTTP ${response.code()} - ${response.message()}")
                        Log.e(TAG, "Error body: ${response.errorBody()?.string()}")
                        onComplete(null, false)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to log in: ${e.message}", e)
                    onComplete(null, false)
                }
            }
        }
    }

    private fun saveUserData(user: User?) {
        if (user == null) {
            Log.e(TAG, "saveUserData: User is null, cannot save data")
            return
        }
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putInt("userId", user.userId)
            .putString("email", user.email)
            .putString("firstName", user.firstName)
            .putString("lastName", user.lastName)
            .putString("oauthId", user.oauthId)
            .apply()
        Log.d(TAG, "Saved user data: userId=${user.userId}")
    }

    fun getCurrentUser(onComplete: (Map<String, Any>?, Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userDetails = apiService.getCurrentUser()
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Fetched current user: $userDetails")
                    onComplete(userDetails, true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to fetch current user: ${e.message}", e)
                    onComplete(null, false)
                }
            }
        }
    }

    fun getLoggedInUser(): User? {
        if (currentUser == null) {
            val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getInt("userId", -1)
            val email = sharedPreferences.getString("email", null)
            val firstName = sharedPreferences.getString("firstName", null) ?: ""
            val lastName = sharedPreferences.getString("lastName", null) ?: ""
            val oauthId = sharedPreferences.getString("oauthId", null)
            Log.d(TAG, "getLoggedInUser: currentUser is null, userId=$userId, email=$email")

            if (userId != -1 && email != null) {
                currentUser = User(
                    userId = userId,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    oauthId = oauthId
                )
            } else {
                Log.w(TAG, "No valid user data in SharedPreferences")
            }
        }
        Log.d(TAG, "getLoggedInUser: returning currentUser=$currentUser")
        return currentUser
    }

    fun logout() {
        currentUser = null
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        students.clear()
        attendanceRecords.clear()
        grades.clear()
        journals.clear()
        events.clear()
        Log.d(TAG, "User logged out, cleared local data")
    }

    fun syncStudents(userId: Int, includeArchived: Boolean = false, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "syncStudents: Fetching students for userId=$userId, includeArchived=$includeArchived")
                val response = apiService.getStudentsByUser(userId, includeArchived)
                students.clear()
                students.addAll(response)
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Synced ${students.size} students")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to sync students: ${e.message}", e)
                    onComplete(false)
                }
            }
        }
    }

    fun addStudent(student: Student, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val addedStudent = apiService.addStudent(student)
                students.add(addedStudent)
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Added student: ${addedStudent.studentId}")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to add student: ${e.message}", e)
                    onComplete(false)
                }
            }
        }
    }

    fun updateStudent(updatedStudent: Student, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val jsonPayload = gson.toJson(updatedStudent)
                Log.d(TAG, "updateStudent: Sending payload for studentId=${updatedStudent.studentId}: $jsonPayload")
                val returnedStudent = apiService.updateStudent(updatedStudent.studentId, updatedStudent)
                val index = students.indexOfFirst { it.studentId == updatedStudent.studentId }
                if (index != -1) {
                    students[index] = returnedStudent
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "Updated student: ${updatedStudent.studentId}")
                        onComplete(true)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "Student with ID ${updatedStudent.studentId} not found locally")
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to update student: ${e.message}", e)
                    onComplete(false)
                }
            }
        }
    }

    fun archiveStudent(studentId: Int, isArchived: Boolean = true, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val studentIndex = students.indexOfFirst { it.studentId == studentId }
                if (studentIndex == -1) {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "Student with ID $studentId not found locally")
                        onComplete(false)
                    }
                    return@launch
                }
                val student = students[studentIndex]
                val user = student.user ?: getLoggedInUser() ?: run {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "No user data available for student $studentId")
                        onComplete(false)
                    }
                    return@launch
                }
                val updatedStudent = student.copy(
                    isArchived = isArchived,
                    user = User(userId = user.userId),
                    userId = user.userId
                )
                val jsonPayload = gson.toJson(updatedStudent)
                Log.d(TAG, "archiveStudent: Sending payload for studentId=$studentId: $jsonPayload")
                val returnedStudent = apiService.updateStudent(studentId, updatedStudent)
                students[studentIndex] = returnedStudent
                val userId = getLoggedInUser()?.userId ?: -1
                if (userId != -1) {
                    val response = apiService.getStudentsByUser(userId, true)
                    students.clear()
                    students.addAll(response)
                }
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "${if (isArchived) "Archived" else "Unarchived"} student: $studentId")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to ${if (isArchived) "archive" else "unarchive"} student: ${e.message}", e)
                    onComplete(false)
                }
            }
        }
    }

    fun getStudents(section: String? = null, includeArchived: Boolean = false): List<Student> {
        val filteredStudents = if (includeArchived) students else students.filter { !it.isArchived }
        val result = if (section != null) {
            filteredStudents.filter { it.section.equals(section, ignoreCase = true) }
        } else {
            filteredStudents.toList()
        }
        Log.d(TAG, "Fetched students: ${result.size} for section: $section")
        return result
    }

    fun getSections(includeArchived: Boolean = false): List<String> {
        val sections = students.filter { if (includeArchived) true else !it.isArchived }
            .map { it.section }
            .distinct()
            .toMutableList()
        Log.d(TAG, "Fetched sections: $sections")
        return sections
    }

    fun syncAttendanceRecords(userId: Int, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "syncAttendanceRecords: Fetching for userId=$userId")
                val response = apiService.getAttendanceByUser(userId)
                attendanceRecords.clear()
                val updatedRecords = response.map { record ->
                    val student = students.firstOrNull { it.studentId == record.studentId }
                    if (student != null) {
                        record.copy(section = student.section, student = student)
                    } else {
                        Log.w(TAG, "No student found for studentId=${record.studentId}")
                        null
                    }
                }.filterNotNull()
                attendanceRecords.addAll(updatedRecords)
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Synced ${attendanceRecords.size} attendance records")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to sync attendance records: ${e.message}", e)
                    onComplete(false)
                }
            }
        }
    }

    fun addAttendanceRecords(records: List<AttendanceRecord>, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val addedRecords = mutableListOf<AttendanceRecord>()
                val invalidStudentIds = mutableListOf<Int>()
                for (record in records) {
                    if (record.studentId <= 0 || record.userId <= 0) {
                        Log.e(TAG, "Invalid record: studentId=${record.studentId}, userId=${record.userId}")
                        invalidStudentIds.add(record.studentId)
                        continue
                    }
                    if (record.status !in listOf("Present", "Absent", "Late")) {
                        Log.e(TAG, "Invalid status: ${record.status} for studentId=${record.studentId}")
                        invalidStudentIds.add(record.studentId)
                        continue
                    }
                    val student = students.firstOrNull { it.studentId == record.studentId }
                    if (student == null) {
                        Log.e(TAG, "No student found for studentId=${record.studentId}")
                        invalidStudentIds.add(record.studentId)
                        continue
                    }
                    val validatedRecord = record.copy(
                        student = student,
                        section = student.section
                    )
                    val jsonPayload = gson.toJson(validatedRecord)
                    Log.d(TAG, "addAttendanceRecords: Sending payload for studentId=${validatedRecord.studentId}: $jsonPayload")
                    val response = apiService.postAttendance(validatedRecord)
                    addedRecords.add(response)
                }
                if (invalidStudentIds.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "Invalid student IDs: $invalidStudentIds")
                        Toast.makeText(context, "Some records skipped due to invalid student IDs: $invalidStudentIds", Toast.LENGTH_LONG).show()
                    }
                }
                if (addedRecords.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "No valid attendance records to add")
                        Toast.makeText(context, "No valid attendance records to save", Toast.LENGTH_SHORT).show()
                        onComplete(false)
                    }
                    return@launch
                }
                attendanceRecords.removeAll { existingRecord ->
                    addedRecords.any { newRecord ->
                        existingRecord.studentId == newRecord.studentId &&
                                existingRecord.section == newRecord.section &&
                                existingRecord.date.isEqual(newRecord.date)
                    }
                }
                attendanceRecords.addAll(addedRecords)
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Added ${addedRecords.size} attendance records")
                    onComplete(true)
                }
            } catch (e: EOFException) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Server sent incomplete response (EOF)", e)
                    Toast.makeText(context, "Server error: Incomplete response", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to add attendance records: HTTP ${e.code()} - ${e.message()}")
                    Log.e(TAG, "Error body: $errorBody")
                    if (errorBody?.contains("AttendanceEntity.getStudent()") == true) {
                        Toast.makeText(context, "Invalid student ID. Please sync students.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Server error: Failed to save attendance", Toast.LENGTH_SHORT).show()
                    }
                    onComplete(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to add attendance records: ${e.message}", e)
                    Toast.makeText(context, "Error saving attendance: ${e.message}", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
            }
        }
    }

    fun getAttendanceRecords(section: String?, startDate: LocalDate, endDate: LocalDate): List<AttendanceRecord> {
        val records = attendanceRecords.filter {
            (section == null || it.section.equals(section, ignoreCase = true)) &&
                    (it.date.isAfter(startDate) || it.date.isEqual(startDate)) &&
                    (it.date.isBefore(endDate) || it.date.isEqual(endDate))
        }
        Log.d(TAG, "Fetched ${records.size} attendance records")
        return records
    }

    fun syncGrades(userId: Int, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "syncGrades: Fetching for userId=$userId")
                val response = apiService.getGradesByUser(userId)
                grades.clear()
                grades.addAll(response)
                students.forEach { student ->
                    val grade = grades.firstOrNull { it.studentId == student.studentId }
                    if (grade != null) {
                        val index = students.indexOf(student)
                        students[index] = student.copy(grade = grade)
                    }
                }
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Synced ${grades.size} grades")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to sync grades: ${e.message}", e)
                    onComplete(false)
                }
            }
        }
    }

    fun updateGrade(grade: Grade, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Validate Grade object
                if (grade.student == null || grade.student.studentId == 0) {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "Invalid grade: student is null or has invalid studentId")
                        Toast.makeText(context, "Error: Invalid student data", Toast.LENGTH_SHORT).show()
                        onComplete(false)
                    }
                    return@launch
                }
                if (grade.user == null || grade.user.userId == 0) {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "Invalid grade: user is null or has invalid userId")
                        Toast.makeText(context, "Error: Invalid user data", Toast.LENGTH_SHORT).show()
                        onComplete(false)
                    }
                    return@launch
                }
                if (grade.studentId != grade.student.studentId || grade.userId != grade.user.userId) {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "Mismatched IDs: studentId=${grade.studentId}, student.studentId=${grade.student.studentId}, userId=${grade.userId}, user.userId=${grade.user.userId}")
                        Toast.makeText(context, "Error: Inconsistent student or user IDs", Toast.LENGTH_SHORT).show()
                        onComplete(false)
                    }
                    return@launch
                }
                // Log the JSON payload
                val jsonPayload = gson.toJson(grade)
                Log.d(TAG, "updateGrade: Sending payload for studentId=${grade.studentId}, gradeId=${grade.gradeId}: $jsonPayload")
                // Send request
                val updatedGrade = if (grade.gradeId == 0) {
                    apiService.postGrade(grade)
                } else {
                    apiService.putGrade(grade.gradeId, grade)
                }
                // Update local data
                val index = grades.indexOfFirst { it.gradeId == updatedGrade.gradeId || (it.gradeId == 0 && it.studentId == updatedGrade.studentId) }
                if (index != -1) {
                    grades[index] = updatedGrade
                } else {
                    grades.add(updatedGrade)
                }
                val studentIndex = students.indexOfFirst { it.studentId == updatedGrade.studentId }
                if (studentIndex != -1) {
                    students[studentIndex] = students[studentIndex].copy(grade = updatedGrade)
                }
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Updated grade for studentId: ${updatedGrade.studentId}")
                    Toast.makeText(context, "Grades saved successfully", Toast.LENGTH_SHORT).show()
                    onComplete(true)
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to update grade: HTTP ${e.code()} - ${e.message()}")
                    Log.e(TAG, "Error body: $errorBody")
                    when {
                        errorBody?.contains("GradeEntity.getStudent()") == true -> {
                            Log.e(TAG, "Server error: Invalid or missing student data")
                            Toast.makeText(context, "Error: Invalid student data. Please sync students.", Toast.LENGTH_LONG).show()
                        }
                        errorBody?.contains("GradeEntity.getUser()") == true -> {
                            Log.e(TAG, "Server error: Invalid or missing user data")
                            Toast.makeText(context, "Error: Invalid user data. Please log in again.", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Log.e(TAG, "Unknown server error")
                            Toast.makeText(context, "Server error: Failed to save grades", Toast.LENGTH_SHORT).show()
                        }
                    }
                    onComplete(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to update grade: ${e.message}", e)
                    Toast.makeText(context, "Error saving grades: ${e.message}", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
            }
        }
    }

    fun getGradeByStudent(studentId: Int): Grade? {
        return grades.firstOrNull { it.studentId == studentId }
    }

    fun deleteStudent(studentId: Int, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiService.deleteStudent(studentId)
                students.removeAll { it.studentId == studentId }
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Deleted student: $studentId")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to delete student: ${e.message}", e)
                    onComplete(false)
                }
            }
        }
    }

    fun syncJournals(userId: Int, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "syncJournals: Fetching for userId=$userId")
                val response = apiService.getJournalsByUser(userId)
                journals.clear()
                journals.addAll(response)
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Synced ${journals.size} journals")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to sync journals: ${e.message}", e)
                    onComplete(false)
                }
            }
        }
    }

    fun getJournals(): List<Journal> {
        val sortedJournals = journals.sortedByDescending { it.date }
        Log.d(TAG, "Fetched ${sortedJournals.size} journals")
        return sortedJournals
    }

    fun addJournal(journal: Journal, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<Journal> = apiService.postJournal(journal)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val addedJournal = response.body()
                        if (addedJournal != null) {
                            journals.add(addedJournal)
                            Log.d(TAG, "Added journal: ${addedJournal.journalId}")
                            onComplete(true)
                        } else {
                            Log.e(TAG, "addJournal: Response body is null")
                            onComplete(false)
                        }
                    } else {
                        Log.e(TAG, "addJournal: HTTP ${response.code()}")
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to add journal: ${e.message}", e)
                    onComplete(false)
                }
            }
        }
    }

    fun updateJournal(journal: Journal, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val updatedJournal = apiService.updateJournal(journal.journalId, journal)
                val index = journals.indexOfFirst { it.journalId == journal.journalId }
                if (index != -1) {
                    journals[index] = updatedJournal
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "Updated journal: ${updatedJournal.journalId}")
                        onComplete(true)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "Journal with ID ${journal.journalId} not found locally")
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to update journal: ${e.message}", e)
                    onComplete(false)
                }
            }
        }
    }

    fun deleteJournal(journalId: Int, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<String> = apiService.deleteJournal(journalId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        journals.removeAll { it.journalId == journalId }
                        Log.d(TAG, "Deleted journal: $journalId")
                        onComplete(true)
                    } else {
                        Log.e(TAG, "Failed to delete journal: HTTP ${response.code()}")
                        journals.removeAll { it.journalId == journalId }
                        onComplete(true)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Exception while deleting journal: ${e.message}", e)
                    journals.removeAll { it.journalId == journalId }
                    onComplete(true)
                }
            }
        }
    }

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun syncEvents(userId: Int, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "syncEvents: Fetching for userId=$userId")
                val response = apiService.getEventsByUser(userId)
                events.clear()
                events.addAll(response)
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Synced ${events.size} events")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to sync events: ${e.message}", e)
                    onComplete(false)
                }
            }
        }
    }

    fun getEventsByDate(date: String): List<Calendar> {
        val filteredEvents = events.filter { it.date == date }
        Log.d(TAG, "Fetched ${filteredEvents.size} events for date: $date")
        return filteredEvents
    }

    fun getLatestEvents(limit: Int = 2): List<Calendar> {
        val sortedEvents = events.sortedByDescending {
            try {
                LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse event date: ${it.date}, using epoch start")
                LocalDate.ofEpochDay(0)
            }
        }.take(limit)
        Log.d(TAG, "Fetched ${sortedEvents.size} latest events: ${sortedEvents.joinToString { "${it.eventDescription} (${it.date})" }}")
        return sortedEvents
    }

    fun addEvent(event: Calendar, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val eventPayload = CalendarRequest(
                    calendarId = event.calendarId,
                    user = CalendarRequest.User(userId = event.userId),
                    eventDescription = event.eventDescription,
                    date = event.date
                )
                Log.d(TAG, "addEvent: Sending payload $eventPayload")
                val addedEvent = apiService.addEvent(eventPayload)
                events.add(addedEvent)
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Added event: ${addedEvent.calendarId}")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to add event: ${e.message}", e)
                    onComplete(false)
                }
            }
        }
    }

    fun updateEvent(event: Calendar, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = getLoggedInUser()
                val userId = if (event.userId != 0) event.userId else currentUser?.userId ?: -1
                if (userId == -1) {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "updateEvent: No valid userId found")
                        onComplete(false)
                    }
                    return@launch
                }
                val eventPayload = CalendarRequest(
                    calendarId = event.calendarId,
                    user = CalendarRequest.User(userId = userId),
                    eventDescription = event.eventDescription,
                    date = event.date
                )
                Log.d(TAG, "updateEvent: Sending payload for calendarId=${event.calendarId}")
                val response: Response<Calendar> = apiService.updateEvent(event.calendarId, eventPayload)
                if (response.isSuccessful) {
                    val updatedEvent = response.body()
                    if (updatedEvent != null) {
                        val index = events.indexOfFirst { it.calendarId == event.calendarId }
                        if (index != -1) {
                            events[index] = updatedEvent
                        } else {
                            events.add(updatedEvent)
                        }
                        withContext(Dispatchers.Main) {
                            Log.d(TAG, "Updated event: ${updatedEvent.calendarId}")
                            onComplete(true)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Log.e(TAG, "updateEvent: Response body is null")
                            onComplete(false)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "updateEvent: HTTP ${response.code()}")
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to update event: ${e.message}", e)
                    onComplete(false)
                }
            }
        }
    }

    fun deleteEvent(eventId: Int, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "deleteEvent: Attempting to delete eventId=$eventId")
                val response: Response<ResponseBody> = apiService.deleteEvent(eventId)
                if (response.isSuccessful) {
                    events.removeAll { it.calendarId == eventId }
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "Deleted event: $eventId")
                        onComplete(true)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "Failed to delete event: HTTP ${response.code()}")
                        events.removeAll { it.calendarId == eventId }
                        onComplete(true)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Exception while deleting event: ${e.message}", e)
                    events.removeAll { it.calendarId == eventId }
                    onComplete(true)
                }
            }
        }
    }
}