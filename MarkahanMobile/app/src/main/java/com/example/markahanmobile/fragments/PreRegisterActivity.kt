package com.example.markahanmobile.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.example.markahanmobile.R

class PreRegisterActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_register)

        val btnProceed: Button = findViewById(R.id.btn_proceed)

        btnProceed.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}