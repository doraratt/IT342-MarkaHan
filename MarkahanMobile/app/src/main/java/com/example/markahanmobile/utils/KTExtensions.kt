package com.example.markahanmobile.utils

import android.app.Activity
import android.widget.Toast

fun Activity.toast(msg: String){
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}