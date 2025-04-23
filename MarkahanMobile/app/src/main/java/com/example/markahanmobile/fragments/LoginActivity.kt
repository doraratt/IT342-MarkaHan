package com.example.markahanmobile.fragments

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.markahanmobile.R
import com.example.markahanmobile.utils.toast

class LoginActivity : AppCompatActivity() {

    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpText: TextView
    private lateinit var forgotPasswordText: TextView
    private lateinit var showPasswordCheckBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Bind views
        emailField = findViewById(R.id.edit_email)
        passwordField = findViewById(R.id.edit_password)
        loginButton = findViewById(R.id.btn_login)
        signUpText = findViewById(R.id.btn_signup)
        forgotPasswordText = findViewById(R.id.forgot_password)
        showPasswordCheckBox = findViewById(R.id.checkbox_show_password)

        // Login button click
        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailField.error = "Enter a valid email"
                emailField.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty() || password.length < 8) {
                passwordField.error = "Password must be at least 6 characters"
                passwordField.requestFocus()
                return@setOnClickListener
            }

            // Call backend login API (placeholder)
            performLogin(email, password)
        }

        // Navigate to Sign Up
        signUpText.setOnClickListener {
            val intent = Intent(this, PreRegisterActivity::class.java)
            startActivity(intent)
        }

        // Handle Forgot Password
        forgotPasswordText.setOnClickListener {
            val intent = Intent(this, ForgotPassActivity::class.java)
            startActivity(intent)
        }

        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Show password
                passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                // Hide password
                passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            // Move the cursor to the end of the text after changing input type
            passwordField.setSelection(passwordField.text.length)
        }
    }

    private fun performLogin(email: String, password: String) {
        // TODO: Replace with real API call using Retrofit
        toast("Logging in with $email")
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
    }
}
