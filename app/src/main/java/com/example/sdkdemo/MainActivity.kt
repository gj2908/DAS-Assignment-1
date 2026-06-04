package com.example.sdkdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core_sdk.TrackerSdk
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SdkDemoScreen()
                }
            }
        }
    }
}

@Composable
fun SdkDemoScreen() {
    var statusText by remember { mutableStateOf("Ready to test SDK.") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Tracker SDK Demo", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(40.dp))

        // Get Session Info Button
        Button(
            onClick = {
                try {
                    statusText = TrackerSdk.getSessionInfo()
                } catch (e: Exception) {
                    statusText = "Error: ${e.message}"
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Get Session Info (Local)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Trigger API Call Button
        Button(
            onClick = {
                isLoading = true
                statusText = "Encrypting and sending data..."

                coroutineScope.launch {
                    try {
                        // Calling your SDK
                        TrackerSdk.trackAction("User_Tapped_Demo_Button")

                        // Artificial delay just so you can see the loading UI state
                        delay(1500)
                        statusText = "Action Sent to SDK Backend!"
                    } catch (e: Exception) {
                        statusText = "Failed: ${e.localizedMessage}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading
        ) {
            Text("Track Event (Network)")
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Status Display Card
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}