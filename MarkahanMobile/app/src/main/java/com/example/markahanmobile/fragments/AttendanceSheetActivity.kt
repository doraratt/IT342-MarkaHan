package com.example.markahanmobile.fragments

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.markahanmobile.R
import com.example.markahanmobile.data.AttendanceRecord
import com.example.markahanmobile.data.DataStore
import com.example.markahanmobile.data.Student
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class AttendanceSheetActivity : AppCompatActivity() {

    private lateinit var scrollDates: HorizontalScrollView
    private lateinit var studentNamesContainer: LinearLayout
    private lateinit var datesHeader: LinearLayout
    private lateinit var attendanceDataContainer: LinearLayout
    private lateinit var monthYearText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var noStudentsText: TextView

    private var selectedSection: String = ""
    private var chosenDate: LocalDate? = null
    private var selectedMonth: LocalDate = LocalDate.now()
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_sheet)

        // Set up the Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable the back arrow

        selectedSection = intent.getStringExtra("SELECTED_SECTION") ?: ""

        val selectedDateStr = intent.getStringExtra("SELECTED_DATE")
        if (selectedDateStr != null) {
            chosenDate = LocalDate.parse(selectedDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            selectedMonth = chosenDate!!
        }

        scrollDates = findViewById(R.id.scrollDates)
        studentNamesContainer = findViewById(R.id.studentNames)
        datesHeader = findViewById(R.id.datesHeader)
        attendanceDataContainer = findViewById(R.id.attendanceData)
        monthYearText = findViewById(R.id.txtMonthYear)
        progressBar = findViewById(R.id.progressBar)
        noStudentsText = findViewById(R.id.noStudentsText)

        updateMonthDisplay()

        findViewById<ImageView>(R.id.btnPrevMonth).setOnClickListener {
            selectedMonth = selectedMonth.minusMonths(1)
            updateMonthDisplay()
            loadAttendanceData()
        }

        findViewById<ImageView>(R.id.btnNextMonth).setOnClickListener {
            selectedMonth = selectedMonth.plusMonths(1)
            updateMonthDisplay()
            loadAttendanceData()
        }

        findViewById<ImageView>(R.id.iconCalendar).setOnClickListener {
            showMonthYearPicker()
        }

        findViewById<Button>(R.id.btnPrint).setOnClickListener {
            Toast.makeText(this, "Printing attendance sheet for $selectedSection...", Toast.LENGTH_SHORT).show()
            // TODO: Implement printing functionality
        }

        loadAttendanceData()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed() // Handle back button press
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateMonthDisplay() {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        monthYearText.text = selectedMonth.format(formatter)
    }

    private fun showMonthYearPicker() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_month_year_picker, null)
        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.monthPicker)
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)

        // Set up month picker
        monthPicker.minValue = 0
        monthPicker.maxValue = 11
        monthPicker.displayedValues = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        monthPicker.value = selectedMonth.monthValue - 1

        // Set up year picker
        val currentYear = LocalDate.now().year
        yearPicker.minValue = currentYear - 50 // Allow 50 years back
        yearPicker.maxValue = currentYear + 10 // Allow 10 years forward
        yearPicker.value = selectedMonth.year

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            val selectedMonthVal = monthPicker.value + 1
            val selectedYear = yearPicker.value
            selectedMonth = LocalDate.of(selectedYear, selectedMonthVal, 1)
            updateMonthDisplay()
            loadAttendanceData()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadAttendanceData() {
        studentNamesContainer.removeAllViews()
        datesHeader.removeAllViews()
        attendanceDataContainer.removeAllViews()

        val daysInMonth = selectedMonth.lengthOfMonth()
        val dates = (1..daysInMonth).map { day ->
            LocalDate.of(selectedMonth.year, selectedMonth.month, day)
        }

        dates.forEach { date ->
            val dateHeader = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    60.dpToPx(),
                    ViewGroup.LayoutParams.MATCH_PARENT
                ).apply {
                    marginStart = 1.dpToPx()
                }
                text = date.dayOfMonth.toString()
                setTextColor(ContextCompat.getColor(this@AttendanceSheetActivity, R.color.white))
                textSize = 12f
                gravity = Gravity.CENTER
                setTypeface(typeface, Typeface.BOLD)
                background = if (chosenDate != null && date == chosenDate) {
                    ContextCompat.getDrawable(this@AttendanceSheetActivity, R.color.colorAccent)
                } else {
                    ContextCompat.getDrawable(this@AttendanceSheetActivity, R.color.header_background)
                }
            }
            datesHeader.addView(dateHeader)
        }

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        userId = sharedPreferences.getInt("userId", -1)

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        progressBar.visibility = View.VISIBLE
        noStudentsText.visibility = View.GONE

        CoroutineScope(Dispatchers.Main).launch {
            var studentSuccess = false
            var attendanceSuccess = false
            var errorMessage = ""
            withContext(Dispatchers.IO) {
                // Sync students with retry logic
                var attempts = 0
                val maxAttempts = 3 // Increased to 3 attempts
                while (attempts < maxAttempts && !studentSuccess) {
                    attempts++
                    try {
                        val studentDeferred = kotlinx.coroutines.CompletableDeferred<Boolean>()
                        DataStore.syncStudents(userId, includeArchived = false) { success ->
                            studentDeferred.complete(success)
                        }
                        studentSuccess = kotlinx.coroutines.withTimeoutOrNull(15000L) { studentDeferred.await() } ?: false
                        if (!studentSuccess) {
                            android.util.Log.w("AttendanceSheetActivity", "syncStudents: Attempt $attempts failed")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AttendanceSheetActivity", "syncStudents: Attempt $attempts error: ${e.message}", e)
                        errorMessage = "Error syncing students: ${e.message}"
                    }
                }

                if (studentSuccess) {
                    // Sync attendance records with retry logic
                    attempts = 0
                    while (attempts < maxAttempts && !attendanceSuccess) {
                        attempts++
                        try {
                            val attendanceDeferred = kotlinx.coroutines.CompletableDeferred<Boolean>()
                            DataStore.syncAttendanceRecords(userId) { success ->
                                attendanceDeferred.complete(success)
                            }
                            attendanceSuccess = kotlinx.coroutines.withTimeoutOrNull(15000L) { attendanceDeferred.await() } ?: false
                            if (!attendanceSuccess) {
                                android.util.Log.w("AttendanceSheetActivity", "syncAttendanceRecords: Attempt $attempts failed")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("AttendanceSheetActivity", "syncAttendanceRecords: Attempt $attempts error: ${e.message}", e)
                            errorMessage = "Error syncing attendance: ${e.message}"
                        }
                    }
                }
            }

            progressBar.visibility = View.GONE

            if (!studentSuccess) {
                Toast.makeText(this@AttendanceSheetActivity, "Error loading students", Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (!attendanceSuccess) {
                Toast.makeText(this@AttendanceSheetActivity, "Error syncing attendance records", Toast.LENGTH_SHORT).show()
            }

            val students = DataStore.getStudents(selectedSection, includeArchived = false)
            val startDate = LocalDate.of(selectedMonth.year, selectedMonth.month, 1)
            val endDate = startDate.plusDays(daysInMonth - 1L)
            val attendanceRecords = DataStore.getAttendanceRecords(selectedSection, startDate, endDate)

            // Debug log to verify fetched data
            android.util.Log.d("AttendanceSheetActivity", "Fetched ${attendanceRecords.size} attendance records for section $selectedSection, from $startDate to $endDate")
            attendanceRecords.forEach { record ->
                android.util.Log.d("AttendanceSheetActivity", "Record: studentId=${record.studentId}, date=${record.date}, status=${record.status}, section=${record.section}")
            }
            students.forEach { student ->
                android.util.Log.d("AttendanceSheetActivity", "Student: studentId=${student.studentId}, name=${student.lastName}, ${student.firstName}")
            }

            if (students.isEmpty()) {
                noStudentsText.visibility = View.VISIBLE
                Toast.makeText(this@AttendanceSheetActivity, "No students found for section $selectedSection", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val maleStudents = students.filter { it.gender == "Male" }
                .sortedWith(compareBy({ it.lastName }, { it.firstName }))
            val femaleStudents = students.filter { it.gender == "Female" }
                .sortedWith(compareBy({ it.lastName }, { it.firstName }))

            var rowIndex = 0

            if (maleStudents.isNotEmpty()) {
                studentNamesContainer.addView(
                    TextView(this@AttendanceSheetActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            48.dpToPx()
                        )
                        text = "Male Students"
                        textSize = 16f
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(8.dpToPx(), 0, 8.dpToPx(), 0)
                        setTextColor(ContextCompat.getColor(this@AttendanceSheetActivity, R.color.white))
                        background = ContextCompat.getDrawable(this@AttendanceSheetActivity, R.color.header_background)
                        setTypeface(typeface, Typeface.BOLD)
                    }
                )

                val maleHeaderRow = LinearLayout(this@AttendanceSheetActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        48.dpToPx()
                    )
                    orientation = LinearLayout.HORIZONTAL
                    background = ContextCompat.getDrawable(this@AttendanceSheetActivity, R.color.header_background)
                }
                dates.forEach {
                    maleHeaderRow.addView(TextView(this@AttendanceSheetActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            60.dpToPx(),
                            ViewGroup.LayoutParams.MATCH_PARENT
                        ).apply {
                            marginStart = 1.dpToPx()
                        }
                    })
                }
                attendanceDataContainer.addView(maleHeaderRow)
                rowIndex++

                maleStudents.forEach { student ->
                    addStudentRow(student, dates, attendanceRecords, rowIndex)
                    rowIndex++
                }
            }

            if (femaleStudents.isNotEmpty()) {
                studentNamesContainer.addView(
                    TextView(this@AttendanceSheetActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            48.dpToPx()
                        )
                        text = "Female Students"
                        textSize = 16f
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(8.dpToPx(), 0, 8.dpToPx(), 0)
                        setTextColor(ContextCompat.getColor(this@AttendanceSheetActivity, R.color.white))
                        background = ContextCompat.getDrawable(this@AttendanceSheetActivity, R.color.header_background)
                        setTypeface(typeface, Typeface.BOLD)
                    }
                )

                val femaleHeaderRow = LinearLayout(this@AttendanceSheetActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        48.dpToPx()
                    )
                    orientation = LinearLayout.HORIZONTAL
                    background = ContextCompat.getDrawable(this@AttendanceSheetActivity, R.color.header_background)
                }
                dates.forEach {
                    femaleHeaderRow.addView(TextView(this@AttendanceSheetActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            60.dpToPx(),
                            ViewGroup.LayoutParams.MATCH_PARENT
                        ).apply {
                            marginStart = 1.dpToPx()
                        }
                    })
                }
                attendanceDataContainer.addView(femaleHeaderRow)
                rowIndex++

                femaleStudents.forEach { student ->
                    addStudentRow(student, dates, attendanceRecords, rowIndex)
                    rowIndex++
                }
            }

            chosenDate?.let { date ->
                val dayIndex = dates.indexOfFirst { it == date }
                if (dayIndex != -1) {
                    scrollDates.post {
                        scrollDates.scrollTo(dayIndex * 61.dpToPx(), 0) // 60dp + 1dp margin
                    }
                }
            }
        }
    }

    private fun addStudentRow(student: Student, dates: List<LocalDate>, attendanceRecords: List<AttendanceRecord>, rowIndex: Int) {
        studentNamesContainer.addView(
            TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    48.dpToPx()
                )
                text = "${student.lastName}, ${student.firstName}"
                textSize = 12f
                gravity = Gravity.CENTER_VERTICAL
                setPadding(8.dpToPx(), 0, 8.dpToPx(), 0)
                background = ContextCompat.getDrawable(
                    this@AttendanceSheetActivity,
                    if (rowIndex % 2 == 0) R.color.white else R.color.light_gray
                )
            }
        )

        val attendanceRow = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                48.dpToPx()
            )
            orientation = LinearLayout.HORIZONTAL
            background = ContextCompat.getDrawable(
                this@AttendanceSheetActivity,
                if (rowIndex % 2 == 0) R.color.white else R.color.light_gray
            )
        }

        dates.forEach { date ->
            val record = attendanceRecords.firstOrNull {
                it.studentId == student.studentId && it.date == date
            }

            val status = record?.status?.capitalize() ?: ""

            attendanceRow.addView(TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    60.dpToPx(),
                    ViewGroup.LayoutParams.MATCH_PARENT
                ).apply {
                    marginStart = 1.dpToPx()
                }
                text = when (status) {
                    "Present" -> "P"
                    "Absent" -> "A"
                    "Late" -> "L"
                    else -> ""
                }
                gravity = Gravity.CENTER
                textSize = 12f
                setTextColor(
                    when (status) {
                        "Present" -> ContextCompat.getColor(context, R.color.present)
                        "Absent" -> ContextCompat.getColor(context, R.color.absent)
                        "Late" -> ContextCompat.getColor(context, R.color.late)
                        else -> ContextCompat.getColor(context, R.color.black)
                    }
                )
            })
        }
        attendanceDataContainer.addView(attendanceRow)
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}