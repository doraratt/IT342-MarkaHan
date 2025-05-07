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
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.R
import com.example.markahanmobile.data.DataStore
import com.example.markahanmobile.data.Student
import com.example.markahanmobile.helper.SectionAdapter
import com.example.markahanmobile.helper.StudentAdapter

class ArchiveActivity : AppCompatActivity() {

    private lateinit var iconSearchStudent: ImageView
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
    private val TAG = "ArchiveActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_archive)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Archived Students"

        progressBar = findViewById(R.id.progressBar)
        noStudentsText = findViewById(R.id.noStudentsText)
        noSectionsText = findViewById(R.id.noSectionsText)
        noSectionsText.text = "No sections available"

        setupRecyclerViews()
        setupButtons()

        loadStudents()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupRecyclerViews() {
        sectionRecyclerView = findViewById(R.id.recyclerViewSections)
        sectionRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        sectionAdapter = SectionAdapter(sectionList) { section ->
            if (!isUpdating) { // Prevent recursion from setSelectedPosition
                selectedSection = section
                isShowingSearchResults = false
                filterStudentsBySection(section)
            }
        }
        sectionRecyclerView.adapter = sectionAdapter

        val studentRecyclerView = findViewById<RecyclerView>(R.id.archivedStudentRecyclerView)
        studentRecyclerView.layoutManager = LinearLayoutManager(this)
        studentAdapter = StudentAdapter(
            studentList,
            ::editStudent,
            ::unarchiveStudent,
            ::deleteStudent,
            showArchived = true
        )
        studentRecyclerView.adapter = studentAdapter
    }

    private fun setupButtons() {
        iconSearchStudent = findViewById(R.id.iconSearchStudent)
        iconSearchStudent.setOnClickListener { showSearchStudentDialog() }
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

        DataStore.syncStudents(userId, includeArchived = true) { success ->
            progressBar.visibility = View.GONE
            if (success) {
                Log.d(TAG, "syncStudents successful, fetching sections and students")
                DataStore.syncGrades(userId) { gradeSuccess ->
                    if (gradeSuccess) {
                        Log.d(TAG, "syncGrades successful")
                    } else {
                        Log.e(TAG, "Error syncing grades")
                        Toast.makeText(this, "Error syncing grades", Toast.LENGTH_SHORT).show()
                    }
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
                }
            } else {
                Log.e(TAG, "Error syncing students")
                Toast.makeText(this, "Error loading archived students", Toast.LENGTH_SHORT).show()
                noStudentsText.visibility = View.VISIBLE
                updateNoSectionsVisibility()
            }
        }
    }

    private fun loadSections() {
        sectionList.clear()
        val sections = DataStore.getStudents(includeArchived = true)
            .filter { it.isArchived }
            .map { it.section }
            .distinct()
        sectionList.addAll(sections)
        sectionAdapter.updateSections(sectionList)
        updateNoSectionsVisibility()
        Log.d(TAG, "Loaded sections with archived students: $sections")
    }

    private fun filterStudentsBySection(section: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", -1)
        if (userId == -1) {
            Log.e(TAG, "User not logged in during filterStudentsBySection")
            return
        }
        DataStore.syncStudents(userId, includeArchived = true) { success ->
            if (success) {
                studentList.clear()
                val students = DataStore.getStudents(section, includeArchived = true)
                    .filter { it.isArchived }
                    .sortedWith(
                        compareBy(
                            { it.gender != "Male" },
                            { it.lastName },
                            { it.firstName })
                    )
                studentList.addAll(students)
                studentAdapter.updateList(studentList)
                updateNoStudentsVisibility()
                Log.d(
                    TAG,
                    "Filtered students for section '$section': ${students.size} students, students=$students"
                )
                val position = sectionList.indexOf(section)
                if (position != -1 && !isUpdating) { // Prevent recursion
                    isUpdating = true
                    try {
                        sectionAdapter.setSelectedPosition(position)
                    } finally {
                        isUpdating = false
                    }
                }
            } else {
                Log.e(TAG, "Failed to sync students in filterStudentsBySection")
                Toast.makeText(this, "Error refreshing archived students", Toast.LENGTH_SHORT)
                    .show()
                noStudentsText.visibility = View.VISIBLE
            }
        }
    }

    private fun editStudent(student: Student) {
        // Placeholder: Implement edit functionality if needed
        Toast.makeText(this, "Edit student not implemented", Toast.LENGTH_SHORT).show()
    }

    private fun unarchiveStudent(student: Student) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Unarchive")
            .setMessage("Are you sure you want to unarchive this student?")
            .setPositiveButton("Yes") { _, _ ->
                DataStore.archiveStudent(student.studentId, isArchived = false) { success ->
                    if (success) {
                        Log.d(TAG, "Student ${student.studentId} unarchived successfully")
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
                        Toast.makeText(this, "Student unarchived successfully", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Log.e(TAG, "Failed to unarchive student ${student.studentId}")
                        Toast.makeText(this, "Failed to unarchive student", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteStudent(student: Student) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to permanently delete this student? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                DataStore.deleteStudent(student.studentId) { success ->
                    if (success) {
                        Log.d(TAG, "Student ${student.studentId} deleted successfully")
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
                        Toast.makeText(this, "Student deleted successfully", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Log.e(TAG, "Failed to delete student ${student.studentId}")
                        Toast.makeText(this, "Failed to delete student", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSearchStudentDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_search_student, null)
        val searchInput = dialogView.findViewById<EditText>(R.id.searchInput)
        val searchButton = dialogView.findViewById<Button>(R.id.btnSearch)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Search Archived Students")
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
            val filtered = DataStore.getStudents(includeArchived = true)
                .filter {
                    it.isArchived && "${it.lastName}, ${it.firstName}".contains(
                        query,
                        ignoreCase = true
                    )
                }
            studentList.clear()
            studentList.addAll(
                filtered.sortedWith(
                    compareBy(
                        { it.gender != "Male" },
                        { it.lastName },
                        { it.firstName })
                )
            )
            studentAdapter.updateList(studentList)
            updateNoStudentsVisibility()
            sectionAdapter.setSelectedPosition(-1)
            isShowingSearchResults = true
            Log.d(TAG, "Search query '$query': ${filtered.size} students found")
        } else {
            isShowingSearchResults = false
            filterStudentsBySection(selectedSection)
        }
    }

    private fun updateNoStudentsVisibility() {
        val recyclerView = findViewById<RecyclerView>(R.id.archivedStudentRecyclerView)
        val params = recyclerView.layoutParams as RelativeLayout.LayoutParams

        noStudentsText.visibility = if (studentList.isEmpty()) View.VISIBLE else View.GONE
        if (studentList.isEmpty()) {
            params.removeRule(RelativeLayout.BELOW)
            params.addRule(RelativeLayout.BELOW, R.id.noStudentsText)
            recyclerView.visibility = View.GONE
        } else {
            params.removeRule(RelativeLayout.BELOW)
            params.addRule(RelativeLayout.BELOW, R.id.headerRow)
            recyclerView.visibility = View.VISIBLE
        }
        recyclerView.layoutParams = params
        recyclerView.requestLayout()
        Log.d(
            TAG,
            "Updated noStudentsText visibility: ${noStudentsText.visibility == View.VISIBLE}, RecyclerView below: ${if (studentList.isEmpty()) "noStudentsText" else "headerRow"}"
        )
    }

    private fun updateNoSectionsVisibility() {
        noSectionsText.visibility = if (sectionList.isEmpty()) View.VISIBLE else View.GONE
        findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.sectionRow).visibility =
            if (sectionList.isEmpty()) View.GONE else View.VISIBLE
        Log.d(
            TAG,
            "Updated noSectionsText visibility: ${noSectionsText.visibility == View.VISIBLE}"
        )
    }

    override fun onResume() {
        super.onResume()
        if (!isShowingSearchResults) {
            loadStudents()
        }
    }
}