package com.example.markahanmobile.fragments

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private var selectedDate = Calendar.getInstance().time
    private var selectedSection = "Faith" // Default to Faith section

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

        loadStudents()

        val sheetPage = findViewById<ImageView>(R.id.iconSheets)
        sheetPage.setOnClickListener{
            val intent = Intent(this, AttendanceSheetActivity::class.java)
            startActivity(intent)
        }
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

        // Only Faith and Hope sections
        sectionList.addAll(listOf("Faith", "Hope"))
        sectionAdapter = SectionAdapter(sectionList) { section ->
            selectedSection = section
            filterStudentsBySection(section)
        }
        sectionRecyclerView.adapter = sectionAdapter
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.attendanceRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AttendanceAdapter(displayedStudents) { position, status ->
            displayedStudents[position].attendanceStatus = status
        }
        recyclerView.adapter = adapter
    }

    private fun setupDateDisplay() {
        val dateTextView = findViewById<TextView>(R.id.txtDate)
        dateTextView.text = SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(selectedDate)

        dateTextView.setOnClickListener {
            val calendar = Calendar.getInstance().apply { time = selectedDate }
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.time
                    dateTextView.text = SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(selectedDate)
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
            // Pass current date to show the same month
            intent.putExtra("selected_date", selectedDate.time)
            startActivity(intent)
        }
    }

    private fun loadStudents() {
        allStudents.clear()
        // Same students as in StudentsListActivity
        allStudents.addAll(listOf(
            Student("1", "John", "Doe", "Faith", "4"),
            Student("2", "Jane", "Smith", "Hope", "4"),
            Student("3", "Emily", "Johnson", "Faith", "4")
        ))
        filterStudentsBySection(selectedSection)
    }

    private fun filterStudentsBySection(section: String) {
        displayedStudents.clear()
        displayedStudents.addAll(allStudents.filter { it.section == section })
        adapter.notifyDataSetChanged()
    }

    private fun saveAttendanceRecords() {
        val recordsToSave = displayedStudents.filter { it.attendanceStatus.isNotEmpty() }
        if (recordsToSave.isEmpty()) {
            toast("No attendance records to save")
            return
        }

        val dateStr = SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(selectedDate)
        val summary = recordsToSave.joinToString("\n") { student ->
            "${student.firstName} ${student.lastName}: ${student.attendanceStatus}"
        }

        AlertDialog.Builder(this)
            .setTitle("Confirm Attendance")
            .setMessage("Save attendance for $selectedSection on $dateStr?\n\n$summary")
            .setPositiveButton("Save") { _, _ ->
                // TODO: Implement actual database save
                toast("Attendance saved for ${recordsToSave.size} students")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}