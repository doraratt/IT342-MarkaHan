package com.example.markahanmobile.fragments

import android.content.Intent
import android.os.Bundle
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
    private lateinit var sectionAdapter: SectionAdapter
    private lateinit var studentAdapter: StudentAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var noStudentsText: TextView
    private val sectionList = mutableListOf<String>()
    private val studentList = mutableListOf<Student>()
    private var selectedSection: String = ""
    private var isShowingSearchResults = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_students_list)

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

        if (sectionList.isNotEmpty()) {
            selectedSection = SectionAdapter.ALL_SECTIONS
            filterStudentsBySection(selectedSection)
            sectionAdapter.setSelectedPosition(0)
        }
    }

    private fun setupNavigation() {
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
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun setupRecyclerViews() {
        val sectionRecyclerView = findViewById<RecyclerView>(R.id.recyclerViewSections)
        sectionRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        sectionAdapter = SectionAdapter(sectionList) { section ->
            selectedSection = section
            isShowingSearchResults = false
            filterStudentsBySection(section)
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
        studentList.addAll(DataStore.getStudents(section, includeArchived = false))
        studentAdapter.updateList(studentList)
        updateNoStudentsVisibility()

        val position = sectionList.indexOf(section)
        if (position != -1) {
            sectionAdapter.setSelectedPosition(position)
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
                DataStore.archiveStudent(student.studentId) { success ->
                    if (success) {
                        loadSections()
                        filterStudentsBySection(selectedSection)
                        Toast.makeText(this, "Student archived successfully", Toast.LENGTH_SHORT).show()
                    } else {
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

        // Setup Grade Level Spinner
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
                            loadSections()
                            filterStudentsBySection(section)
                            dialog.dismiss()
                            Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show()
                        } else {
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
                        loadSections()
                        filterStudentsBySection(selectedSection)
                        parentDialog.dismiss()
                        Toast.makeText(this, "Student updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
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
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Clear") { _, _ ->
                isShowingSearchResults = false
                filterStudentsBySection(selectedSection)
            }
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
                "${it.lastName}, ${it.firstName}".contains(query, ignoreCase = true)
            }
            studentList.clear()
            studentList.addAll(filtered.sortedWith(compareBy({ it.gender != "Male" }, { it.lastName }, { it.firstName })))
            studentAdapter.updateList(studentList)
            updateNoStudentsVisibility()

            findViewById<TextView>(R.id.sectionsLabel).text = "Search Results"
            sectionAdapter.setSelectedPosition(-1)
            isShowingSearchResults = true
        } else {
            isShowingSearchResults = false
            filterStudentsBySection(selectedSection)
        }
    }

    private fun loadSections() {
        sectionList.clear()
        sectionList.addAll(DataStore.getSections())
        sectionAdapter.updateSections(sectionList)
    }

    private fun loadStudents() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", -1)

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        progressBar.visibility = View.VISIBLE
        noStudentsText.visibility = View.GONE

        DataStore.syncStudents(userId, includeArchived = false) { success ->
            if (success) {
                // Sync grades to ensure student data is up-to-date
                DataStore.syncGrades(userId) { gradeSuccess ->
                    progressBar.visibility = View.GONE
                    if (gradeSuccess) {
                        loadSections()
                        filterStudentsBySection(selectedSection)
                    } else {
                        Toast.makeText(this, "Error syncing grades", Toast.LENGTH_SHORT).show()
                        loadSections()
                        filterStudentsBySection(selectedSection)
                    }
                }
            } else {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error loading students", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateNoStudentsVisibility() {
        noStudentsText.visibility = if (studentList.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        if (!isShowingSearchResults) {
            loadStudents()
        }
    }
}