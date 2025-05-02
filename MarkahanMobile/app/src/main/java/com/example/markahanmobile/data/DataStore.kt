package com.example.markahanmobile.data

import android.content.Context
import android.util.Log
import com.example.markahanmobile.helper.ApiClient
import com.example.markahanmobile.helper.ApiService
import com.example.markahanmobile.helper.LoginRequest
import com.example.markahanmobile.helper.SectionAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.time.LocalDate

object DataStore {
    private const val TAG = "DataStore"

    private val students = mutableListOf<Student>()
    private val attendanceRecords = mutableListOf<AttendanceRecord>()
    private val grades = mutableListOf<Grade>()
    private val journals = mutableListOf<Journal>()
    private val events = mutableListOf<Calendar>()
    private var currentUser: User? = null

    // Inject Context for SharedPreferences
    private lateinit var context: Context
    private val apiService: ApiService by lazy { ApiClient.apiService }

    fun init(context: Context) {
        this.context = context
        ApiClient.init(context)
    }

    // User Operations
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
                    e.printStackTrace()
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
                    // Convert LoginResponse to User
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
                    e.printStackTrace()
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
                    e.printStackTrace()
                    onComplete(null, false)
                }
            }
        }
    }

    fun getLoggedInUser(): User? {
        Log.d(TAG, "getLoggedInUser: currentUser=$currentUser")
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

    // Student Operations
    fun syncStudents(userId: Int, includeArchived: Boolean = false, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "syncStudents: Fetching students for userId=$userId, includeArchived=$includeArchived")
                val response = apiService.getStudentsByUser(userId, includeArchived)
                students.clear()
                students.addAll(response)
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Synced ${students.size} students: ${students.map { "${it.studentId}, ${it.firstName} ${it.lastName}" }}")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to sync students: ${e.message}", e)
                    e.printStackTrace()
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
                    Log.d(TAG, "Added student: ${addedStudent.studentId}, ${addedStudent.firstName} ${addedStudent.lastName}")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to add student: ${e.message}", e)
                    e.printStackTrace()
                    onComplete(false)
                }
            }
        }
    }

    fun updateStudent(updatedStudent: Student, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val returnedStudent = apiService.updateStudent(updatedStudent.studentId, updatedStudent)
                val index = students.indexOfFirst { it.studentId == updatedStudent.studentId }
                if (index != -1) {
                    students[index] = returnedStudent
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "Updated student: ${updatedStudent.studentId}, Grade: ${updatedStudent.grade?.finalGrade}")
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
                    e.printStackTrace()
                    onComplete(false)
                }
            }
        }
    }

    fun archiveStudent(studentId: Int, isArchived: Boolean = true, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val updatedStudent = apiService.archiveStudent(studentId, isArchived)
                val index = students.indexOfFirst { it.studentId == studentId }
                if (index != -1) {
                    students[index] = updatedStudent
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "${if (isArchived) "Archived" else "Unarchived"} student: $studentId")
                        onComplete(true)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "Student with ID $studentId not found locally")
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to ${if (isArchived) "archive" else "unarchive"} student: ${e.message}", e)
                    e.printStackTrace()
                    onComplete(false)
                }
            }
        }
    }

    fun getStudents(section: String? = null, includeArchived: Boolean = false): List<Student> {
        val filteredStudents = if (includeArchived) students else students.filter { !it.isArchived }
        val result = if (section != null && section != SectionAdapter.ALL_SECTIONS) {
            filteredStudents.filter { it.section.equals(section, ignoreCase = true) }
        } else {
            filteredStudents.toList()
        }
        Log.d(TAG, "Fetched students: ${result.size} for section: $section")
        return result
    }

    fun getSections(): List<String> {
        val sections = students.filter { !it.isArchived }
            .map { it.section }
            .distinct()
            .toMutableList()
            .apply {
                add(0, SectionAdapter.ALL_SECTIONS)
            }
        Log.d(TAG, "Fetched sections: $sections")
        return sections
    }

    // Attendance Operations
    fun syncAttendanceRecords(userId: Int, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "syncAttendanceRecords: Fetching for userId=$userId")
                val response = apiService.getAttendanceByUser(userId)
                attendanceRecords.clear()
                attendanceRecords.addAll(response)
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Synced ${attendanceRecords.size} attendance records")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to sync attendance records: ${e.message}", e)
                    e.printStackTrace()
                    onComplete(false)
                }
            }
        }
    }

    fun addAttendanceRecords(records: List<AttendanceRecord>, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "addAttendanceRecords: Sending records=$records")
                val addedRecords = mutableListOf<AttendanceRecord>()
                for (record in records) {
                    val addedRecord = apiService.postAttendance(record)
                    addedRecords.add(addedRecord)
                }
                attendanceRecords.removeAll { existingRecord ->
                    records.any { newRecord ->
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
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to add attendance records: ${e.message}", e)
                    e.printStackTrace()
                    onComplete(false)
                }
            }
        }
    }

    fun getAttendanceRecords(section: String, startDate: LocalDate, endDate: LocalDate): List<AttendanceRecord> {
        val records = attendanceRecords.filter {
            it.section == section &&
                    (it.date.isAfter(startDate) || it.date.isEqual(startDate)) &&
                    (it.date.isBefore(endDate) || it.date.isEqual(endDate))
        }
        Log.d(TAG, "Fetched ${records.size} attendance records for section: $section")
        return records
    }

    // Grade Operations
    fun syncGrades(userId: Int, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "syncGrades: Students before: ${students.map { "${it.studentId}, Grade=${it.grade?.gradeId}" }}")
                val response = apiService.getGradesByUser(userId)
                Log.d(TAG, "syncGrades: Fetched ${response.size} grades: $response")
                grades.clear()
                grades.addAll(response)
                students.forEach { student ->
                    val grade = grades.firstOrNull { it.studentId == student.studentId }
                    if (grade != null) {
                        val index = students.indexOf(student)
                        students[index] = student.copy(grade = grade)
                        Log.d(TAG, "syncGrades: Updated student ${student.studentId} with grade ${grade.gradeId}")
                    }
                }
                Log.d(TAG, "syncGrades: Students after: ${students.map { "${it.studentId}, Grade=${it.grade?.gradeId}" }}")
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Synced ${grades.size} grades from backend for userId: $userId")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to sync grades: ${e.message}", e)
                    e.printStackTrace()
                    onComplete(false)
                }
            }
        }
    }

    fun updateGrade(grade: Grade, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val updatedGrade = if (grade.gradeId == 0) {
                    apiService.postGrade(grade)
                } else {
                    apiService.putGrade(grade.gradeId, grade)
                }
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
                    Log.d(TAG, "Updated grade for studentId: ${updatedGrade.studentId}, GradeId: ${updatedGrade.gradeId}")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to update grade: ${e.message}", e)
                    e.printStackTrace()
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
                    e.printStackTrace()
                    onComplete(false)
                }
            }
        }
    }

    // Journal Operations
    fun syncJournals(userId: Int, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "syncJournals: Fetching journals for userId=$userId")
                val response = apiService.getJournalsByUser(userId)
                Log.d(TAG, "syncJournals: Received ${response.size} journals")
                journals.clear()
                journals.addAll(response)
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Synced ${journals.size} journals from backend for userId: $userId")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to sync journals: ${e.message}", e)
                    e.printStackTrace()
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
                val addedJournal = apiService.postJournal(journal)
                journals.add(addedJournal)
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Added journal: ${addedJournal.journalId}")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to add journal: ${e.message}", e)
                    e.printStackTrace()
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
                    e.printStackTrace()
                    onComplete(false)
                }
            }
        }
    }

    fun deleteJournal(journalId: Int, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiService.deleteJournal(journalId)
                journals.removeAll { it.journalId == journalId }
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Deleted journal: $journalId")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to delete journal: ${e.message}", e)
                    e.printStackTrace()
                    onComplete(false)
                }
            }
        }
    }

    // Calendar Operations
    fun syncEvents(userId: Int, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "syncEvents: Fetching events for userId=$userId")
                val response = apiService.getEventsByUser(userId)
                Log.d(TAG, "syncEvents: Received ${response.size} events")
                events.clear()
                events.addAll(response)
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Synced ${events.size} events from backend for userId: $userId")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to sync events: ${e.message}", e)
                    e.printStackTrace()
                    onComplete(false)
                }
            }
        }
    }

    fun getEventsByDate(date: LocalDate): List<Calendar> {
        val filteredEvents = events.filter { it.date.isEqual(date) }
        Log.d(TAG, "Fetched ${filteredEvents.size} events for date: $date")
        return filteredEvents
    }

    fun addEvent(event: Calendar, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val addedEvent = apiService.addEvent(event)
                events.add(addedEvent)
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Added event: ${addedEvent.calendarId}")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to add event: ${e.message}", e)
                    e.printStackTrace()
                    onComplete(false)
                }
            }
        }
    }

    fun updateEvent(event: Calendar, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val updatedEvent = apiService.updateEvent(event.calendarId, event)
                val index = events.indexOfFirst { it.calendarId == event.calendarId }
                if (index != -1) {
                    events[index] = updatedEvent
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "Updated event: ${updatedEvent.calendarId}")
                        onComplete(true)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "Event with ID ${event.calendarId} not found locally")
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to update event: ${e.message}", e)
                    e.printStackTrace()
                    onComplete(false)
                }
            }
        }
    }

    fun deleteEvent(eventId: Int, onComplete: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiService.deleteEvent(eventId)
                events.removeAll { it.calendarId == eventId }
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Deleted event: $eventId")
                    onComplete(true)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to delete event: ${e.message}", e)
                    e.printStackTrace()
                    onComplete(false)
                }
            }
        }
    }
}