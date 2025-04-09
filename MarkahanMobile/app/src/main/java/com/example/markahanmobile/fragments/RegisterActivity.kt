package com.example.markahanmobile.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import com.example.markahanmobile.R
import com.example.markahanmobile.utils.toast

class RegisterActivity : Activity() {

    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPhone: EditText
    private lateinit var editPassword: EditText
    private lateinit var editConfirmPassword: EditText
    private lateinit var btnSignup: Button
    private lateinit var checkboxShowPassword: CheckBox
    private lateinit var checkboxShowConfirmPassword: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize views
        editName = findViewById(R.id.edit_name)
        editEmail = findViewById(R.id.edit_email)
        editPhone = findViewById(R.id.edit_phone)
        editPassword = findViewById(R.id.edit_password)
        editConfirmPassword = findViewById(R.id.edit_confirmpassword)
        btnSignup = findViewById(R.id.btn_signup)
        checkboxShowPassword = findViewById(R.id.checkbox_show_password)
        checkboxShowConfirmPassword = findViewById(R.id.checkbox_show_confirm_password)

        // Set up the sign-up button click listener
        btnSignup.setOnClickListener {
            if (validateInput()) {
                // Proceed with user registration (e.g., store data, call API)
                toast("Account successfully created")

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
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
    }

    // Input validation function
    private fun validateInput(): Boolean {
        // Get the input values
        val name = editName.text.toString().trim()
        val email = editEmail.text.toString().trim()
        val phone = editPhone.text.toString().trim()
        val password = editPassword.text.toString().trim()
        val confirmPassword = editConfirmPassword.text.toString().trim()

        // Validate Full Name
        if (name.isEmpty()) {
            editName.error = "Full Name is required"
            editName.requestFocus()
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

        // Validate Phone Number
        if (phone.isEmpty()) {
            editPhone.error = "Phone number is required"
            editPhone.requestFocus()
            return false
        } else if (phone.length != 11 || !phone.all { it.isDigit() }) {
            editPhone.error = "Invalid phone number"
            editPhone.requestFocus()
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