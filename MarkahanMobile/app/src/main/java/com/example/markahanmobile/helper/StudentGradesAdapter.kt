package com.example.markahanmobile.helper

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.R
import com.example.markahanmobile.data.DataStore
import com.example.markahanmobile.data.Student
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

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
                Log.d("StudentGradesAdapter", "Binding student at position $position: ${student.firstName} ${student.lastName}, Section=${student.section}, GradeId=${student.grade?.gradeId}, Remarks=${student.grade?.remarks}")
                holder.studentName.text = "${student.lastName}, ${student.firstName}"
                val remarks = student.grade?.remarks ?: "No Remarks"
                holder.studentRemarks.text = remarks
                // Set color based on remarks value
                val context = holder.itemView.context
                holder.studentRemarks.setTextColor(
                    when (remarks) {
                        "PASSED" -> ContextCompat.getColor(context, R.color.remarks_passed)
                        "FAILED" -> ContextCompat.getColor(context, R.color.remarks_failed)
                        else -> ContextCompat.getColor(context, R.color.remarks_none) // Covers "No Remarks" or unexpected values
                    }
                )
                holder.viewGradesBtn.setOnClickListener {
                    // Sync grades before navigating to ensure the latest data
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            withContext(Dispatchers.IO) {
                                val gradeDeferred = CompletableDeferred<Boolean>()
                                DataStore.syncGrades(student.userId) { success ->
                                    Log.d("StudentGradesAdapter", "viewGrades: Grade sync success=$success for studentId=${student.studentId}")
                                    gradeDeferred.complete(success)
                                }
                                withTimeoutOrNull(5000L) { gradeDeferred.await() }
                            }
                            // Fetch the latest grade after sync
                            val updatedGrade = DataStore.getGradeByStudent(student.studentId)
                            val updatedStudent = student.copy(grade = updatedGrade)
                            Log.d("StudentGradesAdapter", "viewGrades: Fetched grade for studentId=${student.studentId}, GradeId=${updatedGrade?.gradeId}, Remarks=${updatedGrade?.remarks}")
                            onViewGrades(updatedStudent)
                        } catch (e: Exception) {
                            Log.e("StudentGradesAdapter", "viewGrades: Error syncing grades for studentId=${student.studentId}: ${e.message}", e)
                            onViewGrades(student) // Proceed with existing data
                        }
                    }
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