package com.example.markahanmobile.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.R
import com.example.markahanmobile.data.Calendar
import com.example.markahanmobile.data.DataStore
import com.example.markahanmobile.helper.CalendarAdapter
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var calendarView: CalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var noEventsText: TextView
    private lateinit var calendarAdapter: CalendarAdapter
    private val events = mutableListOf<Calendar>()
    private var userId: Int = 0
    private var selectedDate: LocalDate = LocalDate.now()
    private val TAG = "CalendarActivity"
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE // "yyyy-MM-dd"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        calendarView = findViewById(R.id.calendarView)
        recyclerView = findViewById(R.id.eventsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        noEventsText = findViewById(R.id.noEventsText)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        setupNavigation()
        setupRecyclerView()
        setupButtons()
        loadEvents()
    }

    private fun setupNavigation() {
        // Update header with user's first name
        val headerView = navView.getHeaderView(0)
        val headerFirstName = headerView.findViewById<TextView>(R.id.header_firstname)
        val user = DataStore.getLoggedInUser()
        if (user != null && user.firstName!!.isNotEmpty()) {
            headerFirstName.text = "Welcome, Teacher ${user.firstName}!"
        } else {
            headerFirstName.text = "Welcome, Teacher!"
            Log.w(TAG, "No user or first name found")
        }

        // Logout setup
        val logoutView = navView.findViewById<TextView>(R.id.nav_logout)
        logoutView?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setIcon(R.drawable.warningsign)
                .setPositiveButton("Logout") { _, _ ->
                    DataStore.logout()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dash -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    true
                }
                R.id.nav_list -> {
                    startActivity(Intent(this, StudentsListActivity::class.java))
                    true
                }
                R.id.nav_att -> {
                    startActivity(Intent(this, AttendanceActivity::class.java))
                    true
                }
                R.id.nav_grades -> {
                    startActivity(Intent(this, GradesActivity::class.java))
                    true
                }
                R.id.nav_journal -> {
                    startActivity(Intent(this, JournalActivity::class.java))
                    true
                }
                else -> {
                    Log.w(TAG, "Unexpected navigation item selected: ${menuItem.itemId}")
                    false
                }
            }.also {
                if (it) drawerLayout.closeDrawers()
            }
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        calendarAdapter = CalendarAdapter(events, ::editEvent, ::deleteEvent)
        recyclerView.adapter = calendarAdapter
    }

    private fun setupButtons() {
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            Log.d(TAG, "Date selected: $selectedDate")
            fetchEventsByDate(selectedDate)
        }

        findViewById<Button>(R.id.btnAddEvent)?.setOnClickListener {
            showAddEventDialog(selectedDate)
        }
    }

    private fun loadEvents() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        userId = DataStore.getLoggedInUser()?.userId ?: sharedPreferences.getInt("userId", -1)
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        progressBar.visibility = View.VISIBLE
        noEventsText.visibility = View.GONE
        recyclerView.visibility = View.GONE

        DataStore.syncEvents(userId) { success ->
            progressBar.visibility = View.GONE
            if (success) {
                fetchEventsByDate(selectedDate)
            } else {
                Toast.makeText(this, "Failed to load events. Please try again.", Toast.LENGTH_SHORT).show()
                updateNoEventsVisibility()
            }
        }
    }

    private fun fetchEventsByDate(date: LocalDate) {
        Log.d(TAG, "Fetching events for date: $date")
        events.clear()
        val dateString = date.format(formatter) // Convert LocalDate to String
        events.addAll(DataStore.getEventsByDate(dateString)) // Pass String to DataStore
        calendarAdapter.updateEvents(events)
        updateNoEventsVisibility()
    }

    private fun updateNoEventsVisibility() {
        noEventsText.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (events.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showAddEventDialog(date: LocalDate, existingEvent: Calendar? = null) {
        try {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_event, null)
            val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
            val input = dialogView.findViewById<EditText>(R.id.eventNameInput)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
            val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmit)

            if (input == null) {
                Log.e(TAG, "EditText with ID eventNameInput not found in dialog_add_event.xml")
                Toast.makeText(this, "Error: Invalid dialog layout", Toast.LENGTH_SHORT).show()
                return
            }

            val formatterDisplay = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.getDefault())
            val formattedDate = date.format(formatterDisplay)

            dialogTitle.text = if (existingEvent != null) "Edit Event on $formattedDate" else "Add Event on $formattedDate"
            input.setText(existingEvent?.eventDescription ?: "")
            btnSubmit.text = if (existingEvent != null) "Update Event" else "Add Event"

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            btnCancel.setOnClickListener { dialog.dismiss() }

            btnSubmit.setOnClickListener {
                val desc = input.text.toString().trim()
                if (desc.isEmpty()) {
                    input.error = "Please enter event description"
                } else {
                    input.error = null
                    if (existingEvent != null) {
                        AlertDialog.Builder(this)
                            .setTitle("Confirm Update")
                            .setMessage("Are you sure you want to update this event?")
                            .setPositiveButton("Update") { _, _ ->
                                val updatedEvent = existingEvent.copy(
                                    eventDescription = desc,
                                    date = date.format(formatter) // Convert LocalDate to String
                                )
                                DataStore.updateEvent(updatedEvent) { success ->
                                    if (success) {
                                        fetchEventsByDate(date)
                                        dialog.dismiss()
                                        Toast.makeText(this, "Event updated successfully", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this, "Failed to update event", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    } else {
                        val newEvent = Calendar(
                            calendarId = 0,
                            userId = userId,
                            eventDescription = desc,
                            date = date.format(formatter) // Convert LocalDate to String
                        )
                        DataStore.addEvent(newEvent) { success ->
                            if (success) {
                                fetchEventsByDate(date)
                                dialog.dismiss()
                                Toast.makeText(this, "Event added successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Failed to add event", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            dialog.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error in showAddEventDialog: ${e.message}", e)
            Toast.makeText(this, "Error displaying dialog", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editEvent(event: Calendar) {
        val eventDate = LocalDate.parse(event.date, formatter) // Parse String to LocalDate
        showAddEventDialog(eventDate, event)
    }

    private fun deleteEvent(event: Calendar) {
        AlertDialog.Builder(this)
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete this event?")
            .setPositiveButton("Delete") { _, _ ->
                val eventDate = LocalDate.parse(event.date, formatter) // Parse String to LocalDate
                DataStore.deleteEvent(event.calendarId) { success ->
                    if (success) {
                        fetchEventsByDate(eventDate)
                        Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to delete event", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}