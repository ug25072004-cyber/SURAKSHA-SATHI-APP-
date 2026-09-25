package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class SurakshaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d("SurakshaApp", "FirebaseApp initialized successfully")
            }
        } catch (e: Exception) {
            Log.e("SurakshaApp", "Failed to initialize FirebaseApp", e)
        }
    }
}
