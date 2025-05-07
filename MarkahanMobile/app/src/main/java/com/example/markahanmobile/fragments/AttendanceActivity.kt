package com.example.markahanmobile.fragments

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.R
import com.example.markahanmobile.data.AttendanceRecord
import com.example.markahanmobile.data.DataStore
import com.example.markahanmobile.data.Student
import com.example.markahanmobile.data.User
import com.example.markahanmobile.helper.AttendanceAdapter
import com.example.markahanmobile.helper.SectionAdapter
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class AttendanceActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var sectionRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var noStudentsText: TextView
    private lateinit var adapter: AttendanceAdapter
    private lateinit var sectionAdapter: SectionAdapter
    private val allStudents = mutableListOf<Student>()
    private val displayedStudents = mutableListOf<Student>()
    private val sectionList = mutableListOf<String>()
    private var selectedDate: LocalDate = LocalDate.now()
    private var selectedSection: String? = null
    private var userId: Int = 0
    private var isLoading = false
    private val TAG = "AttendanceActivity"
    private val SYNC_TIMEOUT = 10000L // 10 seconds timeout for sync
    private val MAX_RETRIES = 2 // Max retries for loading students
    private val knownInvalidStudentIds = mutableSetOf<Int>() // Track invalid student IDs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        progressBar = findViewById(R.id.progressBar)
        noStudentsText = findViewById(R.id.noStudentsText)

        // Initialize DataStore for API calls and data storage
        DataStore.init(applicationContext)

        // Verify user is logged in
        val user = DataStore.getLoggedInUser()
        if (user == null) {
            Log.w(TAG, "onCreate: No logged-in user, redirecting to LoginActivity")
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Setup navigation drawer toggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        setupNavigation()
        setupSections()
        setupRecyclerView()
        setupDateDisplay()
        setupSubmitButton()
        setupSheetsIcon()

        loadSections()
        loadStudents()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Reloading data")
        if (!isLoading) {
            loadSections()
            loadStudents()
        }
    }

    private fun setupNavigation() {
        // Update header with user's first name
        val headerView = navView.getHeaderView(0)
        val headerFirstName = headerView.findViewById<TextView>(R.id.header_firstname)
        val user = DataStore.getLoggedInUser()
        if (user != null && user.firstName!!.isNotEmpty()) {
            headerFirstName.text = "Welcome, Teacher ${user.firstName}!"
        } else {
            headerFirstName.text = "Welcome, Teacher!"
            Log.w(TAG, "No user or first name found")
        }

        // Logout setup
        val logoutView = navView.findViewById<TextView>(R.id.nav_logout)
        logoutView?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setIcon(R.drawable.warningsign)
                .setPositiveButton("Logout") { _, _ ->
                    DataStore.logout()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dash -> startActivity(Intent(this, DashboardActivity::class.java))
                R.id.nav_cal -> startActivity(Intent(this, CalendarActivity::class.java))
                R.id.nav_list -> startActivity(Intent(this, StudentsListActivity::class.java))
                R.id.nav_grades -> startActivity(Intent(this, GradesActivity::class.java))
                R.id.nav_journal -> startActivity(Intent(this, JournalActivity::class.java))
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun setupSections() {
        sectionRecyclerView = findViewById(R.id.recyclerViewSections)
        sectionRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        sectionAdapter = SectionAdapter(sectionList) { section ->
            selectedSection = section
            filterStudentsBySection(section)
        }
        sectionRecyclerView.adapter = sectionAdapter
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.attendanceRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AttendanceAdapter { studentPosition, status ->
            displayedStudents[studentPosition] = displayedStudents[studentPosition].copy(attendanceStatus = status)
            Log.d(TAG, "Updated attendance for ${displayedStudents[studentPosition].lastName}: $status")
        }
        recyclerView.adapter = adapter
    }

    private fun setupDateDisplay() {
        val dateTextView = findViewById<TextView>(R.id.txtDate)
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yy", Locale.getDefault())
        dateTextView.text = selectedDate.format(formatter)

        dateTextView.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedDate = LocalDate.of(year, month + 1, day)
                    dateTextView.text = selectedDate.format(formatter)
                    loadStudents()
                },
                selectedDate.year,
                selectedDate.monthValue - 1,
                selectedDate.dayOfMonth
            ).show()
        }
    }

    private fun setupSubmitButton() {
        findViewById<Button>(R.id.btnSubmitAttendance).setOnClickListener {
            saveAttendanceRecords()
        }
    }

    private fun setupSheetsIcon() {
        findViewById<ImageView>(R.id.iconSheets).setOnClickListener {
            val intent = Intent(this, AttendanceSheetActivity::class.java)
            intent.putExtra("SELECTED_SECTION", selectedSection)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
            intent.putExtra("SELECTED_DATE", selectedDate.format(formatter))
            startActivity(intent)
        }
    }

    private fun loadStudents(retryCount: Int = 0) {
        if (isLoading) {
            Log.d(TAG, "loadStudents: Already loading")
            return
        }
        isLoading = true

        // Get userId
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        userId = DataStore.getLoggedInUser()?.userId ?: sharedPreferences.getInt("userId", -1)
        if (userId == -1) {
            Log.e(TAG, "loadStudents: Invalid userId, redirecting to login")
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            isLoading = false
            return
        }

        Log.d(TAG, "loadStudents: Loading for userId: $userId, retry: $retryCount")
        progressBar.visibility = View.VISIBLE
        noStudentsText.visibility = View.GONE

        CoroutineScope(Dispatchers.Main).launch {
            var studentSuccess = false
            var attendanceSuccess = false
            withContext(Dispatchers.IO) {
                // Fetch students from backend with timeout
                val studentDeferred = CompletableDeferred<Boolean>()
                DataStore.syncStudents(userId, includeArchived = false) { success ->
                    Log.d(TAG, "syncStudents: Success=$success")
                    studentDeferred.complete(success)
                }
                studentSuccess = withTimeoutOrNull(SYNC_TIMEOUT) { studentDeferred.await() } ?: false

                if (studentSuccess) {
                    // Fetch attendance records
                    val attendanceDeferred = CompletableDeferred<Boolean>()
                    DataStore.syncAttendanceRecords(userId) { success ->
                        Log.d(TAG, "syncAttendanceRecords: Success=$success")
                        attendanceDeferred.complete(success)
                    }
                    attendanceSuccess = withTimeoutOrNull(SYNC_TIMEOUT) { attendanceDeferred.await() } ?: false
                }
            }

            progressBar.visibility = View.GONE
            allStudents.clear()
            val students = DataStore.getStudents(includeArchived = false)
            Log.d(TAG, "loadStudents: Got ${students.size} students: ${students.map { "${it.studentId}, ${it.firstName} ${it.lastName}" }}")
            allStudents.addAll(students)

            if (!studentSuccess && retryCount < MAX_RETRIES) {
                Log.w(TAG, "loadStudents: Sync failed, retrying (${retryCount + 1}/$MAX_RETRIES)")
                isLoading = false
                loadStudents(retryCount + 1)
                return@launch
            }

            if (allStudents.isEmpty()) {
                Log.w(TAG, "loadStudents: No students found")
                noStudentsText.text = "No students found. Please add students."
                noStudentsText.visibility = View.VISIBLE
                Toast.makeText(this@AttendanceActivity, "No students found", Toast.LENGTH_LONG).show()
                updateNoStudentsVisibility()
            } else {
                if (sectionList.isEmpty()) {
                    loadSections()
                }
                val sectionToFilter = selectedSection ?: sectionList.firstOrNull()
                if (sectionToFilter != null) {
                    val records = DataStore.getAttendanceRecords(sectionToFilter, selectedDate, selectedDate)
                    filterStudentsBySection(sectionToFilter, records)
                } else {
                    Log.w(TAG, "loadStudents: No section, showing all students")
                    displayedStudents.clear()
                    displayedStudents.addAll(allStudents.map { student ->
                        val record = DataStore.getAttendanceRecords("", selectedDate, selectedDate)
                            .firstOrNull { it.studentId == student.studentId && it.date == selectedDate }
                        student.copy(attendanceStatus = record?.status ?: "")
                    })
                    adapter.updateList(displayedStudents)
                    noStudentsText.text = "No sections available."
                    updateNoStudentsVisibility()
                }
            }
            isLoading = false
        }
    }

    private fun loadSections() {
        sectionList.clear()
        sectionList.addAll(DataStore.getSections(includeArchived = false))
        sectionAdapter.updateSections(sectionList)
        sectionAdapter.notifyDataSetChanged()
        if (sectionList.isNotEmpty()) {
            if (selectedSection == null || selectedSection !in sectionList) {
                selectedSection = sectionList.first()
                sectionAdapter.setSelectedPosition(0)
            } else {
                val position = sectionList.indexOf(selectedSection)
                sectionAdapter.setSelectedPosition(position)
            }
        } else {
            selectedSection = null
            sectionAdapter.setSelectedPosition(-1)
        }
        Log.d(TAG, "loadSections: Loaded ${sectionList.size} sections: $sectionList")
    }

    private fun filterStudentsBySection(section: String, records: List<AttendanceRecord> = emptyList()) {
        Log.d(TAG, "Filtering students for section: $section")
        displayedStudents.clear()
        val students = allStudents.filter { it.section == section && !it.isArchived }
        displayedStudents.addAll(students.map { student ->
            val record = records.firstOrNull { it.studentId == student.studentId && it.date == selectedDate }
            student.copy(attendanceStatus = record?.status ?: "")
        })
        adapter.updateList(displayedStudents)
        updateNoStudentsVisibility()
        Log.d(TAG, "Filtered ${displayedStudents.size} students")
    }

    private fun saveAttendanceRecords() {
        // Validate student records
        val validStudentIds = allStudents.map { it.studentId }.toSet()
        val recordsToSave = displayedStudents.filter { student ->
            student.attendanceStatus in listOf("Present", "Absent", "Late") &&
                    student.studentId > 0 &&
                    student.studentId in validStudentIds &&
                    !knownInvalidStudentIds.contains(student.studentId) &&
                    student.firstName?.isNotEmpty() == true &&
                    student.lastName?.isNotEmpty() == true &&
                    student.section?.isNotEmpty() == true
        }
        if (recordsToSave.isEmpty()) {
            Toast.makeText(this, "No valid attendance records to save.", Toast.LENGTH_LONG).show()
            return
        }

        // Show confirmation dialog
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yy", Locale.getDefault())
        val dateStr = selectedDate.format(formatter)
        val sectionToShow = selectedSection ?: "Unknown Section"
        val summary = recordsToSave.joinToString("\n") { student ->
            "${student.lastName}, ${student.firstName}: ${student.attendanceStatus}"
        }
        Log.d(TAG, "Records to save: ${recordsToSave.map { "studentId=${it.studentId}, status=${it.attendanceStatus}" }}")

        AlertDialog.Builder(this)
            .setTitle("Confirm Attendance")
            .setMessage("Save attendance for $sectionToShow on $dateStr?\n\n$summary")
            .setPositiveButton("Save") { _, _ ->
                val user = DataStore.getLoggedInUser()
                val userId = user?.userId ?: 0
                if (userId <= 0) {
                    Toast.makeText(this, "No valid user logged in", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Sync students to ensure valid IDs
                CoroutineScope(Dispatchers.Main).launch {
                    var syncSuccess = false
                    withContext(Dispatchers.IO) {
                        val syncDeferred = CompletableDeferred<Boolean>()
                        DataStore.syncStudents(userId, includeArchived = false) { success ->
                            Log.d(TAG, "saveAttendanceRecords: Sync success=$success")
                            syncDeferred.complete(success)
                        }
                        syncSuccess = withTimeoutOrNull(SYNC_TIMEOUT) { syncDeferred.await() } ?: false
                    }

                    if (!syncSuccess) {
                        Snackbar.make(recyclerView, "Failed to sync students. Retry?", Snackbar.LENGTH_LONG)
                            .setAction("Retry") { saveAttendanceRecords() }
                            .show()
                        isLoading = false
                        loadStudents()
                        return@launch
                    }

                    // Refresh student list
                    allStudents.clear()
                    val students = DataStore.getStudents(includeArchived = false)
                    allStudents.addAll(students)
                    Log.d(TAG, "saveAttendanceRecords: Synced ${allStudents.size} students")
                    val updatedValidStudentIds = allStudents.map { it.studentId }.toSet()

                    // Re-validate records
                    val validatedRecordsToSave = recordsToSave.filter { student ->
                        student.studentId in updatedValidStudentIds && !knownInvalidStudentIds.contains(student.studentId)
                    }
                    if (validatedRecordsToSave.isEmpty()) {
                        Snackbar.make(recyclerView, "No valid students after sync. Retry?", Snackbar.LENGTH_LONG)
                            .setAction("Retry") { saveAttendanceRecords() }
                            .show()
                        loadStudents()
                        return@launch
                    }

                    // Create AttendanceRecord objects with minimal student and user data
                    val newRecords = validatedRecordsToSave.map { student ->
                        AttendanceRecord(
                            attendanceId = 0,
                            studentId = student.studentId,
                            userId = userId,
                            date = selectedDate,
                            status = student.attendanceStatus,
                            section = student.section,
                            user = User(userId = userId), // Minimal user object with only userId
                            student = Student(
                                studentId = student.studentId,
                                userId = userId,
                                firstName = student.firstName,
                                lastName = student.lastName,
                                gender = student.gender,
                                section = student.section,
                                gradeLevel = student.gradeLevel
                            ) // Minimal student object with required fields
                        )
                    }
                    Log.d(TAG, "Saving ${newRecords.size} records: ${newRecords.map { "studentId=${it.studentId}, status=${it.status}" }}")

                    // Send records to backend
                    DataStore.addAttendanceRecords(newRecords) { success ->
                        if (success) {
                            Toast.makeText(this@AttendanceActivity, "Attendance saved", Toast.LENGTH_SHORT).show()
                            knownInvalidStudentIds.clear()
                            loadStudents()
                        } else {
                            val invalidIds = newRecords.map { it.studentId }.toSet()
                            knownInvalidStudentIds.addAll(invalidIds)
                            Log.w(TAG, "saveAttendanceRecords: Invalid IDs: $invalidIds")
                            Snackbar.make(recyclerView, "Invalid student IDs detected. Retry?", Snackbar.LENGTH_LONG)
                                .setAction("Retry") { saveAttendanceRecords() }
                                .show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateNoStudentsVisibility() {
        noStudentsText.visibility = if (displayedStudents.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (displayedStudents.isEmpty()) View.GONE else View.VISIBLE
        Log.d(TAG, "updateNoStudentsVisibility: Students=${displayedStudents.size}")
    }
}