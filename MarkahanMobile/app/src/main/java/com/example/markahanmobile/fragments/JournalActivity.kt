package com.example.markahanmobile.fragments

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.markahanmobile.R
import com.example.markahanmobile.data.Journal
import com.example.markahanmobile.helper.JournalAdapter
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class JournalActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: JournalAdapter
    private val journalList = mutableListOf<Journal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 🔥 Correctly access the logout view inside NavigationView
        val logoutView = navView.findViewById<TextView>(R.id.nav_logout)
        logoutView?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setIcon(R.drawable.warningsign)
                .setPositiveButton("Yes") { _, _ ->
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Handle other menu items from drawer_menu.xml
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

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.journalRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = JournalAdapter(
            journalList,
            ::showJournalDetails,
            ::showEditJournalDialog,
            ::deleteJournal
        )
        recyclerView.adapter = adapter

        val journalHeader = findViewById<LinearLayout>(R.id.journalHeader)
        journalHeader.setOnClickListener {
            showAddJournalDialog()
        }

        loadSampleJournals()
    }

    private fun showAddJournalDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_journal, null)
        setupJournalDialog(dialogView, null)
    }

    private fun setupJournalDialog(dialogView: View, existingJournal: Journal?) {
        val entryInput = dialogView.findViewById<EditText>(R.id.journalEntryInput)
        val dateText = dialogView.findViewById<TextView>(R.id.journalDatePicker)
        val addButton = dialogView.findViewById<Button>(R.id.addJournalEntryButton)
        val closeButton = dialogView.findViewById<ImageView>(R.id.btnClose)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.journalDialogTitle)

        val calendar = Calendar.getInstance()
        var selectedDate = existingJournal?.date ?: calendar.time

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Initialize fields
        dateText.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selectedDate)
        closeButton.setOnClickListener { dialog.dismiss() }

        if (existingJournal != null) {
            // Edit mode
            dialogTitle.text = "Edit Journal Entry"
            addButton.text = "Update Entry"
            entryInput.setText(existingJournal.journalEntry)
        } else {
            // Add mode
            dialogTitle.text = "Add Journal Entry"
            addButton.text = "Add Entry"
        }

        // Date picker click
        dateText.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.time
                    dateText.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Submit logic
        addButton.setOnClickListener {
            val entry = entryInput.text.toString().trim()
            if (entry.isEmpty()) {
                entryInput.error = "Please enter journal content"
            } else {
                if (existingJournal != null) {
                    // Confirm update
                    AlertDialog.Builder(this)
                        .setTitle("Confirm Update")
                        .setMessage("Are you sure you want to update this entry?")
                        .setPositiveButton("Yes") { _, _ ->
                            val index = journalList.indexOfFirst { it.journalID == existingJournal.journalID }
                            if (index != -1) {
                                journalList[index] = existingJournal.copy(
                                    journalEntry = entry,
                                    date = selectedDate
                                )
                                adapter.notifyItemChanged(index)
                            }
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    // Add new journal
                    journalList.add(Journal(
                        journalID = UUID.randomUUID().toString(),
                        journalEntry = entry,
                        date = selectedDate
                    ))
                    adapter.notifyItemInserted(journalList.size - 1)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun showJournalDetails(journal: Journal) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_journal, null)
        val entryInput = dialogView.findViewById<EditText>(R.id.journalEntryInput)
        val dateText = dialogView.findViewById<TextView>(R.id.journalDatePicker)
        val addButton = dialogView.findViewById<Button>(R.id.addJournalEntryButton)
        val closeButton = dialogView.findViewById<ImageView>(R.id.btnClose)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.journalDialogTitle)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogTitle.text = "Journal Details"

        entryInput.setText(journal.journalEntry)
        entryInput.isEnabled = false

        dateText.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(journal.date)
        dateText.isEnabled = false

        addButton.visibility = View.GONE

        entryInput.isEnabled = false

        dateText.isEnabled = false

        closeButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showEditJournalDialog(journal: Journal) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_journal, null)
        setupJournalDialog(dialogView, journal)
    }

    private fun deleteJournal(journal: Journal) {
        AlertDialog.Builder(this)
            .setTitle("Delete Journal Entry")
            .setMessage("Are you sure you want to delete this entry?")
            .setPositiveButton("Delete") { _, _ ->
                val index = journalList.indexOfFirst { it.journalID == journal.journalID }
                if (index != -1) {
                    journalList.removeAt(index)
                    adapter.notifyItemRemoved(index)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadSampleJournals() {
        val today = Date()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.time

        journalList.addAll(listOf(
            Journal(
                journalID = UUID.randomUUID().toString(),
                journalEntry = "Today's reflection on class progress",
                date = today
            ),
            Journal(
                journalID = UUID.randomUUID().toString(),
                journalEntry = "Notes from yesterday's parent meeting",
                date = yesterday
            )
        ))
        adapter.notifyDataSetChanged()
    }

}