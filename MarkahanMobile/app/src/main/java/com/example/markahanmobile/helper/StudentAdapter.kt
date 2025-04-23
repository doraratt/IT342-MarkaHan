package com.example.markahanmobile.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.R
import com.example.markahanmobile.data.Student

class StudentAdapter(
    private var students: List<Student>,
    private val onEdit: (Student) -> Unit,
    private val onArchive: (Student) -> Unit,
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        holder.fullName.text = "${student.firstName} ${student.lastName}"
        holder.section.text = student.section
        holder.gradeLevel.text = student.gradeLevel
        holder.editButton.setOnClickListener { onEdit(student) }
        holder.archiveButton.setOnClickListener { onArchive(student) }
    }

    override fun getItemCount(): Int = students.size

    fun updateList(newStudents: List<Student>) {
        students = newStudents
        notifyDataSetChanged()
    }

    class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fullName: TextView = view.findViewById(R.id.txtStudentName)
        val section: TextView = view.findViewById(R.id.txtStudentSection)
        val gradeLevel: TextView = view.findViewById(R.id.txtGradeLevel)
        val editButton: ImageView = view.findViewById(R.id.btnEditStudent)
        val archiveButton: ImageView = view.findViewById(R.id.btnArchiveStudent)
    }

    fun filter(query: String) {
        val filteredList = students.filter { student ->
            student.firstName.contains(query, ignoreCase = true) ||
                    student.lastName.contains(query, ignoreCase = true) ||
                    student.section.contains(query, ignoreCase = true)
        }
        updateList(filteredList)
    }
}