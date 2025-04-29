package com.example.markahanmobile.helper

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.R
import com.example.markahanmobile.data.Student

class StudentGradesAdapter(
    private val onViewGrades: (Student) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_STUDENT = 1
    }

    private val items: MutableList<Any> = mutableListOf() // List can contain Students or Strings (headers)

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) VIEW_TYPE_HEADER else VIEW_TYPE_STUDENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_student_grades, parent, false)
            StudentViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val header = items[position] as String
                holder.headerTitle.text = header
            }
            is StudentViewHolder -> {
                val student = items[position] as Student
                Log.d("StudentGradesAdapter", "Binding student at position $position: ${student.firstName} ${student.lastName}, Section=${student.section}")
                holder.studentName.text = "${student.lastName}, ${student.firstName}"
                val remarks = student.grade?.remarks ?: "No Remarks"
                holder.studentRemarks.text = remarks
                holder.studentRemarks.setTextColor(
                    if (student.grade?.finalGrade ?: 0.0 >= 75)
                        android.graphics.Color.GREEN
                    else
                        android.graphics.Color.RED
                )
                holder.viewGradesBtn.setOnClickListener {
                    onViewGrades(student)
                }
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateList(newList: List<Student>) {
        items.clear()

        // Group students by gender and sort alphabetically by last name, then first name
        val maleStudents = newList.filter { it.gender == "Male" }
            .sortedWith(compareBy({ it.lastName }, { it.firstName }))
        val femaleStudents = newList.filter { it.gender == "Female" }
            .sortedWith(compareBy({ it.lastName }, { it.firstName }))

        // Add Male Students section if there are any
        if (maleStudents.isNotEmpty()) {
            items.add("Male Students")
            items.addAll(maleStudents)
        }

        // Add Female Students section if there are any
        if (femaleStudents.isNotEmpty()) {
            items.add("Female Students")
            items.addAll(femaleStudents)
        }

        Log.d("StudentGradesAdapter", "Updated list with ${newList.size} students: ${newList.map { "${it.firstName} ${it.lastName}, Remarks: ${it.grade?.remarks}" }}")
        notifyDataSetChanged()
    }

    class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val studentName: TextView = view.findViewById(R.id.studentName)
        val studentRemarks: TextView = view.findViewById(R.id.studentRemarks)
        val viewGradesBtn: TextView = view.findViewById(R.id.viewGradesBtn)
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerTitle: TextView = view.findViewById(R.id.headerTitle)
    }
}