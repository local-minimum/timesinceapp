package se.localminimum.timesince

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TimeSinceTracker::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timeSinceTrackerDao(): TimeSinceTrackerDAO
}