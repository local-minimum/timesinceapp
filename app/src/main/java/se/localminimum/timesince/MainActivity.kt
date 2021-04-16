package se.localminimum.timesince

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.widget.TextView
import java.util.*
import java.util.concurrent.Callable
import kotlin.math.hypot

const val MAX_DURATION_VALUE = 99
const val MAX_STAY_TOUCH_SPEED = 5 // px / s
const val NEW_TIME_SINCE_EVENT_TOUCH_DURATION = 2000f // millis
const val CHECK_STATUS_FREQUENCY_HIGH = 50L
const val CHECK_STATUS_FREQUENCY_LOW = 150L

class MainActivity : AppCompatActivity()  {
    private var mVelocityTracker: VelocityTracker? = null
    private var registerNewTimeSinceEvent: Boolean = false
    private var touchStart: Long = 0

    private var timeSinceEvent: TimeSinceEvent? = null

    private val mainHandler: Handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btnListTrackers).setOnClickListener { onClickListTrackers() }

        this.setTimeSince(interestingTimeSinceEvent())
        initLiveMainHandler()
    }

    private fun onClickListTrackers() {
        // Log.d("NAV", "list trackers")
        //TODO: swap views
    }

    private fun initLiveMainHandler() {
        mainHandler.post(object : Runnable {
            override fun run() {
                checkStatus()
                mainHandler.postDelayed(
                        this,
                        when (registerNewTimeSinceEvent) {
                            true -> CHECK_STATUS_FREQUENCY_HIGH
                            false -> CHECK_STATUS_FREQUENCY_LOW
                        }
                )
            }
        })
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
                }
                MotionEvent.ACTION_UP -> {
                    mVelocityTracker?.recycle()
                    mVelocityTracker = null
                    registerNewTimeSinceEvent = false
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
            //TODO: Dispatch to db
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

    private fun getStationaryPressDuration(): Float {
        return when(registerNewTimeSinceEvent) {
            false -> 0f
            true -> (System.currentTimeMillis() - touchStart).toFloat() / NEW_TIME_SINCE_EVENT_TOUCH_DURATION
        }
    }

    private fun checkStatus() {
        val durationView = findViewById<TextView>(R.id.durationText)
        val durationPlusView = findViewById<TextView>(R.id.durationTextPlus)
        val stationaryTouch = getStationaryPressDuration()

        if (stationaryTouch > 1) {
            onNewTimeSinceEvent()
            durationView.alpha = 1f
            durationPlusView.alpha = 1f
        } else if (stationaryTouch > 0) {
            durationView.alpha = 1 - stationaryTouch
            durationPlusView.alpha = 1 - stationaryTouch
        } else {
            durationView.alpha = 1f
            durationPlusView.alpha = 1f
            timeSinceEvent?.let { setTimeSince(it) }
        }
    }
}