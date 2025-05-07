package com.example.markahanmobile

import android.app.Application
import com.example.markahanmobile.data.DataStore
import com.example.markahanmobile.helper.ApiClient

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
        DataStore.init(this)
    }
}