package com.example.markahanmobile.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.R
import com.example.markahanmobile.data.Journal
import java.text.SimpleDateFormat
import java.util.*

class JournalAdapter(
    private var journals: List<Journal>,
    private val onJournalClick: (Journal) -> Unit,
    private val onEditClick: (Journal) -> Unit,
    private val onDeleteClick: (Journal) -> Unit
) : RecyclerView.Adapter<JournalAdapter.JournalViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

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
        holder.dateText.text = dateFormat.format(journal.date)
        holder.entryText.text = journal.journalEntry

        holder.itemView.setOnClickListener { onJournalClick(journal) }
        holder.editButton.setOnClickListener { onEditClick(journal) }
        holder.deleteButton.setOnClickListener { onDeleteClick(journal) }
    }

    override fun getItemCount() = journals.size

    fun updateJournals(newJournals: List<Journal>) {
        journals = newJournals
        notifyDataSetChanged()
    }
}