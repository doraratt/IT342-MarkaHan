package com.example.markahanmobile.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.R
import com.example.markahanmobile.data.Student

class StudentGradesAdapter(
    private var students: List<Student>,
    private val onViewGrades: (Student) -> Unit,
    private val onEditGrades: (Student) -> Unit
) : RecyclerView.Adapter<StudentGradesAdapter.StudentViewHolder>() {

    class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val studentName: TextView = view.findViewById(R.id.studentName)
        val studentRemarks: TextView = view.findViewById(R.id.studentRemarks)
        val viewGradesBtn: TextView = view.findViewById(R.id.viewGradesBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_grades, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        holder.studentName.text = "${student.lastName}, ${student.firstName}"
        holder.studentRemarks.text = student.remarks

        holder.studentRemarks.setTextColor(
            if (student.average >= 75)
                android.graphics.Color.GREEN
            else
                android.graphics.Color.RED
        )

        holder.viewGradesBtn.setOnClickListener {
            onViewGrades(student)
        }

        holder.itemView.setOnLongClickListener {
            onEditGrades(student)
            true
        }
    }

    override fun getItemCount() = students.size

    fun updateList(newList: List<Student>) {
        students = newList
        notifyDataSetChanged()
    }
}