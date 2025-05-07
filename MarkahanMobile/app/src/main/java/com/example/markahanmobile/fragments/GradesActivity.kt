package com.example.markahanmobile.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.R
import com.example.markahanmobile.data.DataStore
import com.example.markahanmobile.data.Student
import com.example.markahanmobile.helper.SectionAdapter
import com.example.markahanmobile.helper.StudentGradesAdapter
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class GradesActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var iconSearchStudent: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var noStudentsText: TextView
    private lateinit var sectionAdapter: SectionAdapter
    private lateinit var studentAdapter: StudentGradesAdapter
    private val sectionList = mutableListOf<String>()
    private val allStudents = mutableListOf<Student>()
    private val studentList = mutableListOf<Student>()
    private var userId: Int = 0
    private var selectedSection: String? = null
    private var isUpdating = false // Flag to prevent recursive section updates
    private var isFiltering = false // Flag to prevent concurrent filter calls
    private var isLoading = false // Flag to prevent multiple loadStudents calls
    private val TAG = "GradesActivity"
    private val VIEW_GRADES_REQUEST = 1001
    private val SYNC_TIMEOUT = 10000L // 10 seconds timeout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grades)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        progressBar = findViewById(R.id.progressBar)
        noStudentsText = findViewById(R.id.noStudentsText)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        setupNavigation()
        setupRecyclerViews()
        setupButtons()

        loadStudents()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Checking if reload is needed, isLoading=$isLoading")
        if (!isLoading) {
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
                R.id.nav_att -> startActivity(Intent(this, AttendanceActivity::class.java))
                R.id.nav_journal -> startActivity(Intent(this, JournalActivity::class.java))
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun setupRecyclerViews() {
        val sectionRecyclerView = findViewById<RecyclerView>(R.id.recyclerViewSections)
        sectionRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        sectionAdapter = SectionAdapter(sectionList) { section ->
            if (!isUpdating) {
                selectedSection = section
                filterStudentsBySection(section)
            }
        }
        sectionRecyclerView.adapter = sectionAdapter

        val studentRecyclerView = findViewById<RecyclerView>(R.id.studentRecyclerView)
        studentRecyclerView.layoutManager = LinearLayoutManager(this)
        studentAdapter = StudentGradesAdapter(::viewGrades)
        studentRecyclerView.adapter = studentAdapter
        studentRecyclerView.visibility = View.VISIBLE
        studentRecyclerView.requestLayout()
        studentRecyclerView.post {
            Log.d(TAG, "setupRecyclerViews: studentRecyclerView dimensions: width=${studentRecyclerView.width}, height=${studentRecyclerView.height}, visibility=${studentRecyclerView.visibility}, itemCount=${studentAdapter.itemCount}")
        }
    }

    private fun setupButtons() {
        iconSearchStudent = findViewById(R.id.iconSearchStudent)
        iconSearchStudent.setOnClickListener { showSearchStudentDialog() }
    }

    private fun filterStudentsBySection(section: String) {
        if (isFiltering) {
            Log.d(TAG, "filterStudentsBySection: Already filtering, skipping call for section: '$section'")
            return
        }
        isFiltering = true
        Log.d(TAG, "filterStudentsBySection: Starting filter for section: '$section', allStudents.size=${allStudents.size}, sectionList=$sectionList")

        try {
            val uniqueSections = allStudents.map { it.section }.distinct()
            Log.d(TAG, "filterStudentsBySection: Unique sections in allStudents: $uniqueSections")
            findViewById<TextView>(R.id.sectionsLabel).text = "Sections"
            studentList.clear()

            val filtered = allStudents.filter { it.section.equals(section, ignoreCase = true) && !it.isArchived }
            studentList.addAll(filtered)
            Log.d(TAG, "filterStudentsBySection: Filtered ${studentList.size} students for section '$section': ${studentList.map { "${it.studentId}, ${it.firstName} ${it.lastName}, Section=${it.section}, isArchived=${it.isArchived}" }}")

            studentAdapter = StudentGradesAdapter(::viewGrades) // Reset adapter
            val studentRecyclerView = findViewById<RecyclerView>(R.id.studentRecyclerView)
            studentRecyclerView.adapter = studentAdapter
            studentAdapter.updateList(studentList)
            studentRecyclerView.requestLayout()
            studentRecyclerView.invalidate()
            Log.d(TAG, "filterStudentsBySection: RecyclerView visibility=${studentRecyclerView.visibility}, itemCount=${studentAdapter.itemCount}, studentList.size=${studentList.size}")
            updateNoStudentsVisibility()

            val position = sectionList.indexOf(section)
            if (position != -1 && sectionList.isNotEmpty() && !isUpdating) {
                isUpdating = true
                try {
                    sectionAdapter.setSelectedPosition(position)
                } finally {
                    isUpdating = false
                }
            } else {
                Log.w(TAG, "filterStudentsBySection: Section '$section' not found in sectionList or sectionList is empty")
            }
        } finally {
            isFiltering = false
            Log.d(TAG, "filterStudentsBySection: Completed filter for section: '$section'")
        }
    }

    private fun viewGrades(student: Student) {
        try {
            Log.d(TAG, "viewGrades: Opening grades for student ${student.studentId}: ${student.firstName} ${student.lastName}, Grade=${student.grade}")
            // Ensure grades are synced before viewing
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    withContext(Dispatchers.IO) {
                        DataStore.syncGrades(userId) { success ->
                            if (success) {
                                Log.d(TAG, "viewGrades: Grades synced for userId=$userId")
                            } else {
                                Log.e(TAG, "viewGrades: Failed to sync grades")
                            }
                        }
                    }
                    // Fetch the latest grade for the student
                    val grade = DataStore.getGradeByStudent(student.studentId)
                    val updatedStudent = student.copy(grade = grade)
                    Log.d(TAG, "viewGrades: Fetched grade for studentId=${student.studentId}, Grade=$grade")
                    val intent = Intent(this@GradesActivity, StudentGradesActivity::class.java).apply {
                        putExtra("student", updatedStudent as android.os.Parcelable)
                    }
                    startActivityForResult(intent, VIEW_GRADES_REQUEST)
                } catch (e: Exception) {
                    Log.e(TAG, "viewGrades: Error syncing grades before opening", e)
                    Toast.makeText(this@GradesActivity, "Error loading grades", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@GradesActivity, StudentGradesActivity::class.java).apply {
                        putExtra("student", student as android.os.Parcelable)
                    }
                    startActivityForResult(intent, VIEW_GRADES_REQUEST)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "viewGrades: Error opening StudentGradesActivity", e)
            Toast.makeText(this, "Error opening grade details", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VIEW_GRADES_REQUEST && resultCode == RESULT_OK) {
            data?.getParcelableExtra<Student>("updatedStudent")?.let { updatedStudent ->
                try {
                    Log.d(TAG, "onActivityResult: Received updated student: ${updatedStudent.studentId}, Grade=${updatedStudent.grade}")
                    val index = allStudents.indexOfFirst { it.studentId == updatedStudent.studentId }
                    if (index != -1) {
                        allStudents[index] = updatedStudent
                    } else {
                        Log.w(TAG, "onActivityResult: Updated student ${updatedStudent.studentId} not found in allStudents")
                    }
                    if (sectionList.isNotEmpty()) {
                        filterStudentsBySection(selectedSection ?: sectionList.firstOrNull() ?: "")
                    } else {
                        studentList.clear()
                        studentAdapter.updateList(studentList)
                        updateNoStudentsVisibility()
                    }
                    Toast.makeText(this, "Grades updated successfully", Toast.LENGTH_SHORT).show()
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "onActivityResult: Error updating student", e)
                    Toast.makeText(this, e.message ?: "Failed to update grades", Toast.LENGTH_SHORT).show()
                }
            } ?: Log.e(TAG, "onActivityResult: No updated student received")
        }
    }

    private fun showSearchStudentDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_search_student, null)
        val searchInput = dialogView.findViewById<EditText>(R.id.searchInput)
        val searchButton = dialogView.findViewById<Button>(R.id.btnSearch)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Search Students")
            .setNegativeButton("Cancel", null)
            .create()

        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchInput.text.toString())
                dialog.dismiss()
                true
            } else {
                false
            }
        }

        searchButton.setOnClickListener {
            performSearch(searchInput.text.toString())
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun performSearch(query: String) {
        if (query.isNotBlank()) {
            val filtered = allStudents.filter {
                "${it.lastName}, ${it.firstName}".contains(query, ignoreCase = true) && !it.isArchived
            }
            studentList.clear()
            studentList.addAll(filtered)
            studentAdapter.updateList(studentList)
            Log.d(TAG, "performSearch: Found ${filtered.size} students for query '$query': ${filtered.map { "${it.firstName} ${it.lastName}" }}")
            findViewById<TextView>(R.id.sectionsLabel).text = "Search Results"
            sectionAdapter.setSelectedPosition(-1)
            updateNoStudentsVisibility()
        } else {
            Log.d(TAG, "performSearch: Empty query, restoring section filter")
            if (sectionList.isNotEmpty()) {
                filterStudentsBySection(selectedSection ?: sectionList.first())
            }
        }
    }

    private fun loadSections() {
        sectionList.clear()
        val sections = DataStore.getSections(includeArchived = false)
        sectionList.addAll(sections)
        sectionAdapter.updateSections(sectionList)
        sectionAdapter.notifyDataSetChanged() // Ensure adapter updates immediately
        Log.d(TAG, "loadSections: Loaded ${sectionList.size} sections: $sectionList from DataStore: $sections, adapter itemCount=${sectionAdapter.itemCount}")
        if (sectionList.isEmpty() && sectionAdapter.itemCount == 0) {
            selectedSection = null
            Log.w(TAG, "loadSections: No sections available")
            Toast.makeText(this, "No sections available. Please add students first.", Toast.LENGTH_LONG).show()
        } else {
            if (selectedSection !in sectionList) {
                selectedSection = sectionList.firstOrNull()
                if (selectedSection != null) {
                    sectionAdapter.setSelectedPosition(sectionList.indexOf(selectedSection))
                }
            }
        }
    }

    private fun loadStudents() {
        if (isLoading) {
            Log.d(TAG, "loadStudents: Already loading, skipping")
            return
        }
        isLoading = true

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        userId = DataStore.getLoggedInUser()?.userId ?: sharedPreferences.getInt("userId", -1)
        if (userId == -1) {
            Log.e(TAG, "loadStudents: Invalid userId: $userId, redirecting to login")
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            isLoading = false
            return
        }

        Log.d(TAG, "loadStudents: Loading students for userId: $userId")
        progressBar.visibility = View.VISIBLE
        noStudentsText.visibility = View.GONE

        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d(TAG, "loadStudents: Starting sync for userId: $userId")
                var studentSuccess = false
                var gradeSuccess = false
                withContext(Dispatchers.IO) {
                    // Sync students
                    val studentDeferred = CompletableDeferred<Boolean>()
                    DataStore.syncStudents(userId, includeArchived = false) { success ->
                        Log.d(TAG, "syncStudents: Callback received, success=$success")
                        val students = DataStore.getStudents(includeArchived = false)
                        Log.d(TAG, "syncStudents: Post-sync, retrieved ${students.size} students: ${students.map { "${it.studentId}, ${it.firstName} ${it.lastName}, Section=${it.section}, isArchived=${it.isArchived}" }}")
                        studentDeferred.complete(success)
                    }
                    studentSuccess = withTimeoutOrNull(SYNC_TIMEOUT) { studentDeferred.await() } ?: false

                    // Sync grades
                    val gradeDeferred = CompletableDeferred<Boolean>()
                    DataStore.syncGrades(userId) { success ->
                        Log.d(TAG, "syncGrades: Callback received, success=$success")
                        gradeDeferred.complete(success)
                    }
                    gradeSuccess = withTimeoutOrNull(SYNC_TIMEOUT) { gradeDeferred.await() } ?: false
                }
                Log.d(TAG, "loadStudents: Sync completed with studentSuccess=$studentSuccess, gradeSuccess=$gradeSuccess")

                allStudents.clear()
                val students = DataStore.getStudents(includeArchived = false)
                Log.d(TAG, "loadStudents: Retrieved ${students.size} students from DataStore: ${students.map { "${it.studentId}, ${it.firstName} ${it.lastName}, Section=${it.section}, isArchived=${it.isArchived}, Grade=${it.grade?.gradeId}" }}")
                allStudents.addAll(students)
                Log.d(TAG, "loadStudents: Loaded ${allStudents.size} students into allStudents")

                if (allStudents.isEmpty()) {
                    Log.w(TAG, "loadStudents: No students retrieved. Check database for userId=$userId, archived=false")
                    Toast.makeText(this@GradesActivity, "No students found for this user. Please add students.", Toast.LENGTH_LONG).show()
                } else {
                    val uniqueSections = allStudents.map { it.section }.distinct()
                    Log.d(TAG, "loadStudents: Unique sections in allStudents: $uniqueSections")
                    Log.d(TAG, "loadStudents: Comparing with sectionList: $sectionList")
                    allStudents.forEach { student ->
                        Log.d(TAG, "loadStudents: Student ${student.studentId}, Section='${student.section}', Length=${student.section.length}, Chars=${student.section.toCharArray().joinToString(",") { it.code.toString() }}, Grade=${student.grade?.gradeId}")
                    }
                }

                loadSections() // Single call to load sections
                if (sectionList.isEmpty()) {
                    Log.w(TAG, "loadStudents: No sections available after reload, clearing student list")
                    studentList.clear()
                    studentAdapter.updateList(studentList)
                    updateNoStudentsVisibility()
                } else {
                    val sectionToFilter = selectedSection ?: sectionList.firstOrNull() ?: ""
                    Log.d(TAG, "loadStudents: Filtering students for section: '$sectionToFilter'")
                    filterStudentsBySection(sectionToFilter)
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadStudents: Exception during loadStudents: ${e.message}", e)
                Toast.makeText(this@GradesActivity, "Error loading data: ${e.message}", Toast.LENGTH_LONG).show()
                allStudents.clear()
                val students = DataStore.getStudents(includeArchived = false)
                Log.d(TAG, "loadStudents: Exception fallback - Retrieved ${students.size} students from DataStore: ${students.map { "${it.studentId}, ${it.firstName} ${it.lastName}, Section=${it.section}, isArchived=${it.isArchived}" }}")
                allStudents.addAll(students)
                loadSections() // Single call to load sections in catch
                if (sectionList.isEmpty()) {
                    studentList.clear()
                    studentAdapter.updateList(studentList)
                    updateNoStudentsVisibility()
                } else {
                    filterStudentsBySection(selectedSection ?: sectionList.firstOrNull() ?: "")
                }
            } finally {
                progressBar.visibility = View.GONE
                isLoading = false
                Log.d(TAG, "loadStudents: Completed, progressBar hidden")
            }
        }
    }

    private fun updateNoStudentsVisibility() {
        val studentRecyclerView = findViewById<RecyclerView>(R.id.studentRecyclerView)
        if (studentList.isEmpty()) {
            noStudentsText.text = if (sectionList.isEmpty()) {
                "No students available. Please add students first."
            } else {
                "No students found in the selected section."
            }
            noStudentsText.visibility = View.VISIBLE
            studentRecyclerView.visibility = View.GONE
            val params = studentRecyclerView.layoutParams as ConstraintLayout.LayoutParams
            params.topToBottom = R.id.noStudentsText
            studentRecyclerView.layoutParams = params
        } else {
            noStudentsText.visibility = View.GONE
            studentRecyclerView.visibility = View.VISIBLE
            val params = studentRecyclerView.layoutParams as ConstraintLayout.LayoutParams
            params.topToBottom = R.id.columnHeaders
            studentRecyclerView.layoutParams = params
        }
        studentRecyclerView.post {
            Log.d(TAG, "updateNoStudentsVisibility: RecyclerView width=${studentRecyclerView.width}, height=${studentRecyclerView.height}, visibility=${studentRecyclerView.visibility}, itemCount=${studentAdapter.itemCount}, studentList.size=${studentList.size}")
        }
    }
}