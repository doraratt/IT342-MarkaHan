package com.example.markahanmobile.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.markahanmobile.R
import com.example.markahanmobile.data.DataStore
import com.example.markahanmobile.data.Grade
import com.example.markahanmobile.data.Student
import com.example.markahanmobile.data.User

class EditGradesActivity : AppCompatActivity() {

    private lateinit var studentNameText: TextView
    private lateinit var sectionText: TextView
    private lateinit var currentAverageText: TextView
    private lateinit var newAverageText: TextView
    private lateinit var filipinoGrade: EditText
    private lateinit var englishGrade: EditText
    private lateinit var mathGrade: EditText
    private lateinit var scienceGrade: EditText
    private lateinit var apGrade: EditText
    private lateinit var espGrade: EditText
    private lateinit var mapehGrade: EditText
    private lateinit var computerGrade: EditText
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    private lateinit var student: Student
    private val gradeInputs = mutableMapOf<String, EditText>()
    private val TAG = "EditGradesActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_grades)

        initializeViews()
        loadData()
        setupListeners()
    }

    private fun initializeViews() {
        studentNameText = findViewById(R.id.studentName)
        sectionText = findViewById(R.id.section)
        currentAverageText = findViewById(R.id.currentAverage)
        newAverageText = findViewById(R.id.newAverage)
        filipinoGrade = findViewById(R.id.inputFilipino)
        englishGrade = findViewById(R.id.inputEnglish)
        mathGrade = findViewById(R.id.inputMathematics)
        scienceGrade = findViewById(R.id.inputScience)
        apGrade = findViewById(R.id.inputAP)
        espGrade = findViewById(R.id.inputESP)
        mapehGrade = findViewById(R.id.inputMAPEH)
        computerGrade = findViewById(R.id.inputComputer)
        cancelButton = findViewById(R.id.btnCancel)
        saveButton = findViewById(R.id.btnSave)

        gradeInputs["Filipino"] = filipinoGrade
        gradeInputs["English"] = englishGrade
        gradeInputs["Math"] = mathGrade
        gradeInputs["Science"] = scienceGrade
        gradeInputs["AP"] = apGrade
        gradeInputs["ESP"] = espGrade
        gradeInputs["MAPEH"] = mapehGrade
        gradeInputs["Computer"] = computerGrade
    }

    private fun loadData() {
        student = intent.getParcelableExtra("student") ?: return finish()

        studentNameText.text = "${student.lastName}, ${student.firstName}"
        sectionText.text = "${student.section} | ${student.gradeLevel}"
        val finalGrade = student.grade?.finalGrade
        currentAverageText.text = if (finalGrade != null && finalGrade > 0) {
            "Current Average: ${String.format("%.2f", finalGrade)}"
        } else {
            "Current Average: Not yet graded"
        }

        student.grade?.let { grade ->
            filipinoGrade.setText(grade.filipino.takeIf { it > 0 }?.toString() ?: "")
            englishGrade.setText(grade.english.takeIf { it > 0 }?.toString() ?: "")
            mathGrade.setText(grade.mathematics.takeIf { it > 0 }?.toString() ?: "")
            scienceGrade.setText(grade.science.takeIf { it > 0 }?.toString() ?: "")
            apGrade.setText(grade.ap.takeIf { it > 0 }?.toString() ?: "")
            espGrade.setText(grade.esp.takeIf { it > 0 }?.toString() ?: "")
            mapehGrade.setText(grade.mapeh.takeIf { it > 0 }?.toString() ?: "")
            computerGrade.setText(grade.computer.takeIf { it > 0 }?.toString() ?: "")
        }

        updateNewAverage()
    }

    private fun setupListeners() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateNewAverage()
            }
        }

        gradeInputs.values.forEach { it.addTextChangedListener(textWatcher) }

        cancelButton.setOnClickListener { finish() }

        saveButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm Submission")
                .setMessage("Are you sure you want to submit these grades?")
                .setPositiveButton("Submit") { _, _ -> submitGrades() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun updateNewAverage() {
        val grades = listOf(
            filipinoGrade.text.toString().toDoubleOrNull() ?: 0.0,
            englishGrade.text.toString().toDoubleOrNull() ?: 0.0,
            mathGrade.text.toString().toDoubleOrNull() ?: 0.0,
            scienceGrade.text.toString().toDoubleOrNull() ?: 0.0,
            apGrade.text.toString().toDoubleOrNull() ?: 0.0,
            espGrade.text.toString().toDoubleOrNull() ?: 0.0,
            mapehGrade.text.toString().toDoubleOrNull() ?: 0.0,
            computerGrade.text.toString().toDoubleOrNull() ?: 0.0
        )
        val newAverage = grades.average()
        newAverageText.text = "New Average: ${String.format("%.2f", newAverage)}"
    }

    private fun submitGrades() {
        val newGrades = mutableMapOf<String, Double>()
        var hasValidGrade = false

        gradeInputs.forEach { (subject, editText) ->
            val grade = editText.text.toString().toDoubleOrNull()
            if (grade != null) {
                if (grade < 0 || grade > 100) {
                    Toast.makeText(this, "$subject grade must be between 0 and 100", Toast.LENGTH_SHORT).show()
                    return
                }
                newGrades[subject] = grade
                hasValidGrade = true
            } else if (editText.text.isNotEmpty()) {
                Toast.makeText(this, "Invalid $subject grade", Toast.LENGTH_SHORT).show()
                return
            }
        }

        if (!hasValidGrade) {
            Toast.makeText(this, "Please enter at least one valid grade", Toast.LENGTH_SHORT).show()
            return
        }

        val average = listOf(
            newGrades["Filipino"] ?: 0.0,
            newGrades["English"] ?: 0.0,
            newGrades["Math"] ?: 0.0,
            newGrades["Science"] ?: 0.0,
            newGrades["AP"] ?: 0.0,
            newGrades["ESP"] ?: 0.0,
            newGrades["MAPEH"] ?: 0.0,
            newGrades["Computer"] ?: 0.0
        ).average()

        val remarks = if (average >= 75) "PASSED" else "FAILED"

        val user = DataStore.getLoggedInUser() ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val updatedGrade = Grade(
            gradeId = student.grade?.gradeId ?: 0,
            studentId = student.studentId,
            userId = user.userId,
            student = student,
            user = User(userId = user.userId),
            filipino = newGrades["Filipino"] ?: 0.0,
            english = newGrades["English"] ?: 0.0,
            mathematics = newGrades["Math"] ?: 0.0,
            science = newGrades["Science"] ?: 0.0,
            ap = newGrades["AP"] ?: 0.0,
            esp = newGrades["ESP"] ?: 0.0,
            mapeh = newGrades["MAPEH"] ?: 0.0,
            computer = newGrades["Computer"] ?: 0.0,
            finalGrade = average,
            remarks = remarks
        )

        val updatedStudent = student.copy(grade = updatedGrade)

        DataStore.updateGrade(updatedGrade) { success ->
            if (success) {
                val resultIntent = Intent()
                resultIntent.putExtra("updatedStudent", updatedStudent as android.os.Parcelable)
                setResult(RESULT_OK, resultIntent)
                Toast.makeText(this, "Grades submitted successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to submit grades", Toast.LENGTH_SHORT).show()
            }
        }
    }
}