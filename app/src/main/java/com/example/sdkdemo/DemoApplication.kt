package com.example.sdkdemo

import android.app.Application
import com.example.core_sdk.TrackerSdk // Importing your engine!

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the SDK the moment the app boots
        TrackerSdk.init(this)
    }
}