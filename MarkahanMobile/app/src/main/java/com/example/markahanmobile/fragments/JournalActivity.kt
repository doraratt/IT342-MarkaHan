package com.example.markahanmobile.fragments

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.R
import com.example.markahanmobile.data.DataStore
import com.example.markahanmobile.data.Journal
import com.example.markahanmobile.helper.JournalAdapter
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class JournalActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var noJournalsText: TextView
    private lateinit var adapter: JournalAdapter
    private val journalList = mutableListOf<Journal>()
    private var userId: Int = 0
    private val TAG = "JournalActivity"

    fun closeNavigationDrawer() {
        drawerLayout.closeDrawers()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)

        DataStore.init(this) // Initialize DataStore with Context

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        recyclerView = findViewById(R.id.journalRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        noJournalsText = findViewById(R.id.noJournalsText)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        setupNavigation()
        setupRecyclerView()
        setupButtons()
        loadJournals()
    }

    private fun setupNavigation() {
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
                R.id.nav_dash -> startActivity(Intent(this, DashboardActivity::class.java))
                R.id.nav_cal -> startActivity(Intent(this, CalendarActivity::class.java))
                R.id.nav_list -> startActivity(Intent(this, StudentsListActivity::class.java))
                R.id.nav_att -> startActivity(Intent(this, AttendanceActivity::class.java))
                R.id.nav_grades -> startActivity(Intent(this, GradesActivity::class.java))
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = JournalAdapter(
            journalList,
            ::showJournalDetails,
            ::showEditJournalDialog,
            ::deleteJournal
        )
        recyclerView.adapter = adapter

        recyclerView.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                return false
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // Unlock the drawer when the user interacts outside the RecyclerView
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        return super.onTouchEvent(event)
    }

    private fun setupButtons() {
        val journalHeader = findViewById<LinearLayout>(R.id.journalHeader)
        journalHeader.setOnClickListener { showAddJournalDialog() }
    }

    private fun loadJournals() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        userId = DataStore.getLoggedInUser()?.userId ?: sharedPreferences.getInt("userId", -1)
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        progressBar.visibility = View.VISIBLE
        noJournalsText.visibility = View.GONE
        recyclerView.visibility = View.GONE

        DataStore.syncJournals(userId) { success ->
            progressBar.visibility = View.GONE
            if (success) {
                journalList.clear()
                journalList.addAll(DataStore.getJournals())
                Log.d(TAG, "Loaded journals: ${journalList.size}, entries: ${journalList.map { it.entry }}")
                adapter.updateJournals(journalList)
                updateNoJournalsVisibility()
            } else {
                Toast.makeText(this, "Failed to load journals. Please try again.", Toast.LENGTH_SHORT).show()
                updateNoJournalsVisibility()
            }
        }
    }

    private fun updateNoJournalsVisibility() {
        noJournalsText.visibility = if (journalList.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (journalList.isEmpty()) View.GONE else View.VISIBLE
        Log.d(TAG, "updateNoJournalsVisibility: journalList.size=${journalList.size}, recyclerView.visibility=${recyclerView.visibility}")
    }

    private fun showAddJournalDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_journal, null)
        setupJournalDialog(dialogView, null)
    }

    private fun setupJournalDialog(dialogView: View, existingJournal: Journal?) {
        val entryLayout = dialogView.findViewById<TextInputLayout>(R.id.journalEntryLayout)
        val entryInput = dialogView.findViewById<TextInputEditText>(R.id.journalEntryInput)
        val dateText = dialogView.findViewById<TextView>(R.id.journalDatePicker)
        val addButton = dialogView.findViewById<Button>(R.id.addJournalEntryButton)
        val closeButton = dialogView.findViewById<ImageView>(R.id.btnClose)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.journalDialogTitle)

        var selectedDate = existingJournal?.date ?: LocalDate.now()

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
        dateText.text = selectedDate.format(formatter)
        closeButton.setOnClickListener { dialog.dismiss() }

        if (existingJournal != null) {
            dialogTitle.text = "Edit Journal Entry"
            addButton.text = "Update Entry"
            entryInput.setText(existingJournal.entry)
        } else {
            dialogTitle.text = "Add Journal Entry"
            addButton.text = "Add Entry"
        }

        dateText.setOnClickListener {
            val year = selectedDate.year
            val month = selectedDate.monthValue - 1
            val day = selectedDate.dayOfMonth

            DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                    dateText.text = selectedDate.format(formatter)
                },
                year,
                month,
                day
            ).show()
        }

        addButton.setOnClickListener {
            val entry = entryInput.text.toString().trim()
            if (entry.isEmpty()) {
                entryLayout.error = "Please enter journal content"
            } else {
                entryLayout.error = null
                if (existingJournal != null) {
                    AlertDialog.Builder(this)
                        .setTitle("Confirm Update")
                        .setMessage("Are you sure you want to update this entry?")
                        .setPositiveButton("Update") { _, _ ->
                            val updatedJournal = existingJournal.copy(
                                user = DataStore.getLoggedInUser(),
                                entry = entry,
                                date = selectedDate
                            )
                            DataStore.updateJournal(updatedJournal) { success ->
                                if (success) {
                                    loadJournals()
                                    dialog.dismiss()
                                    Toast.makeText(this, "Journal updated successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "Failed to update journal", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    val user = DataStore.getLoggedInUser()
                    val newJournal = Journal(
                        journalId = 0,
                        user = user,
                        entry = entry,
                        date = selectedDate
                    )
                    DataStore.addJournal(newJournal) { success ->
                        if (success) {
                            loadJournals()
                            dialog.dismiss()
                            Toast.makeText(this, "Journal added successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to add journal", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        dialog.show()
    }

    private fun showJournalDetails(journal: Journal) {
        try {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_journal, null)
            val entryLayout = dialogView.findViewById<TextInputLayout>(R.id.journalEntryLayout)
            val entryInput = dialogView.findViewById<TextInputEditText>(R.id.journalEntryInput)
            val dateText = dialogView.findViewById<TextView>(R.id.journalDatePicker)
            val addButton = dialogView.findViewById<Button>(R.id.addJournalEntryButton)
            val closeButton = dialogView.findViewById<ImageView>(R.id.btnClose)
            val dialogTitle = dialogView.findViewById<TextView>(R.id.journalDialogTitle)

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            dialogTitle.text = "Journal Details"
            entryInput.setText(journal.entry)
            entryInput.isEnabled = false
            entryLayout.hint = "Journal Entry"
            entryLayout.isHelperTextEnabled = false

            val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
            dateText.text = journal.date.format(formatter)
            dateText.isEnabled = false

            addButton.visibility = View.GONE
            closeButton.setOnClickListener { dialog.dismiss() }
            dialog.show()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show journal details: ${e.message}", e)
            Toast.makeText(this, "Error displaying journal details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditJournalDialog(journal: Journal) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_journal, null)
        setupJournalDialog(dialogView, journal)
    }

    private fun deleteJournal(journal: Journal) {
        AlertDialog.Builder(this)
            .setTitle("Delete Journal Entry")
            .setMessage("Are you sure you want to delete this entry?")
            .setPositiveButton("Delete") { _, _ ->
                DataStore.deleteJournal(journal.journalId) { success ->
                    if (success) {
                        loadJournals()
                        Toast.makeText(this, "Journal deleted successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to delete journal", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}