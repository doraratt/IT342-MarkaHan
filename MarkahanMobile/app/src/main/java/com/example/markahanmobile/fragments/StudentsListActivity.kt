package com.example.markahanmobile.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.data.Student
import com.example.markahanmobile.helper.SectionAdapter
import com.example.markahanmobile.helper.StudentAdapter
import com.example.markahanmobile.utils.toast
import com.example.markahanmobile.R
import com.google.android.material.navigation.NavigationView

class StudentsListActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var iconAddStudent: ImageView
    private lateinit var iconSearchStudent: ImageView
    private lateinit var sectionAdapter: SectionAdapter
    private lateinit var studentAdapter: StudentAdapter
    private val sectionList = mutableListOf<String>()
    private val allStudents = mutableListOf<Student>()
    private val studentList = mutableListOf<Student>()
    private var selectedSection: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_students_list)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Navigation setup
        setupNavigation()

        // Initialize RecyclerViews
        val sectionRecyclerView = findViewById<RecyclerView>(R.id.recyclerViewSections)
        sectionRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        sectionAdapter = SectionAdapter(sectionList) { section ->
            selectedSection = section
            filterStudentsBySection(section)
        }
        sectionRecyclerView.adapter = sectionAdapter

        val studentRecyclerView = findViewById<RecyclerView>(R.id.studentRecyclerView)
        studentRecyclerView.layoutManager = LinearLayoutManager(this)
        studentAdapter = StudentAdapter(studentList, ::editStudent, ::archiveStudent)
        studentRecyclerView.adapter = studentAdapter

        // Initialize buttons
        iconAddStudent = findViewById(R.id.iconAddStudent)
        iconSearchStudent = findViewById(R.id.iconSearchStudent)

        iconAddStudent.setOnClickListener { showAddStudentDialog() }
        iconSearchStudent.setOnClickListener { showSearchStudentDialog() }

        // Load data
        loadSections()
        loadStudents()

        // Set initial section selection
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
                    startActivity(Intent(this, LoginActivity::class.java))
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

    private fun filterStudentsBySection(section: String) {
        findViewById<TextView>(R.id.sectionsLabel).text = "Sections"
        studentList.clear()
        if(section == SectionAdapter.ALL_SECTIONS){
            studentList.addAll(allStudents)
        } else {
            studentList.addAll(allStudents.filter {
                it.section == section
            })
        }
        studentAdapter.updateList(studentList)

        val position = sectionList.indexOf(section)
        if(position != -1)
            sectionAdapter.setSelectedPosition(position)
    }

    private fun editStudent(student: Student) {
        showAddStudentDialog(student)
    }

    private fun archiveStudent(student: Student) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Archive")
            .setMessage("Are you sure you want to archive this student?")
            .setPositiveButton("Yes") { _, _ ->
                allStudents.remove(student)
                filterStudentsBySection(selectedSection)

                // Remove section if no more students in it
                if (allStudents.none { it.section == student.section }) {
                    sectionList.remove(student.section)
                    sectionAdapter.updateSections(sectionList)
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
        val gradeLevelEditText = dialogView.findViewById<EditText>(R.id.inputGradeLevel)
        val submitButton = dialogView.findViewById<Button>(R.id.btnSubmitStudent)

        if (student == null) {
            dialogTitle.text = "Add Student"
            submitButton.text = "Add Student"
        } else {
            dialogTitle.text = "Edit Student"
            submitButton.text = "Apply Changes"
            firstNameEditText.setText(student.firstName)
            lastNameEditText.setText(student.lastName)
            sectionEditText.setText(student.section)
            gradeLevelEditText.setText(student.gradeLevel)
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
            val gradeLevel = gradeLevelEditText.text.toString().trim() // Get gradeLevel value

            // Pass all parameters including gradeLevel
            if (validateInput(firstName, lastName, section, gradeLevel)) {
                if (student == null) {
                    addStudent(Student(
                        firstName = firstName,
                        lastName = lastName,
                        section = section,
                        gradeLevel = gradeLevel
                    ))
                    dialog.dismiss()
                } else {
                    showUpdateConfirmationDialog(
                        originalStudent = student,
                        newFirstName = firstName,
                        newLastName = lastName,
                        newSection = section,
                        newGradeLevel = gradeLevel,
                        parentDialog = dialog
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
        parentDialog: AlertDialog
    ) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Changes")
            .setMessage("Are you sure you want to update this student's information?")
            .setPositiveButton("Update") { _, _ ->
                val updatedStudent = originalStudent.copy(
                    firstName = newFirstName,
                    lastName = newLastName,
                    section = newSection,
                    gradeLevel = newGradeLevel
                )
                updateStudent(updatedStudent)
                parentDialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun validateInput(
        firstName: String,
        lastName: String,
        section: String,
        gradeLevel: String): Boolean {
        return when {
            firstName.isEmpty() -> { toast("Please enter first name"); false }
            lastName.isEmpty() -> { toast("Please enter last name"); false }
            section.isEmpty() -> { toast("Please enter section"); false }
            gradeLevel.isEmpty() -> { toast("Please enter grade level"); false }
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
                "${it.firstName} ${it.lastName}".contains(query, true)
            }
            studentList.clear()
            studentList.addAll(filtered)
            studentAdapter.updateList(studentList)

            // Show search state in UI
            findViewById<TextView>(R.id.sectionsLabel).text = "Search Results"
            sectionAdapter.setSelectedPosition(-1)
        }
    }

    private fun loadSections() {
        sectionList.addAll(listOf(SectionAdapter.ALL_SECTIONS, "Faith", "Hope"))
        sectionAdapter.updateSections(sectionList)
    }

    private fun loadStudents() {
        allStudents.addAll(listOf(
            Student("1", "John", "Doe", "Faith", "4"),
            Student("2", "Jane", "Smith", "Hope", "4"),
            Student("3", "Emily", "Johnson", "Faith", "4")
        ))
        filterStudentsBySection(selectedSection)
    }

    private fun addStudent(student: Student) {
        val newStudent = student.copy(studentID = System.currentTimeMillis().toString())
        allStudents.add(newStudent)
        if (!sectionList.contains(newStudent.section)) {
            sectionList.add(newStudent.section)
            sectionAdapter.updateSections(sectionList)
        }
        filterStudentsBySection(newStudent.section)
    }

    private fun updateStudent(updatedStudent: Student) {
        val index = allStudents.indexOfFirst {
           it.studentID == updatedStudent.studentID
        }
        if (index != -1) {
            allStudents[index] = updatedStudent
            filterStudentsBySection(selectedSection)
            toast("Student updated successfully")
        }
    }
}