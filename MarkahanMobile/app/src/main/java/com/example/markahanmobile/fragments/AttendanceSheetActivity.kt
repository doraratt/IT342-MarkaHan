package com.example.markahanmobile.fragments

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.markahanmobile.data.DataStore
import com.example.markahanmobile.utils.toast
import com.example.markahanmobile.R
import com.example.markahanmobile.data.Student
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
    private var chosenDate: Date? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_sheet)

        selectedSection = intent.getStringExtra("SELECTED_SECTION") ?: ""

        val selectedDateMillis = intent.getLongExtra("SELECTED_DATE", -1L)
        if (selectedDateMillis != -1L){
            calendar.time = Date(selectedDateMillis)
        }

        scrollDates = findViewById(R.id.scrollDates)
        studentNamesContainer = findViewById(R.id.studentNames)
        datesHeader = findViewById(R.id.datesHeader)
        attendanceDataContainer = findViewById(R.id.attendanceData)
        monthYearText = findViewById(R.id.txtMonthYear)

        updateMonthDisplay()

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

        findViewById<ImageView>(R.id.iconTable).setOnClickListener {
            startActivity(Intent(this, AttendanceActivity::class.java))
        }

        loadAttendanceData()
    }

    private fun updateMonthDisplay() {
        monthYearText.text = dateFormat.format(calendar.time)
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
        monthPicker.value = calendar.get(Calendar.MONTH)

        // Set up year picker
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = currentYear - 50 // Allow 50 years back
        yearPicker.maxValue = currentYear + 10 // Allow 10 years forward
        yearPicker.value = calendar.get(Calendar.YEAR)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            val selectedMonth = monthPicker.value
            val selectedYear = yearPicker.value
            calendar.set(selectedYear, selectedMonth, 1)
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

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val dates = (1..daysInMonth).map { day ->
            Calendar.getInstance().apply {
                set(currentYear, currentMonth, day, 0,0 ,0)
                set(Calendar.MILLISECOND, 0)
            }.time
        }

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
                background =
                    if (chosenDate != null && isSameDay(date, chosenDate!!)) {
                    ContextCompat.getDrawable(this@AttendanceSheetActivity, R.color.colorAccent)
                    } else {
                        ContextCompat.getDrawable(this@AttendanceSheetActivity, R.color.student_names_column_background)
                    }
            }
            datesHeader.addView(dateHeader)
        }

        // Filter students by selected section
        val studentsToShow = DataStore.getStudents(selectedSection)
          if (studentsToShow.isEmpty()) {
              toast("No students found for section $selectedSection")
              return
          }

        val maleStudents = studentsToShow.filter { it.gender == "Male" }
            .sortedWith(compareBy({ it.lastName }, { it.firstName }))
        val femaleStudents = studentsToShow.filter { it.gender == "Female" }
            .sortedWith(compareBy({ it.lastName }, { it.firstName }))

        val startDate = Calendar.getInstance().apply{
            set(currentYear, currentMonth, 1,0,0,0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val endDate = Calendar.getInstance().apply{
            set(currentYear, currentMonth, daysInMonth,23,59,59)
            set(Calendar.MILLISECOND, 999)
        }.time

        val attendanceRecords = DataStore.getAttendanceRecords(selectedSection, startDate, endDate)

        var rowIndex = 0

        if (maleStudents.isNotEmpty()) {
            // Add header for student names column
            studentNamesContainer.addView(
                TextView(this).apply {
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

            // Add empty header row in attendance data to align with the student names header
            val maleHeaderRow = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    48.dpToPx()
                )
                orientation = LinearLayout.HORIZONTAL
                background = ContextCompat.getDrawable(this@AttendanceSheetActivity, R.color.header_background)
            }
            dates.forEach {
                maleHeaderRow.addView(TextView(this).apply {
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

        // Add Female Students section if there are any
        if (femaleStudents.isNotEmpty()) {
            // Add header for student names column
            studentNamesContainer.addView(
                TextView(this).apply {
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

            // Add empty header row in attendance data to align with the student names header
            val femaleHeaderRow = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    48.dpToPx()
                )
                orientation = LinearLayout.HORIZONTAL
                background = ContextCompat.getDrawable(this@AttendanceSheetActivity, R.color.header_background)
            }
            dates.forEach {
                femaleHeaderRow.addView(TextView(this).apply {
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

            // Add female students
            femaleStudents.forEach { student ->
                addStudentRow(student, dates, attendanceRecords, rowIndex)
                rowIndex++
            }
        }

        chosenDate?.let { date ->
            val dayIndex = dates.indexOfFirst { isSameDay(it, date) }
            if (dayIndex != -1) {
                scrollDates.post {
                    scrollDates.scrollTo(dayIndex * 61.dpToPx(), 0) // 60dp + 1dp margin
                }
            }
        }
    }

    private fun addStudentRow(student: Student, dates: List<Date>, attendanceRecords: List<com.example.markahanmobile.data.AttendanceRecord>, rowIndex: Int) {
        // Add student name (fixed column)
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

        // Add attendance row for this student
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

        // Add attendance status for each date
        dates.forEach { date ->
            val record = attendanceRecords.firstOrNull {
                it.studentId == student.studentID && isSameDay(it.date, date)
            }

            val status = when (record?.status) {
                "P" -> "P"
                "A" -> "A"
                "L" -> "L"
                else -> ""
            }

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

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply {
            time = date1
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val cal2 = Calendar.getInstance().apply {
            time = date2
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}