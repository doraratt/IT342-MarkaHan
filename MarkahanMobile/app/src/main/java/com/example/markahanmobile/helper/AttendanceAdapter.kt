package com.example.markahanmobile.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.R
import com.example.markahanmobile.data.Student

class AttendanceAdapter(
    private val students: List<Student>,
    private val onAttendanceChanged: (Int, String) -> Unit
) : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    class AttendanceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val studentName: TextView = view.findViewById(R.id.txtStudentName)
        val cbPresent: CheckBox = view.findViewById(R.id.cbPresent)
        val cbLate: CheckBox = view.findViewById(R.id.cbLate)
        val cbAbsent: CheckBox = view.findViewById(R.id.cbAbsent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val student = students[position]
        holder.studentName.text = "${student.firstName} ${student.lastName}"

        // Set initial state
        holder.cbPresent.isChecked = student.attendanceStatus == "P"
        holder.cbLate.isChecked = student.attendanceStatus == "L"
        holder.cbAbsent.isChecked = student.attendanceStatus == "A"

        // Set listeners
        holder.cbPresent.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                holder.cbLate.isChecked = false
                holder.cbAbsent.isChecked = false
                onAttendanceChanged(position, "P")
            }
        }

        holder.cbLate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                holder.cbPresent.isChecked = false
                holder.cbAbsent.isChecked = false
                onAttendanceChanged(position, "L")
            }
        }

        holder.cbAbsent.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                holder.cbPresent.isChecked = false
                holder.cbLate.isChecked = false
                onAttendanceChanged(position, "A")
            }
        }
    }

    override fun getItemCount() = students.size
}