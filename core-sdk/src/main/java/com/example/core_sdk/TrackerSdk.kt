package com.example.core_sdk

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.example.coresdk.network.ApiClient
import com.example.coresdk.network.CryptoUtils
import com.example.coresdk.network.PayloadRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

object TrackerSdk {
    private var isInitialized = false
    private lateinit var deviceId: String
    private const val TAG = "TrackerSdk"

    // Coroutine scope for handling background network calls
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Initializes the SDK. Must be called before any other methods.
     */
    @SuppressLint("HardwareIds")
    fun init(context: Context) {
        if (isInitialized) {
            SdkLogger.w(TAG, "SDK is already initialized.")
            return
        }

        try {
            // Generate/Retrieve Unique Device ID
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            deviceId = androidId ?: UUID.randomUUID().toString()

            isInitialized = true
            SdkLogger.i(TAG, "SDK Initialized successfully with Device ID: $deviceId")
        } catch (e: Exception) {
            SdkLogger.e(TAG, "Failed to initialize SDK", e)
            throw SdkInitializationException("Initialization failed: ${e.message}")
        }
    }

    // Public Method 1: Get Device Info
    fun getSessionInfo(): String {
        checkInitialization()
        return "DeviceID: $deviceId, SessionActive: true"
    }

    // Public Method 2: Track an Event Securely
    fun trackAction(actionName: String) {
        checkInitialization()
        SdkLogger.i(TAG, "Action triggered: $actionName. Preparing secure payload...")

        // Launch a background coroutine so we don't block the main UI thread
        scope.launch {
            try {
                // 1. Create the raw JSON payload
                val rawPayload = "{\"device\":\"$deviceId\", \"action\":\"$actionName\"}"

                // 2. Encrypt the payload using AES
                val encryptedPayload = CryptoUtils.encrypt(rawPayload)
                SdkLogger.i(TAG, "Payload encrypted successfully.")

                // 3. Send to API via Retrofit
                val response = ApiClient.api.sendData(PayloadRequest(encryptedPayload))
                SdkLogger.i(TAG, "API Success: Status=${response.status}, Message=${response.message}")

            } catch (e: Exception) {
                SdkLogger.e(TAG, "API call failed after retries", e)
            }
        }
    }

    private fun checkInitialization() {
        if (!isInitialized) {
            throw SdkNotInitializedException("You must call TrackerSdk.init(context) first.")
        }
    }
}

// Custom Exceptions
class SdkInitializationException(message: String) : Exception(message)
class SdkNotInitializedException(message: String) : Exception(message)

// Internal Logging Mechanism
internal object SdkLogger {
    var isEnabled = true

    fun i(tag: String, message: String) { if (isEnabled) Log.i(tag, message) }
    fun w(tag: String, message: String) { if (isEnabled) Log.w(tag, message) }
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (isEnabled) Log.e(tag, message, throwable)
    }
}