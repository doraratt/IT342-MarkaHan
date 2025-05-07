package com.example.markahanmobile.fragments

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.markahanmobile.R
import com.example.markahanmobile.data.DataStore

class LoginActivity : AppCompatActivity() {

    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpText: TextView
    private lateinit var forgotPasswordText: TextView
    private lateinit var showPasswordCheckBox: CheckBox
    private lateinit var googleSignInButton: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailField = findViewById(R.id.edit_email)
        passwordField = findViewById(R.id.edit_password)
        loginButton = findViewById(R.id.btn_login)
        signUpText = findViewById(R.id.btn_signup)
        forgotPasswordText = findViewById(R.id.forgot_password)
        showPasswordCheckBox = findViewById(R.id.checkbox_show_password)

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", -1)
        if (userId != -1) {
            startActivity(Intent(this, StudentsListActivity::class.java))
            finish()
            return
        }

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            DataStore.login(email, password) { user, success ->
                if (success && user != null) {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    val targetActivity = intent.getStringExtra("TARGET_ACTIVITY")
                    val targetIntent = when (targetActivity) {
                        "JournalActivity" -> Intent(this, JournalActivity::class.java)
                        else -> Intent(this, DashboardActivity::class.java)
                    }
                    startActivity(targetIntent)
                    finish()
                } else {
                    Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        signUpText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        forgotPasswordText.setOnClickListener {
            val intent = Intent(this, ForgotPassActivity::class.java)
            startActivity(intent)
        }

        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            passwordField.setSelection(passwordField.text.length)
        }
    }
}