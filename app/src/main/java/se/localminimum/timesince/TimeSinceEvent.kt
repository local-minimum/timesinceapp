package se.localminimum.timesince

import java.util.*

data class TimeSinceEvent(val id: UUID, val name: String, val time: Long)

fun interestingTimeSinceEvent(): TimeSinceEvent {
    return TimeSinceEvent(
            UUID.randomUUID(),
            "Eventlessness",
            System.currentTimeMillis()
    )
}