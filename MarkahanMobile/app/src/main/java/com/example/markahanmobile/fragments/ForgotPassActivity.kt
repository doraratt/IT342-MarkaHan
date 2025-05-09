package com.example.markahanmobile.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.markahanmobile.R

class ForgotPassActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pass)

        val btnRecover: Button = findViewById(R.id.recover_password)
        val emailEditText: EditText = findViewById(R.id.edit_email)

        btnRecover.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                emailEditText.error = "Email is required"
                emailEditText.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.error = "Enter a valid email"
                emailEditText.requestFocus()
                return@setOnClickListener
            }

            // Simulate email validation success
            Toast.makeText(this,"Reset link sent to $email", Toast.LENGTH_SHORT).show()

            // Now navigate to the next activity
            val intent = Intent(this, CheckEmailActivity::class.java)
            startActivity(intent)
        }
    }
}
