package com.example.markahanmobile.helper

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.R
import com.example.markahanmobile.data.Student

class AttendanceAdapter(
    private val onStatusChanged: (Int, String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_STUDENT = 1
    }

    private val items: MutableList<Any> = mutableListOf() // List can contain Students or Strings (headers)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attendance, parent, false)
            AttendanceViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) VIEW_TYPE_HEADER else VIEW_TYPE_STUDENT
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val header = items[position] as String
                holder.headerTitle.text = header
            }
            is AttendanceViewHolder -> {
                val student = items[position] as Student
                val studentPosition = items.filterIndexed { index, item -> index <= position && item is Student }.count() - 1
                Log.d("AttendanceAdapter", "Binding student at position $position (studentPosition $studentPosition): ${student.firstName} ${student.lastName}, Section=${student.section}")
                holder.nameTextView.text = "${student.lastName}, ${student.firstName}"

                when (student.attendanceStatus) {
                    "Present" -> holder.radioPresent.isChecked = true
                    "Late" -> holder.radioLate.isChecked = true
                    "Absent" -> holder.radioAbsent.isChecked = true
                    else -> {
                        holder.radioPresent.isChecked = false
                        holder.radioLate.isChecked = false
                        holder.radioAbsent.isChecked = false
                    }
                }

                holder.radioPresent.setOnClickListener {
                    if (holder.radioPresent.isChecked) {
                        holder.radioLate.isChecked = false
                        holder.radioAbsent.isChecked = false
                        onStatusChanged(studentPosition, "Present")
                    }
                }
                holder.radioLate.setOnClickListener {
                    if (holder.radioLate.isChecked) {
                        holder.radioPresent.isChecked = false
                        holder.radioAbsent.isChecked = false
                        onStatusChanged(studentPosition, "Late")
                    }
                }
                holder.radioAbsent.setOnClickListener {
                    if (holder.radioAbsent.isChecked) {
                        holder.radioPresent.isChecked = false
                        holder.radioLate.isChecked = false
                        onStatusChanged(studentPosition, "Absent")
                    }
                }
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateList(newStudents: List<Student>) {
        items.clear()

        // Group students by gender and sort alphabetically by last name, then first name
        val maleStudents = newStudents.filter { it.gender == "Male" }
            .sortedWith(compareBy({ it.lastName }, { it.firstName }))
        val femaleStudents = newStudents.filter { it.gender == "Female" }
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

        Log.d("AttendanceAdapter", "Updated list with ${newStudents.size} students: ${newStudents.map { "${it.firstName} ${it.lastName}, Section=${it.section}" }}")
        notifyDataSetChanged()
    }

    class AttendanceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.studentName)
        val radioPresent: CheckBox = view.findViewById(R.id.radioPresent)
        val radioLate: CheckBox = view.findViewById(R.id.radioLate)
        val radioAbsent: CheckBox = view.findViewById(R.id.radioAbsent)
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerTitle: TextView = view.findViewById(R.id.headerTitle)
    }
}