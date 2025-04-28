package com.example.markahanmobile.fragments

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.data.AttendanceRecord
import com.example.markahanmobile.data.DataStore
import com.example.markahanmobile.data.Student
import com.example.markahanmobile.helper.AttendanceAdapter
import com.example.markahanmobile.helper.SectionAdapter
import com.example.markahanmobile.utils.toast
import com.example.markahanmobile.R
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.*

class AttendanceActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var sectionRecyclerView: RecyclerView
    private lateinit var adapter: AttendanceAdapter
    private lateinit var sectionAdapter: SectionAdapter
    private val allStudents = mutableListOf<Student>()
    private val displayedStudents = mutableListOf<Student>()
    private val sectionList = mutableListOf<String>()
    private var selectedDate = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time
    private var selectedSection = SectionAdapter.ALL_SECTIONS
    private val TAG = "AttendanceActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

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
        adapter = AttendanceAdapter { position, status ->
            displayedStudents[position].attendanceStatus = status
        }
        recyclerView.adapter = adapter
        filterStudentsBySection(selectedSection)
    }

    private fun setupDateDisplay() {
        val dateTextView = findViewById<TextView>(R.id.txtDate)
        dateTextView.text = SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(selectedDate)

        dateTextView.setOnClickListener {
            val calendar = Calendar.getInstance().apply { time = selectedDate }
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day, 0, 0, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    selectedDate = calendar.time
                    dateTextView.text = SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(selectedDate)
                    loadStudents() // Refresh to apply any existing attendance statuses
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
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
            intent.putExtra("SELECTED_DATE", selectedDate.time)
            startActivity(intent)
        }
    }

    private fun loadStudents() {
        allStudents.clear()
        allStudents.addAll(DataStore.getStudents())
        Log.d(TAG, "Loaded ${allStudents.size} students: ${allStudents.map { "${it.lastName}, ${it.firstName}, Section=${it.section}" }}")
        filterStudentsBySection(selectedSection)
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

    private fun filterStudentsBySection(section: String) {
        Log.d(TAG, "Filtering students for section: $section")
        displayedStudents.clear()
        val students = DataStore.getStudents(section)
        val records = DataStore.getAttendanceRecords(section, selectedDate, selectedDate)
        displayedStudents.addAll(students.map { student ->
            val status = records.firstOrNull { it.studentId == student.studentID }?.status ?: ""
            student.copy(attendanceStatus = when (status) {
                "P" -> "P"
                "A" -> "A"
                "L" -> "L"
                else -> ""
            })
        }.sortedWith(compareBy({ it.gender != "Male" }, { it.lastName }, { it.firstName })))
        adapter.updateList(displayedStudents)
        Log.d(TAG, "Filtered ${displayedStudents.size} students for section: $section, Students: ${displayedStudents.map { "${it.lastName}, ${it.firstName}, Section=${it.section}" }}")
    }

    private fun saveAttendanceRecords() {
        val recordsToSave = displayedStudents.filter { it.attendanceStatus.isNotEmpty() }
        if (recordsToSave.isEmpty()) {
            toast("No attendance records to save")
            return
        }

        val dateStr = SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(selectedDate)
        val summary = recordsToSave.joinToString("\n") { student ->
            "${student.lastName}, ${student.firstName}: ${student.attendanceStatus}"
        }

        AlertDialog.Builder(this)
            .setTitle("Confirm Attendance")
            .setMessage("Save attendance for $selectedSection on $dateStr?\n\n$summary")
            .setPositiveButton("Save") { _, _ ->
                val newRecords = recordsToSave.map { student ->
                    AttendanceRecord(
                        studentId = student.studentID,
                        date = selectedDate,
                        status = student.attendanceStatus,
                        section = selectedSection
                    )
                }
                DataStore.addAttendanceRecords(newRecords)
                toast("Attendance saved for ${recordsToSave.size} students")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}