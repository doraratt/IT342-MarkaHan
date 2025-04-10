package com.example.markahanmobile.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.example.markahanmobile.R

class CheckEmailActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_email)

        val btnGTEmail: Button = findViewById(R.id.gotoemail)

        btnGTEmail.setOnClickListener {
            val intent = Intent(this, ResetPassActivity::class.java)
            startActivity(intent)
        }
    }
}