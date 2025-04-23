package com.example.markahanmobile.fragments

import android.app.DatePickerDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.markahanmobile.data.AttendanceRecord
import com.example.markahanmobile.data.Student
import com.example.markahanmobile.utils.toast
import com.example.markahanmobile.R
import java.text.SimpleDateFormat
import java.util.*

class AttendanceSheetActivity : AppCompatActivity() {

    private lateinit var scrollDates: HorizontalScrollView
    private lateinit var studentNamesContainer: LinearLayout
    private lateinit var datesHeader: LinearLayout
    private lateinit var attendanceDataContainer: LinearLayout
    private lateinit var monthYearText: TextView

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
    private var selectedSection: String = ""

    // Sample data - replace with your actual data source
    private val allStudents = mutableListOf<Student>()
    private val attendanceRecords = mutableListOf<AttendanceRecord>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_sheet)

        // Get selected section from intent
        selectedSection = intent.getStringExtra("SELECTED_SECTION") ?: ""

        // Initialize views
        scrollDates = findViewById(R.id.scrollDates)
        studentNamesContainer = findViewById(R.id.studentNames)
        datesHeader = findViewById(R.id.datesHeader)
        attendanceDataContainer = findViewById(R.id.attendanceData)
        monthYearText = findViewById(R.id.txtMonthYear)

        // Load sample data
        loadSampleData()

        // Set current month
        updateMonthDisplay()

        // Set up navigation
        findViewById<ImageView>(R.id.btnPrevMonth).setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateMonthDisplay()
            loadAttendanceData()
        }

        findViewById<ImageView>(R.id.btnNextMonth).setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateMonthDisplay()
            loadAttendanceData()
        }

        findViewById<ImageView>(R.id.iconCalendar).setOnClickListener {
            showMonthYearPicker()
        }

        findViewById<Button>(R.id.btnPrint).setOnClickListener {
            toast("Printing attendance sheet for $selectedSection...")
            // TODO: Implement printing functionality
        }

        loadAttendanceData()
    }

    private fun loadSampleData() {
        // Sample students - replace with your actual data
        allStudents.addAll(listOf(
            Student("1", "John", "Doe", "Faith", "4"),
            Student("2", "Jane", "Smith", "Hope", "4"),
            Student("3", "Emily", "Johnson", "Faith", "4"),
        ))

        // Sample attendance records - replace with your actual data
        val sampleDate = Calendar.getInstance().apply {
            set(2023, Calendar.APRIL, 1)
        }.time

        attendanceRecords.addAll(listOf(
            AttendanceRecord("1", sampleDate, "P", "Faith"),
            AttendanceRecord("2", sampleDate, "A", "Hope"),
            AttendanceRecord("3", sampleDate, "P", "Faith"),
        ))
    }

    private fun updateMonthDisplay() {
        monthYearText.text = dateFormat.format(calendar.time)
    }

    private fun showMonthYearPicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, _ ->
                calendar.set(selectedYear, selectedMonth, 1)
                updateMonthDisplay()
                loadAttendanceData()
            },
            year,
            month,
            1
        ).apply {
            // Try to hide the day picker
            try {
                val dayPicker = datePicker.findViewById<View>(
                    resources.getIdentifier("day", "id", "android")
                )
                dayPicker?.visibility = View.GONE
            } catch (e: Exception) {
                e.printStackTrace()
            }
            setTitle("Select Month and Year")
        }.show()
    }

    private fun loadAttendanceData() {
        // Clear existing views
        studentNamesContainer.removeAllViews()
        datesHeader.removeAllViews()
        attendanceDataContainer.removeAllViews()

        // Get all days in current month
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        // Create dates for the month
        val dates = (1..daysInMonth).map { day ->
            Calendar.getInstance().apply {
                set(currentYear, currentMonth, day)
            }.time
        }

        // Add date headers
        dates.forEach { date ->
            val dateHeader = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    60.dpToPx(),
                    ViewGroup.LayoutParams.MATCH_PARENT
                ).apply {
                    marginStart = 1.dpToPx()
                }
                text = dayFormat.format(date)
                setTextColor(ContextCompat.getColor(this@AttendanceSheetActivity, R.color.white))
                textSize = 12f
                gravity = Gravity.CENTER
                setTypeface(typeface, Typeface.BOLD)
                background = ContextCompat.getDrawable(this@AttendanceSheetActivity, R.color.colorPrimary)
            }
            datesHeader.addView(dateHeader)
        }

        // Filter students by selected section
        val studentsToShow = if (selectedSection.isNotEmpty() && selectedSection != "All Sections") {
            allStudents.filter { it.section == selectedSection }
        } else {
            allStudents
        }

        // Create student rows
        studentsToShow.forEachIndexed { index, student ->
            // Add student name (fixed column)
            studentNamesContainer.addView(
                TextView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        48.dpToPx()
                    )
                    text = "${student.firstName} ${student.lastName}"
                    textSize = 12f
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(8.dpToPx(), 0, 8.dpToPx(), 0)
                    background = ContextCompat.getDrawable(
                        this@AttendanceSheetActivity,
                        if (index % 2 == 0) R.color.white else R.color.light_gray
                    )
                }
            )

            // Add attendance row for this student
            val attendanceRow = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    48.dpToPx()
                )
                orientation = LinearLayout.HORIZONTAL
                background = ContextCompat.getDrawable(
                    this@AttendanceSheetActivity,
                    if (index % 2 == 0) R.color.white else R.color.light_gray
                )
            }

            // Add attendance status for each date
            dates.forEach { date ->
                val status = attendanceRecords.firstOrNull {
                    it.studentId == student.studentID && isSameDay(it.date, date)
                }?.status ?: ""

                attendanceRow.addView(TextView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        60.dpToPx(),
                        ViewGroup.LayoutParams.MATCH_PARENT
                    ).apply {
                        marginStart = 1.dpToPx()
                    }
                    text = status
                    gravity = Gravity.CENTER
                    textSize = 12f
                    setTextColor(
                        when (status) {
                            "P" -> ContextCompat.getColor(context, R.color.present)
                            "A" -> ContextCompat.getColor(context, R.color.absent)
                            "L" -> ContextCompat.getColor(context, R.color.late)
                            else -> ContextCompat.getColor(context, R.color.black)
                        }
                    )
                })
            }
            attendanceDataContainer.addView(attendanceRow)
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}