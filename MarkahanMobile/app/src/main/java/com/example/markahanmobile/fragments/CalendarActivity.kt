package com.example.markahanmobile.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.data.Event
import com.example.markahanmobile.helper.EventAdapter
import com.example.markahanmobile.utils.toast
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import com.example.markahanmobile.R

class CalendarActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var calendarView: CalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private val events = mutableListOf<Event>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Logout listener
        val logoutView = navView.findViewById<TextView>(R.id.nav_logout)
        logoutView?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setIcon(R.drawable.warningsign)
                .setPositiveButton("Logout") { _, _ ->
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Navigation drawer
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dash -> startActivity(Intent(this, DashboardActivity::class.java))
                R.id.nav_list -> startActivity(Intent(this, StudentsListActivity::class.java))
                R.id.nav_att -> startActivity(Intent(this, AttendanceActivity::class.java))
                R.id.nav_grades -> startActivity(Intent(this, GradesActivity::class.java))
                R.id.nav_journal -> startActivity(Intent(this, JournalActivity::class.java))
            }
            drawerLayout.closeDrawers()
            true
        }

        // Calendar and RecyclerView setup
        calendarView = findViewById(R.id.calendarView)
        recyclerView = findViewById(R.id.eventsRecyclerView)

        eventAdapter = EventAdapter(events, ::editEvent, ::deleteEvent)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = eventAdapter

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%02d/%02d/%04d", month + 1, dayOfMonth, year)
            showAddEventDialog(selectedDate)
        }
    }

    private fun showAddEventDialog(date: String, existingEvent: Event? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_event, null)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val input = dialogView.findViewById<EditText>(R.id.eventNameInput)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmit)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        dialogTitle.text = if (existingEvent != null) "Edit Event on $date" else "Add Event on $date"

        input.setText(existingEvent?.description ?: "")
        btnSubmit.text = if (existingEvent != null) "Update Event" else "Add Event"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnSubmit.setOnClickListener {
            val desc = input.text.toString().trim()
            if (desc.isEmpty()) {
                toast("Please enter event name")
            } else {
                if (existingEvent != null) {
                    showUpdateConfirmationDialog(
                        originalEvent = existingEvent,
                        newDescription = desc,
                        parentDialog = dialog
                    )
                } else {
                    val newEvent = Event(
                        eventID = System.currentTimeMillis().toString(),
                        date = date,
                        description = desc
                    )
                    events.add(newEvent)
                    eventAdapter.notifyItemInserted(events.size - 1)
                    toast("Event added")
                    dialog.dismiss()
                }
            }
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showUpdateConfirmationDialog(
        originalEvent: Event,
        newDescription: String,
        parentDialog: AlertDialog
    ) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Update")
            .setMessage("Are you sure you want to update this event?")
            .setPositiveButton("Update") { _, _ ->
                val updatedEvent = originalEvent.copy(description = newDescription)
                val index = events.indexOfFirst { it.eventID == originalEvent.eventID }
                if (index != -1) {
                    events[index] = updatedEvent
                    eventAdapter.updateEvents(events)
                    toast("Event updated")
                    parentDialog.dismiss()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun editEvent(event: Event) {
        showAddEventDialog(event.date, event)
    }

    private fun deleteEvent(event: Event) {
        AlertDialog.Builder(this)
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete this event?")
            .setPositiveButton("Delete") { _, _ ->
                val index = events.indexOfFirst { it.eventID == event.eventID }
                if (index != -1) {
                    events.removeAt(index)
                    eventAdapter.updateEvents(events)
                    toast("Event deleted")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
