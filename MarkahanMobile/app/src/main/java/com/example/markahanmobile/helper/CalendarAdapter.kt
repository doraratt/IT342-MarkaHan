package com.example.markahanmobile.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.R
import com.example.markahanmobile.data.Calendar
import java.time.format.DateTimeFormatter
import java.util.Locale

class CalendarAdapter(
    private var events: List<Calendar>,
    private val onEditClick: (Calendar) -> Unit,
    private val onDeleteClick: (Calendar) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventDate: TextView = itemView.findViewById(R.id.eventDate)
        val eventDesc: TextView = itemView.findViewById(R.id.eventDesc)
        val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.getDefault())
        holder.eventDate.text = event.date.format(formatter)
        holder.eventDesc.text = event.eventDescription

        holder.btnEdit.setOnClickListener { onEditClick(event) }
        holder.btnDelete.setOnClickListener { onDeleteClick(event) }
    }

    override fun getItemCount(): Int = events.size

    fun updateEvents(newEvents: List<Calendar>) {
        events = newEvents
        notifyDataSetChanged()
    }
}