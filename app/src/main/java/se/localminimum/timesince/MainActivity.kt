package se.localminimum.timesince

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import java.util.*

const val MAX_DURATION_VALUE = 99

class MainActivity : AppCompatActivity() {

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

    private fun setTimeSince(event: TimeSinceEvent) {
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