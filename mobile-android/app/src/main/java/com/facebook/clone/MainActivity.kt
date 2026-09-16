package com.facebook.clone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.facebook.clone.bubble.BubblePermissionDialog
import com.facebook.clone.bubble.BubblePermissionHelper
import com.facebook.clone.bubble.FloatingBubbleService
import com.facebook.clone.theme.FacebookTheme
import com.facebook.clone.ui.home.HomeScreen

import androidx.activity.compose.BackHandler
import com.facebook.clone.ui.messenger.MessengerView

class MainActivity : ComponentActivity() {

    private var showPermissionDialog by mutableStateOf(false)
    private var isMessengerOpen by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check if overlay permission is granted
        if (!BubblePermissionHelper.hasOverlayPermission(this)) {
            showPermissionDialog = true
        }

        setContent {
            FacebookTheme {
                if (isMessengerOpen) {
                    BackHandler {
                        isMessengerOpen = false
                    }
                    MessengerView(
                        onNavigateBack = {
                            isMessengerOpen = false
                        }
                    )
                } else {
                    HomeScreen(
                        onNavigateToMessenger = {
                            isMessengerOpen = true
                        }
                    )
                }

                if (showPermissionDialog) {
                    BubblePermissionDialog(
                        onDismiss = { showPermissionDialog = false },
                        onGrantPermission = {
                            showPermissionDialog = false
                            BubblePermissionHelper.requestOverlayPermission(this@MainActivity)
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // If the user re-enters the app, remove the floating bubble
        FloatingBubbleService.stop(this)

        // Update dialog state if user granted permission in settings
        if (BubblePermissionHelper.hasOverlayPermission(this)) {
            showPermissionDialog = false
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Called when user minimizes the app (presses Home button or switches apps)
        if (BubblePermissionHelper.hasOverlayPermission(this)) {
            FloatingBubbleService.start(this)
        }
    }
}

