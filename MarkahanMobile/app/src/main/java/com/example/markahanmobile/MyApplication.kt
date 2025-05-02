package com.example.markahanmobile

import android.app.Application
import com.example.markahanmobile.data.DataStore

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DataStore.init(this)
    }
}