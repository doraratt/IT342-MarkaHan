package com.example.markahanmobile.helper

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.R
import com.example.markahanmobile.data.Journal
import com.example.markahanmobile.fragments.JournalActivity
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class JournalAdapter(
    private var journals: List<Journal>,
    private val onJournalClick: (Journal) -> Unit,
    private val onEditClick: (Journal) -> Unit,
    private val onDeleteClick: (Journal) -> Unit
) : RecyclerView.Adapter<JournalAdapter.JournalViewHolder>() {

    private val dateFormat = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())

    inner class JournalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateText: TextView = view.findViewById(R.id.journalDate)
        val entryText: TextView = view.findViewById(R.id.journalEntry)
        val editButton: ImageView = view.findViewById(R.id.editJournalButton)
        val deleteButton: ImageView = view.findViewById(R.id.deleteJournalButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_journal, parent, false)
        return JournalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        val journal = journals[position]
        Log.d("JournalAdapter", "Binding journal at position $position: entry=${journal.entry}")
        holder.dateText.text = dateFormat.format(journal.date)
        holder.entryText.text = journal.entry

        val context = holder.itemView.context as? JournalActivity
        holder.itemView.setOnClickListener {
            context?.closeNavigationDrawer()
            onJournalClick(journal)
        }
        holder.editButton.setOnClickListener {
            Log.d("JournalAdapter", "Edit button clicked for journal: ${journal.entry}")
            context?.closeNavigationDrawer()
            onEditClick(journal)
            true
        }
        holder.deleteButton.setOnClickListener {
            context?.closeNavigationDrawer()
            onDeleteClick(journal)
            true
        }
    }

    override fun getItemCount() = journals.size

    fun updateJournals(newJournals: List<Journal>) {
        journals = newJournals
        notifyDataSetChanged()
    }
}