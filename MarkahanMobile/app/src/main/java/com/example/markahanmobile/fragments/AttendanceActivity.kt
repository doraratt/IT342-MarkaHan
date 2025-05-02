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
import com.example.markahanmobile.helper.AttendanceAdapter
import com.example.markahanmobile.helper.SectionAdapter
import com.google.android.material.navigation.NavigationView
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
    private var selectedSection = SectionAdapter.ALL_SECTIONS
    private var userId: Int = 0
    private val TAG = "AttendanceActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

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
            val student = displayedStudents[studentPosition]
            displayedStudents[studentPosition] = student.copy(attendanceStatus = status)
            Log.d(TAG, "Updated attendance for ${student.lastName}, ${student.firstName}: $status")
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

        DataStore.syncStudents(userId, includeArchived = false) { success ->
            if (success) {
                DataStore.syncAttendanceRecords(userId) { attendanceSuccess ->
                    progressBar.visibility = View.GONE
                    if (attendanceSuccess) {
                        allStudents.clear()
                        allStudents.addAll(DataStore.getStudents(includeArchived = false))
                        Log.d(TAG, "Loaded ${allStudents.size} students: ${allStudents.map { "${it.lastName}, ${it.firstName}, Section=${it.section}" }}")
                        val records = DataStore.getAttendanceRecords(selectedSection, selectedDate, selectedDate)
                        filterStudentsBySection(selectedSection, records)
                    } else {
                        Toast.makeText(this, "Error syncing attendance records", Toast.LENGTH_SHORT).show()
                        filterStudentsBySection(selectedSection)
                    }
                }
            } else {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error loading students", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSections() {
        sectionList.clear()
        sectionList.addAll(DataStore.getSections())
        sectionAdapter.updateSections(sectionList)
        if (sectionList.isNotEmpty()) {
            selectedSection = SectionAdapter.ALL_SECTIONS
            sectionAdapter.setSelectedPosition(0)
        }
        Log.d(TAG, "Loaded sections: $sectionList")
    }

    private fun filterStudentsBySection(section: String, records: List<AttendanceRecord> = emptyList()) {
        Log.d(TAG, "Filtering students for section: $section")
        displayedStudents.clear()
        val students = if (section == SectionAdapter.ALL_SECTIONS) {
            allStudents
        } else {
            allStudents.filter { it.section == section }
        }
        displayedStudents.addAll(students.map { student ->
            val record = records.firstOrNull { it.studentId == student.studentId && it.date == selectedDate }
            val status = record?.status ?: ""
            student.copy(attendanceStatus = status)
        })
        adapter.updateList(displayedStudents)
        updateNoStudentsVisibility()
        Log.d(TAG, "Filtered ${displayedStudents.size} students for section: $section")
    }

    private fun saveAttendanceRecords() {
        val recordsToSave = displayedStudents.filter { it.attendanceStatus.isNotEmpty() }
        if (recordsToSave.isEmpty()) {
            Toast.makeText(this, "No attendance records to save", Toast.LENGTH_SHORT).show()
            return
        }

        val formatter = DateTimeFormatter.ofPattern("MM/dd/yy", Locale.getDefault())
        val dateStr = selectedDate.format(formatter)
        val summary = recordsToSave.joinToString("\n") { student ->
            "${student.lastName}, ${student.firstName}: ${student.attendanceStatus}"
        }

        AlertDialog.Builder(this)
            .setTitle("Confirm Attendance")
            .setMessage("Save attendance for $selectedSection on $dateStr?\n\n$summary")
            .setPositiveButton("Save") { _, _ ->
                val newRecords = recordsToSave.map { student ->
                    AttendanceRecord(
                        attendanceId = 0,
                        studentId = student.studentId,
                        userId = userId,
                        date = selectedDate,
                        status = student.attendanceStatus,
                        section = student.section,
                        student = student,
                        user = DataStore.getLoggedInUser()
                    )
                }
                DataStore.addAttendanceRecords(newRecords) { success ->
                    if (success) {
                        Toast.makeText(this, "Attendance saved for ${recordsToSave.size} students", Toast.LENGTH_SHORT).show()
                        loadStudents()
                    } else {
                        Toast.makeText(this, "Failed to save attendance. Check logs for details.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateNoStudentsVisibility() {
        noStudentsText.visibility = if (displayedStudents.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (displayedStudents.isEmpty()) View.GONE else View.VISIBLE
    }
}