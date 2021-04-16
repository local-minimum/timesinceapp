package se.localminimum.timesince

data class TimeSince(val name: String, val duration: Int, val unit: String)

fun createTimeSince(event: TimeSinceEvent): TimeSince {
    val duration = when (val minutes: Long = (System.currentTimeMillis() - event.time) / 60000) {
        in 61L..Long.MAX_VALUE -> when (val hours: Long = minutes / 60) {
            in 25L..Long.MAX_VALUE -> when (val days: Long = hours / 24) {
                1L -> Pair(days, "day")
                else -> Pair(days, "days")
            }
            1L -> Pair(hours, "hour")
            else -> Pair(hours, "hours")
        }
        1L -> Pair(minutes, "minute")
        else -> Pair(minutes, "minutes")
    }
    return TimeSince(event.name, duration.first.toInt(), duration.second)
}
