package com.example.markahanmobile.fragments

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.markahanmobile.R
import com.example.markahanmobile.data.DataStore
import com.example.markahanmobile.data.User

class RegisterActivity : AppCompatActivity() {

    private lateinit var editFirstName: EditText
    private lateinit var editLastName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var editConfirmPassword: EditText
    private lateinit var btnSignup: Button
    private lateinit var checkboxShowPassword: CheckBox
    private lateinit var checkboxShowConfirmPassword: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize views
        editFirstName = findViewById(R.id.edit_first_name)
        editLastName = findViewById(R.id.edit_last_name)
        editEmail = findViewById(R.id.edit_email)
        editPassword = findViewById(R.id.edit_password)
        editConfirmPassword = findViewById(R.id.edit_confirmpassword)
        btnSignup = findViewById(R.id.btn_signup)
        checkboxShowPassword = findViewById(R.id.checkbox_show_password)
        checkboxShowConfirmPassword = findViewById(R.id.checkbox_show_confirm_password)

        // Set up the sign-up button click listener
        btnSignup.setOnClickListener {
            if (validateInput()) {
                val firstName = editFirstName.text.toString().trim()
                val lastName = editLastName.text.toString().trim()
                val email = editEmail.text.toString().trim()
                val password = editPassword.text.toString().trim()

                val user = User(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                )

                DataStore.signup(user) { createdUser, success ->
                    if (success && createdUser != null) {
                        Toast.makeText(this, "Account successfully created", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to create account", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Set up password visibility toggle
        checkboxShowPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT
            } else {
                editPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            editPassword.setSelection(editPassword.text.length)
        }

        checkboxShowConfirmPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editConfirmPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT
            } else {
                editConfirmPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            editConfirmPassword.setSelection(editConfirmPassword.text.length)
        }

        val loginPage = findViewById<TextView>(R.id.btn_login)
        loginPage.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    // Input validation function
    private fun validateInput(): Boolean {
        // Get the input values
        val firstName = editFirstName.text.toString().trim()
        val lastName = editLastName.text.toString().trim()
        val email = editEmail.text.toString().trim()
        val password = editPassword.text.toString().trim()
        val confirmPassword = editConfirmPassword.text.toString().trim()

        // Validate First Name
        if (firstName.isEmpty()) {
            editFirstName.error = "First Name is required"
            editFirstName.requestFocus()
            return false
        }

        // Validate Last Name
        if (lastName.isEmpty()) {
            editLastName.error = "Last Name is required"
            editLastName.requestFocus()
            return false
        }

        // Validate Email
        if (email.isEmpty()) {
            editEmail.error = "Email is required"
            editEmail.requestFocus()
            return false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.error = "Invalid email address"
            editEmail.requestFocus()
            return false
        }

        // Validate Password
        if (password.isEmpty()) {
            editPassword.error = "Password is required"
            editPassword.requestFocus()
            return false
        } else if (password.length < 8) {
            editPassword.error = "Password must be at least 8 characters"
            editPassword.requestFocus()
            return false
        }

        // Validate Confirm Password
        if (confirmPassword.isEmpty()) {
            editConfirmPassword.error = "Please confirm your password"
            editConfirmPassword.requestFocus()
            return false
        } else if (confirmPassword != password) {
            editConfirmPassword.error = "Passwords do not match"
            editConfirmPassword.requestFocus()
            return false
        }

        // If all validations pass, return true
        return true
    }
}