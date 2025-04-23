package com.example.markahanmobile.fragments

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.markahanmobile.data.Student
import com.example.markahanmobile.data.SubjectGrade
import com.example.markahanmobile.utils.toast
import com.example.markahanmobile.R
import com.google.android.material.textfield.TextInputEditText

class EditGradesActivity : AppCompatActivity() {

    private lateinit var currentStudent: Student
    private val inputFields = mutableMapOf<String, TextInputEditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_grades)

        currentStudent = intent.getParcelableExtra("student") ?: run {
            toast("Student data not found")
            finish()
            return
        }

        setupStudentInfo()
        setupInputFields()
        setupButtons()
        calculateNewAverage()
    }

    private fun setupStudentInfo() {
        findViewById<TextView>(R.id.studentName).text =
            "Editing Grades for ${currentStudent.lastName}, ${currentStudent.firstName}"

        findViewById<TextView>(R.id.currentAverage).text =
            "Current Average: %.2f".format(currentStudent.average)
    }

    private fun setupInputFields() {
        // Map of subject names to their input fields
        inputFields.apply {
            put("FILIPINO", findViewById(R.id.inputFilipino))
            put("ENGLISH", findViewById(R.id.inputEnglish))
            put("MATHEMATICS", findViewById(R.id.inputMathematics))
            put("SCIENCE", findViewById(R.id.inputScience))
            put("AP", findViewById(R.id.inputAP))
            put("ESP", findViewById(R.id.inputESP))
            put("MAPEH", findViewById(R.id.inputMAPEH))
            put("COMPUTER", findViewById(R.id.inputComputer))
            put("TLE", findViewById(R.id.inputTLE))
        }

        // Populate existing grades
        currentStudent.grades.forEach { (subject, grade) ->
            inputFields[subject]?.setText(grade.grade.toString())
        }

        // Add text change listeners
        inputFields.values.forEach { editText ->
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) calculateNewAverage()
            }
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            if (validateGrades()) {
                saveGradesAndFinish()
            }
        }
    }

    private fun calculateNewAverage() {
        val validGrades = inputFields.values.mapNotNull {
            it.text.toString().toDoubleOrNull()?.takeIf { grade -> grade in 0.0..100.0 }
        }

        findViewById<TextView>(R.id.newAverage).text = if (validGrades.isNotEmpty()) {
            val average = validGrades.average()
            "New Average: %.2f (${if (average >= 75) "PASSED" else "FAILED"})".format(average)
        } else {
            "Enter grades to calculate average"
        }
    }

    private fun validateGrades(): Boolean {
        var isValid = true

        inputFields.forEach { (subject, field) ->
            when (val grade = field.text.toString().toDoubleOrNull()) {
                null -> {
                    field.error = "Required"
                    isValid = false
                }
                !in 0.0..100.0 -> {
                    field.error = "Must be 0-100"
                    isValid = false
                }
                else -> field.error = null
            }
        }

        return isValid
    }

    private fun saveGradesAndFinish() {
        val updatedGrades = inputFields.mapNotNull { (subject, field) ->
            field.text.toString().toDoubleOrNull()?.let { grade ->
                SubjectGrade(subject, grade)
            }
        }.associateBy { it.subjectName }

        val updatedStudent = currentStudent.copy(grades = updatedGrades)

        setResult(RESULT_OK, Intent().putExtra("updatedStudent", updatedStudent))
        finish()
    }
}