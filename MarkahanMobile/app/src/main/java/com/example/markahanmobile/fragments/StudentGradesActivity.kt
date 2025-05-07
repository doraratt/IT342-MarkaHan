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
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.markahanmobile.R
import com.example.markahanmobile.data.DataStore
import com.example.markahanmobile.data.Student
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StudentGradesActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
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
        setupToolbar()
        loadData()
        setupListeners()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
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

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
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

        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(Dispatchers.IO) {
                    DataStore.syncGrades(userId) { success ->
                        if (success) {
                            Log.d(TAG, "loadData: Grades synced for userId=$userId")
                        } else {
                            Log.e(TAG, "loadData: Failed to sync grades")
                        }
                    }
                }
                // Fetch the latest grade for the student
                val grade = DataStore.getGradeByStudent(student.studentId)
                if (grade == null) {
                    Log.w(TAG, "loadData: No grade found for studentId=${student.studentId}")
                } else {
                    Log.d(TAG, "loadData: Fetched grade for studentId=${student.studentId}, GradeId=${grade.gradeId}, FinalGrade=${grade.finalGrade}")
                }
                student = student.copy(grade = grade)
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    scrollView.visibility = View.VISIBLE
                    setupUI()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    scrollView.visibility = View.VISIBLE
                    Log.e(TAG, "loadData: Error syncing grades: ${e.message}", e)
                    Toast.makeText(this@StudentGradesActivity, "Error loading grades", Toast.LENGTH_SHORT).show()
                    setupUI()
                }
            }
        }
    }

    private fun setupUI() {
        // Always display learning area rows with subject labels
        bindGradeRow(R.id.rowFilipino, "Filipino")
        bindGradeRow(R.id.rowEnglish, "English")
        bindGradeRow(R.id.rowMath, "Math")
        bindGradeRow(R.id.rowScience, "Science")
        bindGradeRow(R.id.rowAP, "AP")
        bindGradeRow(R.id.rowESP, "ESP")
        bindGradeRow(R.id.rowMAPEH, "MAPEH")
        bindGradeRow(R.id.rowComputer, "Computer")

        student.grade?.let { grade ->
            Log.d(TAG, "setupUI: Displaying grades for studentId=${student.studentId}, GradeId=${grade.gradeId}, FinalGrade=${grade.finalGrade}")
            updateGradeRow(R.id.rowFilipino, grade.filipino, grade.subjectGrades["Filipino"]?.remarks ?: "")
            updateGradeRow(R.id.rowEnglish, grade.english, grade.subjectGrades["English"]?.remarks ?: "")
            updateGradeRow(R.id.rowMath, grade.mathematics, grade.subjectGrades["Math"]?.remarks ?: "")
            updateGradeRow(R.id.rowScience, grade.science, grade.subjectGrades["Science"]?.remarks ?: "")
            updateGradeRow(R.id.rowAP, grade.ap, grade.subjectGrades["AP"]?.remarks ?: "")
            updateGradeRow(R.id.rowESP, grade.esp, grade.subjectGrades["ESP"]?.remarks ?: "")
            updateGradeRow(R.id.rowMAPEH, grade.mapeh, grade.subjectGrades["MAPEH"]?.remarks ?: "")
            updateGradeRow(R.id.rowComputer, grade.computer, grade.subjectGrades["Computer"]?.remarks ?: "")

            averageGradeText.text = if (grade.finalGrade > 0) String.format("%.2f", grade.finalGrade) else "-"
            averageRemarksText.text = grade.remarks.ifEmpty { "-" }
            averageRemarksText.setTextColor(
                ContextCompat.getColor(
                    this,
                    when (grade.remarks) {
                        "PASSED" -> R.color.remarks_passed
                        "FAILED" -> R.color.remarks_failed
                        else -> R.color.remarks_none // Covers "-" or empty remarks
                    }
                )
            )
        } ?: run {
            Log.w(TAG, "setupUI: No grades available for studentId=${student.studentId}")
            averageGradeText.text = "-"
            averageRemarksText.text = "-"
            averageRemarksText.setTextColor(ContextCompat.getColor(this, R.color.remarks_none))
        }
    }

    private fun bindGradeRow(rowId: Int, subject: String) {
        val row = findViewById<LinearLayout>(rowId)
        val subjectText = row.findViewById<TextView>(R.id.txtSubject)
        val gradeText = row.findViewById<TextView>(R.id.txtGrade)
        val remarksText = row.findViewById<TextView>(R.id.txtRemarks)

        subjectText.text = subject
        gradeText.text = "-"
        remarksText.text = "-"
        remarksText.setTextColor(ContextCompat.getColor(this, R.color.remarks_none))
        row.visibility = View.VISIBLE
    }

    private fun updateGradeRow(rowId: Int, grade: Double, remarks: String) {
        val row = findViewById<LinearLayout>(rowId)
        val gradeText = row.findViewById<TextView>(R.id.txtGrade)
        val remarksText = row.findViewById<TextView>(R.id.txtRemarks)

        gradeText.text = if (grade > 0) String.format("%.2f", grade) else "-"
        remarksText.text = remarks.ifEmpty { "-" }
        remarksText.setTextColor(
            ContextCompat.getColor(
                this,
                when (remarks) {
                    "PASSED" -> R.color.remarks_passed
                    "FAILED" -> R.color.remarks_failed
                    else -> R.color.remarks_none // Covers "-" or empty remarks
                }
            )
        )
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