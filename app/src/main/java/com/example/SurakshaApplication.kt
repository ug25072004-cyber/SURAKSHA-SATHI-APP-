package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class SurakshaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val app = FirebaseApp.initializeApp(this)
                if (app != null) {
                    Log.d("SurakshaApp", "FirebaseApp initialized successfully: ${app.name}")
                } else {
                    initializeFallbackFirebase()
                }
            }
        } catch (e: Exception) {
            Log.w("SurakshaApp", "Automatic Firebase init deferred, attempting programmatic config: ${e.message}")
            initializeFallbackFirebase()
        }
    }

    private fun initializeFallbackFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setProjectId("suraksha-sathi-app-ebb0b")
                    .setApplicationId("1:461522556814:android:0d6945a8e1b1d28eef60bb")
                    .setApiKey("AIzaSySurakshaSathiFirebaseClientKey001")
                    .setStorageBucket("suraksha-sathi-app-ebb0b.firebasestorage.app")
                    .setGcmSenderId("461522556814")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("SurakshaApp", "FirebaseApp initialized with programmatic options")
            }
        } catch (fallbackError: Exception) {
            Log.e("SurakshaApp", "Failed to initialize FirebaseApp with fallback options", fallbackError)
        }
    }
}
