package se.localminimum.timesince

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TimeSinceTracker(
    @PrimaryKey val tId: Int,
    @ColumnInfo(name = "name") val name: String
)
