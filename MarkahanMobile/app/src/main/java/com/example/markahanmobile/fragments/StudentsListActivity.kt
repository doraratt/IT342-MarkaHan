package com.example.markahanmobile.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
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
import com.example.markahanmobile.data.DataStore
import com.example.markahanmobile.data.Student
import com.example.markahanmobile.data.User
import com.example.markahanmobile.helper.GenderSpinnerAdapter
import com.example.markahanmobile.helper.SectionAdapter
import com.example.markahanmobile.helper.StudentAdapter
import com.google.android.material.navigation.NavigationView

class StudentsListActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var iconAddStudent: ImageView
    private lateinit var iconSearchStudent: ImageView
    private lateinit var iconToggleArchived: ImageView
    private lateinit var sectionRecyclerView: RecyclerView
    private lateinit var sectionAdapter: SectionAdapter
    private lateinit var studentAdapter: StudentAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var noStudentsText: TextView
    private lateinit var noSectionsText: TextView
    private val sectionList = mutableListOf<String>()
    private val studentList = mutableListOf<Student>()
    private var selectedSection: String = ""
    private var isShowingSearchResults = false
    private var isUpdating = false // Flag to prevent recursive calls
    private val TAG = "StudentsListActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_students_list)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        progressBar = findViewById(R.id.progressBar)
        noStudentsText = findViewById(R.id.noStudentsText)
        noSectionsText = findViewById(R.id.noSectionsText)

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
                    val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    sharedPreferences.edit().clear().apply()
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
                R.id.nav_att -> startActivity(Intent(this, AttendanceActivity::class.java))
                R.id.nav_grades -> startActivity(Intent(this, GradesActivity::class.java))
                R.id.nav_journal -> startActivity(Intent(this, JournalActivity::class.java))
                R.id.nav_list -> startActivity(Intent(this, StudentsListActivity::class.java))
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun setupRecyclerViews() {
        sectionRecyclerView = findViewById(R.id.recyclerViewSections)
        sectionRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        sectionAdapter = SectionAdapter(sectionList) { section ->
            if (!isUpdating) { // Prevent recursion from setSelectedPosition
                selectedSection = section
                isShowingSearchResults = false
                filterStudentsBySection(section)
            }
        }
        sectionRecyclerView.adapter = sectionAdapter

        val studentRecyclerView = findViewById<RecyclerView>(R.id.studentRecyclerView)
        studentRecyclerView.layoutManager = LinearLayoutManager(this)
        studentAdapter = StudentAdapter(studentList, ::editStudent, ::archiveStudent, null, showArchived = false)
        studentRecyclerView.adapter = studentAdapter
    }

    private fun setupButtons() {
        iconAddStudent = findViewById(R.id.iconAddStudent)
        iconSearchStudent = findViewById(R.id.iconSearchStudent)
        iconToggleArchived = findViewById(R.id.iconToggleArchived)

        iconAddStudent.setOnClickListener { showAddStudentDialog() }
        iconSearchStudent.setOnClickListener { showSearchStudentDialog() }
        iconToggleArchived.setOnClickListener {
            startActivity(Intent(this, ArchiveActivity::class.java))
        }
    }

    private fun filterStudentsBySection(section: String) {
        findViewById<TextView>(R.id.sectionsLabel).text = "Sections"
        studentList.clear()
        val students = DataStore.getStudents(section, includeArchived = false)
            .sortedWith(compareBy({ it.gender != "Male" }, { it.lastName }, { it.firstName }))
        studentList.addAll(students)
        studentAdapter.updateList(studentList)
        updateNoStudentsVisibility()
        Log.d(TAG, "Filtered students for section '$section': ${students.size} students, students=$students")

        val position = sectionList.indexOf(section)
        if (position != -1 && !isUpdating) { // Only set position if not in a recursive update
            isUpdating = true
            try {
                sectionAdapter.setSelectedPosition(position)
            } finally {
                isUpdating = false
            }
        }
    }

    private fun editStudent(student: Student) {
        showAddStudentDialog(student)
    }

    private fun archiveStudent(student: Student) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Archive")
            .setMessage("Are you sure you want to archive this student?")
            .setPositiveButton("Yes") { _, _ ->
                DataStore.archiveStudent(student.studentId, isArchived = true) { success ->
                    if (success) {
                        Log.d(TAG, "Student ${student.studentId} archived successfully")
                        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        val userId = sharedPreferences.getInt("userId", -1)
                        if (userId != -1) {
                            DataStore.syncStudents(userId, includeArchived = false) { syncSuccess ->
                                if (syncSuccess) {
                                    Log.d(TAG, "Student list refreshed after archiving")
                                    loadSections()
                                    if (sectionList.isNotEmpty()) {
                                        if (!sectionList.contains(selectedSection)) {
                                            selectedSection = sectionList[0]
                                        }
                                        filterStudentsBySection(selectedSection)
                                    } else {
                                        studentList.clear()
                                        studentAdapter.updateList(studentList)
                                        updateNoStudentsVisibility()
                                        updateNoSectionsVisibility()
                                    }
                                    Toast.makeText(this, "Student archived successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Log.e(TAG, "Failed to refresh student list after archiving")
                                    Toast.makeText(this, "Failed to refresh student list", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Log.e(TAG, "User not logged in during archiving")
                            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e(TAG, "Failed to archive student ${student.studentId}")
                        Toast.makeText(this, "Failed to archive student", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showAddStudentDialog(student: Student? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_student, null)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val firstNameEditText = dialogView.findViewById<EditText>(R.id.inputFirstName)
        val lastNameEditText = dialogView.findViewById<EditText>(R.id.inputLastName)
        val sectionEditText = dialogView.findViewById<EditText>(R.id.inputSection)
        val gradeLevelSpinner = dialogView.findViewById<Spinner>(R.id.inputGradeLevel)
        val genderSpinner = dialogView.findViewById<Spinner>(R.id.inputGender)
        val submitButton = dialogView.findViewById<Button>(R.id.btnSubmitStudent)

        val gradeLevels = listOf("Select Grade Level", "1", "2", "3", "4", "5", "6")
        val gradeLevelAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, gradeLevels)
        gradeLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gradeLevelSpinner.adapter = gradeLevelAdapter

        val genderAdapter = GenderSpinnerAdapter(
            this,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.gender_options).toList()
        )
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter

        if (student == null) {
            dialogTitle.text = "Add Student"
            submitButton.text = "Add Student"
            genderSpinner.setSelection(0)
            gradeLevelSpinner.setSelection(0)
        } else {
            dialogTitle.text = "Edit Student"
            submitButton.text = "Apply Changes"
            firstNameEditText.setText(student.firstName)
            lastNameEditText.setText(student.lastName)
            sectionEditText.setText(student.section)
            gradeLevelSpinner.setSelection(gradeLevels.indexOf(student.gradeLevel))
            genderSpinner.setSelection(
                if (student.gender == "Male") 1
                else if (student.gender == "Female") 2
                else 0
            )
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnCancelStudent).setOnClickListener {
            dialog.dismiss()
        }

        submitButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val section = sectionEditText.text.toString().trim()
            val gradeLevel = gradeLevelSpinner.selectedItem.toString()
            val gender = genderSpinner.selectedItem.toString()

            if (gender == "Select Gender") {
                Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (gradeLevel == "Select Grade Level") {
                Toast.makeText(this, "Please select a grade level", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (validateInput(firstName, lastName, section)) {
                val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                val userId = sharedPreferences.getInt("userId", -1)

                if (userId == -1) {
                    Log.e(TAG, "User not logged in during add/edit")
                    Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    return@setOnClickListener
                }

                val user = User(userId = userId)

                if (student == null) {
                    val newStudent = Student(
                        studentId = 0,
                        firstName = firstName,
                        lastName = lastName,
                        section = section,
                        gradeLevel = gradeLevel,
                        gender = gender,
                        user = user
                    )
                    DataStore.addStudent(newStudent) { success ->
                        if (success) {
                            Log.d(TAG, "Student added successfully: $firstName $lastName")
                            loadSections()
                            filterStudentsBySection(section)
                            dialog.dismiss()
                            Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e(TAG, "Failed to add student: $firstName $lastName")
                            Toast.makeText(this, "Failed to add student", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    showUpdateConfirmationDialog(
                        originalStudent = student,
                        newFirstName = firstName,
                        newLastName = lastName,
                        newSection = section,
                        newGradeLevel = gradeLevel,
                        newGender = gender,
                        parentDialog = dialog,
                        user = user
                    )
                }
            }
        }
        dialog.show()
    }

    private fun showUpdateConfirmationDialog(
        originalStudent: Student,
        newFirstName: String,
        newLastName: String,
        newSection: String,
        newGradeLevel: String,
        newGender: String,
        parentDialog: AlertDialog,
        user: User
    ) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Changes")
            .setMessage("Are you sure you want to update this student's information?")
            .setPositiveButton("Update") { _, _ ->
                val updatedStudent = Student(
                    studentId = originalStudent.studentId,
                    firstName = newFirstName,
                    lastName = newLastName,
                    section = newSection,
                    gradeLevel = newGradeLevel,
                    gender = newGender,
                    user = user,
                    attendanceStatus = originalStudent.attendanceStatus,
                    isArchived = originalStudent.isArchived,
                    grade = originalStudent.grade
                )
                DataStore.updateStudent(updatedStudent) { success ->
                    if (success) {
                        Log.d(TAG, "Student ${updatedStudent.studentId} updated successfully")
                        loadSections()
                        filterStudentsBySection(selectedSection)
                        parentDialog.dismiss()
                        Toast.makeText(this, "Student updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "Failed to update student ${updatedStudent.studentId}")
                        Toast.makeText(this, "Failed to update student", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun validateInput(
        firstName: String,
        lastName: String,
        section: String
    ): Boolean {
        return when {
            firstName.isEmpty() -> {
                Toast.makeText(this, "Please enter first name", Toast.LENGTH_SHORT).show()
                false
            }
            lastName.isEmpty() -> {
                Toast.makeText(this, "Please enter last name", Toast.LENGTH_SHORT).show()
                false
            }
            section.isEmpty() -> {
                Toast.makeText(this, "Please enter section", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun showSearchStudentDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_search_student, null)
        val searchInput = dialogView.findViewById<EditText>(R.id.searchInput)
        val searchButton = dialogView.findViewById<Button>(R.id.btnSearch)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Search Students")
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
            val filtered = DataStore.getStudents(includeArchived = false).filter {
                "${it.lastName}, ${it.firstName}".contains(query, ignoreCase = true) && !it.isArchived
            }
            studentList.clear()
            studentList.addAll(filtered.sortedWith(compareBy({ it.gender != "Male" }, { it.lastName }, { it.firstName })))
            studentAdapter.updateList(studentList)
            updateNoStudentsVisibility()

            findViewById<TextView>(R.id.sectionsLabel).text = "Search Results"
            sectionAdapter.setSelectedPosition(-1)
            isShowingSearchResults = true
            Log.d(TAG, "Search query '$query': ${filtered.size} students found")
        } else {
            isShowingSearchResults = false
            filterStudentsBySection(selectedSection)
        }
    }

    private fun loadSections() {
        sectionList.clear()
        val sections = DataStore.getSections(includeArchived = false)
        sectionList.addAll(sections)
        sectionAdapter.updateSections(sectionList)
        updateNoSectionsVisibility()
        Log.d(TAG, "Loaded sections: $sections")
    }

    private fun loadStudents() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", -1)

        if (userId == -1) {
            Log.e(TAG, "User not logged in")
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        progressBar.visibility = View.VISIBLE
        noStudentsText.visibility = View.GONE
        noSectionsText.visibility = View.GONE

        DataStore.syncStudents(userId, includeArchived = false) { success ->
            if (success) {
                DataStore.syncGrades(userId) { gradeSuccess ->
                    progressBar.visibility = View.GONE
                    if (gradeSuccess) {
                        Log.d(TAG, "syncStudents and syncGrades successful")
                        loadSections()
                        if (sectionList.isNotEmpty()) {
                            if (!sectionList.contains(selectedSection)) {
                                selectedSection = sectionList[0]
                            }
                            sectionAdapter.setSelectedPosition(sectionList.indexOf(selectedSection))
                            filterStudentsBySection(selectedSection)
                        } else {
                            studentList.clear()
                            studentAdapter.updateList(studentList)
                            updateNoStudentsVisibility()
                            updateNoSectionsVisibility()
                        }
                    } else {
                        Log.e(TAG, "Error syncing grades")
                        Toast.makeText(this, "Error syncing grades", Toast.LENGTH_SHORT).show()
                        loadSections()
                        if (sectionList.isNotEmpty()) {
                            filterStudentsBySection(selectedSection)
                        } else {
                            updateNoSectionsVisibility()
                        }
                    }
                }
            } else {
                progressBar.visibility = View.GONE
                Log.e(TAG, "Error syncing students")
                Toast.makeText(this, "Error loading students", Toast.LENGTH_SHORT).show()
                updateNoSectionsVisibility()
            }
        }
    }

    private fun updateNoStudentsVisibility() {
        noStudentsText.visibility = if (studentList.isEmpty()) View.VISIBLE else View.GONE
        Log.d(TAG, "Updated noStudentsText visibility: ${noStudentsText.visibility == View.VISIBLE}")
    }

    private fun updateNoSectionsVisibility() {
        noSectionsText.visibility = if (sectionList.isEmpty()) View.VISIBLE else View.GONE
        sectionRecyclerView.visibility = if (sectionList.isEmpty()) View.GONE else View.VISIBLE
        Log.d(TAG, "Updated noSectionsText visibility: ${noSectionsText.visibility == View.VISIBLE}")
    }

    override fun onResume() {
        super.onResume()
        if (!isShowingSearchResults) {
            loadStudents()
        }
    }
}