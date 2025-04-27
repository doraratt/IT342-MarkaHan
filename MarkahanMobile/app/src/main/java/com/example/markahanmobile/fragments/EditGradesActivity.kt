package com.example.markahanmobile.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.markahanmobile.data.Student
import com.example.markahanmobile.data.SubjectGrade
import com.example.markahanmobile.utils.toast
import com.example.markahanmobile.R

class EditGradesActivity : AppCompatActivity() {

    private lateinit var student: Student
    private lateinit var filipinoGrade: EditText
    private lateinit var englishGrade: EditText
    private lateinit var mathGrade: EditText
    private lateinit var scienceGrade: EditText
    private lateinit var apGrade: EditText
    private lateinit var espGrade: EditText
    private lateinit var tleGrade: EditText
    private lateinit var mapehGrade: EditText
    private lateinit var computerGrade: EditText
    private lateinit var currentAverageText: TextView
    private lateinit var newAverageText: TextView
    private val TAG = "EditGradesActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_grades)

        student = intent.getParcelableExtra("student") ?: run {
            toast("Error: Student data not found")
            finish()
            return
        }
        Log.d(TAG, "Received student: ${student.studentID}, Current Grades: ${student.grades}")

        setupUI()
        setupButtons()
    }

    private fun setupUI() {
        val studentNameTextView = findViewById<TextView>(R.id.studentName)
        if(studentNameTextView != null){
            studentNameTextView.text = "${student.firstName} ${student.lastName}"
        } else {
            Log.e(TAG, "TextView with ID studentName not found in layout")
            toast("Error: Student name TextView not found")
        }

        val sectionTextView = findViewById<TextView>(R.id.section)
        if (sectionTextView != null) {
            sectionTextView.text = student.section
        } else {
            Log.e(TAG, "TextView with ID section not found in layout")
            toast("Error: Section TextView not found")
        }

        filipinoGrade = findViewById(R.id.inputFilipino) ?: run {
            Log.e(TAG, "EditText with ID inputFilipino not found in layout")
            toast("Error: Filipino grade input not found")
            return
        }
        englishGrade = findViewById(R.id.inputEnglish) ?: run {
            Log.e(TAG, "EditText with ID inputEnglish not found in layout")
            toast("Error: English grade input not found")
            return
        }
        mathGrade = findViewById(R.id.inputMathematics) ?: run {
            Log.e(TAG, "EditText with ID inputMathematics not found in layout")
            toast("Error: Math grade input not found")
            return
        }
        scienceGrade = findViewById(R.id.inputScience) ?: run {
            Log.e(TAG, "EditText with ID inputScience not found in layout")
            toast("Error: Science grade input not found")
            return
        }
        apGrade = findViewById(R.id.inputAP) ?: run {
            Log.e(TAG, "EditText with ID inputAP not found in layout")
            toast("Error: AP grade input not found")
            return
        }
        espGrade = findViewById(R.id.inputESP) ?: run {
            Log.e(TAG, "EditText with ID inputESP not found in layout")
            toast("Error: ESP grade input not found")
            return
        }
        tleGrade = findViewById(R.id.inputTLE) ?: run {
            Log.e(TAG, "EditText with ID inputTLE not found in layout")
            toast("Error: TLE grade input not found")
            return
        }
        mapehGrade = findViewById(R.id.inputMAPEH) ?: run {
            Log.e(TAG, "EditText with ID inputMAPEH not found in layout")
            toast("Error: MAPEH grade input not found")
            return
        }
        computerGrade = findViewById(R.id.inputComputer) ?: run {
            Log.e(TAG, "EditText with ID inputComputer not found in layout")
            toast("Error: Computer grade input not found")
            return
        }
        currentAverageText = findViewById(R.id.currentAverage) ?: run {
            Log.e(TAG, "TextView with ID currentAverage not found in layout")
            toast("Error: Current average TextView not found")
            return
        }
        newAverageText = findViewById(R.id.newAverage) ?: run {
            Log.e(TAG, "TextView with ID newAverage not found in layout")
            toast("Error: New average TextView not found")
            return
        }

        // Pre-fill grades if available
        student.grades["Filipino"]?.let { filipinoGrade.setText(it.grade.toString()) }
        student.grades["English"]?.let { englishGrade.setText(it.grade.toString()) }
        student.grades["Math"]?.let { mathGrade.setText(it.grade.toString()) }
        student.grades["Science"]?.let { scienceGrade.setText(it.grade.toString()) }
        student.grades["AP"]?.let { apGrade.setText(it.grade.toString()) }
        student.grades["ESP"]?.let { espGrade.setText(it.grade.toString()) }
        student.grades["TLE"]?.let { tleGrade.setText(it.grade.toString()) }
        student.grades["MAPEH"]?.let { mapehGrade.setText(it.grade.toString()) }
        student.grades["Computer"]?.let { computerGrade.setText(it.grade.toString()) }

        // Display current average
        currentAverageText.text = "Current Average: ${String.format("%.2f", student.average)}"

        // Add TextWatcher to update new average dynamically
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateNewAverage()
            }
        }
        filipinoGrade.addTextChangedListener(textWatcher)
        englishGrade.addTextChangedListener(textWatcher)
        mathGrade.addTextChangedListener(textWatcher)
        scienceGrade.addTextChangedListener(textWatcher)
        apGrade.addTextChangedListener(textWatcher)
        espGrade.addTextChangedListener(textWatcher)
        tleGrade.addTextChangedListener(textWatcher)
        mapehGrade.addTextChangedListener(textWatcher)
        computerGrade.addTextChangedListener(textWatcher)

        updateNewAverage()
    }

    private fun updateNewAverage() {
        val grades = mutableListOf<Double>()
        val gradeInputs = listOf(
            filipinoGrade, englishGrade, mathGrade, scienceGrade,
            apGrade, espGrade, tleGrade, mapehGrade, computerGrade
        )
        gradeInputs.forEach { editText ->
            val gradeText = editText.text.toString().trim()
            if (gradeText.isNotEmpty()) {
                try {
                    val grade = gradeText.toDouble()
                    if (grade in 0.0..100.0) {
                        grades.add(grade)
                    }
                } catch (e: NumberFormatException) {
                    // Ignore invalid grades for average calculation
                }
            }
        }
        val newAverage = if (grades.isNotEmpty()) grades.average() else 0.0
        newAverageText.text = "New Average: ${String.format("%.2f", newAverage)}"
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            submitGrades()
        }

        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun submitGrades() {
        val newGrades = student.grades.toMutableMap() // Preserve existing grades
        val gradesToValidate = listOf(
            "Filipino" to filipinoGrade,
            "English" to englishGrade,
            "Math" to mathGrade,
            "Science" to scienceGrade,
            "AP" to apGrade,
            "ESP" to espGrade,
            "TLE" to tleGrade,
            "MAPEH" to mapehGrade,
            "Computer" to computerGrade
        )

        var hasErrors = false
        for ((subject, editText) in gradesToValidate) {
            val gradeText = editText.text.toString().trim()
            if (gradeText.isNotEmpty()) {
                try {
                    val grade = gradeText.toDouble()
                    if (grade in 0.0..100.0) {
                        newGrades[subject] = SubjectGrade(subject, grade)
                    } else {
                        editText.error = "Grade must be between 0 and 100"
                        hasErrors = true
                    }
                } catch (e: NumberFormatException) {
                    editText.error = "Invalid grade format"
                    hasErrors = true
                }
            } else {
                newGrades.remove(subject) // Remove grade if input is cleared
            }
        }

        if (!hasErrors) {
            val average = if (newGrades.isNotEmpty()) newGrades.values.map { it.grade }.average() else 0.0
            val updatedStudent = student.copy(grades = newGrades)
            Log.d(TAG, "Submitting updated student: ${updatedStudent.studentID}, New Grades: ${updatedStudent.grades}, Remarks: ${updatedStudent.remarks}")

            AlertDialog.Builder(this)
                .setTitle("Confirm Grades")
                .setMessage("Are you sure you want to update grades for ${student.firstName} ${student.lastName}?\n\n" +
                        newGrades.entries.joinToString("\n") { "${it.key}: ${it.value.grade} (${it.value.remarks})" } +
                        "\n\nOverall Average: ${String.format("%.2f", average)} (${updatedStudent.remarks})")
                .setPositiveButton("Submit") { _, _ ->
                    val resultIntent = Intent().apply {
                        putExtra("updatedStudent", updatedStudent)
                    }
                    setResult(RESULT_OK, resultIntent)
                    toast("Grades submitted successfully")
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}