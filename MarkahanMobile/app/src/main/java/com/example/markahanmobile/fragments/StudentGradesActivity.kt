package com.example.markahanmobile.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.markahanmobile.R
import com.example.markahanmobile.data.DataStore
import com.example.markahanmobile.data.Student
import com.example.markahanmobile.utils.toast

class StudentGradesActivity : AppCompatActivity() {

    private lateinit var student: Student
    private val TAG = "StudentGradesActivity"
    private val EDIT_GRADES_REQUEST = 1002
    private val isEditMode: Boolean
        get() = intent.getBooleanExtra("editMode", false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_grades)

        try {
            student = intent.getParcelableExtra("student") ?: run {
                Log.e(TAG, "Student data not found in Intent")
                toast("Error: Student data not found")
                finish()
                return
            }

            val latestStudent = DataStore.getStudents(includeArchived = true).find { it.studentID == student.studentID }
            if (latestStudent == null) {
                Log.e(TAG, "Student not found in DataStore: ${student.studentID}")
                toast("Error: Student not found in DataStore")
                finish()
                return
            }
            student = latestStudent
            Log.d(TAG, "Fetched student: ${student.studentID}, Grades: ${student.grades}, Remarks: ${student.remarks}")

            setupUI()
            setupButtons()

            if (isEditMode) {
                launchEditGradesActivity()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            toast("Failed to load grades: ${e.message}")
            finish()
        }
    }

    private fun setupUI() {
        try {
            findViewById<TextView>(R.id.studentName)?.text =
                ("${student.firstName} ${student.lastName}"
                    ?: Log.e(TAG, "studentName TextView not found")).toString()

            Log.d(TAG, "Setting student info - Section: ${student.section}, GradeLevel: ${student.gradeLevel}")
            val sectionTextView = findViewById<TextView>(R.id.studentSection)
            val gradeLevelTextView = findViewById<TextView>(R.id.studentGradeLevel)

            if (sectionTextView != null) {
                sectionTextView.text = student.section.takeIf { !it.isNullOrEmpty() } ?: "N/A"
            } else {
                Log.e(TAG, "studentSection TextView not found")
            }

            if (gradeLevelTextView != null) {
                gradeLevelTextView.text = student.gradeLevel.takeIf { !it.isNullOrEmpty() } ?: "N/A"
            } else {
                Log.e(TAG, "studentGradeLevel TextView not found")
            }

            findViewById<TextView>(R.id.txtAverageGrade)?.text =
                (String.format("%.2f", student.average)
                    ?: Log.e(TAG, "txtAverageGrade TextView not found")).toString()
            findViewById<TextView>(R.id.txtAverageRemarks)?.let {
                it.text = if (student.average >= 75) "PASSED" else "FAILED"
                it.setTextColor(
                    if (student.average >= 75) android.graphics.Color.GREEN
                    else android.graphics.Color.RED
                )
            } ?: Log.e(TAG, "txtAverageRemarks TextView not found")

            bindGradeRow(R.id.rowFilipino, "Filipino")
            bindGradeRow(R.id.rowEnglish, "English")
            bindGradeRow(R.id.rowMath, "Math")
            bindGradeRow(R.id.rowScience, "Science")
            bindGradeRow(R.id.rowAP, "AP")
            bindGradeRow(R.id.rowESP, "ESP")
            bindGradeRow(R.id.rowMAPEH, "MAPEH")
            bindGradeRow(R.id.rowComputer, "Computer")
            bindGradeRow(R.id.rowTLE, "TLE")
        } catch (e: Exception) {
            Log.e(TAG, "Error in setupUI", e)
            toast("Failed to set up UI: ${e.message}")
        }
    }

    private fun bindGradeRow(rowId: Int, subject: String) {
        try {
            val row = findViewById<View>(rowId) ?: run {
                Log.e(TAG, "Grade row not found for ID: $rowId")
                return
            }
            row.findViewById<TextView>(R.id.subjectName)?.text = (subject ?: Log.e(TAG, "subjectName TextView not found in grade row $rowId")).toString()
            val grade = student.grades[subject]
            row.findViewById<TextView>(R.id.subjectGrade)?.let {
                it.text = grade?.grade?.let { g -> String.format("%.2f", g) } ?: "-"
            } ?: Log.e(TAG, "subjectGrade TextView not found in grade row $rowId")
            row.findViewById<TextView>(R.id.subjectRemarks)?.let {
                it.text = grade?.remarks ?: "-"
                it.setTextColor(
                    if (grade != null && grade.grade >= 75) android.graphics.Color.GREEN else android.graphics.Color.RED
                )
            } ?: Log.e(TAG, "subjectRemarks TextView not found in grade row $rowId")
        } catch (e: Exception) {
            Log.e(TAG, "Error binding grade row for subject: $subject", e)
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnEditGrades)?.setOnClickListener {
           launchEditGradesActivity()
        }

        findViewById<Button>(R.id.btnPrintGrades)?.setOnClickListener {
            Log.d(TAG, "Printing grades for ${student.studentID}")
            toast("Printing grades for ${student.firstName} ${student.lastName}")
            // Implement actual print functionality if needed
        }
    }

    private fun launchEditGradesActivity() {
        Log.d(TAG, "Launching EditGradesActivity for ${student.studentID}")
        val intent = Intent(this, EditGradesActivity::class.java).apply {
            putExtra("student", student)
        }
        startActivityForResult(intent, EDIT_GRADES_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_GRADES_REQUEST) {
            when (resultCode) {
                RESULT_OK -> {
                    data?.getParcelableExtra<Student>("updatedStudent")?.let { updatedStudent ->
                        try {
                            Log.d(TAG, "Received updated student: ${updatedStudent.studentID}, Grades: ${updatedStudent.grades}, Remarks: ${updatedStudent.remarks}")
                            DataStore.updateStudent(updatedStudent)
                            student = updatedStudent
                            setupUI()
                            toast("Grades updated successfully")

                            if (isEditMode) {
                                val resultIntent = Intent().apply {
                                    putExtra("updatedStudent", updatedStudent)
                                }
                                setResult(RESULT_OK, resultIntent)
                                finish()
                            }
                        } catch (e: IllegalArgumentException) {
                            Log.e(TAG, "Error updating student", e)
                            toast(e.message ?: "Failed to update grades")
                        }
                    } ?: Log.e(TAG, "No updated student received in onActivityResult")
                }
                RESULT_CANCELED -> {
                    if (isEditMode) {
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                }
            }
        }
    }
}
