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
import com.example.markahanmobile.helper.StudentGradesAdapter
import com.example.markahanmobile.utils.toast
import com.example.markahanmobile.R
import com.google.android.material.navigation.NavigationView

class GradesActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var iconSearchStudent: ImageView
    private lateinit var sectionAdapter: SectionAdapter
    private lateinit var studentAdapter: StudentGradesAdapter
    private val sectionList = mutableListOf<String>()
    private val allStudents = mutableListOf<Student>()
    private val studentList = mutableListOf<Student>()
    private var selectedSection: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grades)

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
        studentAdapter = StudentGradesAdapter(studentList, ::viewGrades, ::editGrades)
        studentRecyclerView.adapter = studentAdapter

        // Initialize buttons
        iconSearchStudent = findViewById(R.id.iconSearchStudent)
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
                R.id.nav_list -> startActivity(Intent(this, StudentsListActivity::class.java))
                R.id.nav_att -> startActivity(Intent(this, AttendanceActivity::class.java))
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

    private fun viewGrades(student: Student) {
        try {
            val intent = Intent(this, StudentGradesActivity::class.java).apply {
                putExtra("student", student)
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            toast("Error opening grade details")
        }
    }

    private fun editGrades(student: Student) {
        // Launch EditGradesActivity instead of showing dialog
        val intent = Intent(this, EditGradesActivity::class.java).apply {
            putExtra("student", student)
        }
        startActivityForResult(intent, EDIT_GRADES_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_GRADES_REQUEST && resultCode == RESULT_OK) {
            data?.getParcelableExtra<Student>("updatedStudent")?.let { updatedStudent ->
                // Update the student in our lists
                val index = allStudents.indexOfFirst { it.studentID == updatedStudent.studentID }
                if (index != -1) {
                    allStudents[index] = updatedStudent
                    filterStudentsBySection(selectedSection) // Refresh the list
                    toast("Grades updated successfully")
                }
            }
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

    companion object {
        const val EDIT_GRADES_REQUEST = 1001
    }
}