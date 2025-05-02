package com.example.markahanmobile.helper

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class GenderSpinnerAdapter(context: Context, resource: Int, items: List<String>) :
    ArrayAdapter<String>(context, resource, items) {

    override fun isEnabled(position: Int): Boolean {
        // Disable the "Select Gender" option (position 0)
        return position != 0
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent) as TextView
        // Optionally, you can style the "Select Gender" option differently
        if (position == 0) {
            view.setTextColor(context.resources.getColor(android.R.color.darker_gray))
        }
        return view
    }
}