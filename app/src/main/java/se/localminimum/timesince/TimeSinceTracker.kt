package se.localminimum.timesince

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [ Index(value = [ "name" ], unique = true) ])
data class TimeSinceTracker(
    @PrimaryKey(autoGenerate = true) val tId: Int? = null,
    @ColumnInfo(name = "name") val name: String
)
