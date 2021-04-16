package se.localminimum.timesince

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.VelocityTracker
import android.widget.TextView
import java.util.*
import kotlin.math.hypot

const val MAX_DURATION_VALUE = 99
const val MAX_STAY_TOUCH_SPEED = 3 // px / s
const val NEW_TIME_SINCE_EVENT_TOUCH_DURATION = 2000 // millis


class MainActivity : AppCompatActivity() {
    private var mVelocityTracker: VelocityTracker? = null
    private var registerNewTimeSinceEvent: Boolean = false
    private var touchStart: Long = 0

    private var timeSinceEvent: TimeSinceEvent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val event = TimeSinceEvent(
                UUID.randomUUID(),
                "Duktighet",
                System.currentTimeMillis() - 60000L * 6500L
        )
        this.setTimeSince(event)
    }

    @SuppressLint("Recycle")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!super.onTouchEvent(event)) {
            when (event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    registerNewTimeSinceEvent = true
                    mVelocityTracker?.clear()
                    mVelocityTracker = mVelocityTracker ?: VelocityTracker.obtain()
                    mVelocityTracker?.addMovement(event)
                    touchStart = System.currentTimeMillis()
                }
                MotionEvent.ACTION_MOVE -> {
                    mVelocityTracker?.apply {
                        val pointerId: Int = event.getPointerId(event.actionIndex)
                        addMovement(event)
                        computeCurrentVelocity(1000)
                        val speed = hypot(getXVelocity(pointerId), getYVelocity(pointerId))
                        registerNewTimeSinceEvent = if (registerNewTimeSinceEvent) speed < MAX_STAY_TOUCH_SPEED else registerNewTimeSinceEvent
                    }
                    if (registerNewTimeSinceEvent && System.currentTimeMillis() - touchStart > NEW_TIME_SINCE_EVENT_TOUCH_DURATION) {
                        onNewTimeSinceEvent()
                    }

                }
                MotionEvent.ACTION_UP -> {
                    mVelocityTracker?.recycle()
                    mVelocityTracker = null
                    if (registerNewTimeSinceEvent) {
                        onNewTimeSinceEvent()
                    }
                }
            }
        }
        return true
    }

    private fun onNewTimeSinceEvent() {
        registerNewTimeSinceEvent = false
        timeSinceEvent?.apply {
            val event = TimeSinceEvent(id, name, System.currentTimeMillis())
            setTimeSince(event)
        }

    }

    private fun setTimeSince(event: TimeSinceEvent) {
        timeSinceEvent = event

        // Get widgets
        val taskNameView = findViewById<TextView>(R.id.taskName)
        val durationView = findViewById<TextView>(R.id.durationText)
        val durationUnitView = findViewById<TextView>(R.id.durationUnit)
        val durationPlusView = findViewById<TextView>(R.id.durationTextPlus)

        // Calculate values
        val timeSince = createTimeSince(event)
        val durationText = when (val duration = timeSince.duration) {
            in 0..MAX_DURATION_VALUE -> "$duration"
            else -> "$MAX_DURATION_VALUE"
        }

        // Update widgets
        taskNameView.text = timeSince.name
        durationPlusView.visibility = when (timeSince.duration > MAX_DURATION_VALUE) {
            true -> android.view.View.VISIBLE
            false -> android.view.View.INVISIBLE
        }
        durationView.text = durationText
        durationUnitView.text = timeSince.unit
    }
}