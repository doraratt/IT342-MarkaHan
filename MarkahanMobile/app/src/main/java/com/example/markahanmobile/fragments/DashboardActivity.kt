package com.example.markahanmobile.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.markahanmobile.R
import com.example.markahanmobile.data.DataStore
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.navigation.NavigationView
import java.time.LocalDate

class DashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private var sections: List<String> = listOf()
    private var currentSectionIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        DataStore.init(this) // Ensure DataStore is initialized

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
        setupDynamicSections()
        setupPieChart()
        setupArrowListeners() // Add listeners for arrow buttons
        updateDashboardData()
    }

    private fun setupNavigation() {
        val headerView = navView.getHeaderView(0)
        val headerFirstName = headerView.findViewById<TextView>(R.id.header_firstname)
        val user = DataStore.getLoggedInUser()
        if (user != null && user.firstName!!.isNotEmpty()) {
            headerFirstName.text = "Welcome, Teacher ${user.firstName}!"
        } else {
            headerFirstName.text = "Welcome, Teacher!"
            Log.w("DashboardActivity", "No user or first name found")
        }

        val logoutView = navView.findViewById<TextView>(R.id.nav_logout)
        logoutView?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setIcon(R.drawable.warningsign)
                .setPositiveButton("Yes") { _, _ ->
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_cal -> startActivity(Intent(this, CalendarActivity::class.java))
                R.id.nav_list -> startActivity(Intent(this, StudentsListActivity::class.java))
                R.id.nav_att -> startActivity(Intent(this, AttendanceActivity::class.java))
                R.id.nav_grades -> startActivity(Intent(this, GradesActivity::class.java))
                R.id.nav_journal -> startActivity(Intent(this, JournalActivity::class.java))
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun setupDynamicSections() {
        val userId = DataStore.getLoggedInUser()?.userId ?: -1
        if (userId == -1) {
            Log.e("DashboardActivity", "No logged-in user found, cannot sync students")
            return
        }
        DataStore.syncStudents(userId) { success ->
            if (success) {
                sections = DataStore.getSections(includeArchived = false)
                Log.d("DashboardActivity", "Sections fetched: $sections")
                val sectionSpinner = findViewById<Spinner>(R.id.section_spinner)
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sections)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                sectionSpinner.adapter = adapter

                // Initialize the section text for attendance
                if (sections.isNotEmpty()) {
                    currentSectionIndex = 0
                    findViewById<TextView>(R.id.attendance_section_text).text = sections[currentSectionIndex]
                    updateArrowButtons()
                } else {
                    findViewById<TextView>(R.id.attendance_section_text).text = "No sections available"
                    findViewById<ImageButton>(R.id.arrow_left).isEnabled = false
                    findViewById<ImageButton>(R.id.arrow_right).isEnabled = false
                }
                updateStudentCounts()
            } else {
                Log.e("DashboardActivity", "Failed to sync students")
            }
        }
    }

    private fun setupPieChart() {
        val pieChart = findViewById<com.github.mikephil.charting.charts.PieChart>(R.id.attendance_chart)
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(ContextCompat.getColor(this, android.R.color.transparent))
        pieChart.transparentCircleRadius = 61f
        pieChart.legend.isEnabled = true
        pieChart.setNoDataText("Loading attendance data...")
        pieChart.setNoDataTextColor(ContextCompat.getColor(this, android.R.color.black))
        pieChart.visibility = View.VISIBLE
        Log.d("DashboardActivity", "PieChart setup - Visibility: ${pieChart.visibility == View.VISIBLE}, Width: ${pieChart.width}, Height: ${pieChart.height}")
    }

    private fun setupArrowListeners() {
        val arrowLeft = findViewById<ImageButton>(R.id.arrow_left)
        val arrowRight = findViewById<ImageButton>(R.id.arrow_right)
        val sectionText = findViewById<TextView>(R.id.attendance_section_text)

        arrowLeft.setOnClickListener {
            if (currentSectionIndex > 0) {
                currentSectionIndex--
                sectionText.text = sections[currentSectionIndex]
                updateArrowButtons()
                updatePieChart()
            }
        }

        arrowRight.setOnClickListener {
            if (currentSectionIndex < sections.size - 1) {
                currentSectionIndex++
                sectionText.text = sections[currentSectionIndex]
                updateArrowButtons()
                updatePieChart()
            }
        }
    }

    private fun updateArrowButtons() {
        val arrowLeft = findViewById<ImageButton>(R.id.arrow_left)
        val arrowRight = findViewById<ImageButton>(R.id.arrow_right)
        arrowLeft.isEnabled = currentSectionIndex > 0
        arrowRight.isEnabled = currentSectionIndex < sections.size - 1
    }

    private fun updatePieChart() {
        val userId = DataStore.getLoggedInUser()?.userId ?: -1
        if (userId == -1) {
            Log.e("DashboardActivity", "No logged-in user found, cannot sync attendance records")
            return
        }

        DataStore.syncAttendanceRecords(userId) { success ->
            if (success) {
                val sectionText = findViewById<TextView>(R.id.attendance_section_text)
                val pieChart = findViewById<com.github.mikephil.charting.charts.PieChart>(R.id.attendance_chart)
                val chartError = findViewById<TextView>(R.id.chart_error)
                val selectedAttendanceSection = sectionText.text.toString()
                val startDate = LocalDate.now()
                val endDate = LocalDate.now()
                val attendanceRecords = DataStore.getAttendanceRecords(selectedAttendanceSection, startDate, endDate)

                Log.d("DashboardActivity", "Attendance records fetched for section '$selectedAttendanceSection' from $startDate to $endDate: ${attendanceRecords.size}")
                attendanceRecords.forEach { record ->
                    Log.d("DashboardActivity", "Record: studentId=${record.studentId}, status=${record.status}, date=${record.date}")
                }

                val present = attendanceRecords.count { it.status == "Present" }.toFloat()
                val late = attendanceRecords.count { it.status == "Late" }.toFloat()
                val totalStudents = DataStore.getStudents(selectedAttendanceSection, includeArchived = false).size.toFloat()
                val absent = totalStudents - (present + late)

                Log.d("DashboardActivity", "Pie chart data - Present: $present, Late: $late, Absent: $absent, Total Students: $totalStudents")

                runOnUiThread {
                    if (attendanceRecords.isNotEmpty()) {
                        // Create all entries for the legend, including zero values
                        val allEntries = listOf(
                            PieEntry(present, "Present"),
                            PieEntry(late, "Late"),
                            PieEntry(absent, "Absent")
                        )

                        // Create display entries, excluding zero values for chart slices
                        val displayEntries = allEntries.filter { it.value > 0f }

                        if (displayEntries.isEmpty()) {
                            // If no non-zero entries, show error message
                            pieChart.clear()
                            pieChart.visibility = View.GONE
                            chartError.visibility = View.VISIBLE
                            chartError.text = "No attendance data to display"
                            Log.w("DashboardActivity", "No non-zero attendance data to display in pie chart")
                        } else {
                            // Use displayEntries for the chart to exclude zero-value slices
                            val dataSet = PieDataSet(displayEntries, "Attendance")
                            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
                            dataSet.valueTextColor = ContextCompat.getColor(this, android.R.color.black)
                            dataSet.valueTextSize = 12f
                            dataSet.sliceSpace = 3f // Add space between slices

                            // Set up the legend to include all possible labels (Present, Late, Absent)
                            val legendEntries = allEntries.map { entry ->
                                com.github.mikephil.charting.components.LegendEntry().apply {
                                    label = entry.label
                                    formColor = when (entry.label) {
                                        "Present" -> ColorTemplate.MATERIAL_COLORS[0]
                                        "Late" -> ColorTemplate.MATERIAL_COLORS[1]
                                        "Absent" -> ColorTemplate.MATERIAL_COLORS[2]
                                        else -> ColorTemplate.MATERIAL_COLORS[0]
                                    }
                                }
                            }
                            pieChart.legend.apply {
                                setCustom(legendEntries)
                                isEnabled = true
                                orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
                                horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                                verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                                isWordWrapEnabled = true
                            }

                            val data = PieData(dataSet)
                            pieChart.data = data
                            pieChart.visibility = View.VISIBLE
                            chartError.visibility = View.GONE
                            pieChart.notifyDataSetChanged()
                            pieChart.invalidate()
                            pieChart.requestLayout() // Force layout refresh
                            Log.d("DashboardActivity", "Pie chart updated with data - Entries: ${displayEntries.size}, Visibility: ${pieChart.visibility == View.VISIBLE}, Width: ${pieChart.width}, Height: ${pieChart.height}")
                        }
                    } else {
                        pieChart.clear()
                        pieChart.visibility = View.GONE
                        chartError.visibility = View.VISIBLE
                        chartError.text = "No attendance data available"
                        Log.w("DashboardActivity", "No non-zero attendance data to display in pie chart")
                    }
                }
            } else {
                Log.e("DashboardActivity", "Failed to sync attendance records")
                runOnUiThread {
                    val pieChart = findViewById<com.github.mikephil.charting.charts.PieChart>(R.id.attendance_chart)
                    val chartError = findViewById<TextView>(R.id.chart_error)
                    pieChart.clear()
                    pieChart.visibility = View.GONE
                    chartError.visibility = View.VISIBLE
                    chartError.text = "Failed to load attendance data"
                }
            }
        }
    }

    private fun updateStudentCounts() {
        val sectionSpinner = findViewById<Spinner>(R.id.section_spinner)
        val maleCount = findViewById<TextView>(R.id.male_count)
        val femaleCount = findViewById<TextView>(R.id.female_count)
        val totalStudents = findViewById<TextView>(R.id.total_students)
        val selectedSection = sectionSpinner.selectedItem?.toString() ?: ""
        val students = DataStore.getStudents(selectedSection, includeArchived = false)
        Log.d("DashboardActivity", "Students fetched for section $selectedSection: ${students.size}")
        maleCount.text = "Male: ${students.count { it.gender == "Male" }}"
        femaleCount.text = "Female: ${students.count { it.gender == "Female" }}"
        totalStudents.text = "Total: ${students.size}"
    }

    private fun updateDashboardData() {
        val userId = DataStore.getLoggedInUser()?.userId ?: -1
        if (userId == -1) {
            Log.e("DashboardActivity", "No logged-in user found, cannot sync data")
            return
        }

        DataStore.syncStudents(userId) { studentsSuccess ->
            if (studentsSuccess) {
                updateStudentCounts()
                updatePieChart() // Update pie chart after syncing students
                DataStore.syncJournals(userId) { journalsSuccess ->
                    if (journalsSuccess) {
                        val journalText = findViewById<TextView>(R.id.journal_text)
                        val journalDate = findViewById<TextView>(R.id.journal_date)
                        val journals = DataStore.getJournals()
                        Log.d("DashboardActivity", "Journals fetched: ${journals.size}")
                        if (journals.isNotEmpty()) {
                            val latestJournal = journals[0]
                            journalText.text = latestJournal.entry
                            journalDate.text = "Date: ${latestJournal.date}"
                        } else {
                            journalText.text = "No journal entries available"
                            journalDate.text = "Date: N/A"
                        }
                    } else {
                        Log.e("DashboardActivity", "Failed to sync journals")
                    }
                }
                DataStore.syncEvents(userId) { eventsSuccess ->
                    if (eventsSuccess) {
                        val event1 = findViewById<TextView>(R.id.event1)
                        val event2 = findViewById<TextView>(R.id.event2)
                        val events = DataStore.getLatestEvents(2) // Get up to 2 latest events
                        if (events.isNotEmpty()) {
                            event1.text = "${events[0].eventDescription} (${events[0].date})"
                            event1.visibility = View.VISIBLE
                            if (events.size > 1) {
                                event2.text = "${events[1].eventDescription} (${events[1].date})"
                                event2.visibility = View.VISIBLE
                            } else {
                                event2.visibility = View.GONE
                            }
                        } else {
                            event1.text = "No events available"
                            event1.visibility = View.VISIBLE
                            event2.visibility = View.GONE
                        }
                    } else {
                        Log.e("DashboardActivity", "Failed to sync events")
                        runOnUiThread {
                            val event1 = findViewById<TextView>(R.id.event1)
                            val event2 = findViewById<TextView>(R.id.event2)
                            event1.text = "Failed to load events"
                            event1.visibility = View.VISIBLE
                            event2.visibility = View.GONE
                        }
                    }
                }
            } else {
                Log.e("DashboardActivity", "Failed to sync students")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateDashboardData()
    }
}