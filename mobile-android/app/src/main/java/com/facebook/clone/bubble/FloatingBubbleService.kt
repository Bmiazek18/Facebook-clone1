package com.facebook.clone.bubble

import android.animation.ValueAnimator
import android.app.ActivityOptions
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.facebook.clone.MainActivity
import kotlin.math.hypot

class FloatingBubbleService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubbleView: ComposeView? = null
    private var dismissView: ComposeView? = null
    private lateinit var bubbleParams: WindowManager.LayoutParams
    private lateinit var dismissParams: WindowManager.LayoutParams

    private var isDragging = false
    private var isOverDismissZone by mutableStateOf(false)
    private var showDismissZone by mutableStateOf(false)

    private val serviceLifecycleOwner = ServiceLifecycleOwner()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        serviceLifecycleOwner.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startForegroundNotification()
        createDismissView()
        createBubbleView()
    }

    private fun startForegroundNotification() {
        val channelId = "floating_bubble_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Messenger Floating Bubbles",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Facebook Clone Chat Head Overlay"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val openIntent = (packageManager.getLaunchIntentForPackage(packageName) ?: Intent(this, MainActivity::class.java)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Facebook Clone")
            .setContentText("Bąbelek czatu jest aktywny – dotknij, aby otworzyć")
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        startForeground(1001, notification)
    }

    private fun getScreenSize(): Point {
        val point = Point()
        val display = windowManager.defaultDisplay
        display.getSize(point)
        return point
    }

    private fun createBubbleView() {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val screenSize = getScreenSize()
        val initialX = screenSize.x - dpToPx(80)
        val initialY = (screenSize.y * 0.35f).toInt()

        bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = initialX
            y = initialY
        }

        bubbleView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(serviceLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(serviceLifecycleOwner)
            setViewTreeViewModelStoreOwner(serviceLifecycleOwner)
            setContent {
                BubbleContent()
            }
            setOnTouchListener(BubbleTouchListener())
        }

        try {
            windowManager.addView(bubbleView, bubbleParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createDismissView() {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        dismissParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            dpToPx(140),
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            x = 0
            y = 0
        }

        dismissView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(serviceLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(serviceLifecycleOwner)
            setViewTreeViewModelStoreOwner(serviceLifecycleOwner)
            setContent {
                DismissTargetContent(
                    visible = showDismissZone,
                    isHovered = isOverDismissZone
                )
            }
        }

        try {
            windowManager.addView(dismissView, dismissParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    @Composable
    private fun BubbleContent() {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            // Main Bubble Circle with Messenger Gradient & Shadow
            Surface(
                modifier = Modifier
                    .size(60.dp)
                    .shadow(10.dp, CircleShape)
                    .clip(CircleShape)
                    .border(2.5.dp, Color.White, CircleShape),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF00C6FF),
                                    Color(0xFF0078FF),
                                    Color(0xFFA033FF)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = "Messenger Bubble",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Notification Badge (Count = 1)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 2.dp)
                    .size(20.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFFFA3E3E))
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "1",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    @Composable
    private fun DismissTargetContent(visible: Boolean, isHovered: Boolean) {
        val scale by animateFloatAsState(targetValue = if (isHovered) 1.25f else 1.0f, label = "scale")
        if (visible) {
            Box(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .size(68.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size((56 * scale).dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(if (isHovered) Color(0xFFFA3E3E) else Color(0xCC242526))
                        .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss Bubble",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }

    private inner class BubbleTouchListener : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f
        private var isDragging = false

        private val gestureDetector = GestureDetector(this@FloatingBubbleService, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                openMainActivity()
                stopSelf()
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                openMainActivity()
                stopSelf()
                return true
            }
        })

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            // First let GestureDetector check for taps
            if (gestureDetector.onTouchEvent(event)) {
                return true
            }

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = bubbleParams.x
                    initialY = bubbleParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    val dist = hypot(deltaX.toDouble(), deltaY.toDouble())

                    // Only initiate drag when moved beyond threshold (10dp)
                    if (dist > dpToPx(10)) {
                        isDragging = true
                        showDismissZone = true
                        bubbleParams.x = initialX + deltaX
                        bubbleParams.y = initialY + deltaY
                        if (bubbleView?.isAttachedToWindow == true) {
                            windowManager.updateViewLayout(bubbleView, bubbleParams)
                        }

                        // Check proximity to dismiss zone at bottom center
                        val screenSize = getScreenSize()
                        val dismissCenterX = screenSize.x / 2
                        val dismissCenterY = screenSize.y - dpToPx(70)

                        val bubbleCenterX = bubbleParams.x + dpToPx(32)
                        val bubbleCenterY = bubbleParams.y + dpToPx(32)

                        val distance = hypot(
                            (bubbleCenterX - dismissCenterX).toDouble(),
                            (bubbleCenterY - dismissCenterY).toDouble()
                        )
                        isOverDismissZone = distance < dpToPx(80)
                    }
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    showDismissZone = false

                    // If it was not a drag, treat as click
                    if (!isDragging) {
                        openMainActivity()
                        stopSelf()
                        return true
                    }

                    if (isOverDismissZone) {
                        // Dropped into "X" close area -> Dismiss bubble
                        stopSelf()
                        return true
                    }

                    // Snap to nearest screen edge (left or right)
                    snapToEdge()
                    return true
                }
            }
            return false
        }
    }

    private fun snapToEdge() {
        val screenSize = getScreenSize()
        val currentX = bubbleParams.x
        val bubbleWidth = dpToPx(64)
        val targetX = if (currentX + bubbleWidth / 2 < screenSize.x / 2) {
            dpToPx(4) // Left edge
        } else {
            screenSize.x - bubbleWidth - dpToPx(4) // Right edge
        }

        val animator = ValueAnimator.ofInt(currentX, targetX).apply {
            duration = 250
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                bubbleParams.x = animation.animatedValue as Int
                if (bubbleView?.isAttachedToWindow == true) {
                    windowManager.updateViewLayout(bubbleView, bubbleParams)
                }
            }
        }
        animator.start()
    }

    private fun openMainActivity() {
        val targetIntent = (packageManager.getLaunchIntentForPackage(packageName) ?: Intent(this, MainActivity::class.java)).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            )
        }

        try {
            if (Build.VERSION.SDK_INT >= 34) {
                // Explicitly grant Background Activity Launch permission on Android 14+ / 15
                val options = ActivityOptions.makeBasic().apply {
                    setPendingIntentBackgroundActivityStartMode(
                        ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                    )
                }.toBundle()

                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    targetIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                pendingIntent.send(this, 0, null, null, null, null, options)
            } else {
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    targetIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                pendingIntent.send()
            }
        } catch (e: Exception) {
            try {
                startActivity(targetIntent)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceLifecycleOwner.onDestroy()
        try {
            if (bubbleView != null && bubbleView?.isAttachedToWindow == true) {
                windowManager.removeView(bubbleView)
            }
            if (dismissView != null && dismissView?.isAttachedToWindow == true) {
                windowManager.removeView(dismissView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun start(context: Context) {
            if (BubblePermissionHelper.hasOverlayPermission(context)) {
                val intent = Intent(context, FloatingBubbleService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingBubbleService::class.java)
            context.stopService(intent)
        }
    }
}

/**
 * Custom lifecycle owner required to host Jetpack Compose inside a Service WindowManager overlay.
 */
private class ServiceLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

    fun onCreate() {
        savedStateRegistryController.performRestore(Bundle())
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}
