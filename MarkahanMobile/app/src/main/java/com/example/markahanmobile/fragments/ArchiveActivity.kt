package com.example.markahanmobile.fragments

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.R
import com.example.markahanmobile.data.DataStore
import com.example.markahanmobile.data.Student
import com.example.markahanmobile.helper.StudentAdapter
import retrofit2.Callback

class ArchiveActivity : AppCompatActivity() {

    private lateinit var studentAdapter: StudentAdapter
    private val studentList = mutableListOf<Student>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_archive)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        loadArchivedStudents()
    }

    private fun setupRecyclerView() {
        val studentRecyclerView = findViewById<RecyclerView>(R.id.archivedStudentRecyclerView)
        studentRecyclerView.layoutManager = LinearLayoutManager(this)
        studentAdapter = StudentAdapter(studentList, ::editStudent, ::unarchiveStudent, ::deleteStudent, showArchived = true)
        studentRecyclerView.adapter = studentAdapter
    }

    private fun loadArchivedStudents() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", -1)

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        DataStore.syncStudents(userId, includeArchived = true) { success ->
            if (success) {
                studentList.clear()
                val archivedStudents = DataStore.getStudents(includeArchived = true).filter { it.isArchived }
                if (archivedStudents.isEmpty()) {
                    Toast.makeText(this, "No archived students found", Toast.LENGTH_SHORT).show()
                }
                studentList.addAll(archivedStudents)
                studentAdapter.updateList(studentList)
            } else {
                Toast.makeText(this, "Error loading archived students", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun editStudent(student: Student) {
        Toast.makeText(this, "Editing archived students is not supported", Toast.LENGTH_SHORT).show()
    }

    private fun unarchiveStudent(student: Student) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Unarchive")
            .setMessage("Are you sure you want to unarchive this student?")
            .setPositiveButton("Yes") { _, _ ->
                DataStore.archiveStudent(student.studentId, false) { success ->
                    if (success) {
                        studentList.remove(student)
                        studentAdapter.updateList(studentList)
                        Toast.makeText(this, "Student unarchived successfully", Toast.LENGTH_SHORT).show()
                        if (studentList.isEmpty()) {
                            Toast.makeText(this, "No archived students remaining", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Failed to unarchive student", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteStudent(student: Student) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to permanently delete this student?")
            .setPositiveButton("Yes") { _, _ ->
                DataStore.deleteStudent(student.studentId) { success ->
                    if (success) {
                        studentList.remove(student)
                        studentAdapter.updateList(studentList)
                        Toast.makeText(this, "Student deleted successfully", Toast.LENGTH_SHORT).show()
                        if (studentList.isEmpty()) {
                            Toast.makeText(this, "No archived students remaining", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Failed to delete student", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}