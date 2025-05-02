package com.example.markahanmobile.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.markahanmobile.R
import com.example.markahanmobile.data.DataStore
import com.example.markahanmobile.data.Student

class StudentGradesActivity : AppCompatActivity() {

    private lateinit var studentNameText: TextView
    private lateinit var sectionText: TextView
    private lateinit var gradeLevelText: TextView
    private lateinit var averageGradeText: TextView
    private lateinit var averageRemarksText: TextView
    private lateinit var editGradesButton: Button
    private lateinit var printGradesButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollView: ScrollView
    private lateinit var student: Student
    private val EDIT_GRADES_REQUEST = 1001
    private val TAG = "StudentGradesActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_grades)

        initializeViews()
        loadData()
        setupListeners()
    }

    private fun initializeViews() {
        studentNameText = findViewById(R.id.studentName)
        sectionText = findViewById(R.id.studentSection)
        gradeLevelText = findViewById(R.id.studentGradeLevel)
        averageGradeText = findViewById(R.id.txtAverageGrade)
        averageRemarksText = findViewById(R.id.txtAverageRemarks)
        editGradesButton = findViewById(R.id.btnEditGrades)
        printGradesButton = findViewById(R.id.btnPrintGrades)
        progressBar = findViewById(R.id.progressBar)
        scrollView = findViewById(R.id.scrollView)
    }

    private fun loadData() {
        student = intent.getParcelableExtra("student") ?: return finish()

        studentNameText.text = "${student.lastName}, ${student.firstName}"
        sectionText.text = student.section
        gradeLevelText.text = student.gradeLevel

        progressBar.visibility = View.VISIBLE
        scrollView.visibility = View.GONE

        val userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("userId", -1)
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        DataStore.syncGrades(userId) { success ->
            progressBar.visibility = View.GONE
            scrollView.visibility = View.VISIBLE
            if (success) {
                val grade = DataStore.getGradeByStudent(student.studentId)
                student = student.copy(grade = grade)
                setupUI()
            } else {
                Toast.makeText(this, "Error loading grades", Toast.LENGTH_SHORT).show()
                setupUI()
            }
        }
    }

    private fun setupUI() {
        student.grade?.let { grade ->
            bindGradeRow(R.id.rowFilipino, "Filipino")
            bindGradeRow(R.id.rowEnglish, "English")
            bindGradeRow(R.id.rowMath, "Math")
            bindGradeRow(R.id.rowScience, "Science")
            bindGradeRow(R.id.rowAP, "AP")
            bindGradeRow(R.id.rowESP, "ESP")
            bindGradeRow(R.id.rowMAPEH, "MAPEH")
            bindGradeRow(R.id.rowComputer, "Computer")

            averageGradeText.text = if (grade.finalGrade > 0) String.format("%.2f", grade.finalGrade) else "-"
            averageRemarksText.text = grade.remarks.ifEmpty { "-" }
            averageRemarksText.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (grade.finalGrade >= 75) R.color.green else R.color.red
                )
            )
        } ?: run {
            Toast.makeText(this, "No grades available for this student", Toast.LENGTH_SHORT).show()
            arrayOf(
                R.id.rowFilipino, R.id.rowEnglish, R.id.rowMath, R.id.rowScience,
                R.id.rowAP, R.id.rowESP, R.id.rowMAPEH, R.id.rowComputer
            ).forEach { rowId ->
                findViewById<LinearLayout>(rowId)?.visibility = View.GONE
            }
            averageGradeText.text = "-"
            averageRemarksText.text = "-"
            averageRemarksText.setTextColor(ContextCompat.getColor(this, R.color.red))
        }
    }

    private fun bindGradeRow(rowId: Int, subject: String) {
        val row = findViewById<LinearLayout>(rowId)
        val subjectText = row.findViewById<TextView>(R.id.txtSubject)
        val gradeText = row.findViewById<TextView>(R.id.txtGrade)
        val remarksText = row.findViewById<TextView>(R.id.txtRemarks)

        student.grade?.subjectGrades?.get(subject)?.let { subjectGrade ->
            subjectText.text = subject
            gradeText.text = if (subjectGrade.grade > 0) String.format("%.2f", subjectGrade.grade) else "-"
            remarksText.text = subjectGrade.remarks.ifEmpty { "-" }
            remarksText.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (subjectGrade.grade >= 75) R.color.green else R.color.red
                )
            )
        } ?: run {
            subjectText.text = subject
            gradeText.text = "-"
            remarksText.text = "-"
            remarksText.setTextColor(ContextCompat.getColor(this, R.color.red))
        }
    }

    private fun setupListeners() {
        editGradesButton.setOnClickListener {
            try {
                val intent = Intent(this, EditGradesActivity::class.java).apply {
                    putExtra("student", student as android.os.Parcelable)
                }
                startActivityForResult(intent, EDIT_GRADES_REQUEST)
            } catch (e: Exception) {
                Log.e(TAG, "Error opening EditGradesActivity", e)
                Toast.makeText(this, "Error opening grade editor", Toast.LENGTH_SHORT).show()
            }
        }

        printGradesButton.setOnClickListener {
            Toast.makeText(this, "Print functionality not implemented", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_GRADES_REQUEST && resultCode == RESULT_OK) {
            data?.getParcelableExtra<Student>("updatedStudent")?.let { updatedStudent ->
                student = updatedStudent
                setupUI()
                val resultIntent = Intent()
                resultIntent.putExtra("updatedStudent", student as android.os.Parcelable)
                setResult(RESULT_OK, resultIntent)
                Toast.makeText(this, "Grades updated successfully", Toast.LENGTH_SHORT).show()
            } ?: run {
                Log.e(TAG, "No updated student received in onActivityResult")
                Toast.makeText(this, "Failed to update grades", Toast.LENGTH_SHORT).show()
            }
        }
    }
}