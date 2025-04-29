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
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.R
import com.example.markahanmobile.data.DataStore
import com.example.markahanmobile.data.Student
import com.example.markahanmobile.helper.SectionAdapter
import com.example.markahanmobile.helper.StudentGradesAdapter
import com.google.android.material.navigation.NavigationView

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
    private var selectedSection: String = SectionAdapter.ALL_SECTIONS
    private var userId: Int = 0
    private val TAG = "GradesActivity"
    private val VIEW_GRADES_REQUEST = 1001

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

        loadSections()
        loadStudents()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Reloading sections and students")
        loadSections()
        loadStudents()
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
            selectedSection = section
            filterStudentsBySection(section)
        }
        sectionRecyclerView.adapter = sectionAdapter

        val studentRecyclerView = findViewById<RecyclerView>(R.id.studentRecyclerView)
        studentRecyclerView.layoutManager = LinearLayoutManager(this)
        studentAdapter = StudentGradesAdapter(::viewGrades)
        studentRecyclerView.adapter = studentAdapter
    }

    private fun setupButtons() {
        iconSearchStudent = findViewById(R.id.iconSearchStudent)
        iconSearchStudent.setOnClickListener { showSearchStudentDialog() }
    }

    private fun filterStudentsBySection(section: String) {
        Log.d(TAG, "Filtering students for section: $section")
        findViewById<TextView>(R.id.sectionsLabel).text = "Sections"
        studentList.clear()
        val filtered = if (section == SectionAdapter.ALL_SECTIONS) {
            allStudents
        } else {
            allStudents.filter { it.section == section }
        }
        studentList.addAll(filtered)
        Log.d(TAG, "Filtered ${studentList.size} students: ${studentList.map { "${it.firstName} ${it.lastName}, Remarks: ${it.grade?.remarks}, Average: ${it.grade?.finalGrade}" }}")
        studentAdapter.updateList(studentList)
        updateNoStudentsVisibility()

        val position = sectionList.indexOf(section)
        if (position != -1) {
            sectionAdapter.setSelectedPosition(position)
        } else {
            Log.w(TAG, "Section $section not found in sectionList")
            sectionAdapter.setSelectedPosition(0)
        }
    }

    private fun viewGrades(student: Student) {
        try {
            Log.d(TAG, "Viewing grades for ${student.studentId}: ${student.grade}")
            val intent = Intent(this, StudentGradesActivity::class.java).apply {
                putExtra("student", student as android.os.Parcelable)
            }
            startActivityForResult(intent, VIEW_GRADES_REQUEST)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening StudentGradesActivity", e)
            Toast.makeText(this, "Error opening grade details", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VIEW_GRADES_REQUEST && resultCode == RESULT_OK) {
            data?.getParcelableExtra<Student>("updatedStudent")?.let { updatedStudent ->
                try {
                    Log.d(TAG, "Received updated student: ${updatedStudent.studentId}, Grade: ${updatedStudent.grade}")
                    val index = allStudents.indexOfFirst { it.studentId == updatedStudent.studentId }
                    if (index != -1) {
                        allStudents[index] = updatedStudent
                    }
                    filterStudentsBySection(selectedSection)
                    Toast.makeText(this, "Grades updated successfully", Toast.LENGTH_SHORT).show()
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "Error updating student", e)
                    Toast.makeText(this, e.message ?: "Failed to update grades", Toast.LENGTH_SHORT).show()
                }
            } ?: Log.e(TAG, "No updated student received in onActivityResult")
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
                "${it.lastName}, ${it.firstName}".contains(query, true)
            }
            studentList.clear()
            studentList.addAll(filtered)
            studentAdapter.updateList(studentList)
            Log.d(TAG, "Search results: ${filtered.size} students")
            findViewById<TextView>(R.id.sectionsLabel).text = "Search Results"
            sectionAdapter.setSelectedPosition(-1)
            updateNoStudentsVisibility()
        }
    }

    private fun loadSections() {
        sectionList.clear()
        sectionList.addAll(DataStore.getSections())
        sectionAdapter.updateSections(sectionList)
        Log.d(TAG, "Loaded sections: $sectionList")
        if (sectionList.isNotEmpty() && !sectionList.contains(selectedSection)) {
            selectedSection = SectionAdapter.ALL_SECTIONS
            sectionAdapter.setSelectedPosition(0)
        }
    }

    private fun loadStudents() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        userId = DataStore.getLoggedInUser()?.userId ?: sharedPreferences.getInt("userId", -1)
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        progressBar.visibility = View.VISIBLE
        noStudentsText.visibility = View.GONE

        DataStore.syncStudents(userId, includeArchived = false) { studentSuccess ->
            if (studentSuccess) {
                DataStore.syncGrades(userId) { gradeSuccess ->
                    progressBar.visibility = View.GONE
                    if (gradeSuccess) {
                        allStudents.clear()
                        allStudents.addAll(DataStore.getStudents(includeArchived = false))
                        Log.d(TAG, "Loaded ${allStudents.size} students: ${allStudents.map { "${it.lastName}, ${it.firstName}, Remarks: ${it.grade?.remarks}, Average: ${it.grade?.finalGrade}" }}")
                        loadSections()
                        filterStudentsBySection(selectedSection)
                    } else {
                        Toast.makeText(this, "Error syncing grades. Check logs for details.", Toast.LENGTH_SHORT).show()
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
        findViewById<RecyclerView>(R.id.studentRecyclerView).visibility = if (studentList.isEmpty()) View.GONE else View.VISIBLE
    }
}