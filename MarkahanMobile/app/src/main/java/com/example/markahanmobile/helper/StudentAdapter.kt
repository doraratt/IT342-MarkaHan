package com.example.markahanmobile.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.R
import com.example.markahanmobile.data.Student

class StudentAdapter(
    private var items: List<Any>,
    private val onEdit: (Student) -> Unit,
    private val onArchiveOrUnarchive: (Student) -> Unit,
    private val onDelete: ((Student) -> Unit)? = null,
    private val showArchived: Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_STUDENT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) VIEW_TYPE_HEADER else VIEW_TYPE_STUDENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_student, parent, false)
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
                holder.fullName.text = "${student.lastName}, ${student.firstName} "
                holder.section.text = student.section
                holder.gradeLevel.text = student.gradeLevel

                if (showArchived) {
                    // In archived view: show unarchive and delete buttons
                    holder.archiveButton.visibility = View.VISIBLE
                    holder.archiveButton.setImageResource(R.drawable.unarchivestudent)
                    holder.archiveButton.contentDescription = "Unarchive"
                    holder.archiveButton.setColorFilter(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.unarchive_tint
                        )
                    )
                    holder.archiveButton.setOnClickListener { onArchiveOrUnarchive(student) }

                    // Show delete button (replacing edit button)
                    holder.editButton.setImageResource(R.drawable.delete)
                    holder.editButton.contentDescription = "Delete"
                    holder.editButton.setColorFilter(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.delete_tint
                        )
                    )
                    holder.editButton.setOnClickListener { onDelete?.invoke(student) }
                } else {
                    // In normal view: show edit and archive buttons
                    holder.editButton.setImageResource(R.drawable.edit)
                    holder.editButton.contentDescription = "Edit"
                    holder.editButton.setColorFilter(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.edit_tint // Assume a color resource for edit tint
                        )
                    )
                    holder.editButton.setOnClickListener { onEdit(student) }

                    holder.archiveButton.visibility = View.VISIBLE
                    holder.archiveButton.setImageResource(R.drawable.archivestudent)
                    holder.archiveButton.contentDescription = "Archive"
                    holder.archiveButton.setColorFilter(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.archive_tint
                        )
                    )
                    holder.archiveButton.setOnClickListener { onArchiveOrUnarchive(student) }
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newStudents: List<Any>) {
        val groupedItems = mutableListOf<Any>()

        // Group students by gender and sort alphabetically
        val maleStudents = newStudents.filter { it is Student && (it as Student).gender == "Male" }
            .sortedWith(compareBy({ (it as Student).lastName }, { (it as Student).firstName }))
        val femaleStudents = newStudents.filter { it is Student && (it as Student).gender == "Female" }
            .sortedWith(compareBy({ (it as Student).lastName }, { (it as Student).firstName }))

        // Add Male Students section if there are any
        if (maleStudents.isNotEmpty()) {
            groupedItems.add("Male Students")
            groupedItems.addAll(maleStudents)
        }

        // Add Female Students section if there are any
        if (femaleStudents.isNotEmpty()) {
            groupedItems.add("Female Students")
            groupedItems.addAll(femaleStudents)
        }

        items = groupedItems
        notifyDataSetChanged()
    }

    class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fullName: TextView = view.findViewById(R.id.txtStudentName)
        val section: TextView = view.findViewById(R.id.txtStudentSection)
        val gradeLevel: TextView = view.findViewById(R.id.txtGradeLevel)
        val editButton: ImageView = view.findViewById(R.id.btnEditStudent)
        val archiveButton: ImageView = view.findViewById(R.id.btnArchiveStudent)
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerTitle: TextView = view.findViewById(R.id.headerTitle)
    }
}