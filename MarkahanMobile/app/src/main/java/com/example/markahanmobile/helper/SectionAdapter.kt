package com.example.markahanmobile.helper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.R

class SectionAdapter(
    private var sections: List<String>,
    private val onSectionClick: (String) -> Unit,
) : RecyclerView.Adapter<SectionAdapter.SectionViewHolder>() {

    private var selectedPosition = 0

    companion object{
        const val ALL_SECTIONS = "All"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_section, parent, false)
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        val section = sections[position]
        holder.sectionName.text = section

        holder.itemView.isSelected = position == selectedPosition

        holder.itemView.setOnClickListener {
            val previousSelected = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousSelected)
            notifyItemChanged(position)

            onSectionClick(section)
        }
    }

    override fun getItemCount(): Int = sections.size

    fun updateSections(newSections: List<String>) {
        sections = newSections
        notifyDataSetChanged()
    }

    class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sectionName: TextView = itemView.findViewById(R.id.sectionName)
    }

    fun setSelectedPosition(position: Int){
        val previousSelected = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousSelected)
        notifyItemChanged(position)
    }
}