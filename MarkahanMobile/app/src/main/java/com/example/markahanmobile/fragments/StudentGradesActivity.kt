package com.example.markahanmobile.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.markahanmobile.R
import com.example.markahanmobile.data.Student
import com.example.markahanmobile.data.SubjectGrade
import com.example.markahanmobile.utils.toast

class StudentGradesActivity : AppCompatActivity() {

    private lateinit var currentStudent: Student

    companion object {
        const val EDIT_GRADES_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_grades)

        currentStudent = intent.getParcelableExtra("student") ?: run {
            toast("Student data not found")
            finish()
            return
        }

        setupStudentInfo()
        setupGradeRows()
        setupButtons()
    }

    private fun setupStudentInfo() {
        findViewById<TextView>(R.id.studentName).text =
            "${currentStudent.lastName}, ${currentStudent.firstName}"
        findViewById<TextView>(R.id.studentSection).text = currentStudent.section
        findViewById<TextView>(R.id.studentGradeLevel).text =
            "Grade ${currentStudent.gradeLevel}"
    }

    private fun setupGradeRows() {
        val subjectRows = mapOf(
            "FILIPINO" to R.id.rowFilipino,
            "ENGLISH" to R.id.rowEnglish,
            "MATHEMATICS" to R.id.rowMath,
            "SCIENCE" to R.id.rowScience,
            "AP" to R.id.rowAP,
            "ESP" to R.id.rowESP,
            "MAPEH" to R.id.rowMAPEH,
            "COMPUTER" to R.id.rowComputer,
            "TLE" to R.id.rowTLE
        )

        subjectRows.forEach { (subject, rowId) ->
            setGradeRow(rowId, subject, currentStudent.grades[subject])
        }

        updateAverageDisplay()
    }

    private fun setGradeRow(rowId: Int, subjectName: String, grade: SubjectGrade?) {
        val row = findViewById<View>(rowId)
        row.findViewById<TextView>(R.id.subjectName).text = subjectName

        if (grade != null) {
            row.findViewById<TextView>(R.id.subjectGrade).text = grade.grade.toInt().toString()
            row.findViewById<TextView>(R.id.subjectRemarks).text = grade.remarks
            row.findViewById<TextView>(R.id.subjectRemarks).setTextColor(
                if (grade.grade >= 75) Color.GREEN else Color.RED
            )
        } else {
            row.findViewById<TextView>(R.id.subjectGrade).text = "-"
            row.findViewById<TextView>(R.id.subjectRemarks).text = "N/A"
            row.findViewById<TextView>(R.id.subjectRemarks).setTextColor(Color.GRAY)
        }
    }

    private fun updateAverageDisplay() {
        findViewById<TextView>(R.id.txtAverageGrade).text = "%.2f".format(currentStudent.average)
        findViewById<TextView>(R.id.txtAverageRemarks).text = currentStudent.remarks
        findViewById<TextView>(R.id.txtAverageRemarks).setTextColor(
            if (currentStudent.average >= 75) Color.GREEN else Color.RED
        )
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnEditGrades).setOnClickListener {
            Intent(this, EditGradesActivity::class.java).apply {
                putExtra("student", currentStudent)
                startActivityForResult(this, EDIT_GRADES_REQUEST)
            }
        }

        findViewById<Button>(R.id.btnPrintGrades).setOnClickListener {
            generateGradeReport(currentStudent)
        }
    }

    private fun generateGradeReport(student: Student) {
        toast("Generating grade report for ${student.firstName} ${student.lastName}")
        // Implement actual PDF generation here
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_GRADES_REQUEST && resultCode == RESULT_OK) {
            val updatedStudent = data?.getParcelableExtra<Student>("updatedStudent")
            if (updatedStudent != null) {
                currentStudent = updatedStudent
                setupGradeRows()
                toast("Grades updated successfully")
            }
        }
    }
}