package com.example.markahanmobile.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.example.markahanmobile.R

class ResetPassActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_pass)

        val newPasswordEditText: EditText = findViewById(R.id.new_pass)
        val confirmNewPasswordEditText: EditText = findViewById(R.id.confirmnew_pass)
        val btnUpdate: Button = findViewById(R.id.recover_password)
        val showNewPasswordCheckBox: CheckBox = findViewById(R.id.checkbox_show_new_password)
        val showConfirmNewPasswordCheckBox: CheckBox = findViewById(R.id.checkbox_show_confirm_new_password)

        // Toggle new password visibility
        showNewPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                newPasswordEditText.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                newPasswordEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

        // Toggle confirm new password visibility
        showConfirmNewPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                confirmNewPasswordEditText.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                confirmNewPasswordEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
        btnUpdate.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString().trim()
            val confirmPassword = confirmNewPasswordEditText.text.toString().trim()

            // Check if new password is empty
            if (newPassword.isEmpty()) {
                newPasswordEditText.error = "Enter new password"
                newPasswordEditText.requestFocus()
                return@setOnClickListener
            }

            // Check if confirm password is empty
            if (confirmPassword.isEmpty()) {
                confirmNewPasswordEditText.error = "Confirm your password"
                confirmNewPasswordEditText.requestFocus()
                return@setOnClickListener
            }

            // Check if new password is less than 6 characters
            if (newPassword.length < 8) {
                newPasswordEditText.error = "Password must be at least 8 characters"
                newPasswordEditText.requestFocus()
                return@setOnClickListener
            }

            // Check if passwords match
            if (newPassword != confirmPassword) {
                confirmNewPasswordEditText.error = "Passwords do not match"
                confirmNewPasswordEditText.requestFocus()
                return@setOnClickListener
            }

            // Simulate a successful password update
            Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()

            // Navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
